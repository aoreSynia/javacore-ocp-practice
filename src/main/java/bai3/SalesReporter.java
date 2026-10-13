package bai3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SalesReporter {

    private SalesReporter() {
    }

    public static SalesReport generateReport(Path csvPath) {
        List<RowError> errors = new ArrayList<>();
        List<OrderRecord> validRecords = new ArrayList<>();
        Set<String> seenOrderIds = new HashSet<>();

        AtomicInteger lineNumberCounter = new AtomicInteger(0);

        try (Stream<String> lines = Files.lines(csvPath)) {
            lines.forEach(rawLine -> {
                int lineNum = lineNumberCounter.incrementAndGet();
                if (lineNum == 1) {
                    // Header line
                    return;
                }

                String trimmed = rawLine.trim();
                if (trimmed.isEmpty()) {
                    return;
                }

                String[] parts = rawLine.split(",", -1);
                if (parts.length != 8) {
                    errors.add(new RowError(lineNum, "", "sai số cột (cần 8, thực tế " + parts.length + ")"));
                    return;
                }

                String orderId = parts[0].trim();
                String orderDateStr = parts[1].trim();
                String region = parts[2].trim();
                String productCode = parts[3].trim();
                String category = parts[4].trim();
                String quantityStr = parts[5].trim();
                String unitPriceStr = parts[6].trim();
                String statusStr = parts[7].trim();

                if (orderId.isEmpty()) {
                    errors.add(new RowError(lineNum, "", "mã đơn rỗng"));
                    return;
                }

                if (!seenOrderIds.add(orderId)) {
                    errors.add(new RowError(lineNum, orderId, "trùng mã đơn"));
                    return;
                }

                LocalDate orderDate;
                try {
                    orderDate = LocalDate.parse(orderDateStr);
                } catch (DateTimeParseException e) {
                    errors.add(new RowError(lineNum, orderId, "ngày không hợp lệ"));
                    return;
                }

                long quantity;
                try {
                    quantity = Long.parseLong(quantityStr);
                    if (quantity <= 0) {
                        errors.add(new RowError(lineNum, orderId, "số lượng không phải số dương"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    errors.add(new RowError(lineNum, orderId, "số lượng không hợp lệ"));
                    return;
                }

                BigDecimal unitPrice;
                try {
                    unitPrice = new BigDecimal(unitPriceStr);
                    if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add(new RowError(lineNum, orderId, "đơn giá không phải số dương"));
                        return;
                    }
                } catch (Exception e) {
                    errors.add(new RowError(lineNum, orderId, "đơn giá không hợp lệ"));
                    return;
                }

                OrderStatus status;
                try {
                    status = OrderStatus.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    errors.add(new RowError(lineNum, orderId, "trạng thái lạ: " + statusStr));
                    return;
                }

                validRecords.add(new OrderRecord(
                        lineNum,
                        orderId,
                        orderDate,
                        region,
                        productCode,
                        category,
                        quantity,
                        unitPrice,
                        status
                ));
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Lỗi đọc file CSV: " + csvPath, e);
        }

        // Phần tổng hợp dùng Stream API theo yêu cầu đề bài.
        Map<String, BigDecimal> netRevenueByRegion = validRecords.stream()
                .collect(Collectors.groupingBy(
                        OrderRecord::region,
                        Collectors.reducing(BigDecimal.ZERO, OrderRecord::netRevenueImpact, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<Map.Entry<String, BigDecimal>> top3Products = validRecords.stream()
                .collect(Collectors.groupingBy(
                        OrderRecord::productCode,
                        Collectors.reducing(BigDecimal.ZERO, OrderRecord::netRevenueImpact, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .toList();

        Map<YearMonth, BigDecimal> netRevenueByMonth = validRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> YearMonth.from(record.orderDate()),
                        Collectors.reducing(BigDecimal.ZERO, OrderRecord::netRevenueImpact, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, BigDecimal> returnRateByCategory = validRecords.stream()
                .map(OrderRecord::category)
                .distinct()
                .sorted()
                .collect(Collectors.toMap(
                        cat -> cat,
                        cat -> {
                            BigDecimal returned = validRecords.stream()
                                    .filter(r -> r.category().equals(cat) && r.status() == OrderStatus.RETURNED)
                                    .map(OrderRecord::lineAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal completed = validRecords.stream()
                                    .filter(r -> r.category().equals(cat) && r.status() == OrderStatus.COMPLETED)
                                    .map(OrderRecord::lineAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            if (completed.compareTo(BigDecimal.ZERO) == 0) {
                                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                            }
                            return returned.multiply(BigDecimal.valueOf(100))
                                    .divide(completed, 2, RoundingMode.HALF_UP);
                        },
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        BigDecimal totalNetRevenue = validRecords.stream()
                .map(OrderRecord::netRevenueImpact)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalesReport(
                netRevenueByRegion,
                top3Products,
                netRevenueByMonth,
                returnRateByCategory,
                errors,
                totalNetRevenue
        );
    }
}
