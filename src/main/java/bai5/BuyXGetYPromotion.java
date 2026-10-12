package bai5;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mua X tặng Y cùng sản phẩm: Mỗi bộ X+Y đơn vị được giảm giá của Y đơn vị.
 */
public final class BuyXGetYPromotion implements Promotion {

    private final String id;
    private final int priority;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final Set<CustomerTier> applicableTiers;
    private final String productCode;
    private final int buyX;
    private final int getY;

    public BuyXGetYPromotion(
            String id,
            int priority,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Set<CustomerTier> applicableTiers,
            String productCode,
            int buyX,
            int getY
    ) {
        this.id = id;
        this.priority = priority;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableTiers = applicableTiers == null ? Collections.emptySet() : applicableTiers;
        this.productCode = productCode;
        this.buyX = buyX;
        this.getY = getY;
    }

    @Override public String id() { return id; }
    @Override public int priority() { return priority; }
    @Override public LocalDateTime validFrom() { return validFrom; }
    @Override public LocalDateTime validTo() { return validTo; }
    @Override public Set<CustomerTier> applicableTiers() { return applicableTiers; }

    public String productCode() { return productCode; }
    public int buyX() { return buyX; }
    public int getY() { return getY; }

    public List<PromotionCandidate> generateCandidates(Map<String, Integer> availableUnits, Map<String, BigDecimal> unitPrices) {
        int available = availableUnits.getOrDefault(productCode, 0);
        int setSize = buyX + getY;
        if (setSize <= 0 || available < setSize) {
            return Collections.emptyList();
        }

        BigDecimal unitPrice = unitPrices.getOrDefault(productCode, BigDecimal.ZERO);
        BigDecimal discountPerSet = unitPrice.multiply(BigDecimal.valueOf(getY));

        int maxSets = available / setSize;
        List<PromotionCandidate> list = new ArrayList<>();

        for (int k = 1; k <= maxSets; k++) {
            int consumed = k * setSize;
            BigDecimal totalDiscount = discountPerSet.multiply(BigDecimal.valueOf(k));
            list.add(new PromotionCandidate(
                    this,
                    Map.of(productCode, consumed),
                    totalDiscount,
                    "Áp " + id + " cho " + consumed + " " + productCode + " (" + k + " bộ, giảm " + totalDiscount + " đ)"
            ));
        }

        return list;
    }
}

