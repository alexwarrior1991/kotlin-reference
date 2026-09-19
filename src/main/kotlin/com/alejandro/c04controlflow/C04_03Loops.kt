package com.alejandro.c04controlflow

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
// `java.lang.*` viene importado por defecto, pero `java.util` no: hay que traerla.
import java.util.ConcurrentModificationException

// =====================================================================================
//  4.3 · Bucles: for, while, do-while y repeat
//
//  QUÉ ES
//    Las tres formas de repetir de Kotlin. El `for` es siempre "para cada elemento
//    de algo"; no existe el `for (int i = 0; i < n; i++)` de C y Java.
//
//  POR QUÉ IMPORTA
//    El 95% de los bucles que escribirás son `for (x in coleccion)`. Y una parte de
//    ésos ni siquiera deberían ser bucles: `map`, `filter` y `sum` dicen mejor qué
//    quieres (capítulo 13). Conviene saber cuándo bajar al bucle explícito.
//
//  ERRORES COMUNES
//    · Buscar el `for` clásico de tres partes. Se sustituye por un rango.
//    · Recorrer con índices cuando no se necesita el índice.
//    · Modificar una lista mientras se recorre (ConcurrentModificationException).
// =====================================================================================

/**
 * `for`: siempre sobre algo iterable.
 */
fun demoForLoop() {
    section("Sobre un rango")

    val acumulado = buildList {
        for (i in 1..5) add(i)
    }
    show("for (i in 1..5)", acumulado)

    val decreciente = buildList {
        for (i in 5 downTo 1) add(i)
    }
    show("for (i in 5 downTo 1)", decreciente)

    val conPaso = buildList {
        for (i in 0..10 step 5) add(i)
    }
    show("for (i in 0..10 step 5)", conPaso)

    section("Sobre una colección")

    val lenguajes = listOf("Kotlin", "Java", "Scala")
    val mayusculas = buildList {
        for (lenguaje in lenguajes) add(lenguaje.uppercase())
    }
    show("for (lenguaje in lenguajes)", mayusculas)

    // ...aunque para esto lo idiomático es no escribir el bucle:
    show("la forma idiomática", lenguajes.map { it.uppercase() })

    section("Sobre una cadena")

    val consonantes = buildList {
        for (c in "Kotlin") if (c.lowercaseChar() !in "aeiou") add(c)
    }
    show("for (c in \"Kotlin\")", consonantes)

    section("Cuando sí necesitas el índice")

    val conIndice = buildList {
        for ((indice, lenguaje) in lenguajes.withIndex()) add("$indice:$lenguaje")
    }
    show("withIndex() + destructuring", conIndice)

    val porIndices = buildList {
        for (i in lenguajes.indices) add("$i=${lenguajes[i]}")
    }
    show("lenguajes.indices", porIndices)

    bullet("`withIndex()` es preferible: te da índice y elemento sin indexar a mano.")

    section("Sobre un Map")

    val precios = mapOf("pan" to 1.2, "leche" to 0.9)
    val lineas = buildList {
        for ((producto, precio) in precios) add("$producto: $precio €")
    }
    show("for ((clave, valor) in mapa)", lineas)

    section("Por qué no existe el for clásico")

    bullet("Java:   for (int i = 0; i < 10; i += 2) { ... }")
    bullet("Kotlin: for (i in 0..<10 step 2) { ... }")
    bullet("La versión de Kotlin no puede equivocarse con el < o el <=, ni olvidar el i++.")
}

/**
 * `while` y `do-while`.
 */
fun demoWhileLoops() {
    section("while: comprueba antes")

    var restantes = 3
    val pasos = buildList {
        while (restantes > 0) {
            add("quedan $restantes")
            restantes--
        }
    }
    show("while (restantes > 0)", pasos)

    // Si la condición es falsa de entrada, el cuerpo no se ejecuta NINGUNA vez.
    var cero = 0
    var vueltas = 0
    while (cero > 0) {
        vueltas++
        cero--
    }
    show("while con condición falsa → vueltas", vueltas)

    section("do-while: comprueba después")

    // El cuerpo se ejecuta SIEMPRE al menos una vez.
    var contador = 0
    var vueltasDoWhile = 0
    do {
        vueltasDoWhile++
        contador--
    } while (contador > 0)
    show("do-while con condición falsa → vueltas", vueltasDoWhile)

    bullet("do-while se usa poco: casi siempre hay una forma más clara.")
    bullet("Su caso típico: leer entrada hasta que sea válida, o reintentar una operación.")

    section("Una variable declarada en el do es visible en el while")

    // Detalle propio de Kotlin: lo declarado dentro del bloque `do` se puede usar en
    // la condición del `while`. En Java esto no compila.
    var intentos = 0
    do {
        val siguiente = intentos + 1
        intentos = siguiente
    } while (intentos < 3)
    show("intentos tras el do-while", intentos)
}

/**
 * `repeat`: no es una palabra clave, es una función.
 */
fun demoRepeat() {
    section("repeat(n) { }")

    val salida = buildList {
        repeat(3) { indice ->
            add("vuelta $indice")
        }
    }
    show("repeat(3) { indice -> ... }", salida)

    // El parámetro se puede omitir si no se usa: se llama `it`.
    val simple = buildList {
        repeat(3) { add("·") }
    }
    show("repeat(3) { ... } sin usar el índice", simple.joinToString(""))

    bullet("`repeat` es una función inline normal de la biblioteca estándar (capítulo 25).")
    bullet("El índice empieza en 0, como en todos los sitios.")
}

/**
 * Cuándo un bucle no es la mejor herramienta.
 */
fun demoLoopVsCollectionOperations() {
    val numeros = listOf(4, 8, 15, 16, 23, 42)

    section("El mismo cálculo, tres formas")

    // 1) Bucle imperativo: hay que leerlo entero para saber qué hace.
    var sumaPares = 0
    for (n in numeros) {
        if (n % 2 == 0) {
            sumaPares += n
        }
    }
    show("con for + if + var", sumaPares)

    // 2) forEach: sigue siendo imperativo, sólo cambia la sintaxis.
    var sumaConForEach = 0
    numeros.forEach { if (it % 2 == 0) sumaConForEach += it }
    show("con forEach", sumaConForEach)

    // 3) Declarativo: el nombre de cada operación dice qué hace. Capítulo 13.
    show("con filter + sum", numeros.filter { it % 2 == 0 }.sum())
    show("aún más corto, con sumOf", numeros.sumOf { if (it % 2 == 0) it else 0 })

    section("Cuándo sí conviene el bucle explícito")

    bullet("Cuando hace falta salir antes de tiempo con `break` (aunque `first` suele valer).")
    bullet("Cuando el cuerpo es largo y con varias ramas.")
    bullet("Cuando trabajas con índices de verdad (algoritmos, matrices).")
    bullet("Cuando el rendimiento está medido y el bucle gana.")

    section("El error clásico: modificar mientras recorres")

    val mutable = mutableListOf(1, 2, 3, 4)
    val resultado = try {
        for (n in mutable) {
            if (n == 2) mutable.remove(n)
        }
        "no falló"
    } catch (e: ConcurrentModificationException) {
        "lanzó ConcurrentModificationException"
    }
    show("borrar dentro del for", resultado)

    // La forma correcta: crear una lista nueva, o usar removeAll/removeIf.
    val filtrada = listOf(1, 2, 3, 4).filter { it != 2 }
    show("la forma correcta: filter", filtrada)
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `for (i in 5 downTo 1)` por `for (i in 5..1)` y comprueba que no itera.
//  2. Convierte el bucle de sumaPares en una sola línea con `filter` y `sum`.
//  3. Haz que el `do-while` tenga una condición falsa desde el principio y cuenta las
//     vueltas: verás que igualmente entra una vez.
//  4. Sustituye `mutable.remove(n)` por `mutable.removeAll { it == 2 }` fuera del bucle.
