package com.alejandro.c19exceptions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  19.3 · Result y runCatching
//
//  QUÉ ES
//    `kotlin.Result<T>` envuelve "salió bien con este valor" o "lanzó esta excepción".
//    `runCatching { }` ejecuta un bloque y devuelve un Result en lugar de propagar.
//
//  POR QUÉ IMPORTA
//    Convierte una excepción en un VALOR que se puede pasar, transformar y encadenar.
//    Es el puente entre el mundo de las excepciones (APIs de Java, entrada/salida) y
//    el estilo funcional del capítulo 11.
//
//  ERRORES COMUNES
//    · `runCatching` captura TODO, incluida CancellationException: en corrutinas eso
//      rompe la cancelación (capítulo 28). Es la trampa más importante.
//    · Usar Result como modelo de dominio: el error siempre es un Throwable.
//    · Llamar a `getOrThrow()` justo después de `runCatching`, que deshace todo.
// =====================================================================================

/**
 * Lo básico.
 */
fun demoBasics() {
    section("runCatching convierte una excepción en un valor")

    val exito = runCatching { "42".toInt() }
    val fallo = runCatching { "abc".toInt() }

    show("runCatching { \"42\".toInt() }", exito)
    show("runCatching { \"abc\".toInt() }", fallo)

    show("exito.isSuccess", exito.isSuccess)
    show("fallo.isFailure", fallo.isFailure)

    section("Sacar el valor")

    show("getOrNull()", exito.getOrNull())
    show("getOrNull() en el fallo", fallo.getOrNull())
    show("getOrDefault(-1)", fallo.getOrDefault(-1))
    show("getOrElse { ... }", fallo.getOrElse { -1 })
    show("getOrElse usando la excepción", fallo.getOrElse { if (it is NumberFormatException) -2 else -3 })

    section("Sacar la excepción")

    show("exceptionOrNull()", fallo.exceptionOrNull()?.let { it::class.simpleName })
    show("exceptionOrNull() en el éxito", exito.exceptionOrNull())

    section("getOrThrow: volver al mundo de las excepciones")

    val relanzada = try {
        fallo.getOrThrow()
        "no lanzó"
    } catch (e: NumberFormatException) {
        "lanzó NumberFormatException"
    }
    show("fallo.getOrThrow()", relanzada)

    bullet("Si vas a llamar a `getOrThrow()` inmediatamente, no envuelvas: el")
    bullet("`runCatching` sobra y sólo añade un objeto.")

    section("Con receptor: runCatching sobre un objeto")

    // Hay una variante de extensión: el objeto está disponible como `this`.
    show("\"42\".runCatching { toInt() }", "42".runCatching { toInt() }.getOrNull())
    show("\"abc\".runCatching { toInt() }", "abc".runCatching { toInt() }.getOrNull())
}

/**
 * Transformar y encadenar.
 */
fun demoTransforming() {
    section("map: transformar el valor si hubo éxito")

    show("éxito .map { it * 2 }", runCatching { "21".toInt() }.map { it * 2 })
    show("fallo .map { it * 2 }", runCatching { "x".toInt() }.map { it * 2 }.exceptionOrNull()?.let { it::class.simpleName })

    bullet("Si era un fallo, `map` no ejecuta la lambda: el error se propaga.")

    section("mapCatching: cuando la transformación TAMBIÉN puede lanzar")

    show("map con lambda que lanza", runCatching { "10" }.mapCatching { it.toInt() / 0 }.exceptionOrNull()?.let { it::class.simpleName })
    bullet("`map` dejaría escapar la excepción de la lambda; `mapCatching` la captura.")

    section("recover: convertir un fallo en un valor")

    show("fallo.recover { 0 }", runCatching { "x".toInt() }.recover { 0 })
    show("éxito.recover { 0 }  (no hace nada)", runCatching { "7".toInt() }.recover { 0 })
    show(
        "recover según el tipo de error",
        runCatching { "x".toInt() }.recover { if (it is NumberFormatException) -1 else -2 },
    )

    section("fold: un valor de cada rama")

    listOf("42", "abc").forEach { entrada ->
        val salida = runCatching { entrada.toInt() }.fold(
            onSuccess = { "número $it" },
            onFailure = { "no era un número: ${it::class.simpleName}" },
        )
        show("fold('$entrada')", salida)
    }

    bullet("`fold` es la forma idiomática de SALIR de un Result, tratando los dos casos.")

    section("onSuccess / onFailure: efectos secundarios sin romper la cadena")

    val traza = mutableListOf<String>()
    val resultado = runCatching { "42".toInt() }
        .onSuccess { traza.add("ok: $it") }
        .onFailure { traza.add("ko: ${it.message}") }
        .map { it + 1 }

    show("resultado", resultado.getOrNull())
    show("traza", traza)

    bullet("Son el `also` del mundo de Result: devuelven el mismo Result.")

    section("Encadenar varias operaciones que pueden fallar")

    show("cadena con entrada válida", procesarEntrada("21"))
    show("cadena con entrada no numérica", procesarEntrada("abc"))
    show("cadena con entrada fuera de rango", procesarEntrada("500"))
}

/**
 * La trampa de CancellationException.
 */
fun demoCancellationTrap() {
    section("El problema")

    bullet("`runCatching` captura `Throwable`. TODO. Sin excepciones.")
    bullet("En corrutinas, la cancelación se implementa lanzando CancellationException.")
    bullet("Si tu `runCatching` la captura, la corrutina NO se entera de que la han")
    bullet("cancelado y sigue trabajando. Capítulo 28.4.")

    section("Qué se ve en la práctica")

    bullet("launch { runCatching { operacionLarga() } }  ← la cancelación no funciona")
    bullet("Y lo peor: parece que todo va bien. No hay error, sólo trabajo de más.")

    section("Las soluciones")

    bullet("1. No uses `runCatching` dentro de corrutinas: usa try/catch del tipo")
    bullet("   concreto que esperas.")
    bullet("2. Si lo usas, re-lanza la cancelación explícitamente.")

    // La forma correcta de escribirlo, ilustrada sin corrutinas de por medio.
    show("runCatching que respeta la cancelación", ejemploQueRelanza())

    bullet("En el capítulo 28 se repite este aviso, porque es donde muerde.")

    section("El mismo problema con catch (e: Exception)")

    bullet("`catch (e: Exception)` también atrapa CancellationException.")
    bullet("La regla es la misma: captura el tipo específico que sabes tratar.")
}

/**
 * Cuándo usar Result y cuándo no.
 */
fun demoWhenToUse() {
    section("SÍ: envolver código que lanza")

    bullet("Una API de Java, entrada/salida, parseo: lo que ya lanza excepciones.")
    bullet("`runCatching { Files.readString(ruta) }` es un uso perfecto.")

    section("SÍ: cuando no quieres propagar y sí quieres seguir")

    bullet("Procesar 100 ficheros y quedarte con los que se pudieron leer.")

    val entradas = listOf("1", "dos", "3", "cuatro", "5")
    val resultados = entradas.map { runCatching { it.toInt() } }
    val (correctos, fallidos) = resultados.partition { it.isSuccess }

    show("correctos", correctos.mapNotNull { it.getOrNull() })
    show("fallidos", fallidos.size)
    show("en una línea", entradas.mapNotNull { it.toIntOrNull() })

    bullet("Fíjate en la última línea: si existe una variante `...OrNull`, úsala.")
    bullet("Es más corta, más rápida y no construye excepciones.")

    section("NO: como modelo de dominio")

    bullet("El error de un Result es SIEMPRE un Throwable.")
    bullet("Eso te obliga a crear excepciones para casos que no son excepcionales,")
    bullet("y a distinguirlos con `is`, perdiendo la exhaustividad del `when`.")
    bullet("Para el dominio, una sealed propia (capítulo 11.4) es mejor.")

    section("NO: cuando existe una variante ...OrNull")

    bullet("toIntOrNull, firstOrNull, getOrNull, maxOrNull, singleOrNull...")
    bullet("Construir una excepción cuesta: rellenar la traza de pila no es gratis.")
    bullet("Si el fallo es esperable y frecuente, evita la excepción del todo.")

    section("La tabla de decisión")

    bullet("¿Error ESPERADO con un solo motivo?        → tipo nulable (`T?`)")
    bullet("¿Error ESPERADO con varios motivos?        → sealed propia")
    bullet("¿Envolver código ajeno que lanza?          → runCatching / Result")
    bullet("¿Error de programación (bug, invariante)?  → excepción, y que suba")
}

// -- Las funciones que usan las demos -------------------------------------------------------

/** Una cadena de operaciones que pueden fallar, resuelta con Result. */
private fun procesarEntrada(entrada: String): String =
    runCatching { entrada.toInt() }
        .mapCatching { numero ->
            require(numero in 0..100) { "$numero está fuera del rango 0..100" }
            numero
        }
        .map { it * 2 }
        .fold(
            onSuccess = { "resultado: $it" },
            onFailure = { "falló: ${it.message}" },
        )

/**
 * El patrón correcto cuando se usa `runCatching` en código que puede cancelarse.
 *
 * Aquí no hay corrutinas (llegan en el capítulo 28), así que simulamos la idea con
 * una excepción propia que representa "no me captures".
 */
private class NoMeCaptures : Exception("esto debe llegar arriba")

private fun ejemploQueRelanza(): String {
    val resultado = runCatching {
        throw NoMeCaptures()
    }.also { r ->
        // Relanzar explícitamente lo que NO debe capturarse.
        val error = r.exceptionOrNull()
        if (error is NoMeCaptures) {
            // En corrutinas, aquí iría: if (error is CancellationException) throw error
            return "relanzada correctamente: ${error.message}"
        }
    }
    return "capturada: ${resultado.exceptionOrNull()?.message}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `mapCatching` por `map` en `procesarEntrada` y observa que la excepción
//     del `require` se escapa.
//  2. Reescribe `procesarEntrada` con la sealed `Resultado` del capítulo 11 y compara.
//  3. Mide cuánto tarda crear 100.000 excepciones frente a 100.000 `toIntOrNull()`.
//  4. Escribe `fun <T> Result<T>.onAny(bloque: () -> Unit): Result<T>`.
