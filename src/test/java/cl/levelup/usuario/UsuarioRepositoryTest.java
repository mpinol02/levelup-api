package cl.levelup.usuario;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repositorio;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void guardaUsuarioYLoEncuentraPorCorreoSinDistinguirMayusculas() {
        // Prepara una cuenta ficticia con los campos obligatorios.
        Usuario usuario = new Usuario();
        usuario.setRun("123456785");
        usuario.setNombre("Mario");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("mario@example.com");
        usuario.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuario.setRegion("Región Metropolitana");
        usuario.setComuna("Maipú");
        usuario.setDireccion("Calle de prueba 123");
        usuario.setPasswordHash("hash-ficticio-solo-para-esta-prueba");

        // Escribe en H2 y limpia la memoria de JPA para volver a leer de la base.
        repositorio.saveAndFlush(usuario);
        entityManager.clear();

        Usuario encontrado = repositorio
                .findByCorreoIgnoreCase("MARIO@EXAMPLE.COM")
                .orElseThrow();

        // Debe recuperar la misma cuenta con el rol Cliente por defecto.
        assertThat(encontrado.getRun()).isEqualTo("123456785");
        assertThat(encontrado.getRol()).isEqualTo(Rol.Cliente);
    }
}