package com.srcarq.hangman;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Petición para registrar una victoria de un jugador en el ranking.
 *
 * @author srcarq
 */
public record ScoreRequest(
        @NotBlank(message = "Falta el nombre")
        @Size(max = 20, message = "El nombre no puede superar 20 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9 _.-]{1,20}$", message = "Nombre no válido")
        String name
) {
}
