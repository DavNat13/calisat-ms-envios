package com.calisat.msenvios.service;

import com.calisat.msenvios.dto.CotizacionRequest;
import com.calisat.msenvios.dto.CotizacionResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tarifas del checkout: zona, envio gratis por monto, recargo por peso e
 * internacional. Son las reglas que el frontend pinta antes de confirmar.
 */
class TarifaEnvioServiceTest {

    private final TarifaEnvioService servicio = new TarifaEnvioService();

    @Test
    void puertoMontt_esZonaSur() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Puerto Montt", "Chile", new BigDecimal("20000"), 1));

        assertEquals("SUR", r.zona());
        assertEquals(new BigDecimal("3990"), r.costo());
        assertFalse(r.gratisPorMonto());
        assertEquals("2-4", r.plazo());
    }

    @Test
    void conTildeMayusculasYEspacios_sigueSiendoLaMismaZona() {
        assertEquals("SUR", servicio.cotizar(
                new CotizacionRequest("  PUERTO MONTT ", "chile", null, null)).zona());
        assertEquals("CENTRO", servicio.cotizar(
                new CotizacionRequest("Ñuñoa", "Chile", null, null)).zona());
        assertEquals("CENTRO", servicio.cotizar(
                new CotizacionRequest("Viña del Mar", "Chile", null, null)).zona());
    }

    @Test
    void santiago_esZonaCentro() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Santiago", "Chile", new BigDecimal("10000"), 1));
        assertEquals("CENTRO", r.zona());
        assertEquals(new BigDecimal("5990"), r.costo());
    }

    @Test
    void iquique_esZonaNorte() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Iquique", "Chile", new BigDecimal("10000"), 1));
        assertEquals("NORTE", r.zona());
        assertEquals(new BigDecimal("7990"), r.costo());
    }

    @Test
    void subtotalAlcanzaElUmbral_esGratis() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Iquique", "Chile", TarifaEnvioService.UMBRAL_GRATIS, 1));

        assertTrue(r.gratisPorMonto());
        assertEquals(0, r.costo().compareTo(BigDecimal.ZERO));
    }

    @Test
    void pesoExcedido_aplicaRecargo() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Puerto Montt", "Chile", new BigDecimal("20000"), 5));

        // 3990 + 2 kg excedidos * 990
        assertEquals(new BigDecimal("5970"), r.costo());
    }

    @Test
    void hastaTresKg_noHayRecargo() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Puerto Montt", "Chile", new BigDecimal("20000"), 3));
        assertEquals(new BigDecimal("3990"), r.costo());
    }

    @Test
    void paisDistintoDeChile_esInternacional() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest("Madrid", "España", new BigDecimal("20000"), 1));

        assertEquals("INTERNACIONAL", r.zona());
        assertEquals(new BigDecimal("14990"), r.costo());
        assertEquals("7-12", r.plazo());
    }

    @Test
    void ciudadVacia_caeEnZonaNorte() {
        CotizacionResponse r = servicio.cotizar(
                new CotizacionRequest(null, null, new BigDecimal("1000"), 1));
        assertEquals("NORTE", r.zona());
    }
}
