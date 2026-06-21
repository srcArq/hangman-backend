package com.srcarq.hangman;

/**
 * Se lanza cuando el token de partida no se puede descifrar: ausente, mal
 * formado, manipulado (el tag GCM no valida) o de una clave distinta.
 *
 * @author srcarq
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Token de partida inválido o manipulado");
    }
}
