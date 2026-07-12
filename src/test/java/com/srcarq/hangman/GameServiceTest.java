package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameServiceTest {

    // Proveedor de palabras de prueba: una palabra fija e independiente de Mongo.
    private final WordProvider words = (language, level) -> "CASA";
    private final GameService service = new GameService(words);

    private GameState gameWith(String word, int attempts) {
        List<String> masked = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            masked.add("_");
        }
        return new GameState("game-test", word, masked, attempts,
                new LinkedHashSet<>(), GameStatus.IN_PROGRESS, Difficulty.EASY,
                0, 0);
    }

    @Test
    void newGameMasksAllLettersAndSetsAttemptsByLevel() {
        GameState easy = service.newGame(Difficulty.EASY, Language.ES);
        assertThat(easy.attempts()).isEqualTo(10);
        assertThat(easy.masked()).containsOnly("_");
        assertThat(easy.masked()).hasSize(easy.word().length());
        assertThat(easy.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(easy.gameId()).isNotBlank();

        assertThat(service.newGame(Difficulty.MEDIUM, Language.CA).attempts()).isEqualTo(7);
        assertThat(service.newGame(Difficulty.HARD, Language.EN).attempts()).isEqualTo(5);
        assertThat(service.newGame(Difficulty.EXTREME, Language.ES).remainingTimeMillis())
                .isEqualTo(30_000);
    }

    @Test
    void correctGuessRevealsAllOccurrencesWithoutLosingAttempts() {
        GameState state = service.guess(gameWith("CASA", 10), "A");
        assertThat(state.masked()).containsExactly("_", "A", "_", "A");
        assertThat(state.attempts()).isEqualTo(10);
        assertThat(state.guessed()).containsExactly("A");
        assertThat(state.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void wrongGuessDecrementsAttempts() {
        GameState state = service.guess(gameWith("CASA", 10), "Z");
        assertThat(state.masked()).containsOnly("_");
        assertThat(state.attempts()).isEqualTo(9);
    }

    @Test
    void repeatedGuessDoesNotPenalizeTwice() {
        GameState first = service.guess(gameWith("CASA", 10), "Z");
        GameState second = service.guess(first, "Z");
        assertThat(second.attempts()).isEqualTo(9);
        assertThat(second).isSameAs(first); // idempotente: mismo estado, sin penalizar
    }

    @Test
    void winsWhenAllLettersRevealed() {
        GameState state = gameWith("OJO", 10);
        state = service.guess(state, "O");
        assertThat(state.status()).isEqualTo(GameStatus.IN_PROGRESS);
        state = service.guess(state, "J");
        assertThat(state.status()).isEqualTo(GameStatus.WON);
        assertThat(state.masked()).containsExactly("O", "J", "O");
    }

    @Test
    void losesWhenAttemptsReachZero() {
        GameState state = service.guess(gameWith("SOL", 1), "Z");
        assertThat(state.attempts()).isEqualTo(0);
        assertThat(state.status()).isEqualTo(GameStatus.LOST);
    }

    @Test
    void rejectsGuessAfterGameOver() {
        GameState lost = new GameState("game-test", "SOL", List.of("_", "_", "_"),
                0, new LinkedHashSet<>(), GameStatus.LOST, Difficulty.EASY, 0, 0);
        assertThatThrownBy(() -> service.guess(lost, "A"))
                .isInstanceOf(GameOverException.class);
    }

    @Test
    void normalizesLowercaseLetters() {
        GameState state = service.guess(gameWith("CASA", 10), "a");
        assertThat(state.masked()).containsExactly("_", "A", "_", "A");
    }

    @Test
    void rejectsInvalidLetters() {
        assertThatThrownBy(() -> service.guess(gameWith("CASA", 10), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.guess(gameWith("CASA", 10), "AB"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.guess(gameWith("CASA", 10), "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handlesEnye() {
        GameState state = service.guess(gameWith("ÑU", 10), "ñ");
        assertThat(state.masked()).containsExactly("Ñ", "_");
    }

    @Test
    void extremeTimerIsServerAuthoritativeAndAppliesBonusAndPenalty() {
        MutableClock clock = new MutableClock(1_000_000);
        GameService timedService = new GameService(words, clock);
        GameState state = timedService.newGame(Difficulty.EXTREME, Language.ES);

        clock.advanceMillis(5_000);
        state = timedService.guess(state, "A");
        assertThat(state.remainingTimeMillis()).isEqualTo(29_000); // 30 - 5 + 4

        clock.advanceMillis(2_000);
        state = timedService.guess(state, "Z");
        assertThat(state.remainingTimeMillis()).isEqualTo(21_000); // 29 - 2 - 6
        assertThat(state.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void extremeReplayCannotResetElapsedTimeAndExpiresOnServer() {
        MutableClock clock = new MutableClock(2_000_000);
        GameService timedService = new GameService(words, clock);
        GameState original = timedService.newGame(Difficulty.EXTREME, Language.ES);

        clock.advanceMillis(31_000);
        GameState expired = timedService.guess(original, "A");

        assertThat(expired.status()).isEqualTo(GameStatus.LOST);
        assertThat(expired.remainingTimeMillis()).isZero();
        assertThat(expired.masked()).containsOnly("_");
    }

    private static final class MutableClock extends Clock {
        private long millis;

        private MutableClock(long millis) {
            this.millis = millis;
        }

        void advanceMillis(long amount) {
            millis += amount;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis() {
            return millis;
        }
    }
}
