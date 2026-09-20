package com.alejandro.c31exercises

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Ejercicio 11 · comprueba el procesado del flujo de eventos. */
@OptIn(ExperimentalCoroutinesApi::class)
class FlowDeEventosTest {

    private val eventos = listOf<EventoUi>(
        EventoUi.Tecleo("ko"),
        EventoUi.Tecleo("kot"),
        EventoUi.Tecleo("kotl"),
        EventoUi.Tecleo("  kotl  "),
        EventoUi.Pulsacion("buscar"),
        EventoUi.Tecleo("kotlin"),
        EventoUi.Desplazamiento(120),
        EventoUi.Cerrar,
        EventoUi.Tecleo("después del cierre"),
    )

    // -- soloBusquedas ---------------------------------------------------------------------

    @Test
    fun `descarta las consultas demasiado cortas`() = runTest {
        val consultas = soloBusquedas(flowOf(EventoUi.Tecleo("ko"), EventoUi.Tecleo("kot"))).toList()

        assertContentEquals(listOf("kot"), consultas)
    }

    @Test
    fun `recorta los espacios`() = runTest {
        val consultas = soloBusquedas(flowOf(EventoUi.Tecleo("  kotlin  "))).toList()

        assertContentEquals(listOf("kotlin"), consultas)
    }

    @Test
    fun `no repite la consulta anterior`() = runTest {
        val consultas = soloBusquedas(
            flowOf(
                EventoUi.Tecleo("kotlin"),
                EventoUi.Tecleo("  kotlin  "),     // la misma tras recortar
                EventoUi.Tecleo("kotlin"),
            ),
        ).toList()

        assertContentEquals(listOf("kotlin"), consultas)
    }

    @Test
    fun `si la consulta vuelve despues de otra, si se emite`() = runTest {
        // `distinctUntilChanged` quita los CONSECUTIVOS, no los duplicados globales.
        val consultas = soloBusquedas(
            flowOf(
                EventoUi.Tecleo("kotlin"),
                EventoUi.Tecleo("corrutinas"),
                EventoUi.Tecleo("kotlin"),
            ),
        ).toList()

        assertContentEquals(listOf("kotlin", "corrutinas", "kotlin"), consultas)
    }

    @Test
    fun `ignora los eventos que no son tecleos`() = runTest {
        val consultas = soloBusquedas(
            flowOf(EventoUi.Pulsacion("buscar"), EventoUi.Desplazamiento(50), EventoUi.Cerrar),
        ).toList()

        assertTrue(consultas.isEmpty())
    }

    @Test
    fun `un flujo vacio produce un flujo vacio`() = runTest {
        assertTrue(soloBusquedas(emptyFlow()).toList().isEmpty())
    }

    // -- hastaCerrar -------------------------------------------------------------------------

    @Test
    fun `corta en Cerrar sin incluirlo`() = runTest {
        val procesados = hastaCerrar(eventos.asFlow()).toList()

        assertEquals(7, procesados.size)
        assertTrue(procesados.none { it is EventoUi.Cerrar })
    }

    @Test
    fun `lo que viene despues de Cerrar no pasa`() = runTest {
        val procesados = hastaCerrar(eventos.asFlow()).toList()

        assertTrue(
            procesados.none { it is EventoUi.Tecleo && "después del cierre" in it.texto },
        )
    }

    @Test
    fun `sin Cerrar pasan todos`() = runTest {
        val sinCierre = listOf<EventoUi>(EventoUi.Tecleo("a"), EventoUi.Pulsacion("b"))

        assertEquals(2, hastaCerrar(sinCierre.asFlow()).toList().size)
    }

    @Test
    fun `los dos operadores se componen`() = runTest {
        val consultas = soloBusquedas(hastaCerrar(eventos.asFlow())).toList()

        assertContentEquals(listOf("kot", "kotl", "kotlin"), consultas)
    }

    // -- contarPorTipo --------------------------------------------------------------------------

    @Test
    fun `cuenta los eventos por clase`() = runTest {
        val cuenta = contarPorTipo(eventos.asFlow())

        assertEquals(6, cuenta["Tecleo"])
        assertEquals(1, cuenta["Pulsacion"])
        assertEquals(1, cuenta["Desplazamiento"])
        assertEquals(1, cuenta["Cerrar"])
    }

    @Test
    fun `contar un flujo vacio da un mapa vacio`() = runTest {
        assertTrue(contarPorTipo(emptyFlow()).isEmpty())
    }

    // -- PantallaDeBusqueda: estado frente a evento -----------------------------------------------

    @Test
    fun `el estado empieza en Inicial y se puede leer sin recoger`() = runTest {
        val pantalla = PantallaDeBusqueda()

        assertEquals(EstadoDeBusqueda.Inicial, pantalla.estado.value)
    }

    @Test
    fun `una busqueda pasa por Buscando y acaba en Resultados`() = runTest {
        val pantalla = PantallaDeBusqueda()
        val vistos = mutableListOf<EstadoDeBusqueda>()

        val observando = launch { pantalla.estado.collect { vistos += it } }
        advanceUntilIdle()          // deja que el colector se suscriba antes de nada

        pantalla.buscar("kotlin")
        advanceUntilIdle()
        observando.cancel()

        assertEquals(3, vistos.size)
        assertEquals(EstadoDeBusqueda.Inicial, vistos[0])
        assertIs<EstadoDeBusqueda.Buscando>(vistos[1])
        assertIs<EstadoDeBusqueda.Resultados>(vistos[2])
    }

    @Test
    fun `una consulta corta avisa y no toca el estado`() = runTest {
        val pantalla = PantallaDeBusqueda()
        val avisos = mutableListOf<String>()

        val observando = launch { pantalla.avisos.collect { avisos += it } }
        advanceUntilIdle()

        pantalla.buscar("ko")
        advanceUntilIdle()
        observando.cancel()

        assertEquals(EstadoDeBusqueda.Inicial, pantalla.estado.value)
        assertEquals(1, avisos.size)
        assertContains(avisos.first(), "al menos $MINIMO_DE_LETRAS")
    }

    @Test
    fun `el MISMO aviso llega dos veces porque es un SharedFlow`() = runTest {
        val pantalla = PantallaDeBusqueda()
        val avisos = mutableListOf<String>()

        val observando = launch { pantalla.avisos.collect { avisos += it } }
        advanceUntilIdle()

        pantalla.buscar("ko")
        pantalla.buscar("ko")
        advanceUntilIdle()
        observando.cancel()

        // Con un StateFlow, el segundo NO llegaría: ésa es la diferencia.
        assertEquals(2, avisos.size, "avisos recibidos: $avisos")
    }

    @Test
    fun `el estado repetido no se emite dos veces, porque es un StateFlow`() = runTest {
        val pantalla = PantallaDeBusqueda()
        val vistos = mutableListOf<EstadoDeBusqueda>()

        val observando = launch { pantalla.estado.collect { vistos += it } }
        advanceUntilIdle()

        pantalla.buscar("kotlin")
        advanceUntilIdle()
        pantalla.buscar("kotlin")          // exactamente la misma búsqueda
        advanceUntilIdle()
        observando.cancel()

        // Inicial, Buscando(kotlin), Resultados(kotlin) y luego Buscando(kotlin)
        // otra vez (que SÍ es distinto del Resultados anterior) y Resultados.
        assertTrue(vistos.size <= 5, "estados vistos: $vistos")
        assertIs<EstadoDeBusqueda.Resultados>(vistos.last())
    }

    @Test
    fun `los resultados llevan la consulta que los ha generado`() = runTest {
        val pantalla = PantallaDeBusqueda()

        pantalla.buscar("corrutinas")

        val estado = pantalla.estado.value
        assertIs<EstadoDeBusqueda.Resultados>(estado)
        assertEquals("corrutinas", estado.consulta)
        assertEquals(3, estado.encontrados.size)
    }

    // -- Errores dentro del flujo -------------------------------------------------------------------

    @Test
    fun `el flujo que se rompe propaga la excepcion`() = runTest {
        val fallo = kotlin.runCatching { flujoQueSeRompe().toList() }.exceptionOrNull()

        assertIs<IllegalStateException>(fallo)
        assertContains(fallo.message.orEmpty(), "se cayó")
    }

    @Test
    fun `con catch se emite un valor de repuesto y el flujo termina bien`() = runTest {
        val valores = flujoConRecuperacion().toList()

        assertEquals(3, valores.size)
        assertContentEquals(listOf("primero", "segundo"), valores.take(2))
        assertContains(valores.last(), "recuperado")
    }

    // -- El operador propio ------------------------------------------------------------------------

    @Test
    fun `agruparDesplazamientos descarta los desplazamientos de cero pixeles`() = runTest {
        val resultado = flowOf(
            EventoUi.Desplazamiento(0),
            EventoUi.Desplazamiento(10),
            EventoUi.Tecleo("x"),
            EventoUi.Desplazamiento(0),
        ).agruparDesplazamientos().toList()

        assertEquals(2, resultado.size)
        assertTrue(resultado.none { it is EventoUi.Desplazamiento && it.pixeles == 0 })
    }
}
