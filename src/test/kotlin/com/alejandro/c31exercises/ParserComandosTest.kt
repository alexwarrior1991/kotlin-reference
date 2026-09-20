package com.alejandro.c31exercises

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Ejercicio 6 · comprueba la solución propuesta del parser de comandos. */
class ParserComandosTest {

    private lateinit var consola: ConsolaDeTareas

    @BeforeTest
    fun preparar() {
        consola = ConsolaDeTareas()
    }

    private fun comandoDe(linea: String): Comando {
        val resultado = parsearComando(linea)
        assertIs<ResultadoParseo.Ok>(resultado, "«$linea» debería parsearse")
        return resultado.comando
    }

    private fun errorDe(linea: String): String {
        val resultado = parsearComando(linea)
        assertIs<ResultadoParseo.Error>(resultado, "«$linea» debería fallar")
        return resultado.mensaje
    }

    // -- trocear ------------------------------------------------------------------------

    @Test
    fun `trocear separa por espacios`() {
        assertContentEquals(listOf("listar", "--todas"), trocear("listar --todas"))
    }

    @Test
    fun `trocear respeta el texto entre comillas`() {
        assertContentEquals(
            listOf("añadir", "Comprar pan", "--prioridad", "alta"),
            trocear("""añadir "Comprar pan" --prioridad alta"""),
        )
    }

    @Test
    fun `trocear ignora los espacios de sobra`() {
        assertContentEquals(listOf("listar", "--todas"), trocear("   listar     --todas   "))
    }

    @Test
    fun `trocear de una linea vacia da una lista vacia`() {
        assertTrue(trocear("").isEmpty())
        assertTrue(trocear("     ").isEmpty())
    }

    @Test
    fun `sin comillas, cada palabra es un trozo`() {
        assertContentEquals(listOf("añadir", "Comprar", "pan"), trocear("añadir Comprar pan"))
    }

    // -- parsearComando -----------------------------------------------------------------

    @Test
    fun `una linea vacia es el comando Vacio, no un error`() {
        assertEquals(Comando.Vacio, comandoDe(""))
        assertEquals(Comando.Vacio, comandoDe("   "))
    }

    @Test
    fun `anadir con prioridad`() {
        assertEquals(
            Comando.Anadir("Comprar pan", Prioridad.ALTA),
            comandoDe("""añadir "Comprar pan" --prioridad alta"""),
        )
    }

    @Test
    fun `anadir sin prioridad usa NORMAL`() {
        assertEquals(
            Comando.Anadir("Regar", Prioridad.NORMAL),
            comandoDe("""añadir "Regar""""),
        )
    }

    @Test
    fun `anadir tambien se acepta escrito sin ene`() {
        assertIs<Comando.Anadir>(comandoDe("""anadir "Sin eñe""""))
    }

    @Test
    fun `listar con y sin todas`() {
        assertEquals(Comando.Listar(incluirHechas = false), comandoDe("listar"))
        assertEquals(Comando.Listar(incluirHechas = true), comandoDe("listar --todas"))
    }

    @Test
    fun `hecha y borrar con su numero`() {
        assertEquals(Comando.MarcarHecha(3), comandoDe("hecha 3"))
        assertEquals(Comando.Borrar(7), comandoDe("borrar 7"))
    }

    @Test
    fun `los comandos no distinguen mayusculas`() {
        assertEquals(Comando.Ayuda, comandoDe("AYUDA"))
        assertEquals(Comando.Listar(incluirHechas = false), comandoDe("LiStAr"))
    }

    // -- Errores ------------------------------------------------------------------------

    @Test
    fun `anadir sin texto es un error`() {
        assertContains(errorDe("añadir"), "necesita el texto")
    }

    @Test
    fun `una prioridad desconocida es un error que dice las validas`() {
        val mensaje = errorDe("""añadir "x" --prioridad urgentisima""")

        assertContains(mensaje, "prioridad desconocida")
        assertContains(mensaje, "alta")
    }

    @Test
    fun `la opcion de prioridad sin valor es un error`() {
        assertContains(errorDe("""añadir "x" --prioridad"""), "necesita un valor")
    }

    @Test
    fun `hecha sin numero es un error`() {
        assertContains(errorDe("hecha"), "necesita el número")
    }

    @Test
    fun `hecha con algo que no es un numero es un error`() {
        assertContains(errorDe("hecha tres"), "no es un número")
    }

    @Test
    fun `un numero de tarea no positivo es un error`() {
        assertContains(errorDe("borrar 0"), "positivo")
        assertContains(errorDe("borrar -3"), "positivo")
    }

    @Test
    fun `un comando desconocido sugiere el parecido`() {
        assertContains(errorDe("""aladir "x""""), "¿Querías decir «añadir»?")
    }

    @Test
    fun `un comando sin parecido remite a la ayuda`() {
        assertContains(errorDe("bailar"), "ayuda")
    }

    // -- La consola ----------------------------------------------------------------------

    @Test
    fun `anadir numera las tareas desde uno`() {
        assertContains(consola.ejecutar("""añadir "Primera""""), "#1")
        assertContains(consola.ejecutar("""añadir "Segunda""""), "#2")

        assertEquals(listOf("Primera", "Segunda"), consola.tareas().map { it.texto })
    }

    @Test
    fun `listar oculta las hechas salvo que se pidan todas`() {
        consola.ejecutar("""añadir "Primera"""")
        consola.ejecutar("""añadir "Segunda"""")
        consola.ejecutar("hecha 1")

        assertTrue("Primera" !in consola.ejecutar("listar"))
        assertContains(consola.ejecutar("listar --todas"), "Primera")
    }

    @Test
    fun `marcar hecha no borra la tarea`() {
        consola.ejecutar("""añadir "Primera"""")

        consola.ejecutar("hecha 1")

        assertEquals(1, consola.tareas().size)
        assertTrue(consola.tareas().first().hecha)
    }

    @Test
    fun `borrar quita la tarea de verdad`() {
        consola.ejecutar("""añadir "Primera"""")
        consola.ejecutar("""añadir "Segunda"""")

        assertContains(consola.ejecutar("borrar 1"), "borrada")

        assertEquals(listOf("Segunda"), consola.tareas().map { it.texto })
    }

    @Test
    fun `operar sobre una tarea que no existe avisa sin romper nada`() {
        assertContains(consola.ejecutar("hecha 99"), "no existe")
        assertContains(consola.ejecutar("borrar 99"), "no existe")
        assertTrue(consola.tareas().isEmpty())
    }

    @Test
    fun `listar sin tareas lo dice`() {
        assertContains(consola.ejecutar("listar"), "no hay tareas")
    }

    @Test
    fun `una linea vacia no hace nada ni imprime nada`() {
        assertEquals("", consola.ejecutar(""))
        assertTrue(consola.tareas().isEmpty())
    }

    @Test
    fun `los identificadores no se reutilizan al borrar`() {
        consola.ejecutar("""añadir "Primera"""")
        consola.ejecutar("borrar 1")

        assertContains(consola.ejecutar("""añadir "Segunda""""), "#2")
    }
}
