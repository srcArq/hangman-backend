package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class LeaderboardServiceTest {

    private GameState won() {
        return won("game-1");
    }

    private GameState won(String gameId) {
        return new GameState(gameId, "CASA", List.of("C", "A", "S", "A"), 7,
                Set.of("C", "A", "S"), GameStatus.WON, Difficulty.EASY, 0, 0);
    }

    private GameState inProgress() {
        return new GameState("game-1", "CASA", List.of("_", "A", "_", "A"), 7,
                Set.of("A"), GameStatus.IN_PROGRESS, Difficulty.EASY, 0, 0);
    }

    @Test
    void recordsWinFromWonGame() {
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        LeaderboardService service = new LeaderboardService(repo, recorded);

        service.recordWin("ana", won());

        ArgumentCaptor<RecordedGame> event = ArgumentCaptor.forClass(RecordedGame.class);
        verify(recorded).insert(event.capture());
        assertThat(event.getValue().getId()).isEqualTo("game-1");
        assertThat(event.getValue().getName()).isEqualTo("ana");
    }

    @Test
    void rejectsNonWinningGame() {
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        LeaderboardService service = new LeaderboardService(
                mock(PlayerScoreRepository.class), recorded);

        assertThatThrownBy(() -> service.recordWin("ana", inProgress()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(recorded, never()).insert(any(RecordedGame.class));
    }

    @Test
    void ignoresReplayedGameEvenWhenWinningTokensCouldDiffer() {
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        when(recorded.insert(any(RecordedGame.class)))
                .thenReturn(new RecordedGame("same-game", "ana"))
                .thenThrow(new DuplicateKeyException("dup"));
        LeaderboardService service = new LeaderboardService(
                mock(PlayerScoreRepository.class), recorded);

        service.recordWin("ana", won("same-game"));
        service.recordWin("ana", won("same-game"));

        verify(recorded, org.mockito.Mockito.times(2)).insert(any(RecordedGame.class));
    }

    @Test
    void topPlayersReturnsRepositoryResult() {
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        when(repo.findAll()).thenReturn(List.of(
                new PlayerScore("ana", 5), new PlayerScore("leo", 3)));
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        when(recorded.findAll()).thenReturn(List.of(
                new RecordedGame("new-1", "leo"),
                new RecordedGame("old-token-hash", null)));
        LeaderboardService service = new LeaderboardService(
                repo, recorded);

        List<PlayerScore> top = service.topPlayers();

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getName()).isEqualTo("ana");
        assertThat(top.get(0).getWins()).isEqualTo(5);
        assertThat(top.get(1).getWins()).isEqualTo(4);
    }

}
