package gestionpa.com.interciclo.Entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "proyecto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_proyecto_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_proyecto_estudiante", columnNames = "estudiante_id")
    },
    indexes = {
        @Index(name = "idx_proyecto_titulo", columnList = "titulo"),
        @Index(name = "idx_proyecto_estado", columnList = "estado")
    }
)
@Check(constraints = "calificacion_final BETWEEN 0 AND 100")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String codigo; // p.ej., PRJ-2025-001

    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumen;

    @Column(columnDefinition = "TEXT")
    private String objetivos;

    @Size(max = 80)
    @Column(length = 80)
    private String areaTematica;

    @Size(max = 200)
    @Column(length = 200)
    private String palabrasClave;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProyecto estado;

    @Digits(integer = 3, fraction = 2)
    @Column(name = "calificacion_final", precision = 5, scale = 2)
    private BigDecimal calificacionFinal;

    @Size(max = 200)
    @Column(length = 200)
    private String urlRepositorio;

    @Size(max = 200)
    @Column(length = 200)
    private String urlDocumento;

    // Tutor (1–N)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_proyecto_tutor"))
    private Tutor tutor;

    // Estudiante (1–1) — dueño de la relación
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "estudiante_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_proyecto_estudiante")
    )
    private Estudiante estudiante;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}
