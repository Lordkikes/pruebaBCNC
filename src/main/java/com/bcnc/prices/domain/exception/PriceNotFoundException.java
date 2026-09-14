package com.bcnc.prices.domain.exception;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long brandId, Long productId, String applicationDate) {
        super("No existe tarifa aplicable para brandId=%s, productId=%s en la fecha %s"
                .formatted(brandId, productId, applicationDate));
    }
}
