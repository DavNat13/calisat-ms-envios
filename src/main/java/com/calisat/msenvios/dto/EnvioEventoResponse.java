package com.calisat.msenvios.dto;

import com.calisat.msenvios.model.EnvioEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EnvioEventoResponse(
        @Schema(description = "Identificador del evento.", example = "12")
        Long id,

        @Schema(description = "Estado del envio en este evento.", example = "DESPACHADO")
        String estado,

        @Schema(description = "Descripcion del evento.", example = "Paquete entregado al transportista")
        String descripcion,

        @Schema(description = "Ubicacion del evento.", example = "Centro logistico Madrid")
        String ubicacion,

        @Schema(description = "Fecha en la que ocurrio el evento.")
        LocalDateTime fechaEvento) {

    public static EnvioEventoResponse desde(EnvioEvento evento) {
        return new EnvioEventoResponse(
                evento.getId(),
                evento.getEstado() != null ? evento.getEstado().name() : null,
                evento.getDescripcion(),
                evento.getUbicacion(),
                evento.getFechaEvento());
    }
}
