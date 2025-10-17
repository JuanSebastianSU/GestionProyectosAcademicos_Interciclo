package gestionpa.com.interciclo.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
    name = "tutor",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tutor_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_tutor_username", columnNames = "username")
    },
    indexes = {
        @Index(name = "idx_tutor_apellido", columnList = "apellido"),
        @Index(name = "idx_tutor_nombre", columnList = "nombre")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String nombre;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String apellido;

    @Email @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String email; // Sugerencia: en PG usar CITEXT por migración SQL

    @NotBlank @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String username; // Sugerencia: CITEXT en migración SQL

    @NotBlank @Size(max = 255)
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
@Column(nullable = false, length = 255)
private String password;

    @Builder.Default
    @Column(nullable = false)
    private Boolean estaActivo = true;

    @Size(max = 80)
    @Column(length = 80)
    private String tituloAcademico;

    @Size(max = 80)
    @Column(length = 80)
    private String departamento;

    @ManyToOne
    @JoinColumn(name = "tipo_usuario_id")
    private TipoUsuario tipoUsuario;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY)
    private List<Proyecto> proyectos;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}
