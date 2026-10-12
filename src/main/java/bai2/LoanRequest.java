package bai2;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRequest(
        BigDecimal loanAmount,
        BigDecimal annualInterestRate,
        int termInMonths,
        LocalDate disbursementDate
) {
    public LoanRequest(long loanAmount, double annualInterestRatePercent, int termInMonths, LocalDate disbursementDate) {
        this(BigDecimal.valueOf(loanAmount), BigDecimal.valueOf(annualInterestRatePercent), termInMonths, disbursementDate);
    }
}
