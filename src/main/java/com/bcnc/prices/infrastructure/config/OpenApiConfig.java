package com.bcnc.prices.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pricesServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BCNC - Servicio de Consulta de Precios")
                        .description("Prueba técnica: consulta de la tarifa y precio final aplicable "
                                + "a un producto de una cadena en una fecha determinada.")
                        .version("v1"));
    }
}
