package com.alejandro.c31exercises

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Ejercicio 9 · comprueba el mini DSL de informes. */
class MiniDslTest {

    private fun informeDeEjemplo(): Informe = informe {
        titulo = "Ventas"
        autor = "Equipo"

        seccion("Resumen") {
            parrafo("Todo bien.")
            lista {
                punto("Norte: +12%")
                punto("Sur: -3%")
            }
        }
        seccion("Detalle") {
            tabla {
                columnas("Región", "Total")
                fila("Norte", "725,50 €")
                fila("Sur", "380,00 €")
            }
        }
    }

    // -- El árbol ------------------------------------------------------------------------

    @Test
    fun `el DSL construye el arbol esperado`() {
        val resultado = informeDeEjemplo()

        assertEquals("Ventas", resultado.titulo)
        assertEquals("Equipo", resultado.autor)
        assertEquals(2, resultado.secciones.size)
        assertContentEquals(listOf("Resumen", "Detalle"), resultado.secciones.map { it.titulo })
    }

    @Test
    fun `los bloques conservan el orden en que se escribieron`() {
        val resultado = informeDeEjemplo()

        val tipos = resultado.secciones.first().bloques.map { it::class.simpleName }
        assertContentEquals(listOf("Parrafo", "Lista"), tipos)
    }

    @Test
    fun `la lista guarda sus puntos`() {
        val lista = informeDeEjemplo().secciones.first().bloques[1]

        assertIs<Bloque.Lista>(lista)
        assertContentEquals(listOf("Norte: +12%", "Sur: -3%"), lista.puntos)
    }

    @Test
    fun `la tabla guarda columnas y filas`() {
        val tabla = informeDeEjemplo().secciones[1].bloques.first()

        assertIs<Bloque.Tabla>(tabla)
        assertContentEquals(listOf("Región", "Total"), tabla.columnas)
        assertEquals(2, tabla.filas.size)
        assertContentEquals(listOf("Norte", "725,50 €"), tabla.filas.first())
    }

    @Test
    fun `una fila corta se rellena hasta el numero de columnas`() {
        val resultado = informe {
            titulo = "t"
            seccion("s") {
                tabla {
                    columnas("A", "B", "C")
                    fila("1")
                }
            }
        }

        val tabla = resultado.secciones.first().bloques.first()
        assertIs<Bloque.Tabla>(tabla)
        assertContentEquals(listOf("1", "", ""), tabla.filas.first())
    }

    @Test
    fun `el autor es opcional`() {
        val sinAutor = informe {
            titulo = "t"
            seccion("s") { parrafo("p") }
        }

        assertNull(sinAutor.autor)
    }

    @Test
    fun `un autor en blanco cuenta como sin autor`() {
        val resultado = informe {
            titulo = "t"
            autor = "   "
            seccion("s") { parrafo("p") }
        }

        assertNull(resultado.autor)
    }

    // -- Validación ------------------------------------------------------------------------

    @Test
    fun `un informe sin titulo no se construye`() {
        val fallo = assertFailsWith<IllegalArgumentException> {
            informe { seccion("s") { parrafo("p") } }
        }
        assertContains(fallo.message.orEmpty(), "titulo")
    }

    @Test
    fun `un informe sin secciones no se construye`() {
        assertFailsWith<IllegalArgumentException> { informe { titulo = "t" } }
    }

    @Test
    fun `una seccion vacia no se construye`() {
        val fallo = assertFailsWith<IllegalArgumentException> {
            informe {
                titulo = "t"
                seccion("vacía") { }
            }
        }
        assertContains(fallo.message.orEmpty(), "vacía")
    }

    @Test
    fun `una lista sin puntos no se construye`() {
        assertFailsWith<IllegalArgumentException> {
            informe {
                titulo = "t"
                seccion("s") { lista { } }
            }
        }
    }

    @Test
    fun `un parrafo o un punto en blanco no se construyen`() {
        assertFailsWith<IllegalArgumentException> {
            informe { titulo = "t"; seccion("s") { parrafo("   ") } }
        }
        assertFailsWith<IllegalArgumentException> {
            informe { titulo = "t"; seccion("s") { lista { punto("") } } }
        }
    }

    @Test
    fun `una tabla sin columnas no se construye`() {
        val fallo = assertFailsWith<IllegalArgumentException> {
            informe {
                titulo = "t"
                seccion("s") { tabla { fila("a") } }
            }
        }
        assertContains(fallo.message.orEmpty(), "columnas")
    }

    @Test
    fun `una fila con mas celdas que columnas no se construye`() {
        val fallo = assertFailsWith<IllegalArgumentException> {
            informe {
                titulo = "t"
                seccion("s") { tabla { columnas("A", "B"); fila("1", "2", "3") } }
            }
        }
        assertContains(fallo.message.orEmpty(), "más celdas")
    }

    // -- Los dos renderizadores sobre el MISMO árbol -------------------------------------------

    @Test
    fun `el texto plano incluye titulo, autor y secciones numeradas`() {
        val texto = informeDeEjemplo().comoTexto()

        assertContains(texto, "VENTAS")
        assertContains(texto, "por Equipo")
        assertContains(texto, "1. Resumen")
        assertContains(texto, "2. Detalle")
    }

    @Test
    fun `el texto plano alinea las columnas de la tabla`() {
        val lineas = informeDeEjemplo().comoTexto().lines()

        val cabecera = lineas.first { it.startsWith("Región") }
        val primeraFila = lineas.first { it.startsWith("Norte") }

        assertEquals(
            cabecera.indexOf('|'),
            primeraFila.indexOf('|'),
            "las barras deberían estar en la misma columna",
        )
    }

    @Test
    fun `el markdown usa la sintaxis de markdown`() {
        val markdown = informeDeEjemplo().comoMarkdown()

        assertContains(markdown, "# Ventas")
        assertContains(markdown, "_por Equipo_")
        assertContains(markdown, "## Resumen")
        assertContains(markdown, "- Norte: +12%")
        assertContains(markdown, "| Región | Total |")
        assertContains(markdown, "| --- | --- |")
    }

    @Test
    fun `los dos formatos salen del mismo informe sin tocarlo`() {
        val resultado = informeDeEjemplo()

        val texto = resultado.comoTexto()
        val markdown = resultado.comoMarkdown()

        assertTrue(texto.isNotBlank() && markdown.isNotBlank())
        assertTrue(texto != markdown)
        // Y el árbol sigue intacto tras renderizar dos veces.
        assertEquals(2, resultado.secciones.size)
    }

    @Test
    fun `un informe sin autor no imprime la linea del autor`() {
        val sinAutor = informe {
            titulo = "t"
            seccion("s") { parrafo("p") }
        }

        assertTrue("por " !in sinAutor.comoTexto())
        assertTrue("_por" !in sinAutor.comoMarkdown())
    }
}
