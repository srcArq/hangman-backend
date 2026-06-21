package com.srcarq.hangman;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Acceso al ranking de jugadores en MongoDB.
 *
 * @author srcarq
 */
public interface PlayerScoreRepository extends MongoRepository<PlayerScore, String> {

    /** Los 5 jugadores con más victorias, de mayor a menor. */
    List<PlayerScore> findTop5ByOrderByWinsDesc();
}
