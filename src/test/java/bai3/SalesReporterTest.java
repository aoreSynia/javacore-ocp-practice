package bai3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SalesReporterTest {

    private static final String EXAM_CSV_CONTENT = """
            orderId,orderDate,region,productCode,category,quantity,unitPrice,status
            O001,2026-07-03,HN,P01,DIEN_TU,2,5000000,COMPLETED
            O002,2026-07-05,HCM,P02,GIA_DUNG,5,400000,COMPLETED
            O003,2026-07-15,HN,P03,THUC_PHAM,20,35000,COMPLETED
            O004,2026-07-20,DN,P01,DIEN_TU,1,5000000,RETURNED
            O005,2026-08-02,HCM,P01,DIEN_TU,3,4800000,COMPLETED
            O006,2026-08-10,HN,P02,GIA_DUNG,2,400000,CANCELLED
            O007,2026-08-12,DN,P04,GIA_DUNG,4,750000,COMPLETED
            O008,2026-08-18,HCM,P03,THUC_PHAM,abc,35000,COMPLETED
            O009,2026-08-25,HN,P04,GIA_DUNG,1,750000,RETURNED
            O010,2026-09-01,HCM,P02,GIA_DUNG,10,380000,COMPLETED
            O011,2026-13-05,HN,P01,DIEN_TU,1,5000000,COMPLETED
            O002,2026-09-03,HCM,P02,GIA_DUNG,1,400000,COMPLETED
            """;

    @Test
    @DisplayName("Kiểm tra ví dụ đề thi: khớp toàn bộ số liệu doanh số, top sản phẩm, tỷ lệ trả và danh sách lỗi")
    void shouldMatchExamCsvExample(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("orders.csv");
        Files.writeString(csvFile, EXAM_CSV_CONTENT);

        SalesReport report = SalesReporter.generateReport(csvFile);

        // 1. Kiểm tra 3 dòng lỗi: 9, 12, 13
        assertThat(report.errors()).hasSize(3);
        assertThat(report.errors().get(0).lineNumber()).isEqualTo(9);
        assertThat(report.errors().get(0).orderId()).isEqualTo("O008");
        assertThat(report.errors().get(0).reason()).contains("số lượng không hợp lệ");

        assertThat(report.errors().get(1).lineNumber()).isEqualTo(12);
        assertThat(report.errors().get(1).orderId()).isEqualTo("O011");
        assertThat(report.errors().get(1).reason()).contains("ngày không hợp lệ");

        assertThat(report.errors().get(2).lineNumber()).isEqualTo(13);
        assertThat(report.errors().get(2).orderId()).isEqualTo("O002");
        assertThat(report.errors().get(2).reason()).contains("trùng mã đơn");

        // 2. Theo vùng (giảm dần): HCM 20.200.000 đ; HN 9.950.000 đ; DN -2.000.000 đ
        Map<String, BigDecimal> regionRev = report.netRevenueByRegion();
        assertThat(regionRev.get("HCM")).isEqualByComparingTo(new BigDecimal("20200000"));
        assertThat(regionRev.get("HN")).isEqualByComparingTo(new BigDecimal("9950000"));
        assertThat(regionRev.get("DN")).isEqualByComparingTo(new BigDecimal("-2000000"));
        assertThat(report.totalNetRevenue()).isEqualByComparingTo(new BigDecimal("28150000"));

        // Kiểm tra thứ tự giảm dần
        List<String> regionsOrdered = regionRev.keySet().stream().toList();
        assertThat(regionsOrdered).containsExactly("HCM", "HN", "DN");

        // 3. Top 3 sản phẩm: P01 19.400.000 đ; P02 5.800.000 đ; P04 2.250.000 đ
        List<Map.Entry<String, BigDecimal>> top3 = report.top3Products();
        assertThat(top3).hasSize(3);
        assertThat(top3.get(0).getKey()).isEqualTo("P01");
        assertThat(top3.get(0).getValue()).isEqualByComparingTo(new BigDecimal("19400000"));
        assertThat(top3.get(1).getKey()).isEqualTo("P02");
        assertThat(top3.get(1).getValue()).isEqualByComparingTo(new BigDecimal("5800000"));
        assertThat(top3.get(2).getKey()).isEqualTo("P04");
        assertThat(top3.get(2).getValue()).isEqualByComparingTo(new BigDecimal("2250000"));

        // 4. Theo tháng (YearMonth tăng dần): 2026-07: 7.700.000, 2026-08: 16.650.000, 2026-09: 3.800.000
        Map<YearMonth, BigDecimal> monthRev = report.netRevenueByMonth();
        assertThat(monthRev.get(YearMonth.of(2026, 7))).isEqualByComparingTo(new BigDecimal("7700000"));
        assertThat(monthRev.get(YearMonth.of(2026, 8))).isEqualByComparingTo(new BigDecimal("16650000"));
        assertThat(monthRev.get(YearMonth.of(2026, 9))).isEqualByComparingTo(new BigDecimal("3800000"));

        List<YearMonth> monthsOrdered = monthRev.keySet().stream().toList();
        assertThat(monthsOrdered).containsExactly(
                YearMonth.of(2026, 7),
                YearMonth.of(2026, 8),
                YearMonth.of(2026, 9)
        );

        // 5. Tỷ lệ hoàn trả: DIEN_TU 20,49%; GIA_DUNG 8,52%; THUC_PHAM 0,00%
        Map<String, BigDecimal> returnRates = report.returnRateByCategory();
        assertThat(returnRates.get("DIEN_TU")).isEqualByComparingTo(new BigDecimal("20.49"));
        assertThat(returnRates.get("GIA_DUNG")).isEqualByComparingTo(new BigDecimal("8.52"));
        assertThat(returnRates.get("THUC_PHAM")).isEqualByComparingTo(new BigDecimal("0.00"));
    }
}
