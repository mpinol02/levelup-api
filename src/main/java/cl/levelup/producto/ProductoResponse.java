package cl.levelup.producto;

import java.math.BigDecimal;

// Define los datos que la API devuelve al consultar un producto.
public record ProductoResponse(

        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        Long categoriaId,
        String categoriaNombre

) {

    // Convierte una entidad Producto en una respuesta JSON.
    public static ProductoResponse desdeEntidad(Producto producto) {

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}
