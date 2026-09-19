package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.5 · Agrupar: groupBy, associateBy y groupingBy
//
//  QUÉ ES
//    Las operaciones que convierten una lista en un Map. Cada una lo hace de forma
//    distinta, y elegir mal tiene consecuencias silenciosas.
//
//  POR QUÉ IMPORTA
//    `groupBy` y `associateBy` se parecen tanto que es fácil confundirlas, pero
//    `associateBy` **descarta duplicados sin avisar**. Es una de las causas más
//    sutiles de "faltan datos y no sé por qué".
//
//  ERRORES COMUNES
//    · Usar `associateBy` con una clave que no es única y perder registros.
//    · Contar con `groupBy { }.mapValues { it.value.size }` en vez de `eachCount()`.
//    · Olvidar que el Map resultante conserva el orden de inserción.
// =====================================================================================

private data class Libro(
    val titulo: String,
    val autor: String,
    val genero: String,
    val anio: Int,
    val paginas: Int,
)

private val biblioteca = listOf(
    Libro("Dune", "Herbert", "ciencia ficción", 1965, 412),
    Libro("Neuromante", "Gibson", "ciencia ficción", 1984, 271),
    Libro("El nombre de la rosa", "Eco", "histórica", 1980, 502),
    Libro("Mundo Anillo", "Niven", "ciencia ficción", 1970, 342),
    Libro("El péndulo de Foucault", "Eco", "histórica", 1988, 641),
    Libro("Fundación", "Asimov", "ciencia ficción", 1951, 255),
)

/**
 * `groupBy`: una clave, VARIOS valores.
 */
fun demoGroupBy() {
    section("Agrupar por una propiedad")

    val porGenero = biblioteca.groupBy { it.genero }
    show("tipo del resultado", "Map<String, List<Libro>>")
    porGenero.forEach { (genero, libros) ->
        show(genero, libros.map { it.titulo })
    }

    section("Con transformación del valor")

    // El segundo lambda transforma cada elemento antes de guardarlo.
    val titulosPorAutor = biblioteca.groupBy({ it.autor }, { it.titulo })
    show("groupBy(clave, valor)", titulosPorAutor)

    section("Agrupar por algo calculado")

    val porDecada = biblioteca.groupBy { "${it.anio / 10 * 10}s" }
    show("por década", porDecada.mapValues { it.value.size })

    val porTamano = biblioteca.groupBy {
        when {
            it.paginas < 300 -> "corto"
            it.paginas < 500 -> "medio"
            else -> "largo"
        }
    }
    show("por tamaño", porTamano.mapValues { entrada -> entrada.value.map { it.titulo } })

    section("Después de agrupar, normalmente se agrega")

    show("nº de libros por género", porGenero.mapValues { it.value.size })
    show("páginas medias por género", porGenero.mapValues { "%.0f".format(it.value.map { l -> l.paginas }.average()) })
    show("el más largo de cada género", porGenero.mapValues { it.value.maxByOrNull { l -> l.paginas }?.titulo })
}

/**
 * `associateBy`: una clave, UN valor. Y la trampa.
 */
fun demoAssociateBy() {
    section("Cuando la clave es única, es justo lo que quieres")

    val porTitulo = biblioteca.associateBy { it.titulo }
    show("tipo del resultado", "Map<String, Libro>")
    show("porTitulo[\"Dune\"]?.autor", porTitulo["Dune"]?.autor)
    show("cuántas entradas", porTitulo.size)

    bullet("Es el índice de toda la vida: buscar por id en O(1) en lugar de recorrer.")

    section("LA TRAMPA: cuando la clave NO es única")

    // Eco tiene dos libros. `associateBy` se queda con el ÚLTIMO y descarta el otro
    // SIN NINGÚN AVISO.
    val porAutor = biblioteca.associateBy { it.autor }
    show("libros en la biblioteca", biblioteca.size)
    show("entradas en associateBy { autor }", porAutor.size)
    show("el libro de Eco que sobrevivió", porAutor["Eco"]?.titulo)

    bullet("De 6 libros salen 4 entradas: se han perdido 2 en silencio.")
    bullet("Gana el ÚLTIMO de cada clave, no el primero.")
    bullet("Es un bug clásico: el código funciona hasta que aparece un duplicado.")

    section("Si la clave puede repetirse, usa groupBy")

    val agrupadoPorAutor = biblioteca.groupBy { it.autor }
    show("groupBy { autor }: entradas", agrupadoPorAutor.size)
    show("y los de Eco", agrupadoPorAutor["Eco"]?.map { it.titulo })
    show("total de libros conservados", agrupadoPorAutor.values.sumOf { it.size })

    section("La regla")

    bullet("¿La clave es única (un id)?       → associateBy")
    bullet("¿Puede repetirse (una categoría)? → groupBy")
    bullet("Si dudas, usa groupBy: no pierde nada.")

    section("associateWith y associate")

    // associateWith: los elementos son las CLAVES y el lambda calcula el valor.
    show("associateWith", listOf("uno", "dos", "tres").associateWith { it.length })

    // associate: el lambda devuelve el par entero. El más flexible, el menos usado.
    show("associate", biblioteca.take(2).associate { it.titulo to it.anio })

    bullet("associateBy  → el lambda da la CLAVE, el elemento es el valor")
    bullet("associateWith → el elemento es la CLAVE, el lambda da el valor")
    bullet("associate    → el lambda da el par completo")
}

/**
 * `groupingBy`: agregar mientras se agrupa.
 */
fun demoGroupingBy() {
    section("El problema")

    // `groupBy` construye TODAS las listas intermedias aunque sólo quieras contar.
    val contandoConGroupBy = biblioteca.groupBy { it.genero }.mapValues { it.value.size }
    show("groupBy { }.mapValues { size }", contandoConGroupBy)
    bullet("Se han creado listas con todos los libros sólo para preguntar cuántos hay.")

    section("La solución: groupingBy + eachCount")

    val contando = biblioteca.groupingBy { it.genero }.eachCount()
    show("groupingBy { }.eachCount()", contando)
    bullet("`groupingBy` no construye nada: devuelve un Grouping perezoso.")
    bullet("La agregación ocurre en una sola pasada, sin listas intermedias.")

    section("Otros agregadores")

    show(
        "fold: páginas por género",
        biblioteca.groupingBy { it.genero }.fold(0) { acc, libro -> acc + libro.paginas },
    )

    show(
        "reduce: el más antiguo por género",
        biblioteca.groupingBy { it.genero }
            .reduce { _, acumulado, libro -> if (libro.anio < acumulado.anio) libro else acumulado }
            .mapValues { it.value.titulo },
    )

    show(
        "aggregate: el más flexible",
        biblioteca.groupingBy { it.genero }.aggregate { _, acc: String?, libro, primero ->
            if (primero) libro.titulo else "$acc, ${libro.titulo}"
        },
    )

    section("Cuándo usar cada uno")

    bullet("¿Necesitas las listas agrupadas? → groupBy")
    bullet("¿Sólo quieres un número por grupo? → groupingBy + eachCount/fold/reduce")
    bullet("Con pocos elementos da igual; con muchos, groupingBy ahorra bastante.")
}

/**
 * Trabajar con el Map resultante.
 */
fun demoWorkingWithMaps() {
    section("Recorrer un Map")

    val porGenero = biblioteca.groupBy { it.genero }.mapValues { it.value.size }

    // Con destructuring, que es lo idiomático.
    porGenero.forEach { (genero, cantidad) ->
        show(genero, cantidad)
    }

    section("Ordenar un Map")

    show("por clave", porGenero.toSortedMap())
    show("por valor descendente", porGenero.entries.sortedByDescending { it.value }.map { "${it.key}=${it.value}" })

    bullet("Un Map normal no tiene orden 'por valor': hay que pasar por entries.")
    bullet("`toSortedMap()` sí ordena por clave y devuelve un Map ordenado.")

    section("Acceso seguro")

    show("porGenero[\"poesía\"]", porGenero["poesía"])
    show("getOrDefault", porGenero.getOrDefault("poesía", 0))
    show("getOrElse", porGenero.getOrElse("poesía") { 0 })
    show("getValue (lanza si falta)", runCatching { porGenero.getValue("poesía") }.getOrElse { "lanzó ${it::class.simpleName}" })

    section("Combinar mapas")

    val a = mapOf("x" to 1, "y" to 2)
    val b = mapOf("y" to 20, "z" to 30)
    show("a + b  (gana el de la derecha)", a + b)
    show("filtrar y volver a mapa", (a + b).filterValues { it > 5 })

    section("Invertir un mapa")

    val original = mapOf("ana" to 34, "luis" to 28)
    show("original", original)
    show("invertido con associate", original.entries.associate { (k, v) -> v to k })
    bullet("Cuidado: si dos claves comparten valor, al invertir se pierde una.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade otro libro de Asimov y comprueba cómo cambia `associateBy { autor }`.
//  2. Calcula, por década, el autor con más páginas escritas.
//  3. Sustituye `groupBy { }.mapValues { it.value.size }` por `eachCount()` en tu código.
//  4. Agrupa por dos criterios a la vez: `groupBy { it.genero to it.anio / 10 }`.
