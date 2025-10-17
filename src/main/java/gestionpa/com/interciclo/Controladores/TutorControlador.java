package gestionpa.com.interciclo.Controladores;

import gestionpa.com.interciclo.Entidades.Tutor;
import gestionpa.com.interciclo.Servicios.TutorServicio;
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
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
public class TutorControlador {

    private final TutorServicio tutorServicio;

    @PostMapping
    public ResponseEntity<Tutor> crear(@Valid @RequestBody Tutor tutor) {
        Tutor creado = tutorServicio.crear(tutor);
        return ResponseEntity.created(URI.create("/api/tutores/" + creado.getId())).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Tutor>> listar() {
        return ResponseEntity.ok(tutorServicio.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutor> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(tutorServicio.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tutor> actualizar(@PathVariable Long id, @Valid @RequestBody Tutor tutor) {
        return ResponseEntity.ok(tutorServicio.actualizar(id, tutor));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Tutor> patch(@PathVariable Long id, @RequestBody Map<String, Object> cambios) {
        return ResponseEntity.ok(tutorServicio.patch(id, cambios));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tutorServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
