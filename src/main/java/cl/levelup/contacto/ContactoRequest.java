package cl.levelup.contacto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactoRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(
                max = 100,
                message = "El nombre no puede superar 100 caracteres."
        )
        String nombre,

        @Email(message = "Ingresa un correo valido.")
        @Size(
                max = 100,
                message = "El correo no puede superar 100 caracteres."
        )
        String correo,

        @NotBlank(message = "El comentario es obligatorio.")
        @Size(
                max = 500,
                message = "El comentario no puede superar 500 caracteres."
        )
        String comentario

) {
}
