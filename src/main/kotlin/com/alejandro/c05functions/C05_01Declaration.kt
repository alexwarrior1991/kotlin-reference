package com.alejandro.c05functions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  5.1 · Declarar funciones
//
//  QUÉ ES
//    La anatomía de una función: parámetros, tipo de retorno, cuerpo de bloque y
//    cuerpo de expresión.
//
//  POR QUÉ IMPORTA
//    Las funciones de Kotlin viven donde quieras (fichero, clase, dentro de otra
//    función) y el cuerpo de expresión permite escribir en una línea lo que en Java
//    ocupa cinco. Saber cuándo escribir el tipo de retorno y cuándo omitirlo es parte
//    del estilo idiomático.
//
//  ERRORES COMUNES
//    · Omitir el tipo de retorno en una función pública con cuerpo de expresión.
//    · Escribir `: Unit` explícitamente (sobra siempre).
//    · Poner `return` dentro de un cuerpo de expresión (no se puede: ya es el valor).
// =====================================================================================

/**
 * Anatomía: parámetros y tipo de retorno.
 */
fun demoBasicDeclaration() {
    section("Cuerpo de bloque")

    //  fun  nombre (parámetro: Tipo, ...) : TipoDeRetorno  { cuerpo }
    show("sumar(3, 4)", sumar(3, 4))
    show("saludar(\"Ana\")", saludar("Ana"))

    section("Los parámetros son siempre `val`")

    // Dentro de la función no se pueden reasignar. Si necesitas cambiar el valor,
    // haz una copia local. Esto evita un montón de bugs sutiles.
    show("duplicarSinTocarElParametro(5)", duplicarSinTocarElParametro(5))
    bullet("`fun f(x: Int) { x = 1 }` no compila: los parámetros son inmutables.")

    section("El tipo de retorno es obligatorio... con cuerpo de bloque")

    bullet("fun sumar(a: Int, b: Int): Int { return a + b }   ← hay que escribir `: Int`")
    bullet("Con cuerpo de EXPRESIÓN se puede inferir: fun sumar(a: Int, b: Int) = a + b")
}

/**
 * Unit: el equivalente a `void`, pero que sí es un tipo.
 */
fun demoUnit() {
    section("Una función que no devuelve nada útil devuelve Unit")

    val resultado = registrar("evento")
    show("tipo devuelto por registrar()", resultado::class.simpleName)
    show("el valor de Unit es...", resultado)

    section("Las tres formas son equivalentes")

    bullet("fun registrar(m: String) { println(m) }             ← la idiomática")
    bullet("fun registrar(m: String): Unit { println(m) }       ← redundante")
    bullet("fun registrar(m: String): Unit { println(m); return Unit }  ← ridícula")

    section("Por qué Unit y no void")

    // `void` de Java no es un tipo: no puedes tener un `List<void>` ni una función
    // genérica que devuelva void. `Unit` sí es un tipo, con un único valor, así que
    // encaja en los genéricos sin casos especiales.
    bullet("Unit es un tipo real con un único valor: el objeto Unit.")
    bullet("Eso permite que `(Int) -> Unit` sea un tipo función como cualquier otro.")
    bullet("Sin Unit habría que duplicar toda la API genérica para el caso 'sin valor'.")
}

/**
 * Cuerpo de expresión: el estilo más usado en Kotlin.
 */
fun demoExpressionBody() {
    section("De bloque a expresión")

    bullet("fun doble(x: Int): Int { return x * 2 }")
    bullet("fun doble(x: Int) = x * 2                 ← lo mismo")

    show("doble(21)", doble(21))
    show("esPar(10)", esPar(10))
    show("clasificar(-3)", clasificar(-3))
    show("clasificar(0)", clasificar(0))

    section("Cualquier expresión vale, también `if` y `when`")

    // Como `if` y `when` devuelven valor, encajan perfectamente como cuerpo.
    show("describirNota(95)", describirNota(95))
    show("describirNota(60)", describirNota(60))
    show("describirNota(20)", describirNota(20))

    section("Cuándo escribir el tipo de retorno igualmente")

    // Con cuerpo de expresión el tipo se infiere, pero en una API pública conviene
    // escribirlo: es el contrato, y así un cambio en la implementación no cambia en
    // silencio el tipo que ven los demás.
    bullet("Función pública o de librería → escribe el tipo.")
    bullet("Función privada corta y obvia → puedes omitirlo.")
    bullet("Si al leer la firma no sabes qué devuelve, escríbelo.")

    section("Lo que NO se puede hacer")

    // Un cuerpo de expresión YA es el valor: poner `return` dentro es un error.
    bullet("fun doble(x: Int) = return x * 2      ← ERROR")
    bullet("Tampoco se puede usar un bloque `{ }` salvo que sea una lambda.")
}

/**
 * Nothing: el tipo de lo que nunca devuelve.
 */
fun demoNothing() {
    section("Funciones que no terminan normalmente")

    // `throw` es una expresión de tipo Nothing, y Nothing es subtipo de TODO.
    // Por eso puede aparecer donde se espera cualquier tipo.
    val resultado = try {
        fallarSiempre()
    } catch (e: IllegalStateException) {
        "lanzó IllegalStateException: ${e.message}"
    }
    show("fallarSiempre()", resultado)

    section("Para qué sirve en la práctica")

    // Gracias a Nothing, esto compila: el compilador sabe que si entra en el `else`
    // la función no continúa, así que `nombre` acaba siendo String (no String?).
    show("nombreObligatorio(\"Ana\")", nombreObligatorio("Ana"))
    val sinNombre = try {
        nombreObligatorio(null)
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException"
    }
    show("nombreObligatorio(null)", sinNombre)

    bullet("`?: throw ...` y `?: return` funcionan porque ambos son de tipo Nothing.")
    bullet("TODO() también devuelve Nothing: por eso compila en cualquier hueco (cap. 25).")

    section("Nothing? es otra cosa")

    // El único valor de Nothing? es null. Aparece al inferir el tipo de `null` solo.
    val soloPuedeSerNulo = null
    show("val x = null   → tipo", "Nothing?")
    show("su valor", soloPuedeSerNulo)
    bullet("Si ves un `Nothing?` inesperado, es que te falta declarar el tipo.")
}

// -- Las funciones que usan las demos -------------------------------------------------

/** Cuerpo de bloque: el tipo de retorno es obligatorio. */
private fun sumar(a: Int, b: Int): Int {
    return a + b
}

private fun saludar(nombre: String): String {
    return "Hola, $nombre"
}

/** Los parámetros son `val`: para modificarlos hay que copiarlos. */
private fun duplicarSinTocarElParametro(x: Int): Int {
    var copia = x     // `x = x * 2` sería un error de compilación
    copia *= 2
    return copia
}

/** Sin tipo de retorno declarado: devuelve Unit. */
private fun registrar(mensaje: String) {
    // Imprimimos con sangría para que se distinga de la salida de `show`.
    println("      [log] $mensaje")
}

// Cuerpos de expresión: el tipo se infiere del lado derecho.
private fun doble(x: Int) = x * 2
private fun esPar(x: Int) = x % 2 == 0
private fun clasificar(n: Int) = if (n < 0) "negativo" else if (n == 0) "cero" else "positivo"

/** Un `when` como cuerpo de expresión: muy habitual. */
private fun describirNota(nota: Int): String = when {
    nota >= 90 -> "sobresaliente"
    nota >= 70 -> "notable"
    nota >= 50 -> "aprobado"
    else -> "suspenso"
}

/** Devuelve Nothing: nunca termina de forma normal. */
private fun fallarSiempre(): Nothing = throw IllegalStateException("esto siempre falla")

/** El `throw` de tipo Nothing permite que `nombre` sea String y no String?. */
private fun nombreObligatorio(nombre: String?): String {
    val seguro = nombre ?: throw IllegalArgumentException("el nombre es obligatorio")
    return seguro.uppercase()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `: Int` de `sumar` dejando el cuerpo de bloque y lee el error.
//  2. Convierte `describirNota` a cuerpo de bloque con `return when { ... }`.
//  3. Escribe `fun f(x: Int) { x = 1 }` y comprueba que los parámetros son inmutables.
//  4. Cambia el tipo de retorno de `fallarSiempre` a `Unit` y mira qué deja de compilar
//     en `nombreObligatorio`.
