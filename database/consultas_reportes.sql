-- 1. Vehículos que permanecen dentro del parqueo.
SELECT s.chapa, v.tipo, v.marca, s.hora_entrada
FROM servicios s JOIN vehiculos v ON v.chapa = s.chapa
WHERE s.hora_salida IS NULL
ORDER BY s.hora_entrada;

-- 2. Vehículos presentes durante más de 24 horas.
SELECT s.chapa, v.tipo, s.hora_entrada,
       LOCALTIMESTAMP - s.hora_entrada AS tiempo_en_parqueo
FROM servicios s JOIN vehiculos v ON v.chapa = s.chapa
WHERE s.hora_salida IS NULL AND s.hora_entrada <= LOCALTIMESTAMP - INTERVAL '24 hours';

-- 3. Servicios cobrados en un intervalo (por defecto, el mes actual; sustituya las fechas si lo necesita).
SELECT s.id, s.chapa, s.hora_entrada, s.hora_salida, s.tarifa_aplicada,
       s.importe, c.nombre AS parqueador
FROM servicios s
JOIN turnos t ON t.id = s.turno_id
JOIN cuentas c ON c.id = t.parqueador_id
WHERE s.hora_salida >= date_trunc('month', CURRENT_DATE)
  AND s.hora_salida <  date_trunc('month', CURRENT_DATE) + INTERVAL '1 month'
ORDER BY s.hora_salida;

-- 4. Total recaudado por turno (suma de los servicios cobrados en cada turno).
SELECT t.id AS turno, t.fecha, c.nombre AS parqueador,
       COUNT(s.id) AS servicios, COALESCE(SUM(s.importe), 0) AS total
FROM turnos t JOIN cuentas c ON c.id = t.parqueador_id
LEFT JOIN servicios s ON s.turno_id = t.id
GROUP BY t.id, t.fecha, c.nombre
ORDER BY t.fecha DESC, t.id DESC;

-- 5. Abonos vigentes en el mes actual.
SELECT a.id, a.chapa, a.mes, a.anio, a.importe
FROM abonos a
WHERE make_date(a.anio, a.mes, 1) = date_trunc('month', CURRENT_DATE)::date
ORDER BY a.chapa;

-- 6. Recaudación diaria por fecha de salida.
SELECT s.hora_salida::date AS fecha, COUNT(*) AS servicios,
       SUM(s.importe) AS total
FROM servicios s
WHERE s.hora_salida IS NOT NULL
GROUP BY s.hora_salida::date
ORDER BY fecha DESC;

-- 7. Espacios ocupados frente a la capacidad configurada (autos y triciclos comparten espacios).
SELECT COUNT(*) FILTER (WHERE v.tipo IN ('AUTO', 'TRICICLO')) AS autos_y_triciclos,
       k.capacidad_autos,
       COUNT(*) FILTER (WHERE v.tipo = 'MOTO') AS motos,
       k.capacidad_motos
FROM capacidad k
LEFT JOIN servicios s ON s.hora_salida IS NULL
LEFT JOIN vehiculos v ON v.chapa = s.chapa
GROUP BY k.capacidad_autos, k.capacidad_motos;
