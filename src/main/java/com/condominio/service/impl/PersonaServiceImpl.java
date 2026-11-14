package com.condominio.service.impl;

import com.condominio.dto.ResidenteDTO;
import com.condominio.modelo.inmuebles.Departamento;
import com.condominio.modelo.personas.Inquilino;
import com.condominio.modelo.personas.Mascota;
import com.condominio.modelo.personas.Propietario;
import com.condominio.modelo.personas.Vehiculo;
import com.condominio.repositorio.DepartamentoRepositorio;
import com.condominio.repositorio.InquilinoRepositorio;
import com.condominio.repositorio.MascotaRepositorio;
import com.condominio.repositorio.PropietarioRepositorio;
import com.condominio.repositorio.UsuarioRepositorio;
import com.condominio.repositorio.VehiculoRepositorio;
import com.condominio.service.IPersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PersonaServiceImpl implements IPersonaService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PropietarioRepositorio propietarioRepositorio;
    private final InquilinoRepositorio inquilinoRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final MascotaRepositorio mascotaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    @Autowired
    public PersonaServiceImpl(
            UsuarioRepositorio usuarioRepositorio,
            PropietarioRepositorio propietarioRepositorio,
            InquilinoRepositorio inquilinoRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            MascotaRepositorio mascotaRepositorio,
            VehiculoRepositorio vehiculoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.propietarioRepositorio = propietarioRepositorio;
        this.inquilinoRepositorio = inquilinoRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.mascotaRepositorio = mascotaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    public PersonaServiceImpl() {
        this(null, null, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidenteDTO> listarResidentes() {
        if (usuarioRepositorio == null) {
            return Collections.emptyList();
        }

        Map<Long, DatosInmuebleResidente> mapaPropietarios = new HashMap<>();
        Map<Long, DatosInmuebleResidente> mapaInquilinos = new HashMap<>();
        poblarMapasInmuebles(mapaPropietarios, mapaInquilinos);

        List<ResidenteDTO> lista = new ArrayList<>();
        mapearPropietariosADTO(lista, mapaPropietarios);
        mapearInquilinosADTO(lista, mapaInquilinos);
        return lista;
    }

    // Pobla las relaciones entre residentes e inmuebles
    private void poblarMapasInmuebles(Map<Long, DatosInmuebleResidente> mapaPropietarios,
                                     Map<Long, DatosInmuebleResidente> mapaInquilinos) {
        if (departamentoRepositorio == null) {
            return;
        }
        List<Departamento> deptos = departamentoRepositorio.findAllConRelaciones();
        for (Departamento d : deptos) {
            String nomTorre = (d.getTorre() != null && d.getTorre().getNombre() != null)
                    ? d.getTorre().getNombre() : "Torre A";
            String nomEdificio = (d.getTorre() != null && d.getTorre().getEdificio() != null && d.getTorre().getEdificio().getNombre() != null)
                    ? d.getTorre().getEdificio().getNombre() : "Edificio Los Sauces";

            DatosInmuebleResidente datos = new DatosInmuebleResidente(d.getId(), d.getNumero(), nomTorre, nomEdificio);

            if (d.getPropietario() != null && d.getPropietario().getId() != null) {
                mapaPropietarios.put(d.getPropietario().getId(), datos);
            }
            if (d.getInquilinoActual() != null && d.getInquilinoActual().getId() != null) {
                mapaInquilinos.put(d.getInquilinoActual().getId(), datos);
            }
        }
    }

    // Mapea entidades de Propietario hacia DTOs
    private void mapearPropietariosADTO(List<ResidenteDTO> lista, Map<Long, DatosInmuebleResidente> mapa) {
        if (propietarioRepositorio == null) return;
        for (Propietario p : propietarioRepositorio.findAll()) {
            DatosInmuebleResidente d = mapa.get(p.getId());
            lista.add(ResidenteDTO.builder()
                    .id(p.getId())
                    .dni(p.getDni())
                    .nombres(p.getNombre())
                    .apellidos(p.getApellidos() != null ? p.getApellidos() : "")
                    .telefono(p.getTelefono())
                    .correoElectronico(p.getCorreo())
                    .tipoUsuario("PROPIETARIO")
                    .departamentoId(d != null ? d.departamentoId : null)
                    .numeroDepartamento(d != null ? d.numeroDepartamento : "Sin asignar")
                    .nombreTorre(d != null ? d.nombreTorre : "Torre A")
                    .nombreEdificio(d != null ? d.nombreEdificio : "Edificio Los Sauces")
                    .build());
        }
    }

    // Mapea entidades de Inquilino hacia DTOs
    private void mapearInquilinosADTO(List<ResidenteDTO> lista, Map<Long, DatosInmuebleResidente> mapa) {
        if (inquilinoRepositorio == null) return;
        for (Inquilino inq : inquilinoRepositorio.findAll()) {
            DatosInmuebleResidente d = mapa.get(inq.getId());
            lista.add(ResidenteDTO.builder()
                    .id(inq.getId())
                    .dni(inq.getDni())
                    .nombres(inq.getNombre())
                    .apellidos(inq.getApellidos() != null ? inq.getApellidos() : "")
                    .telefono(inq.getTelefono())
                    .correoElectronico(inq.getCorreo())
                    .tipoUsuario("INQUILINO")
                    .departamentoId(d != null ? d.departamentoId : null)
                    .numeroDepartamento(d != null ? d.numeroDepartamento : "Sin asignar")
                    .nombreTorre(d != null ? d.nombreTorre : "Torre A")
                    .nombreEdificio(d != null ? d.nombreEdificio : "Edificio Los Sauces")
                    .build());
        }
    }

    @Override
    @Transactional
    public void registrarResidente(ResidenteDTO dto) {
        validarDatosRegistroResidente(dto);

        String dniLimpio = dto.getDni().trim();
        if (usuarioRepositorio.existsByDni(dniLimpio)) {
            throw new IllegalArgumentException("Ya existe un residente registrado con el DNI: " + dniLimpio);
        }

        String tipo = dto.getTipoUsuario().trim().toUpperCase();
        if ("PROPIETARIO".equals(tipo)) {
            registrarPropietarioConDepartamento(dto, dniLimpio);
        } else if ("INQUILINO".equals(tipo)) {
            registrarInquilinoConDepartamento(dto, dniLimpio);
        } else {
            throw new IllegalArgumentException("Tipo de residente no válido. Debe ser PROPIETARIO o INQUILINO.");
        }
    }

    // Valida las reglas de campos obligatorios para registrar residente
    private void validarDatosRegistroResidente(ResidenteDTO dto) {
        if (usuarioRepositorio == null) {
            throw new IllegalStateException("Repositorio de usuarios no disponible en el servicio");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del residente no pueden ser nulos");
        }
        if (dto.getDni() == null || dto.getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        if (dto.getNombres() == null || dto.getNombres().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.getApellidos() == null || dto.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        if (dto.getTipoUsuario() == null || dto.getTipoUsuario().isBlank()) {
            throw new IllegalArgumentException("El tipo de usuario es obligatorio (PROPIETARIO o INQUILINO)");
        }
    }

    // Registra la entidad Propietario y su vinculacion con departamento
    private void registrarPropietarioConDepartamento(ResidenteDTO dto, String dniLimpio) {
        Propietario propietario = Propietario.builder()
                .dni(dniLimpio)
                .nombre(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                .correo(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim() : null)
                .fechaRegistro(LocalDate.now())
                .build();

        if (propietarioRepositorio != null) {
            propietario = propietarioRepositorio.save(propietario);
        } else {
            propietario = (Propietario) usuarioRepositorio.save(propietario);
        }

        if (dto.getDepartamentoId() != null && departamentoRepositorio != null) {
            Departamento depto = departamentoRepositorio.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado con ID: " + dto.getDepartamentoId()));
            depto.setPropietario(propietario);
            departamentoRepositorio.save(depto);
            log.info("Propietario {} asignado al departamento N° {}", propietario.getNombre(), depto.getNumero());
        }

        log.info("Propietario registrado exitosamente: DNI={}, Nombre={} {}",
                propietario.getDni(), propietario.getNombre(), propietario.getApellidos());
    }

    // Registra la entidad Inquilino y su vinculacion con departamento
    private void registrarInquilinoConDepartamento(ResidenteDTO dto, String dniLimpio) {
        Inquilino inquilino = Inquilino.builder()
                .dni(dniLimpio)
                .nombre(dto.getNombres().trim())
                .apellidos(dto.getApellidos().trim())
                .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                .correo(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim() : null)
                .fechaRegistro(LocalDate.now())
                .fechaInicioContrato(LocalDate.now())
                .fechaFinContrato(LocalDate.now().plusYears(1))
                .build();

        if (inquilinoRepositorio != null) {
            inquilino = inquilinoRepositorio.save(inquilino);
        } else {
            inquilino = (Inquilino) usuarioRepositorio.save(inquilino);
        }

        if (dto.getDepartamentoId() != null && departamentoRepositorio != null) {
            Departamento depto = departamentoRepositorio.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado con ID: " + dto.getDepartamentoId()));
            depto.setInquilinoActual(inquilino);
            departamentoRepositorio.save(depto);
            log.info("Inquilino {} asignado al departamento N° {}", inquilino.getNombre(), depto.getNumero());
        }

        log.info("Inquilino registrado exitosamente: DNI={}, Nombre={} {}",
                inquilino.getDni(), inquilino.getNombre(), inquilino.getApellidos());
    }

    // Estructura interna inmutable para mapear datos inmobiliarios de residentes
    private static class DatosInmuebleResidente {
        final Long departamentoId;
        final String numeroDepartamento;
        final String nombreTorre;
        final String nombreEdificio;

        DatosInmuebleResidente(Long departamentoId, String numeroDepartamento, String nombreTorre, String nombreEdificio) {
            this.departamentoId = departamentoId;
            this.numeroDepartamento = numeroDepartamento;
            this.nombreTorre = nombreTorre;
            this.nombreEdificio = nombreEdificio;
        }
    }

    @Override
    @Transactional
    public void actualizarResidente(ResidenteDTO dto) {
        if (dto == null || dto.getDni() == null || dto.getDni().isBlank()) {
            throw new IllegalArgumentException("DNI inválido para actualización");
        }
        String dni = dto.getDni().trim();
        boolean actualizado = false;

        if (propietarioRepositorio != null) {
            Optional<Propietario> propOpt = propietarioRepositorio.findByDni(dni);
            if (propOpt.isPresent()) {
                Propietario p = propOpt.get();
                if (dto.getNombres() != null && !dto.getNombres().isBlank()) p.setNombre(dto.getNombres().trim());
                if (dto.getApellidos() != null) p.setApellidos(dto.getApellidos().trim());
                if (dto.getTelefono() != null) p.setTelefono(dto.getTelefono().trim());
                if (dto.getCorreoElectronico() != null) p.setCorreo(dto.getCorreoElectronico().trim());
                propietarioRepositorio.save(p);

                if (departamentoRepositorio != null && dto.getDepartamentoId() != null) {
                    List<Departamento> prevs = departamentoRepositorio.findByPropietarioId(p.getId());
                    for (Departamento prev : prevs) {
                        if (!prev.getId().equals(dto.getDepartamentoId())) {
                            prev.setPropietario(null);
                            departamentoRepositorio.save(prev);
                        }
                    }
                    departamentoRepositorio.findById(dto.getDepartamentoId()).ifPresent(depto -> {
                        depto.setPropietario(p);
                        departamentoRepositorio.save(depto);
                    });
                }
                actualizado = true;
                log.info("Propietario actualizado: DNI={}", dni);
            }
        }

        if (!actualizado && inquilinoRepositorio != null) {
            Optional<Inquilino> inqOpt = inquilinoRepositorio.findByDni(dni);
            if (inqOpt.isPresent()) {
                Inquilino inq = inqOpt.get();
                if (dto.getNombres() != null && !dto.getNombres().isBlank()) inq.setNombre(dto.getNombres().trim());
                if (dto.getApellidos() != null) inq.setApellidos(dto.getApellidos().trim());
                if (dto.getTelefono() != null) inq.setTelefono(dto.getTelefono().trim());
                if (dto.getCorreoElectronico() != null) inq.setCorreo(dto.getCorreoElectronico().trim());
                inquilinoRepositorio.save(inq);

                if (departamentoRepositorio != null && dto.getDepartamentoId() != null) {
                    List<Departamento> prevs = departamentoRepositorio.findByInquilinoActualId(inq.getId());
                    for (Departamento prev : prevs) {
                        if (!prev.getId().equals(dto.getDepartamentoId())) {
                            prev.setInquilinoActual(null);
                            departamentoRepositorio.save(prev);
                        }
                    }
                    departamentoRepositorio.findById(dto.getDepartamentoId()).ifPresent(depto -> {
                        depto.setInquilinoActual(inq);
                        departamentoRepositorio.save(depto);
                    });
                }
                actualizado = true;
                log.info("Inquilino actualizado: DNI={}", dni);
            }
        }

        if (!actualizado) {
            throw new IllegalArgumentException("No se encontró ningún residente con el DNI: " + dni);
        }
    }

    @Override
    @Transactional
    public void eliminarResidentePorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("DNI no puede estar vacío");
        }
        String dniLimpio = dni.trim();
        boolean eliminado = false;

        if (propietarioRepositorio != null) {
            Optional<Propietario> propOpt = propietarioRepositorio.findByDni(dniLimpio);
            if (propOpt.isPresent()) {
                Propietario p = propOpt.get();
                if (departamentoRepositorio != null) {
                    List<Departamento> deptos = departamentoRepositorio.findByPropietarioId(p.getId());
                    for (Departamento d : deptos) {
                        d.setPropietario(null);
                        departamentoRepositorio.save(d);
                    }
                }
                propietarioRepositorio.delete(p);
                eliminado = true;
                log.info("Propietario eliminado del sistema: DNI={}", dniLimpio);
            }
        }

        if (!eliminado && inquilinoRepositorio != null) {
            Optional<Inquilino> inqOpt = inquilinoRepositorio.findByDni(dniLimpio);
            if (inqOpt.isPresent()) {
                Inquilino inq = inqOpt.get();
                if (departamentoRepositorio != null) {
                    List<Departamento> deptos = departamentoRepositorio.findByInquilinoActualId(inq.getId());
                    for (Departamento d : deptos) {
                        d.setInquilinoActual(null);
                        departamentoRepositorio.save(d);
                    }
                }
                inquilinoRepositorio.delete(inq);
                eliminado = true;
                log.info("Inquilino eliminado del sistema: DNI={}", dniLimpio);
            }
        }

        if (!eliminado) {
            throw new IllegalArgumentException("No se encontró ningún residente con el DNI: " + dniLimpio);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> listarMascotasPorDepartamento(Long departamentoId) {
        if (mascotaRepositorio == null || departamentoId == null) return Collections.emptyList();
        return mascotaRepositorio.findByDepartamentoId(departamentoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehiculo> listarVehiculosPorDepartamento(Long departamentoId) {
        if (vehiculoRepositorio == null || departamentoId == null) return Collections.emptyList();
        return vehiculoRepositorio.findByDepartamentoId(departamentoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeResidentePorDni(String dni) {
        if (dni == null || dni.isBlank()) return false;
        String dniLimpio = dni.trim();
        if (usuarioRepositorio != null && usuarioRepositorio.existsByDni(dniLimpio)) {
            return true;
        }
        if (propietarioRepositorio != null && propietarioRepositorio.existsByDni(dniLimpio)) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePropietarioEnDepartamento(Long departamentoId) {
        if (departamentoId == null || departamentoRepositorio == null) return false;
        return departamentoRepositorio.findById(departamentoId)
                .map(d -> d.getPropietario() != null)
                .orElse(false);
    }
}
