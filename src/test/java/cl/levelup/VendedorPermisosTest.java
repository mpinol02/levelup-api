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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class VendedorPermisosTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private JwtService jwtService;

    // Registra un vendedor y genera su token JWT.
    private String tokenVendedor() {

        Usuario vendedor = usuarios.registrar(new RegistroRequest(
                "123456785",
                "Vendedor",
                "Prueba",
                "vendedor.permisos@gmail.com",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Direccion de prueba",
                "clave123"
        ));

        vendedor.setRol(Rol.Vendedor);

        return jwtService.generarToken(vendedor);
    }

    // El vendedor puede consultar productos.
    @Test
    void vendedorPuedeConsultarProductos() throws Exception {
        mvc.perform(get("/api/v1/productos")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // El vendedor puede consultar el listado de ordenes.
    @Test
    void vendedorPuedeListarOrdenes() throws Exception {
        mvc.perform(get("/api/v1/admin/ordenes")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Puede consultar el detalle; una orden inexistente devuelve 404.
    @Test
    void vendedorPuedeSolicitarDetalleDeOrden() throws Exception {
        mvc.perform(get("/api/v1/admin/ordenes/999999999")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isNotFound());
    }

    // No puede crear productos.
    @Test
    void vendedorNoPuedeCrearProductos() throws Exception {
        mvc.perform(post("/api/v1/productos")
                .header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // No puede actualizar productos.
    @Test
    void vendedorNoPuedeActualizarProductos() throws Exception {
        mvc.perform(put("/api/v1/productos/1")
                .header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // No puede eliminar productos.
    @Test
    void vendedorNoPuedeEliminarProductos() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede modificar el stock.
    @Test
    void vendedorNoPuedeModificarStock() throws Exception {
        mvc.perform(patch("/api/v1/productos/1/stock")
                .header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    // No puede administrar usuarios.
    @Test
    void vendedorNoPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/v1/admin/usuarios")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede consultar reportes administrativos.
    @Test
    void vendedorNoPuedeConsultarReportes() throws Exception {
        mvc.perform(get("/api/v1/admin/reportes/usuarios")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede consultar mensajes de contacto.
    @Test
    void vendedorNoPuedeConsultarContactos() throws Exception {
        mvc.perform(get("/api/v1/admin/contactos")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede utilizar el carrito.
    @Test
    void vendedorNoPuedeAccederAlCarrito() throws Exception {
        mvc.perform(get("/api/v1/carrito")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede realizar compras.
    @Test
    void vendedorNoPuedeComprar() throws Exception {
        mvc.perform(post("/api/v1/compras")
                .header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // No puede consultar el historial administrativo por RUN.
    @Test
    void vendedorNoPuedeConsultarHistorialPorRun() throws Exception {
        mvc.perform(get("/api/v1/admin/ordenes/usuario/123456785")
                .header("Authorization", "Bearer " + tokenVendedor()))
                .andExpect(status().isForbidden());
    }

    // No puede administrar categorias.
    @Test
    void vendedorNoPuedeCrearCategorias() throws Exception {
        mvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + tokenVendedor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
