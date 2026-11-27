
package cl.levelup.usuario;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("prod")
public class AdminProdConfig {

    // Prepara el administrador inicial en producción.
    @Bean
    CommandLineRunner prepararAdminProd(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            @Value("${LEVELUP_ADMIN_PROD_PASSWORD:}") String password
    ) {

        return args -> {

            // No crea una cuenta si no se configura una contraseña.
            if (password.isBlank()) {
                return;
            }

            // Exige una contraseña suficientemente larga.
            if (password.length() < 12) {
                throw new IllegalArgumentException(
                        "La contraseña administrativa debe tener al menos 12 caracteres."
                );
            }

            String run = "111111111";
            String correo = "admin.levelup@gmail.com";

            // Evita duplicar o modificar cuentas existentes.
            if (usuarios.existsById(run)
                    || usuarios.existsByCorreoIgnoreCase(correo)) {

                return;
            }

            // Configura los datos del administrador inicial.
            Usuario admin = new Usuario();

            admin.setRun(run);
            admin.setNombre("Administrador");
            admin.setApellidos("Level-Up");
            admin.setCorreo(correo);
            admin.setFechaNacimiento(
                    LocalDate.of(2000, 1, 1)
            );

            admin.setRegion("Metropolitana");
            admin.setComuna("Maipu");
            admin.setDireccion("Administracion Level-Up");

            admin.setRol(Rol.Administrador);

            // Guarda únicamente el hash BCrypt de la contraseña.
            admin.setPasswordHash(
                    encoder.encode(password)
            );

            usuarios.save(admin);

            System.out.println(
                    "Administrador inicial de produccion creado correctamente."
            );
        };
    }
}
