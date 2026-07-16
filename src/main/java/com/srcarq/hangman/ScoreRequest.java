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
        String name,

        // Identificador anónimo guardado en el navegador. No es login ni secreto.
        @Size(max = 80, message = "La sesión del navegador no puede superar 80 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,80}$", message = "Sesión de navegador no válida")
        String browserId,

        // Token de la partida ganada: el servidor verifica que realmente es WON.
        @NotBlank(message = "Falta el token de la partida")
        String token
) {
}
