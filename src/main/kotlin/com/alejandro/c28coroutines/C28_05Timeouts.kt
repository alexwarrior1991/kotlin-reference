package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

// =====================================================================================
//  28.5 · withTimeout y withTimeoutOrNull
//
//  QUÉ ES
//    Poner un límite de tiempo a un bloque suspendido. Si se pasa, la corrutina se
//    cancela: `withTimeout` lanzando y `withTimeoutOrNull` devolviendo `null`.
//
//  POR QUÉ IMPORTA
//    Toda llamada a un sistema externo necesita un límite. Sin él, una respuesta que
//    nunca llega deja la corrutina esperando para siempre y, con ella, todo lo que
//    dependa de su ámbito.
//
//  ERRORES COMUNES
//    · No saber que `TimeoutCancellationException` ES una `CancellationException`,
//      y relanzarla por sistema sin poder tratarla.
//    · Poner el timeout dentro del bucle de reintentos en vez de fuera (o al revés,
//      sin pensarlo).
//    · Creer que el timeout interrumpe código que no coopera (no: ver demo 28.13).
// =====================================================================================

/**
 * `withTimeout`: lanza si se pasa.
 */
fun demoWithTimeout() {
    section("Cuando da tiempo")

    runBlocking {
        val resultado = withTimeout(300) {
            delay(50)
            "terminé a tiempo"
        }
        show("withTimeout(300) { delay(50) }", resultado)
    }

    section("Cuando no da tiempo")

    runBlocking {
        val resultado = try {
            withTimeout(100) {
                delay(500)
                "esto nunca se devuelve"
            }
        } catch (e: TimeoutCancellationException) {
            "lanzó ${e::class.simpleName}: ${e.message}"
        }
        show("withTimeout(100) { delay(500) }", resultado)
    }

    bullet("Al pasarse el tiempo, el bloque se CANCELA y sube la excepción.")

    section("El bloque se cancela de verdad")

    runBlocking {
        val traza = mutableListOf<String>()

        runCatching {
            withTimeout(60) {
                try {
                    traza.add("empiezo")
                    delay(500)
                    traza.add("esto no pasa")
                } finally {
                    traza.add("finally ejecutado al cancelar")
                }
            }
        }

        traza.forEach { bullet(it) }
    }

    bullet("El `finally` corre, igual que con cualquier cancelación (demo 28.15).")
}

/**
 * `withTimeoutOrNull`: null en lugar de excepción.
 */
fun demoWithTimeoutOrNull() {
    section("La versión sin excepciones")

    runBlocking {
        val aTiempo = withTimeoutOrNull(300) {
            delay(50)
            "llegué"
        }
        show("withTimeoutOrNull(300) { delay(50) }", aTiempo)

        val tarde = withTimeoutOrNull(100) {
            delay(500)
            "no llego"
        }
        show("withTimeoutOrNull(100) { delay(500) }", tarde)
    }

    bullet("Devuelve `null` al agotarse, que encaja perfectamente con un `?:`.")

    section("El patrón completo")

    runBlocking {
        val datos = withTimeoutOrNull(120) { cargarDesdeRed() } ?: "(datos en caché)"
        show("con valor por defecto", datos)

        val rapido = withTimeoutOrNull(400) { cargarDesdeRed() } ?: "(caché)"
        show("cuando sí da tiempo", rapido)
    }

    section("Cuál usar")

    bullet("`withTimeoutOrNull` → el timeout es un caso NORMAL: hay alternativa")
    bullet("                      (caché, valor por defecto, reintento).")
    bullet("`withTimeout`       → el timeout es un ERROR que debe propagarse.")
    bullet("En la práctica, `withTimeoutOrNull` se usa mucho más.")

    section("Un matiz que sorprende")

    // Si el bloque devuelve null legítimamente, no puedes distinguirlo del timeout.
    runBlocking {
        val devuelveNull = withTimeoutOrNull(200) {
            delay(20)
            null
        }
        show("el bloque devolvió null de verdad", devuelveNull)
        show("¿fue timeout o fue el valor?", "imposible saberlo")
    }

    bullet("Si tu bloque puede devolver null, envuélvelo (por ejemplo en un Result)")
    bullet("o usa `withTimeout` con try/catch para distinguir los dos casos.")
}

/**
 * La relación con la cancelación.
 */
fun demoTimeoutIsCancellation() {
    section("TimeoutCancellationException ES una CancellationException")

    runBlocking {
        val jerarquia = try {
            withTimeout(50) { delay(500) }
            "no lanzó"
        } catch (e: CancellationException) {
            "capturada como CancellationException: ${e::class.simpleName}"
        }
        show("catch (e: CancellationException)", jerarquia)
    }

    bullet("Eso tiene dos consecuencias importantes.")

    section("Consecuencia 1: el catch genérico la traga")

    runBlocking {
        val tragada = withTimeoutOrNull(200) {
            try {
                withTimeout(50) { delay(500) }
                "no lanzó"
            } catch (e: Exception) {
                "me tragué el timeout"      // ← compila y se ejecuta
            }
        }
        show("catch (e: Exception) alrededor de withTimeout", tragada)
    }

    bullet("El timeout se capturó y el código continuó como si nada.")
    bullet("A veces es lo que quieres; lo peligroso es que pase sin darte cuenta.")

    section("Consecuencia 2: la regla de 'relanzar siempre' choca aquí")

    bullet("La regla de la demo 28.16 dice: relanza siempre CancellationException.")
    bullet("Pero un timeout que quieres TRATAR es una CancellationException.")
    bullet("Solución: captura `TimeoutCancellationException` (el tipo concreto)")
    bullet("antes que `CancellationException`, o usa `withTimeoutOrNull`.")

    runBlocking {
        val bienHecho = try {
            withTimeout(50) { delay(500) }
            "no lanzó"
        } catch (e: TimeoutCancellationException) {
            "timeout tratado correctamente"     // el tipo concreto: se puede tratar
        } catch (e: CancellationException) {
            throw e                              // cualquier otra cancelación, arriba
        }
        show("capturando el tipo concreto", bienHecho)
    }

    section("El timeout no puede parar código que no coopera")

    runBlocking {
        val traza = mutableListOf<String>()
        val resultado = withTimeoutOrNull(50) {
            // Bucle de CPU sin puntos de suspensión: el timeout no tiene dónde actuar.
            val hasta = System.currentTimeMillis() + 150
            while (System.currentTimeMillis() < hasta) {
                // trabajo de CPU
            }
            traza.add("el bucle terminó ENTERO pese al timeout de 50 ms")
            "completado"
        }
        traza.forEach { bullet(it) }
        show("resultado", resultado)
    }

    bullet("El bloque tardó 150 ms con un timeout de 50, y aun así se completó.")
    bullet("`withTimeoutOrNull` devolvió null porque al SALIR ya se había pasado,")
    bullet("pero no pudo interrumpir nada. Es la misma lección de la demo 28.13.")
}

/**
 * Patrones con timeout.
 */
fun demoTimeoutPatterns() {
    section("1. Timeout por operación")

    runBlocking {
        val resultados = listOf(30L, 80L, 200L).map { tardanza ->
            withTimeoutOrNull(100) {
                delay(tardanza)
                "ok en $tardanza ms"
            } ?: "timeout ($tardanza ms)"
        }
        resultados.forEach { bullet(it) }
    }

    bullet("Cada operación tiene su propio límite; una lenta no afecta a las demás.")

    section("2. Timeout global para varias en paralelo")

    runBlocking {
        val resultado = withTimeoutOrNull(150) {
            coroutineScope {
                listOf(30L, 50L, 80L)
                    .map { async { delay(it); "ok-$it" } }
                    .awaitAll()
            }
        }
        show("todas dentro del límite global", resultado)

        val conUnaLenta = withTimeoutOrNull(150) {
            coroutineScope {
                listOf(30L, 50L, 500L)
                    .map { async { delay(it); "ok-$it" } }
                    .awaitAll()
            }
        }
        show("una se pasa → se cancela todo", conUnaLenta)
    }

    bullet("El timeout cancela el `coroutineScope` entero, con todos sus hijos.")

    section("3. Reintentos con timeout")

    runBlocking {
        show("conReintentos(exito al 3er intento)", conReintentos(exitoEnIntento = 3))
        show("conReintentos(nunca funciona)", conReintentos(exitoEnIntento = 99))
    }

    bullet("El timeout va DENTRO del bucle: cada intento tiene su propio límite.")
    bullet("Si lo pusieras fuera, el límite sería para todos los intentos juntos.")
    bullet("Las dos cosas son válidas: decídelo a propósito, no por accidente.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/** Simula una llamada a un servicio externo que tarda 200 ms. */
private suspend fun cargarDesdeRed(): String {
    delay(200)
    return "datos de la red"
}

/**
 * Reintenta una operación, con un timeout POR INTENTO.
 */
private suspend fun conReintentos(exitoEnIntento: Int): String {
    var intento = 0
    var espera = 20L

    while (intento < 3) {
        intento++
        val resultado = withTimeoutOrNull(60) {
            delay(if (intento >= exitoEnIntento) 10 else 500)
            "conseguido en el intento $intento"
        }
        if (resultado != null) return resultado

        delay(espera)                 // espera creciente entre intentos
        espera *= 2
    }
    return "fallaron los 3 intentos"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `withTimeoutOrNull` por `withTimeout` en `conReintentos` y añade el
//     try/catch que hace falta.
//  2. Saca el timeout fuera del bucle de reintentos y compara el comportamiento.
//  3. Pon `catch (e: Exception)` alrededor de un withTimeout de tu código y comprueba
//     si se te estaba tragando el timeout sin saberlo.
//  4. Añade `ensureActive()` al bucle de CPU de la última demo y verás que ya sí se corta.
