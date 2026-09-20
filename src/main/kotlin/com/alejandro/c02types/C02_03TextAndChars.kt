package com.alejandro.c02types

import com.alejandro.infra.bullet
import com.alejandro.infra.quoted
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  2.3 · String, Char y plantillas de cadena
//
//  QUÉ ES
//    El tipo texto de Kotlin, sus plantillas (`"Hola, $nombre"`), las cadenas sin
//    escapes (`"""..."""`) y el tipo Char.
//
//  POR QUÉ IMPORTA
//    Las plantillas sustituyen a la concatenación y a String.format en el 95% de los
//    casos, y las cadenas en crudo hacen legibles los JSON, SQL y expresiones
//    regulares incrustados en el código.
//
//  ERRORES COMUNES
//    · Concatenar dentro de un bucle: crea una cadena nueva en cada vuelta.
//    · Olvidar `trimIndent()` y arrastrar la indentación del código al texto.
//    · Confundir `==` (contenido) con `===` (misma referencia).
//    · Creer que `isEmpty()` y `isBlank()` son lo mismo: "   " no está vacío.
// =====================================================================================

/**
 * Plantillas de cadena: la forma idiomática de construir texto.
 */
fun demoStringTemplates() {
    val nombre = "Alejandro"
    val edad = 34

    section("Concatenación vs plantilla")

    show("con +", "Hola, " + nombre + ". Tienes " + edad + " años.")
    show("con plantilla", "Hola, $nombre. Tienes $edad años.")

    section("\$variable y \${expresión}")

    // `$nombre` funciona para un identificador suelto.
    // Para cualquier otra cosa (llamadas, operaciones, propiedades) hacen falta llaves.
    show("\$nombre", "$nombre")
    show("\${nombre.uppercase()}", "${nombre.uppercase()}")
    show("\${edad + 1}", "${edad + 1}")
    show("\${nombre.length}", "${nombre.length}")

    // Sin llaves, `$nombre.length` se lee como (valor de nombre) + el texto ".length"
    show("\$nombre.length  (sin llaves)", "$nombre.length")

    section("Un dólar literal")

    // `$` empieza una plantilla, así que para imprimirlo hay que escaparlo.
    show("con barra invertida", "Precio: 100\$")
    show("con \${'\$'}", "Precio: 100${'$'}")

    section("Dentro de las plantillas cabe casi todo")

    val precios = listOf(10, 25, 7)
    show("una expresión if", "El más caro cuesta ${if (precios.isEmpty()) 0 else precios.max()}")

    // Detalle de sintaxis: dentro de `${ }` se pueden escribir cadenas con comillas
    // normales, sin escaparlas. El compilador sabe dónde acaba cada una.
    show("una llamada encadenada", "Ordenados: ${precios.sorted().joinToString(" < ")}")
}

/**
 * Cadenas en crudo: tres comillas, sin escapes.
 */
fun demoRawStrings() {
    section("El problema que resuelven")

    // Con comillas simples hay que escapar cada comilla y cada barra:
    val jsonEscapado = "{\"nombre\": \"Ana\", \"ruta\": \"C:\\\\datos\"}"
    show("cadena normal", jsonEscapado)

    // Con tres comillas, el texto va tal cual:
    val jsonCrudo = """{"nombre": "Ana", "ruta": "C:\datos"}"""
    show("cadena en crudo", jsonCrudo)

    section("Varias líneas y trimIndent()")

    // Sin trimIndent, la indentación del CÓDIGO acaba dentro del texto.
    val sinLimpiar = """
        SELECT id, nombre
        FROM usuarios
        WHERE activo = true
    """
    // Mostramos los saltos de línea como "\n" para que se vea el margen que arrastra.
    show("sin trimIndent", quoted(sinLimpiar.take(30).replace("\n", "\\n") + "..."))

    // trimIndent quita el margen común a todas las líneas y las líneas en blanco
    // inicial y final. Es lo que quieres el 90% de las veces.
    val limpia = """
        SELECT id, nombre
        FROM usuarios
        WHERE activo = true
    """.trimIndent()
    println()
    println(limpia)

    section("trimMargin(): cuando quieres controlar el margen")

    // Útil si alguna línea debe ir indentada a propósito.
    val conMargen = """
        |Informe
        |  · primera línea indentada
        |  · segunda
        |Fin
    """.trimMargin()
    println()
    println(conMargen)

    bullet("trimIndent() usa la indentación mínima común como margen.")
    bullet("trimMargin() usa un carácter marcador, por defecto '|'.")

    section("Interpolación dentro de una cadena en crudo")

    val usuario = "ana"
    // Las plantillas SÍ funcionan dentro de """...""".
    println()
    println(
        """
        Usuario: $usuario
        Saldo:   ${'$'}1.250,00
        """.trimIndent()
    )
    bullet("No hay escapes en crudo, así que un \$ literal se pone con \${'\$'}.")
}

/**
 * Igualdad de cadenas: contenido frente a referencia.
 */
fun demoStringEquality() {
    section("== compara contenido, === compara referencia")

    val a = "hola"
    val b = "hola"                                   // literal idéntico: la JVM lo reutiliza
    val c = buildString { append("ho"); append("la") } // construida en ejecución

    show("a == b", a == b)
    show("a === b   (mismo objeto, por internado de literales)", a === b)
    show("a == c    (mismo contenido)", a == c)
    show("a === c   (objetos distintos)", a === c)

    bullet("En Kotlin usa SIEMPRE ==. `equals` se llama por debajo.")
    bullet("=== sólo sirve para preguntar '¿son el mismo objeto?', casi nunca hace falta.")
    bullet("Esto es lo contrario de Java, donde `==` entre objetos compara referencias.")

    section("Comparar ignorando mayúsculas")

    show("\"Hola\" == \"hola\"", "Hola" == "hola")
    show("\"Hola\".equals(\"hola\", ignoreCase = true)", "Hola".equals("hola", ignoreCase = true))
    show("\"Hola\".lowercase() == \"hola\"", "Hola".lowercase() == "hola")
}

/**
 * Operaciones de cadena que se usan todos los días.
 */
fun demoStringOperations() {
    val texto = "  Kotlin es conciso  "

    section("Vacío, en blanco y nulo")

    show("\"\".isEmpty()", "".isEmpty())
    show("\"   \".isEmpty()", "   ".isEmpty())
    show("\"   \".isBlank()   (espacios cuentan como vacío)", "   ".isBlank())
    val nulo: String? = null
    show("null.isNullOrBlank()", nulo.isNullOrBlank())

    section("Limpiar y transformar")

    show("trim()", quoted(texto.trim()))
    show("uppercase()", texto.trim().uppercase())
    show("replace()", texto.trim().replace("conciso", "expresivo"))
    show("take(6) / takeLast(7)", "${texto.trim().take(6)} ... ${texto.trim().takeLast(7)}")

    section("Trocear y unir")

    val csv = "ana,luis,marta"
    val nombres = csv.split(",")
    show("split(\",\")", nombres)
    show("joinToString(\" | \")", nombres.joinToString(" | "))
    show("first() / last()", "${csv.first()} ... ${csv.last()}")

    section("Indexar")

    val palabra = "Kotlin"
    show("palabra[0]", palabra[0])
    show("palabra.length", palabra.length)
    show("palabra.lastIndex", palabra.lastIndex)
    show("palabra.substring(0, 3)", palabra.substring(0, 3))
    show("palabra.reversed()", palabra.reversed())
    show("palabra.contains(\"lin\")", palabra.contains("lin"))
    show("\"lin\" in palabra   (lo mismo, más legible)", "lin" in palabra)

    section("Rellenar y repetir")

    show("padStart(8, '0')", quoted("42".padStart(8, '0')))
    show("padEnd(8, '.')", quoted("42".padEnd(8, '.')))
    show("\"-\".repeat(10)", "-".repeat(10))

    section("Construir texto en bucle")

    // MAL: cada `+=` crea una cadena nueva. Con 10.000 vueltas se nota mucho.
    var concatenado = ""
    for (i in 1..5) concatenado += i

    // BIEN: buildString usa un StringBuilder por debajo y crea la cadena una sola vez.
    val construido = buildString {
        for (i in 1..5) append(i)
    }

    show("con += en bucle (evítalo)", concatenado)
    show("con buildString { }", construido)
    bullet("Para 5 vueltas da igual; para 10.000, buildString es órdenes de magnitud mejor.")
}

/**
 * El tipo Char: un carácter, no una cadena de longitud uno.
 */
fun demoChars() {
    section("Literales")

    val letra = 'K'
    show("val letra = 'K'", letra)
    show("tipo", letra::class.simpleName)
    bullet("Comillas SIMPLES para Char, dobles para String. No son intercambiables.")

    section("Char y su código numérico")

    show("'A'.code", 'A'.code)
    show("Char(66)", Char(66))
    show("'7'.digitToInt()   (el dígito, no el código)", '7'.digitToInt())
    show("'7'.code           (el código Unicode)", '7'.code)

    bullet("Cuidado: '7'.code es 55, no 7. Para el valor numérico usa digitToInt().")

    section("Aritmética con Char")

    show("'a' + 2", 'a' + 2)
    show("'z' - 'a'   (distancia: da un Int)", 'z' - 'a')
    show("'c' - 1", 'c' - 1)

    section("Clasificar caracteres")

    show("'7'.isDigit()", '7'.isDigit())
    show("'k'.isLetter()", 'k'.isLetter())
    show("' '.isWhitespace()", ' '.isWhitespace())
    show("'k'.uppercaseChar()", 'k'.uppercaseChar())
    show("'Ñ'.lowercaseChar()", 'Ñ'.lowercaseChar())

    section("Recorrer una cadena carácter a carácter")

    val palabra = "Kotlin"
    val vocales = palabra.count { it.lowercaseChar() in "aeiou" }
    show("vocales en \"Kotlin\"", vocales)
    show("a lista de Char", palabra.toList())
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `.trimIndent()` de la consulta SQL y compara la salida.
//  2. Cambia `${'$'}1.250,00` por `$1.250,00` y lee el error del compilador.
//  3. Comprueba que `a === c` sigue siendo false aunque el contenido sea idéntico.
//  4. Sube el bucle de `concatenado += i` a 1..100_000 y cronométralo contra buildString.
