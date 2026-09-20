package com.alejandro.c05functions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  5.5 · inline, noinline, crossinline y lambdas con receptor
//
//  QUÉ ES
//    `inline` le dice al compilador que copie el cuerpo de la función (y el de sus
//    lambdas) en el lugar de la llamada. `noinline` y `crossinline` son dos ajustes
//    sobre esa copia. Y las lambdas con receptor son las que hacen posibles `apply`,
//    `buildString` y los DSL.
//
//  POR QUÉ IMPORTA
//    `inline` no es sólo una optimización: es lo que HABILITA el `return` no local y
//    los parámetros `reified` (capítulo 12). Sin entenderlo, media biblioteca estándar
//    parece magia.
//
//  ERRORES COMUNES
//    · Marcar `inline` funciones grandes sin lambdas: sólo infla el bytecode.
//    · Pelearse con "Can't inline 'bloque' here" sin saber que la respuesta es
//      `crossinline`.
//    · Confundir `T.() -> R` (con receptor) con `(T) -> R` (parámetro normal).
// =====================================================================================

/**
 * Qué hace realmente `inline`.
 */
fun demoInline() {
    section("El problema que resuelve")

    // Cada lambda que pasas a una función normal se compila como un OBJETO. En un
    // bucle apretado, eso son millones de objetos creados y recogidos por el GC.
    bullet("Función normal + lambda  → se crea un objeto por cada lambda.")
    bullet("Función inline + lambda  → el cuerpo se copia; no se crea ningún objeto.")

    section("En funcionamiento")

    show("medir { ... } (inline)", medir { (1..1_000).sum() })
    show("conRepeticion(3) { it * it }", conRepeticion(3) { it * it })

    section("Lo que de verdad habilita `inline`")

    // 1) El `return` no local: como el cuerpo se copia dentro de quien llama, un
    //    `return` dentro de la lambda puede salir de la función externa.
    show("buscarPrimerNegativo(listOf(1, -5, 3))", buscarPrimerNegativo(listOf(1, -5, 3)))
    show("buscarPrimerNegativo(listOf(1, 2))", buscarPrimerNegativo(listOf(1, 2)))
    bullet("Sin `inline`, un `return` dentro de la lambda no compilaría.")

    // 2) Los parámetros `reified`: poder preguntar por el tipo genérico en ejecución.
    //    Se ve a fondo en el capítulo 12.
    bullet("`inline fun <reified T>` permite escribir `T::class`, imposible si no.")

    section("Cuándo NO marcar inline")

    bullet("Si la función no recibe ninguna lambda: no hay nada que ganar.")
    bullet("Si es grande: copiarla en 50 sitios multiplica el tamaño del bytecode.")
    bullet("El compilador ya avisa: 'Expected performance impact is insignificant'.")
    bullet("Regla: funciones PEQUEÑAS que reciben lambdas. Como las de la stdlib.")
}

/**
 * `noinline`: exceptuar un parámetro de la copia.
 */
fun demoNoinline() {
    section("El problema")

    // Una lambda "inlineada" no existe como objeto, así que no se puede guardar en
    // una variable, meter en una lista ni devolver. Si necesitas hacer eso con uno
    // de los parámetros, hay que marcarlo `noinline`.
    val resultado = conRegistroDiferido(
        accion = { "trabajo hecho" },
        alTerminar = { mensaje -> "callback: $mensaje" },
    )
    show("conRegistroDiferido(...)", resultado)

    section("La regla")

    bullet("`inline` copia TODAS las lambdas del parámetro por defecto.")
    bullet("`noinline` marca la que sí debe seguir siendo un objeto.")
    bullet("Hace falta cuando la guardas, la devuelves o la pasas a otra función no inline.")
}

/**
 * `crossinline`: prohibir el `return` no local.
 */
fun demoCrossinline() {
    section("El problema")

    // Si la lambda se va a ejecutar desde OTRO contexto (dentro de un objeto anónimo,
    // en otro hilo, más tarde), un `return` no local no tendría sentido: la función
    // externa quizá ya haya terminado. `crossinline` lo prohíbe en tiempo de
    // compilación, manteniendo el resto de ventajas de `inline`.
    val salida = ejecutarEnvuelto { "tarea ejecutada" }
    show("ejecutarEnvuelto { ... }", salida)

    section("Las tres variantes, en una tabla")

    bullet("(normal)     se copia · permite `return` no local")
    bullet("noinline     NO se copia · es un objeto · se puede guardar")
    bullet("crossinline  se copia · PROHÍBE `return` no local")

    section("Cómo saber cuál necesitas")

    bullet("Si el compilador dice \"Can't inline ... here\" → prueba `crossinline`.")
    bullet("Si dice \"Illegal usage of inline-parameter\" → necesitas `noinline`.")
    bullet("Si no dice nada, no necesitas ninguna de las dos.")
}

/**
 * Lambdas con receptor: `T.() -> R`.
 */
fun demoLambdasWithReceiver() {
    section("La diferencia con una lambda normal")

    // (String) -> String : el String llega como PARÁMETRO, se usa como `it`.
    val comoParametro: (String) -> String = { it.uppercase() }

    // String.() -> String : el String es el RECEPTOR, se usa como `this` (o implícito).
    val comoReceptor: String.() -> String = { uppercase() }

    show("comoParametro(\"kotlin\")", comoParametro("kotlin"))
    show("\"kotlin\".comoReceptor()", "kotlin".comoReceptor())

    bullet("Con receptor, dentro de la lambda estás 'dentro' del objeto.")
    bullet("Eso permite llamar a sus miembros sin escribir el nombre una y otra vez.")

    section("Por qué importa: sin receptor vs con receptor")

    show("construir sin receptor", informeSinReceptor())
    show("construir con receptor", informeConReceptor())

    section("La stdlib está llena de ellas")

    // `buildString` recibe una `StringBuilder.() -> Unit`: por eso dentro puedes
    // llamar a `append` a secas.
    val texto = buildString {
        append("Kotlin")
        append(" es ")
        append("expresivo")
    }
    show("buildString { append(...) }", texto)

    // `apply` recibe una `T.() -> Unit` y devuelve el propio objeto. Capítulo 16.
    val lista = mutableListOf<Int>().apply {
        add(1)
        add(2)
        add(3)
    }
    show("mutableListOf<Int>().apply { add(...) }", lista)

    section("Una función propia con receptor")

    val configuracion = configurar {
        host = "localhost"
        puerto = 8080
        depuracion = true
    }
    show("configurar { host = ...; puerto = ... }", configuracion)

    bullet("Esto es el germen de un DSL. El capítulo 29 lo desarrolla entero.")
    bullet("La infraestructura de este repo (`chapter { demo(...) }`) funciona así.")
}

// -- Las funciones que usan las demos -------------------------------------------------

/** Función inline pequeña que recibe una lambda: el caso de libro. */
private inline fun <T> medir(bloque: () -> T): String {
    val inicio = System.nanoTime()
    val resultado = bloque()
    val transcurrido = System.nanoTime() - inicio
    // No imprimimos el tiempo exacto: variaría en cada ejecución.
    return "resultado=$resultado (medido en ${if (transcurrido > 0) "un instante" else "0 ns"})"
}

private inline fun conRepeticion(veces: Int, transformar: (Int) -> Int): List<Int> =
    (1..veces).map(transformar)

/**
 * El `return` de dentro de la lambda sale de ESTA función. Sólo es posible porque
 * `forEach` es inline.
 */
private fun buscarPrimerNegativo(numeros: List<Int>): String {
    numeros.forEach { n ->
        if (n < 0) return "el primero es $n"
    }
    return "no hay negativos"
}

/**
 * `alTerminar` se guarda en una variable, así que no puede copiarse: `noinline`.
 */
private inline fun conRegistroDiferido(
    accion: () -> String,
    noinline alTerminar: (String) -> String,
): String {
    val mensaje = accion()
    // Guardar la lambda en una variable es justo lo que exige `noinline`.
    val guardada: (String) -> String = alTerminar
    return guardada(mensaje)
}

/**
 * `bloque` se ejecuta desde dentro de un objeto anónimo (un Runnable), así que un
 * `return` no local sería imposible: `crossinline`.
 */
private inline fun ejecutarEnvuelto(crossinline bloque: () -> String): String {
    var resultado = ""
    val tarea = Runnable { resultado = bloque() }
    tarea.run()
    return resultado
}

/** Construir un texto SIN receptor: hay que nombrar el objeto en cada línea. */
private fun informeSinReceptor(): String {
    val sb = StringBuilder()
    sb.append("Informe")
    sb.append(" · 3 líneas")
    sb.append(" · fin")
    return sb.toString()
}

/** Lo mismo CON receptor: el objeto es implícito. */
private fun informeConReceptor(): String = StringBuilder().apply {
    append("Informe")
    append(" · 3 líneas")
    append(" · fin")
}.toString()

/** Un mini-builder con receptor: el patrón de todos los DSL de Kotlin. */
private class Configuracion {
    var host: String = "0.0.0.0"
    var puerto: Int = 80
    var depuracion: Boolean = false
    override fun toString(): String = "$host:$puerto (depuración=$depuracion)"
}

/**
 * `bloque: Configuracion.() -> Unit` es lo que permite escribir `host = "..."`
 * directamente dentro de las llaves, sin repetir el nombre del objeto.
 */
private fun configurar(bloque: Configuracion.() -> Unit): Configuracion =
    Configuracion().apply(bloque)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `inline` de `medir` y comprueba que sigue funcionando (sólo cambia el coste).
//  2. Quita `noinline` de `alTerminar` y lee el error "Illegal usage of inline-parameter".
//  3. Quita `crossinline` de `ejecutarEnvuelto` y lee el error "Can't inline ... here".
//  4. Cambia `Configuracion.() -> Unit` por `(Configuracion) -> Unit` y observa cómo
//     tendrías que escribir `it.host = ...` en cada línea.
