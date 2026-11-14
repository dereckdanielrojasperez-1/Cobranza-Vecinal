-- Pagos registrados
INSERT INTO financiero.pago (id, monto, fecha_pago, metodo_pago, departamento_id) VALUES
(1, 150.00, '2026-11-05', 'TRANSFERENCIA', 1),
(2, 175.00, '2026-11-06', 'DEPOSITO', 2),
(3, 160.00, '2026-11-07', 'YAPE', 3),
(4, 90.00, '2026-11-08', 'PLIN', 4),
(5, 190.00, '2026-11-09', 'TRANSFERENCIA', 6)
ON CONFLICT (id) DO NOTHING;

-- Reservas de areas comunes
INSERT INTO operaciones.reserva_area (id, fecha_reserva, hora_inicio, hora_fin, estado, propietario_id, area_comun_id) VALUES
(1, '2026-11-15', '14:00:00', '18:00:00', 'CONFIRMADA', 1, 1),
(2, '2026-11-16', '19:00:00', '23:00:00', 'CONFIRMADA', 2, 2),
(3, '2026-11-17', '10:00:00', '13:00:00', 'CONFIRMADA', 3, 3)
ON CONFLICT (id) DO NOTHING;

-- Incidentes
INSERT INTO operaciones.incidente (id, descripcion, fecha, estado, reportado_por) VALUES
(1, 'Ruido excesivo reportado en torre A despues de las 23:00', '2026-11-08', 'RESUELTO', 'Conserje de Turno'),
(2, 'Fuga leve de agua en tuberia del sotano 1', '2026-11-10', 'EN_PROCESO', 'Carlos Rojas (Depto 101)')
ON CONFLICT (id) DO NOTHING;

-- Gastos Comunes
INSERT INTO financiero.gasto_comun (id, monto, fecha, descripcion, condominio_id) VALUES
(1, 1200.00, '2026-11-01', 'Servicio de Agua Potable Sedapal - Factura Noviembre', 1),
(2, 1850.50, '2026-11-01', 'Servicio Electrico Luz del Sur Areas Comunes', 1),
(3, 950.00, '2026-11-02', 'Insumos y Limpieza Brilla SAC', 1)
ON CONFLICT (id) DO NOTHING;

-- Mascotas
INSERT INTO personas.mascota (id, nombre, especie, raza, departamento_id) VALUES
(1, 'Bobby', 'Perro', 'Golden Retriever', 1),
(2, 'Michi', 'Gato', 'Siames', 3)
ON CONFLICT (id) DO NOTHING;

-- Vehiculos
INSERT INTO personas.vehiculo (id, placa, marca, modelo, color, departamento_id) VALUES
(1, 'ABC-123', 'Toyota', 'Corolla', 'Plata', 1),
(2, 'XYZ-789', 'Hyundai', 'Tucson', 'Negro', 2)
ON CONFLICT (id) DO NOTHING;
