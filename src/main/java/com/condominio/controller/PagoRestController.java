package com.condominio.controller;

import com.condominio.dto.PagoRegistroRequest;
import com.condominio.patrones.facade.CobranzaFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos y Amortizaciones", description = "Endpoints para registro, conciliación y procesamiento de pagos de cuotas")
public class PagoRestController {

    private final CobranzaFachada fachada;

    @PostMapping
    @Operation(summary = "Registrar un pago", description = "Registra un pago para una cuota validando montos y estados con excepciones de negocio")
    public ResponseEntity<Map<String, Object>> registrarPago(@jakarta.validation.Valid @RequestBody PagoRegistroRequest request) {
        if (request == null || request.getIdCuota() == null || request.getMonto() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Los campos idCuota y monto son obligatorios"));
        }

        boolean exito = fachada.procesarPagoConRepositorio(request.getIdCuota(), request.getMonto(), request.getCodigoOperacion());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "exito", exito,
                "mensaje", "Pago procesado y persistido correctamente",
                "idCuota", request.getIdCuota(),
                "montoAbonado", request.getMonto()
        ));
    }
}
