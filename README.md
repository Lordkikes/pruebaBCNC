# Prueba Técnica BCNC — Servicio de Consulta de Precios

Servicio Spring Boot que expone un endpoint REST para consultar la tarifa y el precio final aplicable a un producto de una cadena en una fecha determinada, según la tabla `PRICES` del enunciado (ver [`TestJava2026.txt`](TestJava2026.txt)).

Este documento explica no solo cómo arrancarlo, sino **por qué** está construido así: qué hace cada tecnología, cómo están repartidas las responsabilidades entre capas y qué decisiones técnicas se tomaron y por qué.

## Stack

| Tecnología | Para qué se usa aquí |
|---|---|
| **Java 21 (LTS)** | Versión con soporte a largo plazo; `record` para modelar `Price` como value object inmutable. |
| **Spring Boot 3.3.5** | Arranque y auto-configuración del servicio (servidor embebido, inyección de dependencias, gestión del ciclo de vida de los beans). |
| `spring-boot-starter-web` | Expone el endpoint REST sobre Tomcat embebido y serializa las respuestas a JSON (Jackson). |
| `spring-boot-starter-data-jpa` | Traduce el puerto de persistencia a una consulta JPA/Hibernate contra H2, sin escribir SQL a mano. |
| `spring-boot-starter-validation` | Valida `brandId`/`productId`/`applicationDate` de forma declarativa (`@NotNull`, `@Positive`) antes de que lleguen a la lógica de negocio. |
| **H2 (en memoria)** | Lo pide el enunciado explícitamente: base de datos de prueba que se recrea limpia en cada arranque, sin infraestructura externa que instalar. |
| **springdoc-openapi** | Genera la documentación OpenAPI/Swagger a partir de las anotaciones del controlador, en vez de mantener un `.yaml` de contrato a mano. |
| **ArchUnit** | Convierte la regla "el dominio no depende de Spring/JPA/Web" en un test que falla en build si se rompe, en vez de una convención que solo vive en la cabeza del equipo. |
| **Docker** | Permite entregar el servicio como una imagen ejecutable, sin que quien lo evalúe necesite tener instalada la misma versión de Java/Maven. |

## Arquitectura

El servicio sigue **arquitectura hexagonal (Ports & Adapters)**, implementada como **paquetes dentro de un único módulo Maven** (no como módulos Maven separados, innecesario para este tamaño de servicio). La idea central: las reglas de negocio (`domain`) no dependen de ningún detalle técnico; son los detalles técnicos (`infrastructure`) los que dependen del dominio, nunca al revés.

```
com.bcnc.prices
├── domain                     núcleo: reglas de negocio, sin dependencias externas
│   ├── model/Price             value object inmutable (record) — valida sus propios invariantes
│   ├── port/in/GetApplicablePriceUseCase   contrato del caso de uso (lo que expone el dominio)
│   ├── port/out/PriceRepositoryPort        contrato que el dominio necesita del exterior
│   └── exception/PriceNotFoundException    error de negocio, no técnico
│
├── application                 orquestación del caso de uso
│   └── PriceQueryService        implementa el puerto de entrada, usa el puerto de salida
│
└── infrastructure               detalles técnicos, implementan los puertos
    ├── adapter/in/web           adaptador de entrada (HTTP)
    │   ├── PriceController          traduce HTTP → llamada al caso de uso → DTO
    │   └── PriceExceptionHandler    traduce excepciones → respuestas ProblemDetail
    ├── adapter/out/persistence  adaptador de salida (H2/JPA)
    │   ├── PriceEntity               mapeo JPA de la tabla `prices`
    │   ├── PriceJpaRepository        repositorio Spring Data (la consulta real)
    │   ├── PriceEntityMapper         PriceEntity ↔ Price (dominio)
    │   └── PriceRepositoryAdapter    implementa PriceRepositoryPort
    └── config/OpenApiConfig     metadatos de la documentación OpenAPI
```

**Regla de dependencia** (verificada automáticamente por [`HexagonalArchitectureTest`](src/test/java/com/bcnc/prices/architecture/HexagonalArchitectureTest.java), no solo documentada aquí): `domain` no importa `org.springframework.*`, `jakarta.persistence.*` ni `jakarta.servlet.*`; `application` no importa nada de `infrastructure`.

### Flujo de una petición

```
Cliente
  │  GET /api/v1/prices?brandId=1&productId=35455&applicationDate=2020-06-14T10:00:00
  ▼
PriceController            (infrastructure/adapter/in/web)
  │  1. Bean Validation valida los parámetros (@NotNull, @Positive)
  │  2. Llama a GetApplicablePriceUseCase.getApplicablePrice(...)
  ▼
PriceQueryService           (application)
  │  3. Pide la tarifa a través de PriceRepositoryPort (no sabe que hay una BD detrás)
  ▼
PriceRepositoryAdapter      (infrastructure/adapter/out/persistence)
  │  4. Traduce la petición a una consulta JPA sobre PriceJpaRepository:
  │     filtra por brand+producto+rango de fechas y ordena por PRIORITY en la propia BD
  │  5. Mapea el PriceEntity resultante a Price (dominio) vía PriceEntityMapper
  ▼
PriceQueryService
  │  6. Si no hay resultado, lanza PriceNotFoundException (error de dominio)
  ▼
PriceController / PriceExceptionHandler
  │  7. Éxito → mapea Price a PriceResponse (DTO) → 200 OK
  │     Error  → mapea la excepción a ProblemDetail → 400/404
  ▼
Cliente
```

## Decisiones técnicas y por qué

- **La desambiguación por `PRIORITY` se resuelve en la propia consulta**, no cargando todas las filas y filtrando en Java: `PriceJpaRepository` usa una query derivada que filtra por `brandId`/`productId`/rango de fechas y ordena por `priority` descendente, devolviendo un único resultado desde la base de datos. Es el criterio de "eficiencia de la extracción de datos" del enunciado, y evita traer a memoria datos que no hacen falta.
- **`BigDecimal` para `price`**, nunca `double`/`float`: dinero no puede perder precisión por redondeo de coma flotante.
- **Errores como `ProblemDetail` (RFC 7807)** en todos los casos (parámetro ausente, mal formado, no positivo, o sin tarifa aplicable): un único formato de error estándar y autodescriptivo, en vez de una estructura ad-hoc distinta por cada caso.
- **`schema.sql` + `data.sql`, no Flyway**: el enunciado pide un dataset fijo cargado al arrancar. Flyway resuelve versionar cambios de esquema en el tiempo — introducirlo aquí sería complejidad sin beneficio real para este alcance.
- **Un único módulo Maven con paquetes**, no módulos separados por capa: la arquitectura hexagonal no exige artefactos Maven distintos: los límites entre capas se imponen con visibilidad de paquete y se verifican con ArchUnit, sin la sobrecarga de gestionar varios `pom.xml`.

## Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`. La base de datos H2 se crea e inicializa automáticamente (`schema.sql` + `data.sql`) con los 4 registros del enunciado para el producto `35455` / brand `1` (ZARA).

Documentación interactiva (OpenAPI/Swagger): `http://localhost:8080/swagger-ui/index.html`.

### Con Docker

```bash
docker build -t prices-service .
docker run -p 8080:8080 prices-service
```

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
  "title": "Parámetro con formato inválido",
  "status": 400,
  "detail": "El parámetro 'applicationDate' con valor '14-06-2020' no es válido: se esperaba una fecha/hora en formato ISO-8601 (yyyy-MM-dd'T'HH:mm:ss), p. ej. 2020-06-14T10:00:00.",
  "instance": "/api/v1/prices"
}
```

Lo mismo aplica si `brandId`/`productId` no son numéricos (`se esperaba un número entero`).

Si falta cualquiera de los tres parámetros, la respuesta es **400 Bad Request** indicando cuál falta:

```json
{
  "type": "about:blank",
  "title": "Falta un parámetro obligatorio",
  "status": 400,
  "detail": "El parámetro obligatorio 'brandId' no fue proporcionado.",
  "instance": "/api/v1/prices"
}
```

### Identificadores no válidos (`brandId`/`productId` ≤ 0)

```json
{
  "type": "about:blank",
  "title": "Parámetros de entrada inválidos",
  "status": 400,
  "detail": "'brandId' debe ser mayor que 0",
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
