package bai4;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Đại diện cho một tài khoản trong sổ cái.
 * Số dư chỉ thay đổi khi ghi nhận bút toán.
 */
public final class Account {

    public static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("500000000");

    private final String id;
    private final BigDecimal overdraftLimit;
    private BigDecimal balance;
    private final Map<LocalDate, BigDecimal> dailyOutflow = new HashMap<>();
    private final List<AccountPosting> postings = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public record AccountPosting(
            String journalId,
            Instant timestamp,
            LocalDate date,
            EntryType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description
    ) {}

    public Account(String id, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        this.id = Objects.requireNonNull(id, "Account ID không được null");
        this.balance = Objects.requireNonNull(initialBalance, "Số dư ban đầu không được null");
        this.overdraftLimit = overdraftLimit == null ? BigDecimal.ZERO : overdraftLimit;
        if (this.overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hạn mức thấu chi không được âm: " + this.overdraftLimit);
        }
    }

    public String id() {
        return id;
    }

    public BigDecimal overdraftLimit() {
        return overdraftLimit;
    }

    public BigDecimal getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock lock() {
        return lock;
    }

    /**
     * Ghi nhận dòng bút toán vào tài khoản. LedgerService gọi khi đã giữ lock.
     */
    void applyPosting(
            String journalId,
            Instant timestamp,
            LocalDate date,
            EntryType type,
            BigDecimal amount,
            String description,
            boolean checkDailyLimit,
            BigDecimal transferPrincipal
    ) {
        BigDecimal delta = (type == EntryType.CREDIT) ? amount : amount.negate();
        BigDecimal newBalance = balance.add(delta);

        BigDecimal minAllowed = overdraftLimit.negate();
        if (newBalance.compareTo(minAllowed) < 0) {
            throw new InsufficientFundsException(
                    "Tài khoản " + id + " không đủ số dư. Số dư hiện tại: " + balance +
                    ", cần trừ: " + amount + ", hạn mức thấu chi: " + overdraftLimit
            );
        }

        if (checkDailyLimit && transferPrincipal != null && transferPrincipal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentDaily = dailyOutflow.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal newDaily = currentDaily.add(transferPrincipal);
            if (newDaily.compareTo(DAILY_TRANSFER_LIMIT) > 0) {
                throw new DailyLimitExceededException(
                        "Tài khoản " + id + " vượt hạn mức chuyển đi trong ngày " + date +
                        ": đã chuyển " + currentDaily + ", thêm " + transferPrincipal +
                        " vượt quá hạn mức tối đa " + DAILY_TRANSFER_LIMIT
                );
            }
            dailyOutflow.put(date, newDaily);
        }

        this.balance = newBalance;
        postings.add(new AccountPosting(journalId, timestamp, date, type, amount, newBalance, description));
    }

    public List<AccountPosting> getPostings() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(postings));
        } finally {
            lock.unlock();
        }
    }
}

