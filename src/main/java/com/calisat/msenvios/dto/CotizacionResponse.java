package com.calisat.msenvios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record CotizacionResponse(
        @Schema(description = "Zona de despacho aplicada.", example = "SUR")
        String zona,

        @Schema(description = "Etiqueta legible de la zona.", example = "Sur (Los Lagos y Aysén)")
        String etiquetaZona,

        @Schema(description = "Costo de envio en CLP.", example = "3990")
        BigDecimal costo,

        @Schema(description = "Plazo estimado en dias.", example = "2-4")
        String plazo,

        @Schema(description = "True si el costo quedo en 0 por monto minimo.", example = "false")
        boolean gratisPorMonto,

        @Schema(description = "Detalle legible del calculo.", example = "Zona Sur 3990 CLP")
        String descripcion) {
}
