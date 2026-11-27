
package cl.levelup.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Permite consultar y administrar productos en la base de datos.
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Busca un producto por nombre.
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    // Comprueba si existe un producto con ese nombre.
    boolean existsByNombreIgnoreCase(String nombre);

    // Busca un producto por su codigo comercial.
    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    // Comprueba si el codigo ya esta registrado.
    boolean existsByCodigoIgnoreCase(String codigo);

    // Obtiene los productos de una categoria.
    List<Producto> findByCategoriaId(Long categoriaId);

    // Obtiene los productos ordenados alfabeticamente.
    List<Producto> findAllByOrderByNombreAsc();

    // Obtiene los productos con stock disponible.
    List<Producto> findByStockGreaterThan(Integer stock);
}
