package com.srcarq.hangman;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Registro de partidas ya contabilizadas (anti-reenvío del ranking).
 *
 * @author srcarq
 */
public interface RecordedGameRepository extends MongoRepository<RecordedGame, String> {
}
