package cl.levelup.usuario;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// El RUN es la clave primaria de Usuario y es de tipo String.
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.run = :run")
    Optional<Usuario> bloquearPorRun(@Param("run") String run);

    // Busca la cuenta para el login, sin distinguir mayúsculas y minúsculas.
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Permite comprobar durante el registro si el correo ya está en uso.
    boolean existsByCorreoIgnoreCase(String correo);
}