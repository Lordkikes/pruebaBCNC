package com.bcnc.prices.infrastructure.adapter.in.web;

import com.bcnc.prices.domain.port.in.GetApplicablePriceUseCase;
import com.bcnc.prices.infrastructure.adapter.in.web.dto.PriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Validated
@Tag(name = "Prices", description = "Consulta de tarifas y precio final aplicable")
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @Operation(
            summary = "Consulta la tarifa aplicable a un producto de una cadena en una fecha",
            description = "Devuelve un único resultado: la tarifa de mayor PRIORITY entre las que "
                    + "solapan la fecha indicada para el producto y la cadena dados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarifa encontrada",
                    content = @Content(schema = @Schema(implementation = PriceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos o mal formados",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No existe tarifa aplicable para esos parámetros",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/api/v1/prices")
    public PriceResponse getApplicablePrice(
            @Parameter(description = "Identificador de la cadena (brand)", example = "1")
            @RequestParam("brandId") @NotNull @Positive Long brandId,
            @Parameter(description = "Identificador del producto", example = "35455")
            @RequestParam("productId") @NotNull @Positive Long productId,
            @Parameter(description = "Fecha/hora de aplicación en formato ISO-8601", example = "2020-06-14T10:00:00")
            @RequestParam("applicationDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NotNull LocalDateTime applicationDate
    ) {
        var price = getApplicablePriceUseCase.getApplicablePrice(brandId, productId, applicationDate);
        return PriceResponse.fromDomain(price);
    }
}
