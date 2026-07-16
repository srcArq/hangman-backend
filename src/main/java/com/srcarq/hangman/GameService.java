package com.srcarq.hangman;

import java.util.ArrayList;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final WordProvider wordProvider;
    private final Clock clock;

    @Autowired
    public GameService(WordProvider wordProvider) {
        this(wordProvider, Clock.systemUTC());
    }

    GameService(WordProvider wordProvider, Clock clock) {
        this.wordProvider = wordProvider;
        this.clock = clock;
    }

    /** Crea una partida nueva con la palabra enmascarada y los intentos del nivel. */
    public GameState newGame(Difficulty difficulty, Language language) {
        String word = wordProvider.selectWord(language, difficulty.getLevel());
        List<String> masked = new ArrayList<>(word.length());
        for (int i = 0; i < word.length(); i++) {
            masked.add("_");
        }
        long now = clock.millis();
        return new GameState(UUID.randomUUID().toString(), word, masked,
                difficulty.getAttempts(), new LinkedHashSet<>(),
                GameStatus.IN_PROGRESS, difficulty,
                difficulty.getInitialTimeMillis(), difficulty.isTimed() ? now : 0);
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
        long now = clock.millis();
        long remainingTime = remainingTimeAt(state, now);

        // Un token antiguo no permite reiniciar el reloj: se descuenta todo el
        // tiempo transcurrido desde la última acción registrada por el servidor.
        if (state.difficulty().isTimed() && remainingTime <= 0) {
            return copyWith(state, state.masked(), state.attempts(), state.guessed(),
                    GameStatus.LOST, 0, now);
        }

        // Idempotente: una letra ya intentada no penaliza ni cambia el estado.
        if (state.guessed().contains(letter)) {
            if (!state.difficulty().isTimed()) {
                return state;
            }
            return copyWith(state, state.masked(), state.attempts(), state.guessed(),
                    state.status(), remainingTime, now);
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

        if (state.difficulty().isTimed()) {
            remainingTime += hit
                    ? state.difficulty().getHitBonusMillis()
                    : -state.difficulty().getMissPenaltyMillis();
            remainingTime = Math.max(0, remainingTime);
        }

        GameStatus status;
        if (!masked.contains("_")) {
            status = GameStatus.WON;
        } else if (state.difficulty().isTimed() && remainingTime <= 0) {
            status = GameStatus.LOST;
        } else if (attempts <= 0) {
            status = GameStatus.LOST;
        } else {
            status = GameStatus.IN_PROGRESS;
        }

        return copyWith(state, masked, attempts, guessed, status, remainingTime,
                state.difficulty().isTimed() ? now : 0);
    }

    /** Cierra una partida en curso como perdida para poder revelar la palabra. */
    public GameState finishAsLost(GameState state) {
        if (state.status() != GameStatus.IN_PROGRESS) {
            return state;
        }
        long now = clock.millis();
        long remainingTime = remainingTimeAt(state, now);
        return copyWith(state, state.masked(), state.attempts(), state.guessed(),
                GameStatus.LOST, state.difficulty().isTimed() ? remainingTime : 0,
                state.difficulty().isTimed() ? now : 0);
    }

    private long remainingTimeAt(GameState state, long now) {
        if (!state.difficulty().isTimed()) {
            return 0;
        }
        long elapsed = Math.max(0, now - state.timerUpdatedAtEpochMillis());
        return Math.max(0, state.remainingTimeMillis() - elapsed);
    }

    private GameState copyWith(GameState state, List<String> masked, int attempts,
                               Set<String> guessed, GameStatus status,
                               long remainingTimeMillis, long timerUpdatedAtEpochMillis) {
        return new GameState(state.gameId(), state.word(), masked, attempts, guessed,
                status, state.difficulty(), remainingTimeMillis,
                timerUpdatedAtEpochMillis);
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
