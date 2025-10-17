package gestionpa.com.interciclo.Servicios;

import gestionpa.com.interciclo.Entidades.Estudiante;
import gestionpa.com.interciclo.Entidades.TipoUsuario;
import gestionpa.com.interciclo.Repositorios.EstudianteRepositorio;
import gestionpa.com.interciclo.Repositorios.TipoUsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EstudianteServicio {

    private final EstudianteRepositorio estudianteRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;

    public EstudianteServicio(EstudianteRepositorio estudianteRepositorio, TipoUsuarioRepositorio tipoUsuarioRepositorio) {
        this.estudianteRepositorio = estudianteRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
    }

    public Estudiante crear(Estudiante e) {
        return estudianteRepositorio.save(e);
    }

    public Estudiante actualizar(Long id, Estudiante e) {
        Estudiante db = obtenerPorId(id);
        db.setNombre(e.getNombre());
        db.setApellido(e.getApellido());
        db.setEmail(e.getEmail());
        db.setUsername(e.getUsername());
        if (e.getPassword() != null) db.setPassword(e.getPassword());
        db.setEstaActivo(e.getEstaActivo() != null ? e.getEstaActivo() : db.getEstaActivo());
        db.setCodigo(e.getCodigo());
        db.setCarrera(e.getCarrera());
        db.setCiclo(e.getCiclo());
        db.setTipoUsuario(e.getTipoUsuario());
        return db;
    }

    public List<Estudiante> listar() {
        return estudianteRepositorio.findAll();
    }

    public Estudiante obtenerPorId(Long id) {
        return estudianteRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado id=" + id));
    }

    public void eliminar(Long id) {
        estudianteRepositorio.deleteById(id);
    }

    /* PATCH parcial */
    public Estudiante patch(Long id, Map<String, Object> cambios) {
        Estudiante db = obtenerPorId(id);

        putIfString(cambios, "nombre", db::setNombre);
        putIfString(cambios, "apellido", db::setApellido);
        putIfString(cambios, "email", db::setEmail);
        putIfString(cambios, "username", db::setUsername);
        putIfString(cambios, "password", db::setPassword);
        putIfBoolean(cambios, "estaActivo", db::setEstaActivo);
        putIfString(cambios, "codigo", db::setCodigo);
        putIfString(cambios, "carrera", db::setCarrera);
        putIfString(cambios, "ciclo", db::setCiclo);

        if (cambios.containsKey("tipoUsuarioId") && cambios.get("tipoUsuarioId") != null) {
            Long tipoId = toLong(cambios.get("tipoUsuarioId"));
            TipoUsuario tu = tipoUsuarioRepositorio.findById(tipoId)
                    .orElseThrow(() -> new IllegalArgumentException("TipoUsuario no encontrado id=" + tipoId));
            db.setTipoUsuario(tu);
        }
        return db;
    }

    /* Helpers */
    private void putIfString(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        if (map.containsKey(key) && map.get(key) != null) setter.accept(String.valueOf(map.get(key)));
    }
    private void putIfBoolean(Map<String, Object> map, String key, java.util.function.Consumer<Boolean> setter) {
        if (map.containsKey(key) && map.get(key) != null) setter.accept(Boolean.valueOf(String.valueOf(map.get(key))));
    }
    private Long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(String.valueOf(v));
    }
}
