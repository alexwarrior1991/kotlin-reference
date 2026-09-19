package com.alejandro.c05functions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  5.4 · Funciones de orden superior, tipos función y referencias
//
//  QUÉ ES
//    Una función de orden superior es la que recibe o devuelve otra función. Para eso
//    hacen falta los TIPOS FUNCIÓN (`(Int) -> String`), las lambdas y las referencias
//    a funciones (`::nombre`).
//
//  POR QUÉ IMPORTA
//    Es el mecanismo sobre el que está construida media biblioteca estándar: `map`,
//    `filter`, `sortedBy`, `let`, `use`, `runCatching`... Entenderlo aquí hace que el
//    capítulo 13 (colecciones) y el 16 (scope functions) sean casi obvios.
//
//  ERRORES COMUNES
//    · Escribir `lista.map { it -> it * 2 }` cuando `{ it * 2 }` ya vale.
//    · Anidar lambdas hasta que `it` significa tres cosas distintas.
//    · No saber que `::metodo` existe y envolver todo en lambdas innecesarias.
// =====================================================================================

/**
 * Tipos función: una función también tiene tipo.
 */
fun demoFunctionTypes() {
    section("La sintaxis")

    bullet("()        -> Unit      sin parámetros, sin resultado útil")
    bullet("(Int)     -> String    un Int, devuelve String")
    bullet("(Int,Int) -> Int       dos Int, devuelve Int")
    bullet("String.(Int) -> String con receptor String (ver 5.5)")
    bullet("((Int) -> String)?     tipo función NULABLE (ojo a los paréntesis)")

    section("Guardar una función en una variable")

    val duplicar: (Int) -> Int = { n -> n * 2 }
    val sumar: (Int, Int) -> Int = { a, b -> a + b }
    val saludar: () -> String = { "hola" }

    show("duplicar(21)", duplicar(21))
    show("sumar(3, 4)", sumar(3, 4))
    show("saludar()", saludar())

    // También se puede invocar con .invoke(), que es lo que hace el compilador.
    show("duplicar.invoke(21)", duplicar.invoke(21))

    section("Tipos función nulables")

    val quizaNula: ((Int) -> Int)? = null
    show("quizaNula?.invoke(5)", quizaNula?.invoke(5))
    show("quizaNula?.invoke(5) ?: -1", quizaNula?.invoke(5) ?: -1)
    bullet("Sin los paréntesis, `(Int) -> Int?` sería una función que devuelve Int?.")

    section("typealias para nombres largos")

    // Un tipo función repetido tres veces pide un alias: se lee mejor y documenta.
    val validador: Validador = { texto -> if (texto.isBlank()) "vacío" else null }
    show("validador(\"\")", validador(""))
    show("validador(\"ok\")", validador("ok"))
}

/**
 * Funciones que RECIBEN funciones.
 */
fun demoFunctionsAsParameters() {
    section("Un ejemplo hecho a mano")

    val numeros = listOf(1, 2, 3, 4, 5, 6)

    show("filtrar(numeros) { it % 2 == 0 }", filtrar(numeros) { it % 2 == 0 })
    show("filtrar(numeros) { it > 4 }", filtrar(numeros) { it > 4 })

    section("Lambda al final: fuera de los paréntesis")

    // Si el ÚLTIMO parámetro es una función, la lambda se puede sacar fuera.
    // Y si es el único parámetro, los paréntesis desaparecen del todo.
    show("aplicarDosVeces(5) { it + 1 }", aplicarDosVeces(5) { it + 1 })
    show("aplicarDosVeces(5, { it + 1 })  (misma llamada)", aplicarDosVeces(5, { it + 1 }))

    bullet("Por eso `lista.map { }` y `repeat(3) { }` se leen como si fueran sintaxis.")
    bullet("Es sólo una convención de llamada, no una construcción del lenguaje.")

    section("`it`: el parámetro implícito")

    // Cuando la lambda tiene UN solo parámetro, se puede omitir y llamarlo `it`.
    show("con nombre explícito", numeros.map { n -> n * n })
    show("con `it`", numeros.map { it * it })

    bullet("`it` es cómodo en lambdas de una línea.")
    bullet("Si la lambda es larga o hay anidamiento, pon un nombre de verdad.")

    section("Varios parámetros, y el guion bajo")

    val pares = listOf("a" to 1, "b" to 2)
    show("con los dos", pares.map { (letra, numero) -> "$letra$numero" })
    show("ignorando uno con _", pares.map { (_, numero) -> numero * 10 })
}

/**
 * Funciones que DEVUELVEN funciones.
 */
fun demoFunctionsAsReturnValues() {
    section("Una fábrica de funciones")

    val porDos = multiplicadorPor(2)
    val porDiez = multiplicadorPor(10)

    show("multiplicadorPor(2)(21)", porDos(21))
    show("multiplicadorPor(10)(7)", porDiez(7))

    // Se puede llamar del tirón, aunque se lea peor.
    show("multiplicadorPor(3)(5)", multiplicadorPor(3)(5))

    section("Un caso real: elegir el comparador")

    val usuarios = listOf(
        Usuario("Ana", 34),
        Usuario("Luis", 28),
        Usuario("Marta", 41),
    )

    show("ordenados por edad", usuarios.sortedWith(comparadorPor("edad")))
    show("ordenados por nombre", usuarios.sortedWith(comparadorPor("nombre")))

    section("Composición")

    // Combinar dos funciones en una tercera.
    val incrementar: (Int) -> Int = { it + 1 }
    val duplicar: (Int) -> Int = { it * 2 }

    val incrementarYDuplicar = incrementar then duplicar
    val duplicarEIncrementar = duplicar then incrementar

    show("(x+1)*2 con x=5", incrementarYDuplicar(5))
    show("(x*2)+1 con x=5", duplicarEIncrementar(5))
    bullet("El orden importa: componer no es conmutativo.")
}

/**
 * Referencias a funciones: `::`.
 */
fun demoFunctionReferences() {
    section("A una función de nivel superior")

    val numeros = listOf(1, 2, 3)

    // `::esImpar` es más corto y más claro que `{ esImpar(it) }`.
    show("numeros.filter(::esImpar)", numeros.filter(::esImpar))
    show("numeros.filter { esImpar(it) }  (equivalente)", numeros.filter { esImpar(it) })

    // Y se puede guardar en una variable, como cualquier función.
    val comprobador: (Int) -> Boolean = ::esImpar
    show("val comprobador: (Int) -> Boolean = ::esImpar", comprobador(7))

    section("A un miembro de una clase")

    val palabras = listOf("kotlin", "es", "expresivo")

    // `String::length` es una función que recibe un String y devuelve su longitud.
    show("palabras.map(String::length)", palabras.map(String::length))
    show("palabras.map(String::uppercase)", palabras.map(String::uppercase))
    show("palabras.sortedBy(String::length)", palabras.sortedBy(String::length))

    section("Referencia ligada (bound): a un objeto concreto")

    val prefijo = "kot"
    // Aquí el receptor ya está fijado: es `prefijo`.
    val empiezaPorPrefijo: (String) -> Boolean = prefijo::startsWith
    show("prefijo::startsWith(\"kotlin\")", empiezaPorPrefijo("kotlin"))

    val ana = Usuario("Ana", 34)
    // Los paréntesis alrededor de la referencia son necesarios: sin ellos, el `.`
    // se aplicaría a `presentacion` y no a la referencia completa.
    val presentacionDeAna: () -> String = ana::presentacion
    show("ana::presentacion", presentacionDeAna())

    section("A un constructor")

    val nombres = listOf("Ana", "Luis")
    // `::Usuario` construye un Usuario. Aquí usamos la sobrecarga de un argumento.
    val creados = nombres.map(::Usuario)
    show("nombres.map(::Usuario)", creados)

    section("A una propiedad")

    val usuarios = listOf(Usuario("Ana", 34), Usuario("Luis", 28))
    show("usuarios.map(Usuario::nombre)", usuarios.map(Usuario::nombre))
    show("usuarios.sumOf(Usuario::edad)", usuarios.sumOf(Usuario::edad))
    show("usuarios.maxByOrNull(Usuario::edad)", usuarios.maxByOrNull(Usuario::edad))

    section("Cuándo usar referencia y cuándo lambda")

    bullet("Referencia si sólo llamas a UNA función con el argumento tal cual.")
    bullet("Lambda si hay que transformar antes, combinar, o pasar argumentos extra.")
    bullet("`map(::procesar)` sí; `map { procesar(it, modo = 2) }` necesita lambda.")
}

/**
 * Lambda frente a función anónima.
 */
fun demoAnonymousFunctions() {
    section("Las dos formas de escribir una función sin nombre")

    val numeros = listOf(1, -2, 3, -4)

    // Lambda: tipo de retorno inferido, `return` no permitido (sin etiqueta).
    val conLambda = numeros.filter { it > 0 }

    // Función anónima: se puede declarar el tipo de retorno y usar `return`.
    val conAnonima = numeros.filter(fun(n: Int): Boolean {
        if (n == 0) return false
        return n > 0
    })

    show("con lambda", conLambda)
    show("con función anónima", conAnonima)

    section("La diferencia que importa: qué hace `return`")

    bullet("En una lambda, `return` sale de la función que la ENVUELVE (ver 4.16).")
    bullet("En una función anónima, `return` sale de la propia función anónima.")
    bullet("Por eso la anónima es útil cuando hay varias salidas dentro del cuerpo.")

    section("En la práctica")

    bullet("El 99% del código usa lambdas: son más cortas.")
    bullet("La función anónima aparece cuando necesitas varios `return` o declarar el tipo.")
}

// -- Tipos y funciones auxiliares -----------------------------------------------------

/** Un alias para un tipo función que se repite. Mejora la legibilidad de las firmas. */
private typealias Validador = (String) -> String?

private class Usuario(val nombre: String, val edad: Int = 0) {
    fun presentacion(): String = "$nombre ($edad)"
    override fun toString(): String = presentacion()
}

/** Función de orden superior escrita a mano: así es `filter` por dentro. */
private fun filtrar(numeros: List<Int>, criterio: (Int) -> Boolean): List<Int> {
    val resultado = mutableListOf<Int>()
    for (n in numeros) {
        if (criterio(n)) resultado.add(n)
    }
    return resultado
}

/** El parámetro función va el ÚLTIMO para poder usar la lambda fuera de los paréntesis. */
private fun aplicarDosVeces(valor: Int, operacion: (Int) -> Int): Int = operacion(operacion(valor))

/** Devuelve una función: cada llamada crea un multiplicador distinto. */
private fun multiplicadorPor(factor: Int): (Int) -> Int = { n -> n * factor }

/** Elegir la estrategia de ordenación en tiempo de ejecución. */
private fun comparadorPor(campo: String): Comparator<Usuario> = when (campo) {
    "edad" -> compareBy { it.edad }
    else -> compareBy { it.nombre }
}

/** Composición de funciones, escrita como infija para que se lea de izquierda a derecha. */
private infix fun <A, B, C> ((A) -> B).then(siguiente: (B) -> C): (A) -> C =
    { entrada -> siguiente(this(entrada)) }

private fun esImpar(n: Int): Boolean = n % 2 != 0

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `numeros.filter(::esImpar)` por `numeros.filter(::esPar)` creando la función.
//  2. Escribe `val f: (Int) -> Int? = { null }` y compara con `((Int) -> Int)?`.
//  3. Invierte el orden de `incrementar then duplicar` y comprueba el resultado.
//  4. Convierte `comparadorPor` para que acepte también "edadDescendente".
