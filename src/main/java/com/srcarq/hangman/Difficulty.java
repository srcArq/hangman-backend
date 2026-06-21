package com.srcarq.hangman;

/**
 * Niveles de dificultad del juego. Única fuente de verdad para el número de
 * intentos por nivel (antes duplicado como números mágicos en el controlador).
 *
 * @author srcarq
 */
public enum Difficulty {
    EASY(1, 10),
    MEDIUM(2, 7),
    HARD(3, 5);

    private final int level;
    private final int attempts;

    Difficulty(int level, int attempts) {
        this.level = level;
        this.attempts = attempts;
    }

    public int getLevel() {
        return level;
    }

    public int getAttempts() {
        return attempts;
    }

    public static Difficulty fromLevel(int level) {
        for (Difficulty difficulty : values()) {
            if (difficulty.level == level) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Nivel no válido: " + level + " (use 1, 2 o 3)");
    }
}
