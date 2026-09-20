package com.alejandro.c04controlflow

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  4.1 · if, como sentencia y como expresión
//
//  QUÉ ES
//    El condicional de siempre, con una diferencia importante: en Kotlin devuelve un
//    valor.
//
//  POR QUÉ IMPORTA
//    Que `if` sea una expresión elimina la necesidad del operador ternario, permite
//    inicializar un `val` en una sola sentencia y hace posible escribir funciones de
//    una sola expresión (capítulo 5).
//
//  ERRORES COMUNES
//    · Buscar el operador ternario `? :`. No existe: no hace falta.
//    · Usar `if` como expresión sin `else` (no compila).
//    · Encadenar `if/else if` largos donde un `when` se leería mucho mejor.
// =====================================================================================

/**
 * `if` como sentencia: la forma clásica.
 */
fun demoIfAsStatement() {
    section("Uso normal")

    val temperatura = 31

    if (temperatura > 30) {
        show("temperatura $temperatura", "hace calor")
    } else if (temperatura > 15) {
        show("temperatura $temperatura", "se está bien")
    } else {
        show("temperatura $temperatura", "hace frío")
    }

    section("Sin else")

    // Como sentencia, el `else` es opcional.
    val saldo = -20
    if (saldo < 0) {
        show("saldo $saldo", "número rojo")
    }

    section("Llaves opcionales para una sola línea")

    val n = 7
    if (n % 2 == 0) show("$n", "par") else show("$n", "impar")

    bullet("Con una sola sentencia las llaves pueden omitirse...")
    bullet("...pero la guía de estilo recomienda ponerlas salvo en líneas muy cortas.")
}

/**
 * `if` como expresión: devuelve un valor.
 */
fun demoIfAsExpression() {
    section("Asignar el resultado")

    val a = 15
    val b = 42

    // En Java: int maximo = (a > b) ? a : b;
    val maximo = if (a > b) a else b
    show("val maximo = if (a > b) a else b", maximo)

    // Aquí el `else` es OBLIGATORIO: si faltara, habría un camino sin valor.
    // val roto = if (a > b) a          // ERROR: 'if' must have both main and 'else' branches

    section("Con bloques: vale la ÚLTIMA expresión del bloque")

    val descripcion = if (a > b) {
        val diferencia = a - b
        "a gana por $diferencia"       // ← este valor es el del bloque
    } else {
        val diferencia = b - a
        "b gana por $diferencia"
    }
    show("if con bloques", descripcion)

    bullet("El valor de un bloque es el de su última expresión. Sin `return`.")
    bullet("Un `return` dentro saldría de la FUNCIÓN, no del `if`.")

    section("Devolver directamente")

    show("clasifica(-5)", clasifica(-5))
    show("clasifica(0)", clasifica(0))
    show("clasifica(9)", clasifica(9))

    section("Encadenar como expresión")

    listOf(95, 72, 45).forEach { nota ->
        val calificacion = if (nota >= 90) "sobresaliente"
        else if (nota >= 70) "notable"
        else if (nota >= 50) "aprobado"
        else "suspenso"
        show("nota $nota", calificacion)
    }

    bullet("Funciona, pero a partir de tres ramas un `when` se lee mucho mejor (4.2).")
}

/**
 * Por qué Kotlin no tiene operador ternario.
 */
fun demoNoTernaryOperator() {
    section("La equivalencia")

    val usuario: String? = null

    bullet("Java:   String n = (u != null) ? u : \"invitado\";")
    bullet("Kotlin: val n = if (u != null) u else \"invitado\"")
    bullet("Kotlin: val n = u ?: \"invitado\"        ← lo idiomático")

    show("con if", if (usuario != null) usuario else "invitado")
    show("con Elvis", usuario ?: "invitado")

    section("La razón de fondo")

    bullet("El ternario existe en Java porque allí `if` NO devuelve valor.")
    bullet("Con `if` como expresión, el ternario sería un segundo modo de hacer lo mismo.")
    bullet("Kotlin prefiere una construcción que se lea, aunque sea un poco más larga.")

    section("Para nulos, casi siempre gana Elvis")

    val nombres = listOf<String?>("Ana", null, "Luis")
    val limpios = nombres.map { it ?: "(sin nombre)" }
    show("nombres.map { it ?: \"(sin nombre)\" }", limpios)
}

/**
 * `if` es una expresión, así que se puede usar donde se espera un valor.
 */
private fun clasifica(n: Int): String = if (n < 0) "negativo" else if (n == 0) "cero" else "positivo"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `val roto = if (a > b) a` y lee el error exacto.
//  2. Pon un `return` dentro de una rama del `if` con bloques y observa que sale de la
//     función entera, no sólo del if.
//  3. Reescribe la cadena de `if/else if` de las notas usando `when`. Compara.
