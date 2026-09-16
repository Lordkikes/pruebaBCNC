package com.bcnc.prices.infrastructure.adapter.in.web;

import com.bcnc.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * @Order(HIGHEST_PRECEDENCE): sin esto, el ProblemDetailsExceptionHandler
 * autoconfigurado por Spring Boot (spring.mvc.problemdetails.enabled=true) se
 * resuelve antes que este advice para MissingServletRequestParameterException/
 * MethodArgumentTypeMismatchException y devuelve su mensaje genérico en vez del
 * nuestro, aunque este @ExceptionHandler exista.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PriceExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Tarifa no encontrada");
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String paramName = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return "'%s' %s".formatted(paramName, violation.getMessage());
                })
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Parámetros de entrada inválidos");
        return problem;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El parámetro obligatorio '%s' no fue proporcionado.".formatted(ex.getParameterName()));
        problem.setTitle("Falta un parámetro obligatorio");
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = describeExpectedFormat(ex.getRequiredType());
        String detail = "El parámetro '%s' con valor '%s' no es válido: se esperaba %s."
                .formatted(ex.getName(), ex.getValue(), expected);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Parámetro con formato inválido");
        return problem;
    }

    private static String describeExpectedFormat(Class<?> requiredType) {
        if (requiredType == null) {
            return "otro formato";
        }
        if (LocalDateTime.class.isAssignableFrom(requiredType)) {
            return "una fecha/hora en formato ISO-8601 (yyyy-MM-dd'T'HH:mm:ss), p. ej. 2020-06-14T10:00:00";
        }
        if (Number.class.isAssignableFrom(requiredType)) {
            return "un número entero";
        }
        return "un valor de tipo " + requiredType.getSimpleName();
    }
}
