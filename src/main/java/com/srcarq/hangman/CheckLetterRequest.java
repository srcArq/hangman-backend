package com.srcarq.hangman;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Petición para jugar una letra sobre una partida existente (identificada por
 * su token).
 *
 * @author srcarq
 */
public record CheckLetterRequest(
        @NotBlank(message = "Falta el token de la partida")
        String token,

        @NotBlank(message = "Falta la letra")
        @Pattern(regexp = "(?i)^[a-zñ]$", message = "La letra debe ser una única letra (A-Z o Ñ)")
        String letter
) {
}
