-- Migración de problemas de condicionales a la base de datos
-- Ejecutar después de asegurar que existe el tema 'conditionals'

-- Insertar el tema si no existe
INSERT INTO temas (id, nombre, descripcion) 
VALUES (1, 'conditionals', 'Problemas de estructuras condicionales')
ON CONFLICT (id) DO NOTHING;

-- Insertar problemas de condicionales
INSERT INTO problemas (id, titulo, descripcion, codigo_inicial, solucion_correcta, test_cases_json, tema_id, dificultad) VALUES

-- FÁCIL
(1, 'Verificador de Edad', 'Escribe una función que determine si una persona es mayor de edad (18 años o más).', 
'def verificar_edad(edad):
  # Tu código aquí
  pass', 
'def verificar_edad(edad):
  if edad >= 18:
    return "Mayor de edad"
  else:
    return "Menor de edad"',
'[{"input": "18", "expectedOutput": "Mayor de edad"}, {"input": "17", "expectedOutput": "Menor de edad"}, {"input": "25", "expectedOutput": "Mayor de edad"}, {"input": "16", "expectedOutput": "Menor de edad"}, {"input": "65", "expectedOutput": "Mayor de edad"}]',
1, 'EASY'),

(2, 'Número Positivo, Negativo o Cero', 'Escribe una función que determine si un número es positivo, negativo o cero.', 
'def verificar_signo(numero):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "10", "expectedOutput": "Positivo"}, {"input": "-1", "expectedOutput": "Negativo"}, {"input": "0", "expectedOutput": "Cero"}, {"input": "100", "expectedOutput": "Positivo"}, {"input": "-50", "expectedOutput": "Negativo"}]',
1, 'EASY'),

(3, 'Vocal o Consonante Simple', 'Dada una letra minúscula, determina si es una vocal (''a'', ''e'', ''i'', ''o'', ''u'').', 
'def es_vocal_simple(letra):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "''e''", "expectedOutput": "Vocal"}, {"input": "''z''", "expectedOutput": "Consonante"}, {"input": "''a''", "expectedOutput": "Vocal"}, {"input": "''i''", "expectedOutput": "Vocal"}, {"input": "''m''", "expectedOutput": "Consonante"}]',
1, 'EASY'),

(4, 'Acceso Permitido', 'Si la contraseña es "1234", permite el acceso. De lo contrario, deniégalo.', 
'def verificar_acceso(clave):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "\"1234\"", "expectedOutput": "Acceso Permitido"}, {"input": "\"0000\"", "expectedOutput": "Acceso Denegado"}, {"input": "\"1234\"", "expectedOutput": "Acceso Permitido"}, {"input": "\"admin\"", "expectedOutput": "Acceso Denegado"}, {"input": "\"password\"", "expectedOutput": "Acceso Denegado"}]',
1, 'EASY'),

(5, 'Fin de Semana', 'Dado un día de la semana (''lunes''...''domingo''), indica si es fin de semana.', 
'def es_fin_de_semana(dia):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "''domingo''", "expectedOutput": "Fin de semana"}, {"input": "''miercoles''", "expectedOutput": "Día de semana"}, {"input": "''sabado''", "expectedOutput": "Fin de semana"}, {"input": "''lunes''", "expectedOutput": "Día de semana"}, {"input": "''viernes''", "expectedOutput": "Día de semana"}]',
1, 'EASY'),

(6, 'Número Par o Impar', 'Escribe una función que determine si un número es par o impar.', 
'def par_o_impar(numero):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "100", "expectedOutput": "Par"}, {"input": "33", "expectedOutput": "Impar"}, {"input": "2", "expectedOutput": "Par"}, {"input": "7", "expectedOutput": "Impar"}, {"input": "0", "expectedOutput": "Par"}]',
1, 'EASY'),

(7, 'Semáforo Simple', 'Dado un color (''rojo'', ''verde''), indica la acción (''Detenerse'', ''Avanzar'').', 
'def accion_semaforo_simple(color):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "''rojo''", "expectedOutput": "Detenerse"}, {"input": "''verde''", "expectedOutput": "Avanzar"}, {"input": "''rojo''", "expectedOutput": "Detenerse"}, {"input": "''verde''", "expectedOutput": "Avanzar"}, {"input": "''rojo''", "expectedOutput": "Detenerse"}]',
1, 'EASY'),

(8, 'Mayor que 10', 'Verifica si un número es mayor que 10.', 
'def mayor_que_diez(num):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "11", "expectedOutput": "Mayor que 10"}, {"input": "10", "expectedOutput": "No es mayor que 10"}, {"input": "15", "expectedOutput": "Mayor que 10"}, {"input": "5", "expectedOutput": "No es mayor que 10"}, {"input": "100", "expectedOutput": "Mayor que 10"}]',
1, 'EASY'),

(9, 'Aprobado o Reprobado', 'Si una calificación es 5 o más, está aprobado. Sino, reprobado (sobre 10).', 
'def estado_calificacion(nota):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "5", "expectedOutput": "Aprobado"}, {"input": "4.9", "expectedOutput": "Reprobado"}, {"input": "7", "expectedOutput": "Aprobado"}, {"input": "3", "expectedOutput": "Reprobado"}, {"input": "10", "expectedOutput": "Aprobado"}]',
1, 'EASY'),

(10, 'Saludo por Hora', 'Si la hora (0-23) es antes de las 12, saluda "Buenos días". Sino, "Buenas tardes/noches".', 
'def saludo_horario(hora):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "8", "expectedOutput": "Buenos días"}, {"input": "20", "expectedOutput": "Buenas tardes/noches"}, {"input": "0", "expectedOutput": "Buenos días"}, {"input": "12", "expectedOutput": "Buenas tardes/noches"}, {"input": "11", "expectedOutput": "Buenos días"}]',
1, 'EASY'),

(11, 'Bebida Permitida', 'Si la edad es 21 o más, se permite "Cerveza". Sino, "Jugo". (Ejemplo simplificado)', 
'def bebida_permitida(edad):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "21", "expectedOutput": "Cerveza"}, {"input": "20", "expectedOutput": "Jugo"}, {"input": "25", "expectedOutput": "Cerveza"}, {"input": "18", "expectedOutput": "Jugo"}, {"input": "30", "expectedOutput": "Cerveza"}]',
1, 'EASY'),

(12, 'Descuento Simple', 'Si el precio es mayor a $50, aplica un 10% de descuento y devuelve "Precio con descuento: XX.X". Sino, "Precio sin descuento: XX.X".', 
'def aplicar_descuento_simple(precio):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "50.01", "expectedOutput": "Precio con descuento: 45.01"}, {"input": "50", "expectedOutput": "Precio sin descuento: 50.0"}, {"input": "60", "expectedOutput": "Precio con descuento: 54.0"}, {"input": "40", "expectedOutput": "Precio sin descuento: 40.0"}, {"input": "100", "expectedOutput": "Precio con descuento: 90.0"}]',
1, 'EASY'),

-- INTERMEDIO
(13, 'Calculadora de Grados Completa', 'Crea una función que convierta una calificación numérica (0-100) a una letra (A, B, C, D, F). 90-100: A, 80-89: B, 70-79: C, 60-69: D, <60: F.', 
'def calcular_grado(calificacion):
  # Tu código aquí
  pass', 
'def calcular_grado(calificacion):
  if calificacion >= 90:
    return "A"
  elif calificacion >= 80:
    return "B"
  elif calificacion >= 70:
    return "C"
  elif calificacion >= 60:
    return "D"
  else:
    return "F"',
'[{"input": "90", "expectedOutput": "A"}, {"input": "80", "expectedOutput": "B"}, {"input": "59", "expectedOutput": "F"}, {"input": "75", "expectedOutput": "C"}, {"input": "65", "expectedOutput": "D"}]',
1, 'INTERMEDIATE'),

(14, 'Descuento por Compra Detallado', 'Si una compra es mayor a $100, aplica 10% de descuento. Si es mayor a $200, aplica 15%. Devuelve el precio final.', 
'def calcular_descuento_detallado(monto):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "100", "expectedOutput": "100.0"}, {"input": "150", "expectedOutput": "135.0"}, {"input": "201", "expectedOutput": "170.85"}, {"input": "50", "expectedOutput": "50.0"}, {"input": "250", "expectedOutput": "212.5"}]',
1, 'INTERMEDIATE'),

(15, 'Tipo de Ángulo Completo', 'Dado un ángulo en grados (0-360), clasifícalo como Agudo (<90), Recto (90), Obtuso (90-180), Llano (180), Cóncavo (>180 y <360) o Completo (360).', 
'def tipo_angulo_completo(angulo):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "90", "expectedOutput": "Recto"}, {"input": "150", "expectedOutput": "Obtuso"}, {"input": "360", "expectedOutput": "Completo"}, {"input": "45", "expectedOutput": "Agudo"}, {"input": "270", "expectedOutput": "Cóncavo"}]',
1, 'INTERMEDIATE'),

(16, 'Máximo de Tres Números sin max()', 'Encuentra el mayor de tres números dados sin usar la función max().', 
'def maximo_de_tres(a, b, c):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "1,2,3", "expectedOutput": "3"}, {"input": "3,2,1", "expectedOutput": "3"}, {"input": "5,5,1", "expectedOutput": "5"}, {"input": "10,5,8", "expectedOutput": "10"}, {"input": "2,2,2", "expectedOutput": "2"}]',
1, 'INTERMEDIATE'),

(17, 'Clasificación de IMC', 'Calcula el IMC (peso/altura^2) y clasifícalo: <18.5 Bajo peso, 18.5-24.9 Normal, 25-29.9 Sobrepeso, >=30 Obesidad.', 
'def clasificar_imc(peso, altura):
  # IMC = peso / (altura ** 2)
  # Devuelve string "Clasificación (IMC: XX.XX)"
  pass', 
NULL,
'[{"input": "50,1.70", "expectedOutput": "Bajo peso (IMC: 17.30)"}, {"input": "80,1.70", "expectedOutput": "Sobrepeso (IMC: 27.68)"}, {"input": "100,1.70", "expectedOutput": "Obesidad (IMC: 34.60)"}, {"input": "70,1.75", "expectedOutput": "Normal (IMC: 22.86)"}, {"input": "45,1.60", "expectedOutput": "Bajo peso (IMC: 17.58)"}]',
1, 'INTERMEDIATE'),

(18, 'Tarifa de Envío por Peso', 'Calcula la tarifa de envío: peso < 1kg: $5; 1kg <= peso <= 5kg: $10; peso > 5kg: $15.', 
'def tarifa_envio(peso_kg):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "0.99", "expectedOutput": "5"}, {"input": "1", "expectedOutput": "10"}, {"input": "5", "expectedOutput": "10"}, {"input": "5.01", "expectedOutput": "15"}, {"input": "0.5", "expectedOutput": "5"}]',
1, 'INTERMEDIATE'),

(19, 'Días en un Mes (sin bisiesto)', 'Dado un número de mes (1-12), devuelve cuántos días tiene. Asume que no es año bisiesto (Febrero tiene 28 días).', 
'def dias_en_mes(numero_mes):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "12", "expectedOutput": "31"}, {"input": "9", "expectedOutput": "30"}, {"input": "2", "expectedOutput": "28"}, {"input": "1", "expectedOutput": "31"}, {"input": "4", "expectedOutput": "30"}]',
1, 'INTERMEDIATE'),

-- DIFÍCIL
(20, 'Triángulo Válido y Tipo', 'Dados tres lados, determina si pueden formar un triángulo. Si es válido, clasifícalo como Equilátero, Isósceles o Escaleno.', 
'def tipo_triangulo(a, b, c):
  # Tu código aquí
  pass', 
'def tipo_triangulo(a, b, c):
  lados = sorted([a, b, c])
  s1, s2, s3 = lados[0], lados[1], lados[2]
  if s1 + s2 > s3:
    if s1 == s2 == s3:
      return "Válido, Equilátero"
    elif s1 == s2 or s2 == s3:
      return "Válido, Isósceles"
    else:
      return "Válido, Escaleno"
  else:
    return "Inválido"',
'[{"input": "2,2,3", "expectedOutput": "Válido, Isósceles"}, {"input": "1,2,1", "expectedOutput": "Inválido"}, {"input": "7,7,7", "expectedOutput": "Válido, Equilátero"}, {"input": "3,4,5", "expectedOutput": "Válido, Escaleno"}, {"input": "1,1,5", "expectedOutput": "Inválido"}]',
1, 'HARD'),

(21, 'Año Bisiesto Detallado', 'Determina si un año es bisiesto. Un año es bisiesto si es divisible por 4, excepto los años divisibles por 100 a menos que también sean divisibles por 400.', 
'def es_bisiesto_detallado(anio):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "2004", "expectedOutput": "Bisiesto"}, {"input": "2100", "expectedOutput": "No Bisiesto"}, {"input": "2400", "expectedOutput": "Bisiesto"}, {"input": "2000", "expectedOutput": "Bisiesto"}, {"input": "1900", "expectedOutput": "No Bisiesto"}]',
1, 'HARD'),

(22, 'Calculadora de Impuestos Progresiva', 'Calcula el impuesto sobre la renta: 0-10k: 0%, 10k-30k: 10% sobre el excedente de 10k, >30k: 20% sobre el excedente de 30k + impuesto tramo anterior.', 
'def calcular_impuestos(renta):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "10000", "expectedOutput": "0.0"}, {"input": "30000", "expectedOutput": "2000.0"}, {"input": "50000", "expectedOutput": "6000.0"}, {"input": "5000", "expectedOutput": "0.0"}, {"input": "20000", "expectedOutput": "1000.0"}]',
1, 'HARD'),

(23, 'Cajero Automático Simple', 'Simula un cajero. Dado un saldo inicial y un monto a retirar, verifica si hay fondos suficientes y si el monto es múltiplo de 10. Actualiza y devuelve el nuevo saldo o un mensaje de error.', 
'def cajero_automatico(saldo_inicial, monto_retiro):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "500,500", "expectedOutput": "Nuevo Saldo: 0"}, {"input": "500,50", "expectedOutput": "Nuevo Saldo: 450"}, {"input": "100,103", "expectedOutput": "Monto inválido"}, {"input": "1000,200", "expectedOutput": "Nuevo Saldo: 800"}, {"input": "100,150", "expectedOutput": "Fondos insuficientes"}]',
1, 'HARD'),

(24, 'Juego Piedra, Papel o Tijera', 'Determina el ganador de una ronda de Piedra, Papel, Tijera (entradas: ''piedra'', ''papel'', ''tijera'' para jugador1 y jugador2).', 
'def ppt(jugador1, jugador2):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "''papel'',''piedra''", "expectedOutput": "Gana Jugador 1"}, {"input": "''tijera'',''piedra''", "expectedOutput": "Gana Jugador 2"}, {"input": "''tijera'',''tijera''", "expectedOutput": "Empate"}, {"input": "''piedra'',''papel''", "expectedOutput": "Gana Jugador 2"}, {"input": "''piedra'',''tijera''", "expectedOutput": "Gana Jugador 1"}]',
1, 'HARD'),

(25, 'Validador de Contraseña Simple', 'Una contraseña es válida si tiene al menos 8 caracteres y contiene al menos un número. Devuelve "Válida" o "Inválida".', 
'def validar_contrasena_simple(clave):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "\"secureP4ssword\"", "expectedOutput": "Válida"}, {"input": "\"short1\"", "expectedOutput": "Inválida"}, {"input": "\"longpasswordnonumber\"", "expectedOutput": "Inválida"}, {"input": "\"password123\"", "expectedOutput": "Válida"}, {"input": "\"abc123\"", "expectedOutput": "Inválida"}]',
1, 'HARD'),

(26, 'Determinar Estación del Año (Simplificado)', 'Dado un mes (1-12), determina la estación (hemisferio norte): Diciembre-Febrero: Invierno, Marzo-Mayo: Primavera, Junio-Agosto: Verano, Septiembre-Noviembre: Otoño.', 
'def estacion_del_ano(mes):
  # Tu código aquí
  pass', 
NULL,
'[{"input": "2", "expectedOutput": "Invierno"}, {"input": "5", "expectedOutput": "Primavera"}, {"input": "8", "expectedOutput": "Verano"}, {"input": "11", "expectedOutput": "Otoño"}, {"input": "12", "expectedOutput": "Invierno"}]',
1, 'HARD');