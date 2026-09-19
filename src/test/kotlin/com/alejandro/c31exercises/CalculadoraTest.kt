package com.alejandro.c31exercises

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Ejercicio 1 · comprueba la solución propuesta de la calculadora. */
class CalculadoraTest {

    private fun valorDe(resultado: ResultadoCalculo): Double {
        assertIs<ResultadoCalculo.Ok>(resultado)
        return resultado.valor
    }

    private fun motivoDe(resultado: ResultadoCalculo): String {
        assertIs<ResultadoCalculo.Error>(resultado)
        return resultado.motivo
    }

    // -- calcular ---------------------------------------------------------------------

    @Test
    fun `las cuatro operaciones basicas`() {
        assertEquals(9.0, valorDe(calcular(6.0, '+', 3.0)))
        assertEquals(3.0, valorDe(calcular(6.0, '-', 3.0)))
        assertEquals(18.0, valorDe(calcular(6.0, '*', 3.0)))
        assertEquals(2.0, valorDe(calcular(6.0, '/', 3.0)))
    }

    @Test
    fun `el resto y la potencia`() {
        assertEquals(1.0, valorDe(calcular(7.0, '%', 3.0)))
        assertEquals(1024.0, valorDe(calcular(2.0, '^', 10.0)))
    }

    @Test
    fun `dividir entre cero es un error, no infinito`() {
        // En Double, 1.0 / 0.0 daría Infinity sin avisar. Aquí no.
        assertContains(motivoDe(calcular(1.0, '/', 0.0)), "dividir entre cero")
    }

    @Test
    fun `el resto de cero tambien es un error`() {
        assertContains(motivoDe(calcular(1.0, '%', 0.0)), "cero")
    }

    @Test
    fun `un operador desconocido no lanza`() {
        assertContains(motivoDe(calcular(1.0, '?', 2.0)), "operador desconocido")
    }

    @Test
    fun `una potencia que se desborda es un error, no Infinity`() {
        assertIs<ResultadoCalculo.Error>(calcular(10.0, '^', 400.0))
    }

    @Test
    fun `los negativos funcionan`() {
        assertEquals(-5.0, valorDe(calcular(-2.0, '-', 3.0)))
        assertEquals(6.0, valorDe(calcular(-2.0, '*', -3.0)))
    }

    // -- evaluarExpresion -------------------------------------------------------------

    @Test
    fun `evalua una expresion con espacios`() {
        assertEquals(7.0, valorDe(evaluarExpresion("3 + 4")))
    }

    @Test
    fun `no le molestan los espacios de sobra`() {
        assertEquals(2.5, valorDe(evaluarExpresion("  10   /  4 ")))
    }

    @Test
    fun `tampoco le hacen falta espacios`() {
        assertEquals(256.0, valorDe(evaluarExpresion("2^8")))
    }

    @Test
    fun `una expresion vacia es un error`() {
        assertContains(motivoDe(evaluarExpresion("")), "vacía")
        assertContains(motivoDe(evaluarExpresion("    ")), "vacía")
    }

    @Test
    fun `sin operador es un error`() {
        assertContains(motivoDe(evaluarExpresion("42")), "operador")
    }

    @Test
    fun `falta el segundo operando`() {
        assertContains(motivoDe(evaluarExpresion("3 +")), "segundo operando")
    }

    @Test
    fun `un operando que no es numero es un error`() {
        assertContains(motivoDe(evaluarExpresion("hola * 2")), "no es un número")
    }

    @Test
    fun `tres operandos todavia no estan soportados`() {
        // Documenta una LIMITACIÓN conocida: quitar este test es el primer paso
        // de la variante difícil nº 1.
        assertIs<ResultadoCalculo.Error>(evaluarExpresion("1 + 2 + 3"))
    }

    @Test
    fun `los errores de calcular llegan a traves de evaluarExpresion`() {
        assertContains(motivoDe(evaluarExpresion("5 / 0")), "dividir entre cero")
    }
}
