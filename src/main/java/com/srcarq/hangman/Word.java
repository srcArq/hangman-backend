package com.srcarq.hangman;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Palabra almacenada en MongoDB (colección "words").
 *
 * @author srcarq
 */
@Document(collection = "words")
public class Word {

    @Id
    private String id;
    private String language; // ES / CA / EN
    private int level;        // 1 / 2 / 3
    private String word;

    public Word() {
    }

    public Word(String language, int level, String word) {
        this.id = language + "_" + level + "_" + word;
        this.language = language;
        this.level = level;
        this.word = word;
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public int getLevel() {
        return level;
    }

    public String getWord() {
        return word;
    }
}
