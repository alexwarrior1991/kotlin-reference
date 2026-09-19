package com.alejandro.c31exercises

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// =====================================================================================
//  Ejercicio 10 · comprueba las descargas concurrentes.
//
//  Todo con `runTest` y tiempo virtual: las esperas de la solución (timeouts de
//  200 ms, reintentos con espera creciente) se recorren al instante, y además se
//  puede AFIRMAR sobre el calendario con `currentTime` (capítulo 30).
// =====================================================================================

@OptIn(ExperimentalCoroutinesApi::class)
class DescargasTest {

    /** Un descargador de mentira: cada url tiene su propio comportamiento. */
    private fun descargadorQue(vararg respuestas: Pair<String, suspend () -> String>): Descargador {
        val mapa = respuestas.toMap()
        return Descargador { url -> mapa.getValue(url)() }
    }

    private fun contenidoDe(resultado: ResultadoDescarga): String {
        assertIs<ResultadoDescarga.Ok>(resultado, "esperaba un éxito, fue: $resultado")
        return resultado.contenido
    }

    // -- Paralelismo ----------------------------------------------------------------------

    @Test
    fun `las descargas van en paralelo, no una detras de otra`() = runTest {
        val descargador = Descargador { url -> delay(100); "contenido de $url" }

        descargarTodo(listOf("a", "b", "c"), descargador, concurrenciaMaxima = 3)

        // En serie serían 300 ms; en paralelo, 100.
        assertEquals(100L, currentTime)
    }

    @Test
    fun `el resultado conserva el orden de entrada aunque terminen desordenadas`() = runTest {
        // Ojo con el timeout por defecto (200 ms): la más lenta se queda justo debajo.
        val descargador = descargadorQue(
            "lenta" to { delay(150); "1" },
            "media" to { delay(100); "2" },
            "rapida" to { delay(10); "3" },
        )

        val resultados = descargarTodo(listOf("lenta", "media", "rapida"), descargador)

        assertContentEquals(listOf("lenta", "media", "rapida"), resultados.map { it.url })
        assertContentEquals(listOf("1", "2", "3"), resultados.map { contenidoDe(it) })
    }

    @Test
    fun `una lista vacia devuelve una lista vacia`() = runTest {
        assertTrue(descargarTodo(emptyList(), Descargador { "x" }).isEmpty())
    }

    // -- Aislamiento de fallos --------------------------------------------------------------

    @Test
    fun `un fallo no cancela las demas descargas`() = runTest {
        val descargador = descargadorQue(
            "buena-1" to { delay(50); "ok-1" },
            "rota" to { error("404") },
            "buena-2" to { delay(50); "ok-2" },
        )

        val resultados = descargarTodo(listOf("buena-1", "rota", "buena-2"), descargador)

        assertEquals("ok-1", contenidoDe(resultados[0]))
        assertIs<ResultadoDescarga.Fallo>(resultados[1])
        assertEquals("ok-2", contenidoDe(resultados[2]))
    }

    @Test
    fun `el fallo conserva el motivo`() = runTest {
        val descargador = descargadorQue("rota" to { error("404: no encontrado") })

        val fallo = descargarTodo(listOf("rota"), descargador, reintentos = 1).first()

        assertIs<ResultadoDescarga.Fallo>(fallo)
        assertContains(fallo.motivo, "404")
    }

    // -- Timeout ------------------------------------------------------------------------------

    @Test
    fun `una descarga que se pasa del timeout falla, pero sin bloquear a las demas`() = runTest {
        val descargador = descargadorQue(
            "lentisima" to { delay(10_000); "no llega" },
            "normal" to { delay(10); "ok" },
        )

        val resultados = descargarTodo(
            listOf("lentisima", "normal"),
            descargador,
            timeoutMs = 100,
            reintentos = 1,
        )

        val fallo = resultados[0]
        assertIs<ResultadoDescarga.Fallo>(fallo)
        assertContains(fallo.motivo, "tiempo agotado")
        assertEquals("ok", contenidoDe(resultados[1]))
    }

    // -- Reintentos -----------------------------------------------------------------------------

    @Test
    fun `reintenta los fallos transitorios hasta que funciona`() = runTest {
        val intentos = AtomicInteger(0)
        val descargador = Descargador {
            if (intentos.incrementAndGet() < 3) error("todavía no") else "conseguido"
        }

        val resultado = descargarTodo(listOf("inestable"), descargador, reintentos = 3).first()

        assertEquals("conseguido", contenidoDe(resultado))
        assertIs<ResultadoDescarga.Ok>(resultado)
        assertEquals(3, resultado.intentos)
    }

    @Test
    fun `al agotar los reintentos devuelve Fallo con el numero de intentos`() = runTest {
        val intentos = AtomicInteger(0)
        val descargador = Descargador { intentos.incrementAndGet(); error("siempre falla") }

        val resultado = descargarTodo(listOf("rota"), descargador, reintentos = 4).first()

        assertIs<ResultadoDescarga.Fallo>(resultado)
        assertEquals(4, resultado.intentos)
        assertEquals(4, intentos.get())
    }

    @Test
    fun `la espera entre reintentos se duplica, y no se espera tras el ultimo`() = runTest {
        val descargador = Descargador { error("siempre falla") }

        descargarTodo(listOf("rota"), descargador, reintentos = 4)

        // Esperas: 20, 40 y 80 ms. Tras el cuarto intento, ninguna.
        assertEquals(140L, currentTime)
    }

    @Test
    fun `una descarga que funciona a la primera no espera nada`() = runTest {
        descargarTodo(listOf("a"), Descargador { "ok" })

        assertEquals(0L, currentTime)
    }

    // -- Concurrencia limitada ---------------------------------------------------------------------

    @Test
    fun `el semaforo limita cuantas descargas hay a la vez`() = runTest {
        val enCurso = AtomicInteger(0)
        val maximo = AtomicInteger(0)

        val descargador = Descargador {
            val ahora = enCurso.incrementAndGet()
            maximo.updateAndGet { anterior -> maxOf(anterior, ahora) }
            delay(60)
            enCurso.decrementAndGet()
            "ok"
        }

        descargarTodo(List(6) { "url-$it" }, descargador, concurrenciaMaxima = 2)

        assertEquals(2, maximo.get(), "nunca debería haber más de 2 a la vez")
        // 6 descargas de 60 ms, de dos en dos: tres tandas.
        assertEquals(180L, currentTime)
    }

    @Test
    fun `sin limite efectivo van todas a la vez`() = runTest {
        val descargador = Descargador { delay(60); "ok" }

        descargarTodo(List(6) { "url-$it" }, descargador, concurrenciaMaxima = 6)

        assertEquals(60L, currentTime)
    }

    // -- Cancelación -------------------------------------------------------------------------------

    @Test
    fun `cancelar detiene las descargas pendientes`() = runTest {
        val completadas = AtomicInteger(0)
        val descargador = Descargador { delay(100); completadas.incrementAndGet(); "ok" }

        val trabajo = launch {
            descargarTodo(List(5) { "url-$it" }, descargador, concurrenciaMaxima = 1)
        }

        advanceTimeBy(150)          // da tiempo a una sola descarga
        trabajo.cancelAndJoin()

        assertTrue(trabajo.isCancelled)
        assertTrue(completadas.get() < 5, "completadas: ${completadas.get()}")
    }
}
