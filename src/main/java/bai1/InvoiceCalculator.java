package bai1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public final class InvoiceCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal INVOICE_DISCOUNT_THRESHOLD = new BigDecimal("2000000");
    private static final BigDecimal INVOICE_DISCOUNT_RATE = new BigDecimal("0.02");
    private static final BigDecimal MAX_LINE_DISCOUNT_PERCENT = new BigDecimal("50");

    private InvoiceCalculator() {
    }

    public static InvoiceResult calculate(List<InvoiceLine> lines) {
        validateLines(lines);

        record IntermediateLine(
                InvoiceLine source,
                BigDecimal grossAmount,
                BigDecimal lineDiscount,
                BigDecimal afterLineDiscount
        ) {}

        List<IntermediateLine> intermediateLines = new ArrayList<>(lines.size());
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalLineDiscount = BigDecimal.ZERO;
        BigDecimal totalAfterLineDiscount = BigDecimal.ZERO;

        for (InvoiceLine line : lines) {
            BigDecimal gross = line.unitPrice()
                    .multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(0, RoundingMode.HALF_UP);

            BigDecimal lineDiscount = gross
                    .multiply(line.discountPercent())
                    .divide(HUNDRED, 0, RoundingMode.HALF_UP);

            BigDecimal afterLineDiscount = gross.subtract(lineDiscount);

            totalGross = totalGross.add(gross);
            totalLineDiscount = totalLineDiscount.add(lineDiscount);
            totalAfterLineDiscount = totalAfterLineDiscount.add(afterLineDiscount);

            intermediateLines.add(new IntermediateLine(line, gross, lineDiscount, afterLineDiscount));
        }

        BigDecimal totalInvoiceDiscount = BigDecimal.ZERO;
        if (totalAfterLineDiscount.compareTo(INVOICE_DISCOUNT_THRESHOLD) >= 0) {
            totalInvoiceDiscount = totalAfterLineDiscount
                    .multiply(INVOICE_DISCOUNT_RATE)
                    .setScale(0, RoundingMode.HALF_UP);
        }

        BigDecimal[] allocatedDiscounts = new BigDecimal[lines.size()];
        BigDecimal sumAllocated = BigDecimal.ZERO;
        int maxAfterDiscountIndex = 0;
        BigDecimal maxAfterDiscountVal = BigDecimal.valueOf(-1);

        for (int i = 0; i < intermediateLines.size(); i++) {
            IntermediateLine item = intermediateLines.get(i);
            BigDecimal allocated;
            if (totalInvoiceDiscount.compareTo(BigDecimal.ZERO) == 0 || totalAfterLineDiscount.compareTo(BigDecimal.ZERO) == 0) {
                allocated = BigDecimal.ZERO;
            } else {
                allocated = totalInvoiceDiscount
                        .multiply(item.afterLineDiscount)
                        .divide(totalAfterLineDiscount, 0, RoundingMode.HALF_UP);
            }
            allocatedDiscounts[i] = allocated;
            sumAllocated = sumAllocated.add(allocated);

            if (item.afterLineDiscount.compareTo(maxAfterDiscountVal) > 0) {
                maxAfterDiscountVal = item.afterLineDiscount;
                maxAfterDiscountIndex = i;
            }
        }

        BigDecimal discountDiff = totalInvoiceDiscount.subtract(sumAllocated);
        if (discountDiff.compareTo(BigDecimal.ZERO) != 0 && lines.size() > 0) {
            allocatedDiscounts[maxAfterDiscountIndex] = allocatedDiscounts[maxAfterDiscountIndex].add(discountDiff);
        }

        List<InvoiceLineDetail> details = new ArrayList<>(lines.size());
        BigDecimal totalTaxableAmount = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;
        Map<BigDecimal, BigDecimal> taxByRate = new TreeMap<>();

        for (int i = 0; i < intermediateLines.size(); i++) {
            IntermediateLine item = intermediateLines.get(i);
            BigDecimal allocated = allocatedDiscounts[i];
            BigDecimal taxable = item.afterLineDiscount.subtract(allocated);
            BigDecimal vat = taxable
                    .multiply(item.source.category().getVatRate())
                    .setScale(0, RoundingMode.HALF_UP);

            totalTaxableAmount = totalTaxableAmount.add(taxable);
            totalVat = totalVat.add(vat);

            taxByRate.merge(item.source.category().getVatRate(), vat, BigDecimal::add);

            details.add(new InvoiceLineDetail(
                    item.source.productCode(),
                    item.source.category(),
                    item.source.unitPrice().setScale(0, RoundingMode.HALF_UP),
                    item.source.quantity(),
                    item.grossAmount,
                    item.source.discountPercent(),
                    item.lineDiscount,
                    item.afterLineDiscount,
                    allocated,
                    taxable,
                    vat
            ));
        }

        BigDecimal totalPayment = totalTaxableAmount.add(totalVat);

        return new InvoiceResult(
                details,
                totalGross,
                totalLineDiscount,
                totalAfterLineDiscount,
                totalInvoiceDiscount,
                totalTaxableAmount,
                totalVat,
                totalPayment,
                taxByRate
        );
    }

    private static void validateLines(List<InvoiceLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new InvalidInvoiceException("Hóa đơn không được rỗng");
        }

        Set<String> seenProductCodes = new HashSet<>();

        for (InvoiceLine line : lines) {
            if (line == null) {
                throw new InvalidInvoiceException("Dòng hóa đơn không được null");
            }
            if (line.productCode() == null || line.productCode().trim().isEmpty()) {
                throw new InvalidInvoiceException("Mã sản phẩm không được rỗng");
            }
            if (!seenProductCodes.add(line.productCode())) {
                throw new InvalidInvoiceException("Trùng mã sản phẩm trên hai dòng: " + line.productCode());
            }
            if (line.category() == null) {
                throw new InvalidInvoiceException("Nhóm sản phẩm không được null");
            }
            if (line.quantity() <= 0) {
                throw new InvalidInvoiceException("Số lượng phải lớn hơn 0 (sản phẩm: " + line.productCode() + ")");
            }
            if (line.unitPrice() == null || line.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidInvoiceException("Đơn giá không được âm (sản phẩm: " + line.productCode() + ")");
            }
            if (line.discountPercent() == null ||
                    line.discountPercent().compareTo(BigDecimal.ZERO) < 0 ||
                    line.discountPercent().compareTo(MAX_LINE_DISCOUNT_PERCENT) > 0) {
                throw new InvalidInvoiceException("Chiết khấu ngoài khoảng 0-50% (sản phẩm: " + line.productCode() + ")");
            }
        }
    }
}
