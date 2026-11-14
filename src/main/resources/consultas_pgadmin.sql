-- SISTEMA DE COBRANZA VECINAL - CONSULTAS PRINCIPALES PARA PGADMIN 4
-- Base de datos: cobranza_vecinal_db
-- Motor: PostgreSQL 18

-- 1. DIAGNOSTICO: Conteo general de registros por esquema y tabla
SELECT 'seguridad.usuario_sistema' AS entidad, count(*) AS total_registros FROM seguridad.usuario_sistema
UNION ALL
SELECT 'personas.usuario', count(*) FROM personas.usuario
UNION ALL
SELECT 'inmuebles.departamento', count(*) FROM inmuebles.departamento
UNION ALL
SELECT 'inmuebles.area_comun', count(*) FROM inmuebles.area_comun
UNION ALL
SELECT 'financiero.cuota_mantenimiento', count(*) FROM financiero.cuota_mantenimiento
UNION ALL
SELECT 'financiero.pago', count(*) FROM financiero.pago
UNION ALL
SELECT 'financiero.gasto_comun', count(*) FROM financiero.gasto_comun
UNION ALL
SELECT 'operaciones.reserva_area', count(*) FROM operaciones.reserva_area
UNION ALL
SELECT 'operaciones.comunicado', count(*) FROM operaciones.comunicado
UNION ALL
SELECT 'operaciones.incidente', count(*) FROM operaciones.incidente
UNION ALL
SELECT 'personas.visita', count(*) FROM personas.visita
ORDER BY total_registros DESC;


-- 2. MODULO FINANCIERO: Estado de cobranzas y cuotas de mantenimiento
-- 2.1 Listado integral de cuotas con datos del departamento y propietario
SELECT 
    c.id AS cuota_id,
    d.numero AS depto,
    t.nombre AS torre,
    e.nombre AS edificio,
    u.nombre || ' ' || u.apellidos AS propietario,
    c.concepto,
    c.monto_final,
    c.fecha_vencimiento,
    c.estado
FROM financiero.cuota_mantenimiento c
JOIN inmuebles.departamento d ON c.departamento_id = d.id
JOIN inmuebles.torre t ON d.torre_id = t.id
JOIN inmuebles.edificio e ON t.edificio_id = e.id
LEFT JOIN personas.propietario p ON d.propietario_id = p.id
LEFT JOIN personas.usuario u ON p.id = u.id
ORDER BY d.numero;

-- 2.2 Reporte de morosidad: Departamentos con cuotas pendientes o parciales
SELECT 
    d.numero AS departamento,
    t.nombre AS torre,
    u.nombre || ' ' || u.apellidos AS propietario,
    u.telefono,
    c.monto_final AS deuda,
    c.fecha_vencimiento,
    c.estado
FROM financiero.cuota_mantenimiento c
JOIN inmuebles.departamento d ON c.departamento_id = d.id
JOIN inmuebles.torre t ON d.torre_id = t.id
LEFT JOIN personas.propietario p ON d.propietario_id = p.id
LEFT JOIN personas.usuario u ON p.id = u.id
WHERE c.estado IN ('PENDIENTE', 'PARCIAL', 'VENCIDO')
ORDER BY c.fecha_vencimiento ASC;

-- 2.3 Historial de pagos procesados con metodo y fecha
SELECT 
    p.id AS pago_id,
    d.numero AS depto,
    p.monto,
    p.metodo_pago,
    p.fecha_pago
FROM financiero.pago p
JOIN inmuebles.departamento d ON p.departamento_id = d.id
ORDER BY p.fecha_pago DESC;

-- 2.4 Resumen de gastos comunes del condominio
SELECT 
    g.id,
    g.fecha,
    g.descripcion,
    g.monto,
    c.nombre AS condominio
FROM financiero.gasto_comun g
JOIN inmuebles.condominio c ON g.condominio_id = c.id
ORDER BY g.fecha DESC;


-- 3. MODULO DE INMUEBLES Y PERSONAS: Directorio vecinal
-- 3.1 Directorio general: Departamentos, metraje, propietarios e inquilinos
SELECT 
    d.numero AS depto,
    d.piso,
    d.metros_cuadrados,
    t.nombre AS torre,
    u_prop.nombre || ' ' || u_prop.apellidos AS propietario,
    u_prop.telefono AS tel_propietario,
    COALESCE(u_inq.nombre || ' ' || u_inq.apellidos, 'Sin inquilino') AS inquilino,
    u_inq.telefono AS tel_inquilino
FROM inmuebles.departamento d
JOIN inmuebles.torre t ON d.torre_id = t.id
LEFT JOIN personas.propietario prop ON d.propietario_id = prop.id
LEFT JOIN personas.usuario u_prop ON prop.id = u_prop.id
LEFT JOIN personas.inquilino inq ON d.inquilino_id = inq.id
LEFT JOIN personas.usuario u_inq ON inq.id = u_inq.id
ORDER BY d.piso, d.numero;

-- 3.2 Registro de mascotas por departamento
SELECT 
    m.nombre AS nombre_mascota,
    m.especie,
    m.raza,
    d.numero AS depto
FROM personas.mascota m
JOIN inmuebles.departamento d ON m.departamento_id = d.id;

-- 3.3 Registro de vehiculos autorizados
SELECT 
    v.placa,
    v.marca,
    v.modelo,
    v.color,
    d.numero AS depto
FROM personas.vehiculo v
JOIN inmuebles.departamento d ON v.departamento_id = d.id;

-- 3.4 Control de garita: Registro de visitas recibidas
SELECT 
    v.id,
    v.nombre AS visitante,
    v.dni,
    v.motivo,
    v.fecha_ingreso,
    COALESCE(TO_CHAR(v.fecha_salida, 'YYYY-MM-DD HH24:MI:SS'), 'EN EDIFICIO') AS fecha_salida,
    d.numero AS departamento_destino
FROM personas.visita v
JOIN inmuebles.departamento d ON v.departamento_id = d.id
ORDER BY v.fecha_ingreso DESC;


-- 4. MODULO DE OPERACIONES: Reservas, comunicados e incidentes
-- 4.1 Reservas de areas comunes con horarios y solicitante
SELECT 
    r.id AS reserva_id,
    a.nombre AS area_comun,
    r.fecha_reserva,
    r.hora_inicio,
    r.hora_fin,
    r.estado,
    u.nombre || ' ' || u.apellidos AS reservado_por
FROM operaciones.reserva_area r
JOIN inmuebles.area_comun a ON r.area_comun_id = a.id
LEFT JOIN personas.propietario p ON r.propietario_id = p.id
LEFT JOIN personas.usuario u ON p.id = u.id
ORDER BY r.fecha_reserva, r.hora_inicio;

-- 4.2 Mural de comunicados vigentes para la comunidad
SELECT 
    c.id,
    c.titulo,
    c.categoria,
    c.autor,
    c.fecha_publicacion,
    c.contenido
FROM operaciones.comunicado c
ORDER BY c.fecha_publicacion DESC;

-- 4.3 Bitacora de incidentes y estado de atencion
SELECT 
    i.id,
    i.fecha,
    i.descripcion,
    i.estado,
    i.reportado_por
FROM operaciones.incidente i
ORDER BY i.fecha DESC;


-- 5. MODULO DE SEGURIDAD: Cuentas de usuario y roles de acceso
SELECT 
    id,
    username,
    rol,
    nombre_completo,
    CASE WHEN activo THEN 'ACTIVO' ELSE 'INACTIVO' END AS estado_cuenta
FROM seguridad.usuario_sistema
ORDER BY rol, username;


-- 6. CONSULTAS ANALITICAS Y ESTADISTICAS PARA ADMINISTRACION
-- 6.1 Total recaudado vs. Total pendiente de cobro
SELECT 
    COUNT(*) AS total_cuotas,
    SUM(CASE WHEN estado = 'PAGADO' THEN monto_final ELSE 0 END) AS total_recaudado,
    SUM(CASE WHEN estado IN ('PENDIENTE', 'PARCIAL') THEN monto_final ELSE 0 END) AS total_por_cobrar,
    ROUND((SUM(CASE WHEN estado = 'PAGADO' THEN monto_final ELSE 0 END)::numeric / 
           NULLIF(SUM(monto_final), 0)::numeric) * 100, 2) AS porcentaje_efectividad_cobranza
FROM financiero.cuota_mantenimiento;

-- 6.2 Resumen de cuotas por torre
SELECT 
    t.nombre AS torre,
    COUNT(c.id) AS numero_cuotas,
    SUM(c.monto_final) AS monto_total_emitido,
    SUM(CASE WHEN c.estado = 'PAGADO' THEN 1 ELSE 0 END) AS pagadas,
    SUM(CASE WHEN c.estado <> 'PAGADO' THEN 1 ELSE 0 END) AS pendientes
FROM financiero.cuota_mantenimiento c
JOIN inmuebles.departamento d ON c.departamento_id = d.id
JOIN inmuebles.torre t ON d.torre_id = t.id
GROUP BY t.nombre
ORDER BY t.nombre;
