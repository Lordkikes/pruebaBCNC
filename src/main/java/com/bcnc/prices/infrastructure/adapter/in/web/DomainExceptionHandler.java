package com.bcnc.prices.infrastructure.adapter.in.web;

import com.bcnc.prices.domain.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce excepciones de dominio a respuestas HTTP. Cambia cuando cambia el
 * vocabulario de negocio (nuevas excepciones de dominio), no cuando cambian
 * las reglas de validación de la petición HTTP — ver
 * {@link RequestValidationExceptionHandler}.
 */
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Tarifa no encontrada");
        return problem;
    }
}
