package com.srcarq.hangman;

import jakarta.validation.Valid;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API del juego. El controlador no guarda estado: toda la partida vive en el
 * token cifrado que viaja con cada petición, por lo que es seguro entre
 * peticiones concurrentes y compatible con despliegues multi-instancia.
 *
 * @author srcarq
 */
@RestController
@RequestMapping("/api")
public class HangmanController {

    private final GameService gameService;
    private final GameCodec gameCodec;

    public HangmanController(GameService gameService, GameCodec gameCodec) {
        this.gameService = gameService;
        this.gameCodec = gameCodec;
    }

    @PostMapping("/selectLevel")
    public GameResponse selectLevel(@Valid @RequestBody SelectLevelRequest request) {
        GameState state = gameService.newGame(Difficulty.fromLevel(request.level()));
        return toResponse(state);
    }

    @PostMapping("/checkLetter")
    public GameResponse checkLetter(@Valid @RequestBody CheckLetterRequest request) {
        GameState current = gameCodec.decode(request.token());
        GameState next = gameService.guess(current, request.letter());
        return toResponse(next);
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
