package bai5;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionEngineTest {

    @Test
    @DisplayName("Kiểm tra ví dụ bẫy khuyến mại trong đề: Giỏ hàng 170k, khách trả đúng 123k, tránh bẫy 135.5k")
    void shouldAvoidGreedyTrapAndFindOptimalPayment() {
        // Giỏ hàng tổng gốc 170.000 đ
        Cart cart = new Cart(List.of(
                new CartItem("SP-A", "DO_UONG", 10_000, 6),
                new CartItem("SP-B", "BANH_KEO", 25_000, 2),
                new CartItem("SP-C", "GIA_VI", 60_000, 1)
        ));

        assertThat(cart.totalOriginalPrice()).isEqualByComparingTo(new BigDecimal("170000"));

        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 12, 0);
        Customer customer = new Customer("CUST_01", CustomerTier.GOLD);

        // P1 BuyXGetY SP-A mua 2 tặng 1
        Promotion p1 = new BuyXGetYPromotion(
                "P1", 10, null, null, null, "SP-A", 2, 1
        );

        // P2 CategoryPercent DO_UONG giảm 25%
        Promotion p2 = new CategoryPercentPromotion(
                "P2", 5, null, null, null, "DO_UONG", new BigDecimal("25"), null
        );

        // P3 Bundle 1 SP-A + 1 SP-B giá 30.000 đ
        Promotion p3 = new BundlePromotion(
                "P3", 8, null, null, null, Map.of("SP-A", 1, "SP-B", 1), new BigDecimal("30000")
        );

        // P4 OrderThreshold từ 150.000 đ giảm 15.000 đ
        Promotion p4 = new OrderThresholdPromotion(
                "P4", 10, null, null, null, new BigDecimal("150000"), new BigDecimal("15000")
        );

        // P5 Voucher SALE10 giảm 10%, tối đa 12.000 đ
        Promotion p5 = new VoucherPromotion(
                "P5", 10, null, null, null, "SALE10", new BigDecimal("10"), new BigDecimal("12000")
        );

        List<Promotion> promos = List.of(p1, p2, p3, p4, p5);

        long startTime = System.currentTimeMillis();
        PricingResult result = PromotionEngine.evaluate(
                cart, promos, customer, Optional.of("SALE10"), now
        );
        long elapsed = System.currentTimeMillis() - startTime;

        // 1. Kiểm tra thời gian thực thi: Dưới 1 giây theo quy định đề bài
        assertThat(elapsed).isLessThan(1000);

        // 2. Kiểm tra tổng gốc
        assertThat(result.originalTotal()).isEqualByComparingTo(new BigDecimal("170000"));

        // 3. Khách trả 123.000 đ (thay vì bẫy 135.500 đ)
        assertThat(result.finalPayment()).isEqualByComparingTo(new BigDecimal("123000"));

        // 4. Kiểm tra các khuyến mại được áp dụng
        List<AppliedPromotion> applied = result.appliedPromotions();
        assertThat(applied).hasSize(3);

        // Áp P1 cho 6 SP-A (2 bộ, giảm 20.000 đ)
        AppliedPromotion ap1 = applied.stream().filter(a -> a.promotionId().equals("P1")).findFirst().orElseThrow();
        assertThat(ap1.discountAmount()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(ap1.unitsConsumed().get("SP-A")).isEqualTo(6);

        // P4 giảm 15.000 đ
        AppliedPromotion ap4 = applied.stream().filter(a -> a.promotionId().equals("P4")).findFirst().orElseThrow();
        assertThat(ap4.discountAmount()).isEqualByComparingTo(new BigDecimal("15000"));

        // P5 Voucher giảm 12.000 đ (10% của 135.000đ = 13.500đ bị chặn trần 12.000đ)
        AppliedPromotion ap5 = applied.stream().filter(a -> a.promotionId().equals("P5")).findFirst().orElseThrow();
        assertThat(ap5.discountAmount()).isEqualByComparingTo(new BigDecimal("12000"));

        // 5. Kiểm tra đoạn giải thích
        assertThat(result.explanation()).contains("170000");
        assertThat(result.explanation()).contains("123000");
    }

    @Test
    @DisplayName("Loại bỏ khuyến mại hết hạn hoặc không đúng hạng khách")
    void shouldFilterExpiredAndTierMismatchedPromotions() {
        Cart cart = new Cart(List.of(new CartItem("SP-A", "DO_UONG", 50_000, 2)));
        Customer bronzeCust = new Customer("C1", CustomerTier.BRONZE);

        Promotion platinumOnly = new CategoryPercentPromotion(
                "P-PLAT", 10, null, null, Set.of(CustomerTier.PLATINUM), "DO_UONG", new BigDecimal("50"), null
        );

        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 12, 0);
        Promotion expired = new CategoryPercentPromotion(
                "P-EXP", 10, now.minusDays(5), now.minusDays(1), null, "DO_UONG", new BigDecimal("50"), null
        );

        PricingResult result = PromotionEngine.evaluate(
                cart, List.of(platinumOnly, expired), bronzeCust, Optional.empty(), now
        );

        // Không áp khuyến mại nào vì không đủ điều kiện
        assertThat(result.finalPayment()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(result.appliedPromotions()).isEmpty();
    }
}
