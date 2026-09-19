package com.alejandro.c12generics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  12.2 · Restricciones: upper bounds y where
//
//  QUÉ ES
//    Limitar qué tipos puede tomar `T`. Sin restricción, `T` podría ser cualquier cosa
//    y dentro de la función sólo puedes tratarlo como `Any?`. Con una restricción,
//    puedes llamar a los métodos del tipo al que lo limitas.
//
//  POR QUÉ IMPORTA
//    Es lo que permite que `maxOf` compare, que `sorted()` ordene y que `sumOf` sume:
//    todas exigen que `T` cumpla algo.
//
//  ERRORES COMUNES
//    · Olvidar que el límite por defecto es `Any?`, así que `T` puede ser nulable.
//    · Intentar poner dos límites separados por coma (hay que usar `where`).
//    · Restringir de más y hacer la función inútil para casos válidos.
// =====================================================================================

/**
 * Sin restricción, `T` es `Any?`.
 */
fun demoNoConstraint() {
    section("Lo poco que puedes hacer con un T sin restringir")

    bullet("Guardarlo, devolverlo, pasarlo a otra función genérica.")
    bullet("Llamar a `toString()`, `hashCode()` y `equals()` (son de Any).")
    bullet("Nada más: el compilador no sabe qué otros métodos tiene.")

    show("describir(42)", describir(42))
    show("describir(\"texto\")", describir("texto"))
    show("describir(null)", describir(null))

    section("El límite por defecto es Any?, no Any")

    // Fíjate en que `describir(null)` compila: T se infiere como Nothing?.
    bullet("`fun <T> f(x: T)` acepta null. Si no quieres, escribe `<T : Any>`.")

    show("exigeNoNulo(42)", exigeNoNulo(42))
    // exigeNoNulo(null)   // ERROR: Null can not be a value of a non-null type
    bullet("`<T : Any>` es la forma de decir 'cualquier tipo, pero no nulable'.")
}

/**
 * Un límite superior: `<T : Algo>`.
 */
fun demoUpperBound() {
    section("Restringir a Comparable para poder comparar")

    show("mayorDe(3, 9)", mayorDe(3, 9))
    show("mayorDe(\"ana\", \"luis\")", mayorDe("ana", "luis"))
    show("mayorDe('a', 'z')", mayorDe('a', 'z'))

    // mayorDe(Persona("Ana"), Persona("Luis"))   // ERROR: Persona no es Comparable
    bullet("Sin `: Comparable<T>` no podrías escribir `a > b` dentro de la función.")

    section("Restringir a Number para operar")

    show("sumaDe(listOf(1, 2, 3))", sumaDe(listOf(1, 2, 3)))
    show("sumaDe(listOf(1.5, 2.5))", sumaDe(listOf(1.5, 2.5)))
    show("sumaDe(listOf(1L, 2L))", sumaDe(listOf(1L, 2L)))

    section("Restringir a una interfaz propia")

    val documentos = listOf(
        Factura("F-1", 120.0),
        Albaran("A-9", 3),
    )
    show("todos son Identificable", documentos.map { referenciaDe(it) })
    show("buscar por referencia", buscarPorReferencia(documentos, "A-9"))

    bullet("Es el uso más frecuente: una interfaz propia como límite.")
}

/**
 * Varios límites: `where`.
 */
fun demoWhereClause() {
    section("Un solo límite va en la declaración")

    bullet("fun <T : Comparable<T>> mayorDe(a: T, b: T): T")

    section("Dos o más límites necesitan `where`")

    // `T` debe ser a la vez Comparable e Identificable. Con coma no se puede:
    // `fun <T : Comparable<T>, Identificable>` significaría otra cosa (dos parámetros).
    val facturas = listOf(
        FacturaOrdenable("F-2", 300.0),
        FacturaOrdenable("F-1", 100.0),
    )
    show("ordenar e identificar a la vez", resumirOrdenado(facturas))

    bullet("fun <T> f(x: T) where T : Comparable<T>, T : Identificable")
    bullet("El `where` va DESPUÉS de la lista de parámetros y antes del cuerpo.")

    section("Un caso muy útil: T : Any y algo más")

    show("primeroNoNuloQueCumpla", primeroValidoDe(listOf(null, FacturaOrdenable("F-3", 50.0))))
}

/**
 * Restricciones en clases genéricas.
 */
fun demoConstrainedClasses() {
    section("La clase también puede restringir")

    val ordenados = ListaOrdenada<Int>()
    ordenados.agregar(5)
    ordenados.agregar(1)
    ordenados.agregar(3)
    show("ListaOrdenada<Int>", ordenados.elementos())
    show("mínimo", ordenados.minimo())

    val palabras = ListaOrdenada<String>()
    palabras.agregar("pera")
    palabras.agregar("manzana")
    show("ListaOrdenada<String>", palabras.elementos())

    // ListaOrdenada<Persona>()   // ERROR: Persona no es Comparable<Persona>
    bullet("La restricción se comprueba al USAR la clase, no al declararla.")

    section("Cómo elegir la restricción")

    bullet("Pon el límite MÁS DÉBIL que te permita hacer lo que necesitas.")
    bullet("Si sólo comparas, `Comparable<T>` basta: no exijas tu interfaz propia.")
    bullet("Cada restricción de más es un caso de uso que excluyes sin necesidad.")
}

// -- Tipos y funciones que usan las demos ----------------------------------------------

/** Sin restricción: dentro sólo se puede tratar como Any?. */
private fun <T> describir(valor: T): String = "valor='$valor' tipo=${valor?.let { it::class.simpleName } ?: "null"}"

/** `T : Any` excluye los tipos nulables. */
private fun <T : Any> exigeNoNulo(valor: T): String = "recibí $valor, seguro que no es null"

/** Restricción a Comparable: dentro se pueden usar <, >, compareTo. */
private fun <T : Comparable<T>> mayorDe(a: T, b: T): T = if (a > b) a else b

/** Restricción a Number: dentro se puede llamar a toDouble(). */
private fun <T : Number> sumaDe(numeros: List<T>): Double = numeros.sumOf { it.toDouble() }

private interface Identificable {
    val referencia: String
}

private data class Factura(override val referencia: String, val importe: Double) : Identificable
private data class Albaran(override val referencia: String, val bultos: Int) : Identificable

/** No es Comparable, así que no sirve para mayorDe. */
private data class Persona(val nombre: String)

private fun <T : Identificable> referenciaDe(documento: T): String = documento.referencia

private fun <T : Identificable> buscarPorReferencia(documentos: List<T>, referencia: String): T? =
    documentos.find { it.referencia == referencia }

/** Cumple las DOS restricciones que pide `resumirOrdenado`. */
private data class FacturaOrdenable(
    override val referencia: String,
    val importe: Double,
) : Identificable, Comparable<FacturaOrdenable> {
    override fun compareTo(other: FacturaOrdenable): Int = referencia.compareTo(other.referencia)
}

/** Dos restricciones sobre el mismo T: hace falta `where`. */
private fun <T> resumirOrdenado(elementos: List<T>): String
    where T : Comparable<T>, T : Identificable =
    elementos.sorted().joinToString(" → ") { it.referencia }

/** Tres restricciones: no nulable, comparable e identificable. */
private fun <T> primeroValidoDe(elementos: List<T?>): String
    where T : Any, T : Comparable<T>, T : Identificable =
    elementos.filterNotNull().minOrNull()?.referencia ?: "(ninguno)"

/** Una clase genérica con restricción. */
private class ListaOrdenada<T : Comparable<T>> {
    private val interna = mutableListOf<T>()

    fun agregar(elemento: T) {
        interna.add(elemento)
        interna.sort()          // sólo posible porque T es Comparable
    }

    fun elementos(): List<T> = interna.toList()
    fun minimo(): T? = interna.firstOrNull()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Llama a `mayorDe(Persona("Ana"), Persona("Luis"))` y lee el error.
//  2. Quita `: Any` de exigeNoNulo y comprueba que entonces sí acepta null.
//  3. Intenta escribir las dos restricciones con coma en lugar de `where`.
//  4. Cambia `ListaOrdenada<T : Comparable<T>>` por `<T>` y mira qué deja de compilar.
