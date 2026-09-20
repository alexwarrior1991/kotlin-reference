package com.alejandro.infra

import com.alejandro.chapters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// =====================================================================================
//  La red de seguridad del registro de capítulos.
//
//  No prueba Kotlin: prueba que este repositorio es coherente. Con más de treinta
//  capítulos, el error más probable es olvidarse de registrar uno en `Main.kt` o
//  copiar un `CNN_00Index.kt` y no cambiarle el número. Estos tests lo detectan al
//  instante y cuestan cuatro líneas cada uno.
// =====================================================================================

class RegistryTest {

    @Test
    fun `los capitulos van del 1 al ultimo, sin huecos ni repetidos`() {
        val numeros = chapters.map { it.number }

        assertEquals(
            (1..chapters.size).toList(),
            numeros,
            "la lista `chapters` de Main.kt debe ir del 1 al ${chapters.size} en orden",
        )
    }

    @Test
    fun `ningun capitulo esta vacio`() {
        val vacios = chapters.filter { it.demos.isEmpty() }

        assertTrue(vacios.isEmpty(), "capítulos sin demos: ${vacios.map { it.number }}")
    }

    @Test
    fun `los identificadores de demo son unicos en todo el registro`() {
        val repetidos = chapters
            .flatMap { it.demos }
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        assertTrue(repetidos.isEmpty(), "identificadores de demo repetidos: $repetidos")
    }

    @Test
    fun `el id de cada demo empieza por el numero de su capitulo`() {
        val descolocadas = chapters.flatMap { capitulo ->
            capitulo.demos.filterNot { it.id.startsWith("${capitulo.number}.") }
                .map { "${it.id} (está en el capítulo ${capitulo.number})" }
        }

        assertTrue(descolocadas.isEmpty(), "demos con identificador incoherente: $descolocadas")
    }

    @Test
    fun `los ids de demo van correlativos dentro de cada capitulo`() {
        chapters.forEach { capitulo ->
            val esperados = capitulo.demos.indices.map { "${capitulo.number}.${it + 1}" }
            assertEquals(esperados, capitulo.demos.map { it.id }, "capítulo ${capitulo.number}")
        }
    }

    @Test
    fun `ningun nombre, resumen ni titulo esta en blanco`() {
        chapters.forEach { capitulo ->
            assertTrue(capitulo.name.isNotBlank(), "el capítulo ${capitulo.number} no tiene nombre")
            assertTrue(capitulo.summary.isNotBlank(), "el capítulo ${capitulo.number} no tiene resumen")

            capitulo.demos.forEach { demo ->
                assertTrue(demo.title.isNotBlank(), "la demo ${demo.id} no tiene título")
            }
        }
    }

    @Test
    fun `el identificador del capitulo se escribe con dos digitos`() {
        assertEquals("01", chapters.first().id)
        assertEquals(chapters.size.toString().padStart(2, '0'), chapters.last().id)
    }

    @Test
    fun `hay demos suficientes para llamar a esto una referencia`() {
        // Un guardián flojo a propósito: sólo salta si se borra medio repositorio.
        val total = chapters.sumOf { it.demos.size }

        assertTrue(total > 200, "sólo hay $total demos registradas")
    }

    @Test
    fun `find devuelve la demo pedida y null si no existe`() {
        val capitulo = chapters.first()
        val primera = capitulo.demos.first()

        assertEquals(primera, capitulo.find(primera.id))
        assertEquals(null, capitulo.find("${capitulo.number}.999"))
    }
}
