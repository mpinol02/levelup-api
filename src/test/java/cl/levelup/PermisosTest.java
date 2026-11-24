package cl.levelup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;
import cl.levelup.config.JwtService;
import cl.levelup.producto.ProductoRequest;
import cl.levelup.producto.ProductoResponse;
import cl.levelup.producto.ProductoService;
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
class PermisosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarios;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoService productoService;

    // Crea un usuario Cliente para comprobar los permisos.
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

    // Genera un token JWT con el rol Cliente.
    private String tokenCliente() {
        Usuario cliente = crearUsuario();
        return jwtService.generarToken(cliente);
    }

    // Genera un token JWT con el rol Administrador.
    private String tokenAdministrador() {
        Usuario admin = crearUsuario();
        admin.setRol(Rol.Administrador);
        return jwtService.generarToken(admin);
    }

    // Crea una categoria para las pruebas de productos.
    private Categoria crearCategoria() {
        return categoriaRepository.saveAndFlush(
                new Categoria(
                        "Categoria-" + UUID.randomUUID(),
                        "Categoria para pruebas de permisos"
                )
        );
    }

    // Crea un producto de prueba directamente desde el servicio.
    private ProductoResponse crearProducto() {
        Categoria categoria = crearCategoria();

        ProductoRequest request = new ProductoRequest(
                "Producto-" + UUID.randomUUID(),
                "Producto para pruebas de seguridad",
                new BigDecimal("45990.00"),
                10,
                categoria.getId()
        );

        return productoService.crear(request);
    }

    // Sin autenticacion no se puede consultar el panel.
    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/estado"))
                .andExpect(status().isUnauthorized());
    }

    // Un Cliente no tiene permisos para entrar al panel.
    @Test
    void clienteNoPuedeAccederAlAdministrador() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/v1/admin/estado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // El Administrador puede consultar el estado del panel.
    @Test
    void administradorPuedeAcceder() throws Exception {
        String token = tokenAdministrador();

        mockMvc.perform(get("/api/v1/admin/estado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.panel").value("administracion"))
                .andExpect(jsonPath("$.estado").value("OK"));
    }

    // No permite listar usuarios sin iniciar sesion.
    @Test
    void listarUsuariosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    // Un Cliente no puede consultar la lista de usuarios.
    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // El Administrador puede listar usuarios sin exponer datos sensibles.
    @Test
    void administradorPuedeListarUsuarios() throws Exception {
        String token = tokenAdministrador();

        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.correo == 'mario.permisos@gmail.com')]"
                ).isNotEmpty())
                .andExpect(jsonPath("$[*].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[*].direccion").doesNotExist())
                .andExpect(jsonPath("$[*].fechaNacimiento").doesNotExist());
    }
    // El catalogo de productos se puede consultar sin token.
    @Test
    void consultarProductosSinTokenDevuelve200() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // No permite crear productos sin autenticacion.
    @Test
    void crearProductoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // Un Cliente no puede registrar productos.
    @Test
    void clienteNoPuedeCrearProductos() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // El Administrador puede registrar productos correctamente.
    @Test
    void administradorPuedeCrearProductos() throws Exception {
        String token = tokenAdministrador();
        Categoria categoria = crearCategoria();

        String json = """
                {
                  "nombre": "Teclado Gamer Prueba",
                  "descripcion": "Teclado mecanico RGB",
                  "precio": 45990.00,
                  "stock": 10,
                  "categoriaId": %d
                }
                """.formatted(categoria.getId());

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre")
                        .value("Teclado Gamer Prueba"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoriaId")
                        .value(categoria.getId()));
    }

    // Un Cliente no puede actualizar productos.
    @Test
    void clienteNoPuedeActualizarProductos() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(put("/api/v1/productos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // Un Cliente no puede eliminar productos.
    @Test
    void clienteNoPuedeEliminarProductos() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(delete("/api/v1/productos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // Un Cliente no puede modificar el stock.
    @Test
    void clienteNoPuedeActualizarStock() throws Exception {
        String token = tokenCliente();

        mockMvc.perform(patch("/api/v1/productos/1/stock")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":20}"))
                .andExpect(status().isForbidden());
    }

    // El Administrador puede actualizar el stock mediante PATCH.
    @Test
    void administradorPuedeActualizarStock() throws Exception {
        ProductoResponse producto = crearProducto();
        String token = tokenAdministrador();

        mockMvc.perform(patch(
                        "/api/v1/productos/{id}/stock",
                        producto.id()
                )
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(producto.id()))
                .andExpect(jsonPath("$.stock").value(25));
    }
}
