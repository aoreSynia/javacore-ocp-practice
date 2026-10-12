package bai3;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record SalesReport(
        Map<String, BigDecimal> netRevenueByRegion,
        List<Map.Entry<String, BigDecimal>> top3Products,
        Map<YearMonth, BigDecimal> netRevenueByMonth,
        Map<String, BigDecimal> returnRateByCategory,
        List<RowError> errors,
        BigDecimal totalNetRevenue
) {
    public SalesReport {
        netRevenueByRegion = Collections.unmodifiableMap(netRevenueByRegion);
        top3Products = Collections.unmodifiableList(top3Products);
        netRevenueByMonth = Collections.unmodifiableMap(netRevenueByMonth);
        returnRateByCategory = Collections.unmodifiableMap(returnRateByCategory);
        errors = Collections.unmodifiableList(errors);
    }
}
