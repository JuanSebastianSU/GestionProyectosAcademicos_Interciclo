package gestionpa.com.interciclo.Controladores;

import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Servicios.TipoUsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tipos-usuario")
@RequiredArgsConstructor
@Validated
public class TipoUsuarioControlador {

    private final TipoUsuarioServicio tipoUsuarioServicio;

    /* Listar y obtener: TUTOR/ADMIN pueden ver */
    @GetMapping
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ResponseEntity<List<TipoUsuario>> listar() {
        return ResponseEntity.ok(tipoUsuarioServicio.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ResponseEntity<TipoUsuario> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(tipoUsuarioServicio.obtenerPorId(id));
    }

    /* Crear/Actualizar/Patch/Eliminar: SOLO ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody TipoUsuario t) {
        if (t.getNombre() != null && "ADMIN".equalsIgnoreCase(t.getNombre().trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No se permite crear el tipo de usuario ADMIN desde este endpoint"));
        }
        TipoUsuario creado = tipoUsuarioServicio.crear(t);
        return ResponseEntity.created(URI.create("/api/tipos-usuario/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody TipoUsuario t) {
        if (t.getNombre() != null && "ADMIN".equalsIgnoreCase(t.getNombre().trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No se permite asignar el nombre ADMIN a un TipoUsuario"));
        }
        return ResponseEntity.ok(tipoUsuarioServicio.actualizar(id, t));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody Map<String, Object> cambios) {
        Object nombre = cambios.get("nombre");
        if (nombre != null && "ADMIN".equalsIgnoreCase(String.valueOf(nombre).trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No se permite actualizar el nombre a ADMIN"));
        }
        return ResponseEntity.ok(tipoUsuarioServicio.patch(id, cambios));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoUsuarioServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
