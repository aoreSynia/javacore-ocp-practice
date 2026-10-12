package bai5;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Giảm p% cho từng đơn vị thuộc nhóm hàng, có trần tổng giảm tùy chọn.
 */
public final class CategoryPercentPromotion implements Promotion {

    private final String id;
    private final int priority;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final Set<CustomerTier> applicableTiers;
    private final String category;
    private final BigDecimal percent;
    private final BigDecimal maxDiscount;

    public CategoryPercentPromotion(
            String id,
            int priority,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Set<CustomerTier> applicableTiers,
            String category,
            BigDecimal percent,
            BigDecimal maxDiscount
    ) {
        this.id = id;
        this.priority = priority;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableTiers = applicableTiers == null ? Collections.emptySet() : applicableTiers;
        this.category = category;
        this.percent = percent;
        this.maxDiscount = maxDiscount;
    }

    @Override public String id() { return id; }
    @Override public int priority() { return priority; }
    @Override public LocalDateTime validFrom() { return validFrom; }
    @Override public LocalDateTime validTo() { return validTo; }
    @Override public Set<CustomerTier> applicableTiers() { return applicableTiers; }

    public String category() { return category; }
    public BigDecimal percent() { return percent; }
    public BigDecimal maxDiscount() { return maxDiscount; }

    public List<PromotionCandidate> generateCandidates(
            Map<String, Integer> availableUnits,
            Map<String, BigDecimal> unitPrices,
            Map<String, String> itemCategories
    ) {
        List<PromotionCandidate> list = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : availableUnits.entrySet()) {
            String pCode = entry.getKey();
            int qty = entry.getValue();
            if (qty <= 0) continue;

            String cat = itemCategories.get(pCode);
            if (!this.category.equals(cat)) continue;

            BigDecimal unitPrice = unitPrices.getOrDefault(pCode, BigDecimal.ZERO);
            BigDecimal discountPerUnit = unitPrice.multiply(percent)
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

            for (int k = 1; k <= qty; k++) {
                BigDecimal totalDiscount = discountPerUnit.multiply(BigDecimal.valueOf(k));
                if (maxDiscount != null && totalDiscount.compareTo(maxDiscount) > 0) {
                    totalDiscount = maxDiscount;
                }
                list.add(new PromotionCandidate(
                        this,
                        Map.of(pCode, k),
                        totalDiscount,
                        "Áp " + id + " (" + percent + "%) cho " + k + " " + pCode + " (giảm " + totalDiscount + " đ)"
                ));
            }
        }

        return list;
    }
}

