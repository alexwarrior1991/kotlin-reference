package com.alejandro.c31exercises

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Ejercicio 3 · comprueba la solución propuesta del conversor de temperaturas. */
class TemperaturasTest {

    /** Con decimales, siempre con tolerancia (capítulo 02). */
    private fun assertCasiIgual(esperado: Double, real: Double) =
        assertEquals(esperado, real, absoluteTolerance = 1e-9)

    // -- convertir ---------------------------------------------------------------------

    @Test
    fun `los puntos de referencia del agua`() {
        assertCasiIgual(32.0, convertir(0.0, Escala.CELSIUS, Escala.FAHRENHEIT))
        assertCasiIgual(212.0, convertir(100.0, Escala.CELSIUS, Escala.FAHRENHEIT))
        assertCasiIgual(273.15, convertir(0.0, Escala.CELSIUS, Escala.KELVIN))
    }

    @Test
    fun `y en el otro sentido`() {
        assertCasiIgual(0.0, convertir(32.0, Escala.FAHRENHEIT, Escala.CELSIUS))
        assertCasiIgual(100.0, convertir(212.0, Escala.FAHRENHEIT, Escala.CELSIUS))
        assertCasiIgual(0.0, convertir(273.15, Escala.KELVIN, Escala.CELSIUS))
    }

    @Test
    fun `convertir a la misma escala devuelve el mismo numero exacto`() {
        // Sin el atajo, ida y vuelta introduciría error de redondeo.
        assertEquals(37.3, convertir(37.3, Escala.CELSIUS, Escala.CELSIUS))
        assertEquals(98.6, convertir(98.6, Escala.FAHRENHEIT, Escala.FAHRENHEIT))
    }

    @Test
    fun `el cero absoluto es el mismo en las tres escalas`() {
        assertCasiIgual(0.0, convertir(-273.15, Escala.CELSIUS, Escala.KELVIN))
        assertCasiIgual(0.0, convertir(-459.67, Escala.FAHRENHEIT, Escala.KELVIN))
    }

    @Test
    fun `ida y vuelta conserva el valor`() {
        val original = 21.5
        val vuelta = convertir(
            convertir(original, Escala.CELSIUS, Escala.FAHRENHEIT),
            Escala.FAHRENHEIT,
            Escala.CELSIUS,
        )
        assertCasiIgual(original, vuelta)
    }

    @Test
    fun `lanza por debajo del cero absoluto en Celsius`() {
        val fallo = assertFailsWith<IllegalArgumentException> {
            convertir(-300.0, Escala.CELSIUS, Escala.KELVIN)
        }
        assertContains(fallo.message.orEmpty(), "cero absoluto")
    }

    @Test
    fun `lanza por debajo del cero absoluto en las otras escalas`() {
        assertFailsWith<IllegalArgumentException> {
            convertir(-500.0, Escala.FAHRENHEIT, Escala.CELSIUS)
        }
        assertFailsWith<IllegalArgumentException> {
            convertir(-1.0, Escala.KELVIN, Escala.CELSIUS)
        }
    }

    @Test
    fun `el cero absoluto exacto si vale`() {
        assertCasiIgual(0.0, convertir(0.0, Escala.KELVIN, Escala.KELVIN))
        assertCasiIgual(-273.15, convertir(-273.15, Escala.CELSIUS, Escala.CELSIUS))
    }

    // -- Temperatura -------------------------------------------------------------------

    @Test
    fun `no se puede construir una temperatura imposible`() {
        assertFailsWith<IllegalArgumentException> { Temperatura(-300.0, Escala.CELSIUS) }
    }

    @Test
    fun `en cambia de escala conservando el calor`() {
        val enCelsius = Temperatura(100.0, Escala.CELSIUS)

        assertCasiIgual(212.0, enCelsius.en(Escala.FAHRENHEIT).valor)
        assertEquals(Escala.FAHRENHEIT, enCelsius.en(Escala.FAHRENHEIT).escala)
    }

    @Test
    fun `compara temperaturas de escalas distintas`() {
        assertTrue(Temperatura(21.0, Escala.CELSIUS) < Temperatura(100.0, Escala.FAHRENHEIT))
        assertTrue(Temperatura(100.0, Escala.CELSIUS) > Temperatura(212.0 - 1, Escala.FAHRENHEIT))
    }

    @Test
    fun `sorted funciona entre escalas mezcladas`() {
        val ordenadas = listOf(38.5.celsius, 0.celsius, 77.fahrenheit, 300.kelvin).sorted()

        assertEquals(listOf(0.0, 77.0, 300.0, 38.5), ordenadas.map { it.valor })
    }

    @Test
    fun `equals compara valor Y escala, mismoCalorQue compara solo el calor`() {
        val cero = Temperatura(0.0, Escala.CELSIUS)
        val treintaYDos = Temperatura(32.0, Escala.FAHRENHEIT)

        assertFalse(cero == treintaYDos, "son el mismo calor pero NO el mismo objeto")
        assertTrue(cero mismoCalorQue treintaYDos)
    }

    @Test
    fun `sumar y restar grados conserva la escala`() {
        val partida = Temperatura(21.0, Escala.CELSIUS)

        assertEquals(Temperatura(26.0, Escala.CELSIUS), partida + 5.0)
        assertEquals(Temperatura(16.0, Escala.CELSIUS), partida - 5.0)
    }

    @Test
    fun `la diferencia se expresa en la escala de la primera`() {
        val fiebre = Temperatura(38.5, Escala.CELSIUS)
        val normal = Temperatura(36.5, Escala.CELSIUS)

        assertCasiIgual(2.0, fiebre.diferenciaCon(normal))
    }

    @Test
    fun `la diferencia funciona entre escalas distintas`() {
        // 32 °F son 0 °C, así que la diferencia con 10 °C es 10 grados Celsius.
        assertCasiIgual(10.0, Temperatura(10.0, Escala.CELSIUS).diferenciaCon(32.fahrenheit))
    }

    // -- Las extensiones cómodas -------------------------------------------------------

    @Test
    fun `las propiedades de extension construyen la temperatura correcta`() {
        assertEquals(Temperatura(25.0, Escala.CELSIUS), 25.celsius)
        assertEquals(Temperatura(77.0, Escala.FAHRENHEIT), 77.fahrenheit)
        assertEquals(Temperatura(300.0, Escala.KELVIN), 300.kelvin)
        assertEquals(Temperatura(36.6, Escala.CELSIUS), 36.6.celsius)
    }

    @Test
    fun `toString se lee como una temperatura`() {
        assertEquals("25 °C", Temperatura(25.0, Escala.CELSIUS).toString())
        assertEquals("36.6 °C", Temperatura(36.6, Escala.CELSIUS).toString())
    }
}
