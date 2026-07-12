package com.srcarq.hangman;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifra y descifra el {@link GameState} en un token opaco que viaja al cliente.
 *
 * <p>Usa AES-GCM, que aporta confidencialidad (la palabra secreta nunca es
 * legible por el cliente) e integridad (un token manipulado falla al validar el
 * tag y se rechaza). Al ser stateless, el backend no guarda partidas en memoria,
 * lo que lo hace compatible con Cloud Run multi-instancia.</p>
 *
 * @author srcarq
 */
@Component
public class GameCodec {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;        // 96 bits, recomendado para GCM
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final ObjectMapper mapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    public GameCodec(@Value("${hangman.token.secret}") String secret, ObjectMapper mapper) {
        this.mapper = mapper;
        this.key = deriveKey(secret);
    }

    /** Serializa el estado a JSON, lo cifra y devuelve un token base64url. */
    public String encode(GameState state) {
        try {
            byte[] plaintext = mapper.writeValueAsBytes(state);

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return encoder.encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token de partida", e);
        }
    }

    /**
     * Descifra el token y reconstruye el estado.
     *
     * @throws InvalidTokenException si el token es nulo, mal formado o manipulado
     */
    public GameState decode(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException();
        }
        try {
            byte[] combined = decoder.decode(token);
            if (combined.length <= IV_LENGTH) {
                throw new InvalidTokenException();
            }
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            GameState state = mapper.readValue(plaintext, GameState.class);
            validate(state);
            return state;
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            // base64 inválido, tag GCM que no valida (manipulado) o JSON corrupto.
            throw new InvalidTokenException();
        }
    }

    private void validate(GameState state) {
        if (state == null || state.gameId() == null || state.gameId().isBlank()
                || state.word() == null || state.word().isBlank()
                || state.masked() == null || state.guessed() == null
                || state.status() == null || state.difficulty() == null
                || state.remainingTimeMillis() < 0) {
            throw new InvalidTokenException();
        }
    }

    private SecretKeySpec deriveKey(String secret) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar la clave del token", e);
        }
    }
}
