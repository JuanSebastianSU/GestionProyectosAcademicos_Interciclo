package gestionpa.com.interciclo.Repositorios;

import gestionpa.com.interciclo.Entidades.EstadoProyecto;
import gestionpa.com.interciclo.Entidades.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepositorio extends JpaRepository<Proyecto, Long> {

    Optional<Proyecto> findByCodigo(String codigo);

    List<Proyecto> findByTutorId(Long tutorId);

    Optional<Proyecto> findByEstudianteId(Long estudianteId);

    boolean existsByEstudianteId(Long estudianteId); // útil para validar el 1–1

    List<Proyecto> findByEstado(EstadoProyecto estado);

    List<Proyecto> findByTituloContainingIgnoreCase(String titulo);
}
