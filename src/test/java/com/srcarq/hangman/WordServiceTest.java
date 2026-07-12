package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class WordServiceTest {

    @Test
    void loadsValidWordsFromResourcesForEveryLanguageAndLevel() {
        // Sin Mongo: el repo lanza al guardar -> los recursos ya están cargados.
        WordRepository repo = mock(WordRepository.class);
        when(repo.saveAll(any())).thenThrow(new RuntimeException("mongo no disponible"));

        WordService service = new WordService(repo);
        service.init();

        for (Language lang : Language.values()) {
            for (int level = 1; level <= 4; level++) {
                String word = service.selectWord(lang, level);
                assertThat(word)
                        .as("palabra de %s nivel %d", lang, level)
                        .isNotBlank()
                        .matches("[A-ZÑ]+");
            }
        }
    }
}
