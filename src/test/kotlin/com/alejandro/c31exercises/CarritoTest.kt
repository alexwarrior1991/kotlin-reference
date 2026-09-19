package com.alejandro.c31exercises

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Ejercicio 5 · comprueba la solución propuesta del carrito de compra. */
class CarritoTest {

    private val teclado = Articulo("TEC-1", "Teclado", precioCentimos = 4_500)
    private val raton = Articulo("RAT-1", "Ratón", precioCentimos = 1_250)
    private val monitor = Articulo("MON-1", "Monitor", precioCentimos = 21_900)

    private lateinit var carrito: Carrito

    @BeforeTest
    fun preparar() {
        carrito = Carrito()
    }

    // -- Añadir, cambiar y quitar ---------------------------------------------------------

    @Test
    fun `un carrito nuevo esta vacio`() {
        assertTrue(carrito.estaVacio())
        assertEquals(0, carrito.subtotalCentimos())
        assertEquals(0, carrito.unidadesTotales())
    }

    @Test
    fun `anadir el mismo articulo dos veces suma unidades, no crea otra linea`() {
        carrito.anadir(raton, 2)
        carrito.anadir(raton, 1)

        assertEquals(1, carrito.lineas().size)
        assertEquals(3, carrito.lineas().first().unidades)
    }

    @Test
    fun `el orden de las lineas es el de insercion`() {
        carrito.anadir(teclado)
        carrito.anadir(raton)
        carrito.anadir(monitor)

        assertEquals(
            listOf("TEC-1", "RAT-1", "MON-1"),
            carrito.lineas().map { it.articulo.codigo },
        )
    }

    @Test
    fun `anadir cero o menos unidades es un error de programacion`() {
        assertFailsWith<IllegalArgumentException> { carrito.anadir(raton, 0) }
        assertFailsWith<IllegalArgumentException> { carrito.anadir(raton, -1) }
    }

    @Test
    fun `cambiarUnidades sustituye la cantidad`() {
        carrito.anadir(raton, 2)

        assertTrue(carrito.cambiarUnidades("RAT-1", 7))

        assertEquals(7, carrito.lineas().first().unidades)
    }

    @Test
    fun `cambiar a cero unidades quita la linea`() {
        carrito.anadir(raton, 2)

        carrito.cambiarUnidades("RAT-1", 0)

        assertTrue(carrito.estaVacio())
    }

    @Test
    fun `cambiar o quitar un codigo que no esta devuelve false`() {
        assertFalse(carrito.cambiarUnidades("NO-EXISTE", 3))
        assertFalse(carrito.quitar("NO-EXISTE"))
    }

    @Test
    fun `vaciar deja el carrito a cero`() {
        carrito.anadir(teclado)
        carrito.anadir(raton, 3)

        carrito.vaciar()

        assertTrue(carrito.estaVacio())
        assertEquals(0, carrito.subtotalCentimos())
    }

    // -- Subtotal --------------------------------------------------------------------------

    @Test
    fun `el subtotal multiplica precio por unidades y suma`() {
        carrito.anadir(teclado, 1)      // 45,00 €
        carrito.anadir(raton, 2)        // 25,00 €

        assertEquals(7_000, carrito.subtotalCentimos())
    }

    @Test
    fun `los centimos no acumulan error, a diferencia de un Double`() {
        // 10 × 0,10 € tiene que dar 1,00 € EXACTO. Con Double, 0.1 sumado diez
        // veces da 0.9999999999999999 (capítulo 02).
        val barato = Articulo("BAR-1", "Chuchería", precioCentimos = 10)
        carrito.anadir(barato, 10)

        assertEquals(100, carrito.subtotalCentimos())
    }

    // -- Envío -----------------------------------------------------------------------------

    @Test
    fun `por debajo del umbral se cobra el envio`() {
        carrito.anadir(raton, 1)        // 12,50 €

        assertEquals(Carrito.ENVIO_CENTIMOS, carrito.resumen().envioCentimos)
    }

    @Test
    fun `al llegar al umbral el envio es gratis`() {
        carrito.anadir(monitor, 1)      // 219,00 €

        assertEquals(0, carrito.resumen().envioCentimos)
    }

    @Test
    fun `el umbral es inclusivo`() {
        val justo = Articulo("JUS-1", "Justo", precioCentimos = Carrito.UMBRAL_ENVIO_GRATIS_CENTIMOS)
        carrito.anadir(justo, 1)

        assertEquals(0, carrito.resumen().envioCentimos)
    }

    @Test
    fun `un carrito vacio no paga envio`() {
        assertEquals(0, carrito.resumen().envioCentimos)
        assertEquals(0, carrito.resumen().totalCentimos)
    }

    @Test
    fun `el cupon de envio gratis funciona aunque el carrito sea pequeno`() {
        carrito.anadir(raton, 1)

        val resumen = carrito.resumen(Cupon.EnvioGratis("PORTES"))

        assertEquals(0, resumen.envioCentimos)
        assertEquals(1_250, resumen.totalCentimos)
    }

    // -- Cupones ----------------------------------------------------------------------------

    @Test
    fun `el cupon de porcentaje descuenta sobre el subtotal`() {
        carrito.anadir(monitor, 1)      // 219,00 €

        val resumen = carrito.resumen(Cupon.Porcentaje("VERANO10", 10))

        assertEquals(2_190, resumen.descuentoCentimos)
        assertEquals(21_900 - 2_190, resumen.totalCentimos)
    }

    @Test
    fun `el cupon de importe fijo descuenta lo que dice`() {
        carrito.anadir(monitor, 1)

        assertEquals(500, carrito.resumen(Cupon.ImporteFijo("MENOS5", 500)).descuentoCentimos)
    }

    @Test
    fun `un descuento mayor que el carrito no deja el total en negativo`() {
        carrito.anadir(raton, 1)        // 12,50 €

        val resumen = carrito.resumen(Cupon.ImporteFijo("EXAGERADO", 999_999))

        assertEquals(1_250, resumen.descuentoCentimos, "el descuento se topa al subtotal")
        assertEquals(0, resumen.subtotalCentimos - resumen.descuentoCentimos)
        assertTrue(resumen.totalCentimos >= 0)
    }

    @Test
    fun `el resumen recuerda que cupon se aplico`() {
        carrito.anadir(raton, 1)

        assertEquals("VERANO10", carrito.resumen(Cupon.Porcentaje("VERANO10", 10)).cuponAplicado)
        assertEquals(null, carrito.resumen().cuponAplicado)
    }

    @Test
    fun `un porcentaje fuera de rango no se puede ni construir`() {
        assertFailsWith<IllegalArgumentException> { Cupon.Porcentaje("MALO", 0) }
        assertFailsWith<IllegalArgumentException> { Cupon.Porcentaje("MALO", 101) }
    }

    @Test
    fun `un cupon de importe cero o negativo no se puede construir`() {
        assertFailsWith<IllegalArgumentException> { Cupon.ImporteFijo("MALO", 0) }
        assertFailsWith<IllegalArgumentException> { Cupon.ImporteFijo("MALO", -100) }
    }

    // -- Artículo -----------------------------------------------------------------------------

    @Test
    fun `un articulo necesita codigo y precio no negativo`() {
        assertFailsWith<IllegalArgumentException> { Articulo("", "x", 100) }
        assertFailsWith<IllegalArgumentException> { Articulo("A", "x", -1) }
    }

    // -- Formato -------------------------------------------------------------------------------

    @Test
    fun `los centimos se formatean con dos decimales siempre`() {
        assertEquals("0,00 €", euros(0))
        assertEquals("0,05 €", euros(5))
        assertEquals("1,00 €", euros(100))
        assertEquals("219,00 €", euros(21_900))
        assertEquals("1234,56 €", euros(123_456))
    }
}
