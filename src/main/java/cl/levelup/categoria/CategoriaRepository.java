package cl.levelup.categoria;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    // Busca una categoría sin distinguir mayúsculas.
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Comprueba si existe una categoría con ese nombre.
    boolean existsByNombreIgnoreCase(String nombre);
}
