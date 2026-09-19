package com.alejandro.c03operators

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  3.2 · Igualdad, comparación y operadores lógicos
//
//  QUÉ ES
//    La diferencia entre igualdad estructural (`==`, el contenido) y referencial
//    (`===`, el mismo objeto), cómo funcionan `<` y `>` por debajo, y los operadores
//    lógicos con y sin cortocircuito.
//
//  POR QUÉ IMPORTA
//    `==` en Kotlin significa lo CONTRARIO que en Java. En Java `==` entre objetos
//    compara referencias y hay que usar `equals`; en Kotlin `==` YA es `equals` (con
//    los nulos resueltos), y para referencias existe `===`. Si vienes de Java, éste es
//    el cambio mental que más errores evita.
//
//  ERRORES COMUNES
//    · Escribir `a.equals(b)` por costumbre en lugar de `a == b`.
//    · Usar `===` sobre números y sorprenderse con la caché de enteros de la JVM.
//    · Sobrescribir `equals` y olvidarse de `hashCode`.
// =====================================================================================

/** Devuelve el mismo número, pero "encajado" en un objeto (boxing). */
private fun enCaja(n: Int): Any = n

/**
 * `==` frente a `===`.
 */
fun demoStructuralVsReferential() {
    section("Con cadenas")

    val a = "hola"
    val b = "hola"
    val c = StringBuilder("ho").append("la").toString()

    show("a == b   (contenido)", a == b)
    show("a == c   (contenido)", a == c)
    show("a === c  (¿el mismo objeto?)", a === c)

    section("Qué hace el compilador con ==")

    // `a == b` no es exactamente `a.equals(b)`: además maneja el nulo por ti.
    // El compilador genera algo equivalente a:
    //     a?.equals(b) ?: (b === null)
    // Por eso `null == null` es true y nunca hay NullPointerException al comparar.
    val nulo: String? = null
    val otroNulo: String? = null
    show("nulo == otroNulo", nulo == otroNulo)
    show("nulo == \"hola\"", nulo == "hola")
    bullet("`==` es seguro con nulos: nunca lanza NPE, aunque el receptor sea null.")

    section("Negaciones")

    show("a != c", a != c)
    show("a !== c", a !== c)

    section("La caché de enteros de la JVM")

    // Al meter un Int en un objeto, la JVM reutiliza instancias para los valores
    // pequeños (-128..127). Con valores grandes crea objetos nuevos.
    val pequenoUno = enCaja(127)
    val pequenoDos = enCaja(127)
    val grandeUno = enCaja(128)
    val grandeDos = enCaja(128)

    show("enCaja(127) == enCaja(127)", pequenoUno == pequenoDos)
    show("enCaja(127) === enCaja(127)", pequenoUno === pequenoDos)
    show("enCaja(128) == enCaja(128)", grandeUno == grandeDos)
    show("enCaja(128) === enCaja(128)  (¡false!)", grandeUno === grandeDos)

    bullet("Es un detalle de la JVM, no de Kotlin: la caché cubre -128..127.")
    bullet("Moraleja: `===` sobre números NUNCA es lo que quieres. Usa `==`.")

    section("Otra rareza de los decimales")

    // Como primitivos, 0.0 y -0.0 son iguales. Como objetos, no.
    val cero = 0.0
    val ceroNegativo = -0.0
    show("0.0 == -0.0   (como Double)", cero == ceroNegativo)
    show("(0.0).equals(-0.0)  (como objetos)", cero.equals(ceroNegativo))
    bullet("Sólo importa si metes decimales en un Set o como claves de un Map.")
}

/**
 * El contrato de equals y hashCode.
 */
fun demoEqualsContract() {
    section("Sin equals: dos objetos distintos nunca son iguales")

    val p1 = PuntoSinEquals(1, 2)
    val p2 = PuntoSinEquals(1, 2)
    show("PuntoSinEquals(1,2) == PuntoSinEquals(1,2)", p1 == p2)
    bullet("Sin equals se hereda el de Any, que es identidad: mismo objeto o nada.")

    section("Con equals y hashCode")

    val q1 = PuntoConEquals(1, 2)
    val q2 = PuntoConEquals(1, 2)
    show("PuntoConEquals(1,2) == PuntoConEquals(1,2)", q1 == q2)
    show("mismo hashCode", q1.hashCode() == q2.hashCode())
    show("en un Set se deduplican", setOf(q1, q2).size)
    show("los que no tienen equals, no", setOf(p1, p2).size)

    section("La regla que no se puede romper")

    bullet("Si a == b, entonces a.hashCode() == b.hashCode(). SIEMPRE.")
    bullet("Al revés no: dos objetos distintos pueden compartir hashCode (colisión).")
    bullet("Romperla hace que tus objetos se 'pierdan' dentro de un HashMap o un HashSet.")

    section("La forma corta")

    // Escribir equals/hashCode a mano es mecánico y fácil de estropear. Para clases
    // que sólo transportan datos, `data class` los genera por ti. Capítulo 9.
    val d1 = PuntoData(1, 2)
    val d2 = PuntoData(1, 2)
    show("data class PuntoData(1,2) == PuntoData(1,2)", d1 == d2)
    show("su toString() también viene hecho", d1)
    bullet("Regla práctica: si la clase representa datos, hazla `data class`.")
}

/**
 * Los operadores de comparación llaman a compareTo.
 */
fun demoComparison() {
    section("< > <= >= son compareTo por debajo")

    val a = 10
    val b = 20

    show("a < b    →  a.compareTo(b) < 0", "${a < b}  ==  ${a.compareTo(b) < 0}")
    show("a.compareTo(b)  (negativo: a va antes)", a.compareTo(b))
    show("b.compareTo(a)  (positivo: b va después)", b.compareTo(a))
    show("a.compareTo(a)  (cero: equivalentes)", a.compareTo(a))

    section("Funciona con cualquier Comparable")

    show("\"ana\" < \"luis\"   (orden alfabético)", "ana" < "luis")
    show("'a' < 'z'", 'a' < 'z')

    // Una clase propia que implementa Comparable también puede usar < y >.
    val corta = Palabra("sol")
    val larga = Palabra("bicicleta")
    show("Palabra(\"sol\") < Palabra(\"bicicleta\")", corta < larga)
    show("maxOf(corta, larga)", maxOf(corta, larga))
    show("listOf(larga, corta).sorted()", listOf(larga, corta).sorted())

    section("Comparar tipos numéricos distintos")

    // `1 == 1L` NO compila: son tipos distintos y Kotlin no los mezcla.
    // En cambio `<` sí funciona entre numéricos, porque hay sobrecargas para ello.
    // Los paréntesis alrededor del 1 no son decorativos: sin ellos, `1.equals` se
    // podría leer como el literal decimal `1.` seguido de un nombre.
    show("1 < 2L         (sí compila)", 1 < 2L)
    show("(1).equals(1L) (false: tipos distintos)", (1).equals(1L))
    show("1.toLong() == 1L", 1.toLong() == 1L)
    bullet("Para igualdad entre Int y Long, convierte explícitamente uno de los dos.")
}

/**
 * Operadores lógicos, con y sin cortocircuito.
 */
fun demoLogicalOperators() {
    section("&& y || cortocircuitan")

    // Si el primer operando ya decide el resultado, el segundo NI SE EVALÚA.
    // Eso permite escribir guardas como `lista != null && lista.isNotEmpty()`.
    var evaluado = false
    fun caro(): Boolean {
        evaluado = true
        return true
    }

    evaluado = false
    val resultadoAnd = false && caro()
    show("false && caro()  → resultado", resultadoAnd)
    show("                 → ¿se evaluó caro()?", evaluado)

    evaluado = false
    val resultadoOr = true || caro()
    show("true || caro()   → resultado", resultadoOr)
    show("                 → ¿se evaluó caro()?", evaluado)

    section("and / or NO cortocircuitan")

    evaluado = false
    val conAnd = false and caro()
    show("false and caro() → resultado", conAnd)
    show("                 → ¿se evaluó caro()?", evaluado)

    bullet("`and`/`or` sobre Boolean evalúan SIEMPRE los dos lados.")
    bullet("Úsalos sólo si necesitas los efectos de ambos; normalmente quieres && y ||.")

    section("Negación y xor")

    show("!true", !true)
    show("true xor true", true xor true)
    show("true xor false", true xor false)

    section("Por qué el cortocircuito importa")

    val lista: List<Int>? = null
    // Sin cortocircuito, la segunda condición lanzaría NPE.
    show("lista != null && lista.isNotEmpty()", lista != null && lista.isNotEmpty())
    bullet("En Kotlin, además, tras `lista != null` el compilador hace smart cast (cap. 6).")
}

/** Clase sin equals: se compara por identidad. */
private class PuntoSinEquals(val x: Int, val y: Int)

/** La misma clase, con equals y hashCode escritos a mano. */
private class PuntoConEquals(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PuntoConEquals) return false
        return x == other.x && y == other.y
    }

    // Debe usar exactamente los mismos campos que equals.
    override fun hashCode(): Int = 31 * x + y

    override fun toString(): String = "PuntoConEquals($x, $y)"
}

/** Lo mismo, pero dejando que el compilador lo escriba. Capítulo 9. */
private data class PuntoData(val x: Int, val y: Int)

/** Implementar Comparable habilita <, >, <=, >=, sorted(), maxOf()... */
private class Palabra(val texto: String) : Comparable<Palabra> {
    override fun compareTo(other: Palabra): Int = texto.length - other.texto.length
    override fun toString(): String = "Palabra($texto)"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia 127 por 128 en la demo de la caché y observa cómo cambia `===`.
//  2. Borra el `hashCode()` de PuntoConEquals y mira qué pasa con `setOf(q1, q2).size`.
//  3. Sustituye `&&` por `and` en la última demo y comprueba que ahora sí lanza NPE.
//  4. Cambia el compareTo de Palabra para ordenar alfabéticamente en lugar de por longitud.
