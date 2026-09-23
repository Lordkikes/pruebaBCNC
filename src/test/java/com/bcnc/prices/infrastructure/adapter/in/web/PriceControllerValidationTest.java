package com.bcnc.prices.infrastructure.adapter.in.web;

import com.bcnc.prices.domain.exception.PriceNotFoundException;
import com.bcnc.prices.domain.port.in.GetApplicablePriceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba únicamente el adaptador web: validación de parámetros y traducción
 * de excepciones a ProblemDetail. El caso de uso va mockeado — no se levanta
 * JPA ni H2 — para que estos tests verifiquen solo la responsabilidad de
 * PriceController/los exception handlers, no la del repositorio (eso lo
 * cubre PriceJpaRepositoryTest) ni la lógica de negocio (PriceQueryServiceTest).
 *
 * Cada caso comprueba el cuerpo del ProblemDetail (title/detail), no solo el
 * código HTTP: un mensaje que vuelva a ser el genérico de Spring debe romper
 * el test, no pasar en silencio.
 */
@WebMvcTest(PriceController.class)
class PriceControllerValidationTest {

    private static final String BASE_PARAMS = "productId=35455&applicationDate=2020-06-14T10:00:00";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetApplicablePriceUseCase getApplicablePriceUseCase;

    @Test
    void faltaBrandId_devuelve400ConDetalleDelParametroQueFalta() throws Exception {
        mockMvc.perform(get("/api/v1/prices?" + BASE_PARAMS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Falta un parámetro obligatorio"))
                .andExpect(jsonPath("$.detail").value("El parámetro obligatorio 'brandId' no fue proporcionado."));

        verifyNoInteractions(getApplicablePriceUseCase);
    }

    @Test
    void faltaProductId_devuelve400ConDetalleDelParametroQueFalta() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Falta un parámetro obligatorio"))
                .andExpect(jsonPath("$.detail").value("El parámetro obligatorio 'productId' no fue proporcionado."));
    }

    @Test
    void faltaApplicationDate_devuelve400ConDetalleDelParametroQueFalta() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Falta un parámetro obligatorio"))
                .andExpect(jsonPath("$.detail").value("El parámetro obligatorio 'applicationDate' no fue proporcionado."));
    }

    @Test
    void brandIdCero_devuelve400PorNoSerPositivo() throws Exception {
        mockMvc.perform(get("/api/v1/prices?brandId=0&" + BASE_PARAMS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetros de entrada inválidos"))
                .andExpect(jsonPath("$.detail").value("'brandId' debe ser mayor que 0"));

        verifyNoInteractions(getApplicablePriceUseCase);
    }

    @Test
    void productIdNegativo_devuelve400PorNoSerPositivo() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "-35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetros de entrada inválidos"))
                .andExpect(jsonPath("$.detail").value("'productId' debe ser mayor que 0"));
    }

    @Test
    void brandIdNoNumerico_devuelve400ConElValorRecibido() throws Exception {
        mockMvc.perform(get("/api/v1/prices?brandId=abc&" + BASE_PARAMS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetro con formato inválido"))
                .andExpect(jsonPath("$.detail").value(
                        "El parámetro 'brandId' con valor 'abc' no es válido: se esperaba un número entero."));

        verifyNoInteractions(getApplicablePriceUseCase);
    }

    @Test
    void productIdNoNumerico_devuelve400ConElValorRecibido() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "treintaycinco")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetro con formato inválido"))
                .andExpect(jsonPath("$.detail").value(
                        "El parámetro 'productId' con valor 'treintaycinco' no es válido: se esperaba un número entero."));
    }

    @Test
    void applicationDateConFormatoInvalido_devuelve400ExplicandoElFormatoEsperado() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "1")
                        .param("productId", "35455")
                        .param("applicationDate", "14-06-2020"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetro con formato inválido"))
                .andExpect(jsonPath("$.detail").value(
                        "El parámetro 'applicationDate' con valor '14-06-2020' no es válido: se esperaba una fecha/hora "
                                + "en formato ISO-8601 (yyyy-MM-dd'T'HH:mm:ss), p. ej. 2020-06-14T10:00:00."));

        verifyNoInteractions(getApplicablePriceUseCase);
    }

    @Test
    void sinTarifaAplicableParaLosParametros_devuelve404ConElMensajeDeDominio() throws Exception {
        when(getApplicablePriceUseCase.getApplicablePrice(eq(999L), eq(35455L), any()))
                .thenThrow(new PriceNotFoundException(999L, 35455L, "2020-06-14T10:00"));

        mockMvc.perform(get("/api/v1/prices")
                        .param("brandId", "999")
                        .param("productId", "35455")
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Tarifa no encontrada"))
                .andExpect(jsonPath("$.detail").value(
                        "No existe tarifa aplicable para brandId=999, productId=35455 en la fecha 2020-06-14T10:00"));
    }
}
