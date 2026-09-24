package com.calisat.msenvios.exception;

public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String valor) {
        super("Estado invalido: " + valor
                + ". Valores permitidos: CREADO, EN_PREPARACION, DESPACHADO, EN_TRANSITO, ENTREGADO, FALLIDO, DEVUELTO");
    }
}
