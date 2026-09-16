package com.bcnc.prices.infrastructure.adapter.out.persistence;

import com.bcnc.prices.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unitario puro: sin Spring context, sin BD. PriceEntity no expone constructor
 * con argumentos (JPA gestiona su ciclo de vida), así que se rellena por
 * reflexión igual que haría Hibernate al hidratar una fila.
 */
class PriceEntityMapperTest {

    @Test
    void mapeaTodosLosCamposDeEntidadADominio() {
        PriceEntity entity = new PriceEntity();
        ReflectionTestUtils.setField(entity, "brandId", 1L);
        ReflectionTestUtils.setField(entity, "productId", 35455L);
        ReflectionTestUtils.setField(entity, "priceList", 2L);
        ReflectionTestUtils.setField(entity, "priority", 1);
        ReflectionTestUtils.setField(entity, "startDate", LocalDateTime.of(2020, 6, 14, 15, 0));
        ReflectionTestUtils.setField(entity, "endDate", LocalDateTime.of(2020, 6, 14, 18, 30));
        ReflectionTestUtils.setField(entity, "price", new BigDecimal("25.45"));
        ReflectionTestUtils.setField(entity, "curr", "EUR");

        Price price = PriceEntityMapper.toDomain(entity);

        assertThat(price.brandId()).isEqualTo(1L);
        assertThat(price.productId()).isEqualTo(35455L);
        assertThat(price.priceList()).isEqualTo(2L);
        assertThat(price.priority()).isEqualTo(1);
        assertThat(price.startDate()).isEqualTo(LocalDateTime.of(2020, 6, 14, 15, 0));
        assertThat(price.endDate()).isEqualTo(LocalDateTime.of(2020, 6, 14, 18, 30));
        assertThat(price.price()).isEqualByComparingTo("25.45");
        assertThat(price.currency()).isEqualTo("EUR");
    }
}
