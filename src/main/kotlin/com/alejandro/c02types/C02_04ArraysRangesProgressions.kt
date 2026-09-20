package com.alejandro.c02types

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  2.4 · Arrays, rangos y progresiones
//
//  QUÉ ES
//    `Array` (tamaño fijo, respaldado por un array de la JVM), los rangos (`1..10`) y
//    las progresiones, que son rangos con un paso y una dirección.
//
//  POR QUÉ IMPORTA
//    Los rangos aparecen en todos los `for` y en todos los `when` con intervalos. Y
//    los arrays hay que conocerlos aunque casi nunca se usen: la respuesta correcta
//    en Kotlin suele ser `List`.
//
//  ERRORES COMUNES
//    · Imprimir un array con `toString()` y obtener `[I@5ca881b5`.
//    · Comparar arrays con `==` (compara referencias) en vez de `contentEquals`.
//    · Usar `Array<Int>` cuando quieres `IntArray` (el primero envuelve cada número).
//    · Olvidar que `1..10` INCLUYE el 10; para excluirlo, `1..<10`.
// =====================================================================================

/**
 * Crear arrays, y por qué normalmente no quieres uno.
 */
fun demoArrays() {
    section("Formas de crear un array")

    val letras = arrayOf("a", "b", "c")
    val ceros = IntArray(5)                    // cinco ceros
    val cuadrados = IntArray(5) { it * it }    // el lambda recibe el índice
    // El tipo hay que escribirlo: `arrayOf` es una función `reified` (capítulo 12) y
    // el compilador no puede reificar el tipo que deduciría solo aquí
    // (Comparable<*> & Serializable). Error: TYPE_INTERSECTION_AS_REIFIED.
    val mixto = arrayOf<Any>(1, "dos", 3.0)
    val nulos = arrayOfNulls<String>(3)

    show("arrayOf(\"a\", \"b\", \"c\")", letras)
    show("IntArray(5)", ceros)
    show("IntArray(5) { it * it }", cuadrados)
    show("arrayOf(1, \"dos\", 3.0)", mixto)
    show("arrayOfNulls<String>(3)", nulos)

    section("Imprimir un array")

    // Ésta es la trampa clásica: toString() de un array no muestra su contenido,
    // sino el tipo y la dirección de memoria (por eso cambia en cada ejecución).
    val crudo = intArrayOf(1, 2, 3)
    show("crudo.toString()", crudo.toString())
    show("crudo.contentToString()", crudo.contentToString())
    show("crudo.joinToString()", crudo.joinToString())

    bullet("Por eso el helper `show` de este repo detecta los arrays y los formatea.")

    section("Comparar arrays")

    val uno = intArrayOf(1, 2, 3)
    val otro = intArrayOf(1, 2, 3)
    show("uno === otro             (¿el mismo objeto?)", uno === otro)
    show("uno.contentEquals(otro)  (¿mismo contenido?)", uno.contentEquals(otro))

    // `==` entre arrays llama a equals(), y los arrays heredan el equals de Any, que
    // es identidad. Es decir: `uno == otro` también daría false. Nunca es lo que
    // quieres, así que para arrays se usa contentEquals().
    bullet("`==` entre arrays NO compara contenido: usa contentEquals().")
    bullet("Para arrays anidados, contentDeepEquals().")

    bullet("Con List esto no pasa: listOf(1,2,3) == listOf(1,2,3) es true y punto.")

    section("Acceso y recorrido")

    val notas = intArrayOf(7, 9, 4, 10)
    show("notas[0]", notas[0])
    show("notas.size", notas.size)
    show("notas.indices", notas.indices)
    show("notas.lastIndex", notas.lastIndex)

    notas[2] = 6   // el TAMAÑO es fijo, pero los elementos se pueden cambiar
    show("tras notas[2] = 6", notas)

    section("IntArray vs Array<Int>")

    val primitivo: IntArray = intArrayOf(1, 2, 3)         // int[] en la JVM
    val envuelto: Array<Int> = arrayOf(1, 2, 3)           // Integer[] en la JVM
    show("IntArray   → int[]", primitivo)
    show("Array<Int> → Integer[]", envuelto)

    bullet("IntArray no envuelve cada número: menos memoria y sin conversiones.")
    bullet("Hay una versión por tipo: IntArray, LongArray, DoubleArray, CharArray...")

    section("¿Cuándo usar un Array?")

    bullet("Interoperar con una API de Java que pide un array.")
    bullet("`vararg` (que por dentro es un array). Capítulo 5.")
    bullet("Rendimiento muy medido con muchos números primitivos.")
    bullet("En cualquier otro caso: List o MutableList. Capítulo 13.")
}

/**
 * Rangos: `..` y `..<`.
 */
fun demoRanges() {
    section("Rango cerrado y rango abierto por la derecha")

    show("1..5      (incluye el 5)", (1..5).toList())
    show("1..<5     (excluye el 5)", (1..<5).toList())
    show("1 until 5 (lo mismo, forma antigua)", (1 until 5).toList())

    bullet("`..<` es la forma recomendada desde Kotlin 1.9; `until` sigue funcionando.")
    bullet("Truco para recordarlo: el símbolo `<` ya dice 'menor que', no 'menor o igual'.")

    section("Rangos vacíos")

    // Si el principio es mayor que el final, el rango está vacío. No es un error.
    show("(5..1).toList()", (5..1).toList())
    show("(5..1).isEmpty()", (5..1).isEmpty())

    section("Pertenencia con `in`")

    val edad = 34
    show("edad in 18..65", edad in 18..65)
    show("edad !in 0..17", edad !in 0..17)

    // Esto se lee mucho mejor que `edad >= 18 && edad <= 65` y es lo idiomático
    // en las condiciones de un `when` (capítulo 4).
    bullet("`in` funciona con rangos, colecciones y cadenas. Capítulo 3.")

    section("Rangos de otros tipos")

    show("'a'..'e'", ('a'..'e').toList())
    show("'c' in 'a'..'e'", 'c' in 'a'..'e')
    show("1L..3L  (Long)", (1L..3L).toList())

    // Los rangos de Double existen, pero NO se pueden recorrer con `for`:
    // ¿cuál sería el siguiente valor después de 1.0? Sólo sirven para `in`.
    val rangoDecimal = 1.0..2.0
    show("1.5 in 1.0..2.0", 1.5 in rangoDecimal)
    bullet("Un rango de Double sirve para comprobar pertenencia, pero no es iterable.")
}

/**
 * Progresiones: un rango con paso y dirección.
 */
fun demoProgressions() {
    section("step: cambiar el paso")

    show("1..10 step 2", (1..10 step 2).toList())
    show("1..10 step 4", (1..10 step 4).toList())

    // El último valor de una progresión es el último ALCANZABLE, que no tiene por qué
    // coincidir con el final del rango: desde 1 y de 4 en 4 se llega a 9, no a 10.
    show("(1..10 step 4).last", (1..10 step 4).last)
    show("(1..10 step 3).last  (aquí sí cae justo)", (1..10 step 3).last)
    bullet("`last` es el último valor alcanzable, no el extremo que escribiste.")

    section("downTo: hacia atrás")

    // Ojo: `10..1` está vacío. Para ir hacia atrás hace falta downTo.
    show("10 downTo 1", (10 downTo 1).toList())
    show("10 downTo 1 step 3", (10 downTo 1 step 3).toList())
    show("(1..10).reversed()", (1..10).reversed().toList())

    bullet("`step` siempre es positivo: la dirección la marca `..` o `downTo`.")

    section("Propiedades de una progresión")

    val progresion = 1..20 step 4
    show("first", progresion.first)
    show("last", progresion.last)
    show("step", progresion.step)
    show("count()", progresion.count())
    show("sum()", progresion.sum())

    section("En un for")

    // El caso de uso real: recorrer índices.
    val palabras = listOf("uno", "dos", "tres", "cuatro")

    val pares = buildList {
        for (i in palabras.indices step 2) add(palabras[i])
    }
    show("uno de cada dos con `indices step 2`", pares)

    val alReves = buildList {
        for (i in palabras.lastIndex downTo 0) add(palabras[i])
    }
    show("al revés con `lastIndex downTo 0`", alReves)

    // Aunque, para esto, lo idiomático es no usar índices en absoluto:
    show("la forma idiomática", palabras.reversed())
    bullet("Los índices son para cuando de verdad necesitas la posición.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `1..5` por `1..<5` en la primera demo y cuenta los elementos.
//  2. Escribe `for (x in 10..1)` y comprueba que no imprime nada; arréglalo con downTo.
//  3. Sustituye `contentEquals` por `==` entre dos arrays y mira qué devuelve.
//  4. Prueba `(1..10 step 4).last` y comprueba por qué no es 10.
