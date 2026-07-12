package com.srcarq.hangman;

import java.util.List;
import java.util.Set;

/**
 * Estado completo de una partida. Inmutable: cada jugada produce un nuevo
 * {@code GameState}. Es la pieza que se cifra dentro del token, por eso
 * incluye la palabra secreta (que nunca debe exponerse al cliente sin cifrar).
 *
 * @author srcarq
 */
public record GameState(
        String gameId,
        String word,
        List<String> masked,
        int attempts,
        Set<String> guessed,
        GameStatus status,
        Difficulty difficulty,
        long remainingTimeMillis,
        long timerUpdatedAtEpochMillis
) {
}
