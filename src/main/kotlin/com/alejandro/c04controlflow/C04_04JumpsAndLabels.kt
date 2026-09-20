package com.alejandro.c04controlflow

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  4.4 · break, continue, etiquetas y try como expresión
//
//  QUÉ ES
//    Las formas de alterar el flujo: salir de un bucle, saltar una vuelta, hacerlo
//    sobre un bucle exterior con etiquetas, y salir de una lambda con `return@`.
//
//  POR QUÉ IMPORTA
//    Las etiquetas son la única forma limpia de romper un bucle anidado sin banderas
//    booleanas. Y entender `return@lambda` frente a `return` a secas evita el error
//    más común al empezar con `forEach`.
//
//  ERRORES COMUNES
//    · Escribir `return` dentro de un `forEach` creyendo que salta un elemento: sale
//      de la función entera.
//    · Intentar usar `break` dentro de un `forEach`. No se puede.
//    · Inventar variables `var encontrado = false` en lugar de usar una etiqueta.
// =====================================================================================

/**
 * `break` y `continue` en bucles normales.
 */
fun demoBreakAndContinue() {
    section("break: salir del bucle")

    val hastaElPrimerNegativo = buildList {
        for (n in listOf(3, 7, 2, -1, 9, 4)) {
            if (n < 0) break
            add(n)
        }
    }
    show("break al primer negativo", hastaElPrimerNegativo)

    section("continue: saltar a la siguiente vuelta")

    val sinNegativos = buildList {
        for (n in listOf(3, 7, 2, -1, 9, 4)) {
            if (n < 0) continue
            add(n)
        }
    }
    show("continue con los negativos", sinNegativos)

    section("También en while")

    var i = 0
    val impares = buildList {
        while (true) {
            i++
            if (i > 10) break
            if (i % 2 == 0) continue
            add(i)
        }
    }
    show("while (true) + break + continue", impares)

    bullet("`while (true)` con `break` dentro es legítimo cuando la salida no es una")
    bullet("condición simple del principio, pero revisa siempre si hay algo más claro.")
}

/**
 * Etiquetas: romper un bucle exterior desde dentro de uno interior.
 */
fun demoLabels() {
    section("El problema")

    // Sin etiquetas, el `break` sólo rompe el bucle MÁS INTERNO, así que el exterior
    // sigue girando. La solución tradicional era una bandera booleana.
    val matriz = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
    )

    var encontradoSinEtiqueta: Pair<Int, Int>? = null
    var terminado = false                 // ← la bandera que queremos evitar
    for (fila in matriz.indices) {
        if (terminado) break
        for (columna in matriz[fila].indices) {
            if (matriz[fila][columna] == 5) {
                encontradoSinEtiqueta = fila to columna
                terminado = true
                break
            }
        }
    }
    show("con bandera booleana", encontradoSinEtiqueta)

    section("La solución: una etiqueta")

    // Una etiqueta es un identificador seguido de @ delante del bucle.
    var encontrado: Pair<Int, Int>? = null
    busqueda@ for (fila in matriz.indices) {
        for (columna in matriz[fila].indices) {
            if (matriz[fila][columna] == 5) {
                encontrado = fila to columna
                break@busqueda          // rompe el bucle EXTERIOR
            }
        }
    }
    show("con break@busqueda", encontrado)

    section("continue con etiqueta")

    // Saltar a la siguiente vuelta del bucle exterior en cuanto sabemos que este
    // grupo no nos sirve.
    val grupos = listOf(
        listOf(2, 4, 6),
        listOf(1, 2, 3),
        listOf(8, 10),
    )

    val soloPares = buildList {
        grupos@ for (grupo in grupos) {
            for (n in grupo) {
                if (n % 2 != 0) continue@grupos   // este grupo no vale, al siguiente
            }
            add(grupo)
        }
    }
    show("grupos en los que todo es par", soloPares)

    bullet("La etiqueta se pone justo delante del `for`, del `while` o de la lambda.")
    bullet("Nombra la etiqueta por lo que hace el bucle (`busqueda@`), no `loop1@`.")
}

/**
 * `return` dentro de una lambda: el error más frecuente al empezar.
 */
fun demoReturnInLambdas() {
    section("El problema")

    // `forEach` recibe una lambda. Dentro de una lambda, `return` a secas NO salta
    // al siguiente elemento: sale de la función que la rodea.
    show("primerNegativoMal(listOf(3, -1, 7))", primerNegativoMal(listOf(3, -1, 7)))
    bullet("El `return` de dentro del forEach salió de la FUNCIÓN, no de la vuelta.")

    section("La solución: return con etiqueta")

    // `return@forEach` es el equivalente a `continue`: termina ESTA vuelta.
    val sinNegativos = buildList {
        listOf(3, -1, 7, -2, 4).forEach {
            if (it < 0) return@forEach     // ← equivale a `continue`
            add(it)
        }
    }
    show("return@forEach ≈ continue", sinNegativos)

    section("¿Y el equivalente a break?")

    // No lo hay: una lambda no puede romper el bucle de quien la llama. Para eso
    // están las funciones que ya paran solas.
    val numeros = listOf(3, 7, 2, -1, 9)
    show("first { }   (para al encontrarlo)", numeros.first { it < 0 })
    show("takeWhile { }  (para al fallar)", numeros.takeWhile { it > 0 })
    show("indexOfFirst { }", numeros.indexOfFirst { it < 0 })
    show("any { }", numeros.any { it < 0 })

    bullet("Si necesitas `break` dentro de un forEach, la respuesta suele ser")
    bullet("`first`, `firstOrNull`, `takeWhile`, `any` o un `for` de toda la vida.")

    section("Etiquetas implícitas y explícitas")

    // La etiqueta por defecto es el nombre de la función: @forEach, @map, @let...
    val conImplicita = listOf(1, 2, 3).map {
        if (it == 2) return@map 0
        it * 10
    }
    show("return@map (etiqueta implícita)", conImplicita)

    // También se puede poner una propia, útil con lambdas anidadas.
    val conExplicita = listOf(1, 2, 3).map porDiez@{
        if (it == 2) return@porDiez 0
        it * 10
    }
    show("return@porDiez (etiqueta propia)", conExplicita)

    section("El valor de return@ es el valor de esa vuelta")

    bullet("En `map`, `return@map 0` aporta un 0 al resultado.")
    bullet("En `forEach`, como no se usa el valor, equivale a saltar el elemento.")
}

/**
 * `try` es una expresión.
 */
fun demoTryAsExpression() {
    section("Asignar el resultado de un try")

    val valido = try {
        "42".toInt()
    } catch (e: NumberFormatException) {
        0
    }
    show("try { \"42\".toInt() } catch { 0 }", valido)

    val invalido = try {
        "cuarenta y dos".toInt()
    } catch (e: NumberFormatException) {
        0
    }
    show("try { \"cuarenta y dos\".toInt() } catch { 0 }", invalido)

    section("El valor sale del try o del catch")

    bullet("Si el try termina bien, vale su última expresión.")
    bullet("Si salta, vale la última expresión del catch que lo atrape.")

    section("finally NO aporta el valor")

    val conFinally = try {
        "10".toInt()
    } catch (e: NumberFormatException) {
        -1
    } finally {
        // Lo que se escriba aquí se ejecuta siempre, pero NO cambia el resultado.
        999
    }
    show("con finally { 999 }", conFinally)
    bullet("`finally` sirve para limpiar (cerrar ficheros, liberar recursos), no para devolver.")

    section("En la práctica, casi siempre hay algo mejor")

    // `toIntOrNull` evita la excepción del todo. Capítulo 19.
    show("\"42\".toIntOrNull() ?: 0", "42".toIntOrNull() ?: 0)
    show("\"abc\".toIntOrNull() ?: 0", "abc".toIntOrNull() ?: 0)
    bullet("Usar excepciones para el flujo normal es caro y se lee peor.")
    bullet("Busca siempre la variante `...OrNull` antes de montar un try.")
}

/**
 * Demostración del `return` no local: sale de ESTA función, no de la lambda.
 *
 * Funciona porque `forEach` es `inline` (capítulo 5): su lambda se copia dentro de
 * esta función al compilar, así que el `return` es un return normal.
 */
private fun primerNegativoMal(numeros: List<Int>): String {
    numeros.forEach {
        if (it < 0) return "encontré el $it y salí de la función"
    }
    return "no había negativos"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `@busqueda` del break y comprueba que el bucle exterior sigue girando.
//  2. En primerNegativoMal, cambia `return` por `return@forEach` y observa qué devuelve.
//  3. Intenta poner `break` dentro de un `forEach`: verás que no compila.
//  4. Añade un `finally` que devuelva otro valor y confirma que se ignora.
