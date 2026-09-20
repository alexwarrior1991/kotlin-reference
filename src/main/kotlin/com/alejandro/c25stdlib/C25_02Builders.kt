package com.alejandro.c25stdlib

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  25.2 · Builders, repeat, lazy y utilidades de construcción
//
//  QUÉ ES
//    `buildList`, `buildSet`, `buildMap`, `buildString`, `repeat` y `lazy`: las
//    funciones que se usan para CONSTRUIR cosas.
//
//  POR QUÉ IMPORTA
//    Un builder deja claro en el tipo que la colección se construye aquí y sale de
//    sólo lectura. Y `buildString` evita el clásico bucle con `+=` que crea una
//    cadena nueva en cada vuelta.
//
//  ERRORES COMUNES
//    · Crear un `mutableListOf`, llenarlo y devolverlo: el tipo miente sobre la
//      intención.
//    · Concatenar en bucle en lugar de `buildString`.
//    · Guardar el builder fuera del bloque (no está permitido, pero se intenta).
// =====================================================================================

/**
 * `buildList`, `buildSet` y `buildMap`.
 */
fun demoCollectionBuilders() {
    section("El patrón antiguo")

    // Funciona, pero el tipo de `resultado` es MutableList hasta el final, y si
    // se te escapa hacia fuera, cualquiera puede modificarlo.
    val antiguo = mutableListOf<String>()
    antiguo.add("cabecera")
    for (i in 1..3) antiguo.add("línea $i")
    show("con mutableListOf", antiguo)

    section("El patrón con builder")

    val moderno = buildList {
        add("cabecera")
        for (i in 1..3) add("línea $i")
    }
    show("con buildList", moderno)
    show("su tipo", "List<String> (de sólo lectura)")

    bullet("Dentro del bloque tienes un MutableList; fuera recibes una List.")
    bullet("Es imposible que la referencia mutable se escape.")

    section("buildSet y buildMap")

    val conjunto = buildSet {
        addAll(listOf(3, 1, 2))
        add(1)          // duplicado, se ignora
    }
    show("buildSet { }", conjunto)

    val mapa = buildMap {
        put("host", "localhost")
        putAll(mapOf("puerto" to "8080"))
        if (size < 5) put("modo", "desarrollo")
    }
    show("buildMap { }", mapa)

    section("Con capacidad inicial")

    // Si sabes cuántos elementos va a haber, se puede reservar espacio.
    val conCapacidad = buildList(capacity = 100) {
        repeat(5) { add(it * it) }
    }
    show("buildList(capacity = 100)", conCapacidad)
    bullet("Evita que el array interno se redimensione varias veces. Sólo importa")
    bullet("con muchos elementos, pero es gratis ponerlo si ya sabes el tamaño.")

    section("Condicionales dentro del builder")

    // Ésta es la gran ventaja frente a `listOf(...)`: se puede incluir lógica.
    val incluirOpcionales = true
    val configuracion = buildList {
        add("--verbose")
        if (incluirOpcionales) {
            add("--color")
            add("--timeout=30")
        }
        addAll(listOf("entrada.txt", "salida.txt"))
    }
    show("con lógica dentro", configuracion)

    bullet("Con `listOf` tendrías que montar un `+ if (x) listOf(...) else emptyList()`.")
}

/**
 * `buildString`.
 */
fun demoBuildString() {
    section("El problema")

    // Cada `+=` crea una cadena NUEVA. Con 5 vueltas da igual; con 10.000 no.
    var concatenado = ""
    for (i in 1..5) {
        concatenado += "línea $i\n"
    }
    show("con += en bucle", concatenado.replace("\n", " ⏎ ").trim())

    bullet("5 vueltas → 5 objetos String creados y tirados.")
    bullet("10.000 vueltas → 10.000 objetos, y cada uno copia el anterior entero.")

    section("La solución")

    val construido = buildString {
        for (i in 1..5) {
            append("línea ")
            append(i)
            appendLine()
        }
    }
    show("con buildString", construido.replace("\n", " ⏎ ").trim())

    bullet("Por dentro es un StringBuilder: una sola cadena al final.")

    section("Los métodos disponibles dentro")

    val informe = buildString {
        appendLine("INFORME")
        append("Fecha: ").appendLine("2026-09-19")
        append("Total: ").append(42).appendLine(" €")
        repeat(20) { append('-') }
    }
    println()
    println(informe.prependIndent("      "))

    bullet("append, appendLine, insert, setLength... todo lo de StringBuilder.")
    bullet("Y `append` devuelve el propio builder, así que se puede encadenar.")

    section("Cuándo NO hace falta")

    // Para una cadena de pocas partes conocidas, la plantilla se lee mejor.
    val nombre = "Ana"
    val edad = 34
    show("plantilla (mejor para esto)", "Hola $nombre, tienes $edad años")
    bullet("`buildString` es para BUCLES y lógica condicional, no para tres partes.")
}

/**
 * `repeat` y `lazy`.
 */
fun demoRepeatAndLazy() {
    section("repeat: una función, no una palabra clave")

    val salida = buildList {
        repeat(4) { indice -> add("vuelta $indice") }
    }
    show("repeat(4) { indice -> }", salida)

    // El parámetro se puede ignorar.
    show("repeat sin usar el índice", buildString { repeat(10) { append('*') } })

    bullet("`repeat` es `inline`, así que no crea ningún objeto lambda.")
    bullet("El índice empieza en 0, como siempre en Kotlin.")

    section("lazy fuera de una clase")

    // Ya se vio como delegado de propiedad (capítulo 18.5). Aquí, como objeto.
    var vecesCalculado = 0
    val perezoso = lazy {
        vecesCalculado++
        "valor caro"
    }

    show("¿inicializado antes de leer?", perezoso.isInitialized())
    show("primera lectura", perezoso.value)
    show("¿inicializado ahora?", perezoso.isInitialized())
    show("segunda lectura", perezoso.value)
    show("veces calculado", vecesCalculado)

    bullet("`lazy { }` devuelve un objeto `Lazy<T>` con `.value` y `.isInitialized()`.")
    bullet("El `by lazy` de las propiedades usa exactamente este objeto.")

    section("lazyOf: cuando el valor ya lo tienes")

    val yaCalculado = lazyOf("valor ya conocido")
    show("lazyOf(x).value", yaCalculado.value)
    bullet("Útil en tests o cuando una API pide un Lazy y tú ya tienes el valor.")
}

/**
 * Otras utilidades de construcción y conversión.
 */
fun demoOtherUtilities() {
    section("Crear colecciones con una función generadora")

    show("List(5) { it * it }", List(5) { it * it })
    show("MutableList(3) { \"x\" }", MutableList(3) { "x$it" })
    show("Array(4) { it + 1 }", Array(4) { it + 1 })
    show("IntArray(4) { it * 10 }", IntArray(4) { it * 10 })

    section("generateSequence: infinito pero perezoso")

    show("primeros 5 pares", generateSequence(0) { it + 2 }.take(5).toList())
    bullet("Capítulo 14 para el detalle.")

    section("Conversión entre tipos, en una línea")

    val texto = "3,1,4,1,5,9,2,6"
    show("texto a lista de Int", texto.split(",").map { it.toInt() })
    show("y de vuelta a texto", listOf(3, 1, 4).joinToString(","))
    show("a Set ordenado", texto.split(",").map { it.toInt() }.toSortedSet())

    section("Utilidades de rango y ajuste")

    show("coerceIn(0..10) con 50", 50.coerceIn(0..10))
    show("coerceIn(0..10) con -5", (-5).coerceIn(0..10))
    show("coerceAtLeast(0) con -3", (-3).coerceAtLeast(0))
    show("coerceAtMost(100) con 250", 250.coerceAtMost(100))

    bullet("`coerceIn` es el «clamp» de toda la vida, y se lee mucho mejor que")
    bullet("`maxOf(minimo, minOf(maximo, valor))`.")

    section("Utilidades numéricas")

    show("maxOf(3, 9)", maxOf(3, 9))
    show("minOf(3, 9, 1)", minOf(3, 9, 1))
    show("5.coerceIn(1, 3)", 5.coerceIn(1, 3))
    show("Math.abs con kotlin.math", kotlin.math.abs(-42))
    show("kotlin.math.round(3.6)", kotlin.math.round(3.6))
    show("kotlin.math.floor(3.6)", kotlin.math.floor(3.6))
    show("kotlin.math.ceil(3.2)", kotlin.math.ceil(3.2))
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia un `mutableListOf` + bucle de tu código por un `buildList`.
//  2. Sube el bucle de `concatenado +=` a 100.000 vueltas y cronométralo contra
//     buildString (pista: `measureTime`, demo 25.13).
//  3. Intenta guardar el builder de `buildList` en una variable de fuera: verás que
//     el compilador no te deja sacarlo del bloque.
//  4. Escribe `buildMap` con una condición que decida qué claves incluir.
