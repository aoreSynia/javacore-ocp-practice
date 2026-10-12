package bai5;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Cây phân cấp các loại khuyến mại, mô hình hóa bằng sealed interface của Java 17.
 */
public sealed interface Promotion permits
        BuyXGetYPromotion,
        CategoryPercentPromotion,
        BundlePromotion,
        OrderThresholdPromotion,
        VoucherPromotion {

    String id();
    int priority();
    LocalDateTime validFrom();
    LocalDateTime validTo();
    Set<CustomerTier> applicableTiers();

    default boolean isEligible(Customer customer, LocalDateTime at) {
        if (validFrom() != null && at.isBefore(validFrom())) {
            return false;
        }
        if (validTo() != null && at.isAfter(validTo())) {
            return false;
        }
        if (applicableTiers() != null && !applicableTiers().isEmpty()) {
            if (customer == null || !applicableTiers().contains(customer.tier())) {
                return false;
            }
        }
        return true;
    }
}

