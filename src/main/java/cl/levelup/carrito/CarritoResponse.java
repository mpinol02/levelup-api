
package cl.levelup.carrito;

import java.math.BigDecimal;
import java.util.List;

// Define la informacion del carrito que recibe React.
public record CarritoResponse(

        List<Item> productos,

        long cantidadTotal,

        BigDecimal subtotal

) {

    // Representa cada producto del carrito.
    public record Item(

            Long productoId,

            String codigo,

            String nombre,

            BigDecimal precio,

            Integer cantidad,

            Integer stock,

            BigDecimal subtotal

    ) {
    }
}
