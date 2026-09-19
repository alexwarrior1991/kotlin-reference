package com.alejandro.c14sequences

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  14.1 · Iterable frente a Sequence: eager y lazy
//
//  QUÉ ES
//    Una `Sequence` ofrece las mismas operaciones que una colección (`map`, `filter`,
//    `take`...) pero las evalúa de forma PEREZOSA: nada ocurre hasta que pides el
//    resultado, y entonces cada elemento recorre toda la cadena de una vez.
//
//  POR QUÉ IMPORTA
//    Con una colección, cada `map` y cada `filter` crea una LISTA INTERMEDIA completa.
//    Con cinco operaciones sobre un millón de elementos, son cinco millones de objetos
//    que el recolector de basura tendrá que limpiar.
//
//  ERRORES COMUNES
//    · Usar secuencias para tres elementos: el coste de montarlas supera la ganancia.
//    · Olvidar la operación terminal y preguntarse por qué "no hace nada".
//    · Creer que una secuencia se puede recorrer varias veces (muchas, no).
// =====================================================================================

/**
 * El orden de evaluación, que es donde está toda la diferencia.
 */
fun demoEvaluationOrder() {
    section("Con una lista (eager): por operaciones")

    // Cada operación procesa TODA la colección antes de pasar a la siguiente.
    val trazaLista = mutableListOf<String>()
    val resultadoLista = listOf(1, 2, 3, 4)
        .map { trazaLista.add("map($it)"); it * 2 }
        .filter { trazaLista.add("filter($it)"); it > 4 }

    show("resultado", resultadoLista)
    println()
    println("      orden real de ejecución:")
    println("      ${trazaLista.joinToString(" ")}")

    bullet("Primero los cuatro `map`, DESPUÉS los cuatro `filter`.")
    bullet("Entre medias se creó una lista intermedia con los cuatro dobles.")

    section("Con una secuencia (lazy): por elementos")

    val trazaSecuencia = mutableListOf<String>()
    val resultadoSecuencia = listOf(1, 2, 3, 4)
        .asSequence()
        .map { trazaSecuencia.add("map($it)"); it * 2 }
        .filter { trazaSecuencia.add("filter($it)"); it > 4 }
        .toList()

    show("resultado", resultadoSecuencia)
    println()
    println("      orden real de ejecución:")
    println("      ${trazaSecuencia.joinToString(" ")}")

    bullet("Cada elemento atraviesa map y filter antes de que entre el siguiente.")
    bullet("No hay lista intermedia: sólo la final.")

    section("Mismo resultado, distinto camino")

    show("¿el resultado es el mismo?", resultadoLista == resultadoSecuencia)
    bullet("Siempre. Lo que cambia es CUÁNTO trabajo se hace y cuánta memoria se usa.")
}

/**
 * Operaciones intermedias y terminales.
 */
fun demoIntermediateAndTerminal() {
    section("Sin operación terminal, no pasa nada")

    var vecesEjecutado = 0
    val secuencia = listOf(1, 2, 3)
        .asSequence()
        .map { vecesEjecutado++; it * 2 }

    show("tras declarar la secuencia, ¿se ejecutó el map?", vecesEjecutado)
    bullet("Cero. `map` sobre una secuencia es INTERMEDIA: sólo describe qué hacer.")

    val resultado = secuencia.toList()     // ← ésta es la terminal
    show("tras toList(), veces ejecutado", vecesEjecutado)
    show("resultado", resultado)

    section("Intermedias (devuelven otra Sequence, son perezosas)")

    bullet("map · filter · mapNotNull · flatMap · take · drop · distinct")
    bullet("sorted · chunked · windowed · zip · onEach · takeWhile · dropWhile")

    section("Terminales (devuelven un valor, disparan el trabajo)")

    bullet("toList · toSet · toMap · first · firstOrNull · last · single")
    bullet("count · sum · max · min · reduce · fold · any · all · none")
    bullet("forEach · joinToString · groupBy · associateBy")

    section("El error clásico")

    // Esto no imprime nada: `map` es intermedia y nadie pide el resultado.
    listOf(1, 2, 3).asSequence().map { println("      esto NO se imprime: $it") }
    bullet("Un `map` perezoso sin terminal es código muerto. Con `forEach` sí actuaría.")
    listOf(1, 2, 3).asSequence().forEach { /* terminal: sí se ejecuta */ }

    section("Una excepción importante: `sorted`")

    bullet("`sorted` es intermedia, pero para ordenar necesita TODOS los elementos.")
    bullet("Internamente acumula todo en una lista: ahí se pierde la pereza.")
    bullet("Por eso, si ordenas, pon el `sorted` lo más tarde posible en la cadena.")
}

/**
 * El cortocircuito: la ventaja más visible.
 */
fun demoShortCircuiting() {
    section("Buscar el primero que cumple")

    val numeros = (1..1_000_000).toList()

    // Con lista: `map` transforma UN MILLÓN de elementos y luego se coge el primero.
    var operacionesLista = 0
    val conLista = numeros
        .map { operacionesLista++; it * 2 }
        .first { it > 10 }

    show("resultado con lista", conLista)
    show("operaciones realizadas", operacionesLista)

    // Con secuencia: para en cuanto encuentra uno.
    var operacionesSecuencia = 0
    val conSecuencia = numeros
        .asSequence()
        .map { operacionesSecuencia++; it * 2 }
        .first { it > 10 }

    show("resultado con secuencia", conSecuencia)
    show("operaciones realizadas", operacionesSecuencia)

    bullet("Un millón de operaciones frente a seis. Mismo resultado.")
    bullet("Es el caso donde las secuencias ganan de calle: `first`, `any`, `take`.")

    section("Lo mismo con take")

    var conTake = 0
    val primerosTres = numeros.asSequence().map { conTake++; it * it }.take(3).toList()
    show("los tres primeros cuadrados", primerosTres)
    show("operaciones realizadas", conTake)

    section("Y con any")

    var conAny = 0
    val hayGrande = numeros.asSequence().onEach { conAny++ }.any { it > 100 }
    show("¿hay alguno > 100?", hayGrande)
    show("elementos examinados", conAny)
}

/**
 * Las limitaciones que conviene conocer.
 */
fun demoSequenceLimitations() {
    section("Una secuencia suele ser de un solo uso")

    // Las creadas con `asSequence()` sobre una colección se pueden recorrer varias
    // veces (la colección sigue ahí), pero las generadas NO.
    val desdeLista = listOf(1, 2, 3).asSequence()
    show("primer recorrido", desdeLista.toList())
    show("segundo recorrido", desdeLista.toList())
    bullet("Ésta sí: por debajo vuelve a pedir el iterador a la lista.")

    val generada = generateSequence(1) { if (it < 3) it + 1 else null }
    show("generateSequence, primer recorrido", generada.toList())
    show("generateSequence, segundo recorrido", generada.toList())
    bullet("Ésta también, porque la función generadora se vuelve a ejecutar.")

    // La que NO se puede repetir es la construida sobre un iterador.
    val deIterador = listOf(1, 2, 3).iterator().asSequence()
    show("desde un iterador, primer recorrido", deIterador.toList())
    val segundoIntento = runCatching { deIterador.toList() }
    show("segundo recorrido", segundoIntento.exceptionOrNull()?.let { it::class.simpleName } ?: segundoIntento.getOrNull())

    bullet("Regla práctica: trata toda secuencia como de un solo uso y no la guardes.")

    section("No hay acceso por índice ni size")

    bullet("`secuencia[0]` no existe: habría que recorrerla hasta ahí.")
    bullet("`secuencia.count()` la recorre ENTERA y la consume.")
    bullet("Si necesitas el tamaño o indexar, quieres una lista.")

    section("Los mensajes de error son peores")

    bullet("Una excepción dentro de una cadena perezosa aparece en la TERMINAL,")
    bullet("lejos de la operación que realmente falló. Depurar cuesta más.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade un tercer `.map { }` a la demo de la traza y observa cómo cambia el orden.
//  2. Quita el `.toList()` final de una cadena de secuencia y comprueba que no hace nada.
//  3. Cambia `first { it > 10 }` por `last { it > 10 }` y razona por qué ya no cortocircuita.
//  4. Pon el `sorted()` al principio y al final de una cadena y piensa qué cambia.
