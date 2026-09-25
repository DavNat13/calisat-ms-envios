package com.calisat.msenvios.controller;

import com.calisat.msenvios.dto.EnvioResponse;
import com.calisat.msenvios.model.Envio;
import com.calisat.msenvios.model.EstadoEnvio;
import com.calisat.msenvios.service.EnvioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la ramificacion de roles del listado de envios:
 * sin ordenId, ADMINISTRADOR/LOGISTICA reciben el listado global
 * del panel y el resto de usuarios autenticados solo los suyos.
 */
@ExtendWith(MockitoExtension.class)
class EnvioControllerTest {

    @Mock
    private EnvioService envioService;

    private EnvioController controller;

    @BeforeEach
    void setUp() {
        controller = new EnvioController(envioService);
    }

    private static Jwt jwtConSub(String sub) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", sub)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private static Authentication autenticacionConRoles(String... roles) {
        Set<GrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        return new UsernamePasswordAuthenticationToken("user", "n/a", authorities);
    }

    private static Envio envio(String guia) {
        Envio envio = new Envio();
        envio.setId(UUID.randomUUID());
        envio.setOrdenId(UUID.randomUUID());
        envio.setUsuarioSub("sub-123");
        envio.setNumeroGuia(guia);
        envio.setEstado(EstadoEnvio.CREADO);
        return envio;
    }

    @Test
    void listar_sinOrdenIdYRolAdmin_devuelveListadoGlobal() {
        when(envioService.listarTodas()).thenReturn(List.of(envio("CAL-1")));

        ResponseEntity<List<EnvioResponse>> respuesta = controller.listar(
                null, jwtConSub("sub-123"), autenticacionConRoles("ROLE_ADMINISTRADOR"));

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(1, respuesta.getBody().size());
        verify(envioService).listarTodas();
        verify(envioService, never()).listarPorUsuario("sub-123");
    }

    @Test
    void listar_sinOrdenIdYRolLogistica_devuelveListadoGlobal() {
        when(envioService.listarTodas()).thenReturn(List.of(envio("CAL-2"), envio("CAL-3")));

        ResponseEntity<List<EnvioResponse>> respuesta = controller.listar(
                null, jwtConSub("sub-123"), autenticacionConRoles("ROLE_LOGISTICA"));

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(2, respuesta.getBody().size());
        verify(envioService).listarTodas();
    }

    @Test
    void listar_sinOrdenIdYRolCliente_devuelveSoloLosSuyos() {
        when(envioService.listarPorUsuario("sub-123")).thenReturn(List.of(envio("CAL-4")));

        ResponseEntity<List<EnvioResponse>> respuesta = controller.listar(
                null, jwtConSub("sub-123"), autenticacionConRoles("ROLE_CLIENTE"));

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(1, respuesta.getBody().size());
        verify(envioService).listarPorUsuario("sub-123");
        verify(envioService, never()).listarTodas();
    }

    @Test
    void listar_conOrdenId_filtraPorOrdenSinMirarRoles() {
        UUID ordenId = UUID.randomUUID();
        when(envioService.listarPorOrden(ordenId)).thenReturn(List.of());

        ResponseEntity<List<EnvioResponse>> respuesta = controller.listar(
                ordenId, jwtConSub("sub-123"), autenticacionConRoles("ROLE_ADMINISTRADOR"));

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().isEmpty());
        verify(envioService).listarPorOrden(ordenId);
        verify(envioService, never()).listarTodas();
    }

    @Test
    void listar_sinOrdenIdYSinAuthentication_devuelveVaciaSiNoHaySub() {
        ResponseEntity<List<EnvioResponse>> respuesta = controller.listar(null, null, null);

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().isEmpty());
        verify(envioService, never()).listarTodas();
        verify(envioService, never()).listarPorUsuario("sub-123");
    }
}
