package gestionpa.com.interciclo.Controladores;

import gestionpa.com.interciclo.Entidades.Proyecto;
import gestionpa.com.interciclo.Servicios.ProyectoServicio;
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
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
public class ProyectoControlador {

    private final ProyectoServicio proyectoServicio;

    @PostMapping
    public ResponseEntity<Proyecto> crear(@Valid @RequestBody Proyecto p) {
        Proyecto creado = proyectoServicio.crear(p);
        return ResponseEntity.created(URI.create("/api/proyectos/" + creado.getId())).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Proyecto>> listar() {
        return ResponseEntity.ok(proyectoServicio.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoServicio.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> actualizar(@PathVariable Long id, @Valid @RequestBody Proyecto p) {
        return ResponseEntity.ok(proyectoServicio.actualizar(id, p));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Proyecto> patch(@PathVariable Long id, @RequestBody Map<String, Object> cambios) {
        return ResponseEntity.ok(proyectoServicio.patch(id, cambios));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proyectoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
