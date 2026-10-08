package com.calisat.msenvios.service;

import com.calisat.msenvios.dto.CotizacionRequest;
import com.calisat.msenvios.dto.CotizacionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Tarifas de despacho del checkout. Logica pura y sin estado: se invoca
 * desde {@code POST /api/v1/envios/cotizar} antes de confirmar el pedido
 * para que el cliente vea el costo del envio y el total a pagar.
 *
 * Reglas (fuente de verdad de esta capa):
 *   - Internacional (pais distinto de Chile o vacio con acento extranjero)
 *     -> 14990 CLP, 7-12 dias.
 *   - Zona SUR (Los Lagos y Aysen: Puerto Montt, Osorno, Castro...) -> 3990.
 *   - Zona CENTRO (Metropolitana, Valparaiso, O'Higgins)            -> 5990.
 *   - Zona NORTE y resto del pais                                   -> 7990.
 *   - Envio GRATIS cuando el subtotal alcanza {@link #UMBRAL_GRATIS}.
 *   - Recargo de {@link #RECARGO_POR_KG} por cada kilo sobre 3 kg.
 */
@Service
public class TarifaEnvioService {

    /** Subtotal en CLP a partir del cual el envio sale gratis. */
    public static final BigDecimal UMBRAL_GRATIS = new BigDecimal("80000");

    /** Kilos cubiertos por la tarifa base. */
    public static final int KG_INCLUIDOS = 3;

    /** Recargo en CLP por kilo adicional. */
    public static final BigDecimal RECARGO_POR_KG = new BigDecimal("990");

    private static final BigDecimal COSTO_SUR = new BigDecimal("3990");
    private static final BigDecimal COSTO_CENTRO = new BigDecimal("5990");
    private static final BigDecimal COSTO_NORTE = new BigDecimal("7990");
    private static final BigDecimal COSTO_INTERNACIONAL = new BigDecimal("14990");

    /** Ciudades de la zona sur, ya normalizadas (sin tildes, minusculas). */
    private static final List<String> CIUDADES_SUR = List.of(
            "puerto montt", "puerto varas", "osorno", "castro", "ancud",
            "chaiten", "calbuco", "pailen", "fresia", "llanquihue",
            "puerto aysen", "coyhaique", "aysen", "quellon", "maullin",
            "dalcahue", "chonchi", "isla de maipo");

    /** Ciudades de la zona centro. */
    private static final List<String> CIUDADES_CENTRO = List.of(
            "santiago", "providencia", "nunoa", "las condes", "vitacura",
            "macul", "penalolen", "la florida", "maipu", "puente alto",
            "san bernardo", "colina", "talagante", "padre hurtado",
            "valparaiso", "vina del mar", "quilpue", "villa alemana",
            "quillota", "san antonio", "san felipe", "los andes",
            "rancagua", "san jose de maipo", "buin", "paine");

    /**
     * Costo de envio para un pedido.
     *
     * @param request ciudad/pais de entrega, subtotal y peso
     * @return 200 con la tarifa aplicada, la zona y el plazo estimado
     */
    public CotizacionResponse cotizar(CotizacionRequest request) {
        String ciudad = normalizar(request == null ? null : request.direccionCiudad());
        String pais = normalizar(request == null ? null : request.direccionPais());
        BigDecimal subtotal = request == null || request.montoSubtotal() == null
                ? BigDecimal.ZERO
                : request.montoSubtotal();
        int pesoKg = request == null || request.pesoKg() == null ? 0 : request.pesoKg();

        if (!pais.isEmpty() && !pais.equals("chile")) {
            return respuesta("INTERNACIONAL", "Internacional",
                    COSTO_INTERNACIONAL, "7-12", false,
                    "Envío internacional 14990 CLP (7-12 días hábiles).");
        }

        String zona;
        String etiqueta;
        BigDecimal base;
        String plazo;
        if (CIUDADES_SUR.contains(ciudad)) {
            zona = "SUR";
            etiqueta = "Sur (Los Lagos y Aysén)";
            base = COSTO_SUR;
            plazo = "2-4";
        } else if (CIUDADES_CENTRO.contains(ciudad)) {
            zona = "CENTRO";
            etiqueta = "Centro (Metropolitana, Valparaíso y O'Higgins)";
            base = COSTO_CENTRO;
            plazo = "1-3";
        } else {
            zona = "NORTE";
            etiqueta = "Norte y resto del país";
            base = COSTO_NORTE;
            plazo = "3-6";
        }

        if (subtotal.compareTo(UMBRAL_GRATIS) >= 0) {
            return respuesta(zona, etiqueta, BigDecimal.ZERO, plazo, true,
                    String.format("Envío gratis: tu pedido supera los %s CLP.", UMBRAL_GRATIS));
        }

        BigDecimal total = base.add(recargoPorPeso(pesoKg));
        return respuesta(zona, etiqueta, total, plazo, false,
                String.format("%s %s CLP%s.", etiqueta, total,
                        pesoKg > KG_INCLUIDOS ? " (incluye recargo por peso)" : ""));
    }

    /** Recargo lineal por peso: 0 hasta 3 kg, luego RECARGO_POR_KG por kilo. */
    private BigDecimal recargoPorPeso(int pesoKg) {
        if (pesoKg <= KG_INCLUIDOS) {
            return BigDecimal.ZERO;
        }
        int excedente = pesoKg - KG_INCLUIDOS;
        return RECARGO_POR_KG.multiply(BigDecimal.valueOf(excedente));
    }

    private CotizacionResponse respuesta(String zona, String etiqueta,
            BigDecimal costo, String plazo, boolean gratis, String descripcion) {
        return new CotizacionResponse(zona, etiqueta,
                costo.setScale(0, RoundingMode.HALF_UP), plazo, gratis, descripcion);
    }

    /** Minusculas sin tildes ni signos sobrantes para comparar ciudades. */
    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}
