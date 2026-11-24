package cl.levelup.producto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Expone los endpoints REST para administrar los productos.
@RestController
@RequestMapping("/api/v1/productos")
@Validated
public class ProductoController {

    private final ProductoService productoService;

    // Recibe el servicio que contiene la logica de productos.
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Permite consultar todos los productos.
    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.listar();
    }

    // Permite consultar un producto mediante su ID.
    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    // Permite consultar productos de una categoria.
    @GetMapping("/categoria/{categoriaId}")
    public List<ProductoResponse> listarPorCategoria(
            @PathVariable Long categoriaId
    ) {
        return productoService.listarPorCategoria(categoriaId);
    }

    // Muestra solamente los productos con stock disponible.
    @GetMapping("/disponibles")
    public List<ProductoResponse> listarDisponibles() {
        return productoService.listarDisponibles();
    }

    // Permite al administrador registrar productos nuevos.
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request
    ) {
        ProductoResponse creado = productoService.crear(request);

        URI ubicacion = URI.create("/api/v1/productos/" + creado.id());

        return ResponseEntity
                .created(ubicacion)
                .body(creado);
    }

    // Permite modificar los datos de un producto existente.
    @PutMapping("/{id}")
    public ProductoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return productoService.actualizar(id, request);
    }

    // Permite eliminar un producto registrado.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    // Permite al administrador actualizar el stock disponible.
    @PatchMapping("/{id}/stock")
    public ProductoResponse actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarStockRequest request
    ) {
        return productoService.actualizarStock(id, request.stock());
    }

    // Valida que el nuevo stock no sea negativo ni nulo.
    public record ActualizarStockRequest(
            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock
    ) {
    }
}
