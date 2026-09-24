package com.calisat.msenvios.dto;

import com.calisat.msenvios.model.Envio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnvioResponse(
        @Schema(description = "Identificador interno de persistencia.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Identificador logico de la orden asociada.", example = "3f1a8c0e-1f2b-4c3d-9e8f-0a1b2c3d4e5f")
        UUID ordenId,

        @Schema(description = "Sub (oid) del usuario propietario del envio.", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789")
        String usuarioSub,

        @Schema(description = "Numero de guia unico (prefijo CAL-). Identificador canonico del envio.", example = "CAL-9F2B41C7A0D3")
        String numeroGuia,

        @Schema(description = "Transportista asignado.", example = "SEUR")
        String transportista,

        @Schema(description = "Estado actual del envio.", example = "EN_TRANSITO")
        String estado,

        @Schema(description = "Calle de entrega (snapshot).", example = "Calle Mayor 1")
        String direccionCalle,

        @Schema(description = "Ciudad de entrega (snapshot).", example = "Madrid")
        String direccionCiudad,

        @Schema(description = "Pais de entrega (snapshot).", example = "Espana")
        String direccionPais,

        @Schema(description = "Codigo postal de entrega (snapshot).", example = "28001")
        String direccionCodigoPostal,

        @Schema(description = "Fecha de creacion del envio.")
        LocalDateTime fechaCreacion,

        @Schema(description = "Fecha de despacho (se establece en EN_PREPARACION/DESPACHADO).")
        LocalDateTime fechaDespacho,

        @Schema(description = "Fecha de entrega estimada.")
        LocalDateTime fechaEntregaEstimada,

        @Schema(description = "Fecha de entrega real (se establece en ENTREGADO).")
        LocalDateTime fechaEntregaReal,

        @Schema(description = "Fecha de ultima actualizacion del registro.")
        LocalDateTime fechaActualizacion) {

    public static EnvioResponse desde(Envio e) {
        return new EnvioResponse(
                e.getId(),
                e.getOrdenId(),
                e.getUsuarioSub(),
                e.getNumeroGuia(),
                e.getTransportista(),
                e.getEstado() != null ? e.getEstado().name() : null,
                e.getDireccionCalle(),
                e.getDireccionCiudad(),
                e.getDireccionPais(),
                e.getDireccionCodigoPostal(),
                e.getFechaCreacion(),
                e.getFechaDespacho(),
                e.getFechaEntregaEstimada(),
                e.getFechaEntregaReal(),
                e.getFechaActualizacion());
    }
}
