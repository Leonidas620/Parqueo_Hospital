-- Datos de prueba que reproducen el ejemplo de la orden. Ejecutar después de crear_base_datos.sql.
--
-- 1) Cuatro vehículos presentes, llegados ayer en este orden: W000001, P123456, B000002, F000003.
--    La lista enlazada (orden inverso de llegada) queda: F000003 -> B000002 -> P123456 -> W000001,
--    de modo que P123456 está en la tercera posición. Al registrar su salida, B000002 pasa a
--    apuntar a W000001. Todos llevan más de 24 horas, así que también aparecen en "Mostrar +24h".
-- 2) Un servicio ya cerrado de P123456 (09:40 a 12:05 = 3 horas cobradas) en un turno del administrador.
-- 3) Un abono del mes actual para B000002.

INSERT INTO vehiculos(chapa, tipo, color, marca, propietario, telefono) VALUES
    ('W000001', 'AUTO',     'Gris',  'Lada',   'Propietario Uno',    '55550001'),
    ('P123456', 'AUTO',     'Azul',  'Kia',    'Propietario Dos',    '55550002'),
    ('B000002', 'MOTO',     'Negro', 'Suzuki', 'Propietario Tres',   '55550003'),
    ('F000003', 'TRICICLO', 'Rojo',  'Bajaj',  'Propietario Cuatro', '55550004')
ON CONFLICT (chapa) DO NOTHING;

INSERT INTO servicios(chapa, hora_entrada, tarifa_aplicada, importe)
SELECT 'W000001', (CURRENT_DATE - 1) + TIME '09:15', tarifa_auto, 0 FROM tarifas WHERE id = 1
AND NOT EXISTS (SELECT 1 FROM servicios WHERE chapa = 'W000001' AND hora_salida IS NULL);
INSERT INTO servicios(chapa, hora_entrada, tarifa_aplicada, importe)
SELECT 'P123456', (CURRENT_DATE - 1) + TIME '09:40', tarifa_auto, 0 FROM tarifas WHERE id = 1
AND NOT EXISTS (SELECT 1 FROM servicios WHERE chapa = 'P123456' AND hora_salida IS NULL);
INSERT INTO servicios(chapa, hora_entrada, tarifa_aplicada, importe)
SELECT 'B000002', (CURRENT_DATE - 1) + TIME '10:05', tarifa_moto, 0 FROM tarifas WHERE id = 1
AND NOT EXISTS (SELECT 1 FROM servicios WHERE chapa = 'B000002' AND hora_salida IS NULL);
INSERT INTO servicios(chapa, hora_entrada, tarifa_aplicada, importe)
SELECT 'F000003', (CURRENT_DATE - 1) + TIME '10:30', tarifa_auto, 0 FROM tarifas WHERE id = 1
AND NOT EXISTS (SELECT 1 FROM servicios WHERE chapa = 'F000003' AND hora_salida IS NULL);

DO $$
DECLARE
    v_admin BIGINT;
    v_turno BIGINT;
BEGIN
    SELECT id INTO v_admin FROM cuentas WHERE usuario = 'admin';
    IF v_admin IS NULL OR EXISTS (
        SELECT 1 FROM servicios WHERE chapa = 'P123456' AND hora_entrada = (CURRENT_DATE - 2) + TIME '09:40'
    ) THEN
        RETURN;
    END IF;
    INSERT INTO turnos(fecha, parqueador_id, hora_inicio, hora_fin)
    VALUES (CURRENT_DATE - 2, v_admin, (CURRENT_DATE - 2) + TIME '09:00', (CURRENT_DATE - 2) + TIME '13:00')
    RETURNING id INTO v_turno;
    INSERT INTO servicios(chapa, hora_entrada, hora_salida, tarifa_aplicada, importe, turno_id)
    SELECT 'P123456', (CURRENT_DATE - 2) + TIME '09:40', (CURRENT_DATE - 2) + TIME '12:05',
           tarifa_auto, 3 * tarifa_auto, v_turno
    FROM tarifas WHERE id = 1;
    UPDATE turnos SET total = (SELECT COALESCE(SUM(importe), 0) FROM servicios WHERE turno_id = v_turno)
    WHERE id = v_turno;
END $$;

INSERT INTO abonos(chapa, mes, anio, importe)
VALUES ('B000002', EXTRACT(MONTH FROM CURRENT_DATE), EXTRACT(YEAR FROM CURRENT_DATE), 500)
ON CONFLICT (chapa, mes, anio) DO NOTHING;
