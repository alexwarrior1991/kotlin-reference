package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

// =====================================================================================
//  28.4 · Cancelación
//
//  QUÉ ES
//    Pedirle a una corrutina que pare. La palabra clave es COOPERATIVA: nadie mata
//    la corrutina por la fuerza; se le avisa y ella tiene que darse por enterada.
//
//  POR QUÉ IMPORTA
//    Una corrutina que no coopera sigue corriendo aunque la hayas cancelado, y no hay
//    forma de pararla. Es el equivalente a `Thread.stop()`, que se eliminó de Java
//    precisamente por eso.
//
//  ERRORES COMUNES
//    · Un bucle de cálculo sin puntos de suspensión: ignora la cancelación entera.
//    · `try { ... } catch (e: Exception)` alrededor de código cancelable: se traga
//      la CancellationException y rompe la cancelación.
//    · Intentar suspender dentro de un `finally` después de cancelar.
// =====================================================================================

/**
 * Cancelar es cooperativo.
 */
fun demoCooperativeCancellation() {
    section("Una corrutina que SÍ coopera")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            repeat(10) { i ->
                traza.add("vuelta $i")
                delay(20)      // `delay` es un punto de suspensión: comprueba la cancelación
            }
            traza.add("terminado del todo")
        }

        delay(70)
        trabajo.cancelAndJoin()

        show("vueltas completadas antes de cancelar", traza.size)
        show("¿llegó al final?", "terminado del todo" in traza)
        show("¿el job está cancelado?", trabajo.isCancelled)
    }

    bullet("`delay` lanza CancellationException si la corrutina ha sido cancelada.")
    bullet("Todas las funciones de kotlinx.coroutines hacen esa comprobación.")

    section("Una corrutina que NO coopera")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch(kotlinx.coroutines.Dispatchers.Default) {
            var i = 0
            // Un bucle de cálculo puro, sin puntos de suspensión: la cancelación
            // no tiene dónde actuar.
            while (i < 5) {
                val hasta = System.currentTimeMillis() + 20
                while (System.currentTimeMillis() < hasta) {
                    // trabajo de CPU simulado
                }
                traza.add("vuelta $i")
                i++
            }
        }

        delay(40)
        trabajo.cancel()
        trabajo.join()

        show("vueltas completadas pese a cancelar", traza.size)
        show("¿el job figura como cancelado?", trabajo.isCancelled)
    }

    bullet("Se canceló el Job, pero el bucle siguió hasta el final: 5 vueltas.")
    bullet("La cancelación es una PETICIÓN, no una orden. Hay que atenderla.")
}

/**
 * Cómo hacer que un bucle coopere.
 */
fun demoMakingItCooperative() {
    section("Opción 1: isActive")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch(kotlinx.coroutines.Dispatchers.Default) {
            var i = 0
            while (isActive && i < 5) {          // ← la comprobación
                val hasta = System.currentTimeMillis() + 20
                while (System.currentTimeMillis() < hasta) {
                    // trabajo de CPU
                }
                traza.add("vuelta $i")
                i++
            }
            traza.add("salí del bucle limpiamente")
        }

        delay(40)
        trabajo.cancelAndJoin()

        show("vueltas completadas", traza.size - 1)
        show("¿salió limpiamente?", "salí del bucle limpiamente" in traza)
    }

    bullet("`isActive` devuelve false en cuanto se cancela. El bucle sale SOLO,")
    bullet("sin excepción: útil cuando quieres terminar ordenadamente.")

    section("Opción 2: ensureActive()")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch(kotlinx.coroutines.Dispatchers.Default) {
            try {
                var i = 0
                while (i < 5) {
                    ensureActive()               // ← lanza si está cancelada
                    val hasta = System.currentTimeMillis() + 20
                    while (System.currentTimeMillis() < hasta) {
                        // trabajo de CPU
                    }
                    traza.add("vuelta $i")
                    i++
                }
            } catch (e: CancellationException) {
                traza.add("salí por CancellationException")
                throw e                          // ← IMPORTANTE: hay que relanzarla
            }
        }

        delay(40)
        trabajo.cancelAndJoin()

        show("vueltas completadas", traza.count { it.startsWith("vuelta") })
        show("¿salió por excepción?", "salí por CancellationException" in traza)
    }

    bullet("`ensureActive()` LANZA CancellationException. Úsalo cuando quieras que")
    bullet("la cancelación interrumpa de verdad, como hace `delay`.")

    section("Opción 3: yield()")

    bullet("`yield()` comprueba la cancelación Y además cede el hilo a otras")
    bullet("corrutinas que estén esperando. En un bucle largo de CPU es lo más justo.")

    runBlocking {
        var vueltas = 0
        val trabajo = launch {
            repeat(1_000) {
                vueltas++
                yield()
            }
        }
        delay(5)
        trabajo.cancelAndJoin()
        show("vueltas antes de cancelar", "$vueltas de 1000")
    }

    section("Cuál elegir")

    bullet("¿Quieres terminar ordenadamente y devolver lo hecho? → `isActive`")
    bullet("¿Quieres interrumpir ya? → `ensureActive()`")
    bullet("¿Bucle largo de CPU compartiendo hilo? → `yield()`")
}

/**
 * Limpiar al cancelar.
 */
fun demoCleanup() {
    section("finally se ejecuta al cancelar")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            try {
                traza.add("abriendo recurso")
                repeat(10) {
                    delay(20)
                }
                traza.add("esto no se alcanza")
            } finally {
                traza.add("cerrando recurso")
            }
        }

        delay(50)
        trabajo.cancelAndJoin()
        traza.forEach { bullet(it) }
    }

    bullet("La CancellationException recorre el `finally` como cualquier excepción.")
    bullet("Por eso `use { }` también cierra correctamente al cancelar.")

    section("La trampa: no se puede suspender en el finally")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            try {
                delay(1_000)
            } finally {
                // La corrutina YA está cancelada: cualquier `delay` aquí lanzaría
                // otra CancellationException inmediatamente.
                val pudo = runCatching { delay(10) }.isSuccess
                traza.add("¿pudo suspender en el finally? $pudo")
            }
        }

        delay(20)
        trabajo.cancelAndJoin()
        traza.forEach { bullet(it) }
    }

    bullet("Una corrutina cancelada no puede suspenderse: ya no tiene permiso.")

    section("La solución: withContext(NonCancellable)")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            try {
                delay(1_000)
            } finally {
                withContext(NonCancellable) {
                    // Dentro de este bloque SÍ se puede suspender, aunque estemos
                    // cancelados. Es para limpieza que requiere esperar.
                    delay(10)
                    traza.add("limpieza asíncrona completada")
                }
            }
        }

        delay(20)
        trabajo.cancelAndJoin()
        traza.forEach { bullet(it) }
    }

    bullet("`NonCancellable` sólo para LIMPIEZA corta: cerrar una conexión, enviar")
    bullet("un último mensaje. Nunca para trabajo normal, o la cancelación no sirve.")
}

/**
 * El error más grave: tragarse la CancellationException.
 */
fun demoDontSwallow() {
    section("El antipatrón")

    bullet("try { ... } catch (e: Exception) { ... }")
    bullet("CancellationException HEREDA de Exception, así que ese catch la atrapa.")
    bullet("Resultado: la corrutina cree que sigue viva y continúa trabajando.")

    section("Demostración")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            repeat(5) { i ->
                try {
                    delay(20)
                    traza.add("vuelta $i ok")
                } catch (e: Exception) {
                    // ← EL ERROR: esto se traga la cancelación
                    traza.add("vuelta $i: me tragué ${e::class.simpleName}")
                }
            }
            traza.add("¡llegué al final pese a estar cancelado!")
        }

        delay(30)
        trabajo.cancel()
        trabajo.join()

        traza.forEach { bullet(it) }
    }

    bullet("Mira la última línea: la corrutina terminó su trabajo COMPLETO.")
    bullet("El `cancel()` no sirvió absolutamente de nada.")

    section("La forma correcta")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            repeat(5) { i ->
                try {
                    delay(20)
                    traza.add("vuelta $i ok")
                } catch (e: CancellationException) {
                    throw e                         // ← relanzar SIEMPRE
                } catch (e: Exception) {
                    traza.add("error real en la vuelta $i")
                }
            }
            traza.add("esto ya no se alcanza")
        }

        delay(30)
        trabajo.cancel()
        trabajo.join()

        traza.forEach { bullet(it) }
    }

    bullet("Ahora sí: la cancelación llega y la corrutina para.")

    section("Las tres reglas")

    bullet("1. Captura tipos CONCRETOS (IOException, NumberFormatException...).")
    bullet("2. Si capturas Exception o Throwable, relanza CancellationException antes.")
    bullet("3. `runCatching` tiene el MISMO problema: captura Throwable (cap. 19.10).")

    section("Y el matiz de TimeoutCancellationException")

    bullet("`withTimeout` lanza TimeoutCancellationException, que ES una")
    bullet("CancellationException. Si la relanzas por sistema, no podrás tratarla.")
    bullet("Para eso está `withTimeoutOrNull`, que se ve en la demo siguiente.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `throw e` del catch de CancellationException y observa la diferencia.
//  2. Cambia `isActive` por `ensureActive()` en el bucle y compara el comportamiento.
//  3. Pon un `delay` en un `finally` sin NonCancellable y comprueba que no se ejecuta.
//  4. Sustituye el `catch (e: Exception)` de tu propio código por tipos concretos.
