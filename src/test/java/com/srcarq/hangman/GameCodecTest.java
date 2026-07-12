package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GameCodecTest {

    private final GameCodec codec =
            new GameCodec("test-secret", new ObjectMapper().findAndRegisterModules());

    private GameState sample() {
        Set<String> guessed = new LinkedHashSet<>(List.of("A", "Z"));
        return new GameState("game-123", "CASA", List.of("_", "A", "_", "A"),
                9, guessed, GameStatus.IN_PROGRESS, Difficulty.EASY, 0, 0);
    }

    @Test
    void encodeThenDecodeRoundTrips() {
        GameState restored = codec.decode(codec.encode(sample()));

        assertThat(restored.word()).isEqualTo("CASA");
        assertThat(restored.gameId()).isEqualTo("game-123");
        assertThat(restored.masked()).containsExactly("_", "A", "_", "A");
        assertThat(restored.attempts()).isEqualTo(9);
        assertThat(restored.guessed()).containsExactlyInAnyOrder("A", "Z");
        assertThat(restored.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(restored.difficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void tokenDoesNotContainPlainWord() {
        assertThat(codec.encode(sample())).doesNotContain("CASA");
    }

    @Test
    void rejectsTamperedToken() {
        String token = codec.encode(sample());
        int mid = token.length() / 2;
        char replacement = token.charAt(mid) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, mid) + replacement + token.substring(mid + 1);

        assertThatThrownBy(() -> codec.decode(tampered))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rejectsGarbageBlankAndNullToken() {
        assertThatThrownBy(() -> codec.decode("not-a-valid-token!!"))
                .isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> codec.decode(""))
                .isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> codec.decode(null))
                .isInstanceOf(InvalidTokenException.class);
    }
}
