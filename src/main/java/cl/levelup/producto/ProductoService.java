package cl.levelup.producto;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

// Contiene la logica para administrar los productos de Level-Up.
@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Recibe los repositorios necesarios para trabajar con productos y categorias.
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Obtiene todos los productos ordenados por nombre.
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {

        return productoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(ProductoResponse::desdeEntidad)
                .toList();
    }

    // Busca un producto por su ID y devuelve un error si no existe.
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {

        Producto producto = obtenerProducto(id);

        return ProductoResponse.desdeEntidad(producto);
    }

    // Obtiene todos los productos que pertenecen a una categoria.
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(Long categoriaId) {

        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La categoria no existe"
            );
        }

        return productoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(ProductoResponse::desdeEntidad)
                .toList();
    }

    // Obtiene solamente los productos que tienen stock disponible.
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarDisponibles() {

        return productoRepository.findByStockGreaterThan(0)
                .stream()
                .map(ProductoResponse::desdeEntidad)
                .toList();
    }

    // Registra un producto nuevo en la base de datos.
    public ProductoResponse crear(ProductoRequest request) {

        String nombre = limpiarNombre(request.nombre());

        validarDatos(request.precio(), request.stock());

        if (productoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese nombre"
            );
        }

        Categoria categoria = obtenerCategoria(request.categoriaId());

        Producto producto = new Producto(
                nombre,
                limpiarDescripcion(request.descripcion()),
                request.precio(),
                request.stock(),
                categoria
        );

        Producto guardado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(guardado);
    }

    // Actualiza los datos de un producto existente.
    public ProductoResponse actualizar(Long id, ProductoRequest request) {

        Producto producto = obtenerProducto(id);

        String nombre = limpiarNombre(request.nombre());

        validarDatos(request.precio(), request.stock());

        productoRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ya existe otro producto con ese nombre"
                        );
                    }
                });

        Categoria categoria = obtenerCategoria(request.categoriaId());

        producto.setNombre(nombre);
        producto.setDescripcion(limpiarDescripcion(request.descripcion()));
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);

        Producto actualizado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(actualizado);
    }

    // Elimina un producto si existe en la base de datos.
    public void eliminar(Long id) {

        Producto producto = obtenerProducto(id);

        productoRepository.delete(producto);
    }

    // Permite actualizar la cantidad disponible de un producto.
    public ProductoResponse actualizarStock(Long id, Integer nuevoStock) {

        Producto producto = obtenerProducto(id);

        validarStock(nuevoStock);

        producto.setStock(nuevoStock);

        Producto actualizado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(actualizado);
    }

    // Busca la entidad Producto y controla los errores 404.
    private Producto obtenerProducto(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }

    // Busca la categoria asociada y comprueba que exista.
    private Categoria obtenerCategoria(Long categoriaId) {

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));
    }

    // Elimina espacios y comprueba que el nombre sea valido.
    private String limpiarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del producto es obligatorio"
            );
        }

        return nombre.trim();
    }

    // Elimina espacios innecesarios de la descripcion.
    private String limpiarDescripcion(String descripcion) {

        return descripcion == null ? null : descripcion.trim();
    }

    // Comprueba que el precio y el stock sean correctos.
    private void validarDatos(BigDecimal precio, Integer stock) {

        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio no puede ser negativo ni nulo"
            );
        }

        validarStock(stock);
    }

    // Impide registrar cantidades de stock negativas o nulas.
    private void validarStock(Integer stock) {

        if (stock == null || stock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock no puede ser negativo ni nulo"
            );
        }
    }
}
