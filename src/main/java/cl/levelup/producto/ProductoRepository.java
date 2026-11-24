package cl.levelup.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Permite consultar y administrar los productos en la base de datos.
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Busca un producto por su nombre sin distinguir mayusculas.
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    // Comprueba si ya existe un producto con ese nombre.
    boolean existsByNombreIgnoreCase(String nombre);

    // Obtiene todos los productos de una categoria.
    List<Producto> findByCategoriaId(Long categoriaId);

    // Obtiene los productos ordenados alfabeticamente.
    List<Producto> findAllByOrderByNombreAsc();

    // Obtiene los productos que tienen stock disponible.
    List<Producto> findByStockGreaterThan(Integer stock);
}
