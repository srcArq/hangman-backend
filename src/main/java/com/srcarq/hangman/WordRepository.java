package com.srcarq.hangman;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Acceso a las palabras en MongoDB.
 *
 * @author srcarq
 */
public interface WordRepository extends MongoRepository<Word, String> {

    List<Word> findByLanguageAndLevel(String language, int level);
}
