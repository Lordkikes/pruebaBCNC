package com.bcnc.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Price(
        Long brandId,
        Long productId,
        Long priceList,
        int priority,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency
) {
    public Price {
        Objects.requireNonNull(brandId, "brandId no puede ser null");
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(priceList, "priceList no puede ser null");
        Objects.requireNonNull(startDate, "startDate no puede ser null");
        Objects.requireNonNull(endDate, "endDate no puede ser null");
        Objects.requireNonNull(price, "price no puede ser null");
        Objects.requireNonNull(currency, "currency no puede ser null");
    }
}
