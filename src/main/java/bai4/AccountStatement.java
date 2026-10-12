package bai4;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record AccountStatement(
        String accountId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal openingBalance,
        List<StatementItem> items,
        BigDecimal closingBalance
) {
    public AccountStatement {
        items = Collections.unmodifiableList(items);
    }
}

