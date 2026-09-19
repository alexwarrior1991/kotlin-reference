package com.alejandro.c31exercises

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Ejercicio 8 · comprueba los informes sobre la lista de ventas. */
class ColeccionesTest {

    private val ventas = ventasDeEjemplo()

    // -- totalPorVendedor ------------------------------------------------------------------

    @Test
    fun `suma el importe de cada vendedor`() {
        val totales = totalPorVendedor(ventas)

        assertEquals(50_550L, totales["Ana"])
        assertEquals(64_500L, totales["Luis"])
        assertEquals(89_500L, totales["Marta"])
        assertEquals(26_000L, totales["Pedro"])
        assertEquals(105_000L, totales["Sara"])
    }

    @Test
    fun `la suma de los totales es el total general`() {
        assertEquals(
            ventas.sumOf { it.importeCentimos.toLong() },
            totalPorVendedor(ventas).values.sum(),
        )
    }

    @Test
    fun `con una lista vacia devuelve un mapa vacio`() {
        assertTrue(totalPorVendedor(emptyList()).isEmpty())
    }

    @Test
    fun `la suma se hace en Long para no desbordarse`() {
        // Con Int, tres ventas de 1.000 millones de céntimos darían un negativo.
        val enormes = List(3) { Venta("Ana", "Norte", 1, 1_000_000_000) }

        assertEquals(3_000_000_000L, totalPorVendedor(enormes).getValue("Ana"))
    }

    // -- mejorVendedorPorRegion -------------------------------------------------------------

    @Test
    fun `identifica al lider de cada region`() {
        val lideres = mejorVendedorPorRegion(ventas)

        assertEquals("Sara", lideres["Norte"])
        assertEquals("Marta", lideres["Sur"])
        assertEquals("Marta", lideres["Este"])
    }

    @Test
    fun `hay un lider por cada region, ni mas ni menos`() {
        assertEquals(
            ventas.map { it.region }.toSet(),
            mejorVendedorPorRegion(ventas).keys,
        )
    }

    // -- mediaPorMes -------------------------------------------------------------------------

    @Test
    fun `calcula la media de cada mes`() {
        val medias = mediaPorMes(ventas)

        // Mes 1: 120,00 + 300,00 + 40,00 → 460,00 / 3
        assertEquals(46_000.0 / 3, medias.getValue(1), absoluteTolerance = 1e-9)
    }

    @Test
    fun `solo aparecen los meses con ventas`() {
        val medias = mediaPorMes(ventas)

        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7, 11, 12), medias.keys)
    }

    // -- porEncimaDeLaMedia --------------------------------------------------------------------

    @Test
    fun `devuelve los que superan la media, en orden alfabetico`() {
        // Media de totales: 335.550 / 5 = 67.110 céntimos.
        assertContentEquals(listOf("Marta", "Sara"), porEncimaDeLaMedia(ventas))
    }

    @Test
    fun `con un solo vendedor nadie supera la media`() {
        val unaSola = listOf(Venta("Ana", "Norte", 1, 100))

        assertTrue(porEncimaDeLaMedia(unaSola).isEmpty())
    }

    @Test
    fun `con una lista vacia devuelve una lista vacia`() {
        assertTrue(porEncimaDeLaMedia(emptyList()).isEmpty())
    }

    // -- rachaMasLargaConVentas ------------------------------------------------------------------

    @Test
    fun `cuenta los meses consecutivos`() {
        val rachas = rachaMasLargaConVentas(ventas)

        assertEquals(5, rachas["Pedro"], "Pedro vendió los meses 1 a 5")
        assertEquals(3, rachas["Ana"], "Ana: 1, 2, 3 y luego 5")
        assertEquals(3, rachas["Luis"], "Luis: 4, 5, 6")
        assertEquals(2, rachas["Marta"], "Marta: 2 y 3")
        assertEquals(2, rachas["Sara"], "Sara: 11 y 12")
    }

    @Test
    fun `varias ventas el mismo mes cuentan como un mes`() {
        val mismoMes = listOf(
            Venta("Ana", "Norte", 3, 100),
            Venta("Ana", "Norte", 3, 200),
            Venta("Ana", "Norte", 3, 300),
        )

        assertEquals(1, rachaMasLargaConVentas(mismoMes).getValue("Ana"))
    }

    @Test
    fun `una sola venta es una racha de uno`() {
        val una = listOf(Venta("Ana", "Norte", 6, 100))

        assertEquals(1, rachaMasLargaConVentas(una).getValue("Ana"))
    }

    // -- resumenPorRegion --------------------------------------------------------------------------

    @Test
    fun `el resumen cuadra con las ventas de la region`() {
        val resumen = resumenPorRegion(ventas)

        assertEquals(7, resumen.getValue("Norte").ventas)
        assertEquals(182_050L, resumen.getValue("Norte").totalCentimos)
        assertEquals(182_050L / 7, resumen.getValue("Norte").ticketMedioCentimos)
    }

    @Test
    fun `el resumen sale ordenado por region`() {
        assertContentEquals(listOf("Este", "Norte", "Sur"), resumenPorRegion(ventas).keys.toList())
    }

    @Test
    fun `el ticket medio de cero ventas es cero, no una division por cero`() {
        assertEquals(0L, ResumenRegion(ventas = 0, totalCentimos = 0).ticketMedioCentimos)
    }

    // -- topN -----------------------------------------------------------------------------------------

    @Test
    fun `el top devuelve los mejores en orden descendente`() {
        val top = topN(ventas, 3)

        assertContentEquals(listOf("Sara", "Marta", "Luis"), top.map { it.first })
        assertEquals(105_000L, top.first().second)
    }

    @Test
    fun `pedir mas de los que hay devuelve todos`() {
        assertEquals(5, topN(ventas, 99).size)
    }

    @Test
    fun `pedir cero devuelve una lista vacia`() {
        assertTrue(topN(ventas, 0).isEmpty())
    }

    @Test
    fun `los empates se resuelven por nombre, siempre igual`() {
        val empatados = listOf(
            Venta("Zoe", "Norte", 1, 100),
            Venta("Ana", "Norte", 1, 100),
            Venta("Mar", "Norte", 1, 100),
        )

        // Sin el desempate, el orden dependería del recorrido del mapa.
        repeat(5) {
            assertContentEquals(listOf("Ana", "Mar", "Zoe"), topN(empatados, 3).map { it.first })
        }
    }

    // -- Lista frente a secuencia -----------------------------------------------------------------------

    @Test
    fun `la cadena con List y con Sequence dan el mismo resultado`() {
        assertContentEquals(conListas(ventas), conSecuencias(ventas))
    }

    @Test
    fun `la cadena devuelve los tres mejores vendedores del primer semestre`() {
        assertContentEquals(listOf("MARTA", "LUIS", "ANA"), conListas(ventas))
    }

    // -- Validación del modelo ----------------------------------------------------------------------------

    @Test
    fun `una venta con mes o importe imposible no se puede construir`() {
        assertFailsWith<IllegalArgumentException> { Venta("Ana", "Norte", 0, 100) }
        assertFailsWith<IllegalArgumentException> { Venta("Ana", "Norte", 13, 100) }
        assertFailsWith<IllegalArgumentException> { Venta("Ana", "Norte", 1, 0) }
    }

    @Test
    fun `los datos de ejemplo son estables`() {
        // Si alguien cambia `ventasDeEjemplo`, los números de arriba dejan de valer:
        // este test lo avisa antes de que fallen diez tests a la vez.
        assertEquals(19, ventas.size)
        assertEquals(5, ventas.map { it.vendedor }.distinct().size)
        assertEquals(335_550L, ventas.sumOf { it.importeCentimos.toLong() })
    }
}
