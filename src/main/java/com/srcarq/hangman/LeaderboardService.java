package com.srcarq.hangman;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Ranking de jugadores por victorias totales, persistido en MongoDB.
 *
 * <p>Solo se contabilizan partidas realmente ganadas: la victoria debe venir
 * acompañada del token ganador, que el servidor verifica ({@code status == WON})
 * y deduplica por su hash, evitando que se infle el ranking con peticiones
 * arbitrarias o reenvíos del mismo token.</p>
 *
 * @author srcarq
 */
@Service
public class LeaderboardService {

    private final MongoTemplate mongoTemplate;
    private final PlayerScoreRepository playerScoreRepository;
    private final RecordedGameRepository recordedGameRepository;

    public LeaderboardService(MongoTemplate mongoTemplate,
                              PlayerScoreRepository playerScoreRepository,
                              RecordedGameRepository recordedGameRepository) {
        this.mongoTemplate = mongoTemplate;
        this.playerScoreRepository = playerScoreRepository;
        this.recordedGameRepository = recordedGameRepository;
    }

    /**
     * Suma una victoria al jugador a partir de una partida ganada.
     *
     * @throws IllegalArgumentException si la partida no está ganada
     */
    public void recordWin(String name, GameState state, String token) {
        if (state.status() != GameStatus.WON) {
            throw new IllegalArgumentException("Solo se puntúan partidas ganadas");
        }
        try {
            recordedGameRepository.insert(new RecordedGame(hash(token)));
        } catch (DuplicateKeyException e) {
            return; // token ya contabilizado (reenvío): no se vuelve a sumar
        }
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(name)),
                new Update().inc("wins", 1),
                PlayerScore.class
        );
    }

    /** Top 5 jugadores por victorias. */
    public List<PlayerScore> topPlayers() {
        return playerScoreRepository.findTop5ByOrderByWinsDesc();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash del token", e);
        }
    }
}
