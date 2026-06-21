package com.srcarq.hangman;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Puntuación de un jugador en el ranking (colección "players"). El nombre es la
 * clave, por lo que cada jugador tiene una única entrada.
 *
 * @author srcarq
 */
@Document(collection = "players")
public class PlayerScore {

    @Id
    private String name;
    private int wins;

    public PlayerScore() {
    }

    public PlayerScore(String name, int wins) {
        this.name = name;
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }
}
