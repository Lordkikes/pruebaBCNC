package com.bcnc.prices.domain.port.in;

import com.bcnc.prices.domain.model.Price;

import java.time.LocalDateTime;

public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
