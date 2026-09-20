package com.alejandro.c30testing

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// =====================================================================================
//  Tests del cálculo de un pedido (funciones puras del capítulo 30.1).
//
//  Fíjate en el patrón: un test por COMPORTAMIENTO, no uno por función. Cuando algo
//  se rompa, el nombre del test que se ponga rojo ya te dirá qué ha dejado de valer.
// =====================================================================================

class PreciosTest {

    // -- calcularSubtotal ------------------------------------------------------------

    @Test
    fun `el subtotal suma cantidad por precio de cada linea`() {
        // Preparar
        val lineas = listOf(
            LineaPedido("teclado", cantidad = 1, precioUnitario = 45.0),
            LineaPedido("ratón", cantidad = 2, precioUnitario = 12.5),
        )

        // Actuar
        val subtotal = calcularSubtotal(lineas)

        // Comprobar
        assertEquals(70.0, subtotal)
    }

    @Test
    fun `el subtotal de una lista vacia es cero`() {
        assertEquals(0.0, calcularSubtotal(emptyList()))
    }

    @Test
    fun `una linea con cantidad cero no suma nada`() {
        val lineas = listOf(LineaPedido("cable", cantidad = 0, precioUnitario = 9.99))

        assertEquals(0.0, calcularSubtotal(lineas))
    }

    // -- aplicarDescuento ------------------------------------------------------------

    @Test
    fun `el descuento resta el porcentaje indicado`() {
        assertEquals(90.0, aplicarDescuento(100.0, porcentaje = 10))
    }

    @Test
    fun `un descuento del cero por ciento deja el importe igual`() {
        assertEquals(100.0, aplicarDescuento(100.0, porcentaje = 0))
    }

    @Test
    fun `un descuento del cien por cien deja el importe a cero`() {
        assertEquals(0.0, aplicarDescuento(100.0, porcentaje = 100))
    }

    @Test
    fun `lanza si el descuento es mayor que cien`() {
        // `assertFailsWith` DEVUELVE la excepción: el mensaje también es contrato.
        val fallo = assertFailsWith<IllegalArgumentException> {
            aplicarDescuento(100.0, porcentaje = 150)
        }

        assertContains(fallo.message.orEmpty(), "entre 0 y 100")
    }

    @Test
    fun `lanza si el descuento es negativo`() {
        assertFailsWith<IllegalArgumentException> {
            aplicarDescuento(100.0, porcentaje = -1)
        }
    }

    // -- anadirIva -------------------------------------------------------------------

    @Test
    fun `el IVA por defecto es el veintiuno por ciento`() {
        // Con decimales conviene una tolerancia: 63.0 * 1.21 no es exacto en binario
        // (capítulo 02). `absoluteTolerance` existe justo para esto.
        assertEquals(76.23, anadirIva(63.0), absoluteTolerance = 1e-9)
    }

    @Test
    fun `el tipo de IVA se puede cambiar`() {
        assertEquals(110.0, anadirIva(100.0, ivaPorCiento = 10), absoluteTolerance = 1e-9)
    }

    @Test
    fun `un IVA del cero por ciento deja el importe igual`() {
        assertEquals(100.0, anadirIva(100.0, ivaPorCiento = 0), absoluteTolerance = 1e-9)
    }

    @Test
    fun `lanza si el IVA es negativo`() {
        assertFailsWith<IllegalArgumentException> { anadirIva(100.0, ivaPorCiento = -1) }
    }

    // -- Las tres juntas, que es como se usan de verdad -------------------------------

    @Test
    fun `el total de un pedido con descuento e IVA cuadra`() {
        val lineas = listOf(
            LineaPedido("teclado", cantidad = 1, precioUnitario = 45.0),
            LineaPedido("ratón", cantidad = 2, precioUnitario = 12.5),
        )

        val total = anadirIva(aplicarDescuento(calcularSubtotal(lineas), porcentaje = 10))

        assertEquals(76.23, total, absoluteTolerance = 1e-9)
    }
}
