package com.bcnc.prices.infrastructure.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los 5 casos de prueba exigidos por el enunciado (TestJava2026.txt) para el
 * producto 35455 de la brand 1 (ZARA), contra los datos de ejemplo cargados en H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test1_peticionA_las10_00_delDia14_devuelveTarifa1() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test2_peticionA_las16_00_delDia14_devuelveTarifa2() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T16:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test3_peticionA_las21_00_delDia14_devuelveTarifa1() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T21:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test4_peticionA_las10_00_delDia15_devuelveTarifa3() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-15T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(3))
                .andExpect(jsonPath("$.price").value(30.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test5_peticionA_las21_00_delDia16_devuelveTarifa4() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-16T21:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(4))
                .andExpect(jsonPath("$.price").value(38.95))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }
}
