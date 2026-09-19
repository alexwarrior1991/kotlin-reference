package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 8 · Operaciones con colecciones                               🟡 medio
//
//  Repasa: map, filter, groupBy, associate, sumOf, maxByOrNull, sortedWith,
//  groupingBy/fold, windowed, zipWithNext, secuencias.
//  Capítulos: 13 (colecciones), 14 (secuencias), 15 (lambdas), 25 (stdlib).
// =====================================================================================

/** Ejercicio 8: informes sobre una lista de ventas. */
fun ejercicio08Colecciones() {
    enunciado(
        "Dada una lista de ventas (vendedor, región, mes e importe en céntimos),",
        "escribe los informes que pediría cualquier jefe de ventas.",
        "",
        "1. `totalPorVendedor`: cuánto ha vendido cada uno.",
        "2. `mejorVendedorPorRegion`: quién lidera cada región.",
        "3. `mediaPorMes`: importe medio de venta en cada mes.",
        "4. `porEncimaDeLaMedia`: vendedores cuyo total supera la media general.",
        "5. `rachaMasLargaConVentas`: meses consecutivos vendiendo, por vendedor.",
        "6. `resumenPorRegion`: total, número de ventas y ticket medio de cada región.",
        "7. `topN`: los N vendedores con más importe, con los empates resueltos por",
        "   nombre para que el resultado sea SIEMPRE el mismo.",
        "",
        "Regla: nada de bucles con acumuladores a mano. Todo con la biblioteca.",
    )

    pistas(
        "`groupBy` devuelve `Map<K, List<T>>`; a partir de ahí, `mapValues`.",
        "`sumOf { it.importeCentimos }` suma en Long si el lambda devuelve Long:",
        "   con muchas ventas, un Int se desborda sin avisar (capítulo 02).",
        "`maxByOrNull` devuelve el elemento, no el máximo: justo lo que quieres para",
        "   saber QUIÉN lidera, no cuánto.",
        "Para ordenar con desempate, `sortedWith(compareByDescending { ... }",
        "   .thenBy { ... })` (capítulo 25).",
        "Para la racha, ordena los meses y usa `zipWithNext` o `fold`: la pregunta",
        "   es cuántos pares consecutivos se diferencian en 1.",
        "`groupingBy { }.eachCount()` cuenta por clave sin construir las listas.",
    )

    solucionEnMarcha()

    val ventas = ventasDeEjemplo()
    show("ventas registradas", ventas.size)
    show("vendedores distintos", ventas.map { it.vendedor }.distinct().size)

    section("1. Total por vendedor")

    totalPorVendedor(ventas)
        .entries
        .sortedByDescending { it.value }
        .forEach { (vendedor, total) -> show(vendedor, euros(total.toInt())) }

    section("2. Mejor vendedor por región")

    mejorVendedorPorRegion(ventas).forEach { (region, vendedor) -> show(region, vendedor) }

    section("3. Importe medio por mes")

    mediaPorMes(ventas).toSortedMap().forEach { (mes, media) ->
        show("mes $mes", euros(media.toInt()))
    }

    section("4. Por encima de la media general")

    val media = totalPorVendedor(ventas).values.average()
    show("media de totales", euros(media.toInt()))
    show("por encima", porEncimaDeLaMedia(ventas))

    section("5. Racha más larga de meses consecutivos")

    rachaMasLargaConVentas(ventas)
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .forEach { (vendedor, racha) -> show(vendedor, "$racha meses seguidos") }

    section("6. Resumen por región")

    resumenPorRegion(ventas).forEach { (region, resumen) ->
        show(region, "${resumen.ventas} ventas · total ${euros(resumen.totalCentimos.toInt())} · ticket medio ${euros(resumen.ticketMedioCentimos.toInt())}")
    }

    section("7. Los tres primeros (con desempate estable)")

    topN(ventas, 3).forEachIndexed { indice, (vendedor, total) ->
        show("${indice + 1}º $vendedor", euros(total.toInt()))
    }

    section("Y lo mismo con secuencias")

    show("con List (7 pasos, 6 listas intermedias)", conListas(ventas))
    show("con Sequence (7 pasos, 0 listas intermedias)", conSecuencias(ventas))
    bullet("El resultado es idéntico; lo que cambia es cuánta basura se genera.")
    bullet("Con 40 elementos da igual; con 400.000, no (capítulo 14).")

    explicacion(
        "Cada informe es una frase de la biblioteca estándar, y ése es el ejercicio:",
        "aprender a VER la operación en vez de escribir el bucle.",
        "",
        "   «cuánto vende cada uno»      → groupBy + mapValues + sumOf",
        "   «quién lidera cada región»   → groupBy + mapValues + maxByOrNull",
        "   «los N mejores»              → sortedWith + take",
        "   «meses seguidos»             → sorted + zipWithNext + fold",
        "",
        "Dos detalles que separan un informe correcto de uno que parece correcto:",
        "",
        "SUMAR EN LONG. `sumOf { it.importeCentimos }` con `Int` se desborda en",
        "silencio a los 21 millones de euros. Aquí `importeCentimos` es Int (cabe de",
        "sobra un precio) pero la SUMA es Long. Ese cambio de tipo es deliberado.",
        "",
        "EMPATES ESTABLES. `sortedByDescending { it.value }` deja los empates en un",
        "orden que depende del recorrido del mapa. `thenBy { it.key }` los resuelve",
        "por nombre, y el informe deja de cambiar entre ejecuciones. Un test sobre",
        "un ranking sin desempate es un test intermitente esperando a fallar.",
        "",
        "Y la comparación final: la misma cadena con `List` y con `Sequence`. Con",
        "cuarenta ventas la diferencia es cero; el ejercicio está para que sepas",
        "dónde mirar cuando sean cuatrocientas mil.",
    )

    varianteDificil(
        "1. Añade `crecimientoMensual`: qué porcentaje sube o baja cada mes respecto",
        "   al anterior (pista: `zipWithNext`).",
        "2. Calcula la MEDIANA por región, no la media. Ojo con el número par de",
        "   elementos.",
        "3. Detecta vendedores con ventas anómalas: más del triple de su propia media.",
        "4. Haz un informe cruzado región × mes como tabla de texto alineada.",
        "5. Reescribe `totalPorVendedor` con `groupingBy { }.fold(0L) { ... }` y",
        "   razona por qué es más eficiente que `groupBy` + `sumOf`.",
        "6. Añade percentiles (p50, p90) del importe de venta.",
    )

    testEn("ColeccionesTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

data class Venta(
    val vendedor: String,
    val region: String,
    val mes: Int,
    val importeCentimos: Int,
) {
    init {
        require(mes in 1..12) { "mes fuera de rango: $mes" }
        require(importeCentimos > 0) { "el importe debe ser positivo: $importeCentimos" }
    }
}

data class ResumenRegion(val ventas: Int, val totalCentimos: Long) {
    /** Ticket medio: cuánto vale de media cada venta. Cero ventas → cero. */
    val ticketMedioCentimos: Long get() = if (ventas == 0) 0 else totalCentimos / ventas
}

/**
 * Cuánto ha vendido cada vendedor.
 *
 * La suma es Long a propósito: `Int` se desborda en silencio pasados los 21 millones
 * de euros en céntimos, y no avisaría de nada (capítulo 02).
 */
fun totalPorVendedor(ventas: List<Venta>): Map<String, Long> = ventas
    .groupBy { it.vendedor }
    .mapValues { (_, suyas) -> suyas.sumOf { it.importeCentimos.toLong() } }

/**
 * Quién lidera cada región.
 *
 * `maxByOrNull` devuelve el ELEMENTO con el máximo, no el máximo: justo lo que hace
 * falta para saber quién. El `?: ""` no se alcanza nunca porque `groupBy` no crea
 * grupos vacíos, pero el compilador no lo sabe.
 */
fun mejorVendedorPorRegion(ventas: List<Venta>): Map<String, String> = ventas
    .groupBy { it.region }
    .mapValues { (_, deLaRegion) ->
        deLaRegion
            .groupBy { it.vendedor }
            .mapValues { (_, suyas) -> suyas.sumOf { it.importeCentimos.toLong() } }
            .maxByOrNull { it.value }
            ?.key
            ?: ""
    }

/** Importe medio de cada venta, mes a mes. */
fun mediaPorMes(ventas: List<Venta>): Map<Int, Double> = ventas
    .groupBy { it.mes }
    .mapValues { (_, delMes) -> delMes.map { it.importeCentimos }.average() }

/** Vendedores cuyo total supera la media de los totales. */
fun porEncimaDeLaMedia(ventas: List<Venta>): List<String> {
    val totales = totalPorVendedor(ventas)
    if (totales.isEmpty()) return emptyList()

    val media = totales.values.average()
    return totales
        .filterValues { it > media }
        .keys
        .sorted()
}

/**
 * La racha más larga de meses CONSECUTIVOS con al menos una venta.
 *
 * `zipWithNext` empareja cada mes con el siguiente; a partir de ahí sólo hay que
 * contar cuántos pares se diferencian en 1.
 */
fun rachaMasLargaConVentas(ventas: List<Venta>): Map<String, Int> = ventas
    .groupBy { it.vendedor }
    .mapValues { (_, suyas) ->
        val meses = suyas.map { it.mes }.distinct().sorted()
        if (meses.isEmpty()) {
            0
        } else {
            var mejor = 1
            var actual = 1
            meses.zipWithNext().forEach { (anterior, siguiente) ->
                if (siguiente == anterior + 1) actual++ else actual = 1
                mejor = maxOf(mejor, actual)
            }
            mejor
        }
    }

/** Total, número de ventas y ticket medio de cada región. */
fun resumenPorRegion(ventas: List<Venta>): Map<String, ResumenRegion> = ventas
    .groupBy { it.region }
    .mapValues { (_, deLaRegion) ->
        ResumenRegion(
            ventas = deLaRegion.size,
            totalCentimos = deLaRegion.sumOf { it.importeCentimos.toLong() },
        )
    }
    .toSortedMap()

/**
 * Los N vendedores con más importe.
 *
 * El desempate por nombre NO es un capricho: sin él, dos vendedores con el mismo
 * total saldrían en un orden que depende del recorrido interno del mapa, y el
 * informe cambiaría de una ejecución a otra.
 */
fun topN(ventas: List<Venta>, n: Int): List<Pair<String, Long>> = totalPorVendedor(ventas)
    .toList()
    .sortedWith(compareByDescending<Pair<String, Long>> { it.second }.thenBy { it.first })
    .take(n)

// -- La misma cadena, con listas y con secuencias -------------------------------------------------

/** Siete operaciones encadenadas: cada una crea una lista intermedia. */
fun conListas(ventas: List<Venta>): List<String> = ventas
    .filter { it.importeCentimos > 1_000 }
    .map { it.copy(vendedor = it.vendedor.uppercase()) }
    .filter { it.mes <= 6 }
    .sortedByDescending { it.importeCentimos }
    .map { it.vendedor }
    .distinct()
    .take(3)

/** Lo mismo sin listas intermedias: sólo se recorre lo necesario. */
fun conSecuencias(ventas: List<Venta>): List<String> = ventas
    .asSequence()
    .filter { it.importeCentimos > 1_000 }
    .map { it.copy(vendedor = it.vendedor.uppercase()) }
    .filter { it.mes <= 6 }
    .sortedByDescending { it.importeCentimos }
    .map { it.vendedor }
    .distinct()
    .take(3)
    .toList()

/** Los datos de ejemplo. Fijos a propósito: los tests necesitan resultados estables. */
fun ventasDeEjemplo(): List<Venta> = listOf(
    Venta("Ana", "Norte", 1, 120_00),
    Venta("Ana", "Norte", 2, 95_50),
    Venta("Ana", "Norte", 3, 210_00),
    Venta("Ana", "Sur", 5, 80_00),
    Venta("Luis", "Norte", 1, 300_00),
    Venta("Luis", "Norte", 4, 45_00),
    Venta("Luis", "Sur", 4, 150_00),
    Venta("Luis", "Sur", 5, 60_00),
    Venta("Luis", "Sur", 6, 90_00),
    Venta("Marta", "Sur", 2, 500_00),
    Venta("Marta", "Sur", 3, 75_00),
    Venta("Marta", "Este", 7, 320_00),
    Venta("Pedro", "Este", 1, 40_00),
    Venta("Pedro", "Este", 2, 55_00),
    Venta("Pedro", "Este", 3, 65_00),
    Venta("Pedro", "Este", 4, 70_00),
    Venta("Pedro", "Este", 5, 30_00),
    Venta("Sara", "Norte", 11, 900_00),
    Venta("Sara", "Norte", 12, 150_00),
)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `.thenBy { it.first }` de `topN` y ejecuta la demo varias veces con
//     dos vendedores empatados: el orden deja de estar garantizado.
//  2. Cambia `sumOf { it.importeCentimos.toLong() }` por `sumOf { it.importeCentimos }`
//     y multiplica los importes por 100.000 para ver el desbordamiento.
//  3. Reescribe `rachaMasLargaConVentas` con `fold` en lugar de dos `var`.
