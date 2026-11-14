package com.condominio.modelo.seguridad;

import jakarta.persistence.*;
import lombok.*;

// Entidad de persistencia para credenciales de acceso al sistema Cobranza Vecinal.
@Entity
@Table(name = "usuario_sistema", schema = "seguridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsuarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 100)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(length = 100)
    private String nombreCompleto;

    @Builder.Default
    private boolean activo = true;
}
