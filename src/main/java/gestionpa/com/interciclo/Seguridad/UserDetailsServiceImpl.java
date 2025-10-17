package gestionpa.com.interciclo.Seguridad;

import gestionpa.com.interciclo.Entidades.Tutor;
import gestionpa.com.interciclo.Repositorios.TutorRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TutorRepositorio tutorRepositorio;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Tutor t = tutorRepositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Normaliza el rol para que Spring Security lo entienda como ROLE_XYZ
        String rol = (t.getTipoUsuario() != null) ? t.getTipoUsuario().getNombre() : "USER";
        rol = rol.trim().toUpperCase().replace(' ', '_');

        return User.builder()
                .username(t.getUsername())
                .password(t.getPassword()) // Debe estar encriptado (BCrypt)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + rol)))
                .accountLocked(false)
                .disabled(Boolean.FALSE.equals(t.getEstaActivo()))
                .build();
    }
}
