package cl.levelup.categoria;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Lista todas las categorías ordenadas por nombre.
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {

        return categoriaRepository.findAll(
                Sort.by(Sort.Direction.ASC, "nombre")
        ).stream()
                .map(CategoriaResponse::desdeEntidad)
                .toList();
    }

    // Busca una categoría por su identificador.
    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {

        Categoria categoria = obtenerCategoria(id);

        return CategoriaResponse.desdeEntidad(categoria);
    }

    // Registra una categoría nueva.
    @Transactional
    public CategoriaResponse crear(CategoriaRequest datos) {

        String nombre = validarNombre(datos.nombre());

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una categoria con ese nombre"
            );
        }

        Categoria categoria = new Categoria(
                nombre,
                limpiarDescripcion(datos.descripcion())
        );

        Categoria guardada = categoriaRepository.save(categoria);

        return CategoriaResponse.desdeEntidad(guardada);
    }

    // Actualiza una categoría existente.
    @Transactional
    public CategoriaResponse actualizar(
            Long id,
            CategoriaRequest datos) {

        Categoria categoria = obtenerCategoria(id);

        String nombre = validarNombre(datos.nombre());

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ya existe una categoria con ese nombre"
                        );
                    }
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(
                limpiarDescripcion(datos.descripcion())
        );

        Categoria actualizada = categoriaRepository.save(categoria);

        return CategoriaResponse.desdeEntidad(actualizada);
    }

    // Elimina una categoría por su ID.
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = obtenerCategoria(id);

        categoriaRepository.delete(categoria);
    }

    // Obtiene una categoría o devuelve HTTP 404.
    private Categoria obtenerCategoria(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));
    }

    // Evita nombres vacíos o con espacios innecesarios.
    private String validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la categoria es obligatorio"
            );
        }

        return nombre.trim();
    }

    private String limpiarDescripcion(String descripcion) {

        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        return descripcion.trim();
    }
}
