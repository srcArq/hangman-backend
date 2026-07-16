package com.srcarq.hangman;

import jakarta.validation.constraints.NotBlank;

/**
 * Petición para cerrar una partida desde el cliente y revelar la palabra.
 *
 * @author srcarq
 */
public record FinishGameRequest(
        @NotBlank(message = "Falta el token de la partida")
        String token
) {
}
