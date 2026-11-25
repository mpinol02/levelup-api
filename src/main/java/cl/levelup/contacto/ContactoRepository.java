package cl.levelup.contacto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactoRepository
        extends JpaRepository<Contacto, Long> {

    // Lista los mensajes desde el mas reciente.
    List<Contacto> findAllByOrderByFechaEnvioDesc();

    // Cuenta las consultas pendientes.
    long countByAtendidoFalse();
}
