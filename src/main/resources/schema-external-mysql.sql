-- SISTEMA DE COBRANZA VECINAL - DDL MYSQL / MARIADB
-- Version: 2.2.0 Corporativa
-- Motores compatibles: MySQL 8.0+, MariaDB 10.6+

CREATE DATABASE IF NOT EXISTS cobranza_vecinal_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cobranza_vecinal_db;

-- 1. TABLA: usuario_sistema
CREATE TABLE IF NOT EXISTS usuario_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    nombre_completo VARCHAR(120),
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_user_username (username)
) ENGINE=InnoDB;

-- 2. TABLA: usuario (Base de Personas)
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    correo VARCHAR(120),
    telefono VARCHAR(30),
    fecha_registro DATE,
    INDEX idx_user_dni (dni),
    INDEX idx_user_nombre (nombre)
) ENGINE=InnoDB;

-- 3. TABLA: propietario (Herencia JOINED)
CREATE TABLE IF NOT EXISTS propietario (
    id BIGINT PRIMARY KEY,
    banco VARCHAR(100),
    cuenta_bancaria VARCHAR(100),
    CONSTRAINT fk_prop_usuario FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. TABLA: inquilino (Herencia JOINED)
CREATE TABLE IF NOT EXISTS inquilino (
    id BIGINT PRIMARY KEY,
    fecha_inicio_contrato DATE,
    fecha_fin_contrato DATE,
    CONSTRAINT fk_inq_usuario FOREIGN KEY (id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. TABLA: condominio
CREATE TABLE IF NOT EXISTS condominio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(250),
    ruc VARCHAR(20)
) ENGINE=InnoDB;

-- 6. TABLA: edificio
CREATE TABLE IF NOT EXISTS edificio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    numero_pisos INT DEFAULT 1,
    condominio_id BIGINT,
    CONSTRAINT fk_edif_condo FOREIGN KEY (condominio_id) REFERENCES condominio(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. TABLA: torre
CREATE TABLE IF NOT EXISTS torre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    numero_pisos INT DEFAULT 1,
    edificio_id BIGINT,
    CONSTRAINT fk_torre_edif FOREIGN KEY (edificio_id) REFERENCES edificio(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. TABLA: departamento
CREATE TABLE IF NOT EXISTS departamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    piso INT,
    metros_cuadrados DOUBLE DEFAULT 80.0,
    numero_habitantes INT DEFAULT 1,
    torre_id BIGINT,
    propietario_id BIGINT,
    inquilino_id BIGINT,
    CONSTRAINT fk_depto_torre FOREIGN KEY (torre_id) REFERENCES torre(id) ON DELETE SET NULL,
    CONSTRAINT fk_depto_prop FOREIGN KEY (propietario_id) REFERENCES propietario(id) ON DELETE SET NULL,
    CONSTRAINT fk_depto_inq FOREIGN KEY (inquilino_id) REFERENCES inquilino(id) ON DELETE SET NULL,
    INDEX idx_depto_num (numero),
    INDEX idx_depto_torre (torre_id)
) ENGINE=InnoDB;

-- 9. TABLA: area_comun
CREATE TABLE IF NOT EXISTS area_comun (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT DEFAULT 20,
    costo_reserva DOUBLE DEFAULT 0.0,
    condominio_id BIGINT,
    CONSTRAINT fk_area_condo FOREIGN KEY (condominio_id) REFERENCES condominio(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 10. TABLA: estacionamiento
CREATE TABLE IF NOT EXISTS estacionamiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    departamento_id BIGINT,
    CONSTRAINT fk_estac_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 11. TABLA: deposito
CREATE TABLE IF NOT EXISTS deposito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    departamento_id BIGINT,
    CONSTRAINT fk_depo_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 12. TABLA: mascota
CREATE TABLE IF NOT EXISTS mascota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50),
    raza VARCHAR(50),
    departamento_id BIGINT,
    CONSTRAINT fk_masc_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. TABLA: vehiculo
CREATE TABLE IF NOT EXISTS vehiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    color VARCHAR(30),
    departamento_id BIGINT,
    CONSTRAINT fk_veh_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. TABLA: visita
CREATE TABLE IF NOT EXISTS visita (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    fecha_ingreso DATETIME,
    fecha_salida DATETIME,
    motivo VARCHAR(200),
    departamento_id BIGINT,
    CONSTRAINT fk_visita_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL,
    INDEX idx_vis_dni (dni)
) ENGINE=InnoDB;

-- 15. TABLA: reserva_area
CREATE TABLE IF NOT EXISTS reserva_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(50) DEFAULT 'CONFIRMADA',
    propietario_id BIGINT,
    area_comun_id BIGINT,
    CONSTRAINT fk_res_prop FOREIGN KEY (propietario_id) REFERENCES propietario(id) ON DELETE SET NULL,
    CONSTRAINT fk_res_area FOREIGN KEY (area_comun_id) REFERENCES area_comun(id) ON DELETE CASCADE,
    INDEX idx_res_fecha (fecha_reserva)
) ENGINE=InnoDB;

-- 16. TABLA: incidente
CREATE TABLE IF NOT EXISTS incidente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(500) NOT NULL,
    fecha DATE,
    estado VARCHAR(50) DEFAULT 'REGISTRADO',
    reportado_por VARCHAR(150)
) ENGINE=InnoDB;

-- 17. TABLA: comunicado
CREATE TABLE IF NOT EXISTS comunicado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_publicacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    categoria VARCHAR(50) DEFAULT 'GENERAL',
    autor VARCHAR(100) DEFAULT 'Administración'
) ENGINE=InnoDB;

-- 18. TABLA: proveedor
CREATE TABLE IF NOT EXISTS proveedor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(200) NOT NULL,
    rubro VARCHAR(100),
    telefono VARCHAR(30),
    email VARCHAR(120)
) ENGINE=InnoDB;

-- 19. TABLA: cuota_mantenimiento
CREATE TABLE IF NOT EXISTS cuota_mantenimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto_base DOUBLE NOT NULL,
    monto_final DOUBLE NOT NULL,
    concepto VARCHAR(255) DEFAULT 'Cuota Ordinaria Mantenimiento',
    fecha_emision DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    departamento_id BIGINT,
    CONSTRAINT fk_cuota_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE CASCADE,
    INDEX idx_cuota_est (estado),
    INDEX idx_cuota_venc (fecha_vencimiento)
) ENGINE=InnoDB;

-- 20. TABLA: pago
CREATE TABLE IF NOT EXISTS pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto DOUBLE NOT NULL,
    fecha_pago DATE,
    metodo_pago VARCHAR(50) DEFAULT 'TRANSFERENCIA',
    departamento_id BIGINT,
    CONSTRAINT fk_pago_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 21. TABLAS: gasto_comun y subtipos
CREATE TABLE IF NOT EXISTS gasto_comun (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto DOUBLE NOT NULL,
    fecha DATE,
    descripcion VARCHAR(250),
    condominio_id BIGINT,
    CONSTRAINT fk_gasto_condo FOREIGN KEY (condominio_id) REFERENCES condominio(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gasto_agua (
    id BIGINT PRIMARY KEY,
    lectura_actual DOUBLE,
    lectura_anterior DOUBLE,
    CONSTRAINT fk_gagua_base FOREIGN KEY (id) REFERENCES gasto_comun(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gasto_luz (
    id BIGINT PRIMARY KEY,
    consumo_kwh DOUBLE,
    tarifa_unitaria DOUBLE,
    CONSTRAINT fk_gluz_base FOREIGN KEY (id) REFERENCES gasto_comun(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gasto_limpieza (
    id BIGINT PRIMARY KEY,
    area_atendida VARCHAR(150),
    personal_asignado VARCHAR(150),
    CONSTRAINT fk_glimp_base FOREIGN KEY (id) REFERENCES gasto_comun(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 22. TABLA: multa
CREATE TABLE IF NOT EXISTS multa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    motivo VARCHAR(255) NOT NULL,
    monto DOUBLE NOT NULL,
    fecha DATE,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    departamento_id BIGINT,
    CONSTRAINT fk_multa_depto FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE CASCADE
) ENGINE=InnoDB;
