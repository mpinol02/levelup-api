package cl.levelup.compra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// Permite guardar y consultar las ordenes de compra.
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    // Busca las compras de un usuario segun su estado.
    // Ordena primero las compras mas recientes.
    List<Orden> findByClienteRunAndEstadoOrderByFechaDescIdDesc(
            String clienteRun,
            String estado
    );
}
