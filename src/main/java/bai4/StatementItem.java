package bai4;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StatementItem(
        String journalId,
        Instant timestamp,
        LocalDate date,
        EntryType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description
) {
}

