package com.alejandro.infra

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// =====================================================================================
//  El lanzador: que entienda sus comandos.
//
//  Estos tests NO usan los capítulos de verdad: construyen dos capítulos de mentira
//  con demos que sólo apuntan su identificador en una lista. Así el test es
//  instantáneo, no imprime medio repositorio y, sobre todo, no se rompe cada vez que
//  se añade un capítulo nuevo.
//
//  Es el mismo principio de la demo 30.6: un FAKE pequeño en lugar del sistema real.
// =====================================================================================

class LauncherTest {

    /** Lo que han ejecutado las demos falsas, en orden. */
    private val ejecutadas = mutableListOf<String>()

    private lateinit var lanzador: Launcher

    @BeforeTest
    fun preparar() {
        ejecutadas.clear()

        val primero = chapter(1, "Capítulo de prueba", "resumen del primero") {
            demo("demo sobre colecciones", { ejecutadas += "1.1" })
            demo("demo sobre corrutinas", { ejecutadas += "1.2" })
        }
        val segundo = chapter(2, "Otro capítulo", "resumen del segundo") {
            demo("demo única", { ejecutadas += "2.1" })
        }

        lanzador = Launcher(listOf(primero, segundo))
    }

    // -- Ejecución -------------------------------------------------------------------

    @Test
    fun `un numero ejecuta el capitulo entero`() {
        silenciandoLaSalida { lanzador.start(arrayOf("1")) }

        assertEquals(listOf("1.1", "1.2"), ejecutadas)
    }

    @Test
    fun `un identificador con punto ejecuta solo esa demo`() {
        silenciandoLaSalida { lanzador.start(arrayOf("1.2")) }

        assertEquals(listOf("1.2"), ejecutadas)
    }

    @Test
    fun `all ejecuta todas las demos de todos los capitulos`() {
        silenciandoLaSalida { lanzador.start(arrayOf("all")) }

        assertEquals(listOf("1.1", "1.2", "2.1"), ejecutadas)
    }

    @Test
    fun `todo es sinonimo de all`() {
        silenciandoLaSalida { lanzador.start(arrayOf("todo")) }

        assertEquals(listOf("1.1", "1.2", "2.1"), ejecutadas)
    }

    @Test
    fun `los comandos no distinguen mayusculas ni el prefijo de guiones`() {
        silenciandoLaSalida { lanzador.start(arrayOf("--ALL")) }

        assertEquals(listOf("1.1", "1.2", "2.1"), ejecutadas)
    }

    // -- Errores ---------------------------------------------------------------------

    @Test
    fun `avisa cuando el capitulo no existe y no ejecuta nada`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("99")) }

        assertContains(salida, "No existe el capítulo '99'")
        assertTrue(ejecutadas.isEmpty(), "no debería haber ejecutado nada")
    }

    @Test
    fun `avisa cuando la demo no existe y no ejecuta nada`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("1.9")) }

        assertContains(salida, "No existe la demo '1.9'")
        assertTrue(ejecutadas.isEmpty(), "no debería haber ejecutado nada")
    }

    @Test
    fun `un texto que no es un comando se trata como capitulo inexistente`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("patata")) }

        assertContains(salida, "No existe el capítulo")
        assertTrue(ejecutadas.isEmpty())
    }

    // -- Consultas -------------------------------------------------------------------

    @Test
    fun `list imprime todos los capitulos con sus demos`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("list")) }

        assertContains(salida, "Capítulo de prueba")
        assertContains(salida, "Otro capítulo")
        assertContains(salida, "1.1")
        assertContains(salida, "2.1")
        assertContains(salida, "3 demos")
        assertTrue(ejecutadas.isEmpty(), "`list` no debe ejecutar ninguna demo")
    }

    @Test
    fun `search encuentra por el titulo de la demo`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("search", "corrutinas")) }

        assertContains(salida, "1.2")
        assertTrue("2.1" !in salida, "la búsqueda no debería traer la demo 2.1")
    }

    @Test
    fun `search no distingue mayusculas`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("search", "COLECCIONES")) }

        assertContains(salida, "1.1")
    }

    @Test
    fun `search sin texto explica como se usa`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("search")) }

        assertContains(salida, "Uso: search")
    }

    @Test
    fun `search sin resultados lo dice`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("search", "berenjena")) }

        assertContains(salida, "Sin resultados")
    }

    @Test
    fun `help imprime la lista de comandos`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("help")) }

        assertContains(salida, "cómo ejecutar")
        assertContains(salida, "search")
    }

    // -- Singular y plural, que se lee en cada arranque -------------------------------

    @Test
    fun `el resumen dice 1 capitulo en singular`() {
        val uno = Launcher(listOf(chapter(1, "Solo", "uno") { demo("única", { }) }))

        val (_, salida) = capturandoLaSalida { uno.start(arrayOf("list")) }

        assertContains(salida, "1 capítulo.")
    }

    @Test
    fun `el resumen usa el plural con varios capitulos`() {
        val (_, salida) = capturandoLaSalida { lanzador.start(arrayOf("list")) }

        assertContains(salida, "2 capítulos")
    }
}
