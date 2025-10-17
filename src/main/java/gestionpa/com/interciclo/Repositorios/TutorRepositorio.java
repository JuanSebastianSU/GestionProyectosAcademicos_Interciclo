package gestionpa.com.interciclo.Repositorios;

import gestionpa.com.interciclo.Entidades.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorRepositorio extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByNombre(String nombre);

    Optional<Tutor> findByEmail(String email);

    Optional<Tutor> findByUsername(String username);

    List<Tutor> findByApellidoContainingIgnoreCase(String apellido);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByTipoUsuarioNombreIgnoreCase(String nombre);

}
