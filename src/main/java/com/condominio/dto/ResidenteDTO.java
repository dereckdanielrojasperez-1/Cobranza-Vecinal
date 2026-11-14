package com.condominio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Objeto de transferencia de datos para la administracion de residentes (Propietarios e Inquilinos).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidenteDTO {

    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    private String telefono;

    private String correoElectronico;

    @NotBlank(message = "El tipo de residente es obligatorio")
    private String tipoUsuario;

    private Long departamentoId;

    private String numeroDepartamento;

    private String nombreTorre;

    private String nombreEdificio;

    public String getNombreCompleto() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }
}
