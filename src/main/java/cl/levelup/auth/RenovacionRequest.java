package cl.levelup.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Recibe el token utilizado para renovar una sesion.
public record RenovacionRequest(

        // Rechaza solicitudes sin token o con un valor vacio.
        @NotBlank(message = "El token de renovacion es obligatorio.")
        @Size(
                max = 512,
                message = "El token de renovacion es demasiado largo."
        )
        String refreshToken

) {}
