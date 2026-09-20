package com.alejandro.c09dataclasses

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  9.1 · data class: qué genera y qué no
//
//  QUÉ ES
//    Una clase pensada para TRANSPORTAR DATOS. El compilador le genera `equals`,
//    `hashCode`, `toString`, `copy` y las funciones `componentN`.
//
//  POR QUÉ IMPORTA
//    Escribir `equals` y `hashCode` a mano es mecánico y fácil de estropear, y basta
//    con olvidarse de actualizarlos al añadir un campo para que los objetos se
//    "pierdan" dentro de un HashMap. Una `data class` no se puede desincronizar.
//
//  ERRORES COMUNES
//    · Creer que TODAS las propiedades entran en equals: sólo las del constructor
//      primario.
//    · Usar `data class` para algo que tiene identidad propia (una entidad con id).
//    · Meter propiedades mutables y luego usar el objeto como clave de un mapa.
// =====================================================================================

/**
 * Lo que el compilador escribe por ti.
 */
fun demoWhatDataClassGenerates() {
    section("La declaración completa")

    bullet("data class Punto(val x: Int, val y: Int)")
    bullet("...y ya está. Una línea.")

    val a = Punto(1, 2)
    val b = Punto(1, 2)
    val c = Punto(3, 4)

    section("toString() legible")

    show("Punto(1, 2).toString()", a.toString())
    show("una clase normal", PuntoNormal(1, 2).toString().substringBefore('@') + "@...")
    bullet("Sin `data`, toString() devuelve el nombre de la clase y un hash inútil.")

    section("equals() por contenido")

    show("a == b  (mismo contenido)", a == b)
    show("a == c", a == c)
    show("a === b (objetos distintos)", a === b)

    // Con una clase normal, dos objetos con los mismos datos NO son iguales.
    show("PuntoNormal(1,2) == PuntoNormal(1,2)", PuntoNormal(1, 2) == PuntoNormal(1, 2))

    section("hashCode() coherente con equals()")

    show("a.hashCode() == b.hashCode()", a.hashCode() == b.hashCode())
    show("se deduplican en un Set", setOf(a, b, c).size)
    show("funcionan como clave de mapa", mapOf(a to "origen")[b])

    bullet("Esto último es lo importante: buscar por una clave EQUIVALENTE funciona.")
    bullet("Con PuntoNormal, `mapOf(a to ...)[b]` devolvería null.")

    section("componentN() para desestructurar")

    val (x, y) = a
    show("val (x, y) = Punto(1, 2)", "x=$x, y=$y")
    bullet("Se ve a fondo en el capítulo 22.")

    section("copy() para crear variantes")

    show("a.copy(y = 99)", a.copy(y = 99))
    bullet("Se ve a fondo en la demo 9.4.")
}

/**
 * Sólo cuentan las propiedades del constructor primario.
 */
fun demoOnlyPrimaryConstructorCounts() {
    section("El matiz que más sorprende")

    // `etiqueta` está declarada en el CUERPO, no en el constructor primario.
    val uno = Articulo("ABC-1", "Teclado")
    val dos = Articulo("ABC-1", "Teclado")

    uno.etiqueta = "oferta"
    dos.etiqueta = "normal"

    show("uno.etiqueta", uno.etiqueta)
    show("dos.etiqueta", dos.etiqueta)
    show("uno == dos  (¡true!)", uno == dos)
    show("uno.toString()", uno.toString())

    bullet("`etiqueta` NO aparece en equals, hashCode, toString ni copy.")
    bullet("Sólo entran las propiedades declaradas entre los paréntesis.")

    section("Por qué es así")

    bullet("El compilador necesita saber qué define la IDENTIDAD del dato.")
    bullet("El constructor primario es justamente esa declaración.")
    bullet("Lo del cuerpo se considera estado derivado o auxiliar.")

    section("Cómo evitar la sorpresa")

    bullet("Si una propiedad forma parte del dato, ponla en el constructor primario.")
    bullet("Si no forma parte, plantéate si debería estar en la clase siquiera.")
}

/**
 * Requisitos y limitaciones.
 */
fun demoRequirementsAndLimits() {
    section("Requisitos")

    bullet("1. Al menos un parámetro en el constructor primario.")
    bullet("2. Todos esos parámetros deben ser `val` o `var`.")
    bullet("3. No puede ser `abstract`, `open`, `sealed` ni `inner`.")

    section("data class Vacia()  → no compila")

    bullet("'Data class must have at least one primary constructor parameter'")
    bullet("Para un tipo sin datos existe `data object` (ver 9.5) o un `object` normal.")

    section("No se puede heredar DE una data class")

    bullet("Una `data class` es final: no puede ser `open`.")
    bullet("Motivo: `equals` generado compara `this::class`; una subclase rompería")
    bullet("la simetría del contrato (a == b pero b != a).")
    bullet("Sí puede implementar interfaces y heredar de una clase abstracta.")

    show("Coordenada implementa Comparable", listOf(Coordenada(3), Coordenada(1)).sorted())

    section("Arrays dentro de una data class: cuidado")

    // `equals` generado usa el `equals` de cada propiedad. Para un array, eso es
    // identidad, no contenido. Dos data class con arrays "iguales" salen distintas.
    val d1 = ConArray(intArrayOf(1, 2, 3))
    val d2 = ConArray(intArrayOf(1, 2, 3))
    show("ConArray(intArrayOf(1,2,3)) == igual", d1 == d2)
    show("mismo contenido con contentEquals", d1.datos.contentEquals(d2.datos))

    bullet("Si necesitas un array dentro, sobrescribe equals/hashCode a mano.")
    bullet("Mucho mejor: usa `List` en lugar de array, y el problema desaparece.")

    val l1 = ConLista(listOf(1, 2, 3))
    val l2 = ConLista(listOf(1, 2, 3))
    show("ConLista(listOf(1,2,3)) == igual", l1 == l2)
}

/**
 * Sobrescribir lo generado.
 */
fun demoOverridingGenerated() {
    section("Puedes escribir tu propia versión")

    // Si declaras `toString`, `equals` o `hashCode`, el compilador respeta el tuyo.
    val precio = Precio(1999, "EUR")
    show("toString() personalizado", precio.toString())
    show("copy() sigue generándose", precio.copy(centimos = 2999))

    bullet("`componentN` y `copy` NO se pueden sobrescribir a mano.")
    bullet("`equals`, `hashCode` y `toString` sí.")

    section("Un caso real: ocultar datos sensibles")

    val credenciales = Credenciales("ana", "supersecreta")
    show("toString() enmascarado", credenciales.toString())
    bullet("Si no lo sobrescribieras, la contraseña acabaría en los logs.")
    bullet("Es una de las pocas razones de peso para tocar el toString generado.")
}

// -- Las clases que usan las demos ----------------------------------------------------

private data class Punto(val x: Int, val y: Int)

/** La misma clase sin `data`, para comparar. */
private class PuntoNormal(val x: Int, val y: Int)

/** `etiqueta` está en el cuerpo: no entra en equals/hashCode/toString/copy. */
private data class Articulo(val codigo: String, val nombre: String) {
    var etiqueta: String = ""
}

/** Una data class sí puede implementar interfaces. */
private data class Coordenada(val valor: Int) : Comparable<Coordenada> {
    override fun compareTo(other: Coordenada): Int = valor - other.valor
}

/**
 * Con un array dentro, el `equals` GENERADO compara los arrays con `==`, que para
 * arrays es identidad (capítulo 2.17). Por eso dos ConArray con el mismo contenido
 * salen distintos.
 *
 * Lo dejamos sin sobrescribir a propósito, para ver el fallo. IntelliJ y el compilador
 * avisan con "Array property in data class: it is recommended to override
 * equals/hashCode"; ese aviso es exactamente esto.
 */
@Suppress("ArrayInDataClass")
private data class ConArray(val datos: IntArray)

/** Con una lista, todo funciona como esperas. */
private data class ConLista(val datos: List<Int>)

/** toString propio; copy y componentN siguen generándose. */
private data class Precio(val centimos: Int, val moneda: String) {
    override fun toString(): String = "%.2f %s".format(centimos / 100.0, moneda)
}

/** Enmascarar datos sensibles en toString: el caso de uso más legítimo. */
private data class Credenciales(val usuario: String, val contrasena: String) {
    override fun toString(): String = "Credenciales(usuario=$usuario, contrasena=***)"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Mueve `etiqueta` al constructor primario de Articulo y vuelve a ejecutar 9.2.
//  2. Intenta escribir `data class Vacia()` y lee el error.
//  3. Marca `Punto` como `open` y comprueba que no está permitido.
//  4. Quita el toString de Credenciales y mira lo que se imprimiría en un log.
