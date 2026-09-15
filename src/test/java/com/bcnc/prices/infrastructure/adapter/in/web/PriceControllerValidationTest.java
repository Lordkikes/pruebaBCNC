package com.bcnc.prices.infrastructure.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validación de parámetros de entrada del endpoint y de los casos sin resultado,
 * separados de los 5 casos "felices" del enunciado (ver PriceControllerIntegrationTest).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void faltaBrandId_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void faltaProductId_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void faltaApplicationDate_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void brandIdNoPositivo_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "0")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productIdNegativo_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "-35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void applicationDateConFormatoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "14-06-2020"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinTarifaAplicableParaLosParametros_devuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "999")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isNotFound());
    }
}
