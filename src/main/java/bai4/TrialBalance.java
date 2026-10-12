package bai4;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public record TrialBalance(
        Map<String, BigDecimal> accountBalances,
        BigDecimal totalBalance
) {
    public TrialBalance {
        accountBalances = Collections.unmodifiableMap(accountBalances);
    }
}

