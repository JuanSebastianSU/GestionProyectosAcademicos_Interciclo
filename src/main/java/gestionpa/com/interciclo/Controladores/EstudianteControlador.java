package gestionpa.com.interciclo.Controladores;

import gestionpa.com.interciclo.Entidades.Estudiante;
import gestionpa.com.interciclo.Servicios.EstudianteServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
public class EstudianteControlador {

    private final EstudianteServicio estudianteServicio;

    @PostMapping
    public ResponseEntity<Estudiante> crear(@Valid @RequestBody Estudiante e) {
        Estudiante creado = estudianteServicio.crear(e);
        return ResponseEntity.created(URI.create("/api/estudiantes/" + creado.getId())).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Estudiante>> listar() {
        return ResponseEntity.ok(estudianteServicio.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteServicio.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable Long id, @Valid @RequestBody Estudiante e) {
        return ResponseEntity.ok(estudianteServicio.actualizar(id, e));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Estudiante> patch(@PathVariable Long id, @RequestBody Map<String, Object> cambios) {
        return ResponseEntity.ok(estudianteServicio.patch(id, cambios));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estudianteServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
