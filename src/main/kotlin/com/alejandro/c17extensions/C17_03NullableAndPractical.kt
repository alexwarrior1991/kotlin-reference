package com.alejandro.c17extensions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  17.3 · Receptores nulables, límites y extensiones útiles
//
//  QUÉ ES
//    Una extensión puede declararse sobre un tipo NULABLE (`fun String?.foo()`), lo
//    que permite llamarla sobre null sin `?.`. Y aquí se recogen también los límites
//    de las extensiones y una colección de las que de verdad merecen la pena.
//
//  POR QUÉ IMPORTA
//    `isNullOrEmpty()` y `orEmpty()` de la biblioteca estándar funcionan así: por eso
//    puedes escribir `texto.isNullOrEmpty()` sin `?.` aunque `texto` sea nulable.
//
//  ERRORES COMUNES
//    · No entender por qué `texto.isNullOrBlank()` compila sin `?.`.
//    · Declarar el receptor nulable "por si acaso" y tener que comprobarlo dentro.
//    · Pensar que una extensión puede ver los miembros privados del receptor.
// =====================================================================================

/**
 * Receptores nulables.
 */
fun demoNullableReceivers() {
    section("Extensión sobre un tipo NO nulable")

    val texto: String? = null

    // `sinEspaciosExtras` está declarada sobre String (no nulable), así que hace
    // falta `?.` para llamarla.
    show("texto?.sinEspaciosExtras()", texto?.sinEspaciosExtras())
    show("\"  a  b  \".sinEspaciosExtras()", "  a  b  ".sinEspaciosExtras())

    section("Extensión sobre un tipo NULABLE")

    // `oVacio` está declarada sobre String?, así que se puede llamar directamente
    // sobre null: dentro de la función, `this` puede ser null.
    show("texto.oVacio()", "'" + texto.oVacio() + "'")
    show("\"hola\".oVacio()", "hola".oVacio())
    show("texto.estaVacioONulo()", texto.estaVacioONulo())
    show("\"\".estaVacioONulo()", "".estaVacioONulo())
    show("\"hola\".estaVacioONulo()", "hola".estaVacioONulo())

    bullet("Fíjate: no hay ningún `?.` en las llamadas. No hace falta.")

    section("Las de la biblioteca estándar funcionan así")

    val nulo: String? = null
    val vacio: String? = ""
    val conTexto: String? = "hola"

    show("nulo.isNullOrEmpty()", nulo.isNullOrEmpty())
    show("vacio.isNullOrEmpty()", vacio.isNullOrEmpty())
    show("conTexto.isNullOrEmpty()", conTexto.isNullOrEmpty())
    show("nulo.orEmpty()", "'" + nulo.orEmpty() + "'")
    show("nulo.isNullOrBlank()", nulo.isNullOrBlank())

    val listaNula: List<Int>? = null
    show("listaNula.orEmpty()", listaNula.orEmpty())
    show("listaNula.isNullOrEmpty()", listaNula.isNullOrEmpty())

    section("Dentro, `this` puede ser null")

    bullet("El cuerpo tiene que tratar ese caso: `if (this == null) ... else ...`")
    bullet("Tras la comprobación hay smart cast, así que el resto es cómodo.")

    section("¿Cuándo declarar el receptor nulable?")

    bullet("SÍ: cuando la operación tiene un resultado razonable para null.")
    bullet("    `orEmpty()` → cadena vacía. `isNullOrBlank()` → true.")
    bullet("NO: cuando null significa 'no se puede hacer nada'. Ahí es mejor el")
    bullet("    receptor no nulable y que quien llame use `?.`, que se ve.")
    bullet("Regla: si dentro lo primero que haces es `?: return null`, no era el caso.")
}

/**
 * Lo que una extensión NO puede hacer.
 */
fun demoLimitations() {
    section("1. No ve los miembros privados")

    val cuenta = Cuenta(1000)
    show("cuenta.saldoFormateado()  (miembro)", cuenta.saldoFormateado())
    show("cuenta.esPositiva()       (extensión)", cuenta.esPositiva())

    bullet("`esPositiva()` sólo puede usar la API PÚBLICA de Cuenta.")
    bullet("El campo interno `saldoCentimos` es privado y no lo ve.")
    bullet("Es la misma limitación que tendría cualquier código de fuera.")

    section("2. No se pueden sobrescribir")

    bullet("No existe `override` para una extensión: no forma parte de la jerarquía.")
    bullet("Ver la demo 17.2 sobre resolución estática.")

    section("3. No añaden estado")

    bullet("Sin backing field. Si necesitas guardar algo por objeto, usa un Map")
    bullet("externo (y piensa bien si no deberías estar modificando la clase).")

    section("4. El miembro siempre gana")

    bullet("Y si la librería añade mañana ese método, tu extensión deja de usarse")
    bullet("en silencio. Es el riesgo de extender tipos que no controlas.")

    section("5. No son visibles desde Java como métodos")

    bullet("Desde Java, `\"x\".enMayusculaInicial()` no existe.")
    bullet("Hay que llamar a `UtilKt.enMayusculaInicial(\"x\")`. Capítulo 27.")

    section("Lo que sí hacen muy bien")

    bullet("Dar una API moderna a tipos antiguos (toda la stdlib sobre java.util).")
    bullet("Separar el modelo de dominio de sus utilidades de presentación.")
    bullet("Encadenar operaciones sin envolver el objeto en un wrapper.")
}

/**
 * Una pequeña biblioteca de extensiones útiles.
 */
fun demoUsefulExtensions() {
    section("Sobre String")

    show("\"ana@ejemplo.com\".esEmailBasico()", "ana@ejemplo.com".esEmailBasico())
    show("\"noesunemail\".esEmailBasico()", "noesunemail".esEmailBasico())
    show("\"Kotlin es genial\".recortarA(10)", "Kotlin es genial".recortarA(10))
    show("\"corto\".recortarA(10)", "corto".recortarA(10))
    show("\"12\".aIntOSi(0)", "12".aIntOSi(0))
    show("\"abc\".aIntOSi(0)", "abc".aIntOSi(0))

    section("Sobre colecciones")

    val numeros = listOf(5, 3, 8, 1)
    show("numeros.segundoMayorONull()", numeros.segundoMayorONull())
    show("listOf(1).segundoMayorONull()", listOf(1).segundoMayorONull())
    show("numeros.mediaOCero()", "%.2f".format(numeros.mediaOCero()))
    show("emptyList<Int>().mediaOCero()", emptyList<Int>().mediaOCero())
    show("numeros.enParejas()", numeros.enParejas())

    section("Sobre números")

    show("1050.aEuros()", 1050.aEuros())
    show("50.limitadoA(0..10)", 50.limitadoA(0..10))
    show("(-5).limitadoA(0..10)", (-5).limitadoA(0..10))
    show("7.limitadoA(0..10)", 7.limitadoA(0..10))
    show("3.vecesRepetido(\"ab\")", 3.vecesRepetido("ab"))

    section("Sobre tipos propios")

    val carrito = Carrito(listOf(Articulo("pan", 120), Articulo("leche", 95)))
    show("carrito.totalFormateado()", carrito.totalFormateado())
    show("carrito.masCaro()?.nombre", carrito.masCaro()?.nombre)
    show("carrito.resumen()", carrito.resumen())

    bullet("Éstas van en el fichero del modelo o en uno de presentación,")
    bullet("según si son lógica de dominio o formato para la interfaz.")

    section("El criterio para crear una extensión")

    bullet("¿La usarías en más de un sitio? → sí, extensión.")
    bullet("¿Es lógica del dominio? → mejor un método de la clase, si puedes tocarla.")
    bullet("¿Es formato o presentación? → extensión, y lejos del modelo.")
    bullet("¿Sólo la usa esta función? → una función local (capítulo 5.9).")
}

// -- Extensiones sobre tipos nulables -----------------------------------------------------

/** Receptor NO nulable: hace falta `?.` para llamarla sobre algo que pueda ser null. */
private fun String.sinEspaciosExtras(): String = trim().replace(Regex("\\s+"), " ")

/** Receptor NULABLE: se puede llamar sobre null directamente. */
private fun String?.oVacio(): String = this ?: ""

private fun String?.estaVacioONulo(): Boolean {
    // `this` puede ser null aquí dentro; tras el chequeo hay smart cast.
    if (this == null) return true
    return isEmpty()
}

// -- Límites -----------------------------------------------------------------------------

private class Cuenta(private val saldoCentimos: Int) {
    fun saldoFormateado(): String = "%.2f €".format(saldoCentimos / 100.0)
    val saldoEnEuros: Double get() = saldoCentimos / 100.0
}

/** Sólo puede usar la API pública: `saldoCentimos` es privado y no lo ve. */
private fun Cuenta.esPositiva(): Boolean = saldoEnEuros > 0

// -- Una pequeña biblioteca de extensiones -------------------------------------------------

private fun String.esEmailBasico(): Boolean =
    matches(Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))

private fun String.recortarA(maximo: Int): String =
    if (length <= maximo) this else take(maximo - 1) + "…"

private fun String.aIntOSi(porDefecto: Int): Int = toIntOrNull() ?: porDefecto

private fun List<Int>.segundoMayorONull(): Int? = distinct().sortedDescending().getOrNull(1)

private fun List<Int>.mediaOCero(): Double = if (isEmpty()) 0.0 else average()

private fun <T> List<T>.enParejas(): List<Pair<T, T>> =
    chunked(2).filter { it.size == 2 }.map { it[0] to it[1] }

private fun Int.aEuros(): String = "%.2f €".format(this / 100.0)

private fun Int.limitadoA(rango: IntRange): Int = coerceIn(rango)

private fun Int.vecesRepetido(texto: String): String = texto.repeat(this)

private data class Articulo(val nombre: String, val precioCentimos: Int)
private data class Carrito(val articulos: List<Articulo>)

private fun Carrito.total(): Int = articulos.sumOf { it.precioCentimos }
private fun Carrito.totalFormateado(): String = total().aEuros()
private fun Carrito.masCaro(): Articulo? = articulos.maxByOrNull { it.precioCentimos }
private fun Carrito.resumen(): String =
    "${articulos.size} artículos por ${totalFormateado()}"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `String?.oVacio()` a `String.oVacio()` y mira qué llamadas se rompen.
//  2. Intenta que `Cuenta.esPositiva()` use `saldoCentimos` y lee el error.
//  3. Escribe `fun <T> List<T>.segundoONull(): T?` y compárala con `getOrNull(1)`.
//  4. Añade `fun String.esUrl(): Boolean` con una expresión regular sencilla.
