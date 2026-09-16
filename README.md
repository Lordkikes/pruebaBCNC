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

## Manejo de errores

El endpoint valida `brandId`, `productId` y `applicationDate`. Cualquier fallo de validación o de formato devuelve un `ProblemDetail` (RFC 7807), nunca un 500.

### Fecha/hora inválida (formato incorrecto)

`applicationDate` debe ir en formato ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`, p. ej. `2020-06-14T10:00:00`). Si no lo cumple, se devuelve **400 Bad Request**:

```bash
curl "http://localhost:8080/api/v1/prices?brandId=1&productId=35455&applicationDate=14-06-2020"
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Failed to convert 'applicationDate' with value: '14-06-2020'",
  "instance": "/api/v1/prices"
}
```

Si falta cualquiera de los tres parámetros (`brandId`, `productId` o `applicationDate`), la respuesta es igualmente **400 Bad Request**.

### Identificadores no válidos (`brandId`/`productId` ≤ 0)

```json
{
  "type": "about:blank",
  "title": "Parámetros de entrada inválidos",
  "status": 400,
  "detail": "getApplicablePrice.brandId: debe ser mayor que 0",
  "instance": "/api/v1/prices"
}
```

### Sin tarifa aplicable

Parámetros válidos pero sin ninguna tarifa que cubra esa fecha/producto/cadena → **404 Not Found**:

```json
{
  "type": "about:blank",
  "title": "Tarifa no encontrada",
  "status": 404,
  "detail": "No existe tarifa aplicable para brandId=999, productId=35455 en la fecha 2020-06-14T10:00",
  "instance": "/api/v1/prices"
}
```

Ver los casos cubiertos en [`PriceControllerValidationTest`](src/test/java/com/bcnc/prices/infrastructure/adapter/in/web/PriceControllerValidationTest.java).

## Tests

```bash
./mvnw test
```

Incluye los 5 tests de integración exigidos por el enunciado (`PriceControllerIntegrationTest`), uno por cada franja horaria: 14/06 10:00, 14/06 16:00, 14/06 21:00, 15/06 10:00 y 16/06 21:00.

## Colección Postman

En [`postman/BCNC-Prices-Service.postman_collection.json`](postman/BCNC-Prices-Service.postman_collection.json) hay una colección con la misma suite de pruebas, para verificar el servicio manualmente o con [newman](https://www.npmjs.com/package/newman):

- **Enunciado - 5 casos**: los mismos 5 escenarios de `PriceControllerIntegrationTest`, cada uno con asserts sobre `priceList`, `price` y `currency`.
- **Validación de parámetros y errores**: parámetros ausentes, `brandId`/`productId` no positivos, fecha con formato inválido (400) y sin tarifa aplicable (404).

Con la aplicación arrancada (`./mvnw spring-boot:run`):

```bash
npx newman run postman/BCNC-Prices-Service.postman_collection.json
```

También se puede importar el fichero directamente en Postman (usa la variable de colección `baseUrl`, por defecto `http://localhost:8080`).
