package com.alejandro.c15lambdas

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  15.1 · Sintaxis de las lambdas
//
//  QUÉ ES
//    Todas las formas de escribir una lambda, y las abreviaturas que Kotlin permite.
//    El capítulo 5 las presentó como parte de las funciones; aquí se miran de cerca.
//
//  POR QUÉ IMPORTA
//    `lista.map { it * 2 }` parece sintaxis del lenguaje, pero son tres abreviaturas
//    encadenadas sobre una llamada normal. Saber cuáles son permite leer código ajeno
//    y saber cuándo conviene volver a la forma larga.
//
//  ERRORES COMUNES
//    · Usar `it` en lambdas anidadas hasta no saber a cuál se refiere.
//    · Escribir `{ it -> it * 2 }`, que es redundante.
//    · Olvidar que el valor de una lambda es su ÚLTIMA expresión, no un `return`.
// =====================================================================================

/**
 * De la forma larga a la corta, paso a paso.
 */
fun demoSyntaxSteps() {
    val numeros = listOf(1, 2, 3)

    section("Paso 0: una función normal")

    show("numeros.map(::doble)", numeros.map(::doble))

    section("Paso 1: la lambda completa, dentro de los paréntesis")

    show("map({ n: Int -> n * 2 })", numeros.map({ n: Int -> n * 2 }))

    section("Paso 2: el tipo del parámetro se infiere")

    show("map({ n -> n * 2 })", numeros.map({ n -> n * 2 }))

    section("Paso 3: si es el último parámetro, la lambda sale fuera")

    show("map() { n -> n * 2 }", numeros.map() { n -> n * 2 })

    section("Paso 4: si es el único parámetro, sobran los paréntesis")

    show("map { n -> n * 2 }", numeros.map { n -> n * 2 })

    section("Paso 5: con un solo parámetro, se llama `it`")

    show("map { it * 2 }", numeros.map { it * 2 })

    bullet("Los cinco pasos dan exactamente el mismo bytecode.")
    bullet("La forma corta es la idiomática, pero conviene saber de dónde sale.")
}

/**
 * El valor de una lambda.
 */
fun demoLambdaValue() {
    section("La última expresión es el valor")

    val numeros = listOf(1, 2, 3)

    val conVariasLineas = numeros.map {
        val doblado = it * 2
        val masUno = doblado + 1
        "resultado: $masUno"        // ← ésta es la que vale
    }
    show("lambda de varias líneas", conVariasLineas)

    bullet("No se escribe `return`: la última expresión ES el valor.")
    bullet("Un `return` a secas saldría de la función que rodea (capítulo 4.16).")

    section("Si hace falta salir antes, return con etiqueta")

    val conSalidaTemprana = numeros.map {
        if (it == 2) return@map "el dos es especial"
        "número $it"
    }
    show("con return@map", conSalidaTemprana)

    section("Una lambda que no devuelve nada útil")

    // Si el tipo esperado es `(T) -> Unit`, el valor de la última expresión se ignora.
    val efectos = mutableListOf<String>()
    numeros.forEach {
        efectos.add("visto $it")
        "este valor se ignora"
    }
    show("forEach ignora el valor", efectos)
}

/**
 * Parámetros: uno, varios, ignorados y desestructurados.
 */
fun demoParameters() {
    section("Un parámetro: `it`")

    show("map { it.length }", listOf("ab", "cde").map { it.length })

    bullet("`it` es cómodo en una línea. Si la lambda crece, ponle nombre.")
    bullet("Escribir `{ it -> ... }` es redundante: o usas `it`, o le das nombre.")

    section("Varios parámetros: hay que nombrarlos")

    val precios = mapOf("pan" to 1.2, "leche" to 0.9)
    show("forEach { clave, valor -> }", precios.map { (clave, valor) -> "$clave=$valor" })
    show("fold { acc, n -> }", listOf(1, 2, 3).fold(0) { acc, n -> acc + n })
    show("mapIndexed { i, v -> }", listOf("a", "b").mapIndexed { i, v -> "$i:$v" })

    bullet("Con dos o más parámetros no existe `it`: hay que nombrarlos todos.")

    section("Ignorar un parámetro con _")

    show("sólo el índice", listOf("a", "b", "c").mapIndexed { i, _ -> i })
    show("sólo el valor del mapa", precios.map { (_, valor) -> valor })

    bullet("`_` documenta que ese parámetro no se usa, y evita el aviso del IDE.")

    section("Desestructurar en la lambda")

    // Si el parámetro es un Pair, un Map.Entry o una data class, se puede abrir
    // directamente entre paréntesis.
    val personas = listOf(Persona("Ana", 34), Persona("Luis", 28))
    show("sin desestructurar", personas.map { "${it.nombre} (${it.edad})" })
    show("desestructurando", personas.map { (nombre, edad) -> "$nombre ($edad)" })

    bullet("Funciona con cualquier tipo que tenga component1(), component2()... (cap. 22)")
    bullet("Ojo: el orden es POSICIONAL, no por nombre. Si cambias el orden de las")
    bullet("propiedades de la data class, esta lambda seguirá compilando y mentirá.")

    section("Lambdas sin parámetros")

    val saludo: () -> String = { "hola" }
    show("val saludo: () -> String = { \"hola\" }", saludo())
    show("repeat(3) { }", buildList { repeat(3) { add("·") } })
}

/**
 * Dónde puede ir la lambda en la llamada.
 */
fun demoTrailingLambda() {
    section("La lambda final fuera de los paréntesis")

    val numeros = listOf(3, 1, 2)

    // Con varios parámetros, sólo el ÚLTIMO puede salir fuera.
    show("fold(0) { acc, n -> acc + n }", numeros.fold(0) { acc, n -> acc + n })
    show("joinToString(\"-\") { }", numeros.joinToString("-") { "n$it" })

    section("Con argumentos nombrados")

    show(
        "joinToString con nombres",
        numeros.joinToString(separator = " | ", prefix = "<", postfix = ">") { "n$it" },
    )

    section("Dos lambdas: sólo una puede salir")

    // Si una función recibe dos lambdas, la segunda va fuera y la primera dentro.
    show("conDosLambdas", conDosLambdas({ "primera" }) { "segunda" })
    show("o las dos dentro", conDosLambdas({ "primera" }, { "segunda" }))

    bullet("Si tu función recibe dos lambdas, plantéate si no sobra alguna.")
    bullet("Alternativa habitual: recibir un objeto con dos métodos, o una sealed.")

    section("Por qué esto importa")

    bullet("Es la convención que hace que `repeat(3) { }`, `measureTime { }` y")
    bullet("`buildList { }` se lean como si fueran palabras clave del lenguaje.")
    bullet("Por eso, al diseñar una función de orden superior, pon la lambda AL FINAL.")
}

private data class Persona(val nombre: String, val edad: Int)

private fun doble(n: Int): Int = n * 2

private fun conDosLambdas(primera: () -> String, segunda: () -> String): String =
    "${primera()} y ${segunda()}"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `numeros.map { it -> it * 2 }` y mira el aviso del IDE.
//  2. Anida dos lambdas usando `it` en las dos y comprueba lo difícil que es leerlo.
//  3. Invierte las propiedades de Persona y observa que la desestructuración miente.
//  4. Escribe una función que reciba dos lambdas y prueba las dos formas de llamarla.
