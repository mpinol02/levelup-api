package cl.levelup.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Recibe y valida los datos para crear o actualizar productos.
public record ProductoRequest(

        // El nombre es obligatorio y permite hasta 150 caracteres.
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        // La descripcion es opcional y permite hasta 1000 caracteres.
        @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
        String descripcion,

        // El precio es obligatorio y no puede ser negativo.
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2,
                message = "El precio permite hasta 10 enteros y 2 decimales")
        BigDecimal precio,

        // El stock es obligatorio y debe ser cero o mayor.
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        // Cada producto debe estar asociado a una categoria existente.
        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId

) {
}
