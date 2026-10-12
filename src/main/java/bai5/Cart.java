package bai5;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record Cart(
        List<CartItem> items
) {
    public Cart {
        Objects.requireNonNull(items, "Danh sách sản phẩm không được null");
        items = Collections.unmodifiableList(items);
    }

    public BigDecimal totalOriginalPrice() {
        return items.stream()
                .map(CartItem::totalOriginalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

