# Changelog - calisat-ms-envios

## [2.0.0] - 2026-09-23

### BREAKING CHANGE
- Versión pom.xml incrementada a 2.0.0 (fase B: integración entre microservicios)
- `EnvioService` ahora exige en su constructor el nuevo cliente HTTP `NotificacionesClient` (paquete `com.calisat.msenvios.client`); cualquier construcción manual del servicio debe inyectarlo
- `PUT /api/v1/envios/{id}/estado` pasa a publicar eventos `ENVIO_DESPACHADO`/`ENVIO_ENTREGADO` hacia notificaciones en cada cambio de hito (operación best-effort, jamás rompe el cambio de estado)

### Added
- Paquete `client` con cliente RestTemplate aislado `NotificacionesClient` (POST /api/v1/notificaciones/eventos), sin service discovery
- URL base por variable de entorno con default localhost: `CALISAT_NOTIFICACIONES_URL` (http://localhost:8087)
- `RestTemplateConfig` con el bean `RestTemplate` compartido por los clientes
- Publicación de `ENVIO_DESPACHADO` y `ENVIO_ENTREGADO` con cabecera `Idempotency-Key` (`envio-{id}-{ESTADO}`) y payload JSON de auditoría (envioId, ordenId, numeroGuia, estado)
- Degradación elegante: si notificaciones está caído, el aviso se registra en log y la transición de estado del envío continúa (try/catch best-effort)
- Tests de cliente con `RestTemplate` mockeado (`NotificacionesClientTest`) y tests de servicio que verifican qué estados publican aviso y cuáles no

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

[2.0.0]: https://github.com/DavNat13/calisat-ms-envios/compare/v1.3.0...v2.0.0
[1.3.0]: https://github.com/DavNat13/calisat-ms-envios/releases/tag/v1.3.0
