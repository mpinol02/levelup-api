
package cl.levelup;

import cl.levelup.carrito.CarritoRequest;
import cl.levelup.carrito.CarritoService;
import cl.levelup.categoria.Categoria;
import cl.levelup.compra.CompraRequest;
import cl.levelup.compra.CompraService;
import cl.levelup.compra.Orden;
import cl.levelup.compra.OrdenRepository;
import cl.levelup.producto.Producto;
import cl.levelup.producto.ProductoRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class CompraServiceTest {

    private static final String RUN = "123456785";
    private static final String OTRO_RUN = "111111111";

    @Autowired
    private CompraService compraService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long productoId;

    @BeforeEach
    void prepararDatos() {

        // Registra dos usuarios de prueba.
        crearUsuario(RUN);
        crearUsuario(OTRO_RUN);

        // Crea una categoría exclusiva para cada prueba.
        String nombreCategoria =
                "Accesorios-" + UUID.randomUUID();

        jdbc.update(
                "insert into categorias (nombre) values (?)",
                nombreCategoria
        );

        Long categoriaId = jdbc.queryForObject(
                "select id from categorias where nombre = ?",
                Long.class,
                nombreCategoria
        );

        Categoria categoria = entityManager.getReference(
                Categoria.class,
                categoriaId
        );

        // Crea un producto de $25.000 con stock de 5 unidades.
        Producto producto = new Producto(
                "Mouse Gamer",
                "Producto de prueba",
                new BigDecimal("25000"),
                5,
                categoria
        );

        productoId = productoRepository
                .saveAndFlush(producto)
                .getId();
    }

    // Registra un usuario válido para las pruebas.
    private void crearUsuario(String run) {

        Integer cantidad = jdbc.queryForObject(
                "select count(*) from usuarios where run = ?",
                Integer.class,
                run
        );

        if (cantidad != null && cantidad > 0) {
            return;
        }

        jdbc.update(
                """
                insert into usuarios (
                    run,
                    nombre,
                    apellidos,
                    correo,
                    fecha_nacimiento,
                    region,
                    comuna,
                    direccion,
                    password_hash,
                    rol,
                    version_seguridad
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                run,
                "Usuario",
                "Prueba",
                UUID.randomUUID() + "@example.com",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Maipu",
                "Direccion de prueba",
                passwordEncoder.encode("ClavePrueba123"),
                "Cliente",
                UUID.randomUUID().toString()
        );
    }

    // PRUEBA 1: una compra aprobada descuenta stock
    // y elimina los productos del carrito.
    @Test
    void pagoAprobadoGuardaLaOrdenYDescuentaDosUnidades() {

        carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 2)
        );

        var respuesta = compraService.procesar(
                solicitudDeDosUnidades("Pagada"),
                RUN,
                false
        );

        entityManager.flush();
        entityManager.clear();

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow();

        Orden orden = ordenRepository
                .findById(respuesta.id())
                .orElseThrow();

        assertThat(producto.getStock()).isEqualTo(3);

        assertThat(orden.getEstado())
                .isEqualTo("Pagada");

        assertThat(orden.getTotal())
                .isEqualByComparingTo("50000");

        assertThat(orden.getProductos())
                .hasSize(1);

        assertThat(orden.getProductos().get(0).getCantidad())
                .isEqualTo(2);

        // Comprueba que la compra vació el carrito.
        assertThat(carritoService.consultar(RUN).productos())
                .isEmpty();
    }

    // PRUEBA 2: un pago rechazado conserva el stock
    // y los productos del carrito.
    @Test
    void pagoRechazadoGuardaLaOrdenYConservaElStock() {

        carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 2)
        );

        var respuesta = compraService.procesar(
                solicitudDeDosUnidades("Pago rechazado"),
                RUN,
                false
        );

        entityManager.flush();
        entityManager.clear();

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow();

        Orden orden = ordenRepository
                .findById(respuesta.id())
                .orElseThrow();

        assertThat(producto.getStock()).isEqualTo(5);

        assertThat(orden.getEstado())
                .isEqualTo("Pago rechazado");

        assertThat(orden.getProductos())
                .hasSize(1);

        assertThat(orden.getProductos().get(0).getCantidad())
                .isEqualTo(2);

        assertThat(carritoService.consultar(RUN).cantidadTotal())
                .isEqualTo(2);
    }

    // PRUEBA 3: agregar productos no descuenta el stock.
    @Test
    void agregarProductoCalculaSubtotalSinDescontarStock() {

        var carrito = carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 2)
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(carrito.productos()).hasSize(1);

        assertThat(carrito.cantidadTotal())
                .isEqualTo(2);

        assertThat(carrito.subtotal())
                .isEqualByComparingTo("50000");

        assertThat(
                productoRepository.findById(productoId)
                        .orElseThrow()
                        .getStock()
        ).isEqualTo(5);
    }

    // PRUEBA 4: no permite superar el stock disponible.
    @Test
    void rechazaCantidadSuperiorAlStock() {

        assertThatThrownBy(() ->
                carritoService.agregar(
                        RUN,
                        new CarritoRequest(productoId, 6)
                )
        )
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(error -> {

            var respuesta = (ResponseStatusException) error;

            assertThat(respuesta.getStatusCode().value())
                    .isEqualTo(409);
        });
    }

    // PRUEBA 5: cada usuario tiene un carrito independiente.
    @Test
    void losCarritosDeUsuariosSonIndependientes() {

        carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 2)
        );

        carritoService.agregar(
                OTRO_RUN,
                new CarritoRequest(productoId, 1)
        );

        assertThat(carritoService.consultar(RUN).cantidadTotal())
                .isEqualTo(2);

        assertThat(carritoService.consultar(OTRO_RUN).cantidadTotal())
                .isEqualTo(1);

        carritoService.vaciar(RUN);

        assertThat(carritoService.consultar(RUN).productos())
                .isEmpty();

        assertThat(carritoService.consultar(OTRO_RUN).cantidadTotal())
                .isEqualTo(1);
    }

    // PRUEBA 6: modifica cantidades y elimina productos.
    @Test
    void actualizaCantidadYEliminaProducto() {

        carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 1)
        );

        var actualizado = carritoService.actualizar(
                RUN,
                productoId,
                3
        );

        assertThat(actualizado.cantidadTotal())
                .isEqualTo(3);

        assertThat(actualizado.subtotal())
                .isEqualByComparingTo("75000");

        var carrito = carritoService.eliminar(
                RUN,
                productoId
        );

        assertThat(carrito.productos()).isEmpty();

        assertThat(carrito.cantidadTotal())
                .isZero();

        assertThat(carrito.subtotal())
                .isEqualByComparingTo("0");
    }

    // PRUEBA 7: no permite comprar con el carrito vacío.
    @Test
    void rechazaCompraConCarritoVacio() {

        carritoService.vaciar(RUN);

        assertThatThrownBy(() ->
                compraService.procesar(
                        solicitudDeDosUnidades("Pagada"),
                        RUN,
                        false
                )
        )
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(error -> {

            var respuesta = (ResponseStatusException) error;

            assertThat(respuesta.getStatusCode().value())
                    .isEqualTo(409);
        });
    }

    // PRUEBA 8: impide manipular cantidades desde el frontend.
    @Test
    void rechazaCompraConCantidadesDistintasAlCarrito() {

        carritoService.agregar(
                RUN,
                new CarritoRequest(productoId, 1)
        );

        assertThatThrownBy(() ->
                compraService.procesar(
                        solicitudDeDosUnidades("Pagada"),
                        RUN,
                        false
                )
        )
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(error -> {

            var respuesta = (ResponseStatusException) error;

            assertThat(respuesta.getStatusCode().value())
                    .isEqualTo(409);
        });
    }

    // Genera una solicitud de compra de dos unidades.
    private CompraRequest solicitudDeDosUnidades(String estado) {

        return new CompraRequest(
                "Axel Prueba",
                "axel.prueba@gmail.com",
                "retiro",
                null,
                estado,
                List.of(
                        new CompraRequest.Item(productoId, 2)
                )
        );
    }
}
