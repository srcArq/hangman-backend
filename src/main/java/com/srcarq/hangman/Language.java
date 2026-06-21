package com.srcarq.hangman;

import java.util.Locale;

/**
 * Idiomas soportados para las palabras del juego.
 *
 * @author srcarq
 */
public enum Language {
    ES, // Castellano
    CA, // Catalán
    EN; // Inglés

    /** Devuelve el idioma a partir de su código (ES/CA/EN); ES por defecto si es inválido. */
    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ES;
        }
        try {
            return Language.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ES;
        }
    }
}
