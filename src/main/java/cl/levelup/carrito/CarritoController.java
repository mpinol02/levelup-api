
package cl.levelup.carrito;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(
            CarritoService carritoService
    ) {
        this.carritoService = carritoService;
    }

    // Consulta el carrito de la cuenta autenticada.
    @GetMapping
    public CarritoResponse consultar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return carritoService.consultar(jwt.getSubject());
    }

    // Agrega un producto al carrito.
    @PostMapping("/items")
    public CarritoResponse agregar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CarritoRequest datos
    ) {
        return carritoService.agregar(
                jwt.getSubject(),
                datos
        );
    }

    // Actualiza la cantidad de un producto.
    @PutMapping("/items/{productoId}")
    public CarritoResponse actualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productoId,
            @Valid @RequestBody CantidadRequest datos
    ) {
        return carritoService.actualizar(
                jwt.getSubject(),
                productoId,
                datos.cantidad()
        );
    }

    // Elimina un producto específico.
    @DeleteMapping("/items/{productoId}")
    public CarritoResponse eliminar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productoId
    ) {
        return carritoService.eliminar(
                jwt.getSubject(),
                productoId
        );
    }

    // Vacía completamente el carrito.
    @DeleteMapping
    public CarritoResponse vaciar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return carritoService.vaciar(jwt.getSubject());
    }

    // DTO utilizado para actualizar cantidades.
    public record CantidadRequest(

            @NotNull(message = "La cantidad es obligatoria.")
            @Positive(message = "La cantidad debe ser mayor que cero.")
            Integer cantidad

    ) {
    }
}
