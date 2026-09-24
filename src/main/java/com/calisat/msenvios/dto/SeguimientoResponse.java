package com.calisat.msenvios.dto;

import com.calisat.msenvios.model.Envio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SeguimientoResponse(
        @Schema(description = "Numero de guia del envio (identificador canonico).", example = "CAL-9F2B41C7A0D3")
        String numeroGuia,

        @Schema(description = "Estado actual del envio.", example = "EN_TRANSITO")
        String estado,

        @Schema(description = "Historial de eventos del envio, del mas reciente al mas antiguo.")
        List<EnvioEventoResponse> eventos) {

    public static SeguimientoResponse desde(Envio envio, List<EnvioEventoResponse> eventos) {
        return new SeguimientoResponse(
                envio.getNumeroGuia(),
                envio.getEstado() != null ? envio.getEstado().name() : null,
                eventos);
    }
}
