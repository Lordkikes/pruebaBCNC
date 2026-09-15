package com.bcnc.prices.application;

import com.bcnc.prices.domain.exception.PriceNotFoundException;
import com.bcnc.prices.domain.model.Price;
import com.bcnc.prices.domain.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario de la capa application: se aisla el puerto de salida con un mock,
 * sin levantar contexto de Spring ni base de datos.
 */
@ExtendWith(MockitoExtension.class)
class PriceQueryServiceTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @Test
    void devuelveLaTarifaCuandoElRepositorioLaEncuentra() {
        Price expected = new Price(BRAND_ID, PRODUCT_ID, 1L, 0, APPLICATION_DATE,
                APPLICATION_DATE.plusHours(1), new BigDecimal("35.50"), "EUR");
        when(priceRepositoryPort.findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(Optional.of(expected));

        PriceQueryService service = new PriceQueryService(priceRepositoryPort);
        Price result = service.getApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);

        assertThat(result).isEqualTo(expected);
        verify(priceRepositoryPort).findApplicablePrice(eq(BRAND_ID), eq(PRODUCT_ID), eq(APPLICATION_DATE));
    }

    @Test
    void lanzaPriceNotFoundCuandoElRepositorioNoEncuentraNada() {
        when(priceRepositoryPort.findApplicablePrice(any(), any(), any()))
                .thenReturn(Optional.empty());

        PriceQueryService service = new PriceQueryService(priceRepositoryPort);

        assertThatThrownBy(() -> service.getApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining(String.valueOf(BRAND_ID))
                .hasMessageContaining(String.valueOf(PRODUCT_ID));
    }
}
