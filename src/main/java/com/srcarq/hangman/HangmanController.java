package com.srcarq.hangman;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API del juego. El controlador no guarda estado de partida: toda la partida
 * vive en el token cifrado que viaja con cada petición. El ranking sí persiste
 * (MongoDB) a través de {@link LeaderboardService}.
 *
 * @author srcarq
 */
@RestController
@RequestMapping("/api")
public class HangmanController {

    private final GameService gameService;
    private final GameCodec gameCodec;
    private final LeaderboardService leaderboardService;

    public HangmanController(GameService gameService, GameCodec gameCodec,
                             LeaderboardService leaderboardService) {
        this.gameService = gameService;
        this.gameCodec = gameCodec;
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/selectLevel")
    public GameResponse selectLevel(@Valid @RequestBody SelectLevelRequest request) {
        GameState state = gameService.newGame(
                Difficulty.fromLevel(request.level()),
                Language.fromCode(request.language())
        );
        return toResponse(state);
    }

    @PostMapping("/checkLetter")
    public GameResponse checkLetter(@Valid @RequestBody CheckLetterRequest request) {
        GameState current = gameCodec.decode(request.token());
        GameState next = gameService.guess(current, request.letter());
        return toResponse(next);
    }

    /** Registra una victoria del jugador en el ranking. */
    @PostMapping("/score")
    public List<RankingEntry> recordWin(@Valid @RequestBody ScoreRequest request) {
        leaderboardService.recordWin(request.name().trim());
        return ranking();
    }

    /** Top 5 jugadores por victorias. */
    @GetMapping("/ranking")
    public List<RankingEntry> ranking() {
        return leaderboardService.topPlayers().stream()
                .map(p -> new RankingEntry(p.getName(), p.getWins()))
                .toList();
    }

    private GameResponse toResponse(GameState state) {
        boolean finished = state.status() != GameStatus.IN_PROGRESS;
        return new GameResponse(
                gameCodec.encode(state),
                state.masked(),
                state.attempts(),
                new ArrayList<>(state.guessed()),
                state.word().length(),
                state.status().name(),
                finished ? state.word() : null
        );
    }
}
