package com.condominio.service.impl;

import com.condominio.dto.InmuebleDTO;
import com.condominio.modelo.inmuebles.AreaComun;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.inmuebles.Edificio;
import com.condominio.modelo.inmuebles.Torre;
import com.condominio.repositorio.AreaComunRepositorio;
import com.condominio.repositorio.DepartamentoRepositorio;
import com.condominio.repositorio.EdificioRepositorio;
import com.condominio.repositorio.TorreRepositorio;
import com.condominio.service.IInmuebleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InmuebleServiceImpl implements IInmuebleService {

    private final DepartamentoRepositorio departamentoRepositorio;
    private final EdificioRepositorio edificioRepositorio;
    private final TorreRepositorio torreRepositorio;
    private final AreaComunRepositorio areaComunRepositorio;

    @Autowired
    public InmuebleServiceImpl(
            DepartamentoRepositorio departamentoRepositorio,
            EdificioRepositorio edificioRepositorio,
            TorreRepositorio torreRepositorio,
            AreaComunRepositorio areaComunRepositorio) {
        this.departamentoRepositorio = departamentoRepositorio;
        this.edificioRepositorio = edificioRepositorio;
        this.torreRepositorio = torreRepositorio;
        this.areaComunRepositorio = areaComunRepositorio;
    }

    public InmuebleServiceImpl() {
        this(null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InmuebleDTO> listarInmuebles() {
        if (departamentoRepositorio == null) {
            return Collections.emptyList();
        }

        List<Departamento> departamentos = departamentoRepositorio.findAllConRelaciones();
        List<InmuebleDTO> lista = new ArrayList<>();

        for (Departamento d : departamentos) {
            String nomTorre = (d.getTorre() != null && d.getTorre().getNombre() != null)
                    ? d.getTorre().getNombre() : "Torre A";
            String nomEdificio = (d.getTorre() != null && d.getTorre().getEdificio() != null && d.getTorre().getEdificio().getNombre() != null)
                    ? d.getTorre().getEdificio().getNombre() : "Edificio Principal";

            lista.add(InmuebleDTO.builder()
                    .id(d.getId())
                    .numero(d.getNumero())
                    .piso(d.getPiso())
                    .metrosCuadrados(d.getMetrosCuadrados())
                    .nombreTorre(nomTorre)
                    .nombreEdificio(nomEdificio)
                    .build());
        }
        return lista;
    }

    @Override
    @Transactional
    public void registrarDepartamento(InmuebleDTO dto) {
        if (departamentoRepositorio == null || edificioRepositorio == null || torreRepositorio == null) {
            throw new IllegalStateException("Repositorios inmobiliarios no configurados");
        }
        validarInmuebleDTO(dto);

        String nomEdificio = (dto.getNombreEdificio() != null && !dto.getNombreEdificio().isBlank())
                ? dto.getNombreEdificio().trim() : "Edificio Principal";
        String nomTorre = (dto.getNombreTorre() != null && !dto.getNombreTorre().isBlank())
                ? dto.getNombreTorre().trim() : "Torre A";

        if (existeDepartamentoPorNumeroTorreYEdificio(dto.getNumero(), nomTorre, nomEdificio)) {
            throw new com.condominio.exception.DepartamentoDuplicadoException(dto.getNumero(), nomTorre, nomEdificio);
        }

        Torre torre = obtenerOCrearTorre(nomEdificio, nomTorre, dto.getPiso());

        Departamento nuevoDepto = Departamento.builder()
                .numero(dto.getNumero().trim())
                .piso(dto.getPiso())
                .metrosCuadrados(dto.getMetrosCuadrados())
                .numeroHabitantes(2)
                .torre(torre)
                .build();

        departamentoRepositorio.save(nuevoDepto);
        log.info("Departamento guardado exitosamente: N° {} en {} - {}",
                nuevoDepto.getNumero(), nomTorre, nomEdificio);
    }

    // Valida las reglas de entrada del DTO inmobiliario
    private void validarInmuebleDTO(InmuebleDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del inmueble no pueden ser nulos");
        }
        if (dto.getNumero() == null || dto.getNumero().isBlank()) {
            throw new IllegalArgumentException("El número de departamento es obligatorio");
        }
        if (dto.getPiso() == null || dto.getPiso() < 0) {
            throw new IllegalArgumentException("El número de piso no puede ser negativo");
        }
        if (dto.getMetrosCuadrados() == null || dto.getMetrosCuadrados() <= 0) {
            throw new IllegalArgumentException("El metraje en m² debe ser mayor a cero");
        }
    }

    // Obtiene o crea la estructura jerarquica de edificio y torre
    private Torre obtenerOCrearTorre(String nomEdificio, String nomTorre, int piso) {
        Edificio edificio = edificioRepositorio.findByNombre(nomEdificio)
                .orElseGet(() -> edificioRepositorio.save(Edificio.builder()
                        .nombre(nomEdificio)
                        .numeroPisos(Math.max(piso + 2, 5))
                        .build()));

        Torre torre = torreRepositorio.findByNombreAndEdificioId(nomTorre, edificio.getId())
                .orElseGet(() -> torreRepositorio.save(Torre.builder()
                        .nombre(nomTorre)
                        .numeroPisos(Math.max(piso + 2, 5))
                        .edificio(edificio)
                        .build()));

        if (torre.getEdificio() == null) {
            torre.setEdificio(edificio);
            torreRepositorio.save(torre);
        }
        return torre;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edificio> listarEdificios() {
        if (edificioRepositorio == null) return Collections.emptyList();
        return edificioRepositorio.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Torre> listarTorres() {
        if (torreRepositorio == null) return Collections.emptyList();
        return torreRepositorio.findAllConEdificio();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDepartamentoPorNumeroTorreYEdificio(String numero, String nombreTorre, String nombreEdificio) {
        if (departamentoRepositorio == null || numero == null) {
            return false;
        }
        String numLimpio = numero.trim();
        Optional<Departamento> opt = departamentoRepositorio.findByNumero(numLimpio);
        if (opt.isEmpty()) {
            return false;
        }
        Departamento d = opt.get();
        boolean torreCoincide = true;
        if (nombreTorre != null && !nombreTorre.isBlank()) {
            torreCoincide = d.getTorre() != null && d.getTorre().getNombre() != null
                    && d.getTorre().getNombre().equalsIgnoreCase(nombreTorre.trim());
        }

        boolean edifCoincide = true;
        if (nombreEdificio != null && !nombreEdificio.isBlank()) {
            edifCoincide = d.getTorre() != null && d.getTorre().getEdificio() != null
                    && d.getTorre().getEdificio().getNombre() != null
                    && d.getTorre().getEdificio().getNombre().equalsIgnoreCase(nombreEdificio.trim());
        }

        return torreCoincide && edifCoincide;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaComun> listarAreasComunes() {
        if (areaComunRepositorio == null) return Collections.emptyList();
        return areaComunRepositorio.findAll();
    }

    @Override
    @Transactional
    public AreaComun registrarAreaComun(AreaComun area) {
        if (area == null) throw new IllegalArgumentException("El área común no puede ser nula");
        if (areaComunRepositorio != null) return areaComunRepositorio.save(area);
        return area;
    }
}
