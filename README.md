# Prueba Técnica BCNC — Servicio de Consulta de Precios

Servicio Spring Boot que expone un endpoint REST para consultar la tarifa y el precio final aplicable a un producto de una cadena en una fecha determinada, según la tabla `PRICES` del enunciado (ver [`TestJava2026.txt`](TestJava2026.txt)).

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- H2 en memoria (se inicializa sola al arrancar con los datos de ejemplo)
- Arquitectura Hexagonal (`domain` / `application` / `infrastructure`)

## Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`. La base de datos H2 se crea e inicializa automáticamente (`schema.sql` + `data.sql`) con los 4 registros del enunciado para el producto `35455` / brand `1` (ZARA).

## Endpoint

```
GET /api/v1/prices?brandId={brandId}&productId={productId}&applicationDate={ISO-8601}
```

Ejemplo:

```bash
curl "http://localhost:8080/api/v1/prices?brandId=1&productId=35455&applicationDate=2020-06-14T10:00:00"
```

Respuesta:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

## Tests

```bash
./mvnw test
```

Incluye los 5 tests de integración exigidos por el enunciado (`PriceControllerIntegrationTest`), uno por cada franja horaria: 14/06 10:00, 14/06 16:00, 14/06 21:00, 15/06 10:00 y 16/06 21:00.
