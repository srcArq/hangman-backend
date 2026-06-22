package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class LeaderboardServiceTest {

    private GameState won() {
        return new GameState("CASA", List.of("C", "A", "S", "A"), 7,
                Set.of("C", "A", "S"), GameStatus.WON, Difficulty.EASY);
    }

    private GameState inProgress() {
        return new GameState("CASA", List.of("_", "A", "_", "A"), 7,
                Set.of("A"), GameStatus.IN_PROGRESS, Difficulty.EASY);
    }

    @Test
    void recordsWinFromWonGame() {
        MongoTemplate template = mock(MongoTemplate.class);
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        LeaderboardService service = new LeaderboardService(template, repo, recorded);

        service.recordWin("ana", won(), "token-abc");

        verify(recorded).insert(any(RecordedGame.class));
        verify(template).upsert(any(Query.class), any(Update.class), eq(PlayerScore.class));
    }

    @Test
    void rejectsNonWinningGame() {
        MongoTemplate template = mock(MongoTemplate.class);
        LeaderboardService service = new LeaderboardService(
                template, mock(PlayerScoreRepository.class), mock(RecordedGameRepository.class));

        assertThatThrownBy(() -> service.recordWin("ana", inProgress(), "tok"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(template, never()).upsert(any(), any(), eq(PlayerScore.class));
    }

    @Test
    void ignoresReplayedToken() {
        MongoTemplate template = mock(MongoTemplate.class);
        RecordedGameRepository recorded = mock(RecordedGameRepository.class);
        when(recorded.insert(any(RecordedGame.class))).thenThrow(new DuplicateKeyException("dup"));
        LeaderboardService service = new LeaderboardService(
                template, mock(PlayerScoreRepository.class), recorded);

        service.recordWin("ana", won(), "token-abc"); // mismo token reenviado

        verify(template, never()).upsert(any(), any(), eq(PlayerScore.class));
    }

    @Test
    void topPlayersReturnsRepositoryResult() {
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        when(repo.findTop5ByOrderByWinsDesc())
                .thenReturn(List.of(new PlayerScore("ana", 5), new PlayerScore("leo", 3)));
        LeaderboardService service = new LeaderboardService(
                mock(MongoTemplate.class), repo, mock(RecordedGameRepository.class));

        List<PlayerScore> top = service.topPlayers();

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getName()).isEqualTo("ana");
    }
}
