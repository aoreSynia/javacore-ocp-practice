package bai4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lõi sổ cái chuyển tiền nội bộ (Ledger Service).
 * Đảm bảo:
 * 1. Bút toán kép: Tổng Nợ = Tổng Có.
 * 2. Phí 0.1% (min 2.000, max 50.000) do người gửi chịu.
 * 3. Kiểm soát thấu chi và hạn mức chuyển 500 triệu/ngày.
 * 4. Idempotency chuẩn mực: nhớ cả kết quả thành công lẫn từ chối.
 * 5. Chống deadlock bằng cơ chế khóa theo thứ tự Account ID.
 * 6. Thread-safe, hiệu năng cao, không khóa toàn cục.
 */
public final class LedgerService {

    public static final String FEE_INCOME_ACCOUNT = "FEE_INCOME";
    private static final BigDecimal FEE_RATE = new BigDecimal("0.001");
    private static final BigDecimal MIN_FEE = new BigDecimal("2000");
    private static final BigDecimal MAX_FEE = new BigDecimal("50000");

    private final Clock clock;
    private final ZoneId zoneId;
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, JournalEntry> journals = new ConcurrentHashMap<>();
    private final Map<String, IdempotencyRecord> idempotencyStore = new ConcurrentHashMap<>();
    private final Account feeIncomeAccount;

    private static final class IdempotencyRecord {
        final TransferRequest request;
        final TransferResult result;
        final RuntimeException exception;

        IdempotencyRecord(TransferRequest request, TransferResult result, RuntimeException exception) {
            this.request = request;
            this.result = result;
            this.exception = exception;
        }
    }

    public LedgerService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock không được null");
        this.zoneId = clock.getZone();
        this.feeIncomeAccount = new Account(FEE_INCOME_ACCOUNT, BigDecimal.ZERO, BigDecimal.ZERO);
        this.accounts.put(FEE_INCOME_ACCOUNT, this.feeIncomeAccount);
    }

    public void openAccount(String accountId, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản không được rỗng");
        }
        if (accounts.containsKey(accountId)) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại: " + accountId);
        }
        accounts.put(accountId, new Account(accountId, initialBalance, overdraftLimit));
    }

    public Account getAccount(String accountId) {
        Account acc = accounts.get(accountId);
        if (acc == null) {
            throw new AccountNotFoundException("Không tìm thấy tài khoản: " + accountId);
        }
        return acc;
    }

    public TransferResult transfer(TransferRequest request) {
        Objects.requireNonNull(request, "TransferRequest không được null");

        // 1. Kiểm tra bộ nhớ đệm Idempotency
        IdempotencyRecord cached = idempotencyStore.get(request.idempotencyKey());
        if (cached != null) {
            if (isSameContent(cached.request, request)) {
                if (cached.exception != null) {
                    throw cached.exception;
                }
                return cached.result;
            } else {
                throw new IdempotencyConflictException(
                        "IdempotencyKey '" + request.idempotencyKey() + "' đã tồn tại nhưng có nội dung khác"
                );
            }
        }

        if (request.fromAccountId().equals(request.toAccountId())) {
            InvalidTransactionException ex = new InvalidTransactionException("Không thể chuyển tiền cho chính mình");
            idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, null, ex));
            throw ex;
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            InvalidTransactionException ex = new InvalidTransactionException("Số tiền chuyển phải lớn hơn 0");
            idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, null, ex));
            throw ex;
        }

        Account fromAcc = accounts.get(request.fromAccountId());
        Account toAcc = accounts.get(request.toAccountId());

        if (fromAcc == null) {
            AccountNotFoundException ex = new AccountNotFoundException("Tài khoản gửi không tồn tại: " + request.fromAccountId());
            idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, null, ex));
            throw ex;
        }
        if (toAcc == null) {
            AccountNotFoundException ex = new AccountNotFoundException("Tài khoản nhận không tồn tại: " + request.toAccountId());
            idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, null, ex));
            throw ex;
        }

        // Tính phí
        BigDecimal fee = calculateFee(request.amount());
        BigDecimal totalDebit = request.amount().add(fee);

        // Khóa hai tài khoản theo thứ tự sắp xếp Account ID để chống deadlock
        Account firstLock = fromAcc.id().compareTo(toAcc.id()) < 0 ? fromAcc : toAcc;
        Account secondLock = fromAcc.id().compareTo(toAcc.id()) < 0 ? toAcc : fromAcc;

        firstLock.lock().lock();
        try {
            secondLock.lock().lock();
            try {
                feeIncomeAccount.lock().lock();
                try {
                    Instant now = clock.instant();
                    LocalDate today = now.atZone(zoneId).toLocalDate();
                    String journalId = "JNL-" + UUID.randomUUID();

                    // Kiểm tra và thực hiện trừ tiền người gửi
                    fromAcc.applyPosting(
                            journalId, now, today, EntryType.DEBIT, totalDebit,
                            "Chuyển tiền tới " + toAcc.id() + " (gốc " + request.amount() + " + phí " + fee + ")",
                            true, request.amount()
                    );

                    // Cộng tiền người nhận
                    toAcc.applyPosting(
                            journalId, now, today, EntryType.CREDIT, request.amount(),
                            "Nhận tiền từ " + fromAcc.id(),
                            false, null
                    );

                    // Cộng phí vào tài khoản FEE_INCOME
                    feeIncomeAccount.applyPosting(
                            journalId, now, today, EntryType.CREDIT, fee,
                            "Thu phí chuyển tiền từ " + fromAcc.id(),
                            false, null
                    );

                    List<JournalLine> lines = List.of(
                            new JournalLine(fromAcc.id(), EntryType.DEBIT, totalDebit),
                            new JournalLine(toAcc.id(), EntryType.CREDIT, request.amount()),
                            new JournalLine(FEE_INCOME_ACCOUNT, EntryType.CREDIT, fee)
                    );

                    JournalEntry entry = new JournalEntry(
                            journalId, request.idempotencyKey(), now,
                            "Chuyển tiền " + fromAcc.id() + " -> " + toAcc.id(),
                            lines
                    );
                    journals.put(journalId, entry);

                    TransferResult result = new TransferResult(
                            journalId,
                            request.idempotencyKey(),
                            fromAcc.id(),
                            toAcc.id(),
                            request.amount(),
                            fee,
                            fromAcc.getBalance(),
                            toAcc.getBalance(),
                            feeIncomeAccount.getBalance()
                    );

                    idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, result, null));
                    return result;

                } catch (RuntimeException ex) {
                    idempotencyStore.put(request.idempotencyKey(), new IdempotencyRecord(request, null, ex));
                    throw ex;
                } finally {
                    feeIncomeAccount.lock().unlock();
                }
            } finally {
                secondLock.lock().unlock();
            }
        } finally {
            firstLock.lock().unlock();
        }
    }

    public JournalEntry reverse(String journalId) {
        JournalEntry original = journals.get(journalId);
        if (original == null) {
            throw new InvalidTransactionException("Không tìm thấy bút toán: " + journalId);
        }

        if (!original.markReversed()) {
            throw new AlreadyReversedException("Bút toán " + journalId + " đã được đảo trước đó");
        }

        JournalLine debitLine = null;
        JournalLine creditReceiverLine = null;
        JournalLine creditFeeLine = null;

        for (JournalLine line : original.lines()) {
            if (line.type() == EntryType.DEBIT) {
                debitLine = line;
            } else if (FEE_INCOME_ACCOUNT.equals(line.accountId())) {
                creditFeeLine = line;
            } else {
                creditReceiverLine = line;
            }
        }

        if (debitLine == null || creditReceiverLine == null || creditFeeLine == null) {
            throw new InvalidTransactionException("Cấu trúc bút toán đảo không hợp lệ");
        }

        Account senderAcc = getAccount(debitLine.accountId());
        Account receiverAcc = getAccount(creditReceiverLine.accountId());

        Account firstLock = senderAcc.id().compareTo(receiverAcc.id()) < 0 ? senderAcc : receiverAcc;
        Account secondLock = senderAcc.id().compareTo(receiverAcc.id()) < 0 ? receiverAcc : senderAcc;

        firstLock.lock().lock();
        try {
            secondLock.lock().lock();
            try {
                feeIncomeAccount.lock().lock();
                try {
                    Instant now = clock.instant();
                    LocalDate today = now.atZone(zoneId).toLocalDate();
                    String reverseJournalId = "REV-" + UUID.randomUUID();

                    // Bút toán đảo không áp hạn mức ngày nhưng vẫn kiểm tra thấu chi của bên bị trừ
                    receiverAcc.applyPosting(
                            reverseJournalId, now, today, EntryType.DEBIT, creditReceiverLine.amount(),
                            "Hoàn tác nhận tiền (đảo bút toán " + journalId + ")",
                            false, null
                    );

                    feeIncomeAccount.applyPosting(
                            reverseJournalId, now, today, EntryType.DEBIT, creditFeeLine.amount(),
                            "Hoàn tác phí giao dịch (đảo bút toán " + journalId + ")",
                            false, null
                    );

                    senderAcc.applyPosting(
                            reverseJournalId, now, today, EntryType.CREDIT, debitLine.amount(),
                            "Hoàn tiền chuyển và phí (đảo bút toán " + journalId + ")",
                            false, null
                    );

                    List<JournalLine> reversedLines = List.of(
                            new JournalLine(receiverAcc.id(), EntryType.DEBIT, creditReceiverLine.amount()),
                            new JournalLine(FEE_INCOME_ACCOUNT, EntryType.DEBIT, creditFeeLine.amount()),
                            new JournalLine(senderAcc.id(), EntryType.CREDIT, debitLine.amount())
                    );

                    JournalEntry reversedEntry = new JournalEntry(
                            reverseJournalId, null, now,
                            "Đảo bút toán " + journalId,
                            reversedLines
                    );
                    journals.put(reverseJournalId, reversedEntry);
                    return reversedEntry;
                } finally {
                    feeIncomeAccount.lock().unlock();
                }
            } finally {
                secondLock.lock().unlock();
            }
        } finally {
            firstLock.lock().unlock();
        }
    }

    public AccountStatement statement(String accountId, LocalDate fromDate, LocalDate toDate) {
        Account acc = getAccount(accountId);
        List<Account.AccountPosting> allPostings = acc.getPostings();

        BigDecimal openingBalance = BigDecimal.ZERO;
        List<StatementItem> items = new ArrayList<>();

        for (Account.AccountPosting p : allPostings) {
            if (p.date().isBefore(fromDate)) {
                // Tích lũy số dư trước kỳ
                BigDecimal delta = (p.type() == EntryType.CREDIT) ? p.amount() : p.amount().negate();
                openingBalance = openingBalance.add(delta);
            } else if (!p.date().isAfter(toDate)) {
                items.add(new StatementItem(
                        p.journalId(), p.timestamp(), p.date(), p.type(), p.amount(), p.balanceAfter(), p.description()
                ));
            }
        }

        BigDecimal closingBalance = openingBalance;
        for (StatementItem item : items) {
            BigDecimal delta = (item.type() == EntryType.CREDIT) ? item.amount() : item.amount().negate();
            closingBalance = closingBalance.add(delta);
        }

        return new AccountStatement(accountId, fromDate, toDate, openingBalance, items, closingBalance);
    }

    public TrialBalance trialBalance() {
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> map = new ConcurrentHashMap<>();

        for (Account acc : accounts.values()) {
            BigDecimal bal = acc.getBalance();
            map.put(acc.id(), bal);
            total = total.add(bal);
        }

        return new TrialBalance(map, total);
    }

    public static BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal rawFee = amount.multiply(FEE_RATE).setScale(0, RoundingMode.HALF_UP);
        if (rawFee.compareTo(MIN_FEE) < 0) {
            return MIN_FEE;
        }
        if (rawFee.compareTo(MAX_FEE) > 0) {
            return MAX_FEE;
        }
        return rawFee;
    }

    private static boolean isSameContent(TransferRequest r1, TransferRequest r2) {
        return Objects.equals(r1.fromAccountId(), r2.fromAccountId()) &&
                Objects.equals(r1.toAccountId(), r2.toAccountId()) &&
                r1.amount().compareTo(r2.amount()) == 0;
    }
}

