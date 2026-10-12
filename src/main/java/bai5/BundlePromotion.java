package bai5;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combo một tập sản phẩm (mỗi loại 1 hoặc nhiều đơn vị) bán với giá cố định.
 */
public final class BundlePromotion implements Promotion {

    private final String id;
    private final int priority;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final Set<CustomerTier> applicableTiers;
    private final Map<String, Integer> bundleItems;
    private final BigDecimal bundlePrice;

    public BundlePromotion(
            String id,
            int priority,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Set<CustomerTier> applicableTiers,
            Map<String, Integer> bundleItems,
            BigDecimal bundlePrice
    ) {
        this.id = id;
        this.priority = priority;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableTiers = applicableTiers == null ? Collections.emptySet() : applicableTiers;
        this.bundleItems = Collections.unmodifiableMap(new HashMap<>(bundleItems));
        this.bundlePrice = bundlePrice;
    }

    @Override public String id() { return id; }
    @Override public int priority() { return priority; }
    @Override public LocalDateTime validFrom() { return validFrom; }
    @Override public LocalDateTime validTo() { return validTo; }
    @Override public Set<CustomerTier> applicableTiers() { return applicableTiers; }

    public Map<String, Integer> bundleItems() { return bundleItems; }
    public BigDecimal bundlePrice() { return bundlePrice; }

    public List<PromotionCandidate> generateCandidates(Map<String, Integer> availableUnits, Map<String, BigDecimal> unitPrices) {
        BigDecimal normalPricePerBundle = BigDecimal.ZERO;
        int maxPossibleSets = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> req : bundleItems.entrySet()) {
            String pCode = req.getKey();
            int reqQty = req.getValue();
            int avail = availableUnits.getOrDefault(pCode, 0);
            if (avail < reqQty) {
                return Collections.emptyList();
            }
            maxPossibleSets = Math.min(maxPossibleSets, avail / reqQty);
            BigDecimal unitPrice = unitPrices.getOrDefault(pCode, BigDecimal.ZERO);
            normalPricePerBundle = normalPricePerBundle.add(unitPrice.multiply(BigDecimal.valueOf(reqQty)));
        }

        if (maxPossibleSets <= 0) {
            return Collections.emptyList();
        }

        BigDecimal discountPerSet = normalPricePerBundle.subtract(bundlePrice);
        if (discountPerSet.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }

        List<PromotionCandidate> list = new ArrayList<>();
        for (int k = 1; k <= maxPossibleSets; k++) {
            Map<String, Integer> consumed = new HashMap<>();
            for (Map.Entry<String, Integer> req : bundleItems.entrySet()) {
                consumed.put(req.getKey(), req.getValue() * k);
            }
            BigDecimal totalDiscount = discountPerSet.multiply(BigDecimal.valueOf(k));
            list.add(new PromotionCandidate(
                    this,
                    consumed,
                    totalDiscount,
                    "Áp " + id + " (" + k + " combo, giảm " + totalDiscount + " đ)"
            ));
        }

        return list;
    }
}

