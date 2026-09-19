package com.alejandro.c12generics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.reflect.KClass

// =====================================================================================
//  12.4 · Borrado de tipos y `reified`
//
//  QUÉ ES
//    En la JVM, los parámetros de tipo DESAPARECEN al compilar: en tiempo de ejecución
//    `List<String>` y `List<Int>` son exactamente la misma clase. Eso es el "borrado
//    de tipos" (type erasure). `reified` es el truco de Kotlin para recuperarlos.
//
//  POR QUÉ IMPORTA
//    Explica por qué `x is List<String>` no compila, por qué no puedes escribir
//    `T()` para crear una instancia, y por qué `filterIsInstance<String>()` sí
//    funciona.
//
//  ERRORES COMUNES
//    · Intentar `if (x is List<String>)` y no entender el error.
//    · Marcar `reified` sin `inline` (no se puede).
//    · Creer que `reified` resuelve todo: sigue sin poder usarse en clases.
// =====================================================================================

/**
 * El borrado de tipos, demostrado.
 */
fun demoTypeErasure() {
    section("Dos listas de tipos distintos son la misma clase")

    val textos = listOf("a", "b")
    val numeros = listOf(1, 2)

    show("textos::class", textos::class.simpleName)
    show("numeros::class", numeros::class.simpleName)
    show("¿son la misma clase?", textos::class == numeros::class)

    bullet("El `<String>` y el `<Int>` sólo existen para el compilador.")
    bullet("En el bytecode las dos son, simplemente, una lista.")

    section("Consecuencia 1: no se puede preguntar por el tipo completo")

    val cosa: Any = textos

    // if (cosa is List<String>)   // ERROR: Cannot check for instance of erased type
    show("cosa is List<*>  (sí compila)", cosa is List<*>)

    bullet("`List<*>` pregunta 'es una lista de algo', que sí se puede saber.")
    bullet("Preguntar 'de qué' es imposible: esa información ya no existe.")

    section("Consecuencia 2: un cast genérico no comprueba nada")

    @Suppress("UNCHECKED_CAST")
    val comoNumeros = cosa as List<Int>       // el compilador avisa: "Unchecked cast"

    // El cast "funciona" porque no comprueba nada. El fallo llega al USAR el dato.
    val cuandoFalla = try {
        val primero: Int = comoNumeros[0]     // aquí sí: hay un String donde se espera Int
        primero.toString()
    } catch (e: ClassCastException) {
        "lanzó ClassCastException al leer el primer elemento"
    }
    show("(listOf(\"a\") as List<Int>)[0]", cuandoFalla)

    bullet("El aviso 'Unchecked cast' significa exactamente esto: nadie comprueba.")
    bullet("Un `as List<Int>` no falla; falla mucho después, lejos de la causa.")

    section("Consecuencia 3: no se puede instanciar T")

    bullet("`fun <T> crear(): T = T()` no compila: no hay nada que instanciar.")
    bullet("La solución clásica es pasar una fábrica: `crear(fabrica: () -> T)`.")
    show("crearCon { \"hola\" }", crearCon { "hola" })
}

/**
 * `reified`: recuperar el tipo en tiempo de ejecución.
 */
fun demoReified() {
    section("Cómo funciona")

    // `inline` copia el cuerpo de la función en el lugar de la llamada. En esa copia,
    // el compilador ya conoce el tipo concreto, así que puede escribirlo literalmente.
    // Por eso `reified` sólo existe con `inline`.
    show("nombreDelTipo<String>()", nombreDelTipo<String>())
    show("nombreDelTipo<Int>()", nombreDelTipo<Int>())
    show("nombreDelTipo<List<String>>()", nombreDelTipo<List<String>>())

    section("Ahora sí se puede usar `is T`")

    val cosas: List<Any> = listOf("a", 1, "b", 2.0, 3, "c")

    show("soloDeTipo<String>(cosas)", soloDeTipo<String>(cosas))
    show("soloDeTipo<Int>(cosas)", soloDeTipo<Int>(cosas))
    show("soloDeTipo<Double>(cosas)", soloDeTipo<Double>(cosas))

    bullet("Sin `reified`, `it is T` dentro de la función no compilaría.")

    section("El equivalente de la biblioteca estándar")

    // `filterIsInstance` está declarada exactamente así.
    show("cosas.filterIsInstance<String>()", cosas.filterIsInstance<String>())
    bullet("inline fun <reified R> Iterable<*>.filterIsInstance(): List<R>")

    section("Conversión segura con reified")

    show("comoOTal<String>(\"hola\")", comoOTal<String>("hola"))
    show("comoOTal<Int>(\"hola\")", comoOTal<Int>("hola"))
    show("comoOTal<Int>(42)", comoOTal<Int>(42))

    section("Antes de reified: pasar la clase a mano")

    // Es lo que se hace en Java y lo que hacía Kotlin sin reified. Funciona, pero
    // obliga a repetir el tipo dos veces y ensucia la firma.
    show("conClase(String::class, \"hola\")", conClase(String::class, "hola"))
    show("conClase(Int::class, \"hola\")", conClase(Int::class, "hola"))
    bullet("Compara: `conClase(String::class, x)` frente a `comoOTal<String>(x)`.")
}

/**
 * Límites de `reified`.
 */
fun demoReifiedLimits() {
    section("Sólo en funciones inline")

    bullet("`reified` exige `inline`: el tipo se conoce porque el cuerpo se copia.")
    bullet("Una función normal se compila UNA vez para todos los tipos: no hay dónde")
    bullet("escribir el tipo concreto.")

    section("No existe en las clases")

    bullet("`class Caja<reified T>` NO se puede escribir.")
    bullet("Una clase se instancia en ejecución; no hay copia donde meter el tipo.")
    bullet("La solución: guardar la KClass en el constructor.")

    val registro = RegistroDeTipo(String::class)
    show("RegistroDeTipo(String::class).nombre", registro.nombre)
    show("acepta(\"hola\")", registro.acepta("hola"))
    show("acepta(42)", registro.acepta(42))

    // ...o una función de fábrica inline con reified, que es el truco habitual:
    val conFabrica = registroDe<Int>()
    show("registroDe<Int>().nombre", conFabrica.nombre)
    bullet("`inline fun <reified T : Any> registroDe() = RegistroDeTipo(T::class)`")
    bullet("Así quien llama escribe `registroDe<Int>()` y no `RegistroDeTipo(Int::class)`.")

    section("Tampoco se puede llamar a un constructor de T")

    bullet("Con `reified` puedes hacer `T::class`, pero no `T()`.")
    bullet("Crear una instancia por reflexión (`T::class.createInstance()`) es posible")
    bullet("pero necesita kotlin-reflect y falla si no hay constructor vacío. Capítulo 24.")

    section("Efecto secundario: el código crece")

    bullet("Cada llamada a una función inline copia su cuerpo. Con reified no hay")
    bullet("alternativa, pero mantén esas funciones pequeñas.")
}

/**
 * Casos de uso reales.
 */
fun demoReifiedInPractice() {
    section("1. Parsear a un tipo concreto")

    show("parsear<Int>(\"42\")", parsear<Int>("42"))
    show("parsear<Double>(\"3.5\")", parsear<Double>("3.5"))
    show("parsear<Boolean>(\"true\")", parsear<Boolean>("true"))
    show("parsear<Int>(\"abc\")", parsear<Int>("abc"))
    show("parsear<Long>(\"42\") (no soportado)", parsear<Long>("42"))

    bullet("Es el patrón de las librerías de serialización: `json.decode<Usuario>(texto)`.")

    section("2. Buscar en una lista heterogénea")

    val configuracion: List<Any> = listOf("modo=rapido", 8080, true, 3.14, "log=debug")
    show("primeroDeTipo<Int>", primeroDeTipo<Int>(configuracion))
    show("primeroDeTipo<Boolean>", primeroDeTipo<Boolean>(configuracion))
    show("primeroDeTipo<Char>", primeroDeTipo<Char>(configuracion))

    section("3. Agrupar por tipo")

    val porTipo = configuracion.groupBy { it::class.simpleName }
    porTipo.forEach { (tipo, valores) -> show(tipo ?: "?", valores) }
}

// -- Funciones y clases que usan las demos ----------------------------------------------

/** Sin reified: hay que pasar una fábrica para poder "crear un T". */
private fun <T> crearCon(fabrica: () -> T): T = fabrica()

/** Con reified, el tipo concreto está disponible dentro del cuerpo. */
private inline fun <reified T> nombreDelTipo(): String = T::class.simpleName ?: "desconocido"

/** `it is T` sólo es posible con reified. */
private inline fun <reified T> soloDeTipo(cosas: List<Any>): List<T> = cosas.filterIsInstance<T>()

/** `as? T` con reified: conversión segura a un tipo que llega por parámetro de tipo. */
private inline fun <reified T> comoOTal(valor: Any): String {
    val convertido = valor as? T
    return if (convertido != null) "sí, es ${T::class.simpleName}" else "no es ${T::class.simpleName}"
}

/** La forma antigua: pasar la KClass como argumento. */
private fun <T : Any> conClase(tipo: KClass<T>, valor: Any): String =
    if (tipo.isInstance(valor)) "sí, es ${tipo.simpleName}" else "no es ${tipo.simpleName}"

/** Una clase no puede tener `reified`: guarda la KClass. */
private class RegistroDeTipo<T : Any>(private val tipo: KClass<T>) {
    val nombre: String get() = tipo.simpleName ?: "desconocido"
    fun acepta(valor: Any): Boolean = tipo.isInstance(valor)
}

/** El truco habitual: una fábrica inline con reified que construye la clase. */
private inline fun <reified T : Any> registroDe(): RegistroDeTipo<T> = RegistroDeTipo(T::class)

/**
 * Parseo genérico: el tipo de destino decide qué conversión aplicar.
 *
 * `when (T::class)` compara KClass con `==`, que es lo que permite ramificar por el
 * tipo pedido. Sin `reified`, `T::class` ni siquiera se podría escribir.
 */
private inline fun <reified T> parsear(texto: String): String {
    val nombre = T::class.simpleName ?: "desconocido"
    val noConvertible = "no se pudo convertir '$texto' a $nombre"

    return when (T::class) {
        Int::class -> texto.toIntOrNull()?.let { "$it ($nombre)" } ?: noConvertible
        Double::class -> texto.toDoubleOrNull()?.let { "$it ($nombre)" } ?: noConvertible
        Boolean::class -> texto.toBooleanStrictOrNull()?.let { "$it ($nombre)" } ?: noConvertible
        String::class -> "$texto ($nombre)"
        else -> "tipo $nombre no soportado"
    }
}

private inline fun <reified T> primeroDeTipo(cosas: List<Any>): String =
    cosas.filterIsInstance<T>().firstOrNull()?.toString() ?: "(no hay ningún ${T::class.simpleName})"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `cosa is List<String>` y lee el error del borrado de tipos.
//  2. Quita `inline` de `nombreDelTipo` y comprueba que `reified` lo exige.
//  3. Añade soporte para `Long` en `parsear` y prueba `parsear<Long>("42")`.
//  4. Escribe `inline fun <reified T> List<*>.contieneAlgunDe(): Boolean`.
