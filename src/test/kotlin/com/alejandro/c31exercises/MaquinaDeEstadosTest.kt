package com.alejandro.c31exercises

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Ejercicio 7 · comprueba la máquina de estados del pedido. */
class MaquinaDeEstadosTest {

    private val instante = 1_700_000_000_000L

    private lateinit var reloj: RelojDePedidos

    @BeforeTest
    fun preparar() {
        reloj = RelojDePedidos(ahoraMs = instante)
    }

    private fun Pedido.tras(vararg eventos: EventoPedido): Pedido {
        var actual = this
        eventos.forEach { evento ->
            val resultado = actual.aplicar(evento, reloj)
            assertIs<ResultadoTransicion.Ok>(resultado, "no se pudo aplicar $evento")
            actual = resultado.pedido
        }
        return actual
    }

    // -- El camino feliz -----------------------------------------------------------------

    @Test
    fun `un pedido nuevo nace en borrador`() {
        assertEquals(EstadoPedido.Borrador, Pedido.nuevo("PED-1").estado)
    }

    @Test
    fun `borrador confirmado enviado entregado`() {
        val entregado = Pedido.nuevo("PED-1").tras(
            EventoPedido.Confirmar,
            EventoPedido.Enviar("ES-1"),
            EventoPedido.Entregar("A. García"),
        )

        val estado = entregado.estado
        assertIs<EstadoPedido.Entregado>(estado)
        assertEquals("ES-1", estado.seguimiento)
        assertEquals("A. García", estado.firmadoPor)
        assertEquals(instante, estado.confirmadoEnMs)
    }

    @Test
    fun `el seguimiento se arrastra de Enviado a Entregado`() {
        val entregado = Pedido.nuevo("PED-1").tras(
            EventoPedido.Confirmar,
            EventoPedido.Enviar("ES-ARRASTRADO"),
            EventoPedido.Entregar("firmante"),
        )

        assertEquals("ES-ARRASTRADO", (entregado.estado as EstadoPedido.Entregado).seguimiento)
    }

    @Test
    fun `el historial recoge todas las transiciones`() {
        val entregado = Pedido.nuevo("PED-1").tras(
            EventoPedido.Confirmar,
            EventoPedido.Enviar("ES-1"),
            EventoPedido.Entregar("firmante"),
        )

        assertEquals(3, entregado.historial.size)
        assertContains(entregado.historial.first(), "Borrador")
        assertContains(entregado.historial.last(), "Entregado")
    }

    // -- Cancelaciones --------------------------------------------------------------------

    @Test
    fun `se puede cancelar desde borrador`() {
        val cancelado = Pedido.nuevo("PED-1").tras(EventoPedido.Cancelar("me arrepentí"))

        val estado = cancelado.estado
        assertIs<EstadoPedido.Cancelado>(estado)
        assertEquals("me arrepentí", estado.motivo)
    }

    @Test
    fun `se puede cancelar desde confirmado y desde enviado`() {
        val desdeConfirmado = Pedido.nuevo("A")
            .tras(EventoPedido.Confirmar, EventoPedido.Cancelar("sin stock"))
        val desdeEnviado = Pedido.nuevo("B")
            .tras(EventoPedido.Confirmar, EventoPedido.Enviar("ES-1"), EventoPedido.Cancelar("perdido"))

        assertIs<EstadoPedido.Cancelado>(desdeConfirmado.estado)
        assertIs<EstadoPedido.Cancelado>(desdeEnviado.estado)
    }

    @Test
    fun `un pedido entregado NO se puede cancelar`() {
        val entregado = Pedido.nuevo("PED-1").tras(
            EventoPedido.Confirmar,
            EventoPedido.Enviar("ES-1"),
            EventoPedido.Entregar("firmante"),
        )

        val resultado = entregado.aplicar(EventoPedido.Cancelar("tarde"), reloj)

        assertIs<ResultadoTransicion.NoPermitida>(resultado)
        assertContains(resultado.motivo, "Entregado")
    }

    @Test
    fun `un pedido cancelado no admite nada`() {
        val cancelado = Pedido.nuevo("PED-1").tras(EventoPedido.Cancelar("x"))

        listOf(
            EventoPedido.Confirmar,
            EventoPedido.Enviar("ES-1"),
            EventoPedido.Entregar("firmante"),
            EventoPedido.Cancelar("otra vez"),
        ).forEach { evento ->
            assertIs<ResultadoTransicion.NoPermitida>(
                cancelado.aplicar(evento, reloj),
                "no debería admitir $evento",
            )
        }
    }

    // -- Transiciones inválidas -----------------------------------------------------------

    @Test
    fun `no se puede enviar un borrador`() {
        val resultado = Pedido.nuevo("PED-1").aplicar(EventoPedido.Enviar("ES-1"), reloj)

        assertIs<ResultadoTransicion.NoPermitida>(resultado)
        assertContains(resultado.motivo, "Borrador")
    }

    @Test
    fun `no se puede entregar lo que no se ha enviado`() {
        val confirmado = Pedido.nuevo("PED-1").tras(EventoPedido.Confirmar)

        assertIs<ResultadoTransicion.NoPermitida>(
            confirmado.aplicar(EventoPedido.Entregar("firmante"), reloj),
        )
    }

    @Test
    fun `no se puede confirmar dos veces`() {
        val confirmado = Pedido.nuevo("PED-1").tras(EventoPedido.Confirmar)

        assertIs<ResultadoTransicion.NoPermitida>(
            confirmado.aplicar(EventoPedido.Confirmar, reloj),
        )
    }

    @Test
    fun `una transicion invalida no cambia el pedido ni el historial`() {
        val confirmado = Pedido.nuevo("PED-1").tras(EventoPedido.Confirmar)

        confirmado.aplicar(EventoPedido.Entregar("firmante"), reloj)

        assertIs<EstadoPedido.Confirmado>(confirmado.estado)
        assertEquals(1, confirmado.historial.size)
    }

    // -- transicion, la función pura -------------------------------------------------------

    @Test
    fun `transicion devuelve null cuando no se puede`() {
        assertNull(transicion(EstadoPedido.Borrador, EventoPedido.Enviar("ES-1"), instante))
        assertNull(transicion(EstadoPedido.Borrador, EventoPedido.Entregar("x"), instante))
    }

    @Test
    fun `transicion usa el instante que se le pasa`() {
        val nuevo = transicion(EstadoPedido.Borrador, EventoPedido.Confirmar, 42L)

        assertEquals(EstadoPedido.Confirmado(42L), nuevo)
    }

    // -- esFinal ---------------------------------------------------------------------------

    @Test
    fun `solo Entregado y Cancelado son finales`() {
        assertFalse(EstadoPedido.Borrador.esFinal)
        assertFalse(EstadoPedido.Confirmado(0).esFinal)
        assertFalse(EstadoPedido.Enviado(0, "ES-1").esFinal)
        assertTrue(EstadoPedido.Entregado(0, "ES-1", "x", 1).esFinal)
        assertTrue(EstadoPedido.Cancelado("x", 0).esFinal)
    }

    // -- Inmutabilidad -----------------------------------------------------------------------

    @Test
    fun `aplicar devuelve un pedido nuevo sin tocar el anterior`() {
        val borrador = Pedido.nuevo("PED-1")

        val confirmado = borrador.tras(EventoPedido.Confirmar)

        assertEquals(EstadoPedido.Borrador, borrador.estado, "el original no debe cambiar")
        assertTrue(borrador.historial.isEmpty())
        assertFalse(borrador === confirmado)
    }

    @Test
    fun `el reloj inyectado controla los instantes`() {
        val confirmado = Pedido.nuevo("PED-1").tras(EventoPedido.Confirmar)
        reloj.avanzarHoras(48)
        val entregado = confirmado.tras(EventoPedido.Enviar("ES-1"), EventoPedido.Entregar("x"))

        val estado = entregado.estado as EstadoPedido.Entregado
        assertEquals(instante, estado.confirmadoEnMs)
        assertEquals(instante + 48 * 3_600_000L, estado.entregadoEnMs)
    }
}
