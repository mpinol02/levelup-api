package cl.levelup.auth;

// Respuesta de un login correcto. expiresIn se expresa en segundos.
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}