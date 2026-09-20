package com.alejandro.c12generics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  12.1 · Clases y funciones genéricas
//
//  QUÉ ES
//    Un parámetro de tipo (`<T>`) permite escribir una clase o función una sola vez y
//    usarla con muchos tipos, sin perder la comprobación del compilador.
//
//  POR QUÉ IMPORTA
//    Es la diferencia entre `List<String>` y una lista de `Any` donde hay que hacer un
//    cast en cada lectura (y rezar). Toda la biblioteca de colecciones se apoya en esto.
//
//  ERRORES COMUNES
//    · Escribir el tipo cuando el compilador ya puede deducirlo.
//    · Usar `Any` en lugar de un parámetro de tipo y perder la información.
//    · Confundir `<T>` en la declaración con `<T>` en el uso.
// =====================================================================================

/**
 * El problema que resuelven.
 */
fun demoWhyGenerics() {
    section("Sin genéricos: todo es Any")

    val cajaSinTipo = CajaDeAny("Kotlin")
    val contenido = cajaSinTipo.contenido        // es Any
    show("cajaSinTipo.contenido", contenido)
    show("su tipo estático", "Any (hay que hacer cast para usarlo)")

    // Para usarlo como String hay que convertirlo, y nada garantiza que funcione.
    val fallo = try {
        (CajaDeAny(42).contenido as String).length.toString()
    } catch (e: ClassCastException) {
        "lanzó ClassCastException"
    }
    show("meter un Int y sacarlo como String", fallo)

    section("Con genéricos: el tipo viaja con el objeto")

    val caja = Caja("Kotlin")
    show("caja.contenido", caja.contenido)
    show("caja.contenido.length  (sin cast)", caja.contenido.length)

    // Caja(42) es Caja<Int>: el compilador no deja tratarlo como texto.
    // Caja(42).contenido.length     // ERROR: Unresolved reference 'length'
    bullet("El error pasa de ejecución a compilación. Ése es todo el objetivo.")
}

/**
 * Clases genéricas.
 */
fun demoGenericClasses() {
    section("Declarar e instanciar")

    val deTexto: Caja<String> = Caja("hola")
    val deNumero = Caja(42)                     // el tipo se infiere: Caja<Int>

    show("Caja<String>", deTexto)
    show("Caja(42) inferida", deNumero)
    show("tipo del contenido", deNumero.contenido::class.simpleName)

    section("Varios parámetros de tipo")

    val par = Pareja("edad", 34)
    show("Pareja(\"edad\", 34)", par)
    show("par.primero", par.primero)
    show("par.segundo", par.segundo)
    show("par.invertida()", par.invertida())

    section("Métodos que cambian el tipo")

    val comoTexto = deNumero.transformar { "número $it" }
    show("Caja(42).transformar { \"número \$it\" }", comoTexto)
    show("su tipo ahora", "Caja<String>")

    section("Genéricos con valores por defecto")

    val vacia = Caja<String?>(null)
    show("Caja<String?>(null)", vacia)
    bullet("`Caja<String>` y `Caja<String?>` son tipos distintos.")
}

/**
 * Funciones genéricas.
 */
fun demoGenericFunctions() {
    section("El parámetro de tipo va ANTES del nombre")

    bullet("fun <T> primeroODefecto(lista: List<T>, porDefecto: T): T")
    bullet("El `<T>` después de `fun` es la declaración; el resto son usos.")

    show("primeroODefecto(listOf(1,2,3), 0)", primeroODefecto(listOf(1, 2, 3), 0))
    show("primeroODefecto(emptyList(), 0)", primeroODefecto(emptyList(), 0))
    show("con texto", primeroODefecto(listOf("a"), "z"))

    section("Normalmente el tipo se infiere")

    show("intercalar(listOf(1,2,3), 0)", intercalar(listOf(1, 2, 3), 0))
    show("intercalar(listOf(\"a\",\"b\"), \"-\")", intercalar(listOf("a", "b"), "-"))

    section("Cuándo hay que escribirlo")

    // Si no hay ningún argumento del que deducir el tipo, hay que decirlo.
    val listaVacia = listaVaciaDe<String>()
    show("listaVaciaDe<String>()", listaVacia)
    bullet("`listaVaciaDe()` a secas no compilaría: no hay nada de donde inferir.")

    section("Dos parámetros de tipo")

    show("emparejar(1, \"uno\")", emparejar(1, "uno"))
    show("aplicar(5) { it * 2 }", aplicar(5) { it * 2 })
    show("aplicar(\"ab\") { it.length }", aplicar("ab") { it.length })

    section("Funciones de extensión genéricas")

    show("listOf(3,1,2).segundoOMenor()", listOf(3, 1, 2).segundoOMenor())
    show("listOf(5).segundoOMenor()", listOf(5).segundoOMenor())
    bullet("Toda la biblioteca de colecciones está escrita así.")
}

// -- Las clases y funciones que usan las demos -----------------------------------------

/** Sin genéricos: guarda cualquier cosa, pero pierde el tipo. */
private class CajaDeAny(val contenido: Any)

/** Con genéricos: el tipo del contenido forma parte del tipo de la caja. */
private class Caja<T>(val contenido: T) {

    /** Un método puede introducir SU propio parámetro de tipo. */
    fun <R> transformar(transformacion: (T) -> R): Caja<R> = Caja(transformacion(contenido))

    override fun toString(): String = "Caja($contenido)"
}

/** Dos parámetros de tipo. */
private class Pareja<A, B>(val primero: A, val segundo: B) {
    fun invertida(): Pareja<B, A> = Pareja(segundo, primero)
    override fun toString(): String = "($primero, $segundo)"
}

private fun <T> primeroODefecto(lista: List<T>, porDefecto: T): T =
    lista.firstOrNull() ?: porDefecto

/** Mete un separador entre cada par de elementos. */
private fun <T> intercalar(lista: List<T>, separador: T): List<T> = buildList {
    lista.forEachIndexed { indice, elemento ->
        if (indice > 0) add(separador)
        add(elemento)
    }
}

/** Sin argumentos de los que inferir: hay que pasar el tipo explícitamente. */
private fun <T> listaVaciaDe(): List<T> = emptyList()

private fun <A, B> emparejar(a: A, b: B): Pareja<A, B> = Pareja(a, b)

private fun <T, R> aplicar(valor: T, transformacion: (T) -> R): R = transformacion(valor)

/** Extensión genérica: devuelve el segundo elemento, o el único si sólo hay uno. */
private fun <T> List<T>.segundoOMenor(): T? = if (size >= 2) this[1] else firstOrNull()

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `Caja(42).contenido.length` y comprueba que el compilador lo impide.
//  2. Llama a `listaVaciaDe()` sin el tipo y lee el error de inferencia.
//  3. Añade a Caja un método `combinar(otra: Caja<T>)` que devuelva una Caja<Pareja<T,T>>.
//  4. Escribe `fun <T> List<T>.penultimo(): T?` y pruébala con listas de 0, 1 y 3 elementos.
