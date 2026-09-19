package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.3 · Filtrar y trocear
//
//  QUÉ ES
//    Las operaciones que producen un SUBCONJUNTO de la colección original: `filter`,
//    `take`, `drop`, `partition`, `chunked`, `windowed`, `distinct`.
//
//  POR QUÉ IMPORTA
//    Son la otra mitad (con `map`) de casi cualquier procesamiento de datos. Y algunas,
//    como `partition`, `chunked` y `windowed`, resuelven en una línea cosas que a mano
//    ocupan quince y salen mal.
//
//  ERRORES COMUNES
//    · Recorrer dos veces con `filter { x }` y `filter { !x }` en vez de `partition`.
//    · Confundir `filterNot` con `filterNotNull` (no tienen nada que ver).
//    · Usar `distinct()` sobre objetos sin `equals` y no entender por qué no deduplica.
// =====================================================================================

private val puntuaciones = listOf(72, 45, 90, 33, 88, 61, 95, 50)
private val palabras = listOf("kotlin", "es", "un", "lenguaje", "moderno", "y", "conciso")

/**
 * `filter` y su familia.
 */
fun demoFilter() {
    section("filter: quedarse con los que cumplen")

    show("puntuaciones", puntuaciones)
    show("filter { it >= 60 }", puntuaciones.filter { it >= 60 })
    show("filterNot { it >= 60 }", puntuaciones.filterNot { it >= 60 })

    bullet("`filterNot { p }` es lo mismo que `filter { !p }`, pero se lee mejor.")

    section("filterIndexed: usando la posición")

    show("los de posición par", puntuaciones.filterIndexed { i, _ -> i % 2 == 0 })

    section("filterNotNull: quitar los nulos")

    val conNulos = listOf("a", null, "b", null, "c")
    show("lista con nulos", conNulos)
    show("filterNotNull()", conNulos.filterNotNull())
    show("su tipo pasa de List<String?> a", "List<String>")

    bullet("Es más que un filtro: CAMBIA EL TIPO. Después ya no hace falta `?.`.")

    section("filterIsInstance: filtrar por tipo")

    val mezcla: List<Any> = listOf("texto", 42, 3.14, "otro", true)
    show("mezcla", mezcla)
    show("filterIsInstance<String>()", mezcla.filterIsInstance<String>())
    show("filterIsInstance<Number>()", mezcla.filterIsInstance<Number>())

    bullet("También cambia el tipo: el resultado es List<String>, no List<Any>.")
    bullet("Por dentro usa `reified` (capítulo 12).")

    section("En un Map")

    val inventario = mapOf("teclado" to 5, "ratón" to 0, "monitor" to 2)
    show("filter { it.value > 0 }", inventario.filter { it.value > 0 })
    show("filterKeys { it.length > 5 }", inventario.filterKeys { it.length > 5 })
    show("filterValues { it == 0 }", inventario.filterValues { it == 0 })
}

/**
 * `partition`: filtrar en dos de una pasada.
 */
fun demoPartition() {
    section("El problema")

    // Con dos filter recorres la colección DOS veces y repites (negada) la condición,
    // que es justo donde se cuela el error.
    val aprobadosMal = puntuaciones.filter { it >= 60 }
    val suspensosMal = puntuaciones.filter { it < 60 }
    show("dos filter → aprobados", aprobadosMal)
    show("dos filter → suspensos", suspensosMal)

    section("La solución")

    val (aprobados, suspensos) = puntuaciones.partition { it >= 60 }
    show("partition → aprobados", aprobados)
    show("partition → suspensos", suspensos)

    bullet("Una sola pasada, una sola condición escrita una sola vez.")
    bullet("Devuelve un Pair, así que se desestructura directamente.")

    section("Un caso real: separar válidos de inválidos")

    val entradas = listOf("12", "abc", "7", "", "99")
    val (validas, invalidas) = entradas.partition { it.toIntOrNull() != null }
    show("válidas", validas)
    show("inválidas", invalidas)
    show("y ya parseadas", validas.map { it.toInt() })
}

/**
 * `take` y `drop`.
 */
fun demoTakeAndDrop() {
    section("Por cantidad")

    show("palabras", palabras)
    show("take(3)", palabras.take(3))
    show("drop(3)", palabras.drop(3))
    show("takeLast(2)", palabras.takeLast(2))
    show("dropLast(2)", palabras.dropLast(2))

    bullet("Si pides más de los que hay, devuelve lo que haya. No lanza.")
    show("take(100)", palabras.take(100))

    section("Por condición")

    // `takeWhile` para en el PRIMERO que no cumple, no filtra todos.
    val creciente = listOf(1, 3, 5, 4, 7, 9)
    show("lista", creciente)
    show("takeWhile { it < 6 }", creciente.takeWhile { it < 6 })
    show("filter { it < 6 }  (¡distinto!)", creciente.filter { it < 6 })
    show("dropWhile { it < 6 }", creciente.dropWhile { it < 6 })

    bullet("`takeWhile` recorre 1, 3, 5, 4... y SE PARA al llegar al 7, que no cumple.")
    bullet("Por eso devuelve [1, 3, 5, 4]: nunca llega a mirar el 9.")
    bullet("`filter` recorre TODO y se queda con todos los que cumplen, incluido el 9")
    bullet("si lo cumpliera. Son operaciones distintas, no dos formas de lo mismo.")

    section("Un caso real: paginar")

    val pagina = 2
    val tamanoPagina = 3
    show("página $pagina de tamaño $tamanoPagina", palabras.drop((pagina - 1) * tamanoPagina).take(tamanoPagina))
}

/**
 * `chunked` y `windowed`: trocear en grupos.
 */
fun demoChunkedAndWindowed() {
    section("chunked: trozos que NO se solapan")

    val numeros = (1..10).toList()
    show("numeros", numeros)
    show("chunked(3)", numeros.chunked(3))
    show("el último trozo puede ser más corto", numeros.chunked(4))

    section("chunked con transformación")

    show("chunked(3) { it.sum() }", numeros.chunked(3) { it.sum() })

    section("windowed: ventanas que SÍ se solapan")

    show("windowed(3)", numeros.windowed(3))
    show("windowed(3, step = 2)", numeros.windowed(3, step = 2))
    show("windowed(3, partialWindows = true)", numeros.windowed(3, partialWindows = true).takeLast(3))

    section("El caso de uso estrella: medias móviles")

    val ventas = listOf(10, 12, 9, 15, 20, 18, 14)
    show("ventas diarias", ventas)
    show("media móvil de 3 días", ventas.windowed(3) { "%.1f".format(it.average()) })

    section("Otro: procesar por lotes")

    val ids = (1..7).toList()
    val lotes = ids.chunked(3).mapIndexed { i, lote -> "lote ${i + 1}: $lote" }
    lotes.forEach { bullet(it) }
    bullet("Es el patrón para enviar a una API que acepta como mucho N por petición.")

    section("La diferencia, en una línea")

    bullet("chunked(3)  → [1,2,3] [4,5,6] ...  (cada elemento en UN grupo)")
    bullet("windowed(3) → [1,2,3] [2,3,4] ...  (cada elemento en VARIOS grupos)")
}

/**
 * `distinct` y compañía.
 */
fun demoDistinct() {
    section("distinct: quitar duplicados")

    val conRepetidos = listOf("a", "b", "a", "c", "b", "a")
    show("lista", conRepetidos)
    show("distinct()", conRepetidos.distinct())
    show("toSet()  (lo mismo, pero devuelve Set)", conRepetidos.toSet())

    bullet("`distinct` conserva el ORDEN de la primera aparición.")

    section("distinctBy: duplicado según un criterio")

    val personas = listOf(
        PersonaSimple("Ana", "Madrid"),
        PersonaSimple("Luis", "Madrid"),
        PersonaSimple("Marta", "Sevilla"),
    )
    show("todas", personas.map { it.nombre })
    show("distinctBy { it.ciudad }", personas.distinctBy { it.ciudad }.map { "${it.nombre} (${it.ciudad})" })

    bullet("Se queda con el PRIMERO de cada grupo, no con el último.")

    section("Cuidado: distinct usa equals")

    // Sin `data`, dos objetos con los mismos datos son distintos para `distinct`.
    val sinEquals = listOf(SinEquals("x"), SinEquals("x"))
    show("sin equals: distinct()", sinEquals.distinct().size)
    val conEquals = listOf(PersonaSimple("Ana", "Madrid"), PersonaSimple("Ana", "Madrid"))
    show("con data class: distinct()", conEquals.distinct().size)

    bullet("Si `distinct` no deduplica lo que esperas, mira el `equals` de tu clase.")

    section("Operaciones de conjunto")

    val a = listOf(1, 2, 3, 4)
    val b = listOf(3, 4, 5)
    show("a", a)
    show("b", b)
    show("a intersect b  (los comunes)", a intersect b)
    show("a subtract b   (los de a que no están en b)", a subtract b)
    show("a union b      (todos, sin duplicados)", a union b)
    show("a - b          (operador, igual que subtract)", a - b)
    show("a + b          (concatena, CON duplicados)", a + b)

    bullet("`intersect`, `subtract` y `union` devuelven un Set, no una List.")
    bullet("Con listas grandes, convierte antes el segundo operando a Set: la")
    bullet("búsqueda pasa de O(n) a O(1) en cada comparación.")
}

private data class PersonaSimple(val nombre: String, val ciudad: String)
private class SinEquals(val valor: String)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Compara `takeWhile { it < 6 }` con `filter { it < 6 }` sobre [1,3,5,4,7,9].
//  2. Usa `partition` para separar palabras largas y cortas en una sola pasada.
//  3. Calcula la media móvil de 5 días con `windowed` y compara con la de 3.
//  4. Convierte SinEquals en `data class` y vuelve a ejecutar la demo de distinct.
