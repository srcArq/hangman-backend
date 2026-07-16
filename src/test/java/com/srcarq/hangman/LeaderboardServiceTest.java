package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
        when(repo.findById("ana")).thenReturn(Optional.empty());
        LeaderboardService service = new LeaderboardService(repo, recorded);

        service.recordWin("ana", "browser-1", won());

        ArgumentCaptor<RecordedGame> event = ArgumentCaptor.forClass(RecordedGame.class);
        verify(recorded).insert(event.capture());
        assertThat(event.getValue().getId()).isEqualTo("game-1");
        assertThat(event.getValue().getName()).isEqualTo("ana");
        assertThat(event.getValue().getBrowserId()).isEqualTo("browser-1");
        verify(repo).save(any(PlayerScore.class));
    }

    @Test
    void rejectsNonWinningGame() {
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        LeaderboardService service = new LeaderboardService(
                mock(PlayerScoreRepository.class), recorded);

        assertThatThrownBy(() -> service.recordWin("ana", "browser-1", inProgress()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(recorded, never()).insert(any(RecordedGame.class));
    }

    @Test
    void ignoresReplayedGameEvenWhenWinningTokensCouldDiffer() {
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        when(recorded.insert(any(RecordedGame.class)))
                .thenReturn(new RecordedGame("same-game", "ana"))
                .thenThrow(new DuplicateKeyException("dup"));
        when(repo.findById("ana")).thenReturn(Optional.empty());
        LeaderboardService service = new LeaderboardService(repo, recorded);

        service.recordWin("ana", "browser-1", won("same-game"));
        service.recordWin("ana", "browser-1", won("same-game"));

        verify(recorded, org.mockito.Mockito.times(2)).insert(any(RecordedGame.class));
        verify(repo, org.mockito.Mockito.times(1)).save(any(PlayerScore.class));
    }

    @Test
    void updatesExistingPlayerInsteadOfCreatingDuplicateRows() {
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        when(repo.findById("ana")).thenReturn(Optional.of(new PlayerScore("ana", 5)));
        LeaderboardService service = new LeaderboardService(repo, recorded);

        service.recordWin(" ana ", "browser-1", won("game-2"));

        ArgumentCaptor<PlayerScore> score = ArgumentCaptor.forClass(PlayerScore.class);
        verify(repo).save(score.capture());
        assertThat(score.getValue().getName()).isEqualTo("ana");
        assertThat(score.getValue().getWins()).isEqualTo(6);
    }

    @Test
    void topPlayersReturnsRepositoryResult() {
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        when(repo.findAll()).thenReturn(List.of(
                new PlayerScore("ana", 5), new PlayerScore("leo", 3)));
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        when(recorded.findAll()).thenReturn(List.of(
                new RecordedGame("new-1", "leo"),
                new RecordedGame("new-3", "mia"),
                new RecordedGame("counted", "ana", "browser-1", true),
                new RecordedGame("old-token-hash", null)));
        LeaderboardService service = new LeaderboardService(
                repo, recorded);

        List<PlayerScore> top = service.topPlayers();

        assertThat(top).hasSize(3);
        assertThat(top.get(0).getName()).isEqualTo("ana");
        assertThat(top.get(0).getWins()).isEqualTo(5);
        assertThat(top.get(1).getWins()).isEqualTo(4);
        assertThat(top.get(2).getName()).isEqualTo("mia");
        assertThat(top.get(2).getWins()).isEqualTo(1);
    }

}
