package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WordFactoryTest {

    private final WordFactory wordFactory = new WordFactory();

    @Test
    void selectsAValidWordForEachLevel() {
        for (int level = 1; level <= 3; level++) {
            String word = wordFactory.selectWordByLevel(level);
            assertThat(word).isNotBlank();
            assertThat(word).matches("[A-ZÑ]+");
        }
    }

    @Test
    void throwsOnInvalidLevel() {
        assertThatThrownBy(() -> wordFactory.selectWordByLevel(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> wordFactory.selectWordByLevel(99))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
