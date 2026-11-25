package cl.levelup.admin;

import java.util.List;

import cl.levelup.usuario.RegistroRequest;
import cl.levelup.usuario.UsuarioRequest;
import cl.levelup.usuario.UsuarioResponse;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// CRUD administrativo de usuarios.
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@PreAuthorize("hasRole('Administrador')")
public class AdminUsuarioController {

    private final AdminUsuarioService servicio;

    public AdminUsuarioController(
            AdminUsuarioService servicio
    ) {
        this.servicio = servicio;
    }

    // GET: lista todas las cuentas.
    @GetMapping
    public ResponseEntity<List<AdminUsuarioResponse>> listarUsuarios() {

        return ResponseEntity.ok(servicio.listar());
    }

    // GET: consulta una cuenta por RUN.
    @GetMapping("/{run}")
    public ResponseEntity<UsuarioResponse> consultarUsuario(
            @PathVariable String run
    ) {
        return ResponseEntity.ok(
                servicio.consultar(run)
        );
    }

    // POST: registra una nueva cuenta.
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody RegistroRequest datos
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.crear(datos));
    }

    // PUT: actualiza los datos y el rol de una cuenta.
    @PutMapping("/{run}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable String run,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UsuarioRequest datos
    ) {
        return ResponseEntity.ok(
                servicio.actualizar(
                        run,
                        jwt.getSubject(),
                        datos
                )
        );
    }

    // DELETE: elimina una cuenta distinta a la del administrador.
    @DeleteMapping("/{run}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable String run,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            servicio.eliminar(
                    run,
                    jwt.getSubject()
            );
        } catch (DataIntegrityViolationException error) {

            // Evita eliminar cuentas vinculadas a otros registros.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el usuario porque tiene registros asociados."
            );
        }

        return ResponseEntity.noContent().build();
    }
}
