package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Dificultad;
import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.model.Tema;
import com.desafios.robotcode.repository.ProblemaRepository;
import com.desafios.robotcode.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

    @Autowired
    private ProblemaRepository problemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    public void migrateConditionalProblems() {
        // Crear o encontrar el tema 'conditionals'
        Tema tema = temaRepository.findByNombre("conditionals")
                .orElseGet(() -> {
                    Tema nuevoTema = new Tema();
                    nuevoTema.setNombre("conditionals");
                    nuevoTema.setDescripcion("Problemas de estructuras condicionales");
                    return temaRepository.save(nuevoTema);
                });

        // Migrar problemas fáciles (101-112)
        crearProblema(101L, "Verificador de Edad", 
            "Escribe una función que determine si una persona es mayor de edad (18 años o más).",
            "def verificar_edad(edad):\n  # Tu código aquí\n  pass",
            "def verificar_edad(edad):\n  if edad >= 18:\n    return \"Mayor de edad\"\n  else:\n    return \"Menor de edad\"",
            "[{\"input\": \"18\", \"expectedOutput\": \"Mayor de edad\"}, {\"input\": \"17\", \"expectedOutput\": \"Menor de edad\"}, {\"input\": \"25\", \"expectedOutput\": \"Mayor de edad\"}, {\"input\": \"16\", \"expectedOutput\": \"Menor de edad\"}, {\"input\": \"65\", \"expectedOutput\": \"Mayor de edad\"}]",
            tema, Dificultad.EASY);

        crearProblema(102L, "Número Positivo, Negativo o Cero",
            "Escribe una función que determine si un número es positivo, negativo o cero.",
            "def verificar_signo(numero):\n  # Tu código aquí\n  pass",
            "def verificar_signo(numero):\n  if numero > 0:\n    return \"Positivo\"\n  elif numero < 0:\n    return \"Negativo\"\n  else:\n    return \"Cero\"",
            "[{\"input\": \"10\", \"expectedOutput\": \"Positivo\"}, {\"input\": \"-1\", \"expectedOutput\": \"Negativo\"}, {\"input\": \"0\", \"expectedOutput\": \"Cero\"}, {\"input\": \"100\", \"expectedOutput\": \"Positivo\"}, {\"input\": \"-50\", \"expectedOutput\": \"Negativo\"}]",
            tema, Dificultad.EASY);

        crearProblema(103L, "Vocal o Consonante Simple",
            "Dada una letra minúscula, determina si es una vocal ('a', 'e', 'i', 'o', 'u').",
            "def es_vocal_simple(letra):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"'e'\", \"expectedOutput\": \"Vocal\"}, {\"input\": \"'z'\", \"expectedOutput\": \"Consonante\"}, {\"input\": \"'a'\", \"expectedOutput\": \"Vocal\"}, {\"input\": \"'i'\", \"expectedOutput\": \"Vocal\"}, {\"input\": \"'m'\", \"expectedOutput\": \"Consonante\"}]",
            tema, Dificultad.EASY);

        crearProblema(104L, "Acceso Permitido",
            "Si la contraseña es \"1234\", permite el acceso. De lo contrario, deniégalo.",
            "def verificar_acceso(clave):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"\\\"1234\\\"\", \"expectedOutput\": \"Acceso Permitido\"}, {\"input\": \"\\\"0000\\\"\", \"expectedOutput\": \"Acceso Denegado\"}, {\"input\": \"\\\"1234\\\"\", \"expectedOutput\": \"Acceso Permitido\"}, {\"input\": \"\\\"admin\\\"\", \"expectedOutput\": \"Acceso Denegado\"}, {\"input\": \"\\\"password\\\"\", \"expectedOutput\": \"Acceso Denegado\"}]",
            tema, Dificultad.EASY);

        crearProblema(105L, "Fin de Semana",
            "Dado un día de la semana ('lunes'...'domingo'), indica si es fin de semana.",
            "def es_fin_de_semana(dia):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"'domingo'\", \"expectedOutput\": \"Fin de semana\"}, {\"input\": \"'miercoles'\", \"expectedOutput\": \"Día de semana\"}, {\"input\": \"'sabado'\", \"expectedOutput\": \"Fin de semana\"}, {\"input\": \"'lunes'\", \"expectedOutput\": \"Día de semana\"}, {\"input\": \"'viernes'\", \"expectedOutput\": \"Día de semana\"}]",
            tema, Dificultad.EASY);

        crearProblema(106L, "Número Par o Impar",
            "Escribe una función que determine si un número es par o impar.",
            "def par_o_impar(numero):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"100\", \"expectedOutput\": \"Par\"}, {\"input\": \"33\", \"expectedOutput\": \"Impar\"}, {\"input\": \"2\", \"expectedOutput\": \"Par\"}, {\"input\": \"7\", \"expectedOutput\": \"Impar\"}, {\"input\": \"0\", \"expectedOutput\": \"Par\"}]",
            tema, Dificultad.EASY);

        crearProblema(107L, "Semáforo Simple",
            "Dado un color ('rojo', 'verde'), indica la acción ('Detenerse', 'Avanzar').",
            "def accion_semaforo_simple(color):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"'rojo'\", \"expectedOutput\": \"Detenerse\"}, {\"input\": \"'verde'\", \"expectedOutput\": \"Avanzar\"}, {\"input\": \"'rojo'\", \"expectedOutput\": \"Detenerse\"}, {\"input\": \"'verde'\", \"expectedOutput\": \"Avanzar\"}, {\"input\": \"'rojo'\", \"expectedOutput\": \"Detenerse\"}]",
            tema, Dificultad.EASY);

        crearProblema(108L, "Mayor que 10",
            "Verifica si un número es mayor que 10.",
            "def mayor_que_diez(num):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"11\", \"expectedOutput\": \"Mayor que 10\"}, {\"input\": \"10\", \"expectedOutput\": \"No es mayor que 10\"}, {\"input\": \"15\", \"expectedOutput\": \"Mayor que 10\"}, {\"input\": \"5\", \"expectedOutput\": \"No es mayor que 10\"}, {\"input\": \"100\", \"expectedOutput\": \"Mayor que 10\"}]",
            tema, Dificultad.EASY);

        crearProblema(109L, "Aprobado o Reprobado",
            "Si una calificación es 5 o más, está aprobado. Sino, reprobado (sobre 10).",
            "def estado_calificacion(nota):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"5\", \"expectedOutput\": \"Aprobado\"}, {\"input\": \"4.9\", \"expectedOutput\": \"Reprobado\"}, {\"input\": \"7\", \"expectedOutput\": \"Aprobado\"}, {\"input\": \"3\", \"expectedOutput\": \"Reprobado\"}, {\"input\": \"10\", \"expectedOutput\": \"Aprobado\"}]",
            tema, Dificultad.EASY);

        crearProblema(110L, "Saludo por Hora",
            "Si la hora (0-23) es antes de las 12, saluda \"Buenos días\". Sino, \"Buenas tardes/noches\".",
            "def saludo_horario(hora):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"8\", \"expectedOutput\": \"Buenos días\"}, {\"input\": \"20\", \"expectedOutput\": \"Buenas tardes/noches\"}, {\"input\": \"0\", \"expectedOutput\": \"Buenos días\"}, {\"input\": \"12\", \"expectedOutput\": \"Buenas tardes/noches\"}, {\"input\": \"11\", \"expectedOutput\": \"Buenos días\"}]",
            tema, Dificultad.EASY);

        crearProblema(111L, "Bebida Permitida",
            "Si la edad es 21 o más, se permite \"Cerveza\". Sino, \"Jugo\". (Ejemplo simplificado)",
            "def bebida_permitida(edad):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"21\", \"expectedOutput\": \"Cerveza\"}, {\"input\": \"20\", \"expectedOutput\": \"Jugo\"}, {\"input\": \"25\", \"expectedOutput\": \"Cerveza\"}, {\"input\": \"18\", \"expectedOutput\": \"Jugo\"}, {\"input\": \"30\", \"expectedOutput\": \"Cerveza\"}]",
            tema, Dificultad.EASY);

        crearProblema(112L, "Descuento Simple",
            "Si el precio es mayor a $50, aplica un 10% de descuento y devuelve \"Precio con descuento: XX.X\". Sino, \"Precio sin descuento: XX.X\".",
            "def aplicar_descuento_simple(precio):\n  # Tu código aquí\n  pass",
            "def aplicar_descuento_simple(precio):\n  if precio > 50:\n    precio_final = round(precio * 0.9, 2)\n    return f\"Precio con descuento: {precio_final}\"\n  else:\n    return f\"Precio sin descuento: {float(precio)}\"",
            "[{\"input\": \"50.01\", \"expectedOutput\": \"Precio con descuento: 45.01\"}, {\"input\": \"50\", \"expectedOutput\": \"Precio sin descuento: 50.0\"}, {\"input\": \"60\", \"expectedOutput\": \"Precio con descuento: 54.0\"}, {\"input\": \"40\", \"expectedOutput\": \"Precio sin descuento: 40.0\"}, {\"input\": \"100\", \"expectedOutput\": \"Precio con descuento: 90.0\"}]",
            tema, Dificultad.EASY);

        // Problemas intermedios (151-157)
        crearProblema(151L, "Calculadora de Grados Completa",
            "Crea una función que convierta una calificación numérica (0-100) a una letra (A, B, C, D, F). 90-100: A, 80-89: B, 70-79: C, 60-69: D, <60: F.",
            "def calcular_grado(calificacion):\n  # Tu código aquí\n  pass",
            "def calcular_grado(calificacion):\n  if calificacion >= 90:\n    return \"A\"\n  elif calificacion >= 80:\n    return \"B\"\n  elif calificacion >= 70:\n    return \"C\"\n  elif calificacion >= 60:\n    return \"D\"\n  else:\n    return \"F\"",
            "[{\"input\": \"90\", \"expectedOutput\": \"A\"}, {\"input\": \"80\", \"expectedOutput\": \"B\"}, {\"input\": \"59\", \"expectedOutput\": \"F\"}, {\"input\": \"75\", \"expectedOutput\": \"C\"}, {\"input\": \"65\", \"expectedOutput\": \"D\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(152L, "Descuento por Compra Detallado",
            "Si una compra es mayor a $100, aplica 10% de descuento. Si es mayor a $200, aplica 15%. Devuelve el precio final.",
            "def calcular_descuento_detallado(monto):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"100\", \"expectedOutput\": \"100.0\"}, {\"input\": \"150\", \"expectedOutput\": \"135.0\"}, {\"input\": \"201\", \"expectedOutput\": \"170.85\"}, {\"input\": \"50\", \"expectedOutput\": \"50.0\"}, {\"input\": \"250\", \"expectedOutput\": \"212.5\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(153L, "Tipo de Ángulo Completo",
            "Dado un ángulo en grados (0-360), clasifícalo como Agudo (<90), Recto (90), Obtuso (90-180), Llano (180), Cóncavo (>180 y <360) o Completo (360).",
            "def tipo_angulo_completo(angulo):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"90\", \"expectedOutput\": \"Recto\"}, {\"input\": \"150\", \"expectedOutput\": \"Obtuso\"}, {\"input\": \"360\", \"expectedOutput\": \"Completo\"}, {\"input\": \"45\", \"expectedOutput\": \"Agudo\"}, {\"input\": \"270\", \"expectedOutput\": \"Cóncavo\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(154L, "Máximo de Tres Números sin max()",
            "Encuentra el mayor de tres números dados sin usar la función max().",
            "def maximo_de_tres(a, b, c):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"1,2,3\", \"expectedOutput\": \"3\"}, {\"input\": \"3,2,1\", \"expectedOutput\": \"3\"}, {\"input\": \"5,5,1\", \"expectedOutput\": \"5\"}, {\"input\": \"10,5,8\", \"expectedOutput\": \"10\"}, {\"input\": \"2,2,2\", \"expectedOutput\": \"2\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(155L, "Clasificación de IMC",
            "Calcula el IMC (peso/altura^2) y clasifícalo: <18.5 Bajo peso, 18.5-24.9 Normal, 25-29.9 Sobrepeso, >=30 Obesidad.",
            "def clasificar_imc(peso, altura):\n  # IMC = peso / (altura ** 2)\n  # Devuelve string \"Clasificación (IMC: XX.XX)\"\n  pass",
            null,
            "[{\"input\": \"50,1.70\", \"expectedOutput\": \"Bajo peso (IMC: 17.30)\"}, {\"input\": \"80,1.70\", \"expectedOutput\": \"Sobrepeso (IMC: 27.68)\"}, {\"input\": \"100,1.70\", \"expectedOutput\": \"Obesidad (IMC: 34.60)\"}, {\"input\": \"70,1.75\", \"expectedOutput\": \"Normal (IMC: 22.86)\"}, {\"input\": \"45,1.60\", \"expectedOutput\": \"Bajo peso (IMC: 17.58)\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(156L, "Tarifa de Envío por Peso",
            "Calcula la tarifa de envío: peso < 1kg: $5; 1kg <= peso <= 5kg: $10; peso > 5kg: $15.",
            "def tarifa_envio(peso_kg):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"0.99\", \"expectedOutput\": \"5\"}, {\"input\": \"1\", \"expectedOutput\": \"10\"}, {\"input\": \"5\", \"expectedOutput\": \"10\"}, {\"input\": \"5.01\", \"expectedOutput\": \"15\"}, {\"input\": \"0.5\", \"expectedOutput\": \"5\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(157L, "Días en un Mes (sin bisiesto)",
            "Dado un número de mes (1-12), devuelve cuántos días tiene. Asume que no es año bisiesto (Febrero tiene 28 días).",
            "def dias_en_mes(numero_mes):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"12\", \"expectedOutput\": \"31\"}, {\"input\": \"9\", \"expectedOutput\": \"30\"}, {\"input\": \"2\", \"expectedOutput\": \"28\"}, {\"input\": \"1\", \"expectedOutput\": \"31\"}, {\"input\": \"4\", \"expectedOutput\": \"30\"}]",
            tema, Dificultad.INTERMEDIATE);

        // Problemas difíciles (181-188)
        crearProblema(181L, "Triángulo Válido y Tipo",
            "Dados tres lados, determina si pueden formar un triángulo. Si es válido, clasifícalo como Equilátero, Isósceles o Escaleno.",
            "def tipo_triangulo(a, b, c):\n  # Tu código aquí\n  pass",
            "def tipo_triangulo(a, b, c):\n  lados = sorted([a, b, c])\n  s1, s2, s3 = lados[0], lados[1], lados[2]\n  if s1 + s2 > s3:\n    if s1 == s2 == s3:\n      return \"Válido, Equilátero\"\n    elif s1 == s2 or s2 == s3:\n      return \"Válido, Isósceles\"\n    else:\n      return \"Válido, Escaleno\"\n  else:\n    return \"Inválido\"",
            "[{\"input\": \"2,2,3\", \"expectedOutput\": \"Válido, Isósceles\"}, {\"input\": \"1,2,1\", \"expectedOutput\": \"Inválido\"}, {\"input\": \"7,7,7\", \"expectedOutput\": \"Válido, Equilátero\"}, {\"input\": \"3,4,5\", \"expectedOutput\": \"Válido, Escaleno\"}, {\"input\": \"1,1,5\", \"expectedOutput\": \"Inválido\"}]",
            tema, Dificultad.HARD);

        crearProblema(182L, "Año Bisiesto Detallado",
            "Determina si un año es bisiesto. Un año es bisiesto si es divisible por 4, excepto los años divisibles por 100 a menos que también sean divisibles por 400.",
            "def es_bisiesto_detallado(anio):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"2004\", \"expectedOutput\": \"Bisiesto\"}, {\"input\": \"2100\", \"expectedOutput\": \"No Bisiesto\"}, {\"input\": \"2400\", \"expectedOutput\": \"Bisiesto\"}, {\"input\": \"2000\", \"expectedOutput\": \"Bisiesto\"}, {\"input\": \"1900\", \"expectedOutput\": \"No Bisiesto\"}]",
            tema, Dificultad.HARD);

        crearProblema(183L, "Calculadora de Impuestos Progresiva",
            "Calcula el impuesto sobre la renta: 0-10k: 0%, 10k-30k: 10% sobre el excedente de 10k, >30k: 20% sobre el excedente de 30k + impuesto tramo anterior.",
            "def calcular_impuestos(renta):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"10000\", \"expectedOutput\": \"0.0\"}, {\"input\": \"30000\", \"expectedOutput\": \"2000.0\"}, {\"input\": \"50000\", \"expectedOutput\": \"6000.0\"}, {\"input\": \"5000\", \"expectedOutput\": \"0.0\"}, {\"input\": \"20000\", \"expectedOutput\": \"1000.0\"}]",
            tema, Dificultad.HARD);

        crearProblema(184L, "Cajero Automático Simple",
            "Simula un cajero. Dado un saldo inicial y un monto a retirar, verifica si hay fondos suficientes y si el monto es múltiplo de 10. Actualiza y devuelve el nuevo saldo o un mensaje de error.",
            "def cajero_automatico(saldo_inicial, monto_retiro):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"500,500\", \"expectedOutput\": \"Nuevo Saldo: 0\"}, {\"input\": \"500,50\", \"expectedOutput\": \"Nuevo Saldo: 450\"}, {\"input\": \"100,103\", \"expectedOutput\": \"Monto inválido\"}, {\"input\": \"1000,200\", \"expectedOutput\": \"Nuevo Saldo: 800\"}, {\"input\": \"100,150\", \"expectedOutput\": \"Fondos insuficientes\"}]",
            tema, Dificultad.HARD);

        crearProblema(185L, "Juego Piedra, Papel o Tijera",
            "Determina el ganador de una ronda de Piedra, Papel, Tijera (entradas: 'piedra', 'papel', 'tijera' para jugador1 y jugador2).",
            "def ppt(jugador1, jugador2):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"'papel','piedra'\", \"expectedOutput\": \"Gana Jugador 1\"}, {\"input\": \"'tijera','piedra'\", \"expectedOutput\": \"Gana Jugador 2\"}, {\"input\": \"'tijera','tijera'\", \"expectedOutput\": \"Empate\"}, {\"input\": \"'piedra','papel'\", \"expectedOutput\": \"Gana Jugador 2\"}, {\"input\": \"'piedra','tijera'\", \"expectedOutput\": \"Gana Jugador 1\"}]",
            tema, Dificultad.HARD);

        crearProblema(186L, "Validador de Contraseña Simple",
            "Una contraseña es válida si tiene al menos 8 caracteres y contiene al menos un número. Devuelve \"Válida\" o \"Inválida\".",
            "def validar_contrasena_simple(clave):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"\\\"secureP4ssword\\\"\", \"expectedOutput\": \"Válida\"}, {\"input\": \"\\\"short1\\\"\", \"expectedOutput\": \"Inválida\"}, {\"input\": \"\\\"longpasswordnonumber\\\"\", \"expectedOutput\": \"Inválida\"}, {\"input\": \"\\\"password123\\\"\", \"expectedOutput\": \"Válida\"}, {\"input\": \"\\\"abc123\\\"\", \"expectedOutput\": \"Inválida\"}]",
            tema, Dificultad.HARD);

        crearProblema(187L, "Determinar Estación del Año (Simplificado)",
            "Dado un mes (1-12), determina la estación (hemisferio norte): Diciembre-Febrero: Invierno, Marzo-Mayo: Primavera, Junio-Agosto: Verano, Septiembre-Noviembre: Otoño.",
            "def estacion_del_ano(mes):\n  # Tu código aquí\n  pass",
            null,
            "[{\"input\": \"2\", \"expectedOutput\": \"Invierno\"}, {\"input\": \"5\", \"expectedOutput\": \"Primavera\"}, {\"input\": \"8\", \"expectedOutput\": \"Verano\"}, {\"input\": \"11\", \"expectedOutput\": \"Otoño\"}, {\"input\": \"12\", \"expectedOutput\": \"Invierno\"}]",
            tema, Dificultad.HARD);

        System.out.println("Migración de problemas de condicionales completada exitosamente!");
    }

    public void migrateLoopProblems() {
        // Crear o encontrar el tema 'Bucles'
        Tema tema = temaRepository.findByNombre("Bucles")
                .orElseGet(() -> {
                    Tema nuevoTema = new Tema();
                    nuevoTema.setNombre("Bucles");
                    nuevoTema.setDescripcion("Problemas de estructuras de repetición y bucles");
                    return temaRepository.save(nuevoTema);
                });

        // Migrar problemas fáciles (201-212)
        crearProblema(201L, "Contar Hasta N",
            "Escribe una función que imprima (o devuelva una lista de) números del 1 a N.",
            "def contar_hasta_n(n):\n  # resultado = []\n  # Tu código aquí\n  # return resultado\n  pass",
            "def contar_hasta_n(n):\n  resultado = []\n  for i in range(1, n + 1):\n    resultado.append(i)\n  return resultado",
            "[{\"input\": \"3\", \"expectedOutput\": \"[1, 2, 3]\"}, {\"input\": \"1\", \"expectedOutput\": \"[1]\"}, {\"input\": \"5\", \"expectedOutput\": \"[1, 2, 3, 4, 5]\"}, {\"input\": \"7\", \"expectedOutput\": \"[1, 2, 3, 4, 5, 6, 7]\"}, {\"input\": \"2\", \"expectedOutput\": \"[1, 2]\"}]",
            tema, Dificultad.EASY);

        crearProblema(202L, "Suma de Primeros N Números",
            "Calcula la suma de los números enteros desde 1 hasta N.",
            "def suma_primeros_n(n):\n  # Tu código aquí\n  pass",
            "def suma_primeros_n(n):\n  suma = 0\n  for i in range(1, n + 1):\n    suma += i\n  return suma",
            "[{\"input\": \"3\", \"expectedOutput\": \"6\"}, {\"input\": \"10\", \"expectedOutput\": \"55\"}, {\"input\": \"5\", \"expectedOutput\": \"15\"}, {\"input\": \"1\", \"expectedOutput\": \"1\"}, {\"input\": \"4\", \"expectedOutput\": \"10\"}]",
            tema, Dificultad.EASY);

        crearProblema(203L, "Tabla de Multiplicar del N (Primeros 5)",
            "Genera los primeros 5 múltiplos de un número N. Devuelve una lista de strings \"NxM=R\".",
            "def tabla_multiplicar_simple(n):\n  # Tu código aquí\n  pass",
            "def tabla_multiplicar_simple(n):\n  resultado = []\n  for i in range(1, 6):\n    resultado.append(f\"{n}x{i}={n*i}\")\n  return resultado",
            "[{\"input\": \"7\", \"expectedOutput\": \"['7x1=7', '7x2=14', '7x3=21', '7x4=28', '7x5=35']\"}, {\"input\": \"1\", \"expectedOutput\": \"['1x1=1', '1x2=2', '1x3=3', '1x4=4', '1x5=5']\"}, {\"input\": \"3\", \"expectedOutput\": \"['3x1=3', '3x2=6', '3x3=9', '3x4=12', '3x5=15']\"}, {\"input\": \"5\", \"expectedOutput\": \"['5x1=5', '5x2=10', '5x3=15', '5x4=20', '5x5=25']\"}, {\"input\": \"2\", \"expectedOutput\": \"['2x1=2', '2x2=4', '2x3=6', '2x4=8', '2x5=10']\"}]",
            tema, Dificultad.EASY);

        crearProblema(204L, "Contar Vocales en Cadena",
            "Dada una cadena de texto, cuenta cuántas vocales (a, e, i, o, u, sin importar mayúsculas) contiene.",
            "def contar_vocales_cadena(cadena):\n  # Tu código aquí\n  pass",
            "def contar_vocales_cadena(cadena):\n  vocales = \"aeiouAEIOU\"\n  contador = 0\n  for caracter in cadena:\n    if caracter in vocales:\n      contador += 1\n  return contador",
            "[{\"input\": \"'AEIOUaeiou'\", \"expectedOutput\": \"10\"}, {\"input\": \"'Rhythm'\", \"expectedOutput\": \"0\"}, {\"input\": \"'Hola Mundo'\", \"expectedOutput\": \"4\"}, {\"input\": \"'Programming'\", \"expectedOutput\": \"3\"}, {\"input\": \"'Hello'\", \"expectedOutput\": \"2\"}]",
            tema, Dificultad.EASY);

        crearProblema(205L, "Repetir Palabra",
            "Dada una palabra y un número N, devuelve una cadena con la palabra repetida N veces.",
            "def repetir_palabra(palabra, n):\n  # Tu código aquí\n  pass",
            "def repetir_palabra(palabra, n):\n  resultado = \"\"\n  for i in range(n):\n    resultado += palabra\n  return resultado",
            "[{\"input\": \"'eco', 2\", \"expectedOutput\": \"ecoeco\"}, {\"input\": \"'test', 1\", \"expectedOutput\": \"test\"}, {\"input\": \"'robot', 3\", \"expectedOutput\": \"robotrobotrobot\"}, {\"input\": \"'Hi', 4\", \"expectedOutput\": \"HiHiHiHi\"}, {\"input\": \"'A', 5\", \"expectedOutput\": \"AAAAA\"}]",
            tema, Dificultad.EASY);

        crearProblema(206L, "Encontrar el Mínimo en una Lista",
            "Encuentra el número más pequeño en una lista de números sin usar la función min().",
            "def minimo_en_lista(lista):\n  # Tu código aquí\n  pass",
            "def minimo_en_lista(lista):\n  if not lista:\n    return None\n  minimo = lista[0]\n  for numero in lista:\n    if numero < minimo:\n      minimo = numero\n  return minimo",
            "[{\"input\": \"[10,20,5,30]\", \"expectedOutput\": \"5\"}, {\"input\": \"[-1,-5,0]\", \"expectedOutput\": \"-5\"}, {\"input\": \"[3,1,4,1,5,9,2,6]\", \"expectedOutput\": \"1\"}, {\"input\": \"[100]\", \"expectedOutput\": \"100\"}, {\"input\": \"[7,2,9,1,8]\", \"expectedOutput\": \"1\"}]",
            tema, Dificultad.EASY);

        crearProblema(207L, "Filtrar Números Pares",
            "Dada una lista de números, devuelve una nueva lista solo con los números pares.",
            "def filtrar_pares_lista(lista):\n  # Tu código aquí\n  pass",
            "def filtrar_pares_lista(lista):\n  pares = []\n  for numero in lista:\n    if numero % 2 == 0:\n      pares.append(numero)\n  return pares",
            "[{\"input\": \"[10, 11, 12, 13, 14]\", \"expectedOutput\": \"[10, 12, 14]\"}, {\"input\": \"[1, 3, 5]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[1, 2, 3, 4, 5, 6]\", \"expectedOutput\": \"[2, 4, 6]\"}, {\"input\": \"[2, 4, 6, 8]\", \"expectedOutput\": \"[2, 4, 6, 8]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.EASY);

        crearProblema(208L, "Potencias de Dos",
            "Genera las primeras N potencias de 2 (empezando desde 2^0).",
            "def potencias_de_dos(n_potencias):\n  # Tu código aquí\n  pass",
            "def potencias_de_dos(n_potencias):\n  resultado = []\n  for i in range(n_potencias):\n    resultado.append(2 ** i)\n  return resultado",
            "[{\"input\": \"1\", \"expectedOutput\": \"[1]\"}, {\"input\": \"5\", \"expectedOutput\": \"[1, 2, 4, 8, 16]\"}, {\"input\": \"4\", \"expectedOutput\": \"[1, 2, 4, 8]\"}, {\"input\": \"3\", \"expectedOutput\": \"[1, 2, 4]\"}, {\"input\": \"6\", \"expectedOutput\": \"[1, 2, 4, 8, 16, 32]\"}]",
            tema, Dificultad.EASY);

        crearProblema(209L, "Imprimir Caracteres de Cadena",
            "Dada una cadena, imprime cada carácter en una nueva línea (o devuelve lista de caracteres).",
            "def imprimir_caracteres(cadena):\n  # Tu código aquí\n  pass",
            "def imprimir_caracteres(cadena):\n  caracteres = []\n  for caracter in cadena:\n    caracteres.append(caracter)\n  return caracteres",
            "[{\"input\": \"'Hi'\", \"expectedOutput\": \"['H', 'i']\"}, {\"input\": \"''\", \"expectedOutput\": \"[]\"}, {\"input\": \"'abc'\", \"expectedOutput\": \"['a', 'b', 'c']\"}, {\"input\": \"'X'\", \"expectedOutput\": \"['X']\"}, {\"input\": \"'Python'\", \"expectedOutput\": \"['P', 'y', 't', 'h', 'o', 'n']\"}]",
            tema, Dificultad.EASY);

        crearProblema(210L, "Suma de Elementos de Lista (Ciclos)",
            "Suma todos los elementos de una lista de números usando un bucle.",
            "def suma_lista_con_bucle(lista):\n  # Tu código aquí\n  pass",
            "def suma_lista_con_bucle(lista):\n  suma = 0\n  for numero in lista:\n    suma += numero\n  return suma",
            "[{\"input\": \"[10, 20, 30]\", \"expectedOutput\": \"60\"}, {\"input\": \"[-1, 0, 1]\", \"expectedOutput\": \"0\"}, {\"input\": \"[1, 2, 3]\", \"expectedOutput\": \"6\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}, {\"input\": \"[5, 5, 5, 5]\", \"expectedOutput\": \"20\"}]",
            tema, Dificultad.EASY);

        crearProblema(211L, "Contar Hacia Atrás",
            "Dado un número N > 0, devuelve una lista de números desde N hasta 1.",
            "def contar_hacia_atras(n):\n  # resultado = []\n  # Tu código aquí\n  # return resultado\n  pass",
            "def contar_hacia_atras(n):\n  resultado = []\n  for i in range(n, 0, -1):\n    resultado.append(i)\n  return resultado",
            "[{\"input\": \"3\", \"expectedOutput\": \"[3, 2, 1]\"}, {\"input\": \"1\", \"expectedOutput\": \"[1]\"}, {\"input\": \"5\", \"expectedOutput\": \"[5, 4, 3, 2, 1]\"}, {\"input\": \"2\", \"expectedOutput\": \"[2, 1]\"}, {\"input\": \"4\", \"expectedOutput\": \"[4, 3, 2, 1]\"}]",
            tema, Dificultad.EASY);

        crearProblema(212L, "Encontrar el Primer Número Par",
            "Dada una lista de números, encuentra y devuelve el primer número par. Si no hay ninguno, devuelve None.",
            "def primer_par_en_lista(lista):\n  # Tu código aquí\n  pass",
            "def primer_par_en_lista(lista):\n  for numero in lista:\n    if numero % 2 == 0:\n      return numero\n  return None",
            "[{\"input\": \"[7, 9, 4, 6]\", \"expectedOutput\": \"4\"}, {\"input\": \"[1, 5, 7]\", \"expectedOutput\": \"None\"}, {\"input\": \"[1, 3, 5, 2, 8]\", \"expectedOutput\": \"2\"}, {\"input\": \"[2]\", \"expectedOutput\": \"2\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.EASY);

        // Problemas intermedios (251-257)
        crearProblema(251L, "Generador de Factoriales (Ciclos)",
            "Crea una función que calcule el factorial de un número entero no negativo usando ciclos.",
            "def factorial_con_bucle(num):\n  # Tu código aquí\n  pass",
            "def factorial_con_bucle(num):\n  if num < 0:\n    return \"No definido para negativos\"\n  if num == 0:\n    return 1\n  resultado = 1\n  for i in range(1, num + 1):\n    resultado *= i\n  return resultado",
            "[{\"input\": \"1\", \"expectedOutput\": \"1\"}, {\"input\": \"6\", \"expectedOutput\": \"720\"}, {\"input\": \"3\", \"expectedOutput\": \"6\"}, {\"input\": \"5\", \"expectedOutput\": \"120\"}, {\"input\": \"0\", \"expectedOutput\": \"1\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(252L, "Invertir Cadena (Ciclos)",
            "Invierte una cadena de texto sin usar funciones integradas de inversión, usando ciclos.",
            "def invertir_cadena_con_bucle(cadena):\n  # Tu código aquí\n  pass",
            "def invertir_cadena_con_bucle(cadena):\n  resultado = \"\"\n  for i in range(len(cadena) - 1, -1, -1):\n    resultado += cadena[i]\n  return resultado",
            "[{\"input\": \"'apple'\", \"expectedOutput\": \"elppa\"}, {\"input\": \"'a'\", \"expectedOutput\": \"a\"}, {\"input\": \"''\", \"expectedOutput\": \"''\"}, {\"input\": \"'robot'\", \"expectedOutput\": \"tobor\"}, {\"input\": \"'hello'\", \"expectedOutput\": \"olleh\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(253L, "Serie de Fibonacci (Primeros N, Ciclos)",
            "Genera los primeros N números de la serie de Fibonacci usando ciclos.",
            "def fibonacci_con_bucle(n_elementos):\n  # Tu código aquí\n  pass",
            "def fibonacci_con_bucle(n_elementos):\n  if n_elementos <= 0:\n    return []\n  elif n_elementos == 1:\n    return [0]\n  elif n_elementos == 2:\n    return [0, 1]\n  \n  fibonacci = [0, 1]\n  for i in range(2, n_elementos):\n    fibonacci.append(fibonacci[i-1] + fibonacci[i-2])\n  return fibonacci",
            "[{\"input\": \"1\", \"expectedOutput\": \"[0]\"}, {\"input\": \"2\", \"expectedOutput\": \"[0, 1]\"}, {\"input\": \"10\", \"expectedOutput\": \"[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]\"}, {\"input\": \"5\", \"expectedOutput\": \"[0, 1, 1, 2, 3]\"}, {\"input\": \"7\", \"expectedOutput\": \"[0, 1, 1, 2, 3, 5, 8]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(254L, "Suma de Dígitos de un Número",
            "Calcula la suma de los dígitos de un número entero positivo.",
            "def suma_digitos_numero(numero):\n  # Tu código aquí\n  pass",
            "def suma_digitos_numero(numero):\n  suma = 0\n  while numero > 0:\n    suma += numero % 10\n    numero //= 10\n  return suma",
            "[{\"input\": \"100\", \"expectedOutput\": \"1\"}, {\"input\": \"9\", \"expectedOutput\": \"9\"}, {\"input\": \"765\", \"expectedOutput\": \"18\"}, {\"input\": \"123\", \"expectedOutput\": \"6\"}, {\"input\": \"9045\", \"expectedOutput\": \"18\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(255L, "Patrón de Triángulo de Asteriscos",
            "Crea una función que devuelva un string representando un triángulo de asteriscos de N filas.\nEj: N=3\n*\n**\n***",
            "def triangulo_asteriscos(n_filas):\n  # resultado_str = \"\"\n  # Tu código aquí\n  # return resultado_str.strip()\n  pass",
            "def triangulo_asteriscos(n_filas):\n  resultado_str = \"\"\n  for i in range(1, n_filas + 1):\n    resultado_str += \"*\" * i + \"\\n\"\n  return resultado_str.strip()",
            "[{\"input\": \"1\", \"expectedOutput\": \"'*'\"}, {\"input\": \"2\", \"expectedOutput\": \"'*\\n**'\"}, {\"input\": \"4\", \"expectedOutput\": \"'*\\n**\\n***\\n****'\"}, {\"input\": \"3\", \"expectedOutput\": \"'*\\n**\\n***'\"}, {\"input\": \"5\", \"expectedOutput\": \"'*\\n**\\n***\\n****\\n*****'\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(256L, "Eliminar Duplicados de Lista (Ciclos)",
            "Dada una lista, devuelve una nueva lista sin elementos duplicados, manteniendo el orden de la primera aparición. Usa ciclos.",
            "def eliminar_duplicados_con_bucle(lista):\n  # unicos = []\n  # Tu código aquí\n  # return unicos\n  pass",
            "def eliminar_duplicados_con_bucle(lista):\n  unicos = []\n  for elemento in lista:\n    if elemento not in unicos:\n      unicos.append(elemento)\n  return unicos",
            "[{\"input\": \"['a', 'b', 'a', 'c', 'b']\", \"expectedOutput\": \"['a', 'b', 'c']\"}, {\"input\": \"[1, 1, 1]\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[1, 2, 2, 3, 4, 3, 5]\", \"expectedOutput\": \"[1, 2, 3, 4, 5]\"}, {\"input\": \"[1, 2, 3, 4, 5]\", \"expectedOutput\": \"[1, 2, 3, 4, 5]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(257L, "Máximo Común Divisor (Ciclos)",
            "Encuentra el Máximo Común Divisor (MCD) de dos números positivos usando ciclos (ej. algoritmo de Euclides iterativo).",
            "def mcd_con_ciclos(a, b):\n  # Tu código aquí\n  pass",
            "def mcd_con_ciclos(a, b):\n  while b:\n    a, b = b, a % b\n  return a",
            "[{\"input\": \"60, 48\", \"expectedOutput\": \"12\"}, {\"input\": \"17, 5\", \"expectedOutput\": \"1\"}, {\"input\": \"7, 7\", \"expectedOutput\": \"7\"}, {\"input\": \"48, 18\", \"expectedOutput\": \"6\"}, {\"input\": \"100, 25\", \"expectedOutput\": \"25\"}]",
            tema, Dificultad.INTERMEDIATE);

        // Problemas difíciles (281-287)
        crearProblema(281L, "Verificar Número Primo",
            "Verifica si un número N es primo. Un número primo es mayor que 1 y solo divisible por 1 y por sí mismo.",
            "def es_primo_numero(n):\n  # Tu código aquí\n  pass",
            "def es_primo_numero(n):\n  if n <= 1:\n    return False\n  if n <= 3:\n    return True\n  if n % 2 == 0 or n % 3 == 0:\n    return False\n  i = 5\n  while i * i <= n:\n    if n % i == 0 or n % (i + 2) == 0:\n      return False\n    i += 6\n  return True",
            "[{\"input\": \"13\", \"expectedOutput\": \"True\"}, {\"input\": \"1\", \"expectedOutput\": \"False\"}, {\"input\": \"97\", \"expectedOutput\": \"True\"}, {\"input\": \"7\", \"expectedOutput\": \"True\"}, {\"input\": \"10\", \"expectedOutput\": \"False\"}]",
            tema, Dificultad.HARD);

        crearProblema(282L, "Palíndromo Numérico",
            "Verifica si un número entero es un palíndromo (se lee igual de izquierda a derecha que de derecha a izquierda).",
            "def es_palindromo_numerico(numero):\n  # Tu código aquí\n  pass",
            "def es_palindromo_numerico(numero):\n  original = numero\n  invertido = 0\n  while numero > 0:\n    invertido = invertido * 10 + numero % 10\n    numero //= 10\n  return original == invertido",
            "[{\"input\": \"1\", \"expectedOutput\": \"True\"}, {\"input\": \"1221\", \"expectedOutput\": \"True\"}, {\"input\": \"12320\", \"expectedOutput\": \"False\"}, {\"input\": \"121\", \"expectedOutput\": \"True\"}, {\"input\": \"123\", \"expectedOutput\": \"False\"}]",
            tema, Dificultad.HARD);

        crearProblema(283L, "Dibujar Cuadrado Hueco",
            "Dibuja un cuadrado hueco de N x N usando asteriscos. Devuelve un string multilínea.\nEj: N=4\n****\n*  *\n*  *\n****",
            "def cuadrado_hueco(n):\n  # resultado_str = \"\"\n  # Tu código aquí\n  # return resultado_str.strip()\n  pass",
            "def cuadrado_hueco(n):\n  resultado_str = \"\"\n  for i in range(n):\n    for j in range(n):\n      if i == 0 or i == n-1 or j == 0 or j == n-1:\n        resultado_str += \"*\"\n      else:\n        resultado_str += \" \"\n    resultado_str += \"\\n\"\n  return resultado_str.strip()",
            "[{\"input\": \"3\", \"expectedOutput\": \"'***\\n* *\\n***'\"}, {\"input\": \"5\", \"expectedOutput\": \"'*****\\n*   *\\n*   *\\n*   *\\n*****'\"}, {\"input\": \"2\", \"expectedOutput\": \"'**\\n**'\"}, {\"input\": \"4\", \"expectedOutput\": \"'****\\n*  *\\n*  *\\n****'\"}, {\"input\": \"1\", \"expectedOutput\": \"'*'\"}]",
            tema, Dificultad.HARD);

        crearProblema(284L, "Adivina el Número (Simulación de Intentos)",
            "Simula un juego: el ordenador \"piensa\" un número (ej. 42). El usuario tiene N intentos para adivinarlo. La función recibe el secreto, una lista de intentos y N. Devuelve \"Adivinado en X intentos\" o \"Agotados los intentos\".",
            "def adivina_numero_sim(secreto, lista_intentos, max_intentos):\n  # Tu código aquí\n  pass",
            "def adivina_numero_sim(secreto, lista_intentos, max_intentos):\n  for i, intento in enumerate(lista_intentos):\n    if intento == secreto:\n      return f\"Adivinado en {i+1} intentos\"\n  return \"Agotados los intentos\"",
            "[{\"input\": \"50, [10, 60, 50, 55], 4\", \"expectedOutput\": \"Adivinado en 3 intentos\"}, {\"input\": \"25, [1, 2, 3], 3\", \"expectedOutput\": \"Agotados los intentos\"}, {\"input\": \"10, [10], 1\", \"expectedOutput\": \"Adivinado en 1 intentos\"}, {\"input\": \"42, [10, 20, 42], 5\", \"expectedOutput\": \"Adivinado en 3 intentos\"}, {\"input\": \"99, [1, 2, 3, 4, 5], 5\", \"expectedOutput\": \"Agotados los intentos\"}]",
            tema, Dificultad.HARD);

        crearProblema(285L, "Descomposición en Factores Primos (Simple)",
            "Encuentra los factores primos de un número N. Devuelve una lista de factores. (Versión simple, puede no ser óptima para números grandes).",
            "def factores_primos_simple(n):\n  # factores = []\n  # Tu código aquí\n  # return factores\n  pass",
            "def factores_primos_simple(n):\n  factores = []\n  divisor = 2\n  while n > 1:\n    while n % divisor == 0:\n      factores.append(divisor)\n      n //= divisor\n    divisor += 1\n  return factores",
            "[{\"input\": \"7\", \"expectedOutput\": \"[7]\"}, {\"input\": \"28\", \"expectedOutput\": \"[2, 2, 7]\"}, {\"input\": \"100\", \"expectedOutput\": \"[2, 2, 5, 5]\"}, {\"input\": \"12\", \"expectedOutput\": \"[2, 2, 3]\"}, {\"input\": \"30\", \"expectedOutput\": \"[2, 3, 5]\"}]",
            tema, Dificultad.HARD);

        crearProblema(286L, "Patrón de Diamante de Asteriscos",
            "Crea una función que devuelva un string representando un diamante de asteriscos de N filas de altura (N debe ser impar).\nEj: N=3\n *\n***\n *\n",
            "def diamante_asteriscos(n_filas_altura):\n  # if n_filas_altura % 2 == 0: return \"N debe ser impar\"\n  # resultado_str = \"\"\n  # Tu código aquí\n  # return resultado_str.strip()\n  pass",
            "def diamante_asteriscos(n_filas_altura):\n  if n_filas_altura % 2 == 0:\n    return \"N debe ser impar\"\n  \n  resultado_str = \"\"\n  mitad = n_filas_altura // 2\n  \n  for i in range(n_filas_altura):\n    espacios = abs(mitad - i)\n    asteriscos = n_filas_altura - 2 * espacios\n    resultado_str += \" \" * espacios + \"*\" * asteriscos + \"\\n\"\n  \n  return resultado_str.strip()",
            "[{\"input\": \"1\", \"expectedOutput\": \"*\"}, {\"input\": \"5\", \"expectedOutput\": \"  *  \\n *** \\n*****\\n *** \\n  *  \"}, {\"input\": \"3\", \"expectedOutput\": \" * \\n***\\n * \"}, {\"input\": \"7\", \"expectedOutput\": \"   *   \\n  ***  \\n ***** \\n*******\\n ***** \\n  ***  \\n   *   \"}, {\"input\": \"9\", \"expectedOutput\": \"    *    \\n   ***   \\n  *****  \\n ******* \\n*********\\n ******* \\n  *****  \\n   ***   \\n    *    \"}]",
            tema, Dificultad.HARD);

        crearProblema(287L, "Encontrar todas las Subcadenas",
            "Dada una cadena, genera y devuelve una lista de todas sus posibles subcadenas contiguas.",
            "def todas_subcadenas(cadena):\n  # subcadenas = []\n  # n = len(cadena)\n  # for i in range(n):\n  #   for j in range(i, n):\n  #     subcadenas.append(cadena[i:j+1])\n  # return sorted(list(set(subcadenas))) # Para un output consistente\n  pass",
            "def todas_subcadenas(cadena):\n  subcadenas = []\n  n = len(cadena)\n  for i in range(n):\n    for j in range(i, n):\n      subcadenas.append(cadena[i:j+1])\n  return sorted(list(set(subcadenas)))",
            "[{\"input\": \"'a'\", \"expectedOutput\": \"['a']\"}, {\"input\": \"'abc'\", \"expectedOutput\": \"['a', 'ab', 'abc', 'b', 'bc', 'c']\"}, {\"input\": \"'ab'\", \"expectedOutput\": \"['a', 'ab', 'b']\"}, {\"input\": \"'xy'\", \"expectedOutput\": \"['x', 'xy', 'y']\"}, {\"input\": \"''\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.HARD);

        System.out.println("Migración de problemas de loops completada exitosamente!");
    }

    public void migrateFunctionProblems() {
        // Crear o encontrar el tema 'Funciones'
        Tema tema = temaRepository.findByNombre("Funciones")
                .orElseGet(() -> {
                    Tema nuevoTema = new Tema();
                    nuevoTema.setNombre("Funciones");
                    nuevoTema.setDescripcion("Problemas de funciones y programación modular");
                    return temaRepository.save(nuevoTema);
                });

        // Migrar problemas fáciles (301-312)
        crearProblema(301L, "Saludar Usuario",
            "Crea una función que salude a un usuario por su nombre.",
            "def saludar(nombre):\n  # Tu código aquí\n  pass",
            "def saludar(nombre):\n  return f\"¡Hola {nombre}!\"",
            "[{\"input\": \"'Ana'\", \"expectedOutput\": \"¡Hola Ana!\"}, {\"input\": \"'Carlos'\", \"expectedOutput\": \"¡Hola Carlos!\"}, {\"input\": \"'María'\", \"expectedOutput\": \"¡Hola María!\"}, {\"input\": \"'Juan'\", \"expectedOutput\": \"¡Hola Juan!\"}, {\"input\": \"'Pedro'\", \"expectedOutput\": \"¡Hola Pedro!\"}]",
            tema, Dificultad.EASY);

        crearProblema(302L, "Calcular Área del Círculo",
            "Calcula el área de un círculo dado su radio (usa π = 3.14159).",
            "def area_circulo(radio):\n  # Tu código aquí\n  pass",
            "def area_circulo(radio):\n  pi = 3.14159\n  return pi * radio * radio",
            "[{\"input\": \"1\", \"expectedOutput\": \"3.14159\"}, {\"input\": \"2\", \"expectedOutput\": \"12.56636\"}, {\"input\": \"5\", \"expectedOutput\": \"78.53975\"}, {\"input\": \"0.5\", \"expectedOutput\": \"0.7853975\"}, {\"input\": \"10\", \"expectedOutput\": \"314.159\"}]",
            tema, Dificultad.EASY);

        crearProblema(303L, "Convertir Celsius a Fahrenheit",
            "Convierte una temperatura de Celsius a Fahrenheit usando la fórmula: F = (C × 9/5) + 32.",
            "def celsius_a_fahrenheit(celsius):\n  # Tu código aquí\n  pass",
            "def celsius_a_fahrenheit(celsius):\n  return (celsius * 9/5) + 32",
            "[{\"input\": \"0\", \"expectedOutput\": \"32.0\"}, {\"input\": \"100\", \"expectedOutput\": \"212.0\"}, {\"input\": \"37\", \"expectedOutput\": \"98.6\"}, {\"input\": \"-40\", \"expectedOutput\": \"-40.0\"}, {\"input\": \"25\", \"expectedOutput\": \"77.0\"}]",
            tema, Dificultad.EASY);

        crearProblema(304L, "Verificar Número Par",
            "Verifica si un número es par o impar. Devuelve 'Par' o 'Impar'.",
            "def verificar_par_impar(numero):\n  # Tu código aquí\n  pass",
            "def verificar_par_impar(numero):\n  if numero % 2 == 0:\n    return \"Par\"\n  else:\n    return \"Impar\"",
            "[{\"input\": \"2\", \"expectedOutput\": \"Par\"}, {\"input\": \"3\", \"expectedOutput\": \"Impar\"}, {\"input\": \"0\", \"expectedOutput\": \"Par\"}, {\"input\": \"-1\", \"expectedOutput\": \"Impar\"}, {\"input\": \"10\", \"expectedOutput\": \"Par\"}]",
            tema, Dificultad.EASY);

        crearProblema(305L, "Calcular Promedio",
            "Calcula el promedio de una lista de números.",
            "def calcular_promedio(lista):\n  # Tu código aquí\n  pass",
            "def calcular_promedio(lista):\n  if not lista:\n    return 0\n  return sum(lista) / len(lista)",
            "[{\"input\": \"[1, 2, 3, 4, 5]\", \"expectedOutput\": \"3.0\"}, {\"input\": \"[10, 20, 30]\", \"expectedOutput\": \"20.0\"}, {\"input\": \"[0, 0, 0]\", \"expectedOutput\": \"0.0\"}, {\"input\": \"[1]\", \"expectedOutput\": \"1.0\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}]",
            tema, Dificultad.EASY);

        crearProblema(306L, "Encontrar Máximo",
            "Encuentra el número más grande en una lista de números.",
            "def encontrar_maximo(lista):\n  # Tu código aquí\n  pass",
            "def encontrar_maximo(lista):\n  if not lista:\n    return None\n  return max(lista)",
            "[{\"input\": \"[1, 5, 3, 9, 2]\", \"expectedOutput\": \"9\"}, {\"input\": \"[-1, -5, -3]\", \"expectedOutput\": \"-1\"}, {\"input\": \"[10]\", \"expectedOutput\": \"10\"}, {\"input\": \"[1, 1, 1, 1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.EASY);

        crearProblema(307L, "Contar Vocales",
            "Cuenta cuántas vocales (a, e, i, o, u) hay en una cadena de texto.",
            "def contar_vocales(texto):\n  # Tu código aquí\n  pass",
            "def contar_vocales(texto):\n  vocales = 'aeiouAEIOU'\n  contador = 0\n  for caracter in texto:\n    if caracter in vocales:\n      contador += 1\n  return contador",
            "[{\"input\": \"'Hola mundo'\", \"expectedOutput\": \"4\"}, {\"input\": \"'Python'\", \"expectedOutput\": \"1\"}, {\"input\": \"'AEIOU'\", \"expectedOutput\": \"5\"}, {\"input\": \"'xyz'\", \"expectedOutput\": \"0\"}, {\"input\": \"'a'\", \"expectedOutput\": \"1\"}]",
            tema, Dificultad.EASY);

        crearProblema(308L, "Invertir Cadena",
            "Invierte una cadena de texto.",
            "def invertir_cadena(texto):\n  # Tu código aquí\n  pass",
            "def invertir_cadena(texto):\n  return texto[::-1]",
            "[{\"input\": \"'hola'\", \"expectedOutput\": \"'aloh'\"}, {\"input\": \"'python'\", \"expectedOutput\": \"'nohtyp'\"}, {\"input\": \"'a'\", \"expectedOutput\": \"'a'\"}, {\"input\": \"''\", \"expectedOutput\": \"''\"}, {\"input\": \"'12345'\", \"expectedOutput\": \"'54321'\"}]",
            tema, Dificultad.EASY);

        crearProblema(309L, "Verificar Palíndromo",
            "Verifica si una palabra es un palíndromo (se lee igual de adelante hacia atrás).",
            "def es_palindromo(palabra):\n  # Tu código aquí\n  pass",
            "def es_palindromo(palabra):\n  palabra_limpia = palabra.lower().replace(' ', '')\n  return palabra_limpia == palabra_limpia[::-1]",
            "[{\"input\": \"'ana'\", \"expectedOutput\": \"True\"}, {\"input\": \"'python'\", \"expectedOutput\": \"False\"}, {\"input\": \"'oso'\", \"expectedOutput\": \"True\"}, {\"input\": \"'radar'\", \"expectedOutput\": \"True\"}, {\"input\": \"'hola'\", \"expectedOutput\": \"False\"}]",
            tema, Dificultad.EASY);

        crearProblema(310L, "Calcular Factorial",
            "Calcula el factorial de un número entero no negativo.",
            "def factorial(n):\n  # Tu código aquí\n  pass",
            "def factorial(n):\n  if n < 0:\n    return None\n  if n == 0:\n    return 1\n  resultado = 1\n  for i in range(1, n + 1):\n    resultado *= i\n  return resultado",
            "[{\"input\": \"0\", \"expectedOutput\": \"1\"}, {\"input\": \"1\", \"expectedOutput\": \"1\"}, {\"input\": \"5\", \"expectedOutput\": \"120\"}, {\"input\": \"3\", \"expectedOutput\": \"6\"}, {\"input\": \"-1\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.EASY);

        crearProblema(311L, "Generar Lista de Números",
            "Genera una lista de números desde 1 hasta N.",
            "def generar_lista(n):\n  # Tu código aquí\n  pass",
            "def generar_lista(n):\n  return list(range(1, n + 1))",
            "[{\"input\": \"3\", \"expectedOutput\": \"[1, 2, 3]\"}, {\"input\": \"1\", \"expectedOutput\": \"[1]\"}, {\"input\": \"5\", \"expectedOutput\": \"[1, 2, 3, 4, 5]\"}, {\"input\": \"0\", \"expectedOutput\": \"[]\"}, {\"input\": \"10\", \"expectedOutput\": \"[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]\"}]",
            tema, Dificultad.EASY);

        crearProblema(312L, "Sumar Lista",
            "Suma todos los elementos de una lista de números.",
            "def sumar_lista(lista):\n  # Tu código aquí\n  pass",
            "def sumar_lista(lista):\n  return sum(lista)",
            "[{\"input\": \"[1, 2, 3, 4, 5]\", \"expectedOutput\": \"15\"}, {\"input\": \"[10, 20, 30]\", \"expectedOutput\": \"60\"}, {\"input\": \"[0, 0, 0]\", \"expectedOutput\": \"0\"}, {\"input\": \"[1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}]",
            tema, Dificultad.EASY);

        // Problemas intermedios (351-357)
        crearProblema(351L, "Función con Múltiples Parámetros",
            "Crea una función que calcule el área de un rectángulo dados su base y altura.",
            "def area_rectangulo(base, altura):\n  # Tu código aquí\n  pass",
            "def area_rectangulo(base, altura):\n  return base * altura",
            "[{\"input\": \"5, 3\", \"expectedOutput\": \"15\"}, {\"input\": \"10, 2\", \"expectedOutput\": \"20\"}, {\"input\": \"1, 1\", \"expectedOutput\": \"1\"}, {\"input\": \"7, 4\", \"expectedOutput\": \"28\"}, {\"input\": \"0, 5\", \"expectedOutput\": \"0\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(352L, "Función con Valor por Defecto",
            "Crea una función que salude a un usuario, con un saludo por defecto si no se especifica.",
            "def saludar_con_defecto(nombre, saludo=\"Hola\"):\n  # Tu código aquí\n  pass",
            "def saludar_con_defecto(nombre, saludo=\"Hola\"):\n  return f\"{saludo}, {nombre}!\"",
            "[{\"input\": \"'Ana'\", \"expectedOutput\": \"Hola, Ana!\"}, {\"input\": \"'Carlos', 'Buenos días'\", \"expectedOutput\": \"Buenos días, Carlos!\"}, {\"input\": \"'María', 'Buenas tardes'\", \"expectedOutput\": \"Buenas tardes, María!\"}, {\"input\": \"'Juan'\", \"expectedOutput\": \"Hola, Juan!\"}, {\"input\": \"'Pedro', 'Adiós'\", \"expectedOutput\": \"Adiós, Pedro!\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(353L, "Función que Retorna Múltiples Valores",
            "Crea una función que calcule el cociente y el residuo de una división.",
            "def division_con_residuo(dividendo, divisor):\n  # Tu código aquí\n  pass",
            "def division_con_residuo(dividendo, divisor):\n  if divisor == 0:\n    return None, None\n  cociente = dividendo // divisor\n  residuo = dividendo % divisor\n  return cociente, residuo",
            "[{\"input\": \"10, 3\", \"expectedOutput\": \"(3, 1)\"}, {\"input\": \"15, 5\", \"expectedOutput\": \"(3, 0)\"}, {\"input\": \"7, 2\", \"expectedOutput\": \"(3, 1)\"}, {\"input\": \"20, 4\", \"expectedOutput\": \"(5, 0)\"}, {\"input\": \"10, 0\", \"expectedOutput\": \"(None, None)\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(354L, "Función Recursiva",
            "Calcula el factorial de un número usando recursión.",
            "def factorial_recursivo(n):\n  # Tu código aquí\n  pass",
            "def factorial_recursivo(n):\n  if n < 0:\n    return None\n  if n == 0 or n == 1:\n    return 1\n  return n * factorial_recursivo(n - 1)",
            "[{\"input\": \"0\", \"expectedOutput\": \"1\"}, {\"input\": \"1\", \"expectedOutput\": \"1\"}, {\"input\": \"5\", \"expectedOutput\": \"120\"}, {\"input\": \"3\", \"expectedOutput\": \"6\"}, {\"input\": \"-1\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(355L, "Función con Lista como Parámetro",
            "Crea una función que encuentre el segundo número más grande en una lista.",
            "def segundo_mas_grande(lista):\n  # Tu código aquí\n  pass",
            "def segundo_mas_grande(lista):\n  if len(lista) < 2:\n    return None\n  lista_ordenada = sorted(lista, reverse=True)\n  return lista_ordenada[1]",
            "[{\"input\": \"[1, 5, 3, 9, 2]\", \"expectedOutput\": \"5\"}, {\"input\": \"[10, 20, 30]\", \"expectedOutput\": \"20\"}, {\"input\": \"[1, 1, 1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[5]\", \"expectedOutput\": \"None\"}, {\"input\": \"[3, 7, 1, 9, 4]\", \"expectedOutput\": \"7\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(356L, "Función con Diccionario",
            "Crea una función que cuente la frecuencia de cada elemento en una lista.",
            "def contar_frecuencia(lista):\n  # Tu código aquí\n  pass",
            "def contar_frecuencia(lista):\n  frecuencia = {}\n  for elemento in lista:\n    if elemento in frecuencia:\n      frecuencia[elemento] += 1\n    else:\n      frecuencia[elemento] = 1\n  return frecuencia",
            "[{\"input\": \"['a', 'b', 'a', 'c', 'b']\", \"expectedOutput\": \"{'a': 2, 'b': 2, 'c': 1}\"}, {\"input\": \"[1, 1, 1]\", \"expectedOutput\": \"{1: 3}\"}, {\"input\": \"['x', 'y', 'z']\", \"expectedOutput\": \"{'x': 1, 'y': 1, 'z': 1}\"}, {\"input\": \"[]\", \"expectedOutput\": \"{}\"}, {\"input\": \"[1, 2, 2, 3, 3, 3]\", \"expectedOutput\": \"{1: 1, 2: 2, 3: 3}\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(357L, "Función Lambda",
            "Crea una función que use una función lambda para elevar al cuadrado cada elemento de una lista.",
            "def elevar_cuadrado(lista):\n  # Tu código aquí\n  pass",
            "def elevar_cuadrado(lista):\n  return list(map(lambda x: x**2, lista))",
            "[{\"input\": \"[1, 2, 3, 4, 5]\", \"expectedOutput\": \"[1, 4, 9, 16, 25]\"}, {\"input\": \"[0, 1, 2]\", \"expectedOutput\": \"[0, 1, 4]\"}, {\"input\": \"[10, 20]\", \"expectedOutput\": \"[100, 400]\"}, {\"input\": \"[-1, -2, -3]\", \"expectedOutput\": \"[1, 4, 9]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.INTERMEDIATE);

        // Problemas difíciles (381-387)
        crearProblema(381L, "Función de Orden Superior",
            "Crea una función que aplique una función dada a cada elemento de una lista.",
            "def aplicar_funcion(lista, funcion):\n  # Tu código aquí\n  pass",
            "def aplicar_funcion(lista, funcion):\n  return [funcion(elemento) for elemento in lista]",
            "[{\"input\": \"[1, 2, 3], lambda x: x*2\", \"expectedOutput\": \"[2, 4, 6]\"}, {\"input\": \"['a', 'b', 'c'], lambda x: x.upper()\", \"expectedOutput\": \"['A', 'B', 'C']\"}, {\"input\": \"[1, 2, 3], lambda x: x**2\", \"expectedOutput\": \"[1, 4, 9]\"}, {\"input\": \"[True, False, True], lambda x: not x\", \"expectedOutput\": \"[False, True, False]\"}, {\"input\": \"[], lambda x: x\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.HARD);

        crearProblema(382L, "Función con Decorador",
            "Crea una función que mida el tiempo de ejecución de otra función.",
            "def medir_tiempo(funcion):\n  # Tu código aquí\n  pass",
            "import time\ndef medir_tiempo(funcion):\n  def wrapper(*args, **kwargs):\n    inicio = time.time()\n    resultado = funcion(*args, **kwargs)\n    fin = time.time()\n    return resultado, fin - inicio\n  return wrapper",
            "[{\"input\": \"lambda: sum(range(1000))\", \"expectedOutput\": \"(499500, tiempo_aproximado)\"}, {\"input\": \"lambda: 'hola'\", \"expectedOutput\": \"('hola', tiempo_aproximado)\"}, {\"input\": \"lambda: [1,2,3]\", \"expectedOutput\": \"([1,2,3], tiempo_aproximado)\"}, {\"input\": \"lambda: None\", \"expectedOutput\": \"(None, tiempo_aproximado)\"}, {\"input\": \"lambda: 42\", \"expectedOutput\": \"(42, tiempo_aproximado)\"}]",
            tema, Dificultad.HARD);

        crearProblema(383L, "Función con Generador",
            "Crea una función generadora que genere números de Fibonacci hasta un límite.",
            "def fibonacci_generador(limite):\n  # Tu código aquí\n  pass",
            "def fibonacci_generador(limite):\n  a, b = 0, 1\n  while a < limite:\n    yield a\n    a, b = b, a + b",
            "[{\"input\": \"10\", \"expectedOutput\": \"[0, 1, 1, 2, 3, 5, 8]\"}, {\"input\": \"1\", \"expectedOutput\": \"[0]\"}, {\"input\": \"100\", \"expectedOutput\": \"[0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89]\"}, {\"input\": \"0\", \"expectedOutput\": \"[]\"}, {\"input\": \"5\", \"expectedOutput\": \"[0, 1, 1, 2, 3]\"}]",
            tema, Dificultad.HARD);

        crearProblema(384L, "Función con Manejo de Excepciones",
            "Crea una función que divida dos números y maneje la excepción de división por cero.",
            "def division_segura(dividendo, divisor):\n  # Tu código aquí\n  pass",
            "def division_segura(dividendo, divisor):\n  try:\n    return dividendo / divisor\n  except ZeroDivisionError:\n    return \"Error: División por cero\"\n  except TypeError:\n    return \"Error: Tipos de datos no válidos\"",
            "[{\"input\": \"10, 2\", \"expectedOutput\": \"5.0\"}, {\"input\": \"10, 0\", \"expectedOutput\": \"Error: División por cero\"}, {\"input\": \"15, 3\", \"expectedOutput\": \"5.0\"}, {\"input\": \"'10', 2\", \"expectedOutput\": \"Error: Tipos de datos no válidos\"}, {\"input\": \"0, 5\", \"expectedOutput\": \"0.0\"}]",
            tema, Dificultad.HARD);

        crearProblema(385L, "Función con Argumentos Variables",
            "Crea una función que calcule el promedio de cualquier cantidad de números.",
            "def promedio_variable(*numeros):\n  # Tu código aquí\n  pass",
            "def promedio_variable(*numeros):\n  if not numeros:\n    return 0\n  return sum(numeros) / len(numeros)",
            "[{\"input\": \"1, 2, 3, 4, 5\", \"expectedOutput\": \"3.0\"}, {\"input\": \"10, 20\", \"expectedOutput\": \"15.0\"}, {\"input\": \"5\", \"expectedOutput\": \"5.0\"}, {\"input\": \"\", \"expectedOutput\": \"0\"}, {\"input\": \"1, 1, 1, 1, 1\", \"expectedOutput\": \"1.0\"}]",
            tema, Dificultad.HARD);

        crearProblema(386L, "Función con Argumentos de Palabra Clave",
            "Crea una función que configure un usuario con parámetros opcionales.",
            "def configurar_usuario(nombre, **kwargs):\n  # Tu código aquí\n  pass",
            "def configurar_usuario(nombre, **kwargs):\n  config = {'nombre': nombre}\n  config.update(kwargs)\n  return config",
            "[{\"input\": \"'Ana'\", \"expectedOutput\": \"{'nombre': 'Ana'}\"}, {\"input\": \"'Carlos', edad=25, ciudad='Madrid'\", \"expectedOutput\": \"{'nombre': 'Carlos', 'edad': 25, 'ciudad': 'Madrid'}\"}, {\"input\": \"'María', admin=True\", \"expectedOutput\": \"{'nombre': 'María', 'admin': True}\"}, {\"input\": \"'Juan', edad=30, email='juan@email.com', activo=False\", \"expectedOutput\": \"{'nombre': 'Juan', 'edad': 30, 'email': 'juan@email.com', 'activo': False}\"}, {\"input\": \"'Pedro'\", \"expectedOutput\": \"{'nombre': 'Pedro'}\"}]",
            tema, Dificultad.HARD);

        crearProblema(387L, "Función con Closures",
            "Crea una función que genere un contador usando closures.",
            "def crear_contador():\n  # Tu código aquí\n  pass",
            "def crear_contador():\n  contador = 0\n  def contar():\n    nonlocal contador\n    contador += 1\n    return contador\n  return contar",
            "[{\"input\": \"crear_contador()\", \"expectedOutput\": \"<function>\", \"test\": \"contador = crear_contador(); [contador() for _ in range(3)]\"}, {\"input\": \"crear_contador()\", \"expectedOutput\": \"[1, 2, 3]\", \"test\": \"contador = crear_contador(); [contador() for _ in range(3)]\"}, {\"input\": \"crear_contador()\", \"expectedOutput\": \"1\", \"test\": \"contador = crear_contador(); contador()\"}, {\"input\": \"crear_contador()\", \"expectedOutput\": \"[1, 2, 3, 4, 5]\", \"test\": \"contador = crear_contador(); [contador() for _ in range(5)]\"}, {\"input\": \"crear_contador()\", \"expectedOutput\": \"1\", \"test\": \"contador = crear_contador(); contador()\"}]",
            tema, Dificultad.HARD);

        System.out.println("Migración de problemas de funciones completada exitosamente!");
    }

    public void migrateListProblems() {
        // Crear o encontrar el tema 'Listas y Arrays'
        Tema tema = temaRepository.findByNombre("Listas y Arrays")
                .orElseGet(() -> {
                    Tema nuevoTema = new Tema();
                    nuevoTema.setNombre("Listas y Arrays");
                    nuevoTema.setDescripcion("Problemas de listas y arrays en Python");
                    return temaRepository.save(nuevoTema);
                });

        // Migrar problemas fáciles (401-412)
        crearProblema(401L, "Crear y Acceder",
            "Crea una lista con los números 1, 2, 3. Luego, devuelve el segundo elemento.",
            "def acceder_lista():\n  mi_lista = [1, 2, 3]\n  # Tu código aquí\n  return None",
            "def acceder_lista():\n  mi_lista = [1, 2, 3]\n  return mi_lista[1]",
            "[{\"input\": \"acceder_lista()\", \"expectedOutput\": \"2\"}]",
            tema, Dificultad.EASY);

        crearProblema(402L, "Longitud de Lista",
            "Dada una lista, devuelve cuántos elementos contiene.",
            "def longitud_de_lista(lista):\n  # Tu código aquí\n  return 0",
            "def longitud_de_lista(lista):\n  return len(lista)",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"5\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}, {\"input\": \"['a','b','c']\", \"expectedOutput\": \"3\"}, {\"input\": \"[10,20]\", \"expectedOutput\": \"2\"}, {\"input\": \"[True,False,None]\", \"expectedOutput\": \"3\"}]",
            tema, Dificultad.EASY);

        crearProblema(403L, "Sumar Elementos",
            "Dada una lista de números, devuelve la suma de todos los elementos.",
            "def sumar_elementos(lista):\n  # Tu código aquí\n  return 0",
            "def sumar_elementos(lista):\n  return sum(lista)",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"15\"}, {\"input\": \"[10,20,30]\", \"expectedOutput\": \"60\"}, {\"input\": \"[0,0,0]\", \"expectedOutput\": \"0\"}, {\"input\": \"[1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}]",
            tema, Dificultad.EASY);

        crearProblema(404L, "Encontrar Máximo",
            "Dada una lista de números, devuelve el número más grande.",
            "def encontrar_maximo(lista):\n  # Tu código aquí\n  return 0",
            "def encontrar_maximo(lista):\n  if not lista:\n    return None\n  return max(lista)",
            "[{\"input\": \"[1,5,3,9,2]\", \"expectedOutput\": \"9\"}, {\"input\": \"[-1,-5,-3]\", \"expectedOutput\": \"-1\"}, {\"input\": \"[10]\", \"expectedOutput\": \"10\"}, {\"input\": \"[1,1,1,1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.EASY);

        crearProblema(405L, "Encontrar Mínimo",
            "Dada una lista de números, devuelve el número más pequeño.",
            "def encontrar_minimo(lista):\n  # Tu código aquí\n  return 0",
            "def encontrar_minimo(lista):\n  if not lista:\n    return None\n  return min(lista)",
            "[{\"input\": \"[1,5,3,9,2]\", \"expectedOutput\": \"1\"}, {\"input\": \"[-1,-5,-3]\", \"expectedOutput\": \"-5\"}, {\"input\": \"[10]\", \"expectedOutput\": \"10\"}, {\"input\": \"[1,1,1,1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}]",
            tema, Dificultad.EASY);

        crearProblema(406L, "Agregar Elemento",
            "Dada una lista y un elemento, agrega el elemento al final de la lista y devuelve la lista.",
            "def agregar_elemento(lista, elemento):\n  # Tu código aquí\n  return lista",
            "def agregar_elemento(lista, elemento):\n  lista.append(elemento)\n  return lista",
            "[{\"input\": \"[1,2,3], 4\", \"expectedOutput\": \"[1,2,3,4]\"}, {\"input\": \"[], 'a'\", \"expectedOutput\": \"['a']\"}, {\"input\": \"['x','y'], 'z'\", \"expectedOutput\": \"['x','y','z']\"}, {\"input\": \"[True], False\", \"expectedOutput\": \"[True,False]\"}, {\"input\": \"[10,20], 30\", \"expectedOutput\": \"[10,20,30]\"}]",
            tema, Dificultad.EASY);

        crearProblema(407L, "Remover Elemento",
            "Dada una lista y un elemento, remueve la primera ocurrencia del elemento y devuelve la lista.",
            "def remover_elemento(lista, elemento):\n  # Tu código aquí\n  return lista",
            "def remover_elemento(lista, elemento):\n  if elemento in lista:\n    lista.remove(elemento)\n  return lista",
            "[{\"input\": \"[1,2,3,2], 2\", \"expectedOutput\": \"[1,3,2]\"}, {\"input\": \"['a','b','c'], 'b'\", \"expectedOutput\": \"['a','c']\"}, {\"input\": \"[1,2,3], 4\", \"expectedOutput\": \"[1,2,3]\"}, {\"input\": \"[], 1\", \"expectedOutput\": \"[]\"}, {\"input\": \"[10,20,30], 20\", \"expectedOutput\": \"[10,30]\"}]",
            tema, Dificultad.EASY);

        crearProblema(408L, "Invertir Lista",
            "Dada una lista, devuelve una nueva lista con los elementos en orden inverso.",
            "def invertir_lista(lista):\n  # Tu código aquí\n  return lista",
            "def invertir_lista(lista):\n  return lista[::-1]",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"[5,4,3,2,1]\"}, {\"input\": \"['a','b','c']\", \"expectedOutput\": \"['c','b','a']\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[1]\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[True,False]\", \"expectedOutput\": \"[False,True]\"}]",
            tema, Dificultad.EASY);

        crearProblema(409L, "Contar Elementos",
            "Dada una lista y un elemento, cuenta cuántas veces aparece el elemento en la lista.",
            "def contar_elementos(lista, elemento):\n  # Tu código aquí\n  return 0",
            "def contar_elementos(lista, elemento):\n  return lista.count(elemento)",
            "[{\"input\": \"[1,2,2,3,2], 2\", \"expectedOutput\": \"3\"}, {\"input\": \"['a','b','a'], 'a'\", \"expectedOutput\": \"2\"}, {\"input\": \"[1,2,3], 4\", \"expectedOutput\": \"0\"}, {\"input\": \"[], 1\", \"expectedOutput\": \"0\"}, {\"input\": \"[True,False,True], True\", \"expectedOutput\": \"2\"}]",
            tema, Dificultad.EASY);

        crearProblema(410L, "Verificar Elemento",
            "Dada una lista y un elemento, devuelve True si el elemento está en la lista, False en caso contrario.",
            "def verificar_elemento(lista, elemento):\n  # Tu código aquí\n  return False",
            "def verificar_elemento(lista, elemento):\n  return elemento in lista",
            "[{\"input\": \"[1,2,3,4,5], 3\", \"expectedOutput\": \"True\"}, {\"input\": \"['a','b','c'], 'd'\", \"expectedOutput\": \"False\"}, {\"input\": \"[10,20,30], 20\", \"expectedOutput\": \"True\"}, {\"input\": \"[], 1\", \"expectedOutput\": \"False\"}, {\"input\": \"[True,False], True\", \"expectedOutput\": \"True\"}]",
            tema, Dificultad.EASY);

        crearProblema(411L, "Obtener Último",
            "Dada una lista, devuelve el último elemento. Si la lista está vacía, devuelve None.",
            "def obtener_ultimo(lista):\n  # Tu código aquí\n  return None",
            "def obtener_ultimo(lista):\n  if not lista:\n    return None\n  return lista[-1]",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"5\"}, {\"input\": \"['a','b','c']\", \"expectedOutput\": \"'c'\"}, {\"input\": \"[10]\", \"expectedOutput\": \"10\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}, {\"input\": \"[True,False,True]\", \"expectedOutput\": \"True\"}]",
            tema, Dificultad.EASY);

        crearProblema(412L, "Obtener Primero",
            "Dada una lista, devuelve el primer elemento. Si la lista está vacía, devuelve None.",
            "def obtener_primero(lista):\n  # Tu código aquí\n  return None",
            "def obtener_primero(lista):\n  if not lista:\n    return None\n  return lista[0]",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"1\"}, {\"input\": \"['a','b','c']\", \"expectedOutput\": \"'a'\"}, {\"input\": \"[10]\", \"expectedOutput\": \"10\"}, {\"input\": \"[]\", \"expectedOutput\": \"None\"}, {\"input\": \"[True,False,True]\", \"expectedOutput\": \"True\"}]",
            tema, Dificultad.EASY);

        // Problemas intermedios (451-457)
        crearProblema(451L, "Encontrar Índice de Elemento",
            "Dada una lista y un elemento, devuelve el índice de la primera ocurrencia del elemento. Si no está, devuelve -1.",
            "def indice_de_elemento(lista, elemento):\n  # Tu código aquí\n  return -1",
            "def indice_de_elemento(lista, elemento):\n  try:\n    return lista.index(elemento)\n  except ValueError:\n    return -1",
            "[{\"input\": \"[10,20,30,20], 20\", \"expectedOutput\": \"1\"}, {\"input\": \"['x'], 'y'\", \"expectedOutput\": \"-1\"}, {\"input\": \"[], 1\", \"expectedOutput\": \"-1\"}, {\"input\": \"['a','b','c'], 'b'\", \"expectedOutput\": \"1\"}, {\"input\": \"[1,2,3,4], 4\", \"expectedOutput\": \"3\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(452L, "Ordenar Lista (Ascendente)",
            "Dada una lista de números o cadenas, devuélvela ordenada de forma ascendente.",
            "def ordenar_lista_asc(lista):\n  # Tu código aquí\n  return lista",
            "def ordenar_lista_asc(lista):\n  return sorted(lista)",
            "[{\"input\": \"['banana', 'apple', 'cherry']\", \"expectedOutput\": \"['apple', 'banana', 'cherry']\"}, {\"input\": \"[5,2,8,1]\", \"expectedOutput\": \"[1,2,5,8]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[3,1,4,1,5,9]\", \"expectedOutput\": \"[1,1,3,4,5,9]\"}, {\"input\": \"[10,5,15,20]\", \"expectedOutput\": \"[5,10,15,20]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(453L, "Eliminar Elemento por Índice",
            "Dada una lista y un índice, elimina el elemento en ese índice. Si el índice es inválido, no hagas nada. Devuelve la lista.",
            "def eliminar_por_indice_lista(lista, indice):\n  # Tu código aquí\n  return lista",
            "def eliminar_por_indice_lista(lista, indice):\n  if 0 <= indice < len(lista):\n    del lista[indice]\n  return lista",
            "[{\"input\": \"['a','b','c'], 0\", \"expectedOutput\": \"['b','c']\"}, {\"input\": \"[1,2,3], 5\", \"expectedOutput\": \"[1,2,3]\"}, {\"input\": \"[1,2,3], -1\", \"expectedOutput\": \"[1,2]\"}, {\"input\": \"[10,20,30], 1\", \"expectedOutput\": \"[10,30]\"}, {\"input\": \"[100], 0\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(454L, "Mapear Lista: Cuadrados",
            "Dada una lista de números, devuelve una nueva lista donde cada número es el cuadrado del original.",
            "def lista_cuadrados(numeros):\n  # nueva_lista = []\n  # Tu código aquí\n  return nueva_lista",
            "def lista_cuadrados(numeros):\n  return [x**2 for x in numeros]",
            "[{\"input\": \"[0, 5, -2]\", \"expectedOutput\": \"[0, 25, 4]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[10]\", \"expectedOutput\": \"[100]\"}, {\"input\": \"[1,2,3,4]\", \"expectedOutput\": \"[1,4,9,16]\"}, {\"input\": \"[-3,3]\", \"expectedOutput\": \"[9,9]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(455L, "Filtrar Números Pares",
            "Dada una lista de números, devuelve una nueva lista que contenga solo los números pares.",
            "def filtrar_pares(numeros):\n  # Tu código aquí\n  return []",
            "def filtrar_pares(numeros):\n  return [x for x in numeros if x % 2 == 0]",
            "[{\"input\": \"[1,2,3,4,5,6]\", \"expectedOutput\": \"[2,4,6]\"}, {\"input\": \"[1,3,5,7]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[0,2,4,6]\", \"expectedOutput\": \"[0,2,4,6]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[10,15,20,25]\", \"expectedOutput\": \"[10,20]\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(456L, "Promedio de Lista",
            "Dada una lista de números, calcula y devuelve el promedio (media aritmética).",
            "def promedio_lista(numeros):\n  # Tu código aquí\n  return 0",
            "def promedio_lista(numeros):\n  if not numeros:\n    return 0\n  return sum(numeros) / len(numeros)",
            "[{\"input\": \"[1,2,3,4,5]\", \"expectedOutput\": \"3.0\"}, {\"input\": \"[10,20,30]\", \"expectedOutput\": \"20.0\"}, {\"input\": \"[0,0,0]\", \"expectedOutput\": \"0.0\"}, {\"input\": \"[1]\", \"expectedOutput\": \"1.0\"}, {\"input\": \"[]\", \"expectedOutput\": \"0\"}]",
            tema, Dificultad.INTERMEDIATE);

        crearProblema(457L, "Unir Listas",
            "Dadas dos listas, devuelve una nueva lista que contenga todos los elementos de ambas listas.",
            "def unir_listas(lista1, lista2):\n  # Tu código aquí\n  return []",
            "def unir_listas(lista1, lista2):\n  return lista1 + lista2",
            "[{\"input\": \"[1,2,3], [4,5,6]\", \"expectedOutput\": \"[1,2,3,4,5,6]\"}, {\"input\": \"[], [1,2,3]\", \"expectedOutput\": \"[1,2,3]\"}, {\"input\": \"['a','b'], ['c','d']\", \"expectedOutput\": \"['a','b','c','d']\"}, {\"input\": \"[1], []\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[], []\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.INTERMEDIATE);

        // Problemas difíciles (481-487)
        crearProblema(481L, "Eliminar Duplicados",
            "Dada una lista, devuelve una nueva lista sin elementos duplicados, manteniendo el orden original.",
            "def eliminar_duplicados(lista):\n  # Tu código aquí\n  return lista",
            "def eliminar_duplicados(lista):\n  resultado = []\n  for elemento in lista:\n    if elemento not in resultado:\n      resultado.append(elemento)\n  return resultado",
            "[{\"input\": \"[1,2,2,3,3,4]\", \"expectedOutput\": \"[1,2,3,4]\"}, {\"input\": \"['a','b','a','c','b']\", \"expectedOutput\": \"['a','b','c']\"}, {\"input\": \"[1,1,1,1]\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[10,20,10,30,20]\", \"expectedOutput\": \"[10,20,30]\"}]",
            tema, Dificultad.HARD);

        crearProblema(482L, "Encontrar Segundo Más Grande",
            "Dada una lista de números, encuentra el segundo número más grande.",
            "def segundo_mas_grande(lista):\n  # Tu código aquí\n  return 0",
            "def segundo_mas_grande(lista):\n  if len(lista) < 2:\n    return None\n  lista_ordenada = sorted(lista, reverse=True)\n  return lista_ordenada[1]",
            "[{\"input\": \"[1,5,3,9,2]\", \"expectedOutput\": \"5\"}, {\"input\": \"[10,20,30]\", \"expectedOutput\": \"20\"}, {\"input\": \"[1,1,1]\", \"expectedOutput\": \"1\"}, {\"input\": \"[5]\", \"expectedOutput\": \"None\"}, {\"input\": \"[3,7,1,9,4]\", \"expectedOutput\": \"7\"}]",
            tema, Dificultad.HARD);

        crearProblema(483L, "Rotar Lista",
            "Dada una lista y un número n, rota la lista n posiciones hacia la derecha. Si n es negativo, rota hacia la izquierda.",
            "def rotar_lista(lista, n):\n  # Tu código aquí\n  return lista",
            "def rotar_lista(lista, n):\n  if not lista:\n    return lista\n  n = n % len(lista)\n  return lista[-n:] + lista[:-n]",
            "[{\"input\": \"[1,2,3,4,5], 2\", \"expectedOutput\": \"[4,5,1,2,3]\"}, {\"input\": \"['a','b','c'], 1\", \"expectedOutput\": \"['c','a','b']\"}, {\"input\": \"[1,2,3], -1\", \"expectedOutput\": \"[2,3,1]\"}, {\"input\": \"[1], 5\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[], 3\", \"expectedOutput\": \"[]\"}]",
            tema, Dificultad.HARD);

        crearProblema(484L, "Intersección de Dos Listas (sin repetidos)",
            "Dadas dos listas, devuelve una nueva lista con los elementos comunes a ambas, sin repetidos.",
            "def interseccion_listas_sin_repetir(lista1, lista2):\n  # Tu código aquí\n  return []",
            "def interseccion_listas_sin_repetir(lista1, lista2):\n  return list(set(lista1) & set(lista2))",
            "[{\"input\": \"['a','b','c'], ['b','d','a']\", \"expectedOutput\": \"['a','b']\"}, {\"input\": \"[1,2,3], [4,5,6]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[1,1,1], [1,1]\", \"expectedOutput\": \"[1]\"}, {\"input\": \"[1,2,2,3,4], [2,3,3,5]\", \"expectedOutput\": \"[2,3]\"}, {\"input\": \"[10,20,30], [5,20,35]\", \"expectedOutput\": \"[20]\"}]",
            tema, Dificultad.HARD);

        crearProblema(485L, "Generar Todas las Sublistas (Power Set Simplificado)",
            "Dada una lista, genera todas las posibles sublistas (incluyendo la lista vacía y la lista misma). Devuelve una lista de listas.",
            "def generar_sublistas(lista):\n  # resultado = [[]]\n  # Tu código aquí\n  return resultado",
            "def generar_sublistas(lista):\n  resultado = [[]]\n  for i in range(len(lista)):\n    for j in range(i + 1, len(lista) + 1):\n      resultado.append(lista[i:j])\n  return resultado",
            "[{\"input\": \"[1]\", \"expectedOutput\": \"[[], [1]]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[[]]\"}, {\"input\": \"['a']\", \"expectedOutput\": \"[[], ['a']]\"}, {\"input\": \"[1,2]\", \"expectedOutput\": \"[[], [1], [2], [1,2]]\"}, {\"input\": \"[0]\", \"expectedOutput\": \"[[], [0]]\"}]",
            tema, Dificultad.HARD);

        crearProblema(486L, "Ordenar Lista de Listas",
            "Dada una lista de listas, ordénala por el segundo elemento de cada sublista.",
            "def ordenar_por_segundo_elemento(lista_de_listas):\n  # Tu código aquí\n  return lista_de_listas",
            "def ordenar_por_segundo_elemento(lista_de_listas):\n  return sorted(lista_de_listas, key=lambda x: x[1])",
            "[{\"input\": \"[[1,3], [2,1], [3,2]]\", \"expectedOutput\": \"[[2,1], [3,2], [1,3]]\"}, {\"input\": \"[['a',2], ['b',1], ['c',3]]\", \"expectedOutput\": \"[['b',1], ['a',2], ['c',3]]\"}, {\"input\": \"[[1,1], [2,1], [3,1]]\", \"expectedOutput\": \"[[1,1], [2,1], [3,1]]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[[10,5], [20,15], [30,10]]\", \"expectedOutput\": \"[[10,5], [30,10], [20,15]]\"}]",
            tema, Dificultad.HARD);

        crearProblema(487L, "Aplanar Lista Anidada",
            "Dada una lista que puede contener otras listas anidadas, devuelve una lista plana con todos los elementos.",
            "def aplanar_lista(lista):\n  # Tu código aquí\n  return []",
            "def aplanar_lista(lista):\n  resultado = []\n  for elemento in lista:\n    if isinstance(elemento, list):\n      resultado.extend(aplanar_lista(elemento))\n    else:\n      resultado.append(elemento)\n  return resultado",
            "[{\"input\": \"[1, [2, 3], [4, [5, 6]]]\", \"expectedOutput\": \"[1, 2, 3, 4, 5, 6]\"}, {\"input\": \"[[1, 2], [3, 4]]\", \"expectedOutput\": \"[1, 2, 3, 4]\"}, {\"input\": \"[1, 2, 3]\", \"expectedOutput\": \"[1, 2, 3]\"}, {\"input\": \"[]\", \"expectedOutput\": \"[]\"}, {\"input\": \"[[1], [2], [3]]\", \"expectedOutput\": \"[1, 2, 3]\"}]",
            tema, Dificultad.HARD);

        System.out.println("Migración de problemas de listas completada exitosamente!");
    }

    public void updateMissingSolutions() {
        // Actualizar soluciones faltantes
        updateSolution(102L, "def verificar_signo(numero):\n  if numero > 0:\n    return \"Positivo\"\n  elif numero < 0:\n    return \"Negativo\"\n  else:\n    return \"Cero\"");
        
        updateSolution(103L, "def es_vocal_simple(letra):\n  if letra in ['a', 'e', 'i', 'o', 'u']:\n    return \"Vocal\"\n  else:\n    return \"Consonante\"");
        
        updateSolution(104L, "def verificar_acceso(clave):\n  if clave == \"1234\":\n    return \"Acceso Permitido\"\n  else:\n    return \"Acceso Denegado\"");
        
        updateSolution(105L, "def es_fin_de_semana(dia):\n  if dia in ['sabado', 'domingo']:\n    return \"Fin de semana\"\n  else:\n    return \"Día de semana\"");
        
        updateSolution(106L, "def par_o_impar(numero):\n  if numero % 2 == 0:\n    return \"Par\"\n  else:\n    return \"Impar\"");
        
        updateSolution(107L, "def accion_semaforo_simple(color):\n  if color == 'rojo':\n    return \"Detenerse\"\n  elif color == 'verde':\n    return \"Avanzar\"");
        
        updateSolution(108L, "def mayor_que_diez(num):\n  if num > 10:\n    return \"Mayor que 10\"\n  else:\n    return \"No es mayor que 10\"");
        
        updateSolution(109L, "def estado_calificacion(nota):\n  if nota >= 5:\n    return \"Aprobado\"\n  else:\n    return \"Reprobado\"");
        
        updateSolution(110L, "def saludo_horario(hora):\n  if hora < 12:\n    return \"Buenos días\"\n  else:\n    return \"Buenas tardes/noches\"");
        
        updateSolution(111L, "def bebida_permitida(edad):\n  if edad >= 21:\n    return \"Cerveza\"\n  else:\n    return \"Jugo\"");
        
        updateSolution(152L, "def calcular_descuento_detallado(monto):\n  if monto > 200:\n    return monto * 0.85\n  elif monto > 100:\n    return monto * 0.9\n  else:\n    return float(monto)");
        
        updateSolution(153L, "def tipo_angulo_completo(angulo):\n  if angulo < 90:\n    return \"Agudo\"\n  elif angulo == 90:\n    return \"Recto\"\n  elif angulo < 180:\n    return \"Obtuso\"\n  elif angulo == 180:\n    return \"Llano\"\n  elif angulo < 360:\n    return \"Cóncavo\"\n  else:\n    return \"Completo\"");
        
        updateSolution(154L, "def maximo_de_tres(a, b, c):\n  if a >= b and a >= c:\n    return a\n  elif b >= a and b >= c:\n    return b\n  else:\n    return c");
        
        updateSolution(155L, "def clasificar_imc(peso, altura):\n  imc = peso / (altura ** 2)\n  imc_rounded = round(imc, 2)\n  imc_str = f\"{imc_rounded:.2f}\"\n  if imc < 18.5:\n    return f\"Bajo peso (IMC: {imc_str})\"\n  elif imc <= 24.9:\n    return f\"Normal (IMC: {imc_str})\"\n  elif imc <= 29.9:\n    return f\"Sobrepeso (IMC: {imc_str})\"\n  else:\n    return f\"Obesidad (IMC: {imc_str})\"");
        
        updateSolution(156L, "def tarifa_envio(peso_kg):\n  if peso_kg < 1:\n    return \"5\"\n  elif peso_kg <= 5:\n    return \"10\"\n  else:\n    return \"15\"");
        
        updateSolution(157L, "def dias_en_mes(numero_mes):\n  if numero_mes in [1, 3, 5, 7, 8, 10, 12]:\n    return \"31\"\n  elif numero_mes in [4, 6, 9, 11]:\n    return \"30\"\n  elif numero_mes == 2:\n    return \"28\"");
        
        updateSolution(182L, "def es_bisiesto_detallado(anio):\n  if anio % 400 == 0:\n    return \"Bisiesto\"\n  elif anio % 100 == 0:\n    return \"No Bisiesto\"\n  elif anio % 4 == 0:\n    return \"Bisiesto\"\n  else:\n    return \"No Bisiesto\"");
        
        updateSolution(183L, "def calcular_impuestos(renta):\n  if renta <= 10000:\n    return \"0.0\"\n  elif renta <= 30000:\n    impuesto = (renta - 10000) * 0.1\n    return str(float(impuesto))\n  else:\n    impuesto = 2000 + (renta - 30000) * 0.2\n    return str(float(impuesto))");
        
        updateSolution(184L, "def cajero_automatico(saldo_inicial, monto_retiro):\n  if monto_retiro % 10 != 0:\n    return \"Monto inválido\"\n  elif monto_retiro > saldo_inicial:\n    return \"Fondos insuficientes\"\n  else:\n    nuevo_saldo = saldo_inicial - monto_retiro\n    return f\"Nuevo Saldo: {nuevo_saldo}\"");
        
        updateSolution(185L, "def ppt(jugador1, jugador2):\n  if jugador1 == jugador2:\n    return \"Empate\"\n  elif (jugador1 == 'piedra' and jugador2 == 'tijera') or \\\n       (jugador1 == 'papel' and jugador2 == 'piedra') or \\\n       (jugador1 == 'tijera' and jugador2 == 'papel'):\n    return \"Gana Jugador 1\"\n  else:\n    return \"Gana Jugador 2\"");
        
        updateSolution(186L, "def validar_contrasena_simple(clave):\n  if len(clave) >= 8 and any(c.isdigit() for c in clave):\n    return \"Válida\"\n  else:\n    return \"Inválida\"");
        
        updateSolution(187L, "def estacion_del_ano(mes):\n  if mes in [12, 1, 2]:\n    return \"Invierno\"\n  elif mes in [3, 4, 5]:\n    return \"Primavera\"\n  elif mes in [6, 7, 8]:\n    return \"Verano\"\n  else:\n    return \"Otoño\"");
        
        System.out.println("Actualización de soluciones completada exitosamente!");
    }

    private void updateSolution(Long id, String solution) {
        problemaRepository.findById(id).ifPresent(problema -> {
            problema.setSolucionCorrecta(solution);
            problemaRepository.save(problema);
        });
    }

    private void crearProblema(Long id, String titulo, String descripcion, String codigoInicial, 
                              String solucionCorrecta, String testCasesJson, Tema tema, Dificultad dificultad) {
        if (!problemaRepository.existsById(id)) {
            Problema problema = new Problema();
            problema.setId(id);
            problema.setTitulo(titulo);
            problema.setDescripcion(descripcion);
            problema.setCodigoInicial(codigoInicial);
            problema.setSolucionCorrecta(solucionCorrecta);
            problema.setTestCasesJson(testCasesJson);
            problema.setTema(tema);
            problema.setDificultad(dificultad);
            problemaRepository.save(problema);
        }
    }
}