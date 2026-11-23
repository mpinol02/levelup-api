package cl.levelup.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 100,
                message = "El nombre no puede superar 100 caracteres"
        )
        String nombre,

        @Size(
                max = 300,
                message = "La descripcion no puede superar 300 caracteres"
        )
        String descripcion

) {}
