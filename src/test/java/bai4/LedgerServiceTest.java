package bai4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceTest {

    private Clock clock;
    private LedgerService ledger;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-10T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        ledger = new LedgerService(clock);
    }

    @Test
    @DisplayName("Kiểm tra toàn bộ 6 bước kịch bản mẫu trong đề thi")
    void shouldMatchExamScenarioSteps1To6() {
        // A có 10.000.000 đ, B có 0 đ, thấu chi 0
        ledger.openAccount("A", new BigDecimal("10000000"), BigDecimal.ZERO);
        ledger.openAccount("B", BigDecimal.ZERO, BigDecimal.ZERO);

        // Bước 1: transfer(k1, A->B, 1.000.000)
        // Phí 2.000 (0,1% = 1.000 < tối thiểu). A = 8.998.000; B = 1.000.000; FEE_INCOME = 2.000
        TransferRequest req1 = new TransferRequest("k1", "A", "B", 1_000_000);
        TransferResult res1 = ledger.transfer(req1);

        assertThat(res1.fee()).isEqualByComparingTo(new BigDecimal("2000"));
        assertThat(ledger.getAccount("A").getBalance()).isEqualByComparingTo(new BigDecimal("8998000"));
        assertThat(ledger.getAccount("B").getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(ledger.getAccount(LedgerService.FEE_INCOME_ACCOUNT).getBalance()).isEqualByComparingTo(new BigDecimal("2000"));

        // Bước 2: transfer(k1, A->B, 1.000.000)
        // Trả kết quả bước 1, số dư không đổi
        TransferResult res2 = ledger.transfer(req1);
        assertThat(res2.journalId()).isEqualTo(res1.journalId());
        assertThat(ledger.getAccount("A").getBalance()).isEqualByComparingTo(new BigDecimal("8998000"));
        assertThat(ledger.getAccount("B").getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(ledger.getAccount(LedgerService.FEE_INCOME_ACCOUNT).getBalance()).isEqualByComparingTo(new BigDecimal("2000"));

        // Bước 3: transfer(k1, A->B, 2.000.000)
        // IdempotencyConflictException
        TransferRequest req3 = new TransferRequest("k1", "A", "B", 2_000_000);
        assertThatThrownBy(() -> ledger.transfer(req3))
                .isInstanceOf(IdempotencyConflictException.class);

        // Bước 4: transfer(k2, A->B, 9.000.000)
        // Phí 9.000; cần 9.009.000 > 8.998.000 nên từ chối InsufficientFundsException
        TransferRequest req4 = new TransferRequest("k2", "A", "B", 9_000_000);
        assertThatThrownBy(() -> ledger.transfer(req4))
                .isInstanceOf(InsufficientFundsException.class);

        // Bước 5: reverse(journal bước 1)
        // A = 10.000.000; B = 0; FEE_INCOME = 0
        ledger.reverse(res1.journalId());
        assertThat(ledger.getAccount("A").getBalance()).isEqualByComparingTo(new BigDecimal("10000000"));
        assertThat(ledger.getAccount("B").getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ledger.getAccount(LedgerService.FEE_INCOME_ACCOUNT).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // Bước 6: reverse(journal bước 1) lần 2
        // AlreadyReversedException
        assertThatThrownBy(() -> ledger.reverse(res1.journalId()))
                .isInstanceOf(AlreadyReversedException.class);
    }

    @Test
    @DisplayName("Kiểm tra trần phí chuyển tiền 50.000đ khi chuyển 80.000.000đ")
    void shouldCapFeeAtFiftyThousand() {
        ledger.openAccount("VIP1", new BigDecimal("100000000"), BigDecimal.ZERO);
        ledger.openAccount("VIP2", BigDecimal.ZERO, BigDecimal.ZERO);

        // 80.000.000 * 0.1% = 80.000đ -> Bị chặn trên còn 50.000đ
        TransferResult result = ledger.transfer(new TransferRequest("k-fee-cap", "VIP1", "VIP2", 80_000_000));
        assertThat(result.fee()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(ledger.getAccount("VIP1").getBalance()).isEqualByComparingTo(new BigDecimal("19950000"));
        assertThat(ledger.getAccount("VIP2").getBalance()).isEqualByComparingTo(new BigDecimal("80000000"));
        assertThat(ledger.getAccount(LedgerService.FEE_INCOME_ACCOUNT).getBalance()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Kiểm tra giới hạn chuyển đi 500 triệu/ngày của một tài khoản")
    void shouldEnforceDailyLimit() {
        ledger.openAccount("C1", new BigDecimal("1000000000"), BigDecimal.ZERO);
        ledger.openAccount("C2", BigDecimal.ZERO, BigDecimal.ZERO);

        // Chuyển lần 1: 400 triệu (hợp lệ)
        ledger.transfer(new TransferRequest("tx-day-1", "C1", "C2", 400_000_000));

        // Chuyển lần 2: 150 triệu -> Tổng 550 triệu > 500 triệu -> Ném ngoại lệ
        assertThatThrownBy(() -> ledger.transfer(new TransferRequest("tx-day-2", "C1", "C2", 150_000_000)))
                .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    @DisplayName("Test tải đồng thời đa luồng: không deadlock, bảng cân đối thử không đổi")
    void shouldHandleConcurrentTransfersWithoutDeadlockAndPreserveTrialBalance() throws Exception {
        int numAccounts = 50;
        BigDecimal initialBalPerAccount = new BigDecimal("100000000"); // 100M each
        BigDecimal initialTotal = initialBalPerAccount.multiply(BigDecimal.valueOf(numAccounts));

        for (int i = 0; i < numAccounts; i++) {
            // Cho thấu chi 50 triệu để các giao dịch ngẫu nhiên diễn ra liên tục
            ledger.openAccount("ACC_" + i, initialBalPerAccount, new BigDecimal("50000000"));
        }

        int numThreads = 8;
        int txPerThread = 2500; // Tổng 20.000 giao dịch chạy đồng thời đa luồng
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        List<String> reusedKeys = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            reusedKeys.add("REUSED_KEY_" + i);
        }

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            tasks.add(() -> {
                Random random = new Random(threadId * 1000L);
                for (int i = 0; i < txPerThread; i++) {
                    int fromIdx = random.nextInt(numAccounts);
                    int toIdx = random.nextInt(numAccounts);
                    if (fromIdx == toIdx) {
                        toIdx = (toIdx + 1) % numAccounts;
                    }
                    String from = "ACC_" + fromIdx;
                    String to = "ACC_" + toIdx;
                    long amount = (random.nextInt(50) + 1) * 10_000L; // 10.000 đến 500.000 đ

                    String key;
                    if (random.nextInt(10) == 0) {
                        // 10% gửi lại khóa cũ
                        key = reusedKeys.get(random.nextInt(reusedKeys.size()));
                    } else {
                        key = "TX_" + threadId + "_" + i;
                    }

                    try {
                        ledger.transfer(new TransferRequest(key, from, to, amount));
                        successCount.incrementAndGet();
                    } catch (InsufficientFundsException | DailyLimitExceededException | IdempotencyConflictException ignored) {
                        // Các lỗi nghiệp vụ từ chối hợp lệ
                    }
                }
                return null;
            });
        }

        long start = System.currentTimeMillis();
        List<Future<Void>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;
        executor.shutdown();

        assertThat(elapsed).isLessThan(30_000); // Hoàn thành dưới 30 giây

        // Kiểm tra quan trọng nhất: Bảng cân đối thử tổng số dư toàn hệ thống KHÔNG ĐỔI
        TrialBalance tb = ledger.trialBalance();
        assertThat(tb.totalBalance()).isEqualByComparingTo(initialTotal);

        // Kiểm tra không tài khoản nào bị âm vượt quá hạn mức thấu chi 50 triệu
        for (int i = 0; i < numAccounts; i++) {
            Account acc = ledger.getAccount("ACC_" + i);
            assertThat(acc.getBalance()).isGreaterThanOrEqualTo(new BigDecimal("-50000000"));
        }
    }
}

