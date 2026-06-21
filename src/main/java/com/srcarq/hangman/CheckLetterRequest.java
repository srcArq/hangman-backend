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

        // Incluimos ambos casos de la Ñ explícitamente: (?i) en Java solo pliega
        // mayúsculas/minúsculas ASCII, por lo que 'Ñ' no casaría con [a-zñ].
        @NotBlank(message = "Falta la letra")
        @Pattern(regexp = "^[A-Za-zÑñ]$", message = "La letra debe ser una única letra (A-Z o Ñ)")
        String letter
) {
}
