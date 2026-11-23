package cl.levelup.categoria;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // GET: consultar todas las categorías.
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {

        return ResponseEntity.ok(
                categoriaService.listar()
        );
    }

    // GET: consultar una categoría por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoriaService.buscarPorId(id)
        );
    }

    // POST: registrar una nueva categoría.
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(
            @Valid @RequestBody CategoriaRequest datos) {

        CategoriaResponse respuesta = categoriaService.crear(datos);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/categorias/" + respuesta.id()
                ))
                .body(respuesta);
    }

    // PUT: actualizar una categoría.
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest datos) {

        return ResponseEntity.ok(
                categoriaService.actualizar(id, datos)
        );
    }

    // DELETE: eliminar una categoría.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
