package com.bcnc.prices.infrastructure.adapter.out.persistence;

import com.bcnc.prices.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unitario del adaptador de salida: PriceJpaRepository mockeado, sin levantar
 * Spring ni H2. Verifica que delega con los mismos parámetros y mapea el
 * resultado a dominio.
 */
@ExtendWith(MockitoExtension.class)
class PriceRepositoryAdapterTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Mock
    private PriceJpaRepository priceJpaRepository;

    @Test
    void devuelveElPrecioMapeadoCuandoElRepositorioJpaLoEncuentra() {
        PriceEntity entity = new PriceEntity();
        ReflectionTestUtils.setField(entity, "brandId", BRAND_ID);
        ReflectionTestUtils.setField(entity, "productId", PRODUCT_ID);
        ReflectionTestUtils.setField(entity, "priceList", 1L);
        ReflectionTestUtils.setField(entity, "priority", 0);
        ReflectionTestUtils.setField(entity, "startDate", LocalDateTime.of(2020, 6, 14, 0, 0));
        ReflectionTestUtils.setField(entity, "endDate", LocalDateTime.of(2020, 12, 31, 23, 59, 59));
        ReflectionTestUtils.setField(entity, "price", new BigDecimal("35.50"));
        ReflectionTestUtils.setField(entity, "curr", "EUR");

        when(priceJpaRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                BRAND_ID, PRODUCT_ID, APPLICATION_DATE, APPLICATION_DATE))
                .thenReturn(Optional.of(entity));

        PriceRepositoryAdapter adapter = new PriceRepositoryAdapter(priceJpaRepository);
        Optional<Price> result = adapter.findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(1L);
        assertThat(result.get().price()).isEqualByComparingTo("35.50");

        verify(priceJpaRepository)
                .findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        BRAND_ID, PRODUCT_ID, APPLICATION_DATE, APPLICATION_DATE);
    }

    @Test
    void devuelveOptionalVacioCuandoElRepositorioJpaNoEncuentraNada() {
        when(priceJpaRepository.findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                BRAND_ID, PRODUCT_ID, APPLICATION_DATE, APPLICATION_DATE))
                .thenReturn(Optional.empty());

        PriceRepositoryAdapter adapter = new PriceRepositoryAdapter(priceJpaRepository);
        Optional<Price> result = adapter.findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);

        assertThat(result).isEmpty();
    }
}
