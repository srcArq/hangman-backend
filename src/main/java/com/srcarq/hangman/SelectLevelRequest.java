package com.srcarq.hangman;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Petición para iniciar una partida.
 *
 * @author srcarq
 */
public record SelectLevelRequest(
        @Min(value = 1, message = "El nivel debe ser 1, 2, 3 o 4")
        @Max(value = 4, message = "El nivel debe ser 1, 2, 3 o 4")
        int level,

        // Opcional: ES (por defecto), CA o EN. Si es inválido se usa ES.
        String language
) {
}
