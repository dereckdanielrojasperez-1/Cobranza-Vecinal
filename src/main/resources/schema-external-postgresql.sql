-- SISTEMA DE COBRANZA VECINAL - DDL POSTGRESQL MULTI-ESQUEMA
-- Version: 2.2.0 Corporativa
-- Motores compatibles: PostgreSQL 13, 14, 15, 16+ / pgAdmin 4

-- 1. CREACION DE ESQUEMAS MODULARES
CREATE SCHEMA IF NOT EXISTS seguridad;
CREATE SCHEMA IF NOT EXISTS personas;
CREATE SCHEMA IF NOT EXISTS inmuebles;
CREATE SCHEMA IF NOT EXISTS operaciones;
CREATE SCHEMA IF NOT EXISTS financiero;

-- ESQUEMA: seguridad
CREATE TABLE IF NOT EXISTS seguridad.usuario_sistema (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    nombre_completo VARCHAR(120),
    activo BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_usuario_sistema_username ON seguridad.usuario_sistema (username);

-- ESQUEMA: personas
CREATE TABLE IF NOT EXISTS personas.usuario (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    correo VARCHAR(120),
    telefono VARCHAR(30),
    fecha_registro DATE DEFAULT CURRENT_DATE
);

CREATE INDEX IF NOT EXISTS idx_usuario_dni ON personas.usuario (dni);
CREATE INDEX IF NOT EXISTS idx_usuario_nombre ON personas.usuario (nombre);

CREATE TABLE IF NOT EXISTS personas.propietario (
    id BIGINT PRIMARY KEY REFERENCES personas.usuario(id) ON DELETE CASCADE,
    banco VARCHAR(100),
    cuenta_bancaria VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS personas.inquilino (
    id BIGINT PRIMARY KEY REFERENCES personas.usuario(id) ON DELETE CASCADE,
    fecha_inicio_contrato DATE,
    fecha_fin_contrato DATE
);

-- ESQUEMA: inmuebles
CREATE TABLE IF NOT EXISTS inmuebles.condominio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(250),
    ruc VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS inmuebles.edificio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    numero_pisos INT DEFAULT 1,
    condominio_id BIGINT REFERENCES inmuebles.condominio(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inmuebles.torre (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    numero_pisos INT DEFAULT 1,
    edificio_id BIGINT REFERENCES inmuebles.edificio(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inmuebles.departamento (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    piso INT,
    metros_cuadrados DOUBLE PRECISION DEFAULT 80.0,
    numero_habitantes INT DEFAULT 1,
    torre_id BIGINT REFERENCES inmuebles.torre(id) ON DELETE SET NULL,
    propietario_id BIGINT REFERENCES personas.propietario(id) ON DELETE SET NULL,
    inquilino_id BIGINT REFERENCES personas.inquilino(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_departamento_numero ON inmuebles.departamento (numero);
CREATE INDEX IF NOT EXISTS idx_departamento_torre ON inmuebles.departamento (torre_id);
CREATE INDEX IF NOT EXISTS idx_departamento_propietario ON inmuebles.departamento (propietario_id);
CREATE INDEX IF NOT EXISTS idx_departamento_inquilino ON inmuebles.departamento (inquilino_id);

CREATE TABLE IF NOT EXISTS inmuebles.area_comun (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT DEFAULT 20,
    costo_reserva DOUBLE PRECISION DEFAULT 0.0,
    condominio_id BIGINT REFERENCES inmuebles.condominio(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inmuebles.estacionamiento (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS inmuebles.deposito (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE SET NULL
);

-- Tablas dependientes de departamento en personas
CREATE TABLE IF NOT EXISTS personas.mascota (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50),
    raza VARCHAR(50),
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS personas.vehiculo (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    color VARCHAR(30),
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS personas.visita (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    fecha_ingreso TIMESTAMP,
    fecha_salida TIMESTAMP,
    motivo VARCHAR(200),
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_visita_dni ON personas.visita (dni);
CREATE INDEX IF NOT EXISTS idx_visita_depto ON personas.visita (departamento_id);

-- ESQUEMA: operaciones
CREATE TABLE IF NOT EXISTS operaciones.reserva_area (
    id BIGSERIAL PRIMARY KEY,
    fecha_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(50) DEFAULT 'CONFIRMADA',
    propietario_id BIGINT REFERENCES personas.propietario(id) ON DELETE SET NULL,
    area_comun_id BIGINT REFERENCES inmuebles.area_comun(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reserva_fecha ON operaciones.reserva_area (fecha_reserva);
CREATE INDEX IF NOT EXISTS idx_reserva_area ON operaciones.reserva_area (area_comun_id);

CREATE TABLE IF NOT EXISTS operaciones.incidente (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(500) NOT NULL,
    fecha DATE DEFAULT CURRENT_DATE,
    estado VARCHAR(50) DEFAULT 'REGISTRADO',
    reportado_por VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS operaciones.comunicado (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    categoria VARCHAR(50) DEFAULT 'GENERAL',
    autor VARCHAR(100) DEFAULT 'Administración'
);

CREATE TABLE IF NOT EXISTS operaciones.proveedor (
    id BIGSERIAL PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(200) NOT NULL,
    rubro VARCHAR(100),
    telefono VARCHAR(30),
    email VARCHAR(120)
);

-- ESQUEMA: financiero
CREATE TABLE IF NOT EXISTS financiero.cuota_mantenimiento (
    id BIGSERIAL PRIMARY KEY,
    monto_base DOUBLE PRECISION NOT NULL,
    monto_final DOUBLE PRECISION NOT NULL,
    concepto VARCHAR(255) DEFAULT 'Cuota Ordinaria Mantenimiento',
    fecha_emision DATE DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cuota_depto ON financiero.cuota_mantenimiento (departamento_id);
CREATE INDEX IF NOT EXISTS idx_cuota_estado ON financiero.cuota_mantenimiento (estado);
CREATE INDEX IF NOT EXISTS idx_cuota_vencimiento ON financiero.cuota_mantenimiento (fecha_vencimiento);

CREATE TABLE IF NOT EXISTS financiero.pago (
    id BIGSERIAL PRIMARY KEY,
    monto DOUBLE PRECISION NOT NULL,
    fecha_pago DATE DEFAULT CURRENT_DATE,
    metodo_pago VARCHAR(50) DEFAULT 'TRANSFERENCIA',
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_pago_depto ON financiero.pago (departamento_id);

CREATE TABLE IF NOT EXISTS financiero.gasto_comun (
    id BIGSERIAL PRIMARY KEY,
    monto DOUBLE PRECISION NOT NULL,
    fecha DATE DEFAULT CURRENT_DATE,
    descripcion VARCHAR(250),
    condominio_id BIGINT REFERENCES inmuebles.condominio(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS financiero.gasto_agua (
    id BIGINT PRIMARY KEY REFERENCES financiero.gasto_comun(id) ON DELETE CASCADE,
    lectura_actual DOUBLE PRECISION,
    lectura_anterior DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS financiero.gasto_luz (
    id BIGINT PRIMARY KEY REFERENCES financiero.gasto_comun(id) ON DELETE CASCADE,
    consumo_kwh DOUBLE PRECISION,
    tarifa_unitaria DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS financiero.gasto_limpieza (
    id BIGINT PRIMARY KEY REFERENCES financiero.gasto_comun(id) ON DELETE CASCADE,
    area_atendida VARCHAR(150),
    personal_asignado VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS financiero.multa (
    id BIGSERIAL PRIMARY KEY,
    motivo VARCHAR(255) NOT NULL,
    monto DOUBLE PRECISION NOT NULL,
    fecha DATE DEFAULT CURRENT_DATE,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    departamento_id BIGINT REFERENCES inmuebles.departamento(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_multa_depto ON financiero.multa (departamento_id);
