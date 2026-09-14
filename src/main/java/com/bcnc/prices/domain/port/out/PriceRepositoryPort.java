package com.bcnc.prices.domain.port.out;

import com.bcnc.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepositoryPort {

    /**
     * Devuelve la tarifa de mayor PRIORITY entre las que solapan applicationDate
     * para el brand y producto dados, resuelto en la propia consulta.
     */
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
