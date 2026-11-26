package cl.levelup.compra;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// Datos que la API devuelve a React para mostrar el resultado de la compra.
public record CompraResponse(
        Long id,
        Instant fecha,
        Cliente cliente,
        String entrega,
        String estado,
        List<Item> productos,
        BigDecimal subtotal,
        int porcentajeDescuento,
        BigDecimal descuento,
        BigDecimal costoEnvio,
        BigDecimal total
) {

    // Datos del cliente que aparecen en el comprobante.
    public record Cliente(
            String run,
            String nombre,
            String correo,
            String direccion
    ) {}

    // Precio y cantidad de cada producto al momento de la compra.
    public record Item(
            Long id,
            String nombre,
            BigDecimal precio,
            int cantidad,
            BigDecimal subtotal
    ) {}
}