
package cl.levelup.producto;

import cl.levelup.categoria.Categoria;
import cl.levelup.categoria.CategoriaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

// Contiene la logica de negocio de los productos.
@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Lista todos los productos ordenados por nombre.
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {

        return productoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(ProductoResponse::desdeEntidad)
                .toList();
    }

    // Busca un producto por su identificador.
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {

        return ProductoResponse.desdeEntidad(
                obtenerProducto(id)
        );
    }

    // Lista los productos de una categoria.
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

    // Lista solamente los productos con stock disponible.
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
        String codigo = limpiarCodigo(request.codigo());

        validarDatos(request.precio(), request.stock());
        validarStockCritico(request.stockCritico());

        validarPrecioAnterior(
                request.precio(),
                request.precioAnterior()
        );

        // Evita nombres duplicados.
        if (productoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese nombre"
            );
        }

        // Evita codigos comerciales duplicados.
        if (codigo != null &&
                productoRepository.existsByCodigoIgnoreCase(codigo)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese codigo"
            );
        }

        Categoria categoria = obtenerCategoria(
                request.categoriaId()
        );

        // Crea el producto con todos sus atributos.
        Producto producto = new Producto(
                codigo,
                nombre,
                limpiarDescripcion(request.descripcion()),
                request.precio(),
                request.precioAnterior(),
                request.stock(),
                request.stockCritico(),
                limpiarImagen(request.imagen()),
                categoria
        );

        Producto guardado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(guardado);
    }

    // Actualiza los datos de un producto existente.
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request
    ) {

        Producto producto = obtenerProducto(id);

        String nombre = limpiarNombre(request.nombre());
        String codigo = limpiarCodigo(request.codigo());

        validarDatos(request.precio(), request.stock());
        validarStockCritico(request.stockCritico());

        validarPrecioAnterior(
                request.precio(),
                request.precioAnterior()
        );

        // Comprueba que el nombre no pertenezca a otro producto.
        productoRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {

                    if (!existente.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ya existe otro producto con ese nombre"
                        );
                    }
                });

        // Comprueba que el codigo no pertenezca a otro producto.
        if (codigo != null) {

            productoRepository.findByCodigoIgnoreCase(codigo)
                    .ifPresent(existente -> {

                        if (!existente.getId().equals(id)) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Ya existe otro producto con ese codigo"
                            );
                        }
                    });
        }

        Categoria categoria = obtenerCategoria(
                request.categoriaId()
        );

        // Actualiza los atributos del producto.
        producto.setCodigo(codigo);
        producto.setNombre(nombre);

        producto.setDescripcion(
                limpiarDescripcion(request.descripcion())
        );

        producto.setPrecio(request.precio());
        producto.setPrecioAnterior(request.precioAnterior());
        producto.setStock(request.stock());
        producto.setStockCritico(request.stockCritico());
        producto.setImagen(limpiarImagen(request.imagen()));
        producto.setCategoria(categoria);

        Producto actualizado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(actualizado);
    }

    // Elimina un producto existente.
    public void eliminar(Long id) {

        Producto producto = obtenerProducto(id);

        productoRepository.delete(producto);
    }

    // Actualiza solamente el stock disponible.
    public ProductoResponse actualizarStock(
            Long id,
            Integer nuevoStock
    ) {

        Producto producto = obtenerProducto(id);

        validarStock(nuevoStock);

        producto.setStock(nuevoStock);

        Producto actualizado = productoRepository.save(producto);

        return ProductoResponse.desdeEntidad(actualizado);
    }

    // Busca un producto o devuelve un error 404.
    private Producto obtenerProducto(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }

    // Comprueba que la categoria exista.
    private Categoria obtenerCategoria(Long categoriaId) {

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));
    }

    // Limpia y valida el nombre del producto.
    private String limpiarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del producto es obligatorio"
            );
        }

        return nombre.trim();
    }

    // Normaliza el codigo comercial a mayusculas.
    private String limpiarCodigo(String codigo) {

        // Mantiene compatibilidad con las pruebas anteriores.
        if (codigo == null) {
            return null;
        }

        String limpio = codigo.trim()
                .toUpperCase(Locale.ROOT);

        if (limpio.length() < 3 || limpio.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El codigo debe tener entre 3 y 50 caracteres"
            );
        }

        return limpio;
    }

    // Elimina espacios innecesarios de la descripcion.
    private String limpiarDescripcion(String descripcion) {

        return descripcion == null
                ? null
                : descripcion.trim();
    }

    // Normaliza la referencia de la imagen.
    private String limpiarImagen(String imagen) {

        if (imagen == null || imagen.isBlank()) {
            return null;
        }

        return imagen.trim();
    }

    // Comprueba que el precio y el stock sean validos.
    private void validarDatos(
            BigDecimal precio,
            Integer stock
    ) {

        if (precio == null ||
                precio.compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio no puede ser negativo ni nulo"
            );
        }

        validarStock(stock);
    }

    // Impide registrar cantidades negativas.
    private void validarStock(Integer stock) {

        if (stock == null || stock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock no puede ser negativo ni nulo"
            );
        }
    }

    // Valida el limite opcional de stock critico.
    private void validarStockCritico(Integer stockCritico) {

        if (stockCritico != null && stockCritico < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock critico no puede ser negativo"
            );
        }
    }

    // El precio anterior debe ser mayor al precio actual.
    private void validarPrecioAnterior(
            BigDecimal precio,
            BigDecimal precioAnterior
    ) {

        if (precioAnterior != null &&
                precioAnterior.compareTo(precio) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio anterior debe ser mayor al precio actual"
            );
        }
    }
}
