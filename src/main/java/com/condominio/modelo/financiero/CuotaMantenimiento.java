package com.condominio.modelo.financiero;

import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.patrones.state.EstadoCuota;
import com.condominio.patrones.state.EstadoPagado;
import com.condominio.patrones.state.EstadoParcial;
import com.condominio.patrones.state.EstadoPendiente;
import com.condominio.patrones.state.EstadoVencido;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;

@Entity
@Table(name = "cuota_mantenimiento", schema = "financiero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montoBase;
    private Double montoTotal;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Departamento departamento;

    @Transient
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstadoCuota estadoLogico;

    @PostLoad
    public void sincronizarEstadoLogico() {
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "PENDIENTE";
        }
        switch (this.estado.toUpperCase().trim()) {
            case "PAGADO" -> this.estadoLogico = new EstadoPagado();
            case "PARCIAL" -> this.estadoLogico = new EstadoParcial();
            case "VENCIDO" -> this.estadoLogico = new EstadoVencido();
            default -> this.estadoLogico = new EstadoPendiente();
        }
    }

    public EstadoCuota getEstadoLogico() {
        if (this.estadoLogico == null) {
            sincronizarEstadoLogico();
        }
        return this.estadoLogico;
    }

    public void setEstadoLogico(EstadoCuota nuevoEstado) {
        this.estadoLogico = nuevoEstado;
        if (nuevoEstado != null) {
            this.estado = nuevoEstado.getNombreEstado();
        }
    }

    public void setEstado(String estado) {
        this.estado = estado;
        sincronizarEstadoLogico();
    }

    public void pagar(double monto) {
        getEstadoLogico().pagar(this, monto);
        this.estado = getEstadoLogico().getNombreEstado();
    }

    public void aplicarMora() {
        getEstadoLogico().aplicarMora(this);
        this.estado = getEstadoLogico().getNombreEstado();
    }
}
