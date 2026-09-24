package com.calisat.msenvios.model;

import com.calisat.msenvios.exception.EstadoInvalidoException;

import java.util.Map;
import java.util.Set;

/**
 * Estados del ciclo de vida de un envio.
 */
public enum EstadoEnvio {

    CREADO,
    EN_PREPARACION,
    DESPACHADO,
    EN_TRANSITO,
    ENTREGADO,
    FALLIDO,
    DEVUELTO;

    /**
     * Maquina de transiciones simple. ENTREGADO y DEVUELTO son estados
     * terminales; no se permite retroceder (p.ej. ENTREGADO -> CREADO).
     */
    private static final Map<EstadoEnvio, Set<EstadoEnvio>> TRANSICIONES = Map.of(
            CREADO, Set.of(EN_PREPARACION, FALLIDO, DEVUELTO),
            EN_PREPARACION, Set.of(DESPACHADO, FALLIDO, DEVUELTO),
            DESPACHADO, Set.of(EN_TRANSITO, FALLIDO, DEVUELTO),
            EN_TRANSITO, Set.of(ENTREGADO, FALLIDO, DEVUELTO),
            FALLIDO, Set.of(DEVUELTO),
            ENTREGADO, Set.of(),
            DEVUELTO, Set.of());

    public static EstadoEnvio from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new EstadoInvalidoException(valor);
        }
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new EstadoInvalidoException(valor);
        }
    }

    public static boolean permiteTransicion(EstadoEnvio actual, EstadoEnvio destino) {
        if (actual == null || destino == null) {
            return false;
        }
        return TRANSICIONES.getOrDefault(actual, Set.of()).contains(destino);
    }
}
