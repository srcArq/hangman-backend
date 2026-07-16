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
    private String name;
    private String browserId;
    private boolean countedInPlayer;

    public RecordedGame() {
    }

    public RecordedGame(String id, String name) {
        this(id, name, null, false);
    }

    public RecordedGame(String id, String name, String browserId) {
        this(id, name, browserId, false);
    }

    public RecordedGame(String id, String name, String browserId, boolean countedInPlayer) {
        this.id = id;
        this.name = name;
        this.browserId = browserId;
        this.countedInPlayer = countedInPlayer;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrowserId() {
        return browserId;
    }

    public boolean isCountedInPlayer() {
        return countedInPlayer;
    }
}
