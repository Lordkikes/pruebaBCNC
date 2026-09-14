package com.bcnc.prices.application;

import com.bcnc.prices.domain.exception.PriceNotFoundException;
import com.bcnc.prices.domain.model.Price;
import com.bcnc.prices.domain.port.in.GetApplicablePriceUseCase;
import com.bcnc.prices.domain.port.out.PriceRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PriceQueryService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    public PriceQueryService(PriceRepositoryPort priceRepositoryPort) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Price getApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        return priceRepositoryPort.findApplicablePrice(brandId, productId, applicationDate)
                .orElseThrow(() -> new PriceNotFoundException(brandId, productId, applicationDate.toString()));
    }
}
