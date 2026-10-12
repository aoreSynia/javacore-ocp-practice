package bai4;

import java.math.BigDecimal;

public record TransferResult(
        String journalId,
        String idempotencyKey,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal fromBalanceAfter,
        BigDecimal toBalanceAfter,
        BigDecimal feeIncomeBalanceAfter
) {
}

