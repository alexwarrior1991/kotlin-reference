package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.2 · Transformar: map, flatMap, zip
//
//  QUÉ ES
//    Las operaciones que producen una colección NUEVA a partir de otra aplicando una
//    función a cada elemento.
//
//  POR QUÉ IMPORTA
//    Sustituyen al 80% de los bucles que escribirías. Y, sobre todo, el nombre de la
//    operación dice qué hace: al leer `map` ya sabes que sale una colección del mismo
//    tamaño, y al leer `filter`, que sale una más corta o igual.
//
//  ERRORES COMUNES
//    · Encadenar diez operaciones sobre una lista grande creando nueve intermedias
//      (para eso están las secuencias, capítulo 14).
//    · Usar `map` cuando el resultado se descarta: eso es `forEach` o `onEach`.
//    · Olvidar que `flatten` necesita una colección DE colecciones.
// =====================================================================================

private data class Empleado(
    val nombre: String,
    val departamento: String,
    val salario: Int,
    val idiomas: List<String>,
)

private val plantilla = listOf(
    Empleado("Ana", "Ingeniería", 52_000, listOf("es", "en")),
    Empleado("Luis", "Ingeniería", 47_000, listOf("es")),
    Empleado("Marta", "Diseño", 45_000, listOf("es", "en", "fr")),
    Empleado("Carlos", "Ventas", 38_000, listOf("es", "pt")),
    Empleado("Sara", "Diseño", 49_000, listOf("en")),
)

/**
 * `map`: uno a uno.
 */
fun demoMap() {
    section("La idea")

    bullet("`map` aplica una función a CADA elemento y devuelve una lista del MISMO tamaño.")

    val numeros = listOf(1, 2, 3, 4)
    show("numeros", numeros)
    show("map { it * 2 }", numeros.map { it * 2 })
    show("map { it.toString() }", numeros.map { it.toString() })

    section("Sobre objetos")

    show("map { it.nombre }", plantilla.map { it.nombre })
    show("con referencia a propiedad", plantilla.map(Empleado::nombre))
    show("map a otro objeto", plantilla.take(2).map { Resumen(it.nombre, it.salario / 12) })

    section("mapIndexed: con la posición")

    show("mapIndexed { i, e -> ... }", plantilla.mapIndexed { i, e -> "${i + 1}. ${e.nombre}" })

    section("mapNotNull: transforma y descarta nulos")

    val textos = listOf("1", "dos", "3", "cuatro", "5")

    // Sin mapNotNull harían falta dos pasos: map y luego filterNotNull.
    show("map { it.toIntOrNull() }", textos.map { it.toIntOrNull() })
    show("mapNotNull { it.toIntOrNull() }", textos.mapNotNull { it.toIntOrNull() })

    bullet("`mapNotNull` es de las operaciones más útiles: parsea y descarta lo inválido.")

    section("En un Map: mapKeys y mapValues")

    val salarios = mapOf("Ana" to 52_000, "Luis" to 47_000)
    show("mapValues { it.value / 12 }", salarios.mapValues { it.value / 12 })
    show("mapKeys { it.key.uppercase() }", salarios.mapKeys { it.key.uppercase() })
    show("map { }  sobre un Map da una List", salarios.map { "${it.key}:${it.value}" })

    bullet("Ojo: `map` sobre un Map devuelve una LIST de lo que produzca la lambda.")
    bullet("Para seguir teniendo un Map, usa `mapValues` o `mapKeys`.")
}

/**
 * `flatMap` y `flatten`: de varios niveles a uno.
 */
fun demoFlatMap() {
    section("El problema")

    // `map` produce una lista de LISTAS, que casi nunca es lo que quieres.
    show("map { it.idiomas }", plantilla.map { it.idiomas })
    show("su tipo", "List<List<String>>")

    section("flatten: aplanar un nivel")

    show("map { it.idiomas }.flatten()", plantilla.map { it.idiomas }.flatten())

    section("flatMap: map + flatten de una vez")

    show("flatMap { it.idiomas }", plantilla.flatMap { it.idiomas })
    show("y sin duplicados", plantilla.flatMap { it.idiomas }.distinct())
    show("cuántos habla cada idioma", plantilla.flatMap { it.idiomas }.groupingBy { it }.eachCount())

    bullet("`flatMap { f }` es exactamente `map { f }.flatten()`, pero en una pasada.")

    section("Otros usos de flatMap")

    show("cada palabra en letras", listOf("sol", "luz").flatMap { it.toList() })
    show("producto cartesiano", listOf(1, 2).flatMap { a -> listOf("x", "y").map { b -> "$a$b" } })

    section("Aplanar un nivel, no todos")

    val anidada = listOf(listOf(listOf(1, 2), listOf(3)), listOf(listOf(4)))
    show("anidada (3 niveles)", anidada)
    show("flatten() una vez", anidada.flatten())
    show("flatten() dos veces", anidada.flatten().flatten())
    bullet("`flatten` quita UN nivel. Para varios, encadénalo.")
}

/**
 * `zip` y `unzip`: emparejar colecciones.
 */
fun demoZip() {
    section("zip: unir dos listas elemento a elemento")

    val nombres = listOf("Ana", "Luis", "Marta")
    val edades = listOf(34, 28, 41)

    show("nombres zip edades", nombres.zip(edades))
    show("es infix, también vale con punto", nombres.zip(edades).first())

    section("Con transformación")

    show("zip { n, e -> \"\$n (\$e)\" }", nombres.zip(edades) { n, e -> "$n ($e)" })

    section("Se para en la más corta")

    val pocos = listOf(1, 2)
    show("3 nombres zip 2 números", nombres.zip(pocos))
    bullet("No lanza ni rellena: simplemente descarta lo que sobra.")

    section("unzip: la operación inversa")

    val pares = listOf("a" to 1, "b" to 2, "c" to 3)
    val (letras, numeros) = pares.unzip()
    show("pares", pares)
    show("unzip → primera", letras)
    show("unzip → segunda", numeros)

    section("zipWithNext: cada elemento con el siguiente")

    val temperaturas = listOf(12, 15, 14, 18, 17)
    show("temperaturas", temperaturas)
    show("zipWithNext()", temperaturas.zipWithNext())
    show("variación entre días", temperaturas.zipWithNext { hoy, manana -> manana - hoy })

    bullet("`zipWithNext` es ideal para diferencias, tendencias y detectar cambios.")

    section("Un caso completo")

    show("a mapa desde dos listas", nombres.zip(edades).toMap())
}

/**
 * Transformaciones que NO transforman: onEach y forEach.
 */
fun demoOnEachVsForEach() {
    section("forEach: hace algo y devuelve Unit")

    val numeros = listOf(1, 2, 3)
    val resultadoForEach = numeros.forEach { }
    show("tipo devuelto por forEach", resultadoForEach::class.simpleName)
    bullet("No se puede encadenar nada después de un `forEach`.")

    section("onEach: hace algo y devuelve la colección")

    val resultadoOnEach = numeros
        .onEach { /* aquí iría una traza */ }
        .map { it * 10 }
    show("onEach { }.map { }", resultadoOnEach)
    bullet("`onEach` sirve para meter una traza EN MEDIO de una cadena sin romperla.")

    section("El error clásico: map para efectos secundarios")

    // Esto crea una lista de Unit que nadie usa. Es un forEach escrito mal.
    val listaDeUnit = numeros.map { }
    show("numeros.map { }  (lista inútil)", listaDeUnit)
    bullet("Si no usas el resultado de un `map`, querías un `forEach`.")

    section("Resumen")

    bullet("map     → transforma y devuelve una colección nueva")
    bullet("onEach  → hace algo y devuelve LA MISMA colección")
    bullet("forEach → hace algo y devuelve Unit")
}

private data class Resumen(val nombre: String, val salarioMensual: Int)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `flatMap { it.idiomas }` por `map { it.idiomas }` y mira el tipo resultante.
//  2. Haz un `zip` de tres listas encadenando dos zips y observa cómo se anidan los Pair.
//  3. Usa `zipWithNext` para detectar en qué días subió la temperatura.
//  4. Escribe `plantilla.mapNotNull { if (it.salario > 48_000) it.nombre else null }`.
