package com.condominio.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Controlador global de excepciones para la API REST.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MontoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> manejarMontoInvalido(MontoInvalidoException ex, HttpServletRequest req) {
        log.warn("Monto inválido en API: {}", ex.getMessage());
        return construirRespuestaError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(CuotaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarCuotaNoEncontrada(CuotaNoEncontradaException ex, HttpServletRequest req) {
        log.warn("Cuota no encontrada en API: {}", ex.getMessage());
        return construirRespuestaError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler({CuotaYaPagadaException.class, ReservaSolapadaException.class, DepartamentoDuplicadoException.class})
    public ResponseEntity<Map<String, Object>> manejarConflictos(BusinessException ex, HttpServletRequest req) {
        log.warn("Conflicto de negocio en API [{}]: {}", ex.getCodigoError(), ex.getMessage());
        return construirRespuestaError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> manejarBusinessGeneral(BusinessException ex, HttpServletRequest req) {
        log.warn("Excepción de regla de negocio en API [{}]: {}", ex.getCodigoError(), ex.getMessage());
        return construirRespuestaError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Parámetro inválido en API: {}", ex.getMessage());
        return construirRespuestaError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalState(IllegalStateException ex, HttpServletRequest req) {
        log.warn("Estado ilícito en API: {}", ex.getMessage());
        return construirRespuestaError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionDTO(org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest req) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validación fallida");
        log.warn("Error de validación DTO en API: {}", mensaje);
        return construirRespuestaError(HttpStatus.BAD_REQUEST, "Bad Request (Validation Failed)", mensaje, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex, HttpServletRequest req) {
        log.error("Excepción no controlada en API: {}", ex.getMessage(), ex);
        return construirRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), req.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> construirRespuestaError(HttpStatus estado, String error, String mensaje, String path) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", estado.value());
        cuerpo.put("error", error);
        cuerpo.put("mensaje", mensaje != null ? mensaje : "Sin detalle");
        cuerpo.put("path", path != null ? path : "/api");
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
