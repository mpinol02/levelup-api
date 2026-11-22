package cl.levelup.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos que React envía cuando el usuario intenta iniciar sesión.
public record LoginRequest(
        @NotBlank @Email @Size(max = 100)
        String correo,

        @NotBlank
        String password
) {}