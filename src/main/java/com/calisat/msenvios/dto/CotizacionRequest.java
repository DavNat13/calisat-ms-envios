package com.calisat.msenvios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CotizacionRequest(
        @Schema(description = "Ciudad o comuna de entrega.", example = "Puerto Montt", maxLength = 120)
        @Size(max = 120, message = "direccionCiudad no puede superar 120 caracteres")
        String direccionCiudad,

        @Schema(description = "Pais de entrega. Cualquier valor distinto de Chile se tarifa como internacional.", example = "Chile", maxLength = 64)
        @Size(max = 64, message = "direccionPais no puede superar 64 caracteres")
        String direccionPais,

        @Schema(description = "Subtotal del pedido (define el envio gratis por monto).", example = "85000")
        @DecimalMin(value = "0", message = "montoSubtotal no puede ser negativo")
        BigDecimal montoSubtotal,

        @Schema(description = "Peso total en kilogramos. Hasta 3 kg no genera recargo.", example = "5")
        @Min(value = 0, message = "pesoKg no puede ser negativo")
        @Max(value = 500, message = "pesoKg no puede superar 500")
        Integer pesoKg) {
}
