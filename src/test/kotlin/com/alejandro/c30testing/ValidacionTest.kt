package com.alejandro.c30testing

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// =====================================================================================
//  Tests del validador de contraseñas.
//
//  Aquí se ven dos asertos que no se usan a diario pero que valen oro con jerarquías
//  selladas: `assertIs<T>` (comprueba el tipo Y hace smart cast) y
//  `assertContentEquals` (compara listas elemento a elemento).
// =====================================================================================

class ValidacionTest {

    @Test
    fun `una contrasena que cumple todo es valida`() {
        val resultado = validarContrasena("C0ntrasena!")

        assertIs<ResultadoValidacion.Valida>(resultado)
    }

    @Test
    fun `una contrasena corta dice que le falta longitud`() {
        val resultado = validarContrasena("A1b!")

        // `assertIs` deja `resultado` con el tipo concreto a partir de aquí.
        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertContentEquals(listOf("debe tener al menos 8 caracteres"), resultado.motivos)
    }

    @Test
    fun `acumula todos los motivos, no solo el primero`() {
        val resultado = validarContrasena("abc")

        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertEquals(4, resultado.motivos.size, "debería fallar por las cuatro reglas")
    }

    @Test
    fun `detecta que falta un digito`() {
        val resultado = validarContrasena("Contrasena!")

        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertContentEquals(listOf("debe contener algún dígito"), resultado.motivos)
    }

    @Test
    fun `detecta que falta una mayuscula`() {
        val resultado = validarContrasena("c0ntrasena!")

        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertContentEquals(listOf("debe contener alguna mayúscula"), resultado.motivos)
    }

    @Test
    fun `detecta que falta un simbolo`() {
        val resultado = validarContrasena("C0ntrasena")

        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertContentEquals(listOf("debe contener algún símbolo"), resultado.motivos)
    }

    @Test
    fun `la cadena vacia falla por las cuatro reglas`() {
        val resultado = validarContrasena("")

        assertIs<ResultadoValidacion.Invalida>(resultado)
        assertEquals(4, resultado.motivos.size)
    }

    @Test
    fun `acepta simbolos poco habituales`() {
        // Un espacio también es "ni letra ni dígito": conviene dejarlo por escrito
        // para que nadie lo cambie sin darse cuenta.
        assertIs<ResultadoValidacion.Valida>(validarContrasena("Contrasena 1"))
    }

    @Test
    fun `la longitud se mide en caracteres, no en bytes`() {
        // Ocho caracteres con acentos siguen siendo ocho caracteres.
        val resultado = validarContrasena("Ñandú1é!")

        assertTrue(
            resultado is ResultadoValidacion.Valida,
            "debería ser válida, pero fue: $resultado",
        )
    }
}
