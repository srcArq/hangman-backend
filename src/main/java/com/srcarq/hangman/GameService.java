package com.srcarq.hangman;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Lógica pura del juego, independiente del transporte y del almacenamiento.
 * Cada operación devuelve un {@link GameState} nuevo (inmutable), por lo que es
 * seguro entre hilos: no hay estado mutable compartido.
 *
 * @author srcarq
 */
@Service
public class GameService {

    private final WordFactory wordFactory;

    public GameService(WordFactory wordFactory) {
        this.wordFactory = wordFactory;
    }

    /** Crea una partida nueva con la palabra enmascarada y los intentos del nivel. */
    public GameState newGame(Difficulty difficulty) {
        String word = wordFactory.selectWordByLevel(difficulty.getLevel());
        List<String> masked = new ArrayList<>(word.length());
        for (int i = 0; i < word.length(); i++) {
            masked.add("_");
        }
        return new GameState(word, masked, difficulty.getAttempts(),
                new LinkedHashSet<>(), GameStatus.IN_PROGRESS, difficulty);
    }

    /**
     * Aplica una letra a la partida.
     *
     * @throws GameOverException        si la partida ya terminó
     * @throws IllegalArgumentException si la letra es nula o no es una única letra [A-ZÑ]
     */
    public GameState guess(GameState state, String rawLetter) {
        if (state.status() != GameStatus.IN_PROGRESS) {
            throw new GameOverException();
        }

        String letter = normalizeLetter(rawLetter);

        // Idempotente: una letra ya intentada no penaliza ni cambia el estado.
        if (state.guessed().contains(letter)) {
            return state;
        }

        Set<String> guessed = new LinkedHashSet<>(state.guessed());
        guessed.add(letter);

        List<String> masked = new ArrayList<>(state.masked());
        String word = state.word();
        boolean hit = false;
        for (int i = 0; i < word.length(); i++) {
            if (String.valueOf(word.charAt(i)).equals(letter)) {
                masked.set(i, letter);
                hit = true;
            }
        }

        int attempts = hit ? state.attempts() : state.attempts() - 1;

        GameStatus status;
        if (!masked.contains("_")) {
            status = GameStatus.WON;
        } else if (attempts <= 0) {
            status = GameStatus.LOST;
        } else {
            status = GameStatus.IN_PROGRESS;
        }

        return new GameState(word, masked, attempts, guessed, status, state.difficulty());
    }

    private String normalizeLetter(String rawLetter) {
        if (rawLetter == null) {
            throw new IllegalArgumentException("Falta la letra");
        }
        String letter = rawLetter.trim().toUpperCase(Locale.ROOT);
        if (!letter.matches("[A-ZÑ]")) {
            throw new IllegalArgumentException("Letra no válida: '" + rawLetter + "'");
        }
        return letter;
    }
}
