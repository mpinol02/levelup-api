
package cl.levelup.compra;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
public class MisComprasController {

    private final MisComprasService misComprasService;

    public MisComprasController(
            MisComprasService misComprasService
    ) {
        this.misComprasService = misComprasService;
    }

    // Recupera el historial de la cuenta autenticada.
    @GetMapping("/mis-compras")
    public List<MisComprasService.ResumenCompra> listar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return misComprasService.listar(
                jwt.getSubject()
        );
    }

    // Recupera únicamente comprobantes propios.
    @GetMapping("/{id}")
    public CompraResponse buscarPorId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id
    ) {
        return misComprasService.buscarPorId(
                id,
                jwt.getSubject()
        );
    }
}
