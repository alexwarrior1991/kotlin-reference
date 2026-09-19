package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.math.pow

// =====================================================================================
//  Ejercicio 1 · Calculadora simple                                        🟢 fácil
//
//  Repasa: when, sealed interface, funciones, validación, parseo básico.
//  Capítulos: 04 (control de flujo), 05 (funciones), 11 (sealed), 19 (errores).
// =====================================================================================

/** Ejercicio 1: calculadora simple. */
fun ejercicio01Calculadora() {
    enunciado(
        "Escribe una calculadora de dos operandos que NUNCA lance excepciones.",
        "",
        "1. `calcular(a, operador, b)` con los operadores + - * / % y ^.",
        "2. Debe devolver un resultado que distinga entre éxito y error, en vez",
        "   de lanzar o devolver un Double raro.",
        "3. Errores que hay que contemplar: dividir entre cero, módulo de cero,",
        "   y un operador que no existe.",
        "4. `evaluarExpresion(\"3 + 4\")` que acepte una línea de texto con espacios",
        "   y devuelva lo mismo que `calcular`.",
        "5. La expresión puede venir mal escrita: `\"3 +\"`, `\"hola * 2\"`, `\"\"`.",
    )

    pistas(
        "Un `sealed interface` con dos implementaciones (Ok y Error) te da un",
        "   resultado que el `when` obliga a tratar por completo (capítulo 11).",
        "Para los operadores, `when (operador) { '+' -> ... }` con `else` al final.",
        "Dividir entre 0.0 en Double NO lanza: da Infinity o NaN. Compruébalo antes.",
        "Para el texto: `split(\" \")` deja trozos vacíos si hay espacios dobles;",
        "   `split(' ').filter { it.isNotBlank() }` o `trim().split(Regex(\"\\\\s+\"))`.",
        "`toDoubleOrNull()` es tu amigo: devuelve null en vez de lanzar (capítulo 06).",
    )

    solucionEnMarcha()

    section("Operaciones correctas")

    listOf(
        Triple(6.0, '+', 3.0),
        Triple(6.0, '-', 3.0),
        Triple(6.0, '*', 3.0),
        Triple(6.0, '/', 3.0),
        Triple(7.0, '%', 3.0),
        Triple(2.0, '^', 10.0),
    ).forEach { (a, op, b) ->
        show("calcular($a, '$op', $b)", describirCalculo(calcular(a, op, b)))
    }

    section("Errores")

    listOf(
        Triple(1.0, '/', 0.0),
        Triple(1.0, '%', 0.0),
        Triple(1.0, '?', 2.0),
    ).forEach { (a, op, b) ->
        show("calcular($a, '$op', $b)", describirCalculo(calcular(a, op, b)))
    }

    section("Desde texto")

    listOf("3 + 4", "  10   /  4 ", "2^8", "3 +", "hola * 2", "", "1 + 2 + 3").forEach { texto ->
        show("evaluarExpresion(\"$texto\")", describirCalculo(evaluarExpresion(texto)))
    }

    explicacion(
        "El `sealed interface` es la pieza central: quien llame a `calcular` NO",
        "puede ignorar el caso de error, porque el `when` no compila sin él.",
        "",
        "Con un `Double` a secas tendrías que devolver NaN o Infinity y confiar en",
        "que alguien se acuerde de comprobarlo. Con una excepción obligarías a un",
        "try/catch para algo que no tiene nada de excepcional: dividir entre cero",
        "es un caso NORMAL en una calculadora (la tabla del capítulo 19).",
        "",
        "`evaluarExpresion` está separada de `calcular` a propósito: una parsea y la",
        "otra calcula. Así `calcular` se puede probar sin pensar en el texto, y el",
        "parseo se puede cambiar (¿notación polaca?) sin tocar la aritmética.",
        "",
        "Fíjate en que `2^8` sin espacios también funciona: el parser busca el",
        "operador en vez de exigir un formato rígido.",
    )

    varianteDificil(
        "1. Soporta varios operandos con precedencia: `2 + 3 * 4` debe dar 14, no 20.",
        "   (Pista: un parser descendente recursivo, o el algoritmo shunting-yard.)",
        "2. Añade paréntesis: `(2 + 3) * 4`.",
        "3. Añade funciones de un argumento: `raiz(16)`, `abs(-3)`.",
        "4. Añade variables: `x = 5` y luego `x * 2`, guardando el estado entre líneas.",
        "5. Acumula TODOS los errores de una expresión en vez de parar en el primero.",
    )

    testEn("CalculadoraTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/**
 * El resultado de un cálculo: o sale bien, o sale mal con un motivo.
 *
 * `sealed` obliga a quien lo reciba a tratar los dos casos (capítulo 11).
 */
sealed interface ResultadoCalculo {
    data class Ok(val valor: Double) : ResultadoCalculo
    data class Error(val motivo: String) : ResultadoCalculo
}

/** Los operadores que entiende la calculadora. Un solo sitio que cambiar. */
private val OPERADORES = charArrayOf('+', '-', '*', '/', '%', '^')

/**
 * Calcula `a operador b`.
 *
 * No lanza nunca: los errores viajan dentro del resultado.
 */
fun calcular(a: Double, operador: Char, b: Double): ResultadoCalculo = when (operador) {
    '+' -> ResultadoCalculo.Ok(a + b)
    '-' -> ResultadoCalculo.Ok(a - b)
    '*' -> ResultadoCalculo.Ok(a * b)

    // Ojo: en Double, 1.0 / 0.0 NO lanza; devuelve Infinity. Hay que comprobarlo.
    '/' -> if (b == 0.0) {
        ResultadoCalculo.Error("no se puede dividir entre cero")
    } else {
        ResultadoCalculo.Ok(a / b)
    }

    '%' -> if (b == 0.0) {
        ResultadoCalculo.Error("no se puede sacar el resto de cero")
    } else {
        ResultadoCalculo.Ok(a % b)
    }

    '^' -> {
        val potencia = a.pow(b)
        if (potencia.isFinite()) {
            ResultadoCalculo.Ok(potencia)
        } else {
            ResultadoCalculo.Error("el resultado de $a^$b no es un número finito")
        }
    }

    else -> ResultadoCalculo.Error("operador desconocido: '$operador'")
}

/**
 * Evalúa una expresión de dos operandos escrita a mano.
 *
 * Admite espacios de sobra y también ninguno: busca el operador dentro del texto en
 * lugar de exigir un formato concreto.
 */
fun evaluarExpresion(texto: String): ResultadoCalculo {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return ResultadoCalculo.Error("la expresión está vacía")

    // Se busca desde la posición 1 para no confundir el signo de un número
    // negativo ("-3 + 4") con el operador.
    val posicion = (1 until limpio.length).firstOrNull { limpio[it] in OPERADORES }
        ?: return ResultadoCalculo.Error("no encuentro ningún operador en «$limpio»")

    val izquierda = limpio.substring(0, posicion).trim()
    val derecha = limpio.substring(posicion + 1).trim()

    val a = izquierda.toDoubleOrNull()
        ?: return ResultadoCalculo.Error("«$izquierda» no es un número")
    val b = derecha.toDoubleOrNull()
        ?: return ResultadoCalculo.Error(
            if (derecha.isEmpty()) "falta el segundo operando" else "«$derecha» no es un número",
        )

    return calcular(a, limpio[posicion], b)
}

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun describirCalculo(resultado: ResultadoCalculo): String = when (resultado) {
    // El `when` sobre un sealed no necesita `else`: el compilador sabe que son dos.
    is ResultadoCalculo.Ok -> formatear(resultado.valor)
    is ResultadoCalculo.Error -> "✗ ${resultado.motivo}"
}

/** 4.0 se lee mejor como "4"; 3.3333333333333335, como "3,3333". */
private fun formatear(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString() else "%.4f".format(valor)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade el operador `//` (división entera) y comprueba qué se rompe del parser.
//  2. Haz que `evaluarExpresion("-3 + 4")` funcione; luego prueba con "-3 - -4".
//  3. Cambia el sealed por un `Result<Double>` y compara qué se gana y qué se pierde.
