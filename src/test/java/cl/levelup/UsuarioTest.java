package cl.levelup;

import java.time.LocalDate;

import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import cl.levelup.usuario.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Transactional // Cada prueba deshace sus cambios al terminar.
class UsuarioTest {

    @Autowired
    private UsuarioService servicio;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Prepara los mismos datos para ambos casos; solo cambia el nacimiento.
    private RegistroRequest datosDeRegistro(LocalDate nacimiento) {
        return new RegistroRequest(
                "123456785",
                "Mario",
                "Prueba",
                "MARIO@GMAIL.COM",
                nacimiento,
                "Región Metropolitana",
                "Maipú",
                "Calle de prueba 123",
                "clave123"
        );
    }

    @Test
    void registraComoClienteAQuienCumple18HoyYGuardaElHash() {
        RegistroRequest datos = datosDeRegistro(LocalDate.now().minusYears(18));

        servicio.registrar(datos);
        usuarios.flush(); // Ejecuta la escritura en H2 dentro de la prueba.

        Usuario guardado = usuarios.findByCorreoIgnoreCase("mario@gmail.com")
                .orElseThrow();

        assertThat(guardado.getRol()).isEqualTo(Rol.Cliente);
        assertThat(guardado.getCorreo()).isEqualTo("mario@gmail.com");
        assertThat(guardado.getPasswordHash()).isNotEqualTo(datos.password());

        // La clave original coincide con el hash, igual que en un futuro login.
        assertThat(passwordEncoder.matches(datos.password(), guardado.getPasswordHash()))
                .isTrue();
    }

    @Test
    void rechazaAQuienCumple18MananaSinGuardarLaCuenta() {
        LocalDate nacimiento = LocalDate.now().minusYears(18).plusDays(1);
        RegistroRequest datos = datosDeRegistro(nacimiento);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.registrar(datos)
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(usuarios.existsById(datos.run())).isFalse();
    }
}