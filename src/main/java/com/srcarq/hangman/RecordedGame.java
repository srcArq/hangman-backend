package com.srcarq.hangman;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Marca una partida ganada ya contabilizada en el ranking (clave = hash del
 * token ganador), para que un mismo token no sume varias veces (anti-reenvío).
 *
 * @author srcarq
 */
@Document(collection = "recordedGames")
public class RecordedGame {

    @Id
    private String id;

    public RecordedGame() {
    }

    public RecordedGame(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
