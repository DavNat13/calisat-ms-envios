package com.calisat.msenvios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EnvioRequest(
        @Schema(description = "Identificador logico de la orden a la que pertenece el envio (unico: una orden = un envio).", example = "3f1a8c0e-1f2b-4c3d-9e8f-0a1b2c3d4e5f")
        @NotNull(message = "ordenId es obligatorio")
        UUID ordenId,

        @Schema(description = "Sub (oid) del usuario propietario del envio. Si se omite se toma del claim 'sub' del JWT.", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", maxLength = 128)
        @Size(max = 128, message = "usuarioSub no puede superar 128 caracteres")
        String usuarioSub,

        @Schema(description = "Calle de entrega (snapshot de la direccion).", example = "Calle Mayor 1", maxLength = 200)
        @Size(max = 200, message = "direccionCalle no puede superar 200 caracteres")
        String direccionCalle,

        @Schema(description = "Ciudad de entrega (snapshot de la direccion).", example = "Madrid", maxLength = 120)
        @Size(max = 120, message = "direccionCiudad no puede superar 120 caracteres")
        String direccionCiudad,

        @Schema(description = "Pais de entrega (snapshot de la direccion).", example = "Espana", maxLength = 64)
        @Size(max = 64, message = "direccionPais no puede superar 64 caracteres")
        String direccionPais,

        @Schema(description = "Codigo postal de entrega (snapshot de la direccion).", example = "28001", maxLength = 16)
        @Size(max = 16, message = "direccionCodigoPostal no puede superar 16 caracteres")
        String direccionCodigoPostal,

        @Schema(description = "Transportista asignado. Opcional hasta el despacho.", example = "SEUR", maxLength = 120)
        @Size(max = 120, message = "transportista no puede superar 120 caracteres")
        String transportista) {
}
