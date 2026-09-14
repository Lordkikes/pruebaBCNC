package com.bcnc.prices.infrastructure.adapter.in.web;

import com.bcnc.prices.domain.port.in.GetApplicablePriceUseCase;
import com.bcnc.prices.infrastructure.adapter.in.web.dto.PriceResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Validated
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @GetMapping("/api/v1/prices")
    public PriceResponse getApplicablePrice(
            @RequestParam("brandId") @NotNull Long brandId,
            @RequestParam("productId") @NotNull Long productId,
            @RequestParam("applicationDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NotNull LocalDateTime applicationDate
    ) {
        var price = getApplicablePriceUseCase.getApplicablePrice(brandId, productId, applicationDate);
        return PriceResponse.fromDomain(price);
    }
}
