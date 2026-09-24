package com.calisat.msenvios.service;

import com.calisat.msenvios.client.NotificacionEventoDto;
import com.calisat.msenvios.client.NotificacionesClient;
import com.calisat.msenvios.dto.EnvioEstadoRequest;
import com.calisat.msenvios.dto.EnvioRequest;
import com.calisat.msenvios.dto.SeguimientoResponse;
import com.calisat.msenvios.exception.EnvioDuplicadoException;
import com.calisat.msenvios.exception.TransicionEstadoNoPermitidaException;
import com.calisat.msenvios.model.Envio;
import com.calisat.msenvios.model.EnvioEvento;
import com.calisat.msenvios.model.EstadoEnvio;
import com.calisat.msenvios.repository.EnvioEventoRepository;
import com.calisat.msenvios.repository.EnvioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private EnvioEventoRepository envioEventoRepository;

    @Mock
    private NotificacionesClient notificacionesClient;

    @InjectMocks
    private EnvioService envioService;

    @Test
    void crear_generaGuiaCalYEventoInicial() {
        UUID ordenId = UUID.randomUUID();
        when(envioRepository.existsByOrdenId(ordenId)).thenReturn(false);
        when(envioRepository.existsByNumeroGuia(any(String.class))).thenReturn(false);
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioRequest request = new EnvioRequest(ordenId, "sub-123",
                "Calle Mayor 1", "Madrid", "Espana", "28001", "SEUR");

        Envio creado = envioService.crear(request);

        assertNotNull(creado.getNumeroGuia());
        assertTrue(creado.getNumeroGuia().startsWith("CAL-"));
        assertEquals(EstadoEnvio.CREADO, creado.getEstado());
        assertEquals(ordenId, creado.getOrdenId());
        assertEquals("sub-123", creado.getUsuarioSub());

        ArgumentCaptor<EnvioEvento> eventoCaptor = ArgumentCaptor.forClass(EnvioEvento.class);
        verify(envioEventoRepository).save(eventoCaptor.capture());
        assertEquals(EstadoEnvio.CREADO, eventoCaptor.getValue().getEstado());
        assertEquals(creado, eventoCaptor.getValue().getEnvio());
    }

    @Test
    void crear_lanzaEnvioDuplicadoSiLaOrdenYaTieneEnvio() {
        UUID ordenId = UUID.randomUUID();
        when(envioRepository.existsByOrdenId(ordenId)).thenReturn(true);

        EnvioRequest request = new EnvioRequest(ordenId, null, null, null, null, null, null);

        EnvioDuplicadoException ex = assertThrows(EnvioDuplicadoException.class,
                () -> envioService.crear(request));
        assertEquals("La orden ya tiene un envio activo", ex.getMessage());
        verify(envioRepository).existsByOrdenId(ordenId);
    }

    @Test
    void actualizarEstado_ENTREGADO_seteaFechaEntregaRealYRegistraEvento() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setEstado(EstadoEnvio.EN_TRANSITO);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioEstadoRequest request = new EnvioEstadoRequest("ENTREGADO", "Entregado al cliente", "Madrid");

        Optional<Envio> resultado = envioService.actualizarEstado(id, request);

        assertTrue(resultado.isPresent());
        assertEquals(EstadoEnvio.ENTREGADO, resultado.get().getEstado());
        assertNotNull(resultado.get().getFechaEntregaReal());

        ArgumentCaptor<EnvioEvento> eventoCaptor = ArgumentCaptor.forClass(EnvioEvento.class);
        verify(envioEventoRepository).save(eventoCaptor.capture());
        assertEquals(EstadoEnvio.ENTREGADO, eventoCaptor.getValue().getEstado());
        assertEquals("Entregado al cliente", eventoCaptor.getValue().getDescripcion());
        assertEquals("Madrid", eventoCaptor.getValue().getUbicacion());
    }

    @Test
    void actualizarEstado_lanzaTransicionNoPermitidaDeEntregadoACreado() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setEstado(EstadoEnvio.ENTREGADO);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));

        EnvioEstadoRequest request = new EnvioEstadoRequest("CREADO", null, null);

        assertThrows(TransicionEstadoNoPermitidaException.class,
                () -> envioService.actualizarEstado(id, request));
    }

    @Test
    void actualizarEstado_devuelveVacioSiNoExiste() {
        UUID id = UUID.randomUUID();
        when(envioRepository.findById(id)).thenReturn(Optional.empty());

        EnvioEstadoRequest request = new EnvioEstadoRequest("EN_PREPARACION", null, null);

        assertTrue(envioService.actualizarEstado(id, request).isEmpty());
    }

    @Test
    void seguimiento_devuelveEstadoYEventos() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setEstado(EstadoEnvio.EN_TRANSITO);
        when(envioRepository.findByNumeroGuia("CAL-ABC123DEF456")).thenReturn(Optional.of(envio));
        when(envioEventoRepository.findByEnvioIdOrderByFechaEventoDesc(id))
                .thenReturn(List.of(new EnvioEvento(envio, EstadoEnvio.EN_TRANSITO, "En transito", "Toledo")));

        Optional<SeguimientoResponse> resultado = envioService.seguimiento("CAL-ABC123DEF456");

        assertTrue(resultado.isPresent());
        assertEquals("CAL-ABC123DEF456", resultado.get().numeroGuia());
        assertEquals("EN_TRANSITO", resultado.get().estado());
        assertEquals(1, resultado.get().eventos().size());
    }

    @Test
    void seguimiento_devuelveVacioSiLaGuiaNoExiste() {
        when(envioRepository.findByNumeroGuia("CAL-NOEXISTE")).thenReturn(Optional.empty());

        assertTrue(envioService.seguimiento("CAL-NOEXISTE").isEmpty());
    }

    @Test
    void actualizarEstado_publicaEventoDeNotificacionAlDespachar() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setUsuarioSub("sub-123");
        envio.setEstado(EstadoEnvio.EN_PREPARACION);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        envioService.actualizarEstado(id, new EnvioEstadoRequest("DESPACHADO", "Sale del hub", "Madrid"));

        verify(notificacionesClient).publicar(
                eq("envio-" + id + "-DESPACHADO"), any(NotificacionEventoDto.class));
    }

    @Test
    void actualizarEstado_publicaEventoDeNotificacionAlEntregar() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setUsuarioSub("sub-123");
        envio.setEstado(EstadoEnvio.EN_TRANSITO);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        envioService.actualizarEstado(id, new EnvioEstadoRequest("ENTREGADO", "Entregado", "Toledo"));

        verify(notificacionesClient).publicar(
                eq("envio-" + id + "-ENTREGADO"), any(NotificacionEventoDto.class));
    }

    @Test
    void actualizarEstado_noPublicaEventosParaEstadosSinAviso() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setEstado(EstadoEnvio.CREADO);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        envioService.actualizarEstado(id, new EnvioEstadoRequest("EN_PREPARACION", null, null));

        verify(notificacionesClient, never()).publicar(any(), any());
    }

    @Test
    void actualizarEstado_noLanzaExcepcionSiNotificacionesEstaCaido() {
        UUID id = UUID.randomUUID();
        Envio envio = new Envio();
        envio.setId(id);
        envio.setOrdenId(UUID.randomUUID());
        envio.setNumeroGuia("CAL-ABC123DEF456");
        envio.setUsuarioSub("sub-123");
        envio.setEstado(EstadoEnvio.EN_PREPARACION);
        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(envioEventoRepository.save(any(EnvioEvento.class))).thenAnswer(inv -> inv.getArgument(0));
        org.mockito.Mockito.doThrow(new IllegalStateException("notificaciones caido"))
                .when(notificacionesClient).publicar(any(), any());

        Optional<Envio> resultado = envioService.actualizarEstado(
                id, new EnvioEstadoRequest("DESPACHADO", null, null));

        assertTrue(resultado.isPresent());
        assertEquals(EstadoEnvio.DESPACHADO, resultado.get().getEstado());
    }
}
