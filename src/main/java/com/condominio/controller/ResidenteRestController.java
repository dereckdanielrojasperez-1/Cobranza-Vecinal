package com.condominio.controller;

import com.condominio.dto.ResidenteDTO;
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
@RequestMapping("/api/residentes")
@RequiredArgsConstructor
@Tag(name = "Residentes", description = "Endpoints para consulta y registro de propietarios e inquilinos")
public class ResidenteRestController {

    private final CobranzaFachada fachada;

    @GetMapping
    @Operation(summary = "Listar todos los residentes", description = "Retorna el padrón de propietarios e inquilinos registrados")
    public ResponseEntity<List<ResidenteDTO>> listarResidentes() {
        return ResponseEntity.ok(fachada.listarResidentes());
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo residente", description = "Registra un propietario o inquilino asociándolo a su departamento")
    public ResponseEntity<Map<String, Object>> registrarResidente(@Valid @RequestBody ResidenteDTO dto) {
        fachada.registrarResidente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "exito", true,
                "mensaje", "Residente registrado exitosamente",
                "dni", dto.getDni(),
                "nombres", dto.getNombres()
        ));
    }
}
