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