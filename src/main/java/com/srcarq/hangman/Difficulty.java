package com.srcarq.hangman;

/**
 * Niveles de dificultad del juego. Única fuente de verdad para el número de
 * intentos por nivel (antes duplicado como números mágicos en el controlador).
 *
 * @author srcarq
 */
public enum Difficulty {
    EASY(1, 10, 0, 0, 0),
    MEDIUM(2, 7, 0, 0, 0),
    HARD(3, 5, 0, 0, 0),
    // En Extremo el servidor es la fuente de verdad del reloj. El valor alto de
    // intentos conserva el dibujo fuera del flujo, pero no sustituye al tiempo.
    EXTREME(4, 99, 30_000, 4_000, 6_000);

    private final int level;
    private final int attempts;
    private final long initialTimeMillis;
    private final long hitBonusMillis;
    private final long missPenaltyMillis;

    Difficulty(int level, int attempts, long initialTimeMillis,
               long hitBonusMillis, long missPenaltyMillis) {
        this.level = level;
        this.attempts = attempts;
        this.initialTimeMillis = initialTimeMillis;
        this.hitBonusMillis = hitBonusMillis;
        this.missPenaltyMillis = missPenaltyMillis;
    }

    public int getLevel() {
        return level;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isTimed() {
        return initialTimeMillis > 0;
    }

    public long getInitialTimeMillis() {
        return initialTimeMillis;
    }

    public long getHitBonusMillis() {
        return hitBonusMillis;
    }

    public long getMissPenaltyMillis() {
        return missPenaltyMillis;
    }

    public static Difficulty fromLevel(int level) {
        for (Difficulty difficulty : values()) {
            if (difficulty.level == level) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Nivel no válido: " + level + " (use 1, 2, 3 o 4)");
    }
}
