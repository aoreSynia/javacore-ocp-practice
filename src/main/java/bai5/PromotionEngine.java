package bai5;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tính giá giỏ hàng sau khuyến mại.
 * Dùng Branch and Bound để xét khuyến mại cấp dòng trước khi áp khuyến mại cấp đơn.
 */
public final class PromotionEngine {

    private PromotionEngine() {
    }

    public static PricingResult evaluate(
            Cart cart,
            List<Promotion> promotions,
            Customer customer,
            Optional<String> voucherCode,
            LocalDateTime at
    ) {
        Objects.requireNonNull(cart, "cart không được null");
        Objects.requireNonNull(promotions, "promotions không được null");
        Objects.requireNonNull(customer, "customer không được null");
        Objects.requireNonNull(at, "at không được null");

        BigDecimal originalTotal = cart.totalOriginalPrice();
        if (cart.items().isEmpty()) {
            return new PricingResult(
                    BigDecimal.ZERO,
                    Collections.emptyList(),
                    BigDecimal.ZERO,
                    "Giỏ hàng rỗng, tổng thanh toán: 0 đ."
            );
        }

        // Lọc khuyến mại còn hiệu lực với hạng khách hiện tại.
        List<Promotion> eligiblePromos = promotions.stream()
                .filter(p -> p.isEligible(customer, at))
                .sorted(Comparator.comparingInt(Promotion::priority).reversed())
                .toList();

        List<BuyXGetYPromotion> buyXGetYPromos = new ArrayList<>();
        List<CategoryPercentPromotion> catPercentPromos = new ArrayList<>();
        List<BundlePromotion> bundlePromos = new ArrayList<>();
        List<OrderThresholdPromotion> thresholdPromos = new ArrayList<>();
        List<VoucherPromotion> voucherPromos = new ArrayList<>();

        for (Promotion p : eligiblePromos) {
            if (p instanceof BuyXGetYPromotion b) {
                buyXGetYPromos.add(b);
            } else if (p instanceof CategoryPercentPromotion c) {
                catPercentPromos.add(c);
            } else if (p instanceof BundlePromotion bun) {
                bundlePromos.add(bun);
            } else if (p instanceof OrderThresholdPromotion t) {
                thresholdPromos.add(t);
            } else if (p instanceof VoucherPromotion v) {
                if (voucherCode.isPresent() && v.voucherCode().equalsIgnoreCase(voucherCode.get())) {
                    voucherPromos.add(v);
                }
            }
        }

        // Gom thông tin giỏ hàng theo mã sản phẩm.
        Map<String, Integer> initialInventory = new HashMap<>();
        Map<String, BigDecimal> unitPrices = new HashMap<>();
        Map<String, String> itemCategories = new HashMap<>();

        for (CartItem item : cart.items()) {
            initialInventory.merge(item.productCode(), item.quantity(), Integer::sum);
            unitPrices.put(item.productCode(), item.unitPrice());
            itemCategories.put(item.productCode(), item.category());
        }

        // Tìm phương án khuyến mại cấp dòng.
        SearchContext context = new SearchContext(
                cart, originalTotal, buyXGetYPromos, catPercentPromos, bundlePromos,
                thresholdPromos, voucherPromos, unitPrices, itemCategories
        );

        Plan bestPlan = context.search(initialInventory);

        return buildResult(originalTotal, bestPlan);
    }

    private static PricingResult buildResult(BigDecimal originalTotal, Plan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tổng tiền gốc: ").append(originalTotal).append(" đ.\n");

        if (plan.appliedPromotions().isEmpty()) {
            sb.append("Không áp dụng chương trình khuyến mại nào. ");
            sb.append("Khách trả: ").append(originalTotal).append(" đ.");
            return new PricingResult(originalTotal, Collections.emptyList(), originalTotal, sb.toString());
        }

        sb.append("Khuyến mại được áp dụng:\n");
        for (AppliedPromotion ap : plan.appliedPromotions()) {
            sb.append("- ").append(ap.description()).append("\n");
        }
        sb.append("Số tiền khách phải trả: ").append(plan.finalPayment()).append(" đ.");

        return new PricingResult(
                originalTotal,
                plan.appliedPromotions(),
                plan.finalPayment(),
                sb.toString()
        );
    }

    private static record Plan(
            List<AppliedPromotion> appliedPromotions,
            int linePromoCount,
            int totalPriority,
            BigDecimal finalPayment
    ) {}

    private static final class SearchContext {
        final Cart cart;
        final BigDecimal originalTotal;
        final List<BuyXGetYPromotion> buyXGetYPromos;
        final List<CategoryPercentPromotion> catPercentPromos;
        final List<BundlePromotion> bundlePromos;
        final List<OrderThresholdPromotion> thresholdPromos;
        final List<VoucherPromotion> voucherPromos;
        final Map<String, BigDecimal> unitPrices;
        final Map<String, String> itemCategories;

        Plan bestPlan = null;

        SearchContext(
                Cart cart,
                BigDecimal originalTotal,
                List<BuyXGetYPromotion> buyXGetYPromos,
                List<CategoryPercentPromotion> catPercentPromos,
                List<BundlePromotion> bundlePromos,
                List<OrderThresholdPromotion> thresholdPromos,
                List<VoucherPromotion> voucherPromos,
                Map<String, BigDecimal> unitPrices,
                Map<String, String> itemCategories
        ) {
            this.cart = cart;
            this.originalTotal = originalTotal;
            this.buyXGetYPromos = buyXGetYPromos;
            this.catPercentPromos = catPercentPromos;
            this.bundlePromos = bundlePromos;
            this.thresholdPromos = thresholdPromos;
            this.voucherPromos = voucherPromos;
            this.unitPrices = unitPrices;
            this.itemCategories = itemCategories;
        }

        Plan search(Map<String, Integer> inventory) {
            // Phương án cơ sở: không áp dụng khuyến mại dòng.
            evaluateAndConsider(inventory, Collections.emptyList());

            explore(inventory, new ArrayList<>(), 0);
            return bestPlan;
        }

        private void explore(
                Map<String, Integer> inventory,
                List<PromotionCandidate> currentCandidates,
                int promoIndex
        ) {
            // Mỗi trạng thái đều có thể là phương án tốt nhất hiện tại.
            evaluateAndConsider(inventory, currentCandidates);

            // Sinh các ứng viên cấp dòng từ lượng hàng còn lại.
            List<PromotionCandidate> availableSteps = new ArrayList<>();

            for (BuyXGetYPromotion p : buyXGetYPromos) {
                availableSteps.addAll(p.generateCandidates(inventory, unitPrices));
            }
            for (BundlePromotion p : bundlePromos) {
                availableSteps.addAll(p.generateCandidates(inventory, unitPrices));
            }
            for (CategoryPercentPromotion p : catPercentPromos) {
                availableSteps.addAll(p.generateCandidates(inventory, unitPrices, itemCategories));
            }

            // Ưu tiên nhánh có mức giảm lớn trước.
            availableSteps.sort(Comparator.comparing(PromotionCandidate::discountAmount).reversed());

            for (PromotionCandidate step : availableSteps) {
                if (canConsume(inventory, step.unitsConsumed())) {
                    Map<String, Integer> nextInventory = consume(inventory, step.unitsConsumed());
                    currentCandidates.add(step);

                    explore(nextInventory, currentCandidates, promoIndex + 1);

                    currentCandidates.remove(currentCandidates.size() - 1);
                }
            }
        }

        private void evaluateAndConsider(Map<String, Integer> inventory, List<PromotionCandidate> lineCandidates) {
            BigDecimal lineDiscount = lineCandidates.stream()
                    .map(PromotionCandidate::discountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal afterLine = originalTotal.subtract(lineDiscount);
            if (afterLine.compareTo(BigDecimal.ZERO) < 0) {
                afterLine = BigDecimal.ZERO;
            }

            Map<String, AppliedPromotion> lineMap = new java.util.LinkedHashMap<>();
            Set<String> distinctLinePromoIds = new HashSet<>();
            int totalPriority = 0;

            for (PromotionCandidate cand : lineCandidates) {
                String pId = cand.promotion().id();
                distinctLinePromoIds.add(pId);
                totalPriority += cand.promotion().priority();

                lineMap.compute(pId, (k, existing) -> {
                    if (existing == null) {
                        return new AppliedPromotion(
                                pId,
                                cand.promotion().getClass().getSimpleName(),
                                cand.unitsConsumed(),
                                cand.discountAmount(),
                                cand.description()
                        );
                    } else {
                        Map<String, Integer> mergedUnits = new HashMap<>(existing.unitsConsumed());
                        cand.unitsConsumed().forEach((p, q) -> mergedUnits.merge(p, q, Integer::sum));
                        BigDecimal mergedDiscount = existing.discountAmount().add(cand.discountAmount());
                        String desc = "Áp " + pId + " (giảm " + mergedDiscount + " đ)";
                        return new AppliedPromotion(
                                pId,
                                existing.promotionType(),
                                mergedUnits,
                                mergedDiscount,
                                desc
                        );
                    }
                });
            }
            List<AppliedPromotion> appliedList = new ArrayList<>(lineMap.values());

            // Khuyến mại cấp đơn áp theo thứ tự đề bài.
            BigDecimal afterThreshold = afterLine;
            for (OrderThresholdPromotion otp : thresholdPromos) {
                if (afterLine.compareTo(otp.threshold()) >= 0) {
                    BigDecimal disc = otp.discountAmount().min(afterThreshold);
                    afterThreshold = afterThreshold.subtract(disc);
                    totalPriority += otp.priority();
                    appliedList.add(new AppliedPromotion(
                            otp.id(),
                            "OrderThreshold",
                            Collections.emptyMap(),
                            disc,
                            "Áp " + otp.id() + " (ngưỡng >= " + otp.threshold() + " đ, giảm " + disc + " đ)"
                    ));
                    break; // Tối đa một OrderThreshold
                }
            }

            BigDecimal finalPayment = afterThreshold;
            if (!voucherPromos.isEmpty()) {
                VoucherPromotion vp = voucherPromos.get(0);
                BigDecimal rawVoucherDisc = afterThreshold.multiply(vp.percent())
                        .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                BigDecimal voucherDisc = rawVoucherDisc;
                if (vp.maxDiscount() != null && voucherDisc.compareTo(vp.maxDiscount()) > 0) {
                    voucherDisc = vp.maxDiscount();
                }
                voucherDisc = voucherDisc.min(finalPayment);
                finalPayment = finalPayment.subtract(voucherDisc);
                totalPriority += vp.priority();
                appliedList.add(new AppliedPromotion(
                        vp.id(),
                        "Voucher",
                        Collections.emptyMap(),
                        voucherDisc,
                        "Áp Voucher " + vp.voucherCode() + " (" + vp.percent() + "%, tối đa " + vp.maxDiscount() + " đ, giảm " + voucherDisc + " đ)"
                ));
            }

            Plan candidatePlan = new Plan(appliedList, distinctLinePromoIds.size(), totalPriority, finalPayment);

            if (bestPlan == null || isBetter(candidatePlan, bestPlan)) {
                bestPlan = candidatePlan;
            }
        }

        private boolean isBetter(Plan candidate, Plan current) {
            // Ưu tiên số tiền phải trả thấp hơn.
            int cmpPayment = candidate.finalPayment().compareTo(current.finalPayment());
            if (cmpPayment < 0) return true;
            if (cmpPayment > 0) return false;

            // Nếu bằng tiền, chọn phương án dùng ít khuyến mại dòng hơn.
            if (candidate.linePromoCount() < current.linePromoCount()) return true;
            if (candidate.linePromoCount() > current.linePromoCount()) return false;

            // Nếu vẫn hòa, chọn tổng độ ưu tiên cao hơn.
            return candidate.totalPriority() > current.totalPriority();
        }

        private boolean canConsume(Map<String, Integer> inventory, Map<String, Integer> required) {
            for (Map.Entry<String, Integer> req : required.entrySet()) {
                if (inventory.getOrDefault(req.getKey(), 0) < req.getValue()) {
                    return false;
                }
            }
            return true;
        }

        private Map<String, Integer> consume(Map<String, Integer> inventory, Map<String, Integer> required) {
            Map<String, Integer> copy = new HashMap<>(inventory);
            for (Map.Entry<String, Integer> req : required.entrySet()) {
                copy.put(req.getKey(), copy.get(req.getKey()) - req.getValue());
            }
            return copy;
        }
    }
}
