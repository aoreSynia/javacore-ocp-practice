package bai5;

import java.util.Objects;

public record Customer(
        String id,
        CustomerTier tier
) {
    public Customer {
        Objects.requireNonNull(id, "Mã khách hàng không được null");
        Objects.requireNonNull(tier, "Hạng khách hàng không được null");
    }
}

