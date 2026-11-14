package com.condominio.controller;

import com.condominio.dto.CuotaDTO;
import com.condominio.patrones.facade.CobranzaFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
@Tag(name = "Cuotas de Mantenimiento", description = "Endpoints para consulta y facturación masiva de cuotas")
public class CuotaRestController {

    private final CobranzaFachada fachada;

    @GetMapping
    @Operation(summary = "Listar todas las cuotas", description = "Retorna el catálogo completo de cuotas de mantenimiento")
    public ResponseEntity<List<CuotaDTO>> listarCuotas() {
        return ResponseEntity.ok(fachada.listarCuotas());
    }

    @PostMapping("/generar-masivas")
    @Operation(summary = "Generación masiva de cuotas (Patrón Prototype)", description = "Clona la cuota base para departamentos sin cuota abierta en el periodo")
    public ResponseEntity<List<CuotaDTO>> generarCuotasMasivas(@RequestParam String periodo, @RequestParam double montoBase) {
        List<CuotaDTO> generadas = fachada.generarCuotasMasivas(periodo, montoBase);
        return ResponseEntity.status(HttpStatus.CREATED).body(generadas);
    }
}
