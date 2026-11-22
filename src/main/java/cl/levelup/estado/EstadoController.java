package cl.levelup.estado;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadoController {
    @GetMapping("/api/v1/estado")
    public Map<String,String> estado() {
        return Map.of("servicio", "levelup-api", "estado", "OK");
    }
}
