package com.condominio.controller;

import com.condominio.dto.InmuebleDTO;
import com.condominio.patrones.facade.CobranzaFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inmuebles")
@RequiredArgsConstructor
@Tag(name = "Inmuebles y Departamentos", description = "Endpoints para consulta y registro de unidades departamentales")
public class InmuebleRestController {

    private final CobranzaFachada fachada;

    @GetMapping
    @Operation(summary = "Listar todos los departamentos", description = "Retorna el catálogo de unidades inmobiliarias registradas")
    public ResponseEntity<List<InmuebleDTO>> listarInmuebles() {
        return ResponseEntity.ok(fachada.listarInmuebles());
    }

    @PostMapping
    @Operation(summary = "Registrar un departamento", description = "Registra una nueva unidad inmobiliaria validando que no esté duplicada en la misma torre")
    public ResponseEntity<Map<String, Object>> registrarInmueble(@Valid @RequestBody InmuebleDTO dto) {
        fachada.registrarDepartamento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "exito", true,
                "mensaje", "Departamento registrado exitosamente",
                "numero", dto.getNumero(),
                "torre", dto.getNombreTorre() != null ? dto.getNombreTorre() : "Torre A"
        ));
    }
}
