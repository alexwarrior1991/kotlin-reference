package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.4 · Agregar: fold, reduce, sum y joinToString
//
//  QUÉ ES
//    Las operaciones que reducen una colección entera a UN solo valor: una suma, un
//    máximo, un texto, o cualquier cosa que quieras acumular.
//
//  POR QUÉ IMPORTA
//    `fold` es la operación más general de todas: `map`, `filter`, `sum` y `count` se
//    pueden escribir con ella. Entenderla es entender de dónde salen las demás.
//
//  ERRORES COMUNES
//    · Usar `reduce` sobre una lista que puede estar vacía (lanza excepción).
//    · Escribir un bucle con `var acumulador` cuando `fold` lo dice en una línea.
//    · Concatenar textos con `fold` en vez de usar `joinToString`.
// =====================================================================================

private val ventas = listOf(120, 80, 260, 45, 190)

private data class Articulo(val nombre: String, val precioCentimos: Int, val unidades: Int)

private val cesta = listOf(
    Articulo("teclado", 4_999, 1),
    Articulo("ratón", 1_550, 2),
    Articulo("monitor", 18_900, 1),
)

/**
 * Las agregaciones que ya vienen hechas.
 */
fun demoBuiltInAggregations() {
    section("Números")

    show("ventas", ventas)
    show("sum()", ventas.sum())
    show("average()", "%.2f".format(ventas.average()))
    show("count()", ventas.count())
    show("count { it > 100 }", ventas.count { it > 100 })
    show("max()", ventas.max())
    show("min()", ventas.min())

    bullet("`max()` y `min()` lanzan si la lista está vacía; `maxOrNull()` devuelve null.")
    show("emptyList<Int>().maxOrNull()", emptyList<Int>().maxOrNull())

    section("Sobre una propiedad: sumOf, maxOf, minOf")

    show("cesta", cesta.map { it.nombre })
    show("sumOf { it.unidades }", cesta.sumOf { it.unidades })
    show("sumOf { precio * unidades }", "%.2f €".format(cesta.sumOf { it.precioCentimos * it.unidades } / 100.0))
    show("maxOf { it.precioCentimos }", cesta.maxOf { it.precioCentimos })

    section("Y para obtener el ELEMENTO, no el valor")

    show("maxByOrNull { it.precioCentimos }", cesta.maxByOrNull { it.precioCentimos }?.nombre)
    show("minByOrNull { it.precioCentimos }", cesta.minByOrNull { it.precioCentimos }?.nombre)

    bullet("maxOf → el VALOR máximo. maxByOrNull → el ELEMENTO con ese valor.")
    bullet("Es la confusión más frecuente de todo el capítulo.")

    section("Booleanas: any, all, none")

    show("any { it > 200 }", ventas.any { it > 200 })
    show("all { it > 0 }", ventas.all { it > 0 })
    show("none { it < 0 }", ventas.none { it < 0 })
    show("any()  (¿hay algo?)", ventas.any())
    show("emptyList<Int>().all { false }", emptyList<Int>().all { false })

    bullet("Ojo con `all` sobre una lista vacía: devuelve TRUE ('vacuously true').")
    bullet("Paran en cuanto saben la respuesta: `any` en el primer true, `all` en el primer false.")
}

/**
 * `reduce`: acumular usando los propios elementos.
 */
fun demoReduce() {
    section("Cómo funciona")

    bullet("Toma el primer elemento como acumulador inicial.")
    bullet("Luego aplica la función al acumulador y a cada elemento siguiente.")

    show("ventas", ventas)
    show("reduce { acc, n -> acc + n }", ventas.reduce { acc, n -> acc + n })
    show("reduce { acc, n -> maxOf(acc, n) }", ventas.reduce { acc, n -> maxOf(acc, n) })

    section("El problema: la lista vacía")

    // Como el primer elemento ES el acumulador inicial, sin elementos no hay nada.
    val fallo = try {
        emptyList<Int>().reduce { a, b -> a + b }.toString()
    } catch (e: UnsupportedOperationException) {
        "lanzó UnsupportedOperationException"
    }
    show("emptyList().reduce { }", fallo)
    show("reduceOrNull { a, b -> a + b }", emptyList<Int>().reduceOrNull { a, b -> a + b })

    bullet("Usa `reduce` sólo si SABES que hay al menos un elemento.")
    bullet("Si no, `reduceOrNull()` o, mejor todavía, `fold` con valor inicial.")

    section("Y otra limitación")

    bullet("El acumulador tiene que ser del MISMO tipo que los elementos.")
    bullet("Con `reduce` sobre List<Int> el resultado sólo puede ser Int.")
    bullet("Para cambiar de tipo hace falta `fold`.")
}

/**
 * `fold`: la operación más general.
 */
fun demoFold() {
    section("Con valor inicial: sin problema de lista vacía")

    show("fold(0) { acc, n -> acc + n }", ventas.fold(0) { acc, n -> acc + n })
    show("sobre lista vacía", emptyList<Int>().fold(0) { acc, n -> acc + n })

    section("El acumulador puede ser de OTRO tipo")

    // Aquí acumulamos Int en un String: imposible con `reduce`.
    show("fold(\"\") { acc, n -> ... }", ventas.fold("") { acc, n -> if (acc.isEmpty()) "$n" else "$acc→$n" })

    // O en una estructura compleja.
    val estadisticas = ventas.fold(Estadisticas()) { acc, n -> acc.mas(n) }
    show("fold a una data class", estadisticas)

    section("fold puede implementar casi todo lo demás")

    show("sum con fold", ventas.fold(0) { acc, n -> acc + n })
    show("count con fold", ventas.fold(0) { acc, _ -> acc + 1 })
    show("max con fold", ventas.fold(Int.MIN_VALUE) { acc, n -> maxOf(acc, n) })
    show("filter con fold", ventas.fold(emptyList<Int>()) { acc, n -> if (n > 100) acc + n else acc })
    show("map con fold", ventas.fold(emptyList<String>()) { acc, n -> acc + "€$n" })
    show("reverse con fold", ventas.fold(emptyList<Int>()) { acc, n -> listOf(n) + acc })

    bullet("No escribas así tu código: usa la operación con nombre.")
    bullet("Pero saber que todas salen de `fold` ayuda a entender qué hace cada una.")

    section("foldRight: desde el final")

    val letras = listOf("a", "b", "c")
    show("fold(\"\") { acc, s -> acc + s }", letras.fold("") { acc, s -> acc + s })
    show("foldRight(\"\") { s, acc -> acc + s }", letras.foldRight("") { s, acc -> acc + s })
    bullet("Fíjate en que en foldRight el ACUMULADOR es el segundo parámetro.")

    section("runningFold: todos los pasos intermedios")

    show("runningFold(0) { acc, n -> acc + n }", ventas.runningFold(0) { acc, n -> acc + n })
    show("runningReduce { acc, n -> acc + n }", ventas.runningReduce { acc, n -> acc + n })
    bullet("Es la suma acumulada: perfecta para gráficas y saldos.")
}

/**
 * `joinToString`: agregar a texto.
 */
fun demoJoinToString() {
    section("Lo básico")

    val nombres = listOf("Ana", "Luis", "Marta")
    show("joinToString()", nombres.joinToString())
    show("joinToString(\" · \")", nombres.joinToString(" · "))

    section("Todos los parámetros")

    show(
        "con prefijo y sufijo",
        nombres.joinToString(separator = ", ", prefix = "[", postfix = "]"),
    )

    show(
        "con transformación",
        nombres.joinToString { it.uppercase() },
    )

    show(
        "limitando",
        (1..100).toList().joinToString(limit = 5, truncated = "... y ${100 - 5} más"),
    )

    section("Sobre objetos")

    show("cesta a texto", cesta.joinToString("\n   ") { "${it.nombre} x${it.unidades}" })

    section("Por qué no hacerlo con fold")

    bullet("`fold` para concatenar crea una cadena nueva en cada vuelta.")
    bullet("`joinToString` usa un StringBuilder: una sola cadena al final.")
    bullet("Y además gestiona el separador sin el típico `if (primero)`.")

    section("joinTo: escribir sobre un StringBuilder existente")

    val informe = StringBuilder("Compra:\n")
    cesta.joinTo(informe, separator = "\n", prefix = "  - ") { it.nombre }
    show("joinTo(StringBuilder)", informe.toString().replace("\n", " | "))
}

/** Un acumulador de tipo propio para `fold`. */
private data class Estadisticas(
    val cantidad: Int = 0,
    val total: Int = 0,
    val maximo: Int = Int.MIN_VALUE,
) {
    fun mas(valor: Int): Estadisticas = Estadisticas(
        cantidad = cantidad + 1,
        total = total + valor,
        maximo = maxOf(maximo, valor),
    )
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `distinct()` usando `fold` y una lista como acumulador.
//  2. Cambia `reduce` por `fold` en la demo de la lista vacía y comprueba la diferencia.
//  3. Usa `runningFold` para calcular el saldo de una cuenta tras cada movimiento.
//  4. Calcula el precio total de la cesta con `fold` y compáralo con `sumOf`.
