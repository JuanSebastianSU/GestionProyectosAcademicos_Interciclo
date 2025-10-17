package gestionpa.com.interciclo.Repositorios;

import gestionpa.com.interciclo.Entidades.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepositorio extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByNombre(String nombre);

    Optional<Estudiante> findByEmail(String email);

    Optional<Estudiante> findByUsername(String username);

    Optional<Estudiante> findByCodigo(String codigo);

    List<Estudiante> findByCarreraContainingIgnoreCase(String carrera);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByCodigo(String codigo);
}
