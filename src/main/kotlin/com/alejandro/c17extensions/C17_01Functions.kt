package com.alejandro.c17extensions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  17.1 · Funciones de extensión
//
//  QUÉ ES
//    Añadir funciones a un tipo que no controlas, sin heredar de él ni modificarlo.
//    Se escriben `fun Tipo.nombre(...)` y se llaman como si fueran métodos suyos.
//
//  POR QUÉ IMPORTA
//    Toda la biblioteca de colecciones de Kotlin son extensiones sobre las interfaces
//    de Java: `map`, `filter` y `first` no están dentro de `java.util.List`, se le
//    añaden desde fuera. Es lo que permite tener una API moderna sobre tipos antiguos.
//
//  ERRORES COMUNES
//    · Creer que una extensión puede acceder a los miembros privados del tipo.
//    · No saber que la resolución es ESTÁTICA (el ejemplo que sorprende, más abajo).
//    · Llenar el proyecto de extensiones sobre `String` que sólo usa un fichero.
// =====================================================================================

/**
 * Lo básico.
 */
fun demoBasics() {
    section("Añadir una función a un tipo que no es tuyo")

    // `String` es de la biblioteca estándar y no se puede tocar. Con una extensión
    // se le añade comportamiento igualmente.
    show("\"kotlin\".enMayusculaInicial()", "kotlin".enMayusculaInicial())
    show("\"\".enMayusculaInicial()", "".enMayusculaInicial())
    show("\"hola mundo\".enMayusculaInicial()", "hola mundo".enMayusculaInicial())

    section("El receptor es `this`")

    bullet("fun String.enMayusculaInicial(): String  →  dentro, `this` es el String.")
    bullet("Como con las lambdas con receptor, `this` se puede omitir.")

    section("Sobre cualquier tipo")

    show("42.esPar()", 42.esPar())
    show("listOf(1,2,3).segundoONull()", listOf(1, 2, 3).segundoONull())
    show("listOf(1).segundoONull()", listOf(1).segundoONull())
    show("(1..10).mitades()", (1..10).mitades())

    section("Con parámetros de tipo")

    show("listOf(3,1,2).ordenadaYUnica()", listOf(3, 1, 2, 1, 3).ordenadaYUnica())
    show("listOf(\"b\",\"a\").ordenadaYUnica()", listOf("b", "a", "b").ordenadaYUnica())

    section("Qué son en realidad")

    // El compilador las convierte en funciones estáticas normales cuyo primer
    // parámetro es el receptor.
    bullet("fun String.foo()  se compila como  static foo(String receptor)")
    bullet("Por eso NO modifican la clase original ni aparecen en su documentación.")
    bullet("Y por eso no pueden acceder a sus miembros `private` o `protected`.")
}

/**
 * La resolución es estática: el ejemplo que sorprende.
 */
fun demoStaticResolution() {
    section("El experimento")

    val comoPadre: Padre = Hija()

    show("comoPadre.metodoNormal()  (miembro, virtual)", comoPadre.metodoNormal())
    show("comoPadre.metodoExtension()  (extensión, estática)", comoPadre.metodoExtension())

    val comoHija: Hija = Hija()
    show("comoHija.metodoExtension()", comoHija.metodoExtension())

    section("Qué ha pasado")

    bullet("`metodoNormal` es polimórfico: se elige por el tipo REAL del objeto (Hija).")
    bullet("`metodoExtension` NO lo es: se elige por el tipo DECLARADO de la variable.")
    bullet("`comoPadre` está declarada como Padre, así que se llamó a la del Padre,")
    bullet("aunque el objeto sea una Hija.")

    section("Por qué es así")

    bullet("Una extensión es una función estática con el receptor como parámetro.")
    bullet("El despacho estático se resuelve al COMPILAR, mirando el tipo declarado.")
    bullet("No hay tabla de métodos virtuales que consultar en ejecución.")

    section("La consecuencia práctica")

    bullet("NO uses extensiones esperando polimorfismo. Si necesitas que cada subclase")
    bullet("se comporte distinto, eso es un método abierto en la jerarquía.")
    bullet("Las extensiones son para utilidades, no para el diseño de tipos.")
}

/**
 * El miembro siempre gana.
 */
fun demoMemberWins() {
    section("Si existen las dos, gana el MIEMBRO")

    val objeto = ConMiembro()
    show("objeto.saludar()", objeto.saludar())

    bullet("La extensión `ConMiembro.saludar()` existe, pero nunca se llama.")
    bullet("Kotlin ni siquiera avisa: simplemente la ignora.")

    section("Salvo que cambie la firma")

    // Con parámetros distintos ya no compiten: es una sobrecarga.
    show("objeto.saludar(\"Ana\")", objeto.saludar("Ana"))
    bullet("Aquí sí se llama a la extensión: el miembro no acepta parámetros.")

    section("Por qué esta regla")

    bullet("Si la extensión ganara, cualquiera podría cambiar el comportamiento de")
    bullet("una clase desde fuera con sólo importar un fichero. Sería un caos.")
    bullet("Consecuencia: si la librería añade mañana ese método, TU extensión")
    bullet("dejará de usarse en silencio. Es un riesgo real al extender tipos ajenos.")
}

/**
 * Dónde definirlas y cómo importarlas.
 */
fun demoScopeAndImports() {
    section("A nivel superior: el caso normal")

    bullet("Se declaran fuera de cualquier clase, normalmente en un fichero de")
    bullet("utilidades por tipo: StringExtensions.kt, ListExtensions.kt...")
    bullet("Para usarlas desde otro paquete hay que importarlas por su nombre:")
    bullet("  import com.alejandro.util.enMayusculaInicial")

    section("Dentro de una clase: extensiones con dos receptores")

    // Una extensión declarada dentro de una clase sólo existe dentro de ella, y tiene
    // acceso a `this` de los dos: el de la extensión y el de la clase que la contiene.
    val formateador = FormateadorDeInforme("EUR")
    show("informe", formateador.generar(listOf(1050, 2999, 500)))

    bullet("`Int.aMoneda()` sólo existe dentro de FormateadorDeInforme.")
    bullet("Dentro puede usar `moneda`, que es del formateador: son dos receptores.")
    bullet("Al de la extensión se le llama 'de extensión'; al de la clase, 'de despacho'.")

    section("Visibilidad")

    bullet("`private fun String.foo()` a nivel superior → sólo ese fichero.")
    bullet("`internal fun String.foo()` → todo el módulo.")
    bullet("Una extensión NUNCA ve los miembros privados de su receptor.")

    section("Buenas prácticas")

    bullet("Agrúpalas por tipo receptor, no por funcionalidad.")
    bullet("Si sólo la usa un fichero, decláralas `private` en ese fichero.")
    bullet("Cuidado con extender tipos muy comunes (String, List): esas extensiones")
    bullet("aparecen en el autocompletado de todo el proyecto y lo ensucian.")
}

// -- Las extensiones que usan las demos -------------------------------------------------

/** Una extensión clásica sobre String. */
private fun String.enMayusculaInicial(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1)

private fun Int.esPar(): Boolean = this % 2 == 0

/** Sobre un tipo genérico: la forma en que está escrita media stdlib. */
private fun <T> List<T>.segundoONull(): T? = getOrNull(1)

private fun <T : Comparable<T>> List<T>.ordenadaYUnica(): List<T> = distinct().sorted()

/** Sobre un rango. */
private fun IntRange.mitades(): Pair<List<Int>, List<Int>> {
    val lista = toList()
    val mitad = lista.size / 2
    return lista.take(mitad) to lista.drop(mitad)
}

// -- Resolución estática -----------------------------------------------------------------

private open class Padre {
    open fun metodoNormal(): String = "miembro del Padre"
}

private class Hija : Padre() {
    override fun metodoNormal(): String = "miembro de la Hija"
}

private fun Padre.metodoExtension(): String = "extensión de Padre"
private fun Hija.metodoExtension(): String = "extensión de Hija"

// -- El miembro gana ----------------------------------------------------------------------

private class ConMiembro {
    fun saludar(): String = "soy el MIEMBRO"
}

/** Nunca se llamará: el miembro tiene la misma firma y gana. */
private fun ConMiembro.saludar(): String = "soy la EXTENSIÓN (nunca me verás)"

/** Ésta sí: firma distinta, así que no compite con el miembro. */
private fun ConMiembro.saludar(nombre: String): String = "hola $nombre, soy la EXTENSIÓN"

// -- Extensión declarada dentro de una clase ----------------------------------------------

private class FormateadorDeInforme(private val moneda: String) {

    /**
     * Extensión con DOS receptores: `this` es el Int, y `moneda` viene del formateador.
     * Fuera de esta clase, `Int.aMoneda()` no existe.
     */
    private fun Int.aMoneda(): String = "%.2f %s".format(this / 100.0, moneda)

    fun generar(centimos: List<Int>): String =
        centimos.joinToString(" + ") { it.aMoneda() } + " = " + centimos.sum().aMoneda()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `metodoExtension()` también para una nieta y comprueba la resolución estática.
//  2. Borra el miembro `saludar()` de ConMiembro y mira qué pasa con la extensión.
//  3. Intenta usar `Int.aMoneda()` fuera de FormateadorDeInforme: no existe.
//  4. Escribe `fun <T> List<T>.penultimoONull(): T?` y pruébala con listas de 0, 1 y 3.
