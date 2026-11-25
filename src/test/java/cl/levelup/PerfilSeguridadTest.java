package cl.levelup;

import java.time.LocalDate;

import cl.levelup.config.JwtService;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import cl.levelup.usuario.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class PerfilSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    // Crea una cuenta ficticia que se elimina al terminar la prueba.
    private Usuario crearUsuario(
            String run,
            String correo,
            String nombre
    ) {
        return usuarioService.registrar(
                new RegistroRequest(
                        run,
                        nombre,
                        "Prueba",
                        correo,
                        LocalDate.of(2000, 1, 1),
                        "Metropolitana",
                        "Maipu",
                        "Direccion original",
                        "clave123"
                )
        );
    }

    // Genera un JWT real para el usuario de prueba.
    private String tokenPara(Usuario usuario) {
        return "Bearer " + jwtService.generarToken(usuario);
    }

    // No permite consultar perfiles sin autenticacion.
    @Test
    void consultarPerfilSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    // No permite modificar perfiles sin autenticacion.
    @Test
    void actualizarPerfilSinTokenDevuelve401() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Prueba",
                                  "apellidos": "Seguridad",
                                  "direccion": "Direccion nueva"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // Un JWT valido permite consultar solamente el perfil propio.
    @Test
    void consultarPerfilConTokenDevuelve200() throws Exception {
        Usuario usuario = crearUsuario(
                "123456785",
                "perfil.seguridad@gmail.com",
                "Mario"
        );

        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header(
                                "Authorization",
                                tokenPara(usuario)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run")
                        .value("123456785"))
                .andExpect(jsonPath("$.nombre")
                        .value("Mario"))
                .andExpect(jsonPath("$.rol")
                        .value("Cliente"))
                .andExpect(jsonPath("$.passwordHash")
                        .doesNotExist());
    }

    // Rechaza campos invalidos sin actualizar la cuenta.
    @Test
    void actualizarConNombreVacioDevuelve400() throws Exception {
        Usuario usuario = crearUsuario(
                "123456785",
                "perfil.validacion@gmail.com",
                "Mario"
        );

        mockMvc.perform(put("/api/v1/usuarios/me")
                        .header(
                                "Authorization",
                                tokenPara(usuario)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "apellidos": "Prueba",
                                  "direccion": "Direccion nueva"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresCampo.nombre")
                        .exists());

        Usuario guardado = usuarioRepository
                .findById("123456785")
                .orElseThrow();

        assertThat(guardado.getNombre())
                .isEqualTo("Mario");
    }

    // El RUN enviado en JSON nunca reemplaza la identidad del JWT.
    @Test
    void noPermiteModificarLaCuentaDeOtroUsuario()
            throws Exception {

        Usuario primero = crearUsuario(
                "123456785",
                "perfil.primero@gmail.com",
                "Mario"
        );

        crearUsuario(
                "876543214",
                "perfil.segundo@gmail.com",
                "Segundo"
        );

        mockMvc.perform(put("/api/v1/usuarios/me")
                        .header(
                                "Authorization",
                                tokenPara(primero)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "run": "876543214",
                                  "nombre": "Mario Actualizado",
                                  "apellidos": "Prueba",
                                  "direccion": "Direccion cambiada"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run")
                        .value("123456785"))
                .andExpect(jsonPath("$.nombre")
                        .value("Mario Actualizado"));

        usuarioRepository.flush();

        Usuario cuentaPropia = usuarioRepository
                .findById("123456785")
                .orElseThrow();

        Usuario cuentaAjena = usuarioRepository
                .findById("876543214")
                .orElseThrow();

        assertThat(cuentaPropia.getDireccion())
                .isEqualTo("Direccion cambiada");

        assertThat(cuentaAjena.getNombre())
                .isEqualTo("Segundo");

        assertThat(cuentaAjena.getDireccion())
                .isEqualTo("Direccion original");
    }
}
