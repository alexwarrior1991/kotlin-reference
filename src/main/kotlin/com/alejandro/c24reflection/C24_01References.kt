package com.alejandro.c24reflection

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.reflect.KClass

// =====================================================================================
//  24.1 · KClass y referencias: lo que NO necesita kotlin-reflect
//
//  QUÉ ES
//    `::class` da el KClass de un valor o de un tipo. `::funcion` y `Clase::propiedad`
//    dan referencias invocables. Todo esto viene en la biblioteca estándar.
//
//  POR QUÉ IMPORTA
//    Hay una división importante que casi nadie conoce: `::class`, `simpleName`,
//    `isInstance` y las referencias a funciones funcionan SIN la dependencia
//    `kotlin-reflect`. La API pesada (`memberProperties`, `primaryConstructor`) sí la
//    necesita, y si falta, el fallo llega EN EJECUCIÓN.
//
//  ERRORES COMUNES
//    · Confundir `::class` (KClass de Kotlin) con `::class.java` (Class de Java).
//    · Usar reflexión donde bastaba una interfaz o un `when` sobre una sealed.
//    · Olvidar la dependencia kotlin-reflect y descubrirlo en producción.
// =====================================================================================

private open class Animal(val nombre: String)
private class Gato(nombre: String, val vidas: Int = 7) : Animal(nombre)
private data class Usuario(val id: Int, val nombre: String, val email: String)

/**
 * `::class` sobre valores y sobre tipos.
 */
fun demoKClass() {
    section("Sobre un VALOR: el tipo real en ejecución")

    val animal: Animal = Gato("Micifuz")

    show("animal::class.simpleName", animal::class.simpleName)
    show("animal::class.qualifiedName", animal::class.qualifiedName)

    bullet("Fíjate: la variable es de tipo Animal, pero `::class` da Gato.")
    bullet("`::class` sobre un valor devuelve su clase REAL, no la declarada.")

    section("Sobre un TIPO: la clase escrita")

    show("Animal::class.simpleName", Animal::class.simpleName)
    show("Gato::class.simpleName", Gato::class.simpleName)
    show("String::class.simpleName", String::class.simpleName)

    section("Lo que se puede preguntar sin kotlin-reflect")

    show("isInstance(animal)", Gato::class.isInstance(animal))
    show("isInstance de un String", Gato::class.isInstance("texto"))
    show("Gato::class == animal::class", Gato::class == animal::class)

    section("KClass frente a Class de Java")

    show("Kotlin: ::class", Gato::class.toString())
    show("Java:   ::class.java", Gato::class.java.simpleName)
    show("de Java a Kotlin: .kotlin", Gato::class.java.kotlin.simpleName)

    bullet("`KClass` es el de Kotlin y conoce sus conceptos (data class, object,")
    bullet("propiedades, funciones de extensión).")
    bullet("`Class` es el de Java y es lo que piden muchas APIs y librerías.")
    bullet("Se convierte con `.java` y `.kotlin` en cada sentido.")

    section("Propiedades útiles de KClass")

    show("isData", Usuario::class.isData)
    show("isAbstract", Animal::class.isAbstract)
    show("isFinal", Gato::class.isFinal)
    show("isCompanion", Usuario::class.isCompanion)

    bullet("Estas tres últimas sí necesitan kotlin-reflect. `simpleName`,")
    bullet("`qualifiedName` e `isInstance`, no.")
}

/**
 * Referencias a funciones.
 */
fun demoFunctionReferences() {
    section("A una función de nivel superior")

    val referencia = ::duplicar
    show("::duplicar", referencia(21))
    show("su nombre", referencia.name)

    show("usada como parámetro", listOf(1, 2, 3).map(::duplicar))

    section("A un método de una clase (sin instancia)")

    // `Clase::metodo` es una función que recibe la instancia como primer parámetro.
    val obtenerNombre = Animal::nombre
    val gato = Gato("Micifuz")
    show("Animal::nombre aplicado a un gato", obtenerNombre(gato))

    show("usada en un map", listOf(Gato("Ada"), Gato("Zoe")).map(Animal::nombre))

    section("A un método de una instancia concreta (ligada)")

    val texto = "Kotlin"

    // Detalle importante: `uppercase` y `contains` tienen VARIAS sobrecargas, así
    // que `texto::uppercase` a secas es ambiguo. Declarar el tipo función esperado
    // le dice al compilador cuál de ellas quieres.
    val enMayusculas: () -> String = texto::uppercase
    show("texto::uppercase", enMayusculas())

    val contiene: (String) -> Boolean = texto::contains
    show("texto::contains(\"lin\")", contiene("lin"))

    bullet("En la referencia ligada, el receptor ya está fijado.")
    bullet("Si el método tiene sobrecargas, escribe el tipo función esperado:")
    bullet("  val f: () -> String = texto::uppercase")
    bullet("Sin él, el error es 'Overload resolution ambiguity'.")

    section("A un constructor")

    val crearGato = ::Gato
    show("::Gato(\"Pelusa\", 9)", crearGato("Pelusa", 9).nombre)

    val nombres = listOf("Ada", "Zoe")
    show("nombres.map(::Animal)", nombres.map(::Animal).map { it.nombre })

    section("Nada de esto necesita kotlin-reflect")

    bullet("Las referencias se compilan a clases normales; sólo si INSPECCIONAS")
    bullet("la referencia (parámetros, tipo de retorno, anotaciones) hace falta")
    bullet("la dependencia.")
    bullet("`referencia.name` sí funciona sin ella. `referencia.parameters`, no.")
}

/**
 * Referencias a propiedades.
 */
fun demoPropertyReferences() {
    section("Leer una propiedad")

    val usuarios = listOf(
        Usuario(1, "Ana", "ana@ejemplo.com"),
        Usuario(2, "Luis", "luis@ejemplo.com"),
    )

    show("map(Usuario::nombre)", usuarios.map(Usuario::nombre))
    show("sortedBy(Usuario::id)", usuarios.sortedBy(Usuario::id).map { it.nombre })
    show("associateBy(Usuario::id)", usuarios.associateBy(Usuario::id).keys)
    show("sumOf(Usuario::id)", usuarios.sumOf(Usuario::id))

    bullet("`Usuario::nombre` es una `KProperty1<Usuario, String>`, que también es")
    bullet("una función `(Usuario) -> String`. Por eso encaja en `map`.")

    section("El nombre de la propiedad")

    val propiedad = Usuario::email
    show("propiedad.name", propiedad.name)
    show("propiedad.get(usuario)", propiedad.get(usuarios[0]))
    show("propiedad(usuario)  (lo mismo)", propiedad(usuarios[0]))

    bullet("`.name` es lo que permite escribir mensajes genéricos que dicen QUÉ")
    bullet("campo falló, sin escribir el nombre a mano en una cadena.")

    section("Propiedades mutables")

    val contador = Contador()
    val propiedadMutable = Contador::valor

    show("antes", propiedadMutable.get(contador))
    propiedadMutable.set(contador, 42)
    show("tras set", contador.valor)

    bullet("`KMutableProperty1` tiene `set` además de `get`.")
    bullet("Sólo aparece si la propiedad es `var`.")

    section("Referencias ligadas a propiedades")

    val usuario = usuarios[0]
    val suNombre = usuario::nombre
    show("usuario::nombre", suNombre.get())
    bullet("Sin parámetros: el objeto ya está dentro de la referencia.")

    section("Un uso típico: validación con el nombre incluido")

    show("validar(usuario, Usuario::nombre)", validarNoVacio(usuario, Usuario::nombre))
    val vacio = Usuario(3, "", "x@y.z")
    show("con el nombre vacío", validarNoVacio(vacio, Usuario::nombre))
    show("con el email lleno", validarNoVacio(vacio, Usuario::email))

    bullet("El mensaje de error dice el nombre del campo sin haberlo escrito.")
    bullet("Si alguien renombra la propiedad, el mensaje se actualiza solo.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

private fun duplicar(valor: Int): Int = valor * 2

private class Contador {
    var valor: Int = 0
}

/**
 * Valida usando una referencia a propiedad, que aporta el VALOR y el NOMBRE.
 */
private fun <T> validarNoVacio(objeto: T, propiedad: kotlin.reflect.KProperty1<T, String>): String {
    val valor = propiedad.get(objeto)
    return if (valor.isBlank()) "el campo '${propiedad.name}' está vacío" else "'${propiedad.name}' correcto"
}

/** Sólo para ilustrar la firma en la demo; no se usa. */
@Suppress("unused")
private fun tipoDe(clase: KClass<*>): String = clase.simpleName ?: "desconocido"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Declara `val a: Animal = Gato("x")` y compara `a::class` con `Animal::class`.
//  2. Usa `validarNoVacio` con una propiedad Int y comprueba que el tipo no encaja.
//  3. Prueba `Usuario::nombre.set(...)`: no existe, porque la propiedad es `val`.
//  4. Convierte `listOf("a","b").map(::Animal)` para que use una lambda y compara.
