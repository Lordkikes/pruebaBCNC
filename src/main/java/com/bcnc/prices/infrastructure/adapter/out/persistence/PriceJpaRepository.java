package com.bcnc.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Filtra por brand + producto + rango de fechas y resuelve la desambiguación
     * por PRIORITY (mayor primero) en la propia consulta, devolviendo un único
     * resultado sin cargar todas las filas en memoria.
     */
    Optional<PriceEntity> findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            Long brandId,
            Long productId,
            LocalDateTime applicationDateAfterStart,
            LocalDateTime applicationDateBeforeEnd
    );
}
