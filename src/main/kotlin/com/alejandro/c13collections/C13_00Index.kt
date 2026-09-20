package com.alejandro.c13collections

import com.alejandro.infra.chapter

/**
 * # Capítulo 13 · Colecciones
 *
 * El capítulo al que más se vuelve. La biblioteca de colecciones de Kotlin sustituye
 * a la mayoría de los bucles que escribirías, y lo hace con nombres que dicen qué
 * ocurre: al leer `map` ya sabes que sale una colección del mismo tamaño.
 *
 * ## Qué se cubre
 * - Crear List, Set y Map; los builders `buildList`/`buildSet`/`buildMap`.
 * - Transformar: `map`, `mapNotNull`, `flatMap`, `flatten`, `zip`, `zipWithNext`.
 * - Filtrar y trocear: `filter`, `partition`, `take`/`drop`, `chunked`, `windowed`,
 *   `distinct`.
 * - Agregar: `sum`, `average`, `reduce`, `fold`, `runningFold`, `joinToString`.
 * - Agrupar: `groupBy`, `associateBy`, `groupingBy`.
 * - Ordenar y buscar: `sortedWith`, `compareBy`/`thenBy`, `find`, `first`, `single`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Sólo lectura ≠ inmutable**: una `List` puede ser la misma colección que un
 *   `MutableList` de otro sitio, y ver sus cambios. Copia al salir de la clase.
 * - **`associateBy` descarta duplicados sin avisar**. Si la clave puede repetirse,
 *   `groupBy`.
 * - `maxOf` da el VALOR; `maxByOrNull` da el ELEMENTO. Es la confusión más frecuente.
 * - Participio (`sorted`, `reversed`) → copia. Imperativo (`sort`, `reverse`) → muta.
 * - `first`/`max` lanzan si no hay nada; `firstOrNull`/`maxOrNull` devuelven `null`.
 * - `takeWhile` **se para** en el primero que falla; `filter` recorre todo.
 * - Filtra antes de transformar: mismo resultado, mucho menos trabajo.
 *
 * ## Errores típicos
 * - Encadenar diez operaciones sobre una lista enorme creando nueve intermedias
 *   (para eso está el capítulo 14).
 * - Usar `map` cuando el resultado se descarta: eso era `forEach`.
 * - Devolver `MutableList` desde una clase y perder el control del propio estado.
 *
 * Siguiente paso: capítulo 14, secuencias.
 */
val chapter13 = chapter(
    number = 13,
    name = "Colecciones",
    summary = "List/Set/Map, map, filter, fold, groupBy, orden y búsqueda",
) {
    demo("List, Set y Map: cuál elegir", ::demoCollectionTypes)
    demo("Formas de crear una colección", ::demoCreation)
    demo("Sólo lectura NO es inmutable", ::demoReadOnlyIsNotImmutable)
    demo("Convertir entre tipos", ::demoConversions)
    demo("map, mapIndexed y mapNotNull", ::demoMap)
    demo("flatMap y flatten", ::demoFlatMap)
    demo("zip, unzip y zipWithNext", ::demoZip)
    demo("onEach frente a forEach", ::demoOnEachVsForEach)
    demo("filter y su familia", ::demoFilter)
    demo("partition: filtrar en dos de una pasada", ::demoPartition)
    demo("take y drop", ::demoTakeAndDrop)
    demo("chunked y windowed", ::demoChunkedAndWindowed)
    demo("distinct y operaciones de conjunto", ::demoDistinct)
    demo("Agregaciones ya hechas", ::demoBuiltInAggregations)
    demo("reduce y sus dos limitaciones", ::demoReduce)
    demo("fold: la operación más general", ::demoFold)
    demo("joinToString", ::demoJoinToString)
    demo("groupBy: una clave, varios valores", ::demoGroupBy)
    demo("associateBy y la trampa de los duplicados", ::demoAssociateBy)
    demo("groupingBy: agregar sin listas intermedias", ::demoGroupingBy)
    demo("Trabajar con el Map resultante", ::demoWorkingWithMaps)
    demo("Ordenar", ::demoSorting)
    demo("Comparadores compuestos", ::demoComparators)
    demo("Buscar elementos", ::demoSearching)
    demo("Buenas prácticas con colecciones", ::demoBestPractices)
}

/** Ejecuta el capítulo 13 completo. */
fun main() = chapter13.runAll()
