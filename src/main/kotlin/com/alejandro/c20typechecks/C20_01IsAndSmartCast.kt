package com.alejandro.c20typechecks

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  20.1 · `is`, `!is` y smart casts
//
//  QUÉ ES
//    `is` comprueba el tipo en tiempo de ejecución. El smart cast es la conversión
//    automática que hace el compilador después de esa comprobación.
//
//  POR QUÉ IMPORTA
//    En Java hay que escribir `if (x instanceof String) { String s = (String) x; ... }`,
//    con el tipo repetido tres veces. En Kotlin el compilador recuerda lo que ya has
//    comprobado y te ahorra el cast. El capítulo 6.9 lo presentó para nulos; aquí se
//    ve para tipos en general.
//
//  ERRORES COMUNES
//    · No entender por qué el smart cast no se aplica a una propiedad `var`.
//    · Comprobar tipos con `is` en una cadena larga cuando lo que falta es polimorfismo.
//    · Intentar `is List<String>` y chocar con el borrado de tipos (capítulo 12.12).
// =====================================================================================

/**
 * Lo básico.
 */
fun demoIsBasics() {
    section("is y !is")

    // Ojo al tipo: como hay un `null`, la lista es List<Any?>, no List<Any>.
    val cosas: List<Any?> = listOf("texto", 42, 3.14, listOf(1, 2), true, null)

    cosas.forEach { cosa ->
        show("$cosa", describir(cosa))
    }

    section("El smart cast en acción")

    val valor: Any = "Kotlin"

    if (valor is String) {
        // Aquí `valor` ya es String: se puede llamar a .length sin ningún cast.
        show("dentro del if, valor.length", valor.length)
        show("dentro del if, valor.uppercase()", valor.uppercase())
    }

    bullet("Sin smart cast habría que escribir `(valor as String).length`.")

    section("También con !is y salida temprana")

    show("longitudSiEsTexto(\"hola\")", longitudSiEsTexto("hola"))
    show("longitudSiEsTexto(42)", longitudSiEsTexto(42))

    bullet("Tras `if (x !is String) return`, el resto de la función ve `x` como String.")

    section("Y en las condiciones compuestas")

    val otro: Any = "abc"
    show("otro is String && otro.length > 2", otro is String && otro.length > 2)
    show("otro !is String || otro.isEmpty()", otro !is String || otro.isEmpty())

    bullet("Tras `x is String &&`, el lado derecho ya lo trata como String.")
    bullet("Tras `x !is String ||`, el lado derecho también, porque si llegamos ahí")
    bullet("es que la primera condición fue falsa, o sea que SÍ es String.")
}

/**
 * `is` en un `when`.
 */
fun demoIsInWhen() {
    section("La forma idiomática de ramificar por tipo")

    val entradas: List<Any> = listOf(
        "texto",
        42,
        listOf("a", "b"),
        mapOf("k" to 1),
        3.14,
        Persona("Ana", 34),
    )

    entradas.forEach { entrada ->
        show(entrada::class.simpleName ?: "?", formatear(entrada))
    }

    bullet("Cada rama tiene smart cast al tipo comprobado. Sin casts manuales.")

    section("Con jerarquías sealed, además es exhaustivo")

    val formas: List<Forma> = listOf(
        Circulo(2.0),
        Cuadrado(3.0),
        Rectangulo(2.0, 5.0),
    )
    formas.forEach { show(it::class.simpleName ?: "?", "área=%.2f".format(area(it))) }

    bullet("Aquí `when` no necesita `else`: el compilador sabe que están todas.")
    bullet("Es el capítulo 11 aplicado. Con `Any` sí haría falta el `else`.")

    section("Cuándo el `is` es una señal de mal diseño")

    // Si tienes un `when (x) { is A -> ...; is B -> ... }` sobre TUS PROPIAS clases
    // y se repite en varios sitios, lo que falta es un método en la jerarquía.
    bullet("Un `when` sobre tipos propios repetido en cinco sitios → falta polimorfismo.")
    bullet("Un `when` sobre una sealed, en UN sitio, para pintar la UI → perfecto.")
    bullet("La diferencia: ¿el comportamiento pertenece al tipo, o al que lo usa?")

    show("con polimorfismo, no hace falta `is`", formas.joinToString { it.nombre() })
}

/**
 * Cuándo el smart cast NO funciona.
 */
fun demoSmartCastLimits() {
    section("1. Propiedad `var` de una clase")

    val contenedor = ContenedorMutable("texto")
    show("longitud (con copia local)", contenedor.longitudDelContenido())

    // Dentro de la clase esto NO compila:
    //     if (contenido is String) return contenido.length
    //     → "Smart cast to 'String' is impossible, because 'contenido' is a mutable
    //        property that could have been changed by this time"
    bullet("Otro hilo podría cambiarla entre el `is` y el uso.")
    bullet("Solución: `val copia = contenido` y comprobar sobre la copia.")

    section("2. Propiedad con getter personalizado")

    val conGetter = ConGetterVariable()
    show("dos lecturas seguidas", "${conGetter.valor} y ${conGetter.valor}")
    bullet("Un getter puede devolver algo distinto cada vez: no hay nada que recordar.")

    section("3. Propiedad `open`")

    bullet("Una subclase podría sobrescribirla con un getter que devuelva otra cosa.")

    section("4. Propiedad de otro módulo")

    bullet("El compilador no puede ver si allí es `var`, `open` o calculada.")

    section("5. Variable local capturada y modificada por una lambda")

    // Si una lambda que se ejecuta después puede cambiar la variable, el compilador
    // deja de fiarse.
    var capturada: Any = "texto"
    val cambiar = { capturada = 42 }
    show("con lambda que la modifica", longitudDe(capturada))
    cambiar()
    show("tras ejecutar la lambda", longitudDe(capturada))

    bullet("Aquí se usa una función auxiliar precisamente porque dentro de esta")
    bullet("función el smart cast sobre `capturada` estaría bloqueado.")

    section("La solución, siempre la misma")

    bullet("Copia a un `val` local y comprueba sobre la copia.")
    bullet("Un `val` local no puede cambiar, así que el compilador sí se fía.")
}

// -- Las funciones y tipos que usan las demos ------------------------------------------------

private fun describir(cosa: Any?): String = when {
    cosa == null -> "es null"
    cosa is String -> "String de ${cosa.length} caracteres"
    cosa is Int -> "Int; su doble es ${cosa * 2}"
    cosa is Double -> "Double redondeado: ${Math.round(cosa)}"
    cosa is List<*> -> "List de ${cosa.size} elementos"
    cosa !is Boolean -> "no es booleano y no sé qué es"
    else -> "Boolean con valor $cosa"
}

private fun longitudSiEsTexto(valor: Any): Int {
    if (valor !is String) return -1
    // A partir de aquí, `valor` es String.
    return valor.length
}

private fun longitudDe(valor: Any): String = if (valor is String) "${valor.length}" else "no es texto"

private data class Persona(val nombre: String, val edad: Int)

private fun formatear(valor: Any): String = when (valor) {
    is String -> "«$valor»"
    is Int -> "entero $valor"
    is Double -> "decimal %.2f".format(valor)
    is List<*> -> "lista [${valor.joinToString()}]"
    is Map<*, *> -> "mapa con ${valor.size} entradas"
    is Persona -> "${valor.nombre}, ${valor.edad} años"
    else -> "desconocido"
}

// Una jerarquía sellada, para el `when` exhaustivo.
private sealed interface Forma {
    fun nombre(): String
}

private data class Circulo(val radio: Double) : Forma {
    override fun nombre(): String = "círculo"
}

private data class Cuadrado(val lado: Double) : Forma {
    override fun nombre(): String = "cuadrado"
}

private data class Rectangulo(val ancho: Double, val alto: Double) : Forma {
    override fun nombre(): String = "rectángulo"
}

private fun area(forma: Forma): Double = when (forma) {
    is Circulo -> Math.PI * forma.radio * forma.radio
    is Cuadrado -> forma.lado * forma.lado
    is Rectangulo -> forma.ancho * forma.alto
}

private class ContenedorMutable(var contenido: Any) {
    fun longitudDelContenido(): Int {
        val copia = contenido        // congelamos el valor
        return if (copia is String) copia.length else -1
    }
}

private class ConGetterVariable {
    private var contador = 0
    val valor: Int get() = ++contador
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Dentro de ContenedorMutable, escribe el `if` sobre `contenido` directamente y
//     lee el error completo: explica exactamente el motivo.
//  2. Cambia `var contenido` por `val contenido` y comprueba que entonces sí compila.
//  3. Añade `Triangulo` a la jerarquía Forma y mira qué `when` dejan de compilar.
//  4. Convierte el `when (valor) { is ... }` de `formatear` en un método `formatear()`
//     de cada clase y decide cuál prefieres.
