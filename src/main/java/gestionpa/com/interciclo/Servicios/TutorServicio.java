package gestionpa.com.interciclo.Servicios;

import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Entidades.Tutor;
import gestionpa.com.interciclo.Repositorios.TipoUsuarioRepositorio;
import gestionpa.com.interciclo.Repositorios.TutorRepositorio;
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
public class TutorServicio {

    private final TutorRepositorio tutorRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public TutorServicio(TutorRepositorio tutorRepositorio,
                         TipoUsuarioRepositorio tipoUsuarioRepositorio,
                         PasswordEncoder passwordEncoder) {
        this.tutorRepositorio = tutorRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_TUTOR = "TUTOR";
    private static final String DOMINIO_TUTOR = "@tutor.com";

    /* ===================== CRUD ===================== */

    public Tutor crear(Tutor tutor) {
        // 1) Si no envían tipoUsuario, asigna/asegura TUTOR
        tutor.setTipoUsuario(resolverTipoUsuarioODefaultTutor(tutor.getTipoUsuario()));

        // 2) Normaliza
        normalizaCampos(tutor);

        // 3) Unicidades
        if (tutorRepositorio.existsByEmail(tutor.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + tutor.getEmail());
        }
        if (tutorRepositorio.existsByUsername(tutor.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + tutor.getUsername());
        }

        // 4) Password hash
        if (tutor.getPassword() == null || tutor.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password es obligatorio");
        }
        tutor.setPassword(passwordEncoder.encode(tutor.getPassword()));
        if (tutor.getEstaActivo() == null) tutor.setEstaActivo(true);

        // 5) Reglas rol/email + único ADMIN
        validarRolYReglas(tutor, null);

        // 6) Persistir
        return tutorRepositorio.save(tutor);
    }

    public Tutor actualizar(Long id, Tutor tutor) {
        Tutor db = obtenerPorId(id);

        // Si llega tipoUsuario, resolver (id o nombre)
        if (tutor.getTipoUsuario() != null) {
            db.setTipoUsuario(resolverTipoUsuarioODefaultTutor(tutor.getTipoUsuario()));
        }

        String nuevoEmail = trimOrNull(tutor.getEmail());
        String nuevoUser  = trimOrNull(tutor.getUsername());

        if (nuevoEmail != null && !equalsIgnoreCase(nuevoEmail, db.getEmail())
                && tutorRepositorio.existsByEmail(nuevoEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + nuevoEmail);
        }
        if (nuevoUser != null && !equalsIgnoreCase(nuevoUser, db.getUsername())
                && tutorRepositorio.existsByUsername(nuevoUser)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + nuevoUser);
        }

        db.setNombre(trimOrNull(tutor.getNombre()));
        db.setApellido(trimOrNull(tutor.getApellido()));
        if (nuevoEmail != null) db.setEmail(nuevoEmail);
        if (nuevoUser  != null) db.setUsername(nuevoUser);

        if (tutor.getPassword() != null && !tutor.getPassword().isBlank()) {
            db.setPassword(passwordEncoder.encode(tutor.getPassword()));
        }

        if (tutor.getEstaActivo() != null) db.setEstaActivo(tutor.getEstaActivo());
        db.setTituloAcademico(trimOrNull(tutor.getTituloAcademico()));
        db.setDepartamento(trimOrNull(tutor.getDepartamento()));

        validarRolYReglas(db, null);

        return db;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Tutor> listar() {
        return tutorRepositorio.findAll();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Tutor obtenerPorId(Long id) {
        return tutorRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutor no encontrado id=" + id));
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Tutor obtenerPorNombre(String nombre) {
        return tutorRepositorio.findByNombre(nombre).orElse(null);
    }

    public void eliminar(Long id) {
        tutorRepositorio.deleteById(id);
    }

    /* ========= NECESARIO PARA LOGIN ========= */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Tutor obtenerPorUsername(String username) {
        return tutorRepositorio.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tutor no encontrado username=" + username));
    }

    /* ===================== PATCH ===================== */

    public Tutor patch(Long id, Map<String, Object> cambios) {
        Tutor db = obtenerPorId(id);
        boolean wasAdmin = isAdmin(db);

        putIfString(cambios, "nombre", v -> db.setNombre(trimOrNull(v)));
        putIfString(cambios, "apellido", v -> db.setApellido(trimOrNull(v)));

        if (cambios.containsKey("email") && cambios.get("email") != null) {
            String email = trimOrNull(String.valueOf(cambios.get("email")));
            if (!equalsIgnoreCase(email, db.getEmail()) && tutorRepositorio.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe: " + email);
            }
            db.setEmail(email);
        }

        if (cambios.containsKey("username") && cambios.get("username") != null) {
            String user = trimOrNull(String.valueOf(cambios.get("username")));
            if (!equalsIgnoreCase(user, db.getUsername()) && tutorRepositorio.existsByUsername(user)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ya existe: " + user);
            }
            db.setUsername(user);
        }

        if (cambios.containsKey("password") && cambios.get("password") != null) {
            String raw = String.valueOf(cambios.get("password"));
            if (!raw.isBlank()) db.setPassword(passwordEncoder.encode(raw));
        }

        putIfBoolean(cambios, "estaActivo", db::setEstaActivo);
        putIfString(cambios, "tituloAcademico", v -> db.setTituloAcademico(trimOrNull(v)));
        putIfString(cambios, "departamento", v -> db.setDepartamento(trimOrNull(v)));

        if (cambios.containsKey("tipoUsuarioId") && cambios.get("tipoUsuarioId") != null) {
            Long tipoId = toLong(cambios.get("tipoUsuarioId"));
            TipoUsuario tu = tipoUsuarioRepositorio.findById(tipoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado id=" + tipoId));
            db.setTipoUsuario(tu);
        } else if (cambios.containsKey("tipoUsuarioNombre") && cambios.get("tipoUsuarioNombre") != null) {
            String nom = normalize(String.valueOf(cambios.get("tipoUsuarioNombre")));
            TipoUsuario tu = tipoUsuarioRepositorio.findByNombre(nom)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado nombre=" + nom));
            db.setTipoUsuario(tu);
        }

        validarRolYReglas(db, wasAdmin ? ROL_ADMIN : null);
        return db;
    }

    /* ===================== Helpers de rol ===================== */

    private TipoUsuario resolverTipoUsuarioODefaultTutor(TipoUsuario entrada) {
        if (entrada == null) {
            return asegurarRolTutor();
        }
        if (entrada.getId() != null) {
            return tipoUsuarioRepositorio.findById(entrada.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado id=" + entrada.getId()));
        }
        if (entrada.getNombre() != null) {
            String nom = normalize(entrada.getNombre());
            return tipoUsuarioRepositorio.findByNombre(nom)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "TipoUsuario no encontrado nombre=" + nom));
        }
        return asegurarRolTutor();
    }

    private TipoUsuario asegurarRolTutor() {
        return tipoUsuarioRepositorio.findByNombre(ROL_TUTOR)
                .orElseGet(() -> {
                    TipoUsuario t = new TipoUsuario();
                    t.setNombre(ROL_TUTOR);
                    t.setDescripcion("Usuario tutor del sistema");
                    return tipoUsuarioRepositorio.save(t);
                });
    }

    private void validarRolYReglas(Tutor estadoActual, String rolAnterior) {
        String rolActual = estadoActual.getTipoUsuario() != null
                ? normalize(estadoActual.getTipoUsuario().getNombre()) : null;

        if (rolActual == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoUsuario es obligatorio");
        }

        boolean esAdminAhora = ROL_ADMIN.equalsIgnoreCase(rolActual);
        boolean eraAdminAntes = ROL_ADMIN.equalsIgnoreCase(rolAnterior);

        if (esAdminAhora && !eraAdminAntes) {
            long admins = tutorRepositorio.countByTipoUsuarioNombreIgnoreCase(ROL_ADMIN);
            if (admins > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario ADMIN");
            }
        }

        if (ROL_TUTOR.equalsIgnoreCase(rolActual)) {
            String email = estadoActual.getEmail() == null ? "" : estadoActual.getEmail().toLowerCase();
            if (!email.endsWith(DOMINIO_TUTOR)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El email del TUTOR debe terminar en " + DOMINIO_TUTOR);
            }
        }

        if (!ROL_ADMIN.equalsIgnoreCase(rolActual) && !ROL_TUTOR.equalsIgnoreCase(rolActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido para Tutor: " + rolActual);
        }
    }

    private boolean isAdmin(Tutor t) {
        String r = t.getTipoUsuario() != null ? t.getTipoUsuario().getNombre() : null;
        return r != null && ROL_ADMIN.equalsIgnoreCase(r.trim());
    }

    /* ===================== Otros helpers ===================== */

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
    private String trimOrNull(String s) { return (s == null) ? null : s.trim(); }
    private boolean equalsIgnoreCase(String a, String b) {
        return Objects.equals(a == null ? null : a.toLowerCase(), b == null ? null : b.toLowerCase());
    }
    private String normalize(String x) {
        return x == null ? null : x.trim().toUpperCase().replace(' ', '_');
    }
    private void normalizaCampos(Tutor t) {
        t.setNombre(trimOrNull(t.getNombre()));
        t.setApellido(trimOrNull(t.getApellido()));
        t.setEmail(trimOrNull(t.getEmail()));
        t.setUsername(trimOrNull(t.getUsername()));
        t.setTituloAcademico(trimOrNull(t.getTituloAcademico()));
        t.setDepartamento(trimOrNull(t.getDepartamento()));
        if (t.getTipoUsuario() != null && t.getTipoUsuario().getNombre() != null) {
            t.getTipoUsuario().setNombre(normalize(t.getTipoUsuario().getNombre()));
        }
    }
}
