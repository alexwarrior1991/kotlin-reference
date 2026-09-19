package com.alejandro.c21operatoroverloading

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.math.sqrt

// =====================================================================================
//  21.1 · Operadores aritméticos: el caso Vector2D
//
//  QUÉ ES
//    Definir `plus`, `minus`, `times`... en tus propias clases para poder usar `+`,
//    `-` y `*` con ellas. El capítulo 3 explicó que cada operador es una función con
//    nombre convenido; aquí se escribe esa función.
//
//  POR QUÉ IMPORTA
//    Para tipos matemáticos (vectores, matrices, dinero, duraciones) la notación con
//    operadores se lee como la fórmula original. `a + b * 2` frente a
//    `a.plus(b.times(2))` no es una cuestión de gusto: es la diferencia entre revisar
//    la fórmula de un vistazo o no.
//
//  ERRORES COMUNES
//    · Sobrecargar operadores en clases donde el significado no es obvio.
//    · Olvidar `operator` (sin esa palabra, el `+` no funciona).
//    · Definir `plusAssign` en una clase inmutable (o los dos a la vez sobre un `var`).
// =====================================================================================

/**
 * Los operadores unarios y binarios.
 */
fun demoBasicArithmetic() {
    section("Suma y resta")

    val a = Vector2D(3.0, 4.0)
    val b = Vector2D(1.0, 2.0)

    show("a", a)
    show("b", b)
    show("a + b", a + b)
    show("a - b", a - b)

    bullet("`a + b` se traduce a `a.plus(b)`. Nada más.")

    section("Multiplicación por un escalar")

    show("a * 2.0", a * 2.0)
    show("a / 2.0", a / 2.0)

    // También se puede definir al revés, como extensión sobre Double.
    show("2.0 * a  (extensión sobre Double)", 2.0 * a)

    bullet("`a * 2.0` necesita `Vector2D.times(Double)`.")
    bullet("`2.0 * a` necesita `Double.times(Vector2D)`, que va como extensión.")

    section("Producto escalar entre vectores")

    show("a * b  (producto escalar)", a * b)
    bullet("El mismo símbolo con distinto tipo de parámetro: son sobrecargas normales.")

    section("Operadores unarios")

    show("-a", -a)
    show("+a", +a)

    section("Operaciones que NO son operadores")

    // No todo merece un símbolo. La magnitud tiene nombre y se queda con él.
    show("a.magnitud()", "%.4f".format(a.magnitud()))
    show("a.normalizado()", a.normalizado())
    show("a.angulo()", "%.4f rad".format(a.angulo()))

    bullet("¿Existe una notación matemática estándar? → operador.")
    bullet("¿Hay que explicar qué hace? → función con nombre.")
}

/**
 * Asignación compuesta: `plus` frente a `plusAssign`.
 */
fun demoCompoundAssignment() {
    section("Con una clase INMUTABLE: define `plus`")

    // `v += otro` se traduce a `v = v.plus(otro)`. Hace falta que `v` sea `var`.
    var inmutable = Vector2D(1.0, 1.0)
    val original = inmutable

    inmutable += Vector2D(2.0, 3.0)

    show("tras v += (2,3)", inmutable)
    show("el objeto original no cambió", original)
    show("¿es el mismo objeto?", inmutable === original)

    bullet("Cada `+=` crea un objeto nuevo. Es lo correcto para un tipo de valor.")

    section("Con una clase MUTABLE: define `plusAssign`")

    // `plusAssign` modifica el objeto en el sitio y devuelve Unit.
    val mutable = AcumuladorMutable()
    val mismaReferencia = mutable

    mutable += 10
    mutable += 5

    show("tras dos +=", mutable)
    show("la otra referencia ve el cambio", mismaReferencia)
    show("¿es el mismo objeto?", mutable === mismaReferencia)

    bullet("Aquí `+=` NO reasigna: llama a `plusAssign`, que muta.")
    bullet("Por eso funciona sobre un `val`.")

    section("El error: definir los dos")

    bullet("Si una clase define `plus` Y `plusAssign`, y la variable es `var`,")
    bullet("el compilador no sabe cuál usar y da 'Assignment operators ambiguity'.")
    bullet("Regla: inmutable → sólo `plus`. Mutable → sólo `plusAssign`.")

    section("El caso de las colecciones")

    // Es exactamente lo que pasa con List y MutableList (capítulo 3.2).
    val listaMutable = mutableListOf(1, 2)
    listaMutable += 3                      // plusAssign: muta
    show("MutableList += 3", listaMutable)

    var listaLectura = listOf(1, 2)
    listaLectura += 3                      // plus + reasignación: crea una nueva
    show("List += 3", listaLectura)
}

/**
 * Incremento y decremento.
 */
fun demoIncrement() {
    section("inc() y dec()")

    var contador = Contador(5)
    show("inicial", contador)

    contador++
    show("tras contador++", contador)

    contador--
    contador--
    show("tras dos contador--", contador)

    section("Deben devolver un objeto NUEVO")

    bullet("`inc()` debe devolver un valor del mismo tipo, no modificar `this`.")
    bullet("El compilador se encarga de reasignar la variable.")
    bullet("Por eso `++` sólo funciona sobre `var`, igual que con los Int.")

    section("Prefijo y sufijo funcionan igual que siempre")

    var c = Contador(0)
    val conSufijo = c++
    show("val x = c++  → x", conSufijo)
    show("             → c", c)

    var d = Contador(0)
    val conPrefijo = ++d
    show("val y = ++d  → y", conPrefijo)
    show("             → d", d)
}

// -- Los tipos que usan las demos ------------------------------------------------------------

/**
 * Un vector de dos dimensiones. Tipo de valor inmutable: cada operación devuelve
 * un objeto nuevo.
 */
private data class Vector2D(val x: Double, val y: Double) {

    // a + b
    operator fun plus(otro: Vector2D): Vector2D = Vector2D(x + otro.x, y + otro.y)

    // a - b
    operator fun minus(otro: Vector2D): Vector2D = Vector2D(x - otro.x, y - otro.y)

    // a * escalar
    operator fun times(escalar: Double): Vector2D = Vector2D(x * escalar, y * escalar)

    // a * b (producto escalar): misma función, distinto parámetro
    operator fun times(otro: Vector2D): Double = x * otro.x + y * otro.y

    // a / escalar
    operator fun div(escalar: Double): Vector2D = Vector2D(x / escalar, y / escalar)

    // -a
    operator fun unaryMinus(): Vector2D = Vector2D(-x, -y)

    // +a
    operator fun unaryPlus(): Vector2D = this

    // Estas NO son operadores: no hay notación estándar para ellas.
    fun magnitud(): Double = sqrt(x * x + y * y)
    fun normalizado(): Vector2D = this / magnitud()
    fun angulo(): Double = Math.atan2(y, x)

    override fun toString(): String = "(%.2f, %.2f)".format(x, y)
}

/** `escalar * vector`: como el receptor es Double, va como extensión. */
private operator fun Double.times(vector: Vector2D): Vector2D = vector * this

/** Clase MUTABLE: define `plusAssign`, que muta y devuelve Unit. */
private class AcumuladorMutable {
    private var total: Int = 0

    operator fun plusAssign(cantidad: Int) {
        total += cantidad
    }

    operator fun minusAssign(cantidad: Int) {
        total -= cantidad
    }

    override fun toString(): String = "Acumulador($total)"
}

/** `inc` y `dec` devuelven un objeto nuevo; el compilador reasigna. */
private data class Contador(val valor: Int) {
    operator fun inc(): Contador = Contador(valor + 1)
    operator fun dec(): Contador = Contador(valor - 1)
    override fun toString(): String = "Contador($valor)"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita la palabra `operator` de `plus` y lee el error al usar `+`.
//  2. Añade `plus` a AcumuladorMutable, hazlo `var` y provoca la ambigüedad.
//  3. Escribe `Vector3D` con las mismas operaciones y un producto vectorial.
//  4. Haz que `inc()` modifique `this` en vez de devolver uno nuevo: verás que
//     no se puede, porque `valor` es un `val`.
