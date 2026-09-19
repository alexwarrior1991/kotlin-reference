package com.alejandro.c20typechecks

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  20.2 · Casts: `as` y `as?`
//
//  QUÉ ES
//    `as` convierte a un tipo y lanza si no encaja. `as?` devuelve `null` en ese caso.
//
//  POR QUÉ IMPORTA
//    En Kotlin idiomático los casts explícitos son raros: el smart cast hace el
//    trabajo. Cuando aparece un `as`, casi siempre es una frontera con Java, con
//    genéricos borrados, o una señal de que falta un tipo mejor.
//
//  ERRORES COMUNES
//    · `as` sobre datos de fuera (JSON, Bundle, Map<String, Any>) sin comprobar.
//    · Ignorar el aviso "Unchecked cast" en casts de genéricos.
//    · Usar `as` donde `as?` + `?:` daría un camino de fallo controlado.
// =====================================================================================

/**
 * `as`: el cast inseguro.
 */
fun demoUnsafeCast() {
    section("Cuando encaja, funciona")

    val cosa: Any = "Kotlin"
    val texto = cosa as String
    show("cosa as String", texto)
    show("y ya se puede usar como String", texto.length)

    section("Cuando no encaja, lanza")

    val numero: Any = 42
    val fallo = try {
        (numero as String).length.toString()
    } catch (e: ClassCastException) {
        "lanzó ClassCastException"
    }
    show("42 as String", fallo)

    section("Con nulos")

    val nulo: Any? = null

    // `as String` sobre null lanza, porque String no admite null.
    val nuloComoNoNulable = try {
        (nulo as String).length.toString()
    } catch (e: NullPointerException) {
        "lanzó NullPointerException"
    }
    show("null as String", nuloComoNoNulable)

    // `as String?` sí lo acepta: el tipo destino admite null.
    val nuloComoNulable = nulo as String?
    show("null as String?", nuloComoNulable)

    bullet("`as T` exige que el valor NO sea null; `as T?` lo permite.")

    section("Cuándo `as` es razonable")

    bullet("Cuando tú GARANTIZAS el tipo y quieres que reviente si te equivocas.")
    bullet("Es el equivalente a `!!`: una afirmación tuya, no una comprobación.")
    bullet("Como con `!!`, si lo escribes, deja al lado el porqué.")
}

/**
 * `as?`: el cast seguro.
 */
fun demoSafeCast() {
    section("Devuelve null en lugar de lanzar")

    val cosas: List<Any> = listOf("texto", 42, 3.14, true)

    cosas.forEach { cosa ->
        show("$cosa as? String", cosa as? String)
    }

    section("El patrón idiomático: as? + ?:")

    cosas.forEach { cosa ->
        val longitud = (cosa as? String)?.length ?: -1
        show("longitud de $cosa", longitud)
    }

    bullet("`(x as? T)?.algo ?: porDefecto` resuelve el caso en una línea.")

    section("as? con nulos")

    val nulo: Any? = null
    show("null as? String", nulo as? String)
    bullet("`as?` nunca lanza: ni por tipo incorrecto ni por null.")

    section("Equivalencias")

    val valor: Any = "hola"

    val conWhen = when (valor) {
        is String -> valor.uppercase()
        else -> null
    }

    show("con as?", (valor as? String)?.uppercase())
    show("con is + smart cast", if (valor is String) valor.uppercase() else null)
    show("con when", conWhen)

    bullet("Las tres formas hacen lo mismo. `as?` gana cuando encadenas después;")
    bullet("`is` gana cuando el bloque tiene varias líneas.")
}

/**
 * Casts y genéricos: el aviso "Unchecked cast".
 */
fun demoGenericCasts() {
    section("El problema")

    val lista: Any = listOf("a", "b", "c")

    // `is List<String>` NO compila (capítulo 12.12): el tipo está borrado.
    // Lo máximo que se puede comprobar es que sea una lista de ALGO.
    show("lista is List<*>", lista is List<*>)

    section("Y el cast no comprueba nada")

    @Suppress("UNCHECKED_CAST")
    val comoNumeros = lista as List<Int>       // aviso: Unchecked cast

    // El cast "funciona". El fallo llega al usar el dato, lejos de aquí.
    val cuandoFalla = try {
        val suma = comoNumeros.sum()
        "suma: $suma"
    } catch (e: ClassCastException) {
        "lanzó ClassCastException al sumar"
    }
    show("(listOf(\"a\") as List<Int>).sum()", cuandoFalla)

    bullet("Ése es el significado exacto del aviso: NADIE está comprobando.")
    bullet("El error aparecerá en otra función, con una traza que no señala la causa.")

    section("La forma correcta: comprobar elemento a elemento")

    show("filterIsInstance<String>()", listOf<Any>("a", 1, "b").filterIsInstance<String>())

    // Y si de verdad necesitas una List<T> completa y validada:
    show("comoListaDe<String>(lista)", comoListaDe<String>(lista))
    show("comoListaDe<Int>(lista)", comoListaDe<Int>(lista))
    show("comoListaDe<Int>(listOf(1,2))", comoListaDe<Int>(listOf(1, 2)))

    bullet("`filterIsInstance` comprueba CADA elemento: no hay sorpresas después.")
    bullet("Cuesta una pasada, pero convierte un fallo tardío en un resultado correcto.")

    section("Dónde aparece esto en la vida real")

    bullet("Leer de un Map<String, Any?> (JSON parseado, configuración, Bundle).")
    bullet("APIs de Java que devuelven colecciones sin genéricos.")
    bullet("Deserialización hecha a mano.")
    bullet("En todos esos casos: valida al ENTRAR, y a partir de ahí usa tipos reales.")
}

/**
 * Casts en jerarquías sealed.
 */
fun demoSealedHierarchies() {
    section("Con una sealed, los casts sobran")

    val eventos: List<Evento> = listOf(
        Evento.Click(10, 20),
        Evento.Teclado('k'),
        Evento.Scroll(-3),
    )

    eventos.forEach { evento ->
        show(evento::class.simpleName ?: "?", manejar(evento))
    }

    bullet("Ni un solo `as`: el `when` con `is` da smart cast en cada rama.")
    bullet("Y al ser exhaustivo, añadir un caso nuevo rompe la compilación aquí.")

    section("Filtrar por subtipo")

    show("sólo los Click", eventos.filterIsInstance<Evento.Click>().map { "(${it.x},${it.y})" })
    show("el primer Scroll", eventos.filterIsInstance<Evento.Scroll>().firstOrNull()?.delta)

    section("Cuando sí hace falta un cast")

    // Caso real: una API que devuelve el supertipo y tú sabes cuál es por contexto.
    val ultimo: Evento = eventos.last()
    show("con as? y valor por defecto", (ultimo as? Evento.Scroll)?.delta ?: 0)

    bullet("Incluso aquí, `as?` es mejor que `as`: el camino de fallo es explícito.")

    section("La regla final del capítulo")

    bullet("1. Diseña con sealed y `when` exhaustivo → no necesitas casts.")
    bullet("2. Si no puedes, usa `is` y deja que el smart cast trabaje.")
    bullet("3. Si necesitas una conversión, usa `as?` con un `?:` detrás.")
    bullet("4. `as` a secas sólo cuando estés afirmando una invariante tuya.")
    bullet("5. Un cast de genéricos con @Suppress es una deuda: documéntala.")
}

// -- Los tipos y funciones que usan las demos ------------------------------------------------

/**
 * Convierte de forma segura: sólo devuelve la lista si TODOS los elementos encajan.
 *
 * Usa `reified` (capítulo 12.13) para poder preguntar por `T` en ejecución.
 */
private inline fun <reified T> comoListaDe(valor: Any): String {
    if (valor !is List<*>) return "no es una lista"
    val todos = valor.all { it is T }
    return if (todos) "lista válida de ${T::class.simpleName}: $valor"
    else "la lista contiene elementos que no son ${T::class.simpleName}"
}

private sealed interface Evento {
    data class Click(val x: Int, val y: Int) : Evento
    data class Teclado(val tecla: Char) : Evento
    data class Scroll(val delta: Int) : Evento
}

private fun manejar(evento: Evento): String = when (evento) {
    is Evento.Click -> "clic en (${evento.x}, ${evento.y})"
    is Evento.Teclado -> "tecla '${evento.tecla}'"
    is Evento.Scroll -> "desplazamiento de ${evento.delta}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el @Suppress del cast de genéricos y lee el aviso completo.
//  2. Cambia `as?` por `as` en el patrón `(cosa as? String)?.length ?: -1` y mira qué
//     pasa con los elementos que no son texto.
//  3. Añade `Evento.Gesto` y comprueba que `manejar` deja de compilar.
//  4. Escribe `inline fun <reified T> Any.comoOTal(): T?` y compárala con `as?`.
