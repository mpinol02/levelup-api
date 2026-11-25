package cl.levelup.usuario;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Permite consultar y actualizar el perfil del usuario autenticado.
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Recibe el servicio encargado de gestionar usuarios.
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Consulta el perfil del usuario identificado por su JWT.
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> consultarMiPerfil(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String run = jwt.getSubject();

        UsuarioResponse perfil =
                usuarioService.consultarPerfil(run);

        return ResponseEntity.ok(perfil);
    }

    // Actualiza solamente los datos editables del perfil.
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PerfilRequest datos
    ) {
        String run = jwt.getSubject();

        UsuarioResponse perfil =
                usuarioService.actualizarPerfil(run, datos);

        return ResponseEntity.ok(perfil);
    }
}
