-- SISTEMA DE COBRANZA VECINAL - DATA SEED PARA BASE DE DATOS EXTERNA
-- Version: 2.2.0 Corporativa

-- 1. USUARIOS DEL SISTEMA (SEGURIDAD)
INSERT INTO seguridad.usuario_sistema (username, password, rol, nombre_completo, activo)
VALUES 
('admin', 'admin123', 'ADMINISTRADOR', 'Administrador General del Condominio', true),
('conserje', 'conserje123', 'CONSERJE', 'Operador de Garita y Accesos', true),
('residente', 'residente123', 'RESIDENTE', 'Residente Propietario Depto 101', true)
ON CONFLICT (username) DO NOTHING;

-- 2. CONDOMINIO
INSERT INTO inmuebles.condominio (id, nombre, direccion, ruc)
VALUES (1, 'Condominio Residencial Las Praderas', 'Av. Las Palmas 742, Santiago de Surco, Lima', '20601234567')
ON CONFLICT (id) DO NOTHING;

-- 3. EDIFICIOS
INSERT INTO inmuebles.edificio (id, nombre, numero_pisos, condominio_id)
VALUES 
(1, 'Edificio Los Sauces', 10, 1),
(2, 'Edificio Las Palmeras', 15, 1),
(3, 'Edificio Los Álamos', 8, 1)
ON CONFLICT (id) DO NOTHING;

-- 4. TORRES
INSERT INTO inmuebles.torre (id, nombre, numero_pisos, edificio_id)
VALUES 
(1, 'Torre A', 10, 1),
(2, 'Torre B', 10, 1),
(3, 'Torre Principal', 15, 2),
(4, 'Torre Central', 8, 3)
ON CONFLICT (id) DO NOTHING;

-- 5. PERSONAS (USUARIOS, PROPIETARIOS, INQUILINOS)
INSERT INTO personas.usuario (id, dni, nombre, apellidos, correo, telefono, fecha_registro)
VALUES 
(1, '45892134', 'Carlos', 'Rojas Mendoza', 'carlos.rojas@email.com', '987654321', '2026-01-15'),
(2, '84736291', 'Ana', 'Rosas Del Campo', 'ana.rosas@email.com', '998877665', '2026-02-10'),
(3, '38475629', 'Miguel Ángel', 'Benítez Vega', 'miguel.benitez@email.com', '945678123', '2026-02-15'),
(4, '75967704', 'Eduardo', 'Gutiérrez Castro', 'eduardo.gutierrez@email.com', '912345678', '2026-03-01'),
(5, '62839401', 'Roberto', 'Chang Alvarado', 'roberto.chang@email.com', '976543210', '2026-03-10'),
(6, '45678912', 'Javier', 'Paredes Quispe', 'javier.paredes@email.com', '912345678', '2026-04-01'),
(7, '56789023', 'Lucía', 'Fernández Vega', 'lucia.fernandez@email.com', '923456789', '2026-04-05'),
(8, '67890134', 'Diego', 'Castro Huamán', 'diego.castro@email.com', '934567890', '2026-04-10')
ON CONFLICT (id) DO NOTHING;

INSERT INTO personas.propietario (id, banco, cuenta_bancaria)
VALUES 
(1, 'BCP', '191-00234567-0-12'),
(2, 'BBVA', '0011-0123-0200345678'),
(3, 'Interbank', '200-3001234567'),
(4, 'Scotiabank', '000-1234567-89'),
(5, 'BCP', '191-88990011-0-45')
ON CONFLICT (id) DO NOTHING;

INSERT INTO personas.inquilino (id, fecha_inicio_contrato, fecha_fin_contrato)
VALUES 
(6, '2026-01-01', '2026-12-31'),
(7, '2026-03-01', '2027-02-28'),
(8, '2026-05-01', '2027-04-30')
ON CONFLICT (id) DO NOTHING;

-- 6. DEPARTAMENTOS
INSERT INTO inmuebles.departamento (id, numero, piso, metros_cuadrados, numero_habitantes, torre_id, propietario_id, inquilino_id)
VALUES 
(1, '101', 1, 85.5, 3, 1, 1, 6),
(2, '102', 1, 75.0, 2, 1, 2, NULL),
(3, '201', 2, 90.0, 4, 1, 3, 7),
(4, '202', 2, 85.5, 2, 2, 4, NULL),
(5, '301', 3, 110.0, 5, 1, 5, 8),
(6, '302', 3, 95.0, 3, 2, 2, NULL),
(7, '401', 4, 95.0, 3, 1, 4, NULL)
ON CONFLICT (id) DO NOTHING;

-- 7. AREAS COMUNES
INSERT INTO inmuebles.area_comun (id, nombre, capacidad, costo_reserva, condominio_id)
VALUES 
(1, 'Zona BBQ y Parrillas', 15, 30.0, 1),
(2, 'Salón Social de Eventos', 30, 80.0, 1),
(3, 'Piscina Panorámica', 20, 20.0, 1),
(4, 'Cancha Multideportiva', 12, 15.0, 1)
ON CONFLICT (id) DO NOTHING;

-- 8. PROVEEDORES
INSERT INTO operaciones.proveedor (id, ruc, razon_social, rubro, telefono, email)
VALUES 
(1, '20100123456', 'Servicios de Limpieza Brilla SAC', 'Limpieza y Aseo', '01-4455667', 'contacto@brillasac.pe'),
(2, '20200234567', 'Seguridad Integral Protek SAC', 'Seguridad y Vigilancia', '01-2233445', 'operaciones@protek.pe'),
(3, '20300345678', 'Mantenimiento de Ascensores Elevatech', 'Mantenimiento Electromecánico', '01-7788990', 'soporte@elevatech.pe')
ON CONFLICT (id) DO NOTHING;

-- 9. COMUNICADOS
INSERT INTO operaciones.comunicado (id, titulo, contenido, fecha_publicacion, categoria, autor)
VALUES 
(1, 'Asamblea General Ordinaria de Propietarios', 'Se convoca a todos los propietarios a la Asamblea General Ordinaria a llevarse a cabo el próximo viernes a las 19:30 hrs en el Salón Social.', '2026-11-01 10:00:00', 'ASAMBLEA', 'Junta Directiva'),
(2, 'Mantenimiento Preventivo de Bombas de Agua', 'Se informa a los residentes que el corte programado por mantenimiento preventivo se realizará el día martes de 09:00 a 12:00 hrs.', '2026-11-05 08:30:00', 'MANTENIMIENTO', 'Administración')
ON CONFLICT (id) DO NOTHING;

-- 10. VISITAS
INSERT INTO personas.visita (id, nombre, dni, fecha_ingreso, fecha_salida, motivo, departamento_id)
VALUES 
(1, 'Marcos Peña Ramos', '44123890', '2026-11-09 08:30:00', NULL, 'Visita Familiar', 1),
(2, 'Sonia Morales Dávila', '71298453', '2026-11-09 09:15:00', '2026-11-09 11:30:00', 'Entrega de Documentos', 2),
(3, 'Ramiro Valdivia Rios', '10984726', '2026-11-09 10:00:00', NULL, 'Servicio Técnico Internet', 3)
ON CONFLICT (id) DO NOTHING;

-- 11. CUOTAS DE MANTENIMIENTO
INSERT INTO financiero.cuota_mantenimiento (id, monto_base, monto_final, concepto, fecha_emision, fecha_vencimiento, estado, departamento_id)
VALUES 
(1001, 150.00, 150.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PAGADO', 1),
(1002, 175.00, 175.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PAGADO', 2),
(1003, 160.00, 160.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PAGADO', 3),
(1004, 180.00, 180.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PARCIAL', 4),
(1005, 210.00, 210.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PENDIENTE', 5),
(1006, 190.00, 190.00, 'Cuota Ordinaria Mantenimiento Noviembre 2026', '2026-11-01', '2026-11-20', 'PAGADO', 6)
ON CONFLICT (id) DO NOTHING;
