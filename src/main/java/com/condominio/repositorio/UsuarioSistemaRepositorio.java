package com.condominio.repositorio;

import com.condominio.modelo.seguridad.RolUsuario;
import com.condominio.modelo.seguridad.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioSistemaRepositorio extends JpaRepository<UsuarioSistema, Long> {
    Optional<UsuarioSistema> findByUsuario(String usuario);
    List<UsuarioSistema> findByRol(RolUsuario rol);
    boolean existsByUsuario(String usuario);
}
