
package cl.levelup.carrito;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Recibe los datos necesarios para agregar un producto.
public record CarritoRequest(

        @NotNull(message = "El producto es obligatorio.")
        @Positive(message = "El ID del producto debe ser positivo.")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer cantidad

) {
}
