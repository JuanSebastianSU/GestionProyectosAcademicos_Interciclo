package gestionpa.com.interciclo.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType;   // "Bearer"
    private String username;
    private String role;
}
