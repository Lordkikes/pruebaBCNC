package com.bcnc.prices.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PriceTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final Long PRICE_LIST = 1L;
    private static final LocalDateTime START = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
    private static final BigDecimal AMOUNT = new BigDecimal("35.50");
    private static final String CURRENCY = "EUR";

    @Test
    void seConstruyeConTodosLosCamposValidos() {
        Price price = new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, 0, START, END, AMOUNT, CURRENCY);

        assertThat(price.brandId()).isEqualTo(BRAND_ID);
        assertThat(price.productId()).isEqualTo(PRODUCT_ID);
        assertThat(price.priceList()).isEqualTo(PRICE_LIST);
        assertThat(price.priority()).isZero();
        assertThat(price.startDate()).isEqualTo(START);
        assertThat(price.endDate()).isEqualTo(END);
        assertThat(price.price()).isEqualByComparingTo(AMOUNT);
        assertThat(price.currency()).isEqualTo(CURRENCY);
    }

    @ParameterizedTest
    @MethodSource("camposObligatoriosNulos")
    void rechazaCamposObligatoriosNulos(Long brandId, Long productId, Long priceList,
                                         LocalDateTime start, LocalDateTime end,
                                         BigDecimal amount, String currency) {
        assertThatNullPointerException().isThrownBy(() ->
                new Price(brandId, productId, priceList, 0, start, end, amount, currency));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> camposObligatoriosNulos() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(null, PRODUCT_ID, PRICE_LIST, START, END, AMOUNT, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, null, PRICE_LIST, START, END, AMOUNT, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, PRODUCT_ID, null, START, END, AMOUNT, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, PRODUCT_ID, PRICE_LIST, null, END, AMOUNT, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, PRODUCT_ID, PRICE_LIST, START, null, AMOUNT, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, PRODUCT_ID, PRICE_LIST, START, END, null, CURRENCY),
                org.junit.jupiter.params.provider.Arguments.of(BRAND_ID, PRODUCT_ID, PRICE_LIST, START, END, AMOUNT, null)
        );
    }
}
