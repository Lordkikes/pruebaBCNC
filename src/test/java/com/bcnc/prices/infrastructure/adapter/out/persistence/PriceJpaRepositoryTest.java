package com.bcnc.prices.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la query real (JPA/H2), no un mock: es la única capa donde la regla
 * "gana la de mayor PRIORITY" y la inclusión/exclusión de los bordes de fecha
 * se pueden verificar de forma aislada y con datos construidos a propósito
 * para cada caso límite, en vez de depender del dataset fijo del enunciado.
 *
 * Cada fixture usa un productId propio (nunca 35455, el del enunciado) para
 * no interferir con las filas que carga data.sql en la misma tabla.
 */
@DataJpaTest
class PriceJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceJpaRepository priceJpaRepository;

    @Test
    void ganaLaTarifaDeMayorPrioridadCuandoDosRangosSolapan() {
        persist(price(1L, 100L, "2024-01-01T00:00:00", "2024-01-31T23:59:59", 0, 1L, "20.00"));
        persist(price(1L, 100L, "2024-01-10T00:00:00", "2024-01-20T23:59:59", 1, 2L, "45.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-01-15T12:00:00");

        assertThat(result).isPresent();
        assertThat(result.get().getPriceList()).isEqualTo(2L);
        assertThat(result.get().getPriority()).isEqualTo(1);
    }

    @Test
    void aplicaLaUnicaTarifaVigenteCuandoLosRangosNoSolapanEnEseInstante() {
        persist(price(1L, 100L, "2024-01-01T00:00:00", "2024-01-31T23:59:59", 0, 1L, "20.00"));
        persist(price(1L, 100L, "2024-02-10T00:00:00", "2024-02-20T23:59:59", 1, 2L, "45.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-01-15T12:00:00");

        assertThat(result).isPresent();
        assertThat(result.get().getPriceList()).isEqualTo(1L);
    }

    @Test
    void incluyeElInstanteExactoDeStartDate() {
        persist(price(1L, 100L, "2024-04-10T09:00:00", "2024-04-10T18:00:00", 0, 1L, "20.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-04-10T09:00:00");

        assertThat(result).isPresent();
    }

    @Test
    void incluyeElInstanteExactoDeEndDate() {
        persist(price(1L, 100L, "2024-04-10T09:00:00", "2024-04-10T18:00:00", 0, 1L, "20.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-04-10T18:00:00");

        assertThat(result).isPresent();
    }

    @Test
    void excluyeUnSegundoAntesDelStartDate() {
        persist(price(1L, 100L, "2024-04-10T09:00:00", "2024-04-10T18:00:00", 0, 1L, "20.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-04-10T08:59:59");

        assertThat(result).isEmpty();
    }

    @Test
    void excluyeUnSegundoDespuesDelEndDate() {
        persist(price(1L, 100L, "2024-04-10T09:00:00", "2024-04-10T18:00:00", 0, 1L, "20.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-04-10T18:00:01");

        assertThat(result).isEmpty();
    }

    @Test
    void noDevuelveTarifasDeOtraCadenaAunqueTenganMayorPrioridad() {
        persist(price(1L, 100L, "2024-05-01T00:00:00", "2024-05-31T23:59:59", 0, 1L, "20.00"));
        persist(price(2L, 100L, "2024-05-01T00:00:00", "2024-05-31T23:59:59", 9, 99L, "999.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-05-15T00:00:00");

        assertThat(result).isPresent();
        assertThat(result.get().getBrandId()).isEqualTo(1L);
        assertThat(result.get().getPriceList()).isEqualTo(1L);
    }

    @Test
    void noDevuelveTarifasDeOtroProductoAunqueTenganMayorPrioridad() {
        persist(price(1L, 100L, "2024-06-01T00:00:00", "2024-06-30T23:59:59", 0, 1L, "20.00"));
        persist(price(1L, 200L, "2024-06-01T00:00:00", "2024-06-30T23:59:59", 9, 99L, "999.00"));

        Optional<PriceEntity> result = findAt(1L, 100L, "2024-06-15T00:00:00");

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(100L);
        assertThat(result.get().getPriceList()).isEqualTo(1L);
    }

    @Test
    void devuelveVacioSiNoExisteNingunaFilaParaEsaCadenaOProducto() {
        persist(price(1L, 100L, "2024-07-01T00:00:00", "2024-07-31T23:59:59", 0, 1L, "20.00"));

        assertThat(findAt(3L, 100L, "2024-07-15T00:00:00")).isEmpty();
        assertThat(findAt(1L, 300L, "2024-07-15T00:00:00")).isEmpty();
    }

    private Optional<PriceEntity> findAt(Long brandId, Long productId, String applicationDate) {
        LocalDateTime date = LocalDateTime.parse(applicationDate);
        return priceJpaRepository
                .findApplicableCandidates(brandId, productId, date, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    private void persist(PriceEntity entity) {
        entityManager.persistAndFlush(entity);
    }

    private static PriceEntity price(Long brandId, Long productId, String start, String end,
                                      int priority, Long priceList, String price) {
        PriceEntity entity = new PriceEntity();
        ReflectionTestUtils.setField(entity, "brandId", brandId);
        ReflectionTestUtils.setField(entity, "productId", productId);
        ReflectionTestUtils.setField(entity, "startDate", LocalDateTime.parse(start));
        ReflectionTestUtils.setField(entity, "endDate", LocalDateTime.parse(end));
        ReflectionTestUtils.setField(entity, "priority", priority);
        ReflectionTestUtils.setField(entity, "priceList", priceList);
        ReflectionTestUtils.setField(entity, "price", new BigDecimal(price));
        ReflectionTestUtils.setField(entity, "curr", "EUR");
        return entity;
    }
}
