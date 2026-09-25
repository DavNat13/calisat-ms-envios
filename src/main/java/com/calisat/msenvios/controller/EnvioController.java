package com.calisat.msenvios.controller;

import com.calisat.msenvios.dto.EnvioEstadoRequest;
import com.calisat.msenvios.dto.EnvioRequest;
import com.calisat.msenvios.dto.EnvioResponse;
import com.calisat.msenvios.dto.SeguimientoResponse;
import com.calisat.msenvios.model.Envio;
import com.calisat.msenvios.service.EnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/envios")
@Tag(name = "Envios", description = "Gestion de envios: creacion, cambio de estado y seguimiento publico por numero de guia. "
        + "Escrituras (POST/PUT) con RBAC ADMINISTRADOR|LOGISTICA; el listado global del panel es solo para esos roles; "
        + "lecturas sin parametros devuelven los envios del usuario JWT; seguimiento publico sin JWT.")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    /**
     * Crea un envio para la orden indicada.
     *
     * <p>Genera un numero de guia unico (prefijo CAL-), deja el envio en
     * estado CREADO y registra el evento inicial. Si el token no trae
     * {@code usuarioSub}, se usa el claim {@code sub} del JWT.</p>
     *
     * @param request datos de la orden y de entrega
     * @param jwt token del usuario autenticado (fallback de usuarioSub)
     * @return 201 con {@code Location} al recurso {@code /{id}}; 409 si la
     *         orden ya tiene un envio; 400 si la entrada es invalida
     */
    @Operation(summary = "Crear envio",
            description = "Crea un envio para una orden y genera su numero de guia unico (CAL-...). "
                    + "201 con Location por id; 409 si la orden ya tiene un envio; 400 si la entrada es invalida. Requiere JWT.")
    @PostMapping
    public ResponseEntity<EnvioResponse> crear(
            @Valid @RequestBody EnvioRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String usuarioSub = (request.usuarioSub() != null && !request.usuarioSub().isBlank())
                ? request.usuarioSub()
                : (jwt != null ? jwt.getSubject() : null);
        EnvioRequest efectiva = new EnvioRequest(
                request.ordenId(),
                usuarioSub,
                request.direccionCalle(),
                request.direccionCiudad(),
                request.direccionPais(),
                request.direccionCodigoPostal(),
                request.transportista());

        Envio guardado = envioService.crear(efectiva);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();
        return ResponseEntity.created(location).body(EnvioResponse.desde(guardado));
    }

    /**
     * Lista envios.
     *
     * <p>Si se proporciona {@code ordenId}, devuelve los envios de esa orden.
     * Si se omite: los roles de gestion (ADMINISTRADOR|LOGISTICA) obtienen el
     * listado global del panel; cualquier otro usuario autenticado obtiene
     * sus propios envios (claim {@code sub} del JWT); si el token no lo
     * incluye, la lista es vacia.</p>
     *
     * @param ordenId identificador logico de la orden (opcional)
     * @param jwt token del usuario autenticado
     * @param authentication autenticacion actual (para el control de rol)
     * @return 200 con la lista de envios
     */
    @Operation(summary = "Listar envios",
            description = "Lista envios por ordenId (parametro opcional). Sin ordenId: los roles ADMINISTRADOR/LOGISTICA "
                    + "reciben el listado global (panel); el resto de usuarios autenticados reciben solo los suyos "
                    + "(claim 'sub' del JWT). Requiere JWT.")
    @GetMapping
    public ResponseEntity<List<EnvioResponse>> listar(
            @Parameter(name = "ordenId", description = "Identificador logico de la orden a filtrar. Si se omite, se decide entre listado global (gestion) y envios propios.")
            @RequestParam(required = false) UUID ordenId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        List<Envio> envios;
        if (ordenId != null) {
            envios = envioService.listarPorOrden(ordenId);
        } else if (esGestion(authentication)) {
            envios = envioService.listarTodas();
        } else {
            String sub = jwt != null ? jwt.getSubject() : null;
            envios = (sub == null || sub.isBlank())
                    ? List.of()
                    : envioService.listarPorUsuario(sub);
        }
        return ResponseEntity.ok(envios.stream().map(EnvioResponse::desde).toList());
    }

    /**
     * Indica si la autenticacion actual pertenece a un rol de gestion del
     * panel (ADMINISTRADOR o LOGISTICA). El converter de seguridad normaliza
     * el claim {@code roles} a autoridades {@code ROLE_*} en mayusculas.
     */
    private static boolean esGestion(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a) || "ROLE_LOGISTICA".equals(a));
    }

    /**
     * Consulta un envio por su identificador interno.
     *
     * @param id identificador UUID del envio
     * @return 200 con el envio; 404 si no existe
     */
    @Operation(summary = "Consultar envio por id",
            description = "Detalle de un envio identificado por su id interno. 404 si no existe. Requiere JWT.")
    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponse> buscarPorId(
            @Parameter(name = "id", description = "Identificador interno del envio (UUID).", required = true)
            @PathVariable UUID id) {
        return envioService.buscarPorId(id)
                .map(EnvioResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza el estado del envio y appenda un evento en su historial.
     *
     * <p>Valida la transicion (no se permite retroceder, p.ej. ENTREGADO ->
     * CREADO). EN_PREPARACION/DESPACHADO fijan la fecha de despacho y
     * ENTREGADO la fecha de entrega real.</p>
     *
     * @param id identificador UUID del envio
     * @param request nuevo estado, descripcion y ubicacion opcionales
     * @return 200 con el envio actualizado; 404 si no existe; 409 si la
     *         transicion no esta permitida; 400 si el estado es desconocido
     */
    @Operation(summary = "Actualizar estado del envio",
            description = "Cambia el estado del envio y registra un evento en el historial (append-only). "
                    + "200 con el envio actualizado; 404 si no existe; 409 si la transicion no esta permitida "
                    + "(p.ej. ENTREGADO a CREADO); 400 si el estado es desconocido. Requiere JWT.")
    @PutMapping("/{id}/estado")
    public ResponseEntity<EnvioResponse> actualizarEstado(
            @Parameter(name = "id", description = "Identificador interno del envio (UUID).", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody EnvioEstadoRequest request) {
        return envioService.actualizarEstado(id, request)
                .map(EnvioResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Seguimiento publico de un envio por su numero de guia.
     *
     * <p>Endpoint permitAll en SecurityConfig: no requiere JWT. Devuelve el
     * estado actual y el historial de eventos (del mas reciente al mas
     * antiguo).</p>
     *
     * @param numeroGuia numero de guia del envio (identificador canonico)
     * @return 200 con el seguimiento; 404 si la guia no existe
     */
    @Operation(summary = "Seguimiento por numero de guia (publico)",
            description = "Consulta publica (sin JWT) del estado y el historial de eventos de un envio "
                    + "a partir de su numero de guia. 404 si la guia no existe.")
    @GetMapping("/seguimiento/{numeroGuia}")
    public ResponseEntity<SeguimientoResponse> seguimiento(
            @Parameter(name = "numeroGuia", description = "Numero de guia del envio (p.ej. CAL-9F2B41C7A0D3).", required = true)
            @PathVariable String numeroGuia) {
        return envioService.seguimiento(numeroGuia)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
