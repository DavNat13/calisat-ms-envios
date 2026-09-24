package com.calisat.msenvios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvioEstadoRequest(
        @Schema(description = "Nuevo estado del envio.", example = "EN_TRANSITO", maxLength = 32)
        @NotBlank(message = "estado es obligatorio")
        @Size(max = 32, message = "estado no puede superar 32 caracteres")
        String estado,

        @Schema(description = "Descripcion opcional del evento.", example = "En reparto", maxLength = 500)
        @Size(max = 500, message = "descripcion no puede superar 500 caracteres")
        String descripcion,

        @Schema(description = "Ubicacion opcional en la que ocurre el evento.", example = "Centro logistico Madrid", maxLength = 200)
        @Size(max = 200, message = "ubicacion no puede superar 200 caracteres")
        String ubicacion) {
}
