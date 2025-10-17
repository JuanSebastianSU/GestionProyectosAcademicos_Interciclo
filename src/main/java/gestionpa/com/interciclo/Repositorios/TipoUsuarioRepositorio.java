package gestionpa.com.interciclo.Repositorios;

import gestionpa.com.interciclo.Entidades.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUsuarioRepositorio extends JpaRepository<TipoUsuario, Long> {

    Optional<TipoUsuario> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
