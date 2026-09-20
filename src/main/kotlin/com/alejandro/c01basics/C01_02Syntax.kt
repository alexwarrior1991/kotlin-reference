package com.alejandro.c01basics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  1.2 · Expresiones, sentencias e inferencia de tipos
//
//  QUÉ ES
//    Dos ideas que atraviesan todo el lenguaje: en Kotlin casi todo DEVUELVE un valor,
//    y casi nunca hace falta escribir el tipo porque el compilador lo deduce.
//
//  POR QUÉ IMPORTA
//    Explica por qué Kotlin no tiene operador ternario, por qué `if`, `when` y `try`
//    se pueden asignar a una variable, y por qué el código se lee tan corto sin perder
//    seguridad de tipos: la inferencia es estática, no dinámica.
//
//  ERRORES COMUNES
//    · Pensar que `val x = 1` es "sin tipo". Tiene tipo `Int`, fijo y comprobado.
//    · Intentar `while ((linea = leer()) != null)`: la asignación NO es una expresión.
//    · Omitir el tipo en el retorno de una función pública, donde conviene escribirlo.
// =====================================================================================

/**
 * Expresión vs sentencia.
 *
 * Una **expresión** produce un valor (y por tanto se puede asignar, devolver o pasar
 * como argumento). Una **sentencia** sólo tiene efecto. En Kotlin la lista de cosas
 * que son expresiones es mucho más larga que en Java.
 */
fun demoExpressionsVsStatements() {
    val a = 7
    val b = 12

    section("`if` es una expresión: por eso Kotlin no tiene operador ternario")

    // En Java: int maximo = (a > b) ? a : b;
    // En Kotlin el `if` ya devuelve valor, así que el ternario sobraría.
    val maximo = if (a > b) a else b
    show("if (a > b) a else b", maximo)

    // Cuando se usa como expresión, el `else` es OBLIGATORIO: si faltara, habría
    // un camino sin valor que devolver.
    val signo = if (a - b >= 0) "positivo" else "negativo"
    show("signo de (a - b)", signo)

    // Usado como sentencia (sin asignar), el `else` sí puede faltar.
    if (a < b) {
        bullet("a es menor que b (esto es un `if` usado como sentencia)")
    }

    section("`when` y `try` también son expresiones")

    val categoria = when {
        a < 5 -> "pequeño"
        a < 10 -> "mediano"
        else -> "grande"
    }
    show("when { ... }", categoria)

    // `try` devuelve el valor del bloque que se ejecute. Lo veremos a fondo en el
    // capítulo 19; aquí sólo importa que es una expresión más.
    val numero = try {
        "42".toInt()
    } catch (e: NumberFormatException) {
        -1
    }
    show("try { \"42\".toInt() } catch { -1 }", numero)

    val fallido = try {
        "no soy un número".toInt()
    } catch (e: NumberFormatException) {
        -1
    }
    show("try { \"no soy un número\".toInt() } catch { -1 }", fallido)

    section("Lo que NO es una expresión")

    // La asignación no devuelve valor. Esto, idiomático en Java, aquí no compila:
    //
    //     var linea: String? = null
    //     while ((linea = leerLinea()) != null) { ... }   // ERROR
    //
    // Es una decisión deliberada: elimina de raíz el clásico `if (x = 5)` escrito
    // por error en lugar de `if (x == 5)`.
    bullet("La asignación (`x = 1`) es una sentencia, no una expresión.")
    bullet("Los bucles `for`, `while` y `do-while` tampoco devuelven valor.")

    section("Unit: el 'void' de Kotlin, que sí es un valor")

    // Una función que no devuelve nada útil devuelve `Unit`, un objeto real con un
    // único valor. Por eso `println(...)` se puede asignar, aunque no sirva de mucho.
    val resultadoDePrintln = println("(esta línea la imprime println dentro de la demo)")
    show("tipo devuelto por println()", resultadoDePrintln::class.simpleName)

    // Escribir `: Unit` es legal pero redundante; se omite siempre.
    bullet("fun saludar(): Unit { ... }  ==  fun saludar() { ... }")
}

/**
 * Inferencia de tipos: el compilador deduce el tipo, pero el tipo existe y es fijo.
 */
fun demoTypeInference() {
    section("Qué tipo deduce el compilador de cada literal")

    val entero = 1
    val largo = 1L
    val decimal = 1.0
    val flotante = 1.0f
    val caracter = 'K'
    val texto = "hola"
    val logico = true

    show("val entero = 1", entero::class.simpleName)
    show("val largo = 1L", largo::class.simpleName)
    show("val decimal = 1.0", decimal::class.simpleName)
    show("val flotante = 1.0f", flotante::class.simpleName)
    show("val caracter = 'K'", caracter::class.simpleName)
    show("val texto = \"hola\"", texto::class.simpleName)
    show("val logico = true", logico::class.simpleName)

    // Ojo con éste: un decimal sin sufijo es Double, NO Float. Es la fuente número uno
    // de sorpresas al venir de otros lenguajes.
    bullet("1.0 es Double. Para Float hace falta el sufijo: 1.0f")

    section("Un matiz bonito: el literal se adapta al tipo esperado")

    // El literal `1` se acepta donde se espera un Long, un Short o un Byte,
    // siempre que quepa. El compilador lo resuelve al compilar, sin conversión.
    val explicitoLargo: Long = 1
    val explicitoByte: Byte = 1
    show("val explicitoLargo: Long = 1", explicitoLargo::class.simpleName)
    show("val explicitoByte: Byte = 1", explicitoByte::class.simpleName)

    // Pero eso vale sólo para LITERALES. Una variable Int no se convierte sola:
    //
    //     val unInt = 1
    //     val noCompila: Long = unInt   // ERROR: Type mismatch
    //     val siCompila: Long = unInt.toLong()
    //
    // Kotlin no hace conversiones numéricas implícitas. Capítulo 2.2.
    bullet("Literal → sí se adapta. Variable → hay que llamar a .toLong(), .toByte()...")

    section("Inferencia con genéricos")

    val numeros = listOf(1, 2, 3)
    show("listOf(1, 2, 3)", "List<Int> → $numeros")

    // Cuando no hay elementos de los que deducir nada, hay que decirlo explícitamente.
    val vacia = listOf<String>()
    show("listOf<String>()", "List<String> → $vacia")

    // También se puede forzar un supertipo más ancho del que se deduciría.
    val comoNumeros: List<Number> = listOf(1, 2.0, 3L)
    show("listOf<Number>(1, 2.0, 3L)", comoNumeros)

    section("Cuándo NO conviene dejar que infiera")

    bullet("En el retorno de funciones públicas: el tipo es parte del contrato.")
    bullet("Cuando el tipo inferido sorprende (Double vs Float, Int vs Long).")
    bullet("Cuando quieres exponer `List` aunque por dentro construyas un `MutableList`.")
}

/**
 * Convenciones de nombrado. No son obligatorias para el compilador, pero sí para
 * cualquiera que lea tu código (y para el formateador de IntelliJ).
 */
fun demoNamingConventions() {
    section("Las reglas de la guía oficial")

    bullet("Paquetes:        todo en minúsculas, sin guiones bajos  → com.alejandro.c01basics")
    bullet("Clases e interfaces: PascalCase                         → CarritoDeCompra")
    bullet("Funciones y variables: camelCase                        → calcularTotal")
    bullet("Constantes `const val`: MAYUSCULAS_CON_GUIONES          → IVA_GENERAL")
    bullet("Ficheros: PascalCase y, si contienen una sola clase, su nombre")

    section("Nombres entre acentos graves")

    // Kotlin permite identificadores con espacios y signos si los rodeas de backticks.
    // Fuera de los tests esto es una rareza; en los tests, en cambio, es lo idiomático
    // porque el nombre del test se lee como una frase (capítulo 30).
    show("resultado", `suma dos números`(2, 3))
    bullet("Úsalo en tests: fun `el carrito vacío suma cero`() { ... }")
    bullet("Fuera de los tests, evítalo: complica la interoperabilidad con Java.")
}

/**
 * Función con nombre entre acentos graves. Compila igual que cualquier otra; sólo
 * cambia cómo se escribe su nombre.
 */
@Suppress("FunctionName")
fun `suma dos números`(a: Int, b: Int): Int = a + b

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `else` de `val maximo = if (a > b) a else b` y lee el error.
//  2. Cambia `val decimal = 1.0` por `val decimal: Float = 1.0` y mira qué se queja.
//  3. Descomenta `val noCompila: Long = unInt` y comprueba que Kotlin no convierte solo.
