package cl.levelup.compra;

import cl.levelup.usuario.Usuario;
import cl.levelup.usuario.UsuarioRepository;
import jakarta.validation.Valid;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final CompraService compraService;
    private final UsuarioRepository usuarioRepository;

    public CompraController(
            CompraService compraService,
            UsuarioRepository usuarioRepository
    ) {
        this.compraService = compraService;
        this.usuarioRepository = usuarioRepository;
    }

    // Registra la compra a nombre de la cuenta que inició sesión.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse crear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompraRequest datos
    ) {
        String run = jwt.getSubject();

        Usuario usuario = usuarioRepository.findById(run)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "La cuenta de la sesión ya no existe."
                ));

        return compraService.procesar(
                datos,
                usuario.getRun(),
                tieneBeneficioDuoc(usuario.getCorreo())
        );
    }

    // El beneficio se calcula con el correo de la cuenta, no el del formulario.
    private boolean tieneBeneficioDuoc(String correoRegistrado) {
        String correo = correoRegistrado.trim().toLowerCase(Locale.ROOT);
        return correo.endsWith("@duoc.cl")
                || correo.endsWith("@profesor.duoc.cl");
    }
}