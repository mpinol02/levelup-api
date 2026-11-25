package cl.levelup.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos que el usuario puede modificar en su propio perfil.
// RUN, correo, rol y contrasena no son editables aqui.
public record PerfilRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                max = 50,
                message = "El nombre no puede superar 50 caracteres."
        )
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(
                max = 100,
                message = "Los apellidos no pueden superar 100 caracteres."
        )
        String apellidos,

        @NotBlank(message = "La direccion es obligatoria.")
        @Size(
                max = 300,
                message = "La direccion no puede superar 300 caracteres."
        )
        String direccion
) {
}
