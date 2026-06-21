package com.srcarq.hangman;

/**
 * Entrada del ranking que se envía al cliente.
 *
 * @author srcarq
 */
public record RankingEntry(String name, int wins) {
}
