package com.srcarq.hangman;

/**
 * Fuente de palabras del juego. Abstrae de dónde vienen (Mongo, recursos…) para
 * que {@link GameService} sea fácil de testear.
 *
 * @author srcarq
 */
public interface WordProvider {

    /** Devuelve una palabra aleatoria para el idioma y nivel dados. */
    String selectWord(Language language, int level);
}
