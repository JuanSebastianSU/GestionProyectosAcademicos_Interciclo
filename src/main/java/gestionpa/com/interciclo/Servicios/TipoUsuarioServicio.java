package gestionpa.com.interciclo.Servicios;

import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Repositorios.TipoUsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class TipoUsuarioServicio {

    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;

    public TipoUsuarioServicio(TipoUsuarioRepositorio tipoUsuarioRepositorio) {
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
    }

    // 🚫 No incluimos ADMIN aquí: sólo se permite TUTOR y ESTUDIANTE.
    private static final Set<String> ROLES_PERMITIDOS = Set.of("TUTOR", "ESTUDIANTE");

    private String normalizaRol(String nombre) {
        return nombre == null ? null : nombre.trim().toUpperCase().replace(' ', '_');
    }

    public TipoUsuario crear(TipoUsuario t) {
        String rol = normalizaRol(t.getNombre());
        if (rol == null || !ROLES_PERMITIDOS.contains(rol)) {
            // ADMIN queda explícitamente prohibido por servicio
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tipo de usuario no permitido. Permitidos: " + ROLES_PERMITIDOS);
        }
        if (tipoUsuarioRepositorio.existsByNombre(rol)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TipoUsuario ya existe: " + rol);
        }
        t.setNombre(rol);
        return tipoUsuarioRepositorio.save(t);
    }

    public TipoUsuario actualizar(Long id, TipoUsuario t) {
        TipoUsuario db = obtenerPorId(id);
        String rol = normalizaRol(t.getNombre());
        if (rol == null || !ROLES_PERMITIDOS.contains(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tipo de usuario no permitido. Permitidos: " + ROLES_PERMITIDOS);
        }
        if (!db.getNombre().equalsIgnoreCase(rol) && tipoUsuarioRepositorio.existsByNombre(rol)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TipoUsuario ya existe: " + rol);
        }
        db.setNombre(rol);
        db.setDescripcion(t.getDescripcion());
        return db;
    }

    public List<TipoUsuario> listar() {
        return tipoUsuarioRepositorio.findAll();
    }

    public TipoUsuario obtenerPorId(Long id) {
        return tipoUsuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TipoUsuario no encontrado id=" + id));
    }

    public void eliminar(Long id) {
        tipoUsuarioRepositorio.deleteById(id);
    }

    public TipoUsuario patch(Long id, Map<String, Object> cambios) {
        TipoUsuario db = obtenerPorId(id);

        if (cambios.containsKey("nombre") && cambios.get("nombre") != null) {
            String rol = normalizaRol(String.valueOf(cambios.get("nombre")));
            if (!ROLES_PERMITIDOS.contains(rol)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Tipo de usuario no permitido. Permitidos: " + ROLES_PERMITIDOS);
            }
            if (!db.getNombre().equalsIgnoreCase(rol) && tipoUsuarioRepositorio.existsByNombre(rol)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "TipoUsuario ya existe: " + rol);
            }
            db.setNombre(rol);
        }

        if (cambios.containsKey("descripcion")) {
            db.setDescripcion(cambios.get("descripcion") == null ? null : String.valueOf(cambios.get("descripcion")));
        }
        return db;
    }
}
