package cl.levelup;

import java.time.LocalDate;

import cl.levelup.auth.AuthService;
import cl.levelup.auth.LoginRequest;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class AuthTest {

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private AuthService auth;

    // Registra una cuenta de prueba en H2 antes de cada caso.
    // Al terminar cada prueba, se deshacen sus cambios.
    @BeforeEach
    void prepararCuenta() {
        usuarios.registrar(new RegistroRequest(
                "123456785",
                "Mario",
                "Prueba",
                "mario.prueba@gmail.com",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Calle de prueba 123",
                "clave123"
        ));
    }

    @Test
    void autenticaConLaPasswordCorrecta() {
        LoginRequest datos = new LoginRequest(
                "mario.prueba@gmail.com", "clave123"
        );

        Usuario autenticado = auth.autenticar(datos);

        // Debe devolver la cuenta que registramos.
        assertThat(autenticado.getRun()).isEqualTo("123456785");
    }

    @Test
    void rechazaLaPasswordIncorrecta() {
        LoginRequest datos = new LoginRequest(
                "mario.prueba@gmail.com", "otraClave"
        );

        // El correo existe, pero la contraseña no coincide.
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> auth.autenticar(datos)
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.getReason()).isEqualTo("Correo o contraseña incorrectos.");
    }
}