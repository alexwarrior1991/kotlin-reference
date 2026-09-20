package com.alejandro.c12generics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  12.3 · Varianza: out, in e invariancia
//
//  QUÉ ES
//    La pregunta "si Gato es un Animal, ¿es `Caja<Gato>` una `Caja<Animal>`?".
//    La respuesta por defecto en Kotlin es NO (invariancia), y `out`/`in` sirven para
//    cambiarla cuando es seguro hacerlo.
//
//  POR QUÉ IMPORTA
//    Es la parte de los genéricos que más cuesta, y la que explica por qué
//    `List<String>` sí encaja donde piden `List<Any>` pero `MutableList<String>` no.
//    Sin entenderla, los errores de "Type mismatch" con genéricos parecen arbitrarios.
//
//  ERRORES COMUNES
//    · Marcar `out` una clase que también consume T (no compila, y con razón).
//    · Confundir la varianza del PARÁMETRO DE TIPO con la del tipo en sí.
//    · No saber que `Array<T>` es invariante en Kotlin (en Java los arrays no lo son,
//      y por eso Java tiene ArrayStoreException).
// =====================================================================================

/**
 * Invariancia: el valor por defecto.
 */
fun demoInvariance() {
    section("Por qué NO es seguro por defecto")

    // Imagina que `Contenedor<Gato>` fuera un `Contenedor<Animal>`. Entonces:
    //
    //     val gatos: Contenedor<Gato> = Contenedor(Gato("Micifuz"))
    //     val animales: Contenedor<Animal> = gatos        // si esto compilara...
    //     animales.guardar(Perro("Toby"))                 // ...meterías un perro
    //     val gato: Gato = gatos.obtener()                // ...y saldría un perro
    //
    // Sería un ClassCastException en tiempo de ejecución. Por eso el compilador lo
    // prohíbe: `Contenedor<T>` es INVARIANTE.

    val gatos = Contenedor(Gato("Micifuz"))
    // val animales: Contenedor<Animal> = gatos   // ERROR: Type mismatch
    show("Contenedor<Gato>.obtener().nombre", gatos.obtener().nombre)

    bullet("Invariante = `Contenedor<Gato>` y `Contenedor<Animal>` no tienen relación,")
    bullet("aunque Gato sí sea un Animal.")

    section("El caso real: MutableList")

    val perros: MutableList<Perro> = mutableListOf(Perro("Toby"))
    // val comoAnimales: MutableList<Animal> = perros   // ERROR
    // comoAnimales.add(Gato("Micifuz"))                // habría un gato entre perros
    show("MutableList<Perro>", perros.map { it.nombre })

    bullet("`MutableList<E>` es invariante justamente por esto.")
}

/**
 * `out`: covarianza, para los que sólo PRODUCEN.
 */
fun demoCovariance() {
    section("Una clase que sólo devuelve T")

    // `Productor<out T>` promete no recibir nunca un T como parámetro. Si sólo
    // produce, sacar un Animal de un productor de Gatos es perfectamente seguro.
    val productorDeGatos: Productor<Gato> = ProductorFijo(Gato("Micifuz"))
    val productorDeAnimales: Productor<Animal> = productorDeGatos   // ✔ compila

    show("Productor<Gato> usado como Productor<Animal>", productorDeAnimales.producir().nombre)
    bullet("Es seguro: lo que salga de ahí siempre será, como mínimo, un Animal.")

    section("El caso real: List")

    val textos: List<String> = listOf("a", "b")
    val cosas: List<Any> = textos              // ✔ porque es List<out E>
    show("List<String> usada como List<Any>", cosas)

    bullet("`List<out E>` es de sólo lectura, así que la covarianza es segura.")
    bullet("Por eso puedes pasar una List<String> a una función que pide List<Any>.")

    section("La regla que impone el compilador")

    // En una clase `out T`, T sólo puede aparecer en posición de SALIDA: tipo de
    // retorno y tipo de propiedades `val`. Nunca como parámetro.
    bullet("Con `out T`, T sólo vale como tipo de retorno o de un `val`.")
    bullet("`fun guardar(valor: T)` en una clase `out T` NO compila.")
    bullet("El mensaje: 'Type parameter T is declared as out but occurs in in position'.")

    section("Sumar tipos con covarianza")

    val animales: List<Animal> = listOf(Gato("Micifuz"), Perro("Toby"))
    show("mezclar gatos y perros en List<Animal>", animales.map { it.nombre })
    show("describirTodos acepta List<Gato>", describirTodos(listOf(Gato("Pelusa"))))
    show("y también List<Perro>", describirTodos(listOf(Perro("Rex"))))
}

/**
 * `in`: contravarianza, para los que sólo CONSUMEN.
 */
fun demoContravariance() {
    section("Una clase que sólo recibe T")

    // `Consumidor<in T>` promete no devolver nunca un T. Si sabe tratar cualquier
    // Animal, también sabe tratar un Gato: por eso un Consumidor<Animal> vale donde
    // se pide un Consumidor<Gato>. Es al revés que la covarianza.
    val consumidorDeAnimales: Consumidor<Animal> = ImprimeNombre()
    val consumidorDeGatos: Consumidor<Gato> = consumidorDeAnimales   // ✔ compila

    show("Consumidor<Animal> usado como Consumidor<Gato>", consumidorDeGatos.consumir(Gato("Micifuz")))

    bullet("Si sabe manejar cualquier Animal, con más razón sabe manejar un Gato.")

    section("El caso real: Comparator")

    // Un Comparator<Animal> puede ordenar una lista de gatos.
    val porNombre: Comparator<Animal> = compareBy { it.nombre }
    val gatos = listOf(Gato("Zoe"), Gato("Ada"), Gato("Micifuz"))
    show("ordenar List<Gato> con Comparator<Animal>", gatos.sortedWith(porNombre).map { it.nombre })

    bullet("`Comparator<in T>` es contravariante por eso mismo.")

    section("La regla que impone el compilador")

    bullet("Con `in T`, T sólo vale como tipo de PARÁMETRO.")
    bullet("`fun obtener(): T` en una clase `in T` NO compila.")

    section("La regla mnemotécnica: PECS")

    bullet("Producer Extends, Consumer Super (viene de Java).")
    bullet("En Kotlin: si PRODUCE, `out`. Si CONSUME, `in`.")
    bullet("Si hace las dos cosas, no puede ser ninguna: se queda invariante.")
}

/**
 * Varianza en el punto de uso (type projections).
 */
fun demoUseSiteVariance() {
    section("Cuando la clase es invariante pero tu función sólo lee")

    // `MutableList<E>` es invariante, así que `copiarDe(MutableList<Animal>)` no
    // aceptaría una `MutableList<Gato>`. Pero si la función SÓLO LEE del origen,
    // se puede proyectar con `out` en el punto de uso.
    val gatos = mutableListOf(Gato("Micifuz"), Gato("Pelusa"))
    val destino = mutableListOf<Animal>()

    copiar(desde = gatos, hacia = destino)
    show("copiar(MutableList<Gato> → MutableList<Animal>)", destino.map { it.nombre })

    bullet("`desde: MutableList<out Animal>` dice 'de aquí sólo leo'.")
    bullet("A cambio, dentro de la función no se puede llamar a `desde.add(...)`.")

    section("Lo mismo con `in`")

    val animales = mutableListOf<Animal>()
    rellenarConGatos(animales)
    show("rellenar MutableList<Animal> con gatos", animales.map { it.nombre })
    bullet("`hacia: MutableList<in Gato>` dice 'aquí sólo escribo gatos'.")

    section("Proyección estrella: `*`")

    // Cuando no te importa el tipo concreto y sólo quieres operaciones que no
    // dependen de él.
    show("tamañoDe(listOf(1,2,3))", tamanoDe(listOf(1, 2, 3)))
    show("tamañoDe(listOf(\"a\"))", tamanoDe(listOf("a")))
    show("describirContenedor", describirContenedor(Contenedor(Gato("Micifuz"))))

    bullet("`List<*>` equivale a `List<out Any?>`: puedes leer como Any?, no escribir.")
    bullet("`MutableList<*>` no deja añadir nada: el compilador no sabe de qué tipo es.")

    section("Declaración frente a uso")

    bullet("Varianza en la DECLARACIÓN (`class Productor<out T>`): la decides una vez")
    bullet("y vale para todos los usos. Es lo preferible cuando puedes.")
    bullet("Varianza en el USO (`fun f(x: List<out Animal>)`): para clases invariantes")
    bullet("que no controlas, o cuando sólo una función necesita la flexibilidad.")
}

// -- Tipos que usan las demos -----------------------------------------------------------

private open class Animal(val nombre: String)
private class Gato(nombre: String) : Animal(nombre)
private class Perro(nombre: String) : Animal(nombre)

/** Invariante: produce Y consume T, así que no puede ser ni `out` ni `in`. */
private class Contenedor<T>(private var valor: T) {
    fun obtener(): T = valor
    fun guardar(nuevo: T) {
        valor = nuevo
    }
}

/** Sólo produce: puede ser covariante. */
private interface Productor<out T> {
    fun producir(): T
}

private class ProductorFijo<out T>(private val valor: T) : Productor<T> {
    override fun producir(): T = valor
}

/** Sólo consume: puede ser contravariante. */
private interface Consumidor<in T> {
    fun consumir(valor: T): String
}

private class ImprimeNombre : Consumidor<Animal> {
    override fun consumir(valor: Animal): String = "consumido: ${valor.nombre}"
}

/** Gracias a que List es `out E`, acepta List<Gato> y List<Perro>. */
private fun describirTodos(animales: List<Animal>): String =
    animales.joinToString { it.nombre }

/** Proyección `out` en el punto de uso: de `desde` sólo se lee. */
private fun copiar(desde: MutableList<out Animal>, hacia: MutableList<Animal>) {
    for (elemento in desde) {
        hacia.add(elemento)
    }
    // desde.add(Gato("x"))   // ERROR: con la proyección `out`, add queda prohibido
}

/** Proyección `in` en el punto de uso: en `hacia` sólo se escribe. */
private fun rellenarConGatos(hacia: MutableList<in Gato>) {
    hacia.add(Gato("Micifuz"))
    hacia.add(Gato("Pelusa"))
    // val g: Gato = hacia[0]   // ERROR: lo que salga de ahí sólo se sabe que es Any?
}

/** Proyección estrella: no nos importa el tipo. */
private fun tamanoDe(lista: List<*>): Int = lista.size

private fun describirContenedor(contenedor: Contenedor<*>): String =
    "contiene algo de tipo ${contenedor.obtener()?.let { it::class.simpleName } ?: "null"}"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `fun guardar(valor: T)` a la interfaz Productor<out T> y lee el error.
//  2. Añade `fun obtener(): T` a Consumidor<in T> y lee el suyo.
//  3. Descomenta `desde.add(Gato("x"))` en `copiar` y comprueba qué prohíbe la proyección.
//  4. Intenta `val animales: Contenedor<Animal> = Contenedor(Gato("x"))` y razona el error.
