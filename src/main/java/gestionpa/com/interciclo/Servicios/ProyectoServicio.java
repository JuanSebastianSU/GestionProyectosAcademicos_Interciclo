package gestionpa.com.interciclo.Servicios;

import gestionpa.com.interciclo.Entidades.*;
import gestionpa.com.interciclo.Repositorios.EstudianteRepositorio;
import gestionpa.com.interciclo.Repositorios.ProyectoRepositorio;
import gestionpa.com.interciclo.Repositorios.TutorRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProyectoServicio {

    private final ProyectoRepositorio proyectoRepositorio;
    private final TutorRepositorio tutorRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;

    public ProyectoServicio(ProyectoRepositorio proyectoRepositorio,
                            TutorRepositorio tutorRepositorio,
                            EstudianteRepositorio estudianteRepositorio) {
        this.proyectoRepositorio = proyectoRepositorio;
        this.tutorRepositorio = tutorRepositorio;
        this.estudianteRepositorio = estudianteRepositorio;
    }

    public Proyecto crear(Proyecto p) {
        Long estId = (p.getEstudiante() != null) ? p.getEstudiante().getId() : null;
        Long tutId = (p.getTutor() != null) ? p.getTutor().getId() : null;

        if (estId == null || !estudianteRepositorio.existsById(estId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante inválido");
        }
        if (tutId == null || !tutorRepositorio.existsById(tutId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor inválido");
        }
        if (proyectoRepositorio.existsByEstudianteId(estId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El estudiante ya tiene un proyecto");
        }
        if (p.getCodigo() != null && proyectoRepositorio.findByCodigo(p.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de proyecto duplicado");
        }
        return proyectoRepositorio.save(p);
    }

    public Proyecto actualizar(Long id, Proyecto p) {
        Proyecto db = obtenerPorId(id);

        if (p.getCodigo() != null && !p.getCodigo().equals(db.getCodigo())
            && proyectoRepositorio.findByCodigo(p.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de proyecto duplicado");
        }

        db.setCodigo(p.getCodigo());
        db.setTitulo(p.getTitulo());
        db.setResumen(p.getResumen());
        db.setObjetivos(p.getObjetivos());
        db.setAreaTematica(p.getAreaTematica());
        db.setPalabrasClave(p.getPalabrasClave());
        db.setFechaInicio(p.getFechaInicio());
        db.setFechaFin(p.getFechaFin());
        db.setEstado(p.getEstado());
        db.setCalificacionFinal(p.getCalificacionFinal());
        db.setUrlRepositorio(p.getUrlRepositorio());
        db.setUrlDocumento(p.getUrlDocumento());

        if (p.getTutor() != null) {
            Long tid = p.getTutor().getId();
            if (tid == null || !tutorRepositorio.existsById(tid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor inválido");
            }
            db.setTutor(p.getTutor());
        }
        if (p.getEstudiante() != null) {
            Long eid = p.getEstudiante().getId();
            if (eid == null || !estudianteRepositorio.existsById(eid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante inválido");
            }
            // Si cambia de estudiante, verifica 1–1
            if (!eid.equals(db.getEstudiante().getId()) && proyectoRepositorio.existsByEstudianteId(eid)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese estudiante ya tiene un proyecto");
            }
            db.setEstudiante(p.getEstudiante());
        }
        return db;
    }

    public List<Proyecto> listar() {
        return proyectoRepositorio.findAll();
    }

    public Proyecto obtenerPorId(Long id) {
        return proyectoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado id=" + id));
    }

    public void eliminar(Long id) {
        proyectoRepositorio.deleteById(id);
    }

    public Proyecto patch(Long id, Map<String, Object> cambios) {
        Proyecto db = obtenerPorId(id);

        putIfString(cambios, "codigo", db::setCodigo);
        putIfString(cambios, "titulo", db::setTitulo);
        putIfString(cambios, "resumen", db::setResumen);
        putIfString(cambios, "objetivos", db::setObjetivos);
        putIfString(cambios, "areaTematica", db::setAreaTematica);
        putIfString(cambios, "palabrasClave", db::setPalabrasClave);

        if (cambios.containsKey("fechaInicio") && cambios.get("fechaInicio") != null) {
            db.setFechaInicio(LocalDate.parse(String.valueOf(cambios.get("fechaInicio"))));
        }
        if (cambios.containsKey("fechaFin") && cambios.get("fechaFin") != null) {
            db.setFechaFin(LocalDate.parse(String.valueOf(cambios.get("fechaFin"))));
        }
        if (cambios.containsKey("estado") && cambios.get("estado") != null) {
            db.setEstado(EstadoProyecto.valueOf(String.valueOf(cambios.get("estado"))));
        }
        if (cambios.containsKey("calificacionFinal") && cambios.get("calificacionFinal") != null) {
            db.setCalificacionFinal(toBigDecimal(cambios.get("calificacionFinal")));
        }
        putIfString(cambios, "urlRepositorio", db::setUrlRepositorio);
        putIfString(cambios, "urlDocumento", db::setUrlDocumento);

        if (cambios.containsKey("tutorId") && cambios.get("tutorId") != null) {
            Long tid = toLong(cambios.get("tutorId"));
            if (!tutorRepositorio.existsById(tid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor inválido");
            }
            Tutor t = tutorRepositorio.findById(tid).orElseThrow();
            db.setTutor(t);
        }
        if (cambios.containsKey("estudianteId") && cambios.get("estudianteId") != null) {
            Long eid = toLong(cambios.get("estudianteId"));
            if (!estudianteRepositorio.existsById(eid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante inválido");
            }
            if (!eid.equals(db.getEstudiante().getId()) && proyectoRepositorio.existsByEstudianteId(eid)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese estudiante ya tiene un proyecto");
            }
            Estudiante e = estudianteRepositorio.findById(eid).orElseThrow();
            db.setEstudiante(e);
        }

        return db;
    }

    /* Helpers */
    private void putIfString(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        if (map.containsKey(key) && map.get(key) != null) setter.accept(String.valueOf(map.get(key)));
    }
    private Long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(String.valueOf(v));
    }
    private java.math.BigDecimal toBigDecimal(Object v) {
        if (v instanceof BigDecimal) return (BigDecimal) v;
        return new BigDecimal(String.valueOf(v));
    }
}
