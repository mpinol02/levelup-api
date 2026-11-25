package cl.levelup;

import java.time.LocalDate;
import java.util.List;

import cl.levelup.config.JwtService;
import cl.levelup.contacto.Contacto;
import cl.levelup.contacto.ContactoRepository;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
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
class ReporteTest {

    private static final String RUTA =
            "/api/v1/admin/reportes/usuarios";

    private static final String RUTA_CONTACTOS =
            "/api/v1/admin/reportes/contactos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContactoRepository contactoRepository;

    // Crea usuarios de prueba con correos Gmail validos.
    private Usuario crearUsuario(
            String run,
            String correo,
            Rol rol
    ) {

        RegistroRequest datos = new RegistroRequest(
                run,
                "Usuario",
                "Prueba",
                correo,
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Direccion de prueba",
                "clave123"
        );

        Usuario usuario = usuarioService.registrar(datos);

        usuario.setRol(rol);

        return usuarioRepository.saveAndFlush(usuario);
    }

    // Crea una consulta de prueba con el estado solicitado.
    private Contacto crearContacto(boolean atendido) {

        Contacto contacto = new Contacto(
                "Cliente Prueba",
                "contacto.reporte@gmail.com",
                "Consulta para pruebas de reportes."
        );

        contacto.setAtendido(atendido);

        return contactoRepository.saveAndFlush(contacto);
    }

    // Genera un JWT real de Administrador.
    private String tokenAdministrador() {

        Usuario admin = crearUsuario(
                "333333333",
                "admin.contactos.reporte@gmail.com",
                Rol.Administrador
        );

        return jwtService.generarToken(admin);
    }

    // ==========================================
    // PRUEBAS DE REPORTES DE USUARIOS
    // ==========================================

    // PRUEBA 1: Sin JWT debe responder 401.
    @Test
    void sinTokenDevuelve401() throws Exception {

        mockMvc.perform(get(RUTA))
                .andExpect(status().isUnauthorized());
    }

    // PRUEBA 2: Un Cliente no puede consultar reportes.
    @Test
    void clienteNoPuedeConsultarReporte() throws Exception {

        Usuario cliente = crearUsuario(
                "222222222",
                "cliente.reporte@gmail.com",
                Rol.Cliente
        );

        String token = jwtService.generarToken(cliente);

        mockMvc.perform(get(RUTA)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isForbidden());
    }

    // PRUEBA 3: Un Administrador puede consultar reportes.
    @Test
    void administradorPuedeConsultarReporte() throws Exception {

        Usuario admin = crearUsuario(
                "333333333",
                "admin.reporte@gmail.com",
                Rol.Administrador
        );

        String token = jwtService.generarToken(admin);

        mockMvc.perform(get(RUTA)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").exists())
                .andExpect(jsonPath("$.totalClientes").exists())
                .andExpect(jsonPath("$.totalAdministradores").exists())
                .andExpect(jsonPath("$.totalVendedores").exists());
    }

    // PRUEBA 4: Comprueba los totales del reporte.
    @Test
    void reporteCalculaCorrectamenteLosTotales() throws Exception {

        List<Usuario> existentes = usuarioRepository.findAll();

        long totalInicial = existentes.size();

        long clientesIniciales = existentes.stream()
                .filter(u -> u.getRol() == Rol.Cliente)
                .count();

        long adminsIniciales = existentes.stream()
                .filter(u -> u.getRol() == Rol.Administrador)
                .count();

        long vendedoresIniciales = existentes.stream()
                .filter(u -> u.getRol() == Rol.Vendedor)
                .count();

        Usuario admin = crearUsuario(
                "444444444",
                "admin.totales@gmail.com",
                Rol.Administrador
        );

        crearUsuario(
                "555555555",
                "cliente.totales@gmail.com",
                Rol.Cliente
        );

        crearUsuario(
                "666666666",
                "vendedor.totales@gmail.com",
                Rol.Vendedor
        );

        String token = jwtService.generarToken(admin);

        mockMvc.perform(get(RUTA)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios")
                        .value(totalInicial + 3))
                .andExpect(jsonPath("$.totalClientes")
                        .value(clientesIniciales + 1))
                .andExpect(jsonPath("$.totalAdministradores")
                        .value(adminsIniciales + 1))
                .andExpect(jsonPath("$.totalVendedores")
                        .value(vendedoresIniciales + 1));
    }

    // ==========================================
    // PRUEBAS DE REPORTES DE CONTACTO
    // ==========================================

    // PRUEBA 5: Sin JWT no permite consultar contactos.
    @Test
    void reporteContactosSinTokenDevuelve401() throws Exception {

        mockMvc.perform(get(RUTA_CONTACTOS))
                .andExpect(status().isUnauthorized());
    }

    // PRUEBA 6: Un Cliente no puede consultar el reporte.
    @Test
    void clienteNoPuedeConsultarReporteContactos()
            throws Exception {

        Usuario cliente = crearUsuario(
                "222222222",
                "cliente.contactos.reporte@gmail.com",
                Rol.Cliente
        );

        String token = jwtService.generarToken(cliente);

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isForbidden());
    }

    // PRUEBA 7: Un Vendedor tampoco puede consultar el reporte.
    @Test
    void vendedorNoPuedeConsultarReporteContactos()
            throws Exception {

        Usuario vendedor = crearUsuario(
                "666666666",
                "vendedor.contactos.reporte@gmail.com",
                Rol.Vendedor
        );

        String token = jwtService.generarToken(vendedor);

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isForbidden());
    }

    // PRUEBA 8: Un Administrador puede consultar el reporte.
    @Test
    void administradorPuedeConsultarReporteContactos()
            throws Exception {

        String token = tokenAdministrador();

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContactos").exists())
                .andExpect(jsonPath("$.totalPendientes").exists())
                .andExpect(jsonPath("$.totalAtendidos").exists());
    }

    // PRUEBA 9: Calcula correctamente las consultas registradas.
    @Test
    void reporteCalculaCorrectamenteLosContactos()
            throws Exception {

        long totalInicial = contactoRepository.count();

        long pendientesIniciales =
                contactoRepository.countByAtendidoFalse();

        long atendidosIniciales =
                totalInicial - pendientesIniciales;

        String token = tokenAdministrador();

        // Registra dos consultas pendientes y una atendida.
        crearContacto(false);
        crearContacto(false);
        crearContacto(true);

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContactos")
                        .value(totalInicial + 3))
                .andExpect(jsonPath("$.totalPendientes")
                        .value(pendientesIniciales + 2))
                .andExpect(jsonPath("$.totalAtendidos")
                        .value(atendidosIniciales + 1));
    }

    // PRUEBA 10: Actualiza los indicadores al cambiar el estado.
    @Test
    void reporteSeActualizaAlMarcarContactoAtendido()
            throws Exception {

        String token = tokenAdministrador();

        Contacto contacto = crearContacto(false);

        long totalInicial = contactoRepository.count();

        long pendientesIniciales =
                contactoRepository.countByAtendidoFalse();

        long atendidosIniciales =
                totalInicial - pendientesIniciales;

        // Cambia la consulta a estado atendido.
        contacto.setAtendido(true);

        contactoRepository.saveAndFlush(contacto);

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContactos")
                        .value(totalInicial))
                .andExpect(jsonPath("$.totalPendientes")
                        .value(pendientesIniciales - 1))
                .andExpect(jsonPath("$.totalAtendidos")
                        .value(atendidosIniciales + 1));
    }

    // PRUEBA 11: Actualiza los indicadores al eliminar una consulta.
    @Test
    void reporteSeActualizaAlEliminarContacto()
            throws Exception {

        String token = tokenAdministrador();

        Contacto contacto = crearContacto(true);

        long totalInicial = contactoRepository.count();

        long pendientesIniciales =
                contactoRepository.countByAtendidoFalse();

        long atendidosIniciales =
                totalInicial - pendientesIniciales;

        // Elimina la consulta de prueba.
        contactoRepository.delete(contacto);
        contactoRepository.flush();

        mockMvc.perform(get(RUTA_CONTACTOS)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContactos")
                        .value(totalInicial - 1))
                .andExpect(jsonPath("$.totalPendientes")
                        .value(pendientesIniciales))
                .andExpect(jsonPath("$.totalAtendidos")
                        .value(atendidosIniciales - 1));
    }
}
