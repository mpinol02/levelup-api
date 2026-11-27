
package cl.levelup.carrito;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Permite consultar los productos del carrito de cada usuario.
@Repository
public interface CarritoRepository
        extends JpaRepository<CarritoItem, Long> {

    // Obtiene todos los productos del usuario.
    List<CarritoItem> findByUsuario_RunOrderByIdAsc(
            String run
    );

    // Busca un producto dentro del carrito del usuario.
    Optional<CarritoItem> findByUsuario_RunAndProducto_Id(
            String run,
            Long productoId
    );

    // Elimina un producto del carrito personal.
    void deleteByUsuario_RunAndProducto_Id(
            String run,
            Long productoId
    );

    // Elimina todos los productos del carrito personal.
    void deleteByUsuario_Run(String run);
}
