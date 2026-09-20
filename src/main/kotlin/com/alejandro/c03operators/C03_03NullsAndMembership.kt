package com.alejandro.c03operators

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  3.3 · Operadores de nulos, pertenencia y qué se puede sobrecargar
//
//  QUÉ ES
//    Los tres operadores que definen la seguridad frente a nulos (`?.`, `?:`, `!!`),
//    el operador de pertenencia (`in`), y el mapa completo de qué operadores se pueden
//    redefinir para tus propias clases.
//
//  POR QUÉ IMPORTA
//    Aquí sólo vemos la MECÁNICA; el capítulo 6 explica el sistema de tipos que hay
//    detrás. Pero merece la pena verlos como operadores, junto a los demás, para
//    entender qué traduce el compilador y qué no.
//
//  ERRORES COMUNES
//    · Encadenar `!!` para "quitarse los avisos de encima".
//    · Escribir `?: return` sin darse cuenta de que sale de la función entera.
//    · Creer que `?:` se puede sobrecargar. No se puede.
// =====================================================================================

/**
 * `?.` — llamada segura.
 */
fun demoSafeCall() {
    section("Con y sin valor")

    val conTexto: String? = "Kotlin"
    val sinTexto: String? = null

    // Si el receptor es null, toda la expresión vale null y NO se llama al método.
    show("conTexto?.length", conTexto?.length)
    show("sinTexto?.length", sinTexto?.length)

    bullet("El resultado de `x?.foo()` es siempre nullable, aunque foo() no lo sea.")

    section("Encadenar")

    val conDireccion = Usuario("Ana", Direccion("Calle Mayor", Ciudad("Madrid")))
    val sinDireccion = Usuario("Luis", null)

    // Basta con que UN eslabón sea null para que toda la cadena dé null.
    show("conDireccion.direccion?.ciudad?.nombre", conDireccion.direccion?.ciudad?.nombre)
    show("sinDireccion.direccion?.ciudad?.nombre", sinDireccion.direccion?.ciudad?.nombre)

    bullet("Sin `?.` harías tres `if != null` anidados. Ésta es la razón de ser del operador.")

    section("?. con let: ejecutar sólo si no es nulo")

    // `?.let { }` ejecuta el bloque únicamente cuando el valor existe. Capítulo 16.
    conTexto?.let { show("conTexto?.let { ... }", "se ejecutó con '$it'") }
    sinTexto?.let { show("sinTexto?.let { ... }", "esto NO se imprime") }
    show("sinTexto?.let devolvió", sinTexto?.let { it.length })
}

/**
 * `?:` — el operador Elvis (se llama así porque de lado parece un tupé).
 */
fun demoElvis() {
    section("Valor por defecto")

    val nombre: String? = null
    val nombreSeguro = nombre ?: "invitado"
    show("nombre ?: \"invitado\"", nombreSeguro)

    val presente: String? = "Ana"
    show("presente ?: \"invitado\"", presente ?: "invitado")

    section("Combinado con ?.")

    // Éste es el patrón más común de todo Kotlin:
    //     valorQuizaNulo?.transformacion() ?: valorPorDefecto
    val usuario = Usuario("Luis", null)
    val ciudad = usuario.direccion?.ciudad?.nombre ?: "desconocida"
    show("usuario.direccion?.ciudad?.nombre ?: \"desconocida\"", ciudad)

    section("A la derecha puede ir cualquier expresión... incluso `return`")

    // Como `return` y `throw` son de tipo Nothing, encajan a la derecha del Elvis.
    // Es la forma idiomática de las guardas al principio de una función.
    show("longitudDe(\"hola\")", longitudDe("hola"))
    show("longitudDe(null)", longitudDe(null))

    val conThrow = try {
        exigeNombre(null)
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("exigeNombre(null)", conThrow)
    show("exigeNombre(\"Ana\")", exigeNombre("Ana"))

    section("Elvis no se puede sobrecargar")

    bullet("`?:` forma parte de la gramática, no es una función con nombre.")
    bullet("Tampoco `?.`, `!!`, `&&`, `||`, `!`, `===` ni `=`.")
}

/**
 * `!!` — la aserción de no-nulo. El operador que hay que aprender a NO usar.
 */
fun demoNotNullAssertion() {
    section("Qué hace")

    val texto: String? = "existe"
    show("texto!!.length", texto!!.length)

    // Sobre un null, lanza NullPointerException. Lo capturamos porque en este
    // repositorio ninguna demo puede propagar excepciones.
    val nulo: String? = null
    val resultado = try {
        nulo!!.length.toString()
    } catch (e: NullPointerException) {
        "lanzó NullPointerException"
    }
    show("nulo!!.length", resultado)

    section("Por qué casi nunca es la respuesta")

    bullet("`!!` convierte un problema de compilación en un fallo en producción.")
    bullet("`a!!.b!!.c!!` es la señal de que el modelo de datos está mal diseñado.")
    bullet("Alternativas por orden de preferencia: ?., ?:, ?.let, requireNotNull().")

    section("Cuándo es aceptable")

    // requireNotNull deja un mensaje que se puede leer; `!!` no deja nada.
    val configurado: String? = "valor"
    val conMensaje = requireNotNull(configurado) { "La configuración no estaba inicializada" }
    show("requireNotNull(x) { \"mensaje\" }", conMensaje)

    bullet("Cuando TÚ garantizas el invariante y el compilador no puede verlo.")
    bullet("Incluso entonces, requireNotNull/checkNotNull dan un mensaje útil. Capítulo 25.")
}

/**
 * `in` — pertenencia. Se traduce a `contains`.
 */
fun demoMembership() {
    section("in → contains")

    val numeros = listOf(1, 2, 3)
    show("2 in numeros   →  numeros.contains(2)", "${2 in numeros}  ==  ${numeros.contains(2)}")
    show("9 in numeros", 9 in numeros)
    show("9 !in numeros", 9 !in numeros)

    section("Funciona con todo lo que defina contains")

    show("5 in 1..10           (rango)", 5 in 1..10)
    show("'o' in \"Kotlin\"      (cadena)", 'o' in "Kotlin")
    show("\"lin\" in \"Kotlin\"    (subcadena)", "lin" in "Kotlin")
    show("\"a\" in setOf(\"a\",\"b\")  (set)", "a" in setOf("a", "b"))
    show("\"clave\" in mapOf(\"clave\" to 1)", "clave" in mapOf("clave" to 1))

    section("Donde de verdad luce: en un `when`")

    // Compara esto con `edad >= 0 && edad <= 12`. Capítulo 4.
    listOf(5, 15, 40, 80).forEach { edad ->
        val etapa = when (edad) {
            in 0..12 -> "infancia"
            in 13..17 -> "adolescencia"
            in 18..64 -> "adultez"
            else -> "vejez"
        }
        show("edad $edad", etapa)
    }

    section("En un for, `in` significa otra cosa")

    // Ojo al matiz: en `for (x in coleccion)` el `in` es parte de la sintaxis del
    // bucle, no el operador de pertenencia. No llama a contains().
    bullet("`for (x in lista)` recorre; `x in lista` pregunta. Mismo símbolo, distinto papel.")
}

/**
 * El mapa completo: qué operadores se pueden redefinir.
 */
fun demoOverloadableOperators() {
    section("Sobrecargables (capítulo 21)")

    bullet("a + b        → a.plus(b)")
    bullet("a - b        → a.minus(b)")
    bullet("a * b        → a.times(b)")
    bullet("a / b        → a.div(b)")
    bullet("a % b        → a.rem(b)")
    bullet("a..b         → a.rangeTo(b)")
    bullet("a..<b        → a.rangeUntil(b)")
    bullet("+a / -a / !a → unaryPlus() / unaryMinus() / not()")
    bullet("a++ / a--    → a.inc() / a.dec()")
    bullet("a += b       → a.plusAssign(b)   (y minusAssign, timesAssign...)")
    bullet("a[i]         → a.get(i)")
    bullet("a[i] = v     → a.set(i, v)")
    bullet("a(x)         → a.invoke(x)")
    bullet("a in b       → b.contains(a)")
    bullet("a == b       → a.equals(b)")
    bullet("a < b        → a.compareTo(b) < 0")
    bullet("for (x in a) → a.iterator()")
    bullet("val (x, y)=a → a.component1(), a.component2()")

    section("NO sobrecargables")

    bullet("&&  ||  !     cortocircuitan; se romperían si fueran funciones normales")
    bullet("?:  ?.  !!    forman parte de la gramática de nulos")
    bullet("===  !==      identidad de objetos: nunca debe poder mentirse")
    bullet("=             la asignación no es una expresión en Kotlin")

    section("La regla de oro de la sobrecarga")

    bullet("Sobrecarga sólo si el significado es OBVIO: Vector + Vector, Dinero + Dinero.")
    bullet("Si hay que explicar qué hace tu `+`, pon una función con nombre.")
}

// -- Tipos auxiliares usados en las demos ---------------------------------------------

private class Ciudad(val nombre: String)
private class Direccion(val calle: String, val ciudad: Ciudad?)
private class Usuario(val nombre: String, val direccion: Direccion?)

/** Guarda con Elvis + return: si no hay texto, salimos ya. */
private fun longitudDe(texto: String?): Int {
    val seguro = texto ?: return -1
    return seguro.length
}

/** Guarda con Elvis + throw. */
private fun exigeNombre(nombre: String?): String {
    val valido = nombre ?: throw IllegalArgumentException("el nombre es obligatorio")
    return valido.uppercase()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita un `?` de la cadena `direccion?.ciudad?.nombre` y lee el error.
//  2. Cambia `?: return -1` por `?: -1` en longitudDe y razona por qué también funciona.
//  3. Sustituye `in 18..64` por `edad >= 18 && edad <= 64` y compara la legibilidad.
//  4. Intenta escribir `operator fun elvis(...)`: comprobarás que no existe tal cosa.
