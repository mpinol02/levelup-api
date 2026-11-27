
package cl.levelup.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Permite guardar y consultar las órdenes de compra.
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    // Consulta las compras de un usuario según su estado.
    List<Orden> findByClienteRunAndEstadoOrderByFechaDescIdDesc(
            String clienteRun,
            String estado
    );

    // Consulta todas las compras de un usuario.
    List<Orden> findByClienteRunOrderByFechaDescIdDesc(
            String clienteRun
    );

    // Busca una orden que pertenezca al usuario autenticado.
    Optional<Orden> findByIdAndClienteRun(
            Long id,
            String clienteRun
    );
}
