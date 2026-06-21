package com.srcarq.hangman;

/**
 * Se lanza cuando se intenta jugar una letra en una partida ya finalizada.
 *
 * @author srcarq
 */
public class GameOverException extends RuntimeException {

    public GameOverException() {
        super("La partida ya ha terminado");
    }
}
