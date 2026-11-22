package cl.levelup.admin;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminEstadoController {

    // SecurityConfig permite llegar aquí solo al rol Administrador.
    @GetMapping("/estado")
    public Map<String, String> consultarEstado() {
        return Map.of("panel", "administracion", "estado", "OK");
    }
}