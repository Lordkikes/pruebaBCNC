package com.bcnc.prices.infrastructure.adapter.out.persistence;

import com.bcnc.prices.domain.model.Price;
import com.bcnc.prices.domain.port.out.PriceRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private static final int ONLY_THE_HIGHEST_PRIORITY_MATCH = 1;

    private final PriceJpaRepository priceJpaRepository;

    public PriceRepositoryAdapter(PriceJpaRepository priceJpaRepository) {
        this.priceJpaRepository = priceJpaRepository;
    }

    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        var topMatch = PageRequest.of(0, ONLY_THE_HIGHEST_PRIORITY_MATCH);
        return priceJpaRepository
                .findApplicableCandidates(brandId, productId, applicationDate, topMatch)
                .stream()
                .findFirst()
                .map(PriceEntityMapper::toDomain);
    }
}
