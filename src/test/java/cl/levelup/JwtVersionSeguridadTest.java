package cl.levelup;

import cl.levelup.admin.AdminUsuarioService;
import cl.levelup.auth.RefreshTokenService;
import cl.levelup.config.JwtService;
import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.Rol;
import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRequest;
import cl.levelup.usuario.UsuarioService;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class JwtVersionSeguridadTest {

    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @Autowired private UsuarioService usuarios;
    @Autowired private AdminUsuarioService admin;
    @Autowired private RefreshTokenService refresh;

    @Test
    void cambiarYRestaurarRolNoRecuperaLasCredencialesAnteriores() throws Exception {
        Usuario usuario = registrar();
        String acceso = jwt.generarToken(usuario);
        String renovacion = refresh.crear(usuario);
        esperarAcceso(acceso, 200);

        cambiarRol(usuario, Rol.Vendedor);
        esperarAcceso(acceso, 401);
        esperarRenovacionRechazada(renovacion);
        String accesoVendedor = jwt.generarToken(usuario);
        esperarAcceso(accesoVendedor, 200);

        cambiarRol(usuario, Rol.Cliente);
        esperarAcceso(acceso, 401);
        esperarAcceso(accesoVendedor, 401);
        esperarAcceso(jwt.generarToken(usuario), 200);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void borrarYRecrearRunNoRecuperaLaSesionAnterior() throws Exception {
        Usuario usuario = registrar();
        String acceso = jwt.generarToken(usuario);
        String renovacion = refresh.crear(usuario);

        admin.eliminar(usuario.getRun(), "admin");
        esperarAcceso(acceso, 401);
        esperarRenovacionRechazada(renovacion);

        Usuario nuevaCuenta = registrar();
        try {
            esperarAcceso(acceso, 401);
            esperarRenovacionRechazada(renovacion);
            esperarAcceso(jwt.generarToken(nuevaCuenta), 200);
        } finally {
            admin.eliminar(nuevaCuenta.getRun(), "admin");
        }
    }

    @Test
    void editarDatosSinCambiarRolConservaLaSesion() throws Exception {
        Usuario usuario = registrar();
        String acceso = jwt.generarToken(usuario);
        String renovacion = refresh.crear(usuario);

        cambiarRol(usuario, Rol.Cliente);
        esperarAcceso(acceso, 200);
        esperarAcceso(refresh.renovar(renovacion).accessToken(), 200);
    }

    private Usuario registrar() {
        return usuarios.registrar(new RegistroRequest(
                "123456785", "Prueba", "Seguridad", "version.seguridad@gmail.com",
                LocalDate.of(2000, 1, 1), "Metropolitana", "Maipu", "Calle de prueba", "clave123"
        ));
    }

    private void cambiarRol(Usuario usuario, Rol rol) {
        admin.actualizar(usuario.getRun(), "admin",
                new UsuarioRequest("Nombre editado", "Seguridad", "Calle de prueba", rol));
    }

    private void esperarAcceso(String token, int codigo) throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().is(codigo));
    }

    private void esperarRenovacionRechazada(String token) {
        assertThatThrownBy(() -> refresh.renovar(token))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> org.assertj.core.api.Assertions.assertThat(error.getStatusCode().value())
                                .isEqualTo(401));
    }
}
