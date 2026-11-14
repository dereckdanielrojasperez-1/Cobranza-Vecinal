package com.condominio.service;

import com.condominio.dto.CuotaDTO;
import com.condominio.modelo.financiero.CuotaMantenimiento;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.HistorialPago;
import com.condominio.modelo.financiero.Multa;
import com.condominio.modelo.inmuebles.Departamento;

import java.time.LocalDate;
import java.util.List;

public interface ICobranzaService {
    List<CuotaDTO> listarCuotas();
    CuotaMantenimiento generarCuotaMensual(Long cuotaId, Departamento departamento, Double montoBase, LocalDate fechaVencimiento);
    double calcularMoraCuota(CuotaMantenimiento cuota, LocalDate fechaEvaluacion);
    HistorialPago registrarPago(Long pagoId, Long historialId, CuotaMantenimiento cuota, Double montoAbonado, String metodoPago);
    boolean procesarPagoConRepositorio(Long idCuota, double monto, String codigoOperacion);
    List<Multa> listarMultas();
    Multa registrarMulta(Multa multa);
    List<GastoComun> listarGastosComunes();
    GastoComun registrarGastoComun(GastoComun gasto);

    // Verifica si un departamento ya tiene una cuota abierta (estado no PAGADO) para el periodo indicado.
    boolean tieneCuotaAbiertaEnPeriodo(Long departamentoId, String periodo);
}
