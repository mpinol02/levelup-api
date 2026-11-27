
package cl.levelup.producto;

import java.math.BigDecimal;

// Define los datos que devuelve la API a React.
public record ProductoResponse(

        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        Long categoriaId,
        String categoriaNombre,
        String codigo,
        Integer stockCritico,
        String imagen,
        BigDecimal precioAnterior

) {

    // Constructor compatible con las pruebas anteriores.
    public ProductoResponse(
            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Long categoriaId,
            String categoriaNombre
    ) {
        this(
                id,
                nombre,
                descripcion,
                precio,
                stock,
                categoriaId,
                categoriaNombre,
                null,
                null,
                null,
                null
        );
    }

    // Constructor sin precio anterior.
    public ProductoResponse(
            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Long categoriaId,
            String categoriaNombre,
            String codigo,
            Integer stockCritico,
            String imagen
    ) {
        this(
                id,
                nombre,
                descripcion,
                precio,
                stock,
                categoriaId,
                categoriaNombre,
                codigo,
                stockCritico,
                imagen,
                null
        );
    }

    // Convierte una entidad Producto en una respuesta JSON.
    public static ProductoResponse desdeEntidad(Producto producto) {

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getCodigo(),
                producto.getStockCritico(),
                producto.getImagen(),
                producto.getPrecioAnterior()
        );
    }
}
