package com.alejandro.c30testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.measureTime

// =====================================================================================
//  Corrutinas con `runTest` y tiempo virtual.
//
//  Todo lo que se prueba aquí tiene esperas de segundos o de horas en su código de
//  producción, y aun así estos tests tardan milisegundos: dentro de `runTest`, el
//  planificador adelanta un reloj VIRTUAL en lugar de dormir de verdad.
//
//  Las funciones que manipulan ese reloj (`advanceTimeBy`, `advanceUntilIdle`,
//  `runCurrent`, `currentTime`, `testScheduler`) todavía llevan la marca
//  `@ExperimentalCoroutinesApi`; de ahí el `@OptIn` de la clase.
// =====================================================================================

@OptIn(ExperimentalCoroutinesApi::class)
class CorrutinasTest {

    // -- El tiempo virtual, de entrada -----------------------------------------------

    @Test
    fun `un delay de una hora es instantaneo en tiempo virtual`() = runTest {
        val tiempoReal = measureTime {
            delay(1.hours)
        }

        assertEquals(3_600_000L, currentTime, "el reloj virtual sí avanza una hora")
        assertTrue(
            tiempoReal.inWholeSeconds < 5,
            "pero el test tardó $tiempoReal de verdad",
        )
    }

    // -- reintentarConEspera ----------------------------------------------------------

    @Test
    fun `devuelve a la primera si la operacion funciona`() = runTest {
        var llamadas = 0

        val resultado = reintentarConEspera(intentos = 3) {
            llamadas++
            "a la primera"
        }

        assertEquals("a la primera", resultado)
        assertEquals(1, llamadas)
        assertEquals(0L, currentTime, "no debería haber esperado nada")
    }

    @Test
    fun `reintenta hasta que la operacion funciona`() = runTest {
        var llamadas = 0

        val resultado = reintentarConEspera(intentos = 3) { numero ->
            llamadas++
            if (numero < 3) error("todavía no") else "conseguido"
        }

        assertEquals("conseguido", resultado)
        assertEquals(3, llamadas)
        // Esperó 1 s tras el primer fallo y 2 s tras el segundo: 3 s virtuales.
        assertEquals(3_000L, currentTime)
    }

    @Test
    fun `la espera se duplica en cada intento`() = runTest {
        val instantes = mutableListOf<Long>()

        assertFailsWith<IllegalStateException> {
            reintentarConEspera(intentos = 4) {
                instantes += currentTime
                error("siempre falla")
            }
        }

        // 0 · 1 s · 1+2 s · 1+2+4 s
        assertEquals(listOf(0L, 1_000L, 3_000L, 7_000L), instantes)
    }

    @Test
    fun `no espera despues del ultimo intento`() = runTest {
        assertFailsWith<IllegalStateException> {
            reintentarConEspera(intentos = 3) { error("siempre falla") }
        }

        // Si esperase también tras el tercero, currentTime sería 7_000.
        assertEquals(3_000L, currentTime)
    }

    @Test
    fun `al agotar los intentos lanza conservando la causa`() = runTest {
        val fallo = assertFailsWith<IllegalStateException> {
            reintentarConEspera(intentos = 2) { error("motivo original") }
        }

        assertContains(fallo.message.orEmpty(), "fallaron los 2 intentos")
        // La causa se conserva: sin esto se perdería la traza (capítulo 19).
        assertIs<IllegalStateException>(fallo.cause)
        assertEquals("motivo original", fallo.cause?.message)
    }

    @Test
    fun `lanza si no se permite ningun intento`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            reintentarConEspera(intentos = 0) { "da igual" }
        }
    }

    // -- cuentaAtras -------------------------------------------------------------------

    @Test
    fun `la cuenta atras emite desde el numero hasta cero`() = runTest {
        val valores = cuentaAtras(desde = 3).toList()

        assertEquals(listOf(3, 2, 1, 0), valores)
    }

    @Test
    fun `la cuenta atras desde cero emite solo el cero`() = runTest {
        assertEquals(listOf(0), cuentaAtras(desde = 0).toList())
    }

    @Test
    fun `la cuenta atras no espera despues del ultimo valor`() = runTest {
        cuentaAtras(desde = 2).toList()

        // Tres emisiones, dos esperas de 1 s: 2 s, no 3.
        assertEquals(2_000L, currentTime)
    }

    @Test
    fun `la cuenta atras emite un valor por intervalo`() = runTest {
        val emitidos = mutableListOf<Int>()

        val trabajo = launch {
            cuentaAtras(desde = 3).collect { emitidos += it }
        }

        runCurrent()
        assertEquals(listOf(3), emitidos, "el primero sale sin esperar")

        advanceTimeBy(1_500)
        runCurrent()
        assertEquals(listOf(3, 2), emitidos, "al segundo y medio sólo van dos")

        advanceUntilIdle()
        assertEquals(listOf(3, 2, 1, 0), emitidos)

        trabajo.join()
    }

    @Test
    fun `un flujo se puede acotar con take`() = runTest {
        // El patrón para flujos largos o infinitos: `take` antes del terminal.
        assertEquals(listOf(10, 9), cuentaAtras(desde = 10).take(2).toList())
    }

    @Test
    fun `la cuenta atras lanza si el numero es negativo`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            cuentaAtras(desde = -1).toList()
        }
    }

    // -- ServicioDeNoticias: inyectar el dispatcher -------------------------------------

    @Test
    fun `devuelve solo los titulares pedidos`() = runTest {
        val servicio = ServicioDeNoticias(
            // `FuenteDeTitulares` es una `fun interface`: una lambda vale como fake.
            fuente = { listOf("uno", "dos", "tres") },
            // Sin este dispatcher, el `withContext` de dentro saldría del tiempo
            // virtual y el test volvería a ser una carrera.
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        assertEquals(listOf("uno", "dos"), servicio.titularesRecientes(limite = 2))
    }

    @Test
    fun `pedir mas titulares de los que hay devuelve los que hay`() = runTest {
        val servicio = ServicioDeNoticias(
            fuente = { listOf("uno") },
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        assertEquals(listOf("uno"), servicio.titularesRecientes(limite = 10))
    }

    @Test
    fun `el servicio respeta las esperas de su fuente`() = runTest {
        val servicio = ServicioDeNoticias(
            fuente = {
                delay(2.hours)                 // una fuente lentísima
                listOf("por fin")
            },
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        assertEquals(listOf("por fin"), servicio.titularesRecientes(limite = 1))
        assertEquals(7_200_000L, currentTime, "dos horas virtuales")
    }

    @Test
    fun `el servicio lanza si el limite no es positivo`() = runTest {
        val servicio = ServicioDeNoticias(fuente = { listOf("uno") })

        assertFailsWith<IllegalArgumentException> { servicio.titularesRecientes(0) }
        assertFailsWith<IllegalArgumentException> { servicio.titularesRecientes(-3) }
    }
}
