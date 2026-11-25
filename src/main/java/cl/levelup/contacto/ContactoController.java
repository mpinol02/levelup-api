package cl.levelup.contacto;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ContactoController {

    private final ContactoService servicio;

    public ContactoController(
            ContactoService servicio
    ) {
        this.servicio = servicio;
    }

    // Permite enviar una consulta desde el formulario publico.
    @PostMapping("/contactos")
    public ResponseEntity<Map<String, Object>> registrar(
            @Valid @RequestBody ContactoRequest datos
    ) {
        ContactoResponse contacto = servicio.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", contacto.id(),
                        "mensaje", "Consulta enviada correctamente."
                ));
    }

    // Lista las consultas recibidas.
    @GetMapping("/admin/contactos")
    @PreAuthorize("hasRole('Administrador')")
    public List<ContactoResponse> listar() {

        return servicio.listar();
    }

    // Consulta el detalle de un mensaje.
    @GetMapping("/admin/contactos/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ContactoResponse consultar(
            @PathVariable Long id
    ) {
        return servicio.consultar(id);
    }

    // Marca una consulta como atendida o pendiente.
    @PatchMapping("/admin/contactos/{id}/estado")
    @PreAuthorize("hasRole('Administrador')")
    public ContactoResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoContactoRequest datos
    ) {
        return servicio.actualizarEstado(
                id,
                datos.atendido()
        );
    }

    // Elimina una consulta.
    @DeleteMapping("/admin/contactos/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        servicio.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // Devuelve la cantidad de consultas pendientes.
    @GetMapping("/admin/contactos/pendientes")
    @PreAuthorize("hasRole('Administrador')")
    public Map<String, Long> pendientes() {

        return Map.of(
                "pendientes",
                servicio.contarPendientes()
        );
    }

    public record EstadoContactoRequest(

            @NotNull(message = "El estado es obligatorio.")
            Boolean atendido

    ) {
    }
}
