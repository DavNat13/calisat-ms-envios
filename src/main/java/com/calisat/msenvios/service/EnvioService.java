package com.calisat.msenvios.service;

import com.calisat.msenvios.dto.EnvioEstadoRequest;
import com.calisat.msenvios.dto.EnvioRequest;
import com.calisat.msenvios.dto.SeguimientoResponse;
import com.calisat.msenvios.dto.EnvioEventoResponse;
import com.calisat.msenvios.exception.EnvioDuplicadoException;
import com.calisat.msenvios.exception.TransicionEstadoNoPermitidaException;
import com.calisat.msenvios.model.Envio;
import com.calisat.msenvios.model.EnvioEvento;
import com.calisat.msenvios.model.EstadoEnvio;
import com.calisat.msenvios.repository.EnvioEventoRepository;
import com.calisat.msenvios.repository.EnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EnvioService {

    private static final String PREFIJO_GUIA = "CAL-";

    private final EnvioRepository envioRepository;
    private final EnvioEventoRepository envioEventoRepository;

    public EnvioService(EnvioRepository envioRepository,
                        EnvioEventoRepository envioEventoRepository) {
        this.envioRepository = envioRepository;
        this.envioEventoRepository = envioEventoRepository;
    }

    /**
     * Crea un envio para la orden indicada, genera su numero de guia unico
     * (prefijo CAL-) y registra el evento inicial CREADO.
     *
     * @throws EnvioDuplicadoException si la orden ya tiene un envio (409)
     */
    public Envio crear(EnvioRequest request) {
        if (envioRepository.existsByOrdenId(request.ordenId())) {
            throw new EnvioDuplicadoException();
        }
        Envio envio = new Envio();
        envio.setOrdenId(request.ordenId());
        envio.setUsuarioSub(request.usuarioSub());
        envio.setNumeroGuia(generarNumeroGuia());
        envio.setTransportista(request.transportista());
        envio.setEstado(EstadoEnvio.CREADO);
        envio.setDireccionCalle(request.direccionCalle());
        envio.setDireccionCiudad(request.direccionCiudad());
        envio.setDireccionPais(request.direccionPais());
        envio.setDireccionCodigoPostal(request.direccionCodigoPostal());

        Envio guardado = envioRepository.save(envio);
        registrarEvento(guardado, EstadoEnvio.CREADO, "Envio creado", null);
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Envio> listarPorOrden(UUID ordenId) {
        return envioRepository.findByOrdenId(ordenId);
    }

    @Transactional(readOnly = true)
    public List<Envio> listarPorUsuario(String usuarioSub) {
        return envioRepository.findByUsuarioSub(usuarioSub);
    }

    @Transactional(readOnly = true)
    public Optional<Envio> buscarPorId(UUID id) {
        return envioRepository.findById(id);
    }

    /**
     * Aplica una transicion de estado al envio, actualiza los campos de fecha
     * relevantes y appenda un {@link EnvioEvento} en el historial.
     *
     * @return el envio actualizado, o vacio si no existe (404)
     * @throws TransicionEstadoNoPermitidaException si la transicion no esta
     *         permitida (p.ej. ENTREGADO -> CREADO)
     */
    public Optional<Envio> actualizarEstado(UUID id, EnvioEstadoRequest request) {
        return envioRepository.findById(id).map(envio -> {
            EstadoEnvio destino = EstadoEnvio.from(request.estado());
            EstadoEnvio actual = envio.getEstado();
            if (!EstadoEnvio.permiteTransicion(actual, destino)) {
                throw new TransicionEstadoNoPermitidaException(
                        actual != null ? actual.name() : "null", destino.name());
            }

            LocalDateTime ahora = LocalDateTime.now();
            envio.setEstado(destino);
            if ((destino == EstadoEnvio.EN_PREPARACION || destino == EstadoEnvio.DESPACHADO)
                    && envio.getFechaDespacho() == null) {
                envio.setFechaDespacho(ahora);
            }
            if (destino == EstadoEnvio.ENTREGADO) {
                envio.setFechaEntregaReal(ahora);
            }
            envio.setFechaActualizacion(ahora);

            registrarEvento(envio, destino, request.descripcion(), request.ubicacion());
            return envioRepository.save(envio);
        });
    }

    /**
     * Consulta publica de seguimiento por numero de guia: estado actual y
     * historial de eventos (del mas reciente al mas antiguo).
     *
     * @return el seguimiento, o vacio si la guia no existe (404)
     */
    @Transactional(readOnly = true)
    public Optional<SeguimientoResponse> seguimiento(String numeroGuia) {
        return envioRepository.findByNumeroGuia(numeroGuia).map(envio -> {
            List<EnvioEventoResponse> eventos = envioEventoRepository
                    .findByEnvioIdOrderByFechaEventoDesc(envio.getId())
                    .stream()
                    .map(EnvioEventoResponse::desde)
                    .toList();
            return SeguimientoResponse.desde(envio, eventos);
        });
    }

    private void registrarEvento(Envio envio, EstadoEnvio estado, String descripcion, String ubicacion) {
        envioEventoRepository.save(new EnvioEvento(envio, estado, descripcion, ubicacion));
    }

    private String generarNumeroGuia() {
        String candidato;
        do {
            String sufijo = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
            candidato = PREFIJO_GUIA + sufijo;
        } while (envioRepository.existsByNumeroGuia(candidato));
        return candidato;
    }
}
