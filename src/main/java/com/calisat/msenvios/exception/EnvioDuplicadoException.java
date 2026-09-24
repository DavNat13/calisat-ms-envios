package com.calisat.msenvios.exception;

public class EnvioDuplicadoException extends RuntimeException {

    public EnvioDuplicadoException() {
        super("La orden ya tiene un envio activo");
    }
}
