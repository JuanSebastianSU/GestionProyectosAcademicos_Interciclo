package gestionpa.com.interciclo.Servicios;

import gestionpa.com.interciclo.Entidades.Estudiante;
import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Repositorios.EstudianteRepositorio;
import gestionpa.com.interciclo.Repositorios.TipoUsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class EstudianteServicio {

    private final EstudianteRepositorio estudianteRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public EstudianteServicio(EstudianteRepositorio estudianteRepositorio,
                              TipoUsuarioRepositorio tipoUsuarioRepositorio,
                              PasswordEncoder passwordEncoder) {
        this.estudianteRepositorio = estudianteRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String ROL_ESTUDIANTE = "ESTUDIANTE";

    /* ===================== CRUD ===================== */

    public Estudiante crear(Estudiante e) {
        // 1) Resolver/forzar rol ESTUDIANTE (si no envían tipoUsuario, se exige ESTUDIANTE existente)
        e.setTipoUsuario(resolverTipoUsuarioEstudiante(e.getTipoUsuario()));

        // 2) Normaliza
        normaliza(e);

        // 3) Unicidades
        if (estudianteRepositorio.existsByEmail(e.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + e.getEmail());
        }
        if (estudianteRepositorio.existsByUsername(e.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + e.getUsername());
        }
        if (estudianteRepositorio.existsByCodigo(e.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código ya existe: " + e.getCodigo());
        }

        // 4) Password hash (obligatorio)
        if (e.getPassword() == null || e.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password es obligatorio");
        }
        e.setPassword(passwordEncoder.encode(e.getPassword()));

        if (e.getEstaActivo() == null) e.setEstaActivo(true);

        // 5) Persistir
        return estudianteRepositorio.save(e);
    }

    public Estudiante actualizar(Long id, Estudiante e) {
        Estudiante db = obtenerPorId(id);

        // Si llega tipoUsuario, se resuelve pero sólo se permite ESTUDIANTE
        if (e.getTipoUsuario() != null) {
            db.setTipoUsuario(resolverTipoUsuarioEstudiante(e.getTipoUsuario()));
        }

        String nuevoEmail = trimOrNull(e.getEmail());
        String nuevoUser  = trimOrNull(e.getUsername());
        String nuevoCod   = trimOrNull(e.getCodigo());

        if (nuevoEmail != null && !equalsIgnoreCase(nuevoEmail, db.getEmail())
                && estudianteRepositorio.existsByEmail(nuevoEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + nuevoEmail);
        }
        if (nuevoUser != null && !equalsIgnoreCase(nuevoUser, db.getUsername())
                && estudianteRepositorio.existsByUsername(nuevoUser)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + nuevoUser);
        }
        if (nuevoCod != null && !equalsIgnoreCase(nuevoCod, db.getCodigo())
                && estudianteRepositorio.existsByCodigo(nuevoCod)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código ya existe: " + nuevoCod);
        }

        db.setNombre(trimOrNull(e.getNombre()));
        db.setApellido(trimOrNull(e.getApellido()));
        if (nuevoEmail != null) db.setEmail(nuevoEmail);
        if (nuevoUser  != null) db.setUsername(nuevoUser);
        if (nuevoCod   != null) db.setCodigo(nuevoCod);
        db.setCarrera(trimOrNull(e.getCarrera()));
        db.setCiclo(trimOrNull(e.getCiclo()));

        if (e.getPassword() != null && !e.getPassword().isBlank()) {
            db.setPassword(passwordEncoder.encode(e.getPassword()));
        }
        if (e.getEstaActivo() != null) db.setEstaActivo(e.getEstaActivo());

        return db;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Estudiante> listar() {
        return estudianteRepositorio.findAll();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Estudiante obtenerPorId(Long id) {
        return estudianteRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Estudiante no encontrado id=" + id));
    }

    public void eliminar(Long id) {
        estudianteRepositorio.deleteById(id);
    }

    /* ===================== PATCH ===================== */

    public Estudiante patch(Long id, Map<String, Object> cambios) {
        Estudiante db = obtenerPorId(id);

        // nombre, apellido
        putIfString(cambios, "nombre", v -> db.setNombre(trimOrNull(v)));
        putIfString(cambios, "apellido", v -> db.setApellido(trimOrNull(v)));

        // email con unicidad
        if (cambios.containsKey("email") && cambios.get("email") != null) {
            String email = trimOrNull(String.valueOf(cambios.get("email")));
            if (!equalsIgnoreCase(email, db.getEmail()) && estudianteRepositorio.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + email);
            }
            db.setEmail(email);
        }

        // username con unicidad
        if (cambios.containsKey("username") && cambios.get("username") != null) {
            String user = trimOrNull(String.valueOf(cambios.get("username")));
            if (!equalsIgnoreCase(user, db.getUsername()) && estudianteRepositorio.existsByUsername(user)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + user);
            }
            db.setUsername(user);
        }

        // código con unicidad
        if (cambios.containsKey("codigo") && cambios.get("codigo") != null) {
            String cod = trimOrNull(String.valueOf(cambios.get("codigo")));
            if (!equalsIgnoreCase(cod, db.getCodigo()) && estudianteRepositorio.existsByCodigo(cod)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Código ya existe: " + cod);
            }
            db.setCodigo(cod);
        }

        // password (hash si llega)
        if (cambios.containsKey("password") && cambios.get("password") != null) {
            String raw = String.valueOf(cambios.get("password"));
            if (!raw.isBlank()) db.setPassword(passwordEncoder.encode(raw));
        }

        // activo
        putIfBoolean(cambios, "estaActivo", db::setEstaActivo);

        // otros
        putIfString(cambios, "carrera", v -> db.setCarrera(trimOrNull(v)));
        putIfString(cambios, "ciclo", v -> db.setCiclo(trimOrNull(v)));

        // tipoUsuario por id o nombre (sólo ESTUDIANTE)
        if (cambios.containsKey("tipoUsuarioId") && cambios.get("tipoUsuarioId") != null) {
            Long tid = toLong(cambios.get("tipoUsuarioId"));
            TipoUsuario tu = tipoUsuarioRepositorio.findById(tid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado id=" + tid));
            if (!ROL_ESTUDIANTE.equalsIgnoreCase(tu.getNombre())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido para Estudiante: " + tu.getNombre());
            }
            db.setTipoUsuario(tu);
        } else if (cambios.containsKey("tipoUsuarioNombre") && cambios.get("tipoUsuarioNombre") != null) {
            String nom = normalize(String.valueOf(cambios.get("tipoUsuarioNombre")));
            if (!ROL_ESTUDIANTE.equalsIgnoreCase(nom)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido para Estudiante: " + nom);
            }
            TipoUsuario tu = tipoUsuarioRepositorio.findByNombre(nom)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado nombre=" + nom));
            db.setTipoUsuario(tu);
        }

        return db;
    }

    /* ===================== Helpers ===================== */

    /** Resuelve/valida que el tipo sea ESTUDIANTE. Si no se envía, exige que exista el rol ESTUDIANTE. */
    private TipoUsuario resolverTipoUsuarioEstudiante(TipoUsuario entrada) {
        TipoUsuario tu;
        if (entrada == null) {
            // Por regla de negocio no auto-creamos roles aquí (solo ADMIN por API de tipos puede crearlos).
            tu = tipoUsuarioRepositorio.findByNombre(ROL_ESTUDIANTE)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Debe existir el tipo de usuario ESTUDIANTE. Pídale al ADMIN crearlo."));
        } else if (entrada.getId() != null) {
            tu = tipoUsuarioRepositorio.findById(entrada.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado id=" + entrada.getId()));
        } else if (entrada.getNombre() != null) {
            String nom = normalize(entrada.getNombre());
            tu = tipoUsuarioRepositorio.findByNombre(nom)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado nombre=" + nom));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoUsuario debe incluir id o nombre");
        }

        if (!ROL_ESTUDIANTE.equalsIgnoreCase(tu.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido para Estudiante: " + tu.getNombre());
        }
        return tu;
    }

    private void putIfString(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        if (map.containsKey(key) && map.get(key) != null) setter.accept(String.valueOf(map.get(key)));
    }

    private void putIfBoolean(Map<String, Object> map, String key, java.util.function.Consumer<Boolean> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object v = map.get(key);
            if (v instanceof Boolean b) setter.accept(b);
            else setter.accept(Boolean.valueOf(String.valueOf(v)));
        }
    }

    private Long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(String.valueOf(v));
    }

    private void normaliza(Estudiante e) {
        e.setNombre(trimOrNull(e.getNombre()));
        e.setApellido(trimOrNull(e.getApellido()));
        e.setEmail(trimOrNull(e.getEmail()));
        e.setUsername(trimOrNull(e.getUsername()));
        e.setCodigo(trimOrNull(e.getCodigo()));
        e.setCarrera(trimOrNull(e.getCarrera()));
        e.setCiclo(trimOrNull(e.getCiclo()));
        if (e.getTipoUsuario() != null && e.getTipoUsuario().getNombre() != null) {
            e.getTipoUsuario().setNombre(normalize(e.getTipoUsuario().getNombre()));
        }
    }

    private String trimOrNull(String s) { return s == null ? null : s.trim(); }

    private boolean equalsIgnoreCase(String a, String b) {
        return Objects.equals(a == null ? null : a.toLowerCase(), b == null ? null : b.toLowerCase());
    }

    private String normalize(String x) {
        return x == null ? null : x.trim().toUpperCase().replace(' ', '_');
    }
}
