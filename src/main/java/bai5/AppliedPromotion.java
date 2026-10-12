package bai5;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public record AppliedPromotion(
        String promotionId,
        String promotionType,
        Map<String, Integer> unitsConsumed,
        BigDecimal discountAmount,
        String description
) {
    public AppliedPromotion {
        unitsConsumed = (unitsConsumed == null) ? Collections.emptyMap() : Collections.unmodifiableMap(unitsConsumed);
    }
}

