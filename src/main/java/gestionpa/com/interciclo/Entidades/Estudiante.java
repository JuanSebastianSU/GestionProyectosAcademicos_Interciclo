package gestionpa.com.interciclo.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "estudiante",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_estudiante_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_estudiante_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_estudiante_codigo", columnNames = "codigo")
    },
    indexes = {
        @Index(name = "idx_estudiante_apellido", columnList = "apellido"),
        @Index(name = "idx_estudiante_nombre", columnList = "nombre")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estudiante {

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
    private String email; // Sugerencia: CITEXT por migración SQL

    @NotBlank @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String username; // Sugerencia: CITEXT por migración SQL

    @NotBlank
@Size(max = 255)
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // <- permite recibirlo en requests pero NO lo devuelve en responses
@Column(nullable = false, length = 255)
private String password;

    @Builder.Default
    @Column(nullable = false)
    private Boolean estaActivo = true;

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String codigo; // matrícula/código institucional único

    @Size(max = 80)
    @Column(length = 80)
    private String carrera;

    @Size(max = 20)
    @Column(length = 20)
    private String ciclo;

    @ManyToOne
    @JoinColumn(name = "tipo_usuario_id")
    private TipoUsuario tipoUsuario;

    // Relación 1–1 (lado inverso)
    @OneToOne(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private Proyecto proyecto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}
