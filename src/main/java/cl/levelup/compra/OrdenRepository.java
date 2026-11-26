package cl.levelup.compra;

import org.springframework.data.jpa.repository.JpaRepository;

// Permite guardar órdenes y consultarlas por su número.
public interface OrdenRepository extends JpaRepository<Orden, Long> {
}