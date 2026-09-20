package com.alejandro.c31exercises

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Ejercicio 2 · comprueba la solución propuesta del validador de emails. */
class ValidadorEmailsTest {

    private fun motivosDe(texto: String): List<String> {
        val resultado = validarEmail(texto)
        assertIs<ResultadoEmail.Invalido>(resultado, "«$texto» debería ser inválido")
        return resultado.motivos
    }

    // -- Casos válidos -----------------------------------------------------------------

    @Test
    fun `acepta direcciones normales`() {
        listOf(
            "ana@ejemplo.com",
            "ana.garcia@correo.ejemplo.es",
            "a@b.io",
            "usuario+etiqueta@ejemplo.org",
            "n1234@dominio.info",
        ).forEach { assertTrue(esEmailValido(it), "«$it» debería ser válido") }
    }

    // -- Casos inválidos ---------------------------------------------------------------

    @Test
    fun `rechaza la cadena vacia o en blanco`() {
        assertContentEquals(listOf("no puede estar en blanco"), motivosDe(""))
        assertContentEquals(listOf("no puede estar en blanco"), motivosDe("   "))
    }

    @Test
    fun `rechaza si falta la arroba`() {
        assertContentEquals(listOf("le falta la arroba"), motivosDe("sinarroba.com"))
    }

    @Test
    fun `rechaza si hay mas de una arroba`() {
        assertContentEquals(listOf("sólo puede haber una arroba"), motivosDe("dos@@arrobas.com"))
    }

    @Test
    fun `rechaza si falta la parte local`() {
        assertContentEquals(listOf("falta la parte anterior a la arroba"), motivosDe("@ejemplo.com"))
    }

    @Test
    fun `rechaza un dominio sin punto`() {
        assertContentEquals(listOf("el dominio necesita al menos un punto"), motivosDe("ana@sinpunto"))
    }

    @Test
    fun `rechaza una extension de una sola letra`() {
        assertEquals(1, motivosDe("ana@ejemplo.c").size)
        assertTrue(motivosDe("ana@ejemplo.c").first().contains("demasiado corta"))
    }

    @Test
    fun `rechaza los espacios`() {
        assertContentEquals(listOf("no puede contener espacios"), motivosDe("ana garcia@ejemplo.com"))
    }

    @Test
    fun `rechaza dos puntos seguidos`() {
        assertContentEquals(listOf("no puede tener dos puntos seguidos"), motivosDe("ana@ejemplo..com"))
    }

    @Test
    fun `rechaza un dominio que empieza o acaba en punto`() {
        assertTrue(motivosDe("ana@.com").any { "empezar por punto" in it })
        assertTrue(motivosDe("ana@ejemplo.").any { "acabar en punto" in it })
    }

    @Test
    fun `acumula varios motivos a la vez`() {
        // Sin arroba Y con espacio: los dos motivos, no sólo el primero.
        val motivos = motivosDe("ana garcia .com")
        assertEquals(2, motivos.size, "esperaba dos motivos, fueron: $motivos")
    }

    @Test
    fun `no evalua las reglas del dominio si no hay arroba`() {
        // Sería ruido: sin arroba no se sabe cuál es el dominio.
        val motivos = motivosDe("sinarroba")
        assertFalse(motivos.any { "dominio" in it }, "no debería hablar del dominio: $motivos")
    }

    // -- normalizarEmail ---------------------------------------------------------------

    @Test
    fun `normalizar recorta y pasa a minusculas`() {
        assertEquals("ana@ejemplo.com", normalizarEmail("  ANA@Ejemplo.COM  "))
    }

    @Test
    fun `normalizar no cambia lo que ya estaba bien`() {
        assertEquals("ana@ejemplo.com", normalizarEmail("ana@ejemplo.com"))
    }

    // -- agruparPorDominio -------------------------------------------------------------

    @Test
    fun `agrupa por dominio descartando los invalidos`() {
        val agrupado = agruparPorDominio(
            listOf(
                "ana@ejemplo.com",
                "luis@ejemplo.com",
                "marta@otra.es",
                "sinarroba.com",
                "  PEDRO@Ejemplo.com  ",
            ),
        )

        assertEquals(setOf("ejemplo.com", "otra.es"), agrupado.keys)
        assertContentEquals(
            listOf("ana@ejemplo.com", "luis@ejemplo.com", "pedro@ejemplo.com"),
            agrupado.getValue("ejemplo.com"),
        )
    }

    @Test
    fun `agrupar una lista vacia devuelve un mapa vacio`() {
        assertTrue(agruparPorDominio(emptyList()).isEmpty())
    }

    @Test
    fun `agrupar solo invalidos devuelve un mapa vacio`() {
        assertTrue(agruparPorDominio(listOf("a", "b@", "@c")).isEmpty())
    }
}
