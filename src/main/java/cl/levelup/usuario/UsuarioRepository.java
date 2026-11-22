package cl.levelup.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// El RUN es la clave primaria de Usuario y es de tipo String.
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    // Busca la cuenta para el login, sin distinguir mayúsculas y minúsculas.
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Permite comprobar durante el registro si el correo ya está en uso.
    boolean existsByCorreoIgnoreCase(String correo);
}