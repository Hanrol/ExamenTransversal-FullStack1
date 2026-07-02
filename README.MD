# Examen Transversal FullStack 1

Proyecto de Benjamín Cubillos.

Este repositorio implementa una arquitectura de microservicios con Spring Boot, Spring Cloud, Eureka Server, API Gateway, bases de datos MySQL independientes y documentación Swagger/OpenAPI.

Para el examen se eligió la opción 2: Microservicio de Reseñas.

## Arquitectura General

El sistema está compuesto por los siguientes servicios:

| Servicio | Puerto | Responsabilidad |
| --- | ---: | --- |
| `eurekaserver` | 8761 | Discovery service para registrar y descubrir microservicios. |
| `gateway` | 9999 | Entrada única del sistema y enrutamiento hacia los servicios registrados en Eureka. |
| `login` | 9000 | Registro, login, generación y validación de token JWT. |
| `destination` | 9001 | Administración de destinos, categorías y validación de existencia de destinos. |
| `traveler` | 9002 | Administración de viajes y detalles de viaje. |
| `review` | 9003 | Administración de reseñas asociadas a destinos y usuarios autenticados. |

El API Gateway usa rutas con `lb://` para resolver servicios mediante Eureka:

| Ruta gateway | Servicio destino |
| --- | --- |
| `/api/v1/users/**` | `login` |
| `/api/v1/destination/**` | `destination` |
| `/api/v1/traveler/**` | `traveler` |
| `/api/v1/reviews/**` | `review` |

## Bases de Datos

Cada microservicio con persistencia usa una base de datos MySQL propia:

| Contenedor | Puerto local | Base de datos | Servicio |
| --- | ---: | --- | --- |
| `mysql_login` | 3310 | `login` | `login` |
| `mysql_destination` | 3311 | `destination` | `destination` |
| `mysql_traveler` | 3312 | `traveler` | `traveler` |
| `mysql_review` | 3313 | `review` | `review` |

Todas las bases de datos usan:

```text
Usuario: examen
Password: examen123
```

La base de datos de `review` se inicializa mediante Flyway en:

```text
review/src/main/resources/db/migration/V1__create_review_table.sql
```

## Microservicio Review

El microservicio `review` permite crear, consultar, actualizar y eliminar lógicamente reseñas asociadas a destinos turísticos.

La reseña cumple con las reglas del enunciado:

- Está asociada a un destino existente del `Destination Service`.
- El autor se obtiene desde el token JWT validado contra el `Login Service`.
- Todas las respuestas usan la estructura `ApiResponse`.
- Todos los endpoints validan token mediante el header `Authorization`.
- Los DTOs aplican Bean Validation.
- La comunicación remota se realiza con `WebClient`.
- El servicio se registra en Eureka.

### Modelo de Datos

El microservicio usa una sola tabla, dentro del máximo de 3 tablas solicitado.

Tabla: `reviews`

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `review_id` | `BIGINT` | Identificador autoincremental de la reseña. |
| `destination_id` | `CHAR(36)` | UUID del destino validado en `destination`. |
| `author_user_id` | `CHAR(36)` | UUID del usuario autenticado validado en `login`. |
| `rating` | `INT` | Nota de la reseña entre 1 y 5. |
| `comment` | `VARCHAR(255)` | Comentario opcional de la reseña. |
| `active` | `BOOLEAN` | Marca para eliminación lógica. |
| `created_at` | `DATETIME` | Fecha de creación. |
| `updated_at` | `DATETIME` | Fecha de última actualización. |

La restricción `chk_reviews_rating` asegura en base de datos que `rating` esté entre 1 y 5.

No se crean claves foráneas hacia otros microservicios porque cada servicio mantiene su propia base de datos. La integridad distribuida se resuelve consultando remotamente a `login` y `destination`.

## Estructura CSR

El microservicio `review` usa separación por capas:

```text
review/src/main/java/cl/duoc/review
├── client
│   ├── DestinationClient.java
│   └── LoginClient.java
├── config
│   ├── SecurityConfig.java
│   └── WebClientConfig.java
├── controller
│   └── ReviewController.java
├── dto
│   ├── ApiResponse.java
│   ├── ReviewCreateRequestDTO.java
│   ├── ReviewResponseDTO.java
│   ├── ReviewUpdateRequestDTO.java
│   └── UserDTO.java
├── exception
│   └── GlobalExceptionHandler.java
├── model
│   └── Review.java
├── repository
│   └── ReviewRepository.java
└── service
    └── ReviewService.java
```

Responsabilidades:

- `ReviewController`: expone endpoints REST y documenta Swagger.
- `ReviewService`: aplica reglas de negocio, validación de token, validación de destino, autorización del autor y eliminación lógica.
- `ReviewRepository`: acceso a datos con `JpaRepository`.
- `Review`: entidad JPA mapeada a la tabla `reviews`.
- `DTOs`: separan entrada y salida del modelo persistente.
- `LoginClient` y `DestinationClient`: comunicación REST con otros microservicios.
- `GlobalExceptionHandler`: respuestas consistentes ante errores.

## Endpoints Review

Todos los endpoints usan el prefijo:

```text
/api/v1/reviews
```

Todos requieren:

```text
Authorization: Bearer <token>
```

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/v1/reviews` | Crea una reseña para un destino existente. |
| `GET` | `/api/v1/reviews` | Lista reseñas activas. |
| `GET` | `/api/v1/reviews/{reviewId}` | Obtiene una reseña activa por ID. |
| `GET` | `/api/v1/reviews/destination/{destinationId}` | Lista reseñas activas de un destino. |
| `GET` | `/api/v1/reviews/user/{authorUserId}` | Lista reseñas activas de un usuario. |
| `GET` | `/api/v1/reviews/destination/{destinationId}/average-rating` | Calcula el promedio de calificación de un destino. |
| `PUT` | `/api/v1/reviews/{reviewId}` | Actualiza una reseña. Solo el autor puede modificarla. |
| `DELETE` | `/api/v1/reviews/{reviewId}` | Elimina lógicamente una reseña. Solo el autor puede eliminarla. |

### Crear Reseña

Request:

```json
{
  "destinationId": "3b3f3937-73c9-11f1-b577-424dc013a170",
  "rating": 5,
  "comment": "Excelente destino"
}
```

Response:

```json
{
  "code": 201,
  "message": "Reseña creada correctamente",
  "data": {
    "reviewId": 1,
    "destinationId": "3b3f3937-73c9-11f1-b577-424dc013a170",
    "authorUserId": "85fbfd69-ce3d-4be3-af31-5d02f9841f9a",
    "rating": 5,
    "comment": "Excelente destino",
    "active": true,
    "createdAt": "2026-07-02T11:59:15.599705",
    "updatedAt": null
  }
}
```

### Actualizar Reseña

Request:

```json
{
  "rating": 4,
  "comment": "Comentario actualizado"
}
```

## Validaciones

Los DTOs aplican Bean Validation:

`ReviewCreateRequestDTO`

- `destinationId`: `@NotNull`
- `rating`: `@NotNull`, `@Min(1)`, `@Max(5)`
- `comment`: `@Size(max = 255)`

`ReviewUpdateRequestDTO`

- `rating`: `@NotNull`, `@Min(1)`, `@Max(5)`
- `comment`: `@Size(max = 255)`

Además, `ReviewService` valida:

- Token JWT obligatorio.
- Token JWT válido en `Login Service`.
- Destino existente en `Destination Service`.
- Reseña existente y activa.
- Usuario autenticado como autor para actualizar o eliminar.

## Manejo de Errores

El servicio centraliza errores en `GlobalExceptionHandler`.

Respuestas relevantes:

| Código | Caso |
| ---: | --- |
| 400 | Body inválido, parámetros inválidos o validación Bean Validation fallida. |
| 401 | Token ausente, inválido o expirado. |
| 403 | Usuario autenticado no es autor de la reseña. |
| 404 | Reseña o destino no encontrado. |
| 502 | Error remoto al consultar otro microservicio. |
| 503 | Servicio remoto no disponible. |
| 500 | Error interno no controlado. |

Todas las respuestas de error mantienen la estructura:

```json
{
  "code": 400,
  "message": "Mensaje del error",
  "data": null
}
```

## Comunicación entre Microservicios

`review` consume dos servicios remotos:

### Login Service

Clase: `LoginClient`

Endpoint consumido:

```text
GET http://login/api/v1/users/validate?token={token}
```

Uso:

- Se recibe `Authorization: Bearer <token>`.
- Se limpia el prefijo `Bearer`.
- Se consulta a `login`.
- Se obtiene el `UserDTO`.
- El campo `id` del usuario validado se usa como `authorUserId`.

### Destination Service

Clase: `DestinationClient`

Endpoint consumido:

```text
GET http://destination/api/v1/destination/destinations/exists?id={destinationId}
```

Uso:

- Antes de crear una reseña se valida que el destino exista.
- Si el destino no existe, `review` responde `404`.
- Si `destination` responde error remoto, se controla con `502` o `503`.

### WebClient y Eureka

`review` usa `WebClient.Builder` con `@LoadBalanced`, por lo que las URLs `http://login` y `http://destination` se resuelven mediante Eureka.

Archivo:

```text
review/src/main/java/cl/duoc/review/config/WebClientConfig.java
```

## Configuración

Archivo principal:

```text
review/application.yml
```

Configuración relevante:

```yaml
spring:
  application:
    name: review
  datasource:
    url: jdbc:mysql://localhost:3313/review
    username: examen
    password: examen123
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 9003

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

## Docker Compose

El archivo `docker-compose.yml` incluye la base de datos del microservicio:

```yaml
mysql_review:
  image: mysql:8.0
  container_name: mysql_review
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: review
    MYSQL_USER: examen
    MYSQL_PASSWORD: examen123
  ports:
    - "3313:3306"
  volumes:
    - mysql_review_data:/var/lib/mysql
```

## API Gateway

El gateway expone `review` en:

```text
http://localhost:9999/api/v1/reviews/**
```

Configuración:

```yaml
- id: review-service
  uri: lb://review
  predicates:
    - Path=/api/v1/reviews/**
```

## Swagger/OpenAPI

Cada servicio expone Swagger. En `review` está configurado en:

```text
http://localhost:9003/index.html
```

También se puede consultar la especificación OpenAPI en:

```text
http://localhost:9003/v3/api-docs
```

`ReviewController` usa:

- `@Tag`
- `@Operation`
- `@ApiResponses`
- `@Parameter`

La documentación describe rutas, parámetros, códigos HTTP y comportamiento esperado.

## Pruebas Unitarias

El microservicio `review` incluye pruebas con JUnit y Mockito.

Archivos:

```text
review/src/test/java/cl/duoc/review/controller/ReviewControllerTest.java
review/src/test/java/cl/duoc/review/service/ReviewServiceTest.java
review/src/test/java/cl/duoc/review/ReviewApplicationTests.java
```

Cobertura funcional:

- Creación de reseña.
- Listado de reseñas.
- Consulta por ID.
- Consulta por destino.
- Consulta por usuario.
- Promedio de calificación por destino.
- Actualización de reseña.
- Eliminación lógica.
- Validación de destino inexistente.
- Validación de usuario no autor.
- Validación de reseña inexistente.
- Validación de token en endpoints de lectura.

Comando:

```bash
cd review
./mvnw test
```

En Windows:

```powershell
cd review
.\mvnw.cmd test
```

Resultado verificado:

```text
Tests run: 20, Failures: 0, Errors: 0
BUILD SUCCESS
```

## Colección Postman

El archivo:

```text
ExamenFullStack1.postman_collection.json
```

incluye requests para:

- Login.
- Validación de token.
- Destinos.
- Categorías.
- Relaciones destino-categoría.
- Viajes.
- Detalles de viaje.
- Reseñas.

La colección usa el gateway en:

```text
http://localhost:9999
```

Los endpoints protegidos requieren header:

```text
Authorization: Bearer <token>
```

## Ejecución Local

1. Levantar bases de datos:

```bash
docker compose up -d
```

2. Ejecutar servicios en este orden:

```text
eurekaserver
login
destination
traveler
review
gateway
```

3. Probar desde Postman usando:

```text
http://localhost:9999
```

## Aporte Personal

El trabajo realizado sobre el microservicio `review` incluye:

- Implementación del modelo `Review`.
- Creación de migración Flyway para la tabla `reviews`.
- Implementación de DTOs de entrada y salida.
- Implementación de `ReviewRepository`.
- Implementación de `ReviewService` con reglas de negocio.
- Implementación de comunicación remota con `Login Service` y `Destination Service`.
- Implementación de `ReviewController`.
- Integración del servicio en Eureka y API Gateway.
- Configuración de base de datos MySQL en Docker Compose.
- Documentación Swagger/OpenAPI.
- Manejo global de excepciones.
- Pruebas unitarias con JUnit y Mockito.
- Actualización de colección Postman para probar los endpoints.
