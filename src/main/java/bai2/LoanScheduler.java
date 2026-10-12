package bai2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LoanScheduler {

    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
    private static final BigDecimal HALF = new BigDecimal("0.50");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private LoanScheduler() {
    }

    public static List<Installment> generate(LoanRequest request, Method method) {
        validateRequest(request);
        if (method == null) {
            throw new InvalidLoanRequestException("Phương thức trả nợ không được null");
        }

        BigDecimal annualDecimalRate = resolveAnnualRate(request.annualInterestRate());
        BigDecimal monthlyRate = annualDecimalRate.divide(TWELVE, 16, RoundingMode.HALF_UP);

        return switch (method) {
            case GOC_DEU -> generateGocDeu(request, monthlyRate);
            case TRA_DEU -> generateTraDeu(request, monthlyRate);
        };
    }

    public static LoanComparisonResult compareMethods(LoanRequest request) {
        List<Installment> gocDeu = generate(request, Method.GOC_DEU);
        List<Installment> traDeu = generate(request, Method.TRA_DEU);

        BigDecimal totalInterestGocDeu = gocDeu.stream()
                .map(Installment::interest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterestTraDeu = traDeu.stream()
                .map(Installment::interest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = totalInterestTraDeu.subtract(totalInterestGocDeu);

        return new LoanComparisonResult(
                totalInterestGocDeu,
                totalInterestTraDeu,
                diff,
                gocDeu,
                traDeu
        );
    }

    private static List<Installment> generateGocDeu(LoanRequest request, BigDecimal monthlyRate) {
        int n = request.termInMonths();
        BigDecimal currentBalance = request.loanAmount().setScale(0, RoundingMode.HALF_UP);
        BigDecimal basePrincipal = request.loanAmount().divide(BigDecimal.valueOf(n), 0, RoundingMode.HALF_UP);

        List<Installment> installments = new ArrayList<>(n);

        for (int k = 1; k <= n; k++) {
            LocalDate dueDate = calculateDueDate(request.disbursementDate(), k);
            BigDecimal openingBalance = currentBalance;
            BigDecimal interest = openingBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

            BigDecimal principal;
            if (k == n) {
                principal = openingBalance;
            } else {
                principal = basePrincipal;
            }

            BigDecimal payment = principal.add(interest);
            BigDecimal closingBalance = openingBalance.subtract(principal);
            currentBalance = closingBalance;

            installments.add(new Installment(k, dueDate, openingBalance, principal, interest, payment, closingBalance));
        }

        return installments;
    }

    private static List<Installment> generateTraDeu(LoanRequest request, BigDecimal monthlyRate) {
        int n = request.termInMonths();
        BigDecimal currentBalance = request.loanAmount().setScale(0, RoundingMode.HALF_UP);

        double r = monthlyRate.doubleValue();
        double p = request.loanAmount().doubleValue();
        BigDecimal targetPayment;
        if (r == 0) {
            targetPayment = request.loanAmount().divide(BigDecimal.valueOf(n), 0, RoundingMode.HALF_UP);
        } else {
            double annuity = (p * r) / (1.0 - Math.pow(1.0 + r, -n));
            targetPayment = BigDecimal.valueOf(annuity).setScale(0, RoundingMode.HALF_UP);
        }

        List<Installment> installments = new ArrayList<>(n);

        for (int k = 1; k <= n; k++) {
            LocalDate dueDate = calculateDueDate(request.disbursementDate(), k);
            BigDecimal openingBalance = currentBalance;
            BigDecimal interest = openingBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

            BigDecimal principal;
            BigDecimal payment;
            BigDecimal closingBalance;

            if (k == n) {
                // Kỳ cuối của cả hai phương pháp trả toàn bộ dư nợ còn lại để dư nợ cuối bằng đúng 0
                principal = openingBalance;
                payment = principal.add(interest);
                closingBalance = BigDecimal.ZERO;
            } else {
                principal = targetPayment.subtract(interest);
                payment = targetPayment;
                closingBalance = openingBalance.subtract(principal);
            }

            currentBalance = closingBalance;
            installments.add(new Installment(k, dueDate, openingBalance, principal, interest, payment, closingBalance));
        }

        return installments;
    }

    private static LocalDate calculateDueDate(LocalDate disbursementDate, int k) {
        LocalDate baseDate = disbursementDate.plusMonths(k);
        DayOfWeek dow = baseDate.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return baseDate.plusDays(2);
        } else if (dow == DayOfWeek.SUNDAY) {
            return baseDate.plusDays(1);
        }
        return baseDate;
    }

    private static BigDecimal resolveAnnualRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanRequestException("Lãi suất không được âm");
        }
        if (rate.compareTo(HALF) <= 0) {
            return rate;
        }
        if (rate.compareTo(FIFTY) <= 0) {
            return rate.divide(HUNDRED, 8, RoundingMode.HALF_UP);
        }
        throw new InvalidLoanRequestException("Lãi suất không được vượt quá 50%/năm: " + rate);
    }

    private static void validateRequest(LoanRequest request) {
        if (request == null) {
            throw new InvalidLoanRequestException("Yêu cầu vay không được null");
        }
        if (request.loanAmount() == null || request.loanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanRequestException("Số tiền vay phải lớn hơn 0");
        }
        if (request.annualInterestRate() == null) {
            throw new InvalidLoanRequestException("Lãi suất không được null");
        }
        if (request.termInMonths() < 1 || request.termInMonths() > 360) {
            throw new InvalidLoanRequestException("Số kỳ vay phải từ 1 đến 360 tháng: " + request.termInMonths());
        }
        if (request.disbursementDate() == null) {
            throw new InvalidLoanRequestException("Ngày giải ngân không được null");
        }
    }
}
