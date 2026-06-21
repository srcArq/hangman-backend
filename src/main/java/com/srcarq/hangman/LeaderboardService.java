package com.srcarq.hangman;

import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Ranking de jugadores por victorias totales, persistido en MongoDB.
 *
 * @author srcarq
 */
@Service
public class LeaderboardService {

    private final MongoTemplate mongoTemplate;
    private final PlayerScoreRepository playerScoreRepository;

    public LeaderboardService(MongoTemplate mongoTemplate, PlayerScoreRepository playerScoreRepository) {
        this.mongoTemplate = mongoTemplate;
        this.playerScoreRepository = playerScoreRepository;
    }

    /** Suma una victoria al jugador (lo crea si no existe) de forma atómica. */
    public void recordWin(String name) {
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
}
