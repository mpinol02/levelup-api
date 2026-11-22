
package cl.levelup.admin;

import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioRepository usuarioRepository;

    public AdminUsuarioController(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<List<AdminUsuarioResponse>> listarUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll(
                Sort.by(Sort.Direction.ASC, "nombre")
        );

        List<AdminUsuarioResponse> respuesta = usuarios.stream()
                .map(usuario -> new AdminUsuarioResponse(
                        usuario.getRun(),
                        usuario.getNombre(),
                        usuario.getApellidos(),
                        usuario.getCorreo(),
                        usuario.getRol()
                ))
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}
