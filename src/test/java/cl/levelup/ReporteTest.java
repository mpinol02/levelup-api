package cl.levelup;

import java.time.LocalDate;
import java.util.List;

import cl.levelup.config.JwtService;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Crea usuarios de prueba con correos Gmail válidos.
    private Usuario crearUsuario(
            String run,
            String correo,
            Rol rol) {

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
                        .header("Authorization", "Bearer " + token))
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
                        .header("Authorization", "Bearer " + token))
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
                        .header("Authorization", "Bearer " + token))
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
}
