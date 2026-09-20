package com.alejandro.c19exceptions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  19.1 · throw, try/catch/finally
//
//  QUÉ ES
//    El mecanismo de excepciones de Kotlin. Es el de la JVM, con una diferencia
//    grande: **no hay excepciones comprobadas** (checked exceptions).
//
//  POR QUÉ IMPORTA
//    Que no haya `throws` en las firmas significa que el compilador NO te avisa de
//    que algo puede fallar. La responsabilidad de decidir qué se captura pasa a ser
//    de diseño, no del lenguaje. Por eso importa tanto el capítulo 19.3.
//
//  ERRORES COMUNES
//    · `catch (e: Exception)` que se traga todo, incluidos los bugs.
//    · Usar excepciones para el flujo normal cuando existe una variante `...OrNull`.
//    · Capturar y volver a lanzar perdiendo la causa original.
// =====================================================================================

/**
 * `throw` y la ausencia de checked exceptions.
 */
fun demoThrow() {
    section("Lanzar una excepción")

    val resultado = try {
        dividir(10, 0)
        "no lanzó"
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("dividir(10, 0)", resultado)
    show("dividir(10, 2)", dividir(10, 2))

    section("`throw` es una EXPRESIÓN de tipo Nothing")

    // Por eso puede aparecer a la derecha de un Elvis o en una rama de un `when`.
    show("con ?: throw", nombreObligatorio("Ana"))
    val conThrow = try {
        nombreObligatorio(null)
    } catch (e: IllegalArgumentException) {
        "lanzó: ${e.message}"
    }
    show("nombreObligatorio(null)", conThrow)

    section("No hay excepciones comprobadas")

    bullet("En Java: `void leer() throws IOException` obliga a capturar o declarar.")
    bullet("En Kotlin no existe `throws`: NADA te obliga a capturar nada.")

    section("Por qué Kotlin las eliminó")

    bullet("En la práctica, las checked exceptions llevaban a dos antipatrones:")
    bullet("  1. `catch (e: IOException) { }` vacío, para callar al compilador.")
    bullet("  2. `throws Exception` en todas las firmas, que no informa de nada.")
    bullet("Y rompen las lambdas: `list.forEach { leerFichero(it) }` no compilaría.")

    section("La consecuencia")

    bullet("Nadie te avisa de qué puede lanzar una función. Hay que documentarlo.")
    bullet("Usa KDoc `@throws` para las excepciones que quien llama debe conocer.")
    bullet("Y para errores esperados, mejor no lanzar: devuelve un tipo (capítulo 11).")
}

/**
 * `try` como expresión.
 */
fun demoTryAsExpression() {
    section("El valor del try")

    val conValor = try {
        "42".toInt()
    } catch (e: NumberFormatException) {
        -1
    }
    show("try { \"42\".toInt() } catch { -1 }", conValor)

    val conFallo = try {
        "abc".toInt()
    } catch (e: NumberFormatException) {
        -1
    }
    show("try { \"abc\".toInt() } catch { -1 }", conFallo)

    section("Vale la última expresión de la rama que se ejecute")

    val conBloques = try {
        val texto = "10"
        val numero = texto.toInt()
        numero * numero          // ← el valor si todo va bien
    } catch (e: NumberFormatException) {
        0                        // ← el valor si falla
    }
    show("con bloques de varias líneas", conBloques)

    section("`finally` NO aporta valor")

    val conFinally = try {
        "7".toInt()
    } catch (e: NumberFormatException) {
        -1
    } finally {
        999      // se ejecuta, pero no cambia el resultado
    }
    show("con finally { 999 }", conFinally)

    section("Varios catch: el primero que encaje")

    listOf("42", "abc", "").forEach { entrada ->
        val salida = try {
            val n = entrada.toInt()
            "entero $n"
        } catch (e: NumberFormatException) {
            "no es un número: '$entrada'"
        } catch (e: Exception) {
            "otro error: ${e::class.simpleName}"
        }
        show("procesar('$entrada')", salida)
    }

    bullet("Ordena los catch de MÁS específico a MÁS general.")
    bullet("Si pones `Exception` primero, los de abajo nunca se ejecutarán.")
}

/**
 * `finally` y la gestión de recursos.
 */
fun demoFinally() {
    section("Se ejecuta pase lo que pase")

    val traza = mutableListOf<String>()

    // Caso feliz
    traza.clear()
    conFinally(fallar = false, traza = traza)
    show("sin fallo", traza.joinToString(" → "))

    // Caso con excepción
    traza.clear()
    runCatching { conFinally(fallar = true, traza = traza) }
    show("con fallo", traza.joinToString(" → "))

    bullet("`finally` corre igual si hay excepción, si hay `return`, y si todo va bien.")

    section("El uso clásico: liberar recursos")

    bullet("var recurso: Recurso? = null")
    bullet("try { recurso = abrir(); usar(recurso) }")
    bullet("finally { recurso?.cerrar() }")

    section("Pero en Kotlin hay algo mejor: `use`")

    // `use` hace exactamente eso, y además no se te olvida.
    val contenido = RecursoSimulado("datos.txt").use { recurso ->
        recurso.leer()
    }
    show("recurso.use { }", contenido)
    show("¿se cerró?", RecursoSimulado.ultimoCerrado)

    bullet("`use` es una extensión sobre Closeable: cierra siempre, incluso si lanza.")
    bullet("Se ve a fondo en los capítulos 25 y 26.")

    section("El antipatrón: `return` dentro de finally")

    // Un `return` en el finally DESCARTA la excepción que estaba propagándose.
    show("conReturnEnFinally()", conReturnEnFinally())
    bullet("Devuelve 'del finally' y la excepción desaparece sin dejar rastro.")
    bullet("Nunca pongas `return` (ni `throw`) dentro de un `finally`.")
}

/**
 * Qué capturar y qué no.
 */
fun demoWhatToCatch() {
    section("La jerarquía")

    bullet("Throwable")
    bullet("├── Error      → fallos de la JVM: OutOfMemory, StackOverflow")
    bullet("└── Exception  → todo lo demás")
    bullet("    └── RuntimeException → IllegalArgument, IllegalState, NPE...")

    section("Qué NO capturar")

    bullet("`Throwable` y `Error`: si la JVM se ha quedado sin memoria, tu catch")
    bullet("no va a arreglarlo, y sí va a ocultar el problema.")
    bullet("`CancellationException`: capturarla rompe la cancelación de corrutinas")
    bullet("(capítulo 28.4). Es la trampa más importante de este capítulo.")

    section("`catch (e: Exception)`: el que hay que mirar con lupa")

    // Captura también los bugs (NPE, IndexOutOfBounds, ClassCast), que deberían
    // llegar hasta arriba para que alguien los vea.
    val tragaTodo = try {
        val lista = listOf(1, 2, 3)
        lista[10]                     // esto es un BUG, no un error esperado
        "no falló"
    } catch (e: Exception) {
        "el catch se tragó un ${e::class.simpleName}"
    }
    show("catch (e: Exception) sobre un bug", tragaTodo)

    bullet("Ese IndexOutOfBounds era un error de programación. Al capturarlo,")
    bullet("el programa sigue con datos incorrectos y nadie se entera.")

    section("La regla")

    bullet("Captura el tipo MÁS ESPECÍFICO que sepas tratar.")
    bullet("Si no sabes qué hacer con una excepción, no la captures.")
    bullet("`catch (e: Exception)` sólo en la frontera del programa (el main, el")
    bullet("manejador global de una petición web), y siempre registrándola.")

    section("Relanzar conservando la causa")

    val conCausa = try {
        try {
            error("fallo de bajo nivel")
        } catch (e: IllegalStateException) {
            throw ErrorDeNegocio("no se pudo completar el pedido", e)
        }
    } catch (e: ErrorDeNegocio) {
        "«${e.message}» causada por «${e.cause?.message}»"
    }
    show("relanzar con cause", conCausa)

    bullet("Pasar la excepción original como `cause` conserva la traza completa.")
    bullet("`throw NuevaExcepcion(mensaje)` a secas la pierde: es un error grave")
    bullet("de diagnóstico que aparece muchísimo en código real.")
}

// -- Las funciones y clases que usan las demos ----------------------------------------------

private fun dividir(a: Int, b: Int): Int {
    require(b != 0) { "no se puede dividir entre cero" }
    return a / b
}

private fun nombreObligatorio(nombre: String?): String {
    val valido = nombre ?: throw IllegalArgumentException("el nombre es obligatorio")
    return valido.uppercase()
}

private fun conFinally(fallar: Boolean, traza: MutableList<String>) {
    traza.add("try")
    try {
        if (fallar) error("fallo simulado")
        traza.add("cuerpo completado")
    } finally {
        traza.add("finally")
    }
}

/** Antipatrón: el `return` del finally descarta la excepción en curso. */
@Suppress("ReturnInsideFinallyBlock")
private fun conReturnEnFinally(): String {
    try {
        error("esta excepción se va a perder")
    } finally {
        return "del finally"
    }
}

private class ErrorDeNegocio(mensaje: String, causa: Throwable?) : Exception(mensaje, causa)

/** Un Closeable de juguete para demostrar `use` sin tocar el disco. */
private class RecursoSimulado(private val nombre: String) : java.io.Closeable {

    fun leer(): String = "contenido de $nombre"

    override fun close() {
        ultimoCerrado = nombre
    }

    companion object {
        var ultimoCerrado: String = "(ninguno)"
            private set
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Invierte el orden de los dos `catch` de demoTryAsExpression y lee el aviso.
//  2. Quita el `return` del finally y comprueba que la excepción vuelve a propagarse.
//  3. Cambia `throw ErrorDeNegocio(msg, e)` por `throw ErrorDeNegocio(msg, null)` y
//     mira qué información pierdes.
//  4. Escribe un `catch (e: Throwable)` y razona qué podrías estar ocultando.
