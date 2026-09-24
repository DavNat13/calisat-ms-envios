package com.calisat.msenvios.exception;

public class TransicionEstadoNoPermitidaException extends RuntimeException {

    public TransicionEstadoNoPermitidaException(String actual, String destino) {
        super("Transicion de estado no permitida: de " + actual + " a " + destino);
    }
}
