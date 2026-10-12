package bai1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceCalculatorTest {

    @Test
    @DisplayName("Kiểm tra ví dụ mẫu trong đề thi - khớp toàn bộ số liệu chi tiết và tổng")
    void shouldMatchExamExample() {
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 32_000, 10, 5),
                new InvoiceLine("SP02", ProductCategory.GIA_DUNG, 1_250_000, 2, 0),
                new InvoiceLine("SP03", ProductCategory.SACH, 45_000, 3, 0)
        );

        InvoiceResult result = InvoiceCalculator.calculate(lines);

        // 1. Kiểm tra dòng 1: SP01
        InvoiceLineDetail d1 = result.lineDetails().get(0);
        assertThat(d1.productCode()).isEqualTo("SP01");
        assertThat(d1.grossAmount()).isEqualByComparingTo(new BigDecimal("320000"));
        assertThat(d1.lineDiscount()).isEqualByComparingTo(new BigDecimal("16000"));
        assertThat(d1.afterLineDiscount()).isEqualByComparingTo(new BigDecimal("304000"));
        assertThat(d1.allocatedDiscount()).isEqualByComparingTo(new BigDecimal("6080"));
        assertThat(d1.taxableAmount()).isEqualByComparingTo(new BigDecimal("297920"));
        assertThat(d1.vatAmount()).isEqualByComparingTo(new BigDecimal("14896"));

        // 2. Kiểm tra dòng 2: SP02
        InvoiceLineDetail d2 = result.lineDetails().get(1);
        assertThat(d2.productCode()).isEqualTo("SP02");
        assertThat(d2.grossAmount()).isEqualByComparingTo(new BigDecimal("2500000"));
        assertThat(d2.lineDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(d2.afterLineDiscount()).isEqualByComparingTo(new BigDecimal("2500000"));
        assertThat(d2.allocatedDiscount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(d2.taxableAmount()).isEqualByComparingTo(new BigDecimal("2450000"));
        assertThat(d2.vatAmount()).isEqualByComparingTo(new BigDecimal("245000"));

        // 3. Kiểm tra dòng 3: SP03
        InvoiceLineDetail d3 = result.lineDetails().get(2);
        assertThat(d3.productCode()).isEqualTo("SP03");
        assertThat(d3.grossAmount()).isEqualByComparingTo(new BigDecimal("135000"));
        assertThat(d3.lineDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(d3.afterLineDiscount()).isEqualByComparingTo(new BigDecimal("135000"));
        assertThat(d3.allocatedDiscount()).isEqualByComparingTo(new BigDecimal("2700"));
        assertThat(d3.taxableAmount()).isEqualByComparingTo(new BigDecimal("132300"));
        assertThat(d3.vatAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        // 4. Kiểm tra tổng
        assertThat(result.totalAfterLineDiscount()).isEqualByComparingTo(new BigDecimal("2939000"));
        assertThat(result.totalInvoiceDiscount()).isEqualByComparingTo(new BigDecimal("58780"));
        assertThat(result.totalTaxableAmount()).isEqualByComparingTo(new BigDecimal("2880220"));
        assertThat(result.totalVat()).isEqualByComparingTo(new BigDecimal("259896"));
        assertThat(result.totalPayment()).isEqualByComparingTo(new BigDecimal("3140116"));

        // 5. Kiểm tra hàm tổng hợp thuế theo từng mức thuế suất
        Map<BigDecimal, BigDecimal> taxByRate = result.taxByRate();
        assertThat(taxByRate.get(new BigDecimal("0.05"))).isEqualByComparingTo(new BigDecimal("14896"));
        assertThat(taxByRate.get(new BigDecimal("0.10"))).isEqualByComparingTo(new BigDecimal("245000"));
        assertThat(taxByRate.get(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Hóa đơn dưới 2.000.000đ thì không được giảm 2% toàn hóa đơn")
    void shouldNotApplyInvoiceDiscountWhenBelowThreshold() {
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 100_000, 10, 0)
        );

        InvoiceResult result = InvoiceCalculator.calculate(lines);

        assertThat(result.totalAfterLineDiscount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(result.totalInvoiceDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.lineDetails().get(0).allocatedDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalTaxableAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        // THUC_PHAM vat 5% -> 50_000
        assertThat(result.totalVat()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.totalPayment()).isEqualByComparingTo(new BigDecimal("1050000"));
    }

    @Test
    @DisplayName("Phần lệch do làm tròn giảm giá phân bổ phải được dồn vào dòng có giá trị lớn nhất")
    void shouldAllocateRoundingDiffToMaxLine() {
        // Tổng = 3.000.000đ -> Giảm 2% = 60.000đ
        // Dòng 1: 1.000.001đ, Dòng 2: 1.000.000đ, Dòng 3: 999.999đ
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("A", ProductCategory.SACH, 1_000_001, 1, 0),
                new InvoiceLine("B", ProductCategory.SACH, 1_000_000, 1, 0),
                new InvoiceLine("C", ProductCategory.SACH, 999_999, 1, 0)
        );

        InvoiceResult result = InvoiceCalculator.calculate(lines);
        BigDecimal sumAllocated = result.lineDetails().stream()
                .map(InvoiceLineDetail::allocatedDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(sumAllocated).isEqualByComparingTo(result.totalInvoiceDiscount());
    }

    @Test
    @DisplayName("Từ chối hóa đơn rỗng")
    void shouldThrowWhenEmpty() {
        assertThatThrownBy(() -> InvoiceCalculator.calculate(List.of()))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Hóa đơn không được rỗng");
    }

    @Test
    @DisplayName("Từ chối số lượng <= 0")
    void shouldThrowWhenQuantityNonPositive() {
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 10_000, 0, 0)
        );
        assertThatThrownBy(() -> InvoiceCalculator.calculate(lines))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Số lượng phải lớn hơn 0");
    }

    @Test
    @DisplayName("Từ chối đơn giá âm")
    void shouldThrowWhenNegativeUnitPrice() {
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, -100, 1, 0)
        );
        assertThatThrownBy(() -> InvoiceCalculator.calculate(lines))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Đơn giá không được âm");
    }

    @Test
    @DisplayName("Từ chối chiết khấu ngoài khoảng 0-50%")
    void shouldThrowWhenDiscountOutOfRange() {
        List<InvoiceLine> linesOver = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 10_000, 1, 55)
        );
        assertThatThrownBy(() -> InvoiceCalculator.calculate(linesOver))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Chiết khấu ngoài khoảng");

        List<InvoiceLine> linesNegative = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 10_000, 1, -5)
        );
        assertThatThrownBy(() -> InvoiceCalculator.calculate(linesNegative))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Chiết khấu ngoài khoảng");
    }

    @Test
    @DisplayName("Từ chối trùng mã sản phẩm trên hai dòng")
    void shouldThrowWhenDuplicateProductCode() {
        List<InvoiceLine> lines = List.of(
                new InvoiceLine("SP01", ProductCategory.THUC_PHAM, 10_000, 1, 0),
                new InvoiceLine("SP01", ProductCategory.GIA_DUNG, 20_000, 2, 0)
        );
        assertThatThrownBy(() -> InvoiceCalculator.calculate(lines))
                .isInstanceOf(InvalidInvoiceException.class)
                .hasMessageContaining("Trùng mã sản phẩm");
    }
}
