package com.alejandro.c06nullsafety

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  6.3 · Smart casts, lateinit, platform types y diseño sin nulos
//
//  QUÉ ES
//    El smart cast es la conversión automática que hace el compilador cuando puede
//    demostrar el tipo real de una variable. Aquí también se ven `as?`, `lateinit`,
//    los tipos que llegan de Java, y cómo diseñar para no necesitar nada de esto.
//
//  POR QUÉ IMPORTA
//    Saber POR QUÉ el smart cast a veces no se aplica ahorra horas de pelea con el
//    compilador. Y la última parte es la más importante del capítulo: el mejor código
//    frente a nulos es el que no tiene nulos.
//
//  ERRORES COMUNES
//    · No entender por qué un `if (x != null)` no basta cuando `x` es una propiedad `var`.
//    · Usar `lateinit` para esquivar el diseño en lugar de un valor por defecto.
//    · Devolver `null` para indicar "no encontrado" y también "hubo un error".
// =====================================================================================

/**
 * Smart cast: el compilador recuerda lo que ya has comprobado.
 */
fun demoSmartCasts() {
    section("Con null")

    val texto: String? = "Kotlin"
    if (texto != null) {
        // Aquí `texto` ya es String, no String?. Sin cast, sin `?.`, sin `!!`.
        show("dentro del if, texto.length", texto.length)
        show("dentro del if, texto.uppercase()", texto.uppercase())
    }

    section("Con tipos (is)")

    val cosas: List<Any> = listOf("hola", 42, listOf(1, 2))
    cosas.forEach { cosa ->
        val descripcion = when (cosa) {
            is String -> "String de ${cosa.length} letras"       // cosa es String
            is Int -> "Int; el doble es ${cosa * 2}"             // cosa es Int
            is List<*> -> "List de ${cosa.size} elementos"       // cosa es List<*>
            else -> "otro"
        }
        show("$cosa", descripcion)
    }

    section("También funciona con && y ||")

    val quizas: String? = "Kotlin"
    // Tras `quizas != null &&`, el lado derecho ya lo trata como String.
    show("quizas != null && quizas.length > 3", quizas != null && quizas.length > 3)

    // Y en la negación: tras `== null ||`, el otro lado sabe que NO es null.
    show("quizas == null || quizas.isNotEmpty()", quizas == null || quizas.isNotEmpty())

    section("Y con la salida temprana")

    show("longitudSegura(\"Kotlin\")", longitudSegura("Kotlin"))
    show("longitudSegura(null)", longitudSegura(null))
    bullet("Tras `if (x == null) return`, el resto de la función ve `x` como no nulo.")
}

/**
 * Cuándo el smart cast NO se aplica.
 */
fun demoSmartCastLimits() {
    section("Regla general")

    bullet("El smart cast sólo se aplica si el compilador puede GARANTIZAR que el")
    bullet("valor no ha cambiado entre la comprobación y el uso.")

    section("1. Propiedad `var` de una clase")

    val conVar = ConPropiedadVar("Kotlin")
    show("longitud (hay que ayudar al compilador)", conVar.longitudDelNombre())

    // Dentro de la clase, esto NO compila:
    //     if (nombre != null) { return nombre.length }
    //     → "Smart cast to 'String' is impossible, because 'nombre' is a mutable property
    //        that could have been changed by this time"
    bullet("Otro hilo podría cambiar la propiedad entre el `if` y el uso.")
    bullet("Solución: copiarla a un `val` local (lo que hace `longitudDelNombre`).")

    section("2. Propiedad con getter personalizado")

    val conGetter = ConGetterPersonalizado()
    show("dos lecturas seguidas dan valores distintos", "${conGetter.aleatorio} y ${conGetter.aleatorio}")
    bullet("Un getter puede devolver algo distinto en cada llamada: no hay nada que cachear.")

    section("3. Propiedad `open`")

    bullet("Una subclase podría sobrescribirla con un getter que devuelva null.")
    bullet("El compilador no sabe qué implementación se ejecutará.")

    section("4. Propiedad de otro módulo")

    bullet("El compilador no puede ver si allí es `var`, `open` o tiene getter.")

    section("La solución es siempre la misma")

    bullet("val local = propiedad   ← una copia que no puede cambiar")
    bullet("if (local != null) { ...usa local... }")
    bullet("O el atajo idiomático: `propiedad?.let { ... }`")

    show("con ?.let", conVar.longitudConLet())
}

/**
 * Casts: `as` y `as?`.
 */
fun demoCasts() {
    section("as: cast inseguro")

    val cosa: Any = "Kotlin"
    val comoTexto = cosa as String
    show("cosa as String", comoTexto)

    // Si el tipo no encaja, lanza ClassCastException.
    val numero: Any = 42
    val falloDeCast = try {
        (numero as String).length.toString()
    } catch (e: ClassCastException) {
        "lanzó ClassCastException"
    }
    show("42 as String", falloDeCast)

    section("as?: cast seguro")

    // Si no encaja, devuelve null en lugar de lanzar.
    show("cosa as? String", cosa as? String)
    show("numero as? String", numero as? String)
    show("numero as? String ?: \"no era texto\"", numero as? String ?: "no era texto")

    bullet("`as?` + `?:` es el patrón idiomático para convertir 'si se puede'.")

    section("En la práctica, casi siempre sobra el cast")

    // Con `is` en un `when`, el smart cast hace el trabajo. Capítulo 20.
    val elementos: List<Any> = listOf("a", 1, "b", 2.0)
    show("filterIsInstance<String>()", elementos.filterIsInstance<String>())
    show("mapNotNull { it as? Int }", elementos.mapNotNull { it as? Int })
}

/**
 * `lateinit`: prometer que lo inicializarás antes de usarlo.
 */
fun demoLateinit() {
    section("Para qué existe")

    // Hay propiedades que no se pueden inicializar en el constructor (inyección de
    // dependencias, `setUp()` de un test, `onCreate` en Android) pero que tampoco son
    // conceptualmente nulables. `lateinit` evita tener que declararlas `T?` y
    // salpicar `?.` por toda la clase.
    val servicio = Servicio()

    show("¿está inicializada?", servicio.estaLista())

    val antesDeInicializar = try {
        servicio.conexion
    } catch (e: UninitializedPropertyAccessException) {
        "lanzó UninitializedPropertyAccessException"
    }
    show("acceder antes de inicializar", antesDeInicializar)

    servicio.conectar("postgres://localhost")
    show("¿está inicializada ahora?", servicio.estaLista())
    show("servicio.conexion", servicio.conexion)

    section("Restricciones")

    bullet("Sólo sobre `var`, nunca sobre `val`.")
    bullet("El tipo no puede ser nulable (sería absurdo) ni primitivo (Int, Double...).")
    bullet("No puede tener getter o setter personalizado.")
    bullet("`::propiedad.isInitialized` sólo se puede consultar desde dentro de la clase.")

    section("lateinit vs otras opciones")

    bullet("¿Tiene un valor por defecto razonable? → úsalo, y olvídate de lateinit.")
    bullet("¿Es caro de crear y se usa a veces? → `by lazy` (capítulo 18).")
    bullet("¿De verdad puede no existir? → `T?` y trátalo como tal.")
    bullet("¿Existe siempre pero se asigna después? → ése es el caso de `lateinit`.")
}

/**
 * Platform types: lo que llega de Java.
 */
fun demoPlatformTypes() {
    section("El problema")

    bullet("Java no distingue String de String?. Todo puede ser null.")
    bullet("Si Kotlin tratara todo lo de Java como nulable, la interoperabilidad sería")
    bullet("insoportable: `?.` en cada llamada a cada librería.")

    section("La solución: el tipo plataforma `String!`")

    bullet("`String!` significa 'String o String?, tú decides'.")
    bullet("El compilador NO te obliga a comprobar... pero tampoco te protege.")
    bullet("Nunca lo escribes tú: sólo aparece en los mensajes de error y en el IDE.")

    section("Cómo protegerse")

    bullet("1. Declara el tipo explícitamente al recibir el valor:")
    bullet("     val nombre: String? = claseJava.getNombre()   ← ahora sí te obliga")
    bullet("2. Usa librerías Java anotadas con @Nullable/@NotNull: Kotlin las respeta.")
    bullet("3. Envuelve la API de Java en una capa Kotlin que fije la nulabilidad.")

    bullet("El capítulo 27 lo demuestra con clases Java de verdad en src/main/java.")
}

/**
 * La parte más importante: diseñar para no necesitar nulos.
 */
fun demoDesignWithoutNulls() {
    section("1. Colección vacía en lugar de null")

    // Devolver null para "no hay resultados" obliga a todo el que llame a comprobarlo.
    show("buscarMal(\"zzz\")", buscarMal("zzz"))
    show("buscarBien(\"zzz\")", buscarBien("zzz"))
    show("y se puede encadenar sin comprobar", buscarBien("zzz").map { it.uppercase() })

    bullet("`emptyList()` no cuesta nada: es un singleton compartido.")

    section("2. Valor por defecto en lugar de nulable")

    show("con String? (hay que comprobar)", saludoNulable(null))
    show("con valor por defecto", saludoConDefecto())

    section("3. Un tipo que modele el resultado, en lugar de null")

    // `null` sólo puede decir "no hay valor". No puede decir POR QUÉ.
    show("dividirConNull(10, 0)", dividirConNull(10, 0))
    show("dividirConResultado(10, 0)", dividirConResultado(10, 0))
    show("dividirConResultado(10, 2)", dividirConResultado(10, 2))

    bullet("Con `sealed` (capítulo 11) el resultado lleva el motivo del fallo dentro.")
    bullet("Regla: `null` vale para 'no hay valor'. Para 'hubo un error', modela el error.")

    section("4. Empuja los nulos a los bordes")

    bullet("Valida y convierte en la ENTRADA (parseo, base de datos, API).")
    bullet("A partir de ahí, el dominio trabaja con tipos no nulables.")
    bullet("Así el `?` aparece en tres sitios, no en trescientos.")

    section("Resumen del capítulo")

    bullet("El objetivo no es manejar bien los nulos: es no tenerlos.")
    bullet("Si tu modelo tiene muchos `?`, el problema está en el modelo.")
}

// -- Tipos y funciones auxiliares -----------------------------------------------------

private fun longitudSegura(texto: String?): Int {
    if (texto == null) return 0
    return texto.length      // smart cast: aquí ya es String
}

/** El smart cast no se aplica a una propiedad `var`: hay que copiarla. */
private class ConPropiedadVar(var nombre: String?) {

    fun longitudDelNombre(): Int {
        // `val copia = nombre` congela el valor; sobre la copia sí hay smart cast.
        val copia = nombre ?: return 0
        return copia.length
    }

    /** La misma idea, con el atajo idiomático. */
    fun longitudConLet(): Int = nombre?.let { it.length } ?: 0
}

/** Un getter personalizado puede devolver algo distinto en cada lectura. */
private class ConGetterPersonalizado {
    private var contador = 0
    val aleatorio: Int
        get() = ++contador
}

/** Caso de libro de `lateinit`: se asigna después de construir el objeto. */
private class Servicio {
    lateinit var conexion: String

    /** `::conexion.isInitialized` sólo es accesible desde dentro de la clase. */
    fun estaLista(): Boolean = ::conexion.isInitialized

    fun conectar(url: String) {
        conexion = url
    }
}

private val catalogo = listOf("kotlin", "java", "scala")

/** Devuelve null cuando no hay resultados: obliga a comprobar a quien llame. */
private fun buscarMal(termino: String): List<String>? =
    catalogo.filter { it.contains(termino) }.ifEmpty { null }

/** Devuelve una lista vacía: quien llame puede encadenar sin más. */
private fun buscarBien(termino: String): List<String> =
    catalogo.filter { it.contains(termino) }

private fun saludoNulable(nombre: String?): String = "Hola, ${nombre ?: "invitado"}"
private fun saludoConDefecto(nombre: String = "invitado"): String = "Hola, $nombre"

/** `null` no puede explicar el motivo. */
private fun dividirConNull(a: Int, b: Int): Int? = if (b == 0) null else a / b

/** Un tipo propio sí puede. Versión mínima de lo que hará el capítulo 11. */
private fun dividirConResultado(a: Int, b: Int): String =
    if (b == 0) "Error(división por cero)" else "Exito(${a / b})"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Dentro de ConPropiedadVar, escribe `if (nombre != null) return nombre.length`
//     y lee el error completo del compilador: explica exactamente por qué.
//  2. Cambia `var nombre` por `val nombre` y comprueba que entonces sí compila.
//  3. Llama a `servicio.conexion` antes de `conectar` sin el try y mira la traza.
//  4. Reescribe `dividirConResultado` con una sealed class de verdad (capítulo 11).
