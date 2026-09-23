package com.bcnc.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Tarifas de brandId+productId que cubren applicationDate (start_date <=
     * fecha <= end_date), de mayor a menor PRIORITY. El adaptador pide solo
     * la primera página de tamaño 1 (ver PriceRepositoryAdapter), por lo que
     * la base de datos aplica un LIMIT 1 real: nunca se cargan más filas de
     * las necesarias.
     */
    @Query("""
            SELECT p FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND p.startDate <= :applicationDate
              AND p.endDate >= :applicationDate
            ORDER BY p.priority DESC
            """)
    List<PriceEntity> findApplicableCandidates(Long brandId, Long productId, LocalDateTime applicationDate, Pageable pageable);
}
