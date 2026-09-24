# Changelog - calisat-ms-envios

## [1.3.0] - 2026-09-23

### Added
- Microservicio calisat-ms-envios con Spring Boot 4.1.0 y Java 21 (puerto 8086)
- Docker Compose con PostgreSQL 15 (`calisat_envio`, puerto host 5434) y app Spring Boot
- Entidades JPA Envio (numero guia único CAL-..., snapshot de dirección) y EnvioEvento (historial append-only de tracking)
- Máquina de estados CREADO → EN_PREPARACION → DESPACHADO → EN_TRANSITO → ENTREGADO / FALLIDO / DEVUELTO
- EnvioService con creación idempotente por orden, cambio de estado validado y seguimiento público por número de guía
- Endpoints: POST/GET /api/v1/envios, GET /api/v1/envios/{id}, PUT /api/v1/envios/{id}/estado, GET /api/v1/envios/seguimiento/{numeroGuia} (público)
- SecurityConfig con validacion JWT de Azure Entra ID (issuer + audience) y CORS; sin RBAC (solo autenticacion)
- GlobalExceptionHandler con manejo de errores de negocio y validacion
- Tests de servicio (EnvioServiceTest)
- Health check via Spring Actuator
