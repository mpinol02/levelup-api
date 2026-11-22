
package cl.levelup;

import java.time.LocalDate;

import cl.levelup.config.JwtService;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class PermisosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private JwtService jwtService;

    // Crea un usuario de prueba mediante el registro existente.
    private Usuario crearUsuario() {
        return usuarios.registrar(new RegistroRequest(
                "123456785",
                "Mario",
                "Prueba",
                "mario.permisos@gmail.com",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Direccion de prueba",
                "clave123"
        ));
    }

    // ==========================================
    // PRUEBAS DEL ESTADO DEL ADMINISTRADOR
    // ==========================================

    @Test
    void sinTokenDevuelve401() throws Exception {

        mockMvc.perform(get("/api/v1/admin/estado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeAccederAlAdministrador() throws Exception {

        Usuario cliente = crearUsuario();

        String token = jwtService.generarToken(cliente);

        mockMvc.perform(get("/api/v1/admin/estado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeAcceder() throws Exception {

        Usuario admin = crearUsuario();
        admin.setRol(Rol.Administrador);

        String token = jwtService.generarToken(admin);

        mockMvc.perform(get("/api/v1/admin/estado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.panel").value("administracion"))
                .andExpect(jsonPath("$.estado").value("OK"));
    }

    // ==========================================
    // PRUEBAS DE CONSULTA DE USUARIOS
    // ==========================================

    @Test
    void listarUsuariosSinTokenDevuelve401() throws Exception {

        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {

        Usuario cliente = crearUsuario();

        String token = jwtService.generarToken(cliente);

        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeListarUsuarios() throws Exception {

        Usuario admin = crearUsuario();
        admin.setRol(Rol.Administrador);

        String token = jwtService.generarToken(admin);

        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())

                // La respuesta debe ser una lista.
                .andExpect(jsonPath("$").isArray())

                // Comprueba que aparece el usuario registrado.
                .andExpect(jsonPath(
                        "$[?(@.correo == 'mario.permisos@gmail.com')]"
                ).isNotEmpty())

                // No deben exponerse campos sensibles.
                .andExpect(jsonPath("$[*].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[*].direccion").doesNotExist())
                .andExpect(jsonPath("$[*].fechaNacimiento").doesNotExist());
    }
}
