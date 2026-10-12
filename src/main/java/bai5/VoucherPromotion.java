package bai5;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Khuyến mại cấp đơn hàng: Voucher mã khách nhập; giảm p% trên giá trị sau mọi khuyến mại khác, có trần.
 */
public final class VoucherPromotion implements Promotion {

    private final String id;
    private final int priority;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final Set<CustomerTier> applicableTiers;
    private final String voucherCode;
    private final BigDecimal percent;
    private final BigDecimal maxDiscount;

    public VoucherPromotion(
            String id,
            int priority,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            Set<CustomerTier> applicableTiers,
            String voucherCode,
            BigDecimal percent,
            BigDecimal maxDiscount
    ) {
        this.id = id;
        this.priority = priority;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableTiers = applicableTiers == null ? Collections.emptySet() : applicableTiers;
        this.voucherCode = voucherCode;
        this.percent = percent;
        this.maxDiscount = maxDiscount;
    }

    @Override public String id() { return id; }
    @Override public int priority() { return priority; }
    @Override public LocalDateTime validFrom() { return validFrom; }
    @Override public LocalDateTime validTo() { return validTo; }
    @Override public Set<CustomerTier> applicableTiers() { return applicableTiers; }

    public String voucherCode() { return voucherCode; }
    public BigDecimal percent() { return percent; }
    public BigDecimal maxDiscount() { return maxDiscount; }
}

