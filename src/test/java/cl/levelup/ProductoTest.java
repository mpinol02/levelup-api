package cl.levelup;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;
import cl.levelup.producto.ProductoRequest;
import cl.levelup.producto.ProductoResponse;
import cl.levelup.producto.ProductoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas principales del CRUD de productos y control de stock.
@SpringBootTest
@ActiveProfiles("local")
@Transactional
class ProductoTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Crea una categoria para utilizarla en las pruebas.
    private Categoria crearCategoria() {
        return categoriaRepository.saveAndFlush(
                new Categoria(
                        "Categoria-" + UUID.randomUUID(),
                        "Categoria de prueba"
                )
        );
    }

    // Prepara los datos de un producto valido.
    private ProductoRequest crearSolicitud(Long categoriaId) {
        return new ProductoRequest(
                "Producto-" + UUID.randomUUID(),
                "Producto gamer de prueba",
                new BigDecimal("45990.00"),
                10,
                categoriaId
        );
    }

    // Comprueba que un producto se registre correctamente.
    @Test
    void crearProducto() {
        Categoria categoria = crearCategoria();

        ProductoRequest solicitud = crearSolicitud(categoria.getId());

        ProductoResponse creado = productoService.crear(solicitud);

        assertNotNull(creado.id());
        assertEquals(solicitud.nombre(), creado.nombre());
        assertEquals(10, creado.stock());
        assertEquals(categoria.getId(), creado.categoriaId());
    }

    // Verifica que un producto se pueda buscar por su ID.
    @Test
    void buscarProductoPorId() {
        Categoria categoria = crearCategoria();

        ProductoResponse creado = productoService.crear(
                crearSolicitud(categoria.getId())
        );

        ProductoResponse encontrado =
                productoService.buscarPorId(creado.id());

        assertEquals(creado.id(), encontrado.id());
        assertEquals(creado.nombre(), encontrado.nombre());
    }

    // Comprueba que los datos de un producto se puedan actualizar.
    @Test
    void actualizarProducto() {
        Categoria categoria = crearCategoria();

        ProductoResponse creado = productoService.crear(
                crearSolicitud(categoria.getId())
        );

        ProductoRequest cambios = new ProductoRequest(
                "Producto actualizado-" + UUID.randomUUID(),
                "Descripcion actualizada",
                new BigDecimal("59990.00"),
                20,
                categoria.getId()
        );

        ProductoResponse actualizado =
                productoService.actualizar(creado.id(), cambios);

        assertEquals("Descripcion actualizada",
                actualizado.descripcion());
        assertEquals(0,
                new BigDecimal("59990.00")
                        .compareTo(actualizado.precio()));
        assertEquals(20, actualizado.stock());
    }

    // Verifica que un producto se pueda eliminar.
    @Test
    void eliminarProducto() {
        Categoria categoria = crearCategoria();

        ProductoResponse creado = productoService.crear(
                crearSolicitud(categoria.getId())
        );

        productoService.eliminar(creado.id());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.buscarPorId(creado.id())
        );

        assertEquals(404, error.getStatusCode().value());
    }

    // Comprueba que no se acepten productos con precios negativos.
    @Test
    void rechazarPrecioNegativo() {
        Categoria categoria = crearCategoria();

        ProductoRequest solicitud = new ProductoRequest(
                "Producto-" + UUID.randomUUID(),
                "Precio invalido",
                new BigDecimal("-1000.00"),
                5,
                categoria.getId()
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(solicitud)
        );

        assertEquals(400, error.getStatusCode().value());
    }

    // Verifica que el stock se actualice y no permita valores negativos.
    @Test
    void actualizarYValidarStock() {
        Categoria categoria = crearCategoria();

        ProductoResponse creado = productoService.crear(
                crearSolicitud(categoria.getId())
        );

        ProductoResponse actualizado =
                productoService.actualizarStock(creado.id(), 25);

        assertEquals(25, actualizado.stock());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.actualizarStock(creado.id(), -5)
        );

        assertEquals(400, error.getStatusCode().value());
    }
}
