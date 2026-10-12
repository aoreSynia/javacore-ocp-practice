package bai5;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record PricingResult(
        BigDecimal originalTotal,
        List<AppliedPromotion> appliedPromotions,
        BigDecimal finalPayment,
        String explanation
) {
    public PricingResult {
        appliedPromotions = Collections.unmodifiableList(appliedPromotions);
    }
}

