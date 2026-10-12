package bai4;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Một dòng phát sinh trong bút toán kép (Nợ hoặc Có của một tài khoản).
 */
public record JournalLine(
        String accountId,
        EntryType type,
        BigDecimal amount
) {
    public JournalLine {
        Objects.requireNonNull(accountId, "Mã tài khoản không được null");
        Objects.requireNonNull(type, "Loại Nợ/Có không được null");
        Objects.requireNonNull(amount, "Số tiền không được null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Số tiền dòng bút toán phải lớn hơn 0");
        }
    }
}

