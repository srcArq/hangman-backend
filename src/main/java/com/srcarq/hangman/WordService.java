package com.srcarq.hangman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Sirve las palabras del juego. Al arrancar reconcilia la colección de Mongo
 * con los ficheros de recurso ({@code words/<lang>_<level>.txt}) usando una
 * inserción por lotes (idempotente y rápida) y cachea las palabras en memoria.
 * Si Mongo no está disponible, hace <em>fallback</em> a los ficheros, de modo
 * que el juego nunca se queda sin palabras.
 *
 * @author srcarq
 */
@Service
public class WordService implements WordProvider {

    private static final Logger log = LoggerFactory.getLogger(WordService.class);
    private static final int[] LEVELS = {1, 2, 3, 4};

    private final WordRepository wordRepository;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        List<Word> all = buildAllFromResources();
        // Los recursos son siempre la base disponible, incluso durante un
        // arranque parcial o una caída de Mongo.
        loadIntoCache(all);
        try {
            if (!all.isEmpty()) {
                // IDs deterministas: saveAll actúa como upsert idempotente y no
                // borra palabras personalizadas ni deja la colección vacía si
                // dos instancias arrancan a la vez.
                wordRepository.saveAll(all);
            }
            loadFromMongo();
            log.info("Palabras cargadas desde MongoDB ({} combinaciones idioma/nivel).", cache.size());
        } catch (Exception e) {
            log.warn("MongoDB no disponible para palabras; usando ficheros de recurso. Causa: {}", e.getMessage());
        }
        log.info("Palabras de recurso disponibles como fallback.");
    }

    @Override
    public String selectWord(Language language, int level) {
        List<String> words = cache.get(key(language, level));
        if (words == null || words.isEmpty()) {
            words = cache.get(key(Language.ES, level)); // fallback de idioma
        }
        if (words == null || words.isEmpty()) {
            throw new IllegalStateException("No hay palabras para el nivel " + level);
        }
        return words.get(ThreadLocalRandom.current().nextInt(words.size()));
    }

    private List<Word> buildAllFromResources() {
        List<Word> all = new ArrayList<>();
        for (Language lang : Language.values()) {
            for (int level : LEVELS) {
                for (String w : readResource(lang, level)) {
                    all.add(new Word(lang.name(), level, w));
                }
            }
        }
        return all;
    }

    private void loadFromMongo() {
        for (Language lang : Language.values()) {
            for (int level : LEVELS) {
                List<String> words = wordRepository.findByLanguageAndLevel(lang.name(), level)
                        .stream().map(Word::getWord).toList();
                if (!words.isEmpty()) {
                    cache.put(key(lang, level), words);
                }
            }
        }
    }

    private void loadIntoCache(List<Word> all) {
        Map<String, List<String>> grouped = new HashMap<>();
        for (Word w : all) {
            grouped.computeIfAbsent(w.getLanguage() + "_" + w.getLevel(), k -> new ArrayList<>())
                    .add(w.getWord());
        }
        cache.putAll(grouped);
    }

    private List<String> readResource(Language lang, int level) {
        String path = "words/" + lang.name().toLowerCase(Locale.ROOT) + "_" + level + ".txt";
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .map(s -> s.trim().toUpperCase(Locale.ROOT))
                    .filter(s -> s.matches("[A-ZÑ]+"))
                    .distinct()
                    .toList();
        } catch (IOException e) {
            log.warn("No se pudo leer el recurso {}", path);
            return List.of();
        }
    }

    private String key(Language lang, int level) {
        return lang.name() + "_" + level;
    }
}
