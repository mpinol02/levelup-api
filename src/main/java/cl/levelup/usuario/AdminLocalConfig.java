package cl.levelup.usuario;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local")
public class AdminLocalConfig {

    // Prepara el administrador de demostración al iniciar la API en modo local.
    @Bean
    CommandLineRunner prepararAdminLocal(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            @Value("${LEVELUP_ADMIN_LOCAL_PASSWORD:}") String password) {

        return args -> {
            // Sin contraseña configurada, la API arranca sin crear la cuenta.
            if (password.isBlank()) {
                return;
            }

            String run = "111111111";
            String correo = "admin.levelup@gmail.com";

            // Si el RUN o el correo ya existen, no modifica la cuenta existente.
            if (usuarios.existsById(run) || usuarios.existsByCorreoIgnoreCase(correo)) {
                return;
            }

            Usuario admin = new Usuario();
            admin.setRun(run);
            admin.setNombre("Administrador");
            admin.setApellidos("Prueba");
            admin.setCorreo(correo);
            admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            admin.setRegion("Metropolitana");
            admin.setComuna("Maipu");
            admin.setDireccion("Direccion de prueba");
            admin.setRol(Rol.Administrador);

            // Guarda la contraseña con el mismo codificador que utiliza el login.
            admin.setPasswordHash(encoder.encode(password));
            usuarios.save(admin);
        };
    }
}