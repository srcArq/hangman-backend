package com.srcarq.hangman;

import java.util.List;

/**
 * Respuesta enviada al cliente. La palabra solo se revela en
 * {@code resultWord} cuando la partida ha terminado (WON o LOST); mientras está
 * en juego viaja únicamente cifrada dentro del {@code token}.
 *
 * @author srcarq
 */
public record GameResponse(
        String token,
        List<String> maskedWord,
        int attempts,
        List<String> guessedLetters,
        int wordLength,
        String status,
        String resultWord
) {
}
