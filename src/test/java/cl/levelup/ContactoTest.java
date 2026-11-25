package cl.levelup;

import java.time.LocalDate;

import cl.levelup.config.JwtService;
import cl.levelup.contacto.Contacto;
import cl.levelup.contacto.ContactoRepository;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
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
class ContactoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    // Crea un usuario ficticio para comprobar los permisos.
    private Usuario crearUsuario(Rol rol) {
        Usuario usuario = usuarioService.registrar(
                new RegistroRequest(
                        "123456785",
                        "Mario",
                        "Prueba",
                        "contacto.test@gmail.com",
                        LocalDate.of(2000, 1, 1),
                        "Metropolitana",
                        "Maipu",
                        "Direccion de prueba",
                        "clave123"
                )
        );

        usuario.setRol(rol);

        return usuario;
    }

    // Genera un JWT real con el rol solicitado.
    private String tokenPara(Rol rol) {
        Usuario usuario = crearUsuario(rol);

        return "Bearer " + jwtService.generarToken(usuario);
    }

    // Registra una consulta directamente en la base de datos.
    private Contacto crearContacto() {
        return contactoRepository.saveAndFlush(
                new Contacto(
                        "Cliente Prueba",
                        "cliente.prueba@gmail.com",
                        "Consulta sobre disponibilidad de productos."
                )
        );
    }

    // 1. Permite enviar consultas publicas sin JWT.
    @Test
    void enviarContactoSinTokenDevuelve201() throws Exception {

        long cantidadAnterior = contactoRepository.count();

        mockMvc.perform(post("/api/v1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Cliente Prueba",
                                  "correo": "cliente.prueba@gmail.com",
                                  "comentario": "Consulta sobre productos."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.mensaje").exists());

        assertThat(contactoRepository.count())
                .isEqualTo(cantidadAnterior + 1);

        assertThat(contactoRepository.findAll())
                .anyMatch(contacto ->
                        contacto.getNombre().equals("Cliente Prueba")
                        && contacto.getComentario()
                                .equals("Consulta sobre productos.")
                        && !contacto.isAtendido()
                );
    }

    // 2. Permite registrar consultas sin correo electronico.
    @Test
    void enviarContactoSinCorreoDevuelve201() throws Exception {

        mockMvc.perform(post("/api/v1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Cliente Sin Correo",
                                  "correo": "",
                                  "comentario": "Consulta sin correo electronico."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        assertThat(contactoRepository.findAll())
                .anyMatch(contacto ->
                        contacto.getNombre()
                                .equals("Cliente Sin Correo")
                        && contacto.getCorreo() == null
                );
    }

    // 3. Rechaza consultas con campos invalidos.
    @Test
    void contactoInvalidoDevuelve400() throws Exception {

        long cantidadAnterior = contactoRepository.count();

        mockMvc.perform(post("/api/v1/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "correo": "correo-invalido",
                                  "comentario": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(contactoRepository.count())
                .isEqualTo(cantidadAnterior);
    }

    // 4. No permite consultar mensajes sin autenticacion.
    @Test
    void listarContactosSinTokenDevuelve401() throws Exception {

        mockMvc.perform(get("/api/v1/admin/contactos"))
                .andExpect(status().isUnauthorized());
    }

    // 5. Un Cliente no puede consultar mensajes administrativos.
    @Test
    void clienteNoPuedeListarContactos() throws Exception {

        mockMvc.perform(get("/api/v1/admin/contactos")
                        .header(
                                "Authorization",
                                tokenPara(Rol.Cliente)
                        ))
                .andExpect(status().isForbidden());
    }

    // 6. El Administrador puede consultar los mensajes registrados.
    @Test
    void administradorPuedeListarContactos() throws Exception {

        crearContacto();

        mockMvc.perform(get("/api/v1/admin/contactos")
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.correo == 'cliente.prueba@gmail.com')]"
                ).isNotEmpty());
    }

    // 7. El Administrador puede consultar una solicitud por ID.
    @Test
    void administradorPuedeConsultarContacto() throws Exception {

        Contacto contacto = crearContacto();

        mockMvc.perform(get(
                        "/api/v1/admin/contactos/{id}",
                        contacto.getId()
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(contacto.getId()))
                .andExpect(jsonPath("$.atendido")
                        .value(false));
    }

    // 8. El Administrador puede marcar una consulta como atendida.
    @Test
    void administradorPuedeMarcarAtendido() throws Exception {

        Contacto contacto = crearContacto();

        mockMvc.perform(patch(
                        "/api/v1/admin/contactos/{id}/estado",
                        contacto.getId()
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atendido": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendido")
                        .value(true));

        contactoRepository.flush();

        Contacto guardado = contactoRepository
                .findById(contacto.getId())
                .orElseThrow();

        assertThat(guardado.isAtendido()).isTrue();
    }

    // 9. El Administrador puede devolver una consulta a pendiente.
    @Test
    void administradorPuedeMarcarPendiente() throws Exception {

        Contacto contacto = crearContacto();

        contacto.setAtendido(true);
        contactoRepository.saveAndFlush(contacto);

        mockMvc.perform(patch(
                        "/api/v1/admin/contactos/{id}/estado",
                        contacto.getId()
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atendido": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendido")
                        .value(false));

        contactoRepository.flush();

        assertThat(contactoRepository
                .findById(contacto.getId())
                .orElseThrow()
                .isAtendido()).isFalse();
    }

    // 10. El Administrador puede eliminar una consulta.
    @Test
    void administradorPuedeEliminarContacto() throws Exception {

        Contacto contacto = crearContacto();
        Long id = contacto.getId();

        mockMvc.perform(delete(
                        "/api/v1/admin/contactos/{id}",
                        id
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        ))
                .andExpect(status().isNoContent());

        contactoRepository.flush();

        assertThat(contactoRepository.existsById(id))
                .isFalse();
    }

    // 11. Un Cliente no puede modificar el estado de una consulta.
    @Test
    void clienteNoPuedeActualizarContacto() throws Exception {

        Contacto contacto = crearContacto();

        mockMvc.perform(patch(
                        "/api/v1/admin/contactos/{id}/estado",
                        contacto.getId()
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Cliente)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atendido": true
                                }
                                """))
                .andExpect(status().isForbidden());

        assertThat(contactoRepository
                .findById(contacto.getId())
                .orElseThrow()
                .isAtendido()).isFalse();
    }

    // 12. Un Cliente no puede eliminar consultas.
    @Test
    void clienteNoPuedeEliminarContacto() throws Exception {

        Contacto contacto = crearContacto();

        mockMvc.perform(delete(
                        "/api/v1/admin/contactos/{id}",
                        contacto.getId()
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Cliente)
                        ))
                .andExpect(status().isForbidden());

        assertThat(contactoRepository
                .existsById(contacto.getId())).isTrue();
    }

    // 13. Consultar un mensaje inexistente devuelve 404.
    @Test
    void contactoInexistenteDevuelve404() throws Exception {

        mockMvc.perform(get(
                        "/api/v1/admin/contactos/{id}",
                        Long.MAX_VALUE
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        ))
                .andExpect(status().isNotFound());
    }

    // 14. Comprueba el contador de consultas pendientes.
    @Test
    void administradorPuedeContarPendientes() throws Exception {

        long pendientesAntes =
                contactoRepository.countByAtendidoFalse();

        crearContacto();

        mockMvc.perform(get(
                        "/api/v1/admin/contactos/pendientes"
                )
                        .header(
                                "Authorization",
                                tokenPara(Rol.Administrador)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendientes")
                        .value(pendientesAntes + 1));
    }
}
