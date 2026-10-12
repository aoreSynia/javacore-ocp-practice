package bai5;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Khuyến mại cấp đơn hàng: Giá trị sau khuyến mại dòng >= ngưỡng thì giảm số tiền cố định.
 */
public final class OrderThresholdPromotion implements Promotion {

    private final String id;
    private final int priority;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final Set<CustomerTier> applicableTiers;
    private final BigDecimal threshold;
    private final BigDecimal discountAmount;

    public OrderThresholdPromotion(
            String id,
            int priority,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Set<CustomerTier> applicableTiers,
            BigDecimal threshold,
            BigDecimal discountAmount
    ) {
        this.id = id;
        this.priority = priority;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableTiers = applicableTiers == null ? Collections.emptySet() : applicableTiers;
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override public String id() { return id; }
    @Override public int priority() { return priority; }
    @Override public LocalDateTime validFrom() { return validFrom; }
    @Override public LocalDateTime validTo() { return validTo; }
    @Override public Set<CustomerTier> applicableTiers() { return applicableTiers; }

    public BigDecimal threshold() { return threshold; }
    public BigDecimal discountAmount() { return discountAmount; }
}

