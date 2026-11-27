
package cl.levelup.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Recibe y valida los datos enviados desde React.
public record ProductoRequest(

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150,
                message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Size(max = 1000,
                message = "La descripcion no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00",
                message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2,
                message = "El precio permite hasta 10 enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0,
                message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId,

        @Size(min = 3, max = 50,
                message = "El codigo debe tener entre 3 y 50 caracteres")
        String codigo,

        @Min(value = 0,
                message = "El stock critico no puede ser negativo")
        Integer stockCritico,

        @Size(max = 500,
                message = "La imagen no puede superar 500 caracteres")
        String imagen,

        // Precio anterior opcional para las ofertas.
        @DecimalMin(value = "0.00",
                message = "El precio anterior no puede ser negativo")
        @Digits(integer = 10, fraction = 2)
        BigDecimal precioAnterior

) {

    // Mantiene compatibilidad con las pruebas anteriores.
    public ProductoRequest(
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Long categoriaId
    ) {
        this(
                nombre,
                descripcion,
                precio,
                stock,
                categoriaId,
                null,
                null,
                null,
                null
        );
    }

    // Constructor sin precio anterior.
    public ProductoRequest(
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Long categoriaId,
            String codigo,
            Integer stockCritico,
            String imagen
    ) {
        this(
                nombre,
                descripcion,
                precio,
                stock,
                categoriaId,
                codigo,
                stockCritico,
                imagen,
                null
        );
    }
}
