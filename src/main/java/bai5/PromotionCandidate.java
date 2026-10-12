package bai5;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Một phương án áp dụng cấp dòng cụ thể (chiếm một số đơn vị sản phẩm và mang lại khoản giảm nhất định).
 */
public record PromotionCandidate(
        Promotion promotion,
        Map<String, Integer> unitsConsumed,
        BigDecimal discountAmount,
        String description
) {
    public PromotionCandidate {
        unitsConsumed = Collections.unmodifiableMap(unitsConsumed);
    }
}

