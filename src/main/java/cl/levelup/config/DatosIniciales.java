
package cl.levelup.config;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;
import cl.levelup.producto.Producto;
import cl.levelup.producto.ProductoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DatosIniciales {

    // Ejecuta la carga inicial cuando esta habilitada.
    @Bean
    @ConditionalOnProperty(
            name = "levelup.catalogo.inicializar",
            havingValue = "true"
    )
    public CommandLineRunner cargarDatosIniciales(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository
    ) {

        return args -> {

            // Carga las ocho categorias originales.
            Categoria juegos = crearCategoria(
                    categoriaRepository,
                    "Juegos de Mesa"
            );

            Categoria accesorios = crearCategoria(
                    categoriaRepository,
                    "Accesorios"
            );

            Categoria consolas = crearCategoria(
                    categoriaRepository,
                    "Consolas"
            );

            Categoria computadores = crearCategoria(
                    categoriaRepository,
                    "Computadores Gamers"
            );

            Categoria sillas = crearCategoria(
                    categoriaRepository,
                    "Sillas Gamers"
            );

            Categoria mouse = crearCategoria(
                    categoriaRepository,
                    "Mouse"
            );

            Categoria mousepad = crearCategoria(
                    categoriaRepository,
                    "Mousepad"
            );

            crearCategoria(
                    categoriaRepository,
                    "Otros"
            );

            // Producto 1: Catan.
            crearProducto(
                    productoRepository,
                    "JM001",
                    "Catan",
                    "Juego de estrategia para construir poblados, intercambiar recursos y compartir con amigos.",
                    29990,
                    34990L,
                    15,
                    3,
                    "catan.webp",
                    juegos
            );

            // Producto 2: Carcassonne.
            crearProducto(
                    productoRepository,
                    "JM002",
                    "Carcassonne",
                    "Juego de colocación de losetas para construir ciudades, caminos y campos.",
                    24990,
                    null,
                    12,
                    3,
                    "carcassonne.jpg",
                    juegos
            );

            // Producto 3: Control Xbox.
            crearProducto(
                    productoRepository,
                    "AC001",
                    "Control inalámbrico Xbox",
                    "Control inalámbrico para complementar tu experiencia de juego.",
                    59990,
                    null,
                    10,
                    2,
                    "xbox-control.webp",
                    accesorios
            );

            // Producto 4: Audifonos HyperX.
            crearProducto(
                    productoRepository,
                    "AC002",
                    "Audífonos HyperX",
                    "Audífonos gamer con micrófono para comunicarte con tu equipo.",
                    79990,
                    89990L,
                    14,
                    3,
                    "hyperx.jpg",
                    accesorios
            );

            // Producto 5: PlayStation 5.
            crearProducto(
                    productoRepository,
                    "CO001",
                    "PlayStation 5",
                    "Consola PlayStation 5 para disfrutar videojuegos y entretenimiento.",
                    549990,
                    null,
                    8,
                    2,
                    "ps5.png",
                    consolas
            );

            // Producto 6: PC Gamer ASUS.
            crearProducto(
                    productoRepository,
                    "CG001",
                    "PC Gamer ASUS",
                    "Computador gamer ASUS para videojuegos y tareas de uso diario.",
                    1299990,
                    null,
                    5,
                    2,
                    "pc-asus.avif",
                    computadores
            );

            // Producto 7: Silla Gamer.
            crearProducto(
                    productoRepository,
                    "SG001",
                    "Silla Gamer",
                    "Silla gamer para complementar tu espacio de juego o estudio.",
                    349990,
                    null,
                    7,
                    2,
                    "silla-gamer.jpg",
                    sillas
            );

            // Producto 8: Mouse Logitech G502.
            crearProducto(
                    productoRepository,
                    "MS001",
                    "Mouse Logitech G502",
                    "Mouse gamer Logitech G502 para complementar tu equipo.",
                    49990,
                    59990L,
                    20,
                    4,
                    "g502.webp",
                    mouse
            );

            // Producto 9: Mousepad Razer.
            crearProducto(
                    productoRepository,
                    "MP001",
                    "Mousepad Razer",
                    "Mousepad para facilitar el desplazamiento del mouse en tu escritorio.",
                    29990,
                    null,
                    18,
                    4,
                    "mousepad-razer.jpeg",
                    mousepad
            );

            System.out.println(
                    "Catalogo inicial de Level-Up Gamer cargado correctamente."
            );
        };
    }

    // Busca una categoria existente o crea una nueva.
    private Categoria crearCategoria(
            CategoriaRepository repository,
            String nombre
    ) {

        return repository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> repository.save(
                        new Categoria(nombre, null)
                ));
    }

    // Registra un producto sin duplicar los existentes.
    private void crearProducto(
            ProductoRepository repository,
            String codigo,
            String nombre,
            String descripcion,
            long precio,
            Long precioAnterior,
            int stock,
            int stockCritico,
            String imagen,
            Categoria categoria
    ) {

        // Conserva los productos que ya estan registrados.
        if (repository.existsByCodigoIgnoreCase(codigo)
                || repository.existsByNombreIgnoreCase(nombre)) {
            return;
        }

        // Convierte el precio anterior cuando existe una oferta.
        BigDecimal anterior = precioAnterior == null
                ? null
                : BigDecimal.valueOf(precioAnterior);

        Producto producto = new Producto(
                codigo,
                nombre,
                descripcion,
                BigDecimal.valueOf(precio),
                anterior,
                stock,
                stockCritico,
                imagen,
                categoria
        );

        repository.save(producto);
    }
}
