package com.condominio.controller;

import com.condominio.modelo.operaciones.Incidente;
import com.condominio.modelo.operaciones.ReservaArea;
import com.condominio.patrones.facade.CobranzaFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operaciones")
@RequiredArgsConstructor
@Tag(name = "Operaciones Condominales", description = "Endpoints para consulta de incidencias y reservaciones de áreas comunes")
public class OperacionRestController {

    private final CobranzaFachada fachada;

    @GetMapping("/incidentes")
    @Operation(summary = "Listar todos los incidentes", description = "Retorna el historial de averías e incidencias reportadas en el condominio")
    public ResponseEntity<List<Incidente>> listarIncidentes() {
        return ResponseEntity.ok(fachada.listarIncidentes());
    }

    @GetMapping("/reservas")
    @Operation(summary = "Listar todas las reservas", description = "Retorna las reservas de áreas comunes con su horario y estado")
    public ResponseEntity<List<Map<String, Object>>> listarReservas() {
        List<ReservaArea> reservas = fachada.listarReservas();
        List<Map<String, Object>> dto = reservas.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fechaReserva", r.getFechaReserva());
            map.put("horaInicio", r.getHoraInicio());
            map.put("horaFin", r.getHoraFin());
            map.put("estado", r.getEstado());
            map.put("areaComun", r.getAreaComun() != null ? r.getAreaComun().getNombre() : "Área Común");
            map.put("propietario", r.getPropietario() != null ? r.getPropietario().getNombre() : "Vecino");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }
}
