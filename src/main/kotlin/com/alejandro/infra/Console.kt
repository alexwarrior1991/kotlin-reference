package com.alejandro.infra

// =====================================================================================
//  Utilidades de impresión por consola.
//
//  Su único objetivo es que las ~150 demos del repositorio tengan TODAS el mismo
//  aspecto: así, cuando leas la salida de un capítulo que no conoces, ya sabes dónde
//  mirar. No aprendas nada de Kotlin aquí; empieza por el capítulo 01.
// =====================================================================================

/** Ancho de los separadores. 88 columnas entran en cualquier terminal razonable. */
private const val WIDTH = 88

/** Línea horizontal de separación. */
fun separator(char: Char = '─') {
    println(char.toString().repeat(WIDTH))
}

/** Banner grande. Se usa para abrir un capítulo entero. */
fun heading(text: String) {
    println()
    separator('━')
    println("  $text")
    separator('━')
}

/** Cabecera de una demo concreta, con su identificador ("13.4"). */
fun demoHeader(id: String, title: String) {
    println()
    println("▶ $id · $title")
    separator()
}

/**
 * Sub-apartado dentro de una demo. Úsalo para separar las variantes de un mismo
 * concepto (por ejemplo: "con let" / "con if != null").
 */
fun section(text: String) {
    println()
    println("· $text")
}

/** Viñeta para una explicación suelta. */
fun bullet(text: String) {
    println("    - $text")
}

/**
 * Imprime `etiqueta → valor` con las columnas alineadas.
 *
 * Es la forma de salida más usada del repositorio: al quedar todo alineado, comparar
 * dos resultados parecidos (por ejemplo `==` frente a `===`) es inmediato.
 */
fun show(label: String, value: Any?) {
    println("    %-44s %s".format(label, valueToText(value)))
}

/**
 * Entrecomilla un texto para que se vean sus bordes.
 *
 * Úsalo cuando lo importante sea el contenido exacto de una cadena: sin comillas,
 * `"  hola  ".trim()` y `"hola"` se imprimen igual y no se aprecia qué ha pasado.
 */
fun quoted(text: String?): String = if (text == null) "null" else "\"$text\""

/**
 * Representación legible de un valor.
 *
 * `toString()` de un array imprime algo como `[I@5ca881b5`, que no dice nada; aquí lo
 * convertimos en su contenido real. Es un detalle pequeño pero evita mucha confusión
 * en el capítulo 02, donde los arrays aparecen por primera vez.
 *
 * Las cadenas se imprimen tal cual, sin comillas: la mayoría de las veces el valor que
 * se muestra es una explicación, no un dato. Cuando quieras ver las comillas, envuelve
 * el valor con [quoted].
 */
private fun valueToText(value: Any?): String = when (value) {
    null -> "null"
    is Array<*> -> value.contentDeepToString()
    is IntArray -> value.contentToString()
    is LongArray -> value.contentToString()
    is DoubleArray -> value.contentToString()
    is FloatArray -> value.contentToString()
    is ShortArray -> value.contentToString()
    is ByteArray -> value.contentToString()
    is CharArray -> value.contentToString()
    is BooleanArray -> value.contentToString()
    else -> value.toString()
}
