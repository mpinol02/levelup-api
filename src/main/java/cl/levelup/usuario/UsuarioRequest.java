package cl.levelup.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Campos que puede modificar un administrador.
public record UsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "La direccion es obligatoria.")
        @Size(max = 300)
        String direccion,

        @NotNull(message = "El rol es obligatorio.")
        Rol rol

) {
}
