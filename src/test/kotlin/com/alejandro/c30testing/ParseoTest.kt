package com.alejandro.c30testing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// =====================================================================================
//  Tests del parser de duraciones.
//
//  Un parser es donde más rentan los tests: la entrada válida son cuatro formas y la
//  inválida, infinitas. Aquí se ve también el patrón de la "tabla de casos", que
//  evita escribir veinte tests casi idénticos.
// =====================================================================================

class ParseoTest {

    @Test
    fun `entiende horas, minutos y segundos combinados`() {
        assertEquals(5_400_000L, parsearDuracionMs("1h30m"))
    }

    @Test
    fun `entiende cada unidad por separado`() {
        assertEquals(7_200_000L, parsearDuracionMs("2h"))
        assertEquals(5_400_000L, parsearDuracionMs("90m"))
        assertEquals(45_000L, parsearDuracionMs("45s"))
    }

    @Test
    fun `entiende las tres unidades a la vez`() {
        assertEquals(3_600_000L + 120_000L + 3_000L, parsearDuracionMs("1h2m3s"))
    }

    @Test
    fun `no le molestan los espacios ni las mayusculas`() {
        assertEquals(3_600_000L, parsearDuracionMs("  1H  ".replace(" ", "")))
        assertEquals(3_600_000L, parsearDuracionMs("1H"))
        assertEquals(3_600_000L, parsearDuracionMs(" 1h "))
    }

    @Test
    fun `el cero es una duracion valida`() {
        assertEquals(0L, parsearDuracionMs("0s"))
    }

    @Test
    fun `una cadena vacia devuelve null`() {
        assertNull(parsearDuracionMs(""))
        assertNull(parsearDuracionMs("   "))
    }

    @Test
    fun `un texto sin unidades devuelve null`() {
        // El patrón encaja con la cadena vacía, así que este caso necesita su propia
        // comprobación en el código. De ahí este test.
        assertNull(parsearDuracionMs("30"))
    }

    @Test
    fun `una tabla de entradas invalidas`() {
        // Un solo test para muchos casos parecidos: si falla, el mensaje dice cuál.
        val invalidas = listOf("hola", "1h2x", "h30m", "1m1h", "--", "1.5h", "1h 30m")

        invalidas.forEach { entrada ->
            assertNull(parsearDuracionMs(entrada), "«$entrada» no debería parsearse")
        }
    }

    @Test
    fun `el orden de las unidades importa`() {
        // "1m1h" no vale: el patrón espera horas, luego minutos, luego segundos.
        assertNull(parsearDuracionMs("1m1h"))
        assertEquals(3_660_000L, parsearDuracionMs("1h1m"))
    }

    @Test
    fun `admite numeros grandes sin desbordarse`() {
        // 10.000 horas en milisegundos se pasa de Int: por eso la función usa Long
        // (capítulo 02, desbordamiento silencioso).
        assertEquals(36_000_000_000L, parsearDuracionMs("10000h"))
    }
}
