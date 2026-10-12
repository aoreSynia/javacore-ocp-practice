package bai4;

import java.math.BigDecimal;
import java.util.Objects;

public record TransferRequest(
        String idempotencyKey,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount
) {
    public TransferRequest {
        Objects.requireNonNull(idempotencyKey, "idempotencyKey không được null");
        Objects.requireNonNull(fromAccountId, "fromAccountId không được null");
        Objects.requireNonNull(toAccountId, "toAccountId không được null");
        Objects.requireNonNull(amount, "amount không được null");
    }

    public TransferRequest(String idempotencyKey, String fromAccountId, String toAccountId, long amount) {
        this(idempotencyKey, fromAccountId, toAccountId, BigDecimal.valueOf(amount));
    }
}

