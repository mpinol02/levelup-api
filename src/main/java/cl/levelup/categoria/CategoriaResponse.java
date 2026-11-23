package cl.levelup.categoria;

// Información que devuelve la API al consultar categorías.
public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion
) {

    public static CategoriaResponse desdeEntidad(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
