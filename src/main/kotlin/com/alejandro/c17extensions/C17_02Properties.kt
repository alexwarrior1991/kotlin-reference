package com.alejandro.c17extensions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  17.2 · Propiedades de extensión
//
//  QUÉ ES
//    Lo mismo que una función de extensión, pero con sintaxis de propiedad:
//    `val String.primeraLetra: Char get() = this[0]`.
//
//  POR QUÉ IMPORTA
//    `list.lastIndex`, `list.indices` y `CharSequence.lastIndex` son propiedades de
//    extensión de la biblioteca estándar. Se leen como atributos del objeto, que es
//    justo lo que son conceptualmente.
//
//  ERRORES COMUNES
//    · Intentar darles un valor inicial: NO pueden tener backing field.
//    · Usar una propiedad donde el cálculo es caro o puede fallar (eso es una función).
//    · Crear una propiedad de extensión con `var` sin un setter que tenga sentido.
// =====================================================================================

private data class Pedido(val referencia: String, val lineas: List<LineaPedido>)
private data class LineaPedido(val producto: String, val unidades: Int, val precioCentimos: Int)

/**
 * Lo básico y la limitación fundamental.
 */
fun demoExtensionProperties() {
    section("Declaración")

    bullet("val String.primeraLetra: Char")
    bullet("    get() = this[0]")
    bullet("Hay que escribir el `get()`: NO hay valor inicial posible.")

    show("\"Kotlin\".primeraLetra", "Kotlin".primeraLetra)
    show("\"Kotlin\".estaVacia", "Kotlin".estaVacia)
    show("\"\".estaVacia", "".estaVacia)
    show("\"  hola  \".sinEspacios", "  hola  ".sinEspacios)

    section("La limitación: no hay backing field")

    // val String.contador: Int = 0        // ERROR: Extension property cannot be initialized
    //                                     // because it has no backing field
    bullet("Una extensión NO añade memoria al objeto: sólo añade código.")
    bullet("El objeto sigue siendo exactamente el mismo en memoria.")
    bullet("Por eso siempre hay que dar un `get()` que CALCULE el valor.")

    section("¿Dónde guardarías el valor?")

    bullet("En ningún sitio. `String` es una clase de la biblioteca estándar y no")
    bullet("puedes añadirle un campo desde fuera. Si necesitas guardar algo asociado")
    bullet("a un objeto ajeno, la respuesta es un Map externo, no una extensión.")

    section("Las de la biblioteca estándar")

    val lista = listOf("a", "b", "c")
    show("lista.lastIndex", lista.lastIndex)
    show("lista.indices", lista.indices)
    show("\"texto\".lastIndex", "texto".lastIndex)
    show("mapa.keys y mapa.values", mapOf(1 to "a").keys)
    bullet("`lastIndex` e `indices` son propiedades de extensión, no miembros.")
}

/**
 * Propiedad o función: cómo elegir.
 */
fun demoPropertyOrFunction() {
    section("El criterio")

    bullet("PROPIEDAD si: es barata, no falla, no tiene efectos, y conceptualmente")
    bullet("             es un ATRIBUTO del objeto ('el tamaño', 'el último índice').")
    bullet("FUNCIÓN si: es cara, puede lanzar, tiene efectos, o es una ACCIÓN")
    bullet("            ('calcular el total', 'enviar', 'guardar').")

    section("La prueba de la biblioteca estándar")

    bullet("lista.size        → propiedad (está ahí, cuesta cero)")
    bullet("lista.lastIndex   → propiedad (una resta)")
    bullet("lista.sum()       → función (recorre toda la colección)")
    bullet("lista.first()     → función (puede lanzar si está vacía)")

    section("Aplicado a un modelo propio")

    val pedido = Pedido(
        "P-1",
        listOf(
            LineaPedido("teclado", 1, 4_999),
            LineaPedido("ratón", 2, 1_550),
        ),
    )

    show("pedido.numeroDeLineas  (propiedad)", pedido.numeroDeLineas)
    show("pedido.estaVacio       (propiedad)", pedido.estaVacio)
    show("pedido.totalCentimos() (función)", pedido.totalCentimos())
    show("pedido.formateado()    (función)", pedido.formateado())

    bullet("`numeroDeLineas` es una lectura directa: propiedad.")
    bullet("`totalCentimos()` recorre y suma: función, aunque sea barata.")
    bullet("La coherencia importa más que la regla exacta: elige una y mantenla.")

    section("Un aviso")

    bullet("Quien lee `objeto.propiedad` asume que es barato y que puede escribirlo")
    bullet("dentro de un bucle sin pensar. Si tu getter hace una consulta a base de")
    bullet("datos, estás mintiendo. Eso tiene que ser una función.")
}

/**
 * Propiedades de extensión con `var`.
 */
fun demoMutableExtensionProperties() {
    section("Se puede, si el setter tiene sentido")

    // El setter no guarda nada por sí mismo: traduce la asignación a una operación
    // real sobre el objeto.
    val lista = mutableListOf(1, 2, 3)
    show("lista", lista)

    lista.ultimo = 99
    show("tras lista.ultimo = 99", lista)

    show("lista.ultimo", lista.ultimo)

    bullet("El getter lee `last()`; el setter escribe en `lastIndex`.")
    bullet("No hay ningún campo nuevo: se delega en operaciones que ya existen.")

    section("Cuándo NO hacerlo")

    bullet("Si el setter tiene que inventarse dónde guardar el valor, no lo hagas.")
    bullet("Una `var` de extensión sólo es honesta si traduce a algo real del objeto.")

    section("Otro ejemplo razonable")

    val builder = StringBuilder("hola")
    show("builder", builder.toString())
    builder.contenido = "adiós"
    show("tras builder.contenido = \"adiós\"", builder.toString())
}

/**
 * Extensiones sobre el companion object.
 */
fun demoCompanionExtensions() {
    section("El truco de las 'funciones estáticas' añadidas")

    // Si una clase tiene companion object, se le pueden añadir extensiones, y se
    // llaman como si fueran estáticas de la clase.
    show("Temperatura.desdeFahrenheit(212)", Temperatura.desdeFahrenheit(212.0))
    show("Temperatura.CERO_ABSOLUTO", Temperatura.CERO_ABSOLUTO)

    bullet("`desdeFahrenheit` no está dentro de Temperatura: es una extensión de su")
    bullet("companion, definida en este fichero.")

    section("El requisito")

    bullet("La clase DEBE tener un `companion object`, aunque esté vacío.")
    bullet("Sin él no hay nada a lo que colgar la extensión.")
    bullet("Es un patrón habitual para añadir fábricas a clases de terceros.")

    section("Para qué se usa")

    bullet("Añadir constructores alternativos a una clase que no controlas.")
    bullet("Agrupar constantes relacionadas sin tocar la clase original.")
    bullet("En librerías: dar fábricas específicas de una plataforma.")
}

// -- Las extensiones que usan las demos -------------------------------------------------

private val String.primeraLetra: Char get() = if (isEmpty()) ' ' else this[0]
private val String.estaVacia: Boolean get() = isEmpty()
private val String.sinEspacios: String get() = replace(" ", "")

private val Pedido.numeroDeLineas: Int get() = lineas.size
private val Pedido.estaVacio: Boolean get() = lineas.isEmpty()

private fun Pedido.totalCentimos(): Int = lineas.sumOf { it.unidades * it.precioCentimos }
private fun Pedido.formateado(): String =
    "$referencia · $numeroDeLineas líneas · %.2f €".format(totalCentimos() / 100.0)

/** Propiedad de extensión mutable: el setter traduce a una operación real. */
private var <T> MutableList<T>.ultimo: T
    get() = last()
    set(valor) {
        if (isNotEmpty()) this[lastIndex] = valor
    }

private var StringBuilder.contenido: String
    get() = toString()
    set(valor) {
        setLength(0)
        append(valor)
    }

// -- Extensión sobre un companion object --------------------------------------------------

private class Temperatura(val celsius: Double) {
    override fun toString(): String = "%.1f °C".format(celsius)

    /** Necesario para poder colgarle extensiones, aunque esté vacío. */
    companion object
}

private fun Temperatura.Companion.desdeFahrenheit(grados: Double): Temperatura =
    Temperatura((grados - 32) * 5 / 9)

private val Temperatura.Companion.CERO_ABSOLUTO: Temperatura get() = Temperatura(-273.15)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Intenta `val String.contador: Int = 0` y lee el error sobre el backing field.
//  2. Convierte `pedido.totalCentimos()` en propiedad y decide si te gusta más.
//  3. Quita el `companion object` de Temperatura y mira qué extensiones dejan de compilar.
//  4. Escribe `var StringBuilder.primeraLinea: String` con getter y setter coherentes.
