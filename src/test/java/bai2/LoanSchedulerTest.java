package bai2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanSchedulerTest {

    @Test
    @DisplayName("Kiểm tra ví dụ đề thi: 120 triệu, 12%/năm, 6 kỳ, giải ngân 31/01/2026")
    void shouldMatchExamExampleForTraDeuAndGocDeu() {
        LoanRequest req = new LoanRequest(
                120_000_000,
                12.0,
                6,
                LocalDate.of(2026, 1, 31)
        );

        // 1. Kiểm tra TRA_DEU
        List<Installment> traDeu = LoanScheduler.generate(req, Method.TRA_DEU);
        assertThat(traDeu).hasSize(6);

        // Kỳ 1: 02/03/2026 (28/02 là Thứ Bảy)
        Installment k1 = traDeu.get(0);
        assertThat(k1.k()).isEqualTo(1);
        assertThat(k1.dueDate()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(k1.interest()).isEqualByComparingTo(new BigDecimal("1200000"));
        assertThat(k1.principal()).isEqualByComparingTo(new BigDecimal("19505804"));
        assertThat(k1.closingBalance()).isEqualByComparingTo(new BigDecimal("100494196"));

        // Kỳ 2: 31/03/2026
        Installment k2 = traDeu.get(1);
        assertThat(k2.k()).isEqualTo(2);
        assertThat(k2.dueDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(k2.interest()).isEqualByComparingTo(new BigDecimal("1004942"));
        assertThat(k2.principal()).isEqualByComparingTo(new BigDecimal("19700862"));
        assertThat(k2.closingBalance()).isEqualByComparingTo(new BigDecimal("80793334"));

        // Kỳ 3: 30/04/2026
        Installment k3 = traDeu.get(2);
        assertThat(k3.k()).isEqualTo(3);
        assertThat(k3.dueDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(k3.interest()).isEqualByComparingTo(new BigDecimal("807933"));
        assertThat(k3.principal()).isEqualByComparingTo(new BigDecimal("19897871"));
        assertThat(k3.closingBalance()).isEqualByComparingTo(new BigDecimal("60895463"));

        // Kỳ 4: 01/06/2026 (31/05 là Chủ nhật)
        Installment k4 = traDeu.get(3);
        assertThat(k4.k()).isEqualTo(4);
        assertThat(k4.dueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(k4.interest()).isEqualByComparingTo(new BigDecimal("608955"));
        assertThat(k4.principal()).isEqualByComparingTo(new BigDecimal("20096849"));
        assertThat(k4.closingBalance()).isEqualByComparingTo(new BigDecimal("40798614"));

        // Kỳ 5: 30/06/2026
        Installment k5 = traDeu.get(4);
        assertThat(k5.k()).isEqualTo(5);
        assertThat(k5.dueDate()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(k5.interest()).isEqualByComparingTo(new BigDecimal("407986"));
        assertThat(k5.principal()).isEqualByComparingTo(new BigDecimal("20297818"));
        assertThat(k5.closingBalance()).isEqualByComparingTo(new BigDecimal("20500796"));

        // Kỳ 6: 31/07/2026
        Installment k6 = traDeu.get(5);
        assertThat(k6.k()).isEqualTo(6);
        assertThat(k6.dueDate()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(k6.interest()).isEqualByComparingTo(new BigDecimal("205008"));
        assertThat(k6.principal()).isEqualByComparingTo(new BigDecimal("20500796"));
        assertThat(k6.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // 2. Kiểm tra GOC_DEU
        List<Installment> gocDeu = LoanScheduler.generate(req, Method.GOC_DEU);
        assertThat(gocDeu).hasSize(6);
        assertThat(gocDeu.get(0).payment()).isEqualByComparingTo(new BigDecimal("21200000"));
        assertThat(gocDeu.get(1).payment()).isEqualByComparingTo(new BigDecimal("21000000"));
        assertThat(gocDeu.get(2).payment()).isEqualByComparingTo(new BigDecimal("20800000"));
        assertThat(gocDeu.get(3).payment()).isEqualByComparingTo(new BigDecimal("20600000"));
        assertThat(gocDeu.get(4).payment()).isEqualByComparingTo(new BigDecimal("20400000"));
        assertThat(gocDeu.get(5).payment()).isEqualByComparingTo(new BigDecimal("20200000"));
        assertThat(gocDeu.get(5).closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // 3. So sánh tổng lãi
        LoanComparisonResult comparison = LoanScheduler.compareMethods(req);
        assertThat(comparison.totalInterestGocDeu()).isEqualByComparingTo(new BigDecimal("4200000"));
        assertThat(comparison.totalInterestTraDeu()).isEqualByComparingTo(new BigDecimal("4234824"));
        assertThat(comparison.interestDifference()).isEqualByComparingTo(new BigDecimal("34824"));
    }

    @Test
    @DisplayName("Từ chối số tiền <= 0")
    void shouldThrowWhenAmountInvalid() {
        LoanRequest reqZero = new LoanRequest(0, 10, 12, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(reqZero, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("Số tiền vay phải lớn hơn 0");

        LoanRequest reqNegative = new LoanRequest(-500_000, 10, 12, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(reqNegative, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("Số tiền vay phải lớn hơn 0");
    }

    @Test
    @DisplayName("Từ chối lãi suất âm hoặc trên 50%/năm")
    void shouldThrowWhenRateInvalid() {
        LoanRequest reqNeg = new LoanRequest(10_000_000, -1, 12, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(reqNeg, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("Lãi suất không được âm");

        LoanRequest reqOver = new LoanRequest(10_000_000, 50.1, 12, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(reqOver, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("không được vượt quá 50%");
    }

    @Test
    @DisplayName("Từ chối số kỳ ngoài 1–360")
    void shouldThrowWhenTermInvalid() {
        LoanRequest req0 = new LoanRequest(10_000_000, 10, 0, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(req0, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("Số kỳ vay phải từ 1 đến 360");

        LoanRequest req361 = new LoanRequest(10_000_000, 10, 361, LocalDate.now());
        assertThatThrownBy(() -> LoanScheduler.generate(req361, Method.GOC_DEU))
                .isInstanceOf(InvalidLoanRequestException.class)
                .hasMessageContaining("Số kỳ vay phải từ 1 đến 360");
    }
}
