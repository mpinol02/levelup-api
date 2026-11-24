package cl.levelup;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Comprueba las operaciones de categorias con Spring Boot y H2.
@SpringBootTest
@ActiveProfiles("local")
@Transactional
class CategoriaTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Genera nombres diferentes para evitar conflictos entre pruebas.
    private String nombreUnico() {
        return "Categoria-" + UUID.randomUUID();
    }

    // Verifica que una categoria se guarde correctamente.
    @Test
    void crearCategoria() {
        Categoria categoria = new Categoria(
                nombreUnico(),
                "Categoria de prueba"
        );

        Categoria guardada = categoriaRepository.saveAndFlush(categoria);

        assertNotNull(guardada.getId());
        assertEquals("Categoria de prueba", guardada.getDescripcion());
    }

    // Comprueba que una categoria se pueda buscar por su ID.
    @Test
    void buscarCategoriaPorId() {
        Categoria categoria = categoriaRepository.saveAndFlush(
                new Categoria(nombreUnico(), "Busqueda por ID")
        );

        Optional<Categoria> encontrada =
                categoriaRepository.findById(categoria.getId());

        assertTrue(encontrada.isPresent());
        assertEquals(categoria.getNombre(), encontrada.get().getNombre());
    }

    // Verifica que los cambios de una categoria se guarden.
    @Test
    void actualizarCategoria() {
        Categoria categoria = categoriaRepository.saveAndFlush(
                new Categoria(nombreUnico(), "Descripcion original")
        );

        categoria.setDescripcion("Descripcion actualizada");

        categoriaRepository.saveAndFlush(categoria);

        Categoria actualizada = categoriaRepository
                .findById(categoria.getId())
                .orElseThrow();

        assertEquals(
                "Descripcion actualizada",
                actualizada.getDescripcion()
        );
    }

    // Comprueba que una categoria se elimine correctamente.
    @Test
    void eliminarCategoria() {
        Categoria categoria = categoriaRepository.saveAndFlush(
                new Categoria(nombreUnico(), "Categoria temporal")
        );

        Long id = categoria.getId();

        categoriaRepository.delete(categoria);
        categoriaRepository.flush();

        assertFalse(categoriaRepository.existsById(id));
    }

    // Verifica que el repositorio detecte nombres repetidos.
    @Test
    void detectarNombreDuplicado() {
        String nombre = nombreUnico();

        categoriaRepository.saveAndFlush(
                new Categoria(nombre, "Primera categoria")
        );

        assertTrue(
                categoriaRepository.existsByNombreIgnoreCase(
                        nombre.toUpperCase()
                )
        );
    }

    // Busca categorias sin distinguir mayusculas y minusculas.
    @Test
    void buscarCategoriaPorNombreSinDistinguirMayusculas() {
        String nombre = nombreUnico();

        categoriaRepository.saveAndFlush(
                new Categoria(nombre, "Busqueda por nombre")
        );

        Optional<Categoria> encontrada =
                categoriaRepository.findByNombreIgnoreCase(
                        nombre.toUpperCase()
                );

        assertTrue(encontrada.isPresent());
        assertEquals(nombre, encontrada.get().getNombre());
    }
}
