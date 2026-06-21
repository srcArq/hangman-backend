package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class LeaderboardServiceTest {

    @Test
    void recordWinUpsertsWithIncrement() {
        MongoTemplate template = mock(MongoTemplate.class);
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        LeaderboardService service = new LeaderboardService(template, repo);

        service.recordWin("ana");

        verify(template).upsert(any(Query.class), any(Update.class), eq(PlayerScore.class));
    }

    @Test
    void topPlayersReturnsRepositoryResult() {
        MongoTemplate template = mock(MongoTemplate.class);
        PlayerScoreRepository repo = mock(PlayerScoreRepository.class);
        when(repo.findTop5ByOrderByWinsDesc())
                .thenReturn(List.of(new PlayerScore("ana", 5), new PlayerScore("leo", 3)));
        LeaderboardService service = new LeaderboardService(template, repo);

        List<PlayerScore> top = service.topPlayers();

        assertThat(top).hasSize(2);
        assertThat(top.get(0).getName()).isEqualTo("ana");
        assertThat(top.get(0).getWins()).isEqualTo(5);
    }
}
