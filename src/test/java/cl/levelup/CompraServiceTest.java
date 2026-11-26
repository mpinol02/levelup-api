package cl.levelup;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local") // Usa la configuración local al iniciar Spring para la prueba.
@Transactional
class CompraServiceTest {

    @Autowired
    private CompraService compraService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    private Long productoId;

    @BeforeEach
    void prepararProducto() {
        // Cada prueba empieza con su propio mouse de $25.000 y stock 5.
        String nombreCategoria = "Accesorios-" + UUID.randomUUID();

        jdbc.update(
                "insert into categorias (nombre) values (?)",
                nombreCategoria
        );

        Long categoriaId = jdbc.queryForObject(
                "select id from categorias where nombre = ?",
                Long.class,
                nombreCategoria
        );

        Categoria categoria = entityManager.getReference(Categoria.class, categoriaId);

        Producto producto = new Producto(
                "Mouse Gamer",
                "Producto de prueba",
                new BigDecimal("25000"),
                5,
                categoria
        );

        productoId = productoRepository.saveAndFlush(producto).getId();
    }

    @Test
    void pagoAprobadoGuardaLaOrdenYDescuentaDosUnidades() {
        var respuesta = compraService.procesar(
                solicitudDeDosUnidades("Pagada"), "123456785", false
        );

        // Lee desde la base para comprobar el stock y la orden guardada.
        entityManager.flush();
        entityManager.clear();

        Producto producto = productoRepository.findById(productoId).orElseThrow();
        Orden orden = ordenRepository.findById(respuesta.id()).orElseThrow();

        assertThat(producto.getStock()).isEqualTo(3);
        assertThat(orden.getEstado()).isEqualTo("Pagada");
        assertThat(orden.getTotal()).isEqualByComparingTo("50000");
        assertThat(orden.getProductos()).hasSize(1);
        assertThat(orden.getProductos().get(0).getCantidad()).isEqualTo(2);
    }

    @Test
    void pagoRechazadoGuardaLaOrdenYConservaElStock() {
        var respuesta = compraService.procesar(
                solicitudDeDosUnidades("Pago rechazado"), "123456785", false
        );

        entityManager.flush();
        entityManager.clear();

        Producto producto = productoRepository.findById(productoId).orElseThrow();
        Orden orden = ordenRepository.findById(respuesta.id()).orElseThrow();

        assertThat(producto.getStock()).isEqualTo(5);
        assertThat(orden.getEstado()).isEqualTo("Pago rechazado");
        assertThat(orden.getProductos()).hasSize(1);
        assertThat(orden.getProductos().get(0).getCantidad()).isEqualTo(2);
    }

    // Ambos casos compran dos unidades con retiro; solo cambia el pago.
    private CompraRequest solicitudDeDosUnidades(String estado) {
        return new CompraRequest(
                "Axel Prueba",
                "axel.prueba@gmail.com",
                "retiro",
                null,
                estado,
                List.of(new CompraRequest.Item(productoId, 2))
        );
    }
}