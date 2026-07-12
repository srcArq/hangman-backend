package com.srcarq.hangman;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
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

    private final PlayerScoreRepository playerScoreRepository;
    private final RecordedGameRepository recordedGameRepository;

    public LeaderboardService(PlayerScoreRepository playerScoreRepository,
                              RecordedGameRepository recordedGameRepository) {
        this.playerScoreRepository = playerScoreRepository;
        this.recordedGameRepository = recordedGameRepository;
    }

    /**
     * Suma una victoria al jugador a partir de una partida ganada.
     *
     * @throws IllegalArgumentException si la partida no está ganada
     */
    public void recordWin(String name, GameState state) {
        if (state.status() != GameStatus.WON) {
            throw new IllegalArgumentException("Solo se puntúan partidas ganadas");
        }
        try {
            // El id estable de la partida, no el token aleatorio, cierra los
            // replays ramificados desde un token anterior a la victoria.
            recordedGameRepository.insert(new RecordedGame(state.gameId(), name));
        } catch (DuplicateKeyException e) {
            return; // partida ya contabilizada: no se vuelve a sumar
        }
    }

    /** Top 5 jugadores por victorias. */
    public List<PlayerScore> topPlayers() {
        Map<String, Integer> totals = new HashMap<>();

        // Compatibilidad con el ranking existente: players queda como línea
        // base histórica y las nuevas victorias se guardan como eventos
        // atómicos en recordedGames.
        for (PlayerScore legacy : playerScoreRepository.findAll()) {
            totals.merge(legacy.getName(), legacy.getWins(), Integer::sum);
        }
        for (RecordedGame game : recordedGameRepository.findAll()) {
            if (game.getName() != null && !game.getName().isBlank()) {
                totals.merge(game.getName(), 1, Integer::sum);
            }
        }

        return totals.entrySet().stream()
                .map(entry -> new PlayerScore(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(PlayerScore::getWins).reversed()
                        .thenComparing(PlayerScore::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(5)
                .toList();
    }
}
