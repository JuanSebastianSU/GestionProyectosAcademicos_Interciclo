package gestionpa.com.interciclo.Controladores;

import gestionpa.com.interciclo.DTO.JwtResponse;
import gestionpa.com.interciclo.DTO.LoginRequest;
import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Entidades.Tutor;
import gestionpa.com.interciclo.Repositorios.TipoUsuarioRepositorio;
import gestionpa.com.interciclo.Repositorios.TutorRepositorio;
import gestionpa.com.interciclo.Servicios.JwtService;
import gestionpa.com.interciclo.Servicios.TutorServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:9090")
@RequiredArgsConstructor
public class AuthControlador {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TutorServicio tutorServicio;
    private final TutorRepositorio tutorRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;

    /* =================== LOGIN =================== */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Tutor tutor = tutorServicio.obtenerPorUsername(request.getUsername());
        String rol = (tutor.getTipoUsuario() != null) ? tutor.getTipoUsuario().getNombre() : "USER";
        String token = jwtService.generateToken(tutor.getUsername(), rol);

        return ResponseEntity.ok(new JwtResponse(token, "Bearer", tutor.getUsername(), rol));
    }

    /* ============== REGISTRO ÚNICO DE ADMIN ============== */
    /**
     * Crea el ÚNICO usuario ADMIN del sistema (si aún no existe).
     * - Permite crear desde Postman sin autenticación.
     * - Auto-crea el TipoUsuario ADMIN si no existe (solo aquí).
     * - Si ya existe un ADMIN, responde 409.
     * Body esperado (también acepta vacío; usa defaults):
     * {
     *   "email": "admin@acceso.com",
     *   "username": "ADMIN",
     *   "password": "admin1234",
     *   "nombre": "ADMIN",     // opcional
     *   "apellido": "ADMIN"    // opcional
     * }
     */
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody(required = false) Map<String, Object> body) {
        long admins = tutorRepositorio.countByTipoUsuarioNombreIgnoreCase("ADMIN");
        if (admins > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe un usuario ADMIN"));
        }

        String email    = (body != null && body.get("email") != null)    ? String.valueOf(body.get("email")).trim()    : "admin@acceso.com";
        String username = (body != null && body.get("username") != null) ? String.valueOf(body.get("username")).trim() : "ADMIN";
        String password = (body != null && body.get("password") != null) ? String.valueOf(body.get("password")).trim() : "admin1234";
        String nombre   = (body != null && body.get("nombre") != null)   ? String.valueOf(body.get("nombre")).trim()   : "ADMIN";
        String apellido = (body != null && body.get("apellido") != null) ? String.valueOf(body.get("apellido")).trim() : "ADMIN";

        // Unicidad rápida
        if (tutorRepositorio.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email ya existe: " + email));
        }
        if (tutorRepositorio.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username ya existe: " + username));
        }

        // Asegurar TipoUsuario ADMIN (solo en este endpoint)
        TipoUsuario adminRole = tipoUsuarioRepositorio.findByNombre("ADMIN")
                .orElseGet(() -> {
                    TipoUsuario t = new TipoUsuario();
                    t.setNombre("ADMIN");
                    t.setDescripcion("Administrador del sistema");
                    return tipoUsuarioRepositorio.save(t);
                });

        // Crear el tutor-ADMIN usando la lógica del servicio (hashea, valida, único ADMIN, etc.)
        Tutor admin = new Tutor();
        admin.setNombre(nombre);
        admin.setApellido(apellido);
        admin.setEmail(email);
        admin.setUsername(username);
        admin.setPassword(password);        // será encriptado en el servicio
        admin.setEstaActivo(true);
        admin.setTipoUsuario(adminRole);

        Tutor creado = tutorServicio.crear(admin);
        return ResponseEntity.created(URI.create("/api/tutores/" + creado.getId())).body(creado);
    }
}
