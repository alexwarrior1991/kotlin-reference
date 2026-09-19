package com.alejandro.c21operatoroverloading

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  21.2 · Comparación, rangos y pertenencia: el caso Dinero
//
//  QUÉ ES
//    `compareTo` habilita `<`, `>`, `<=` y `>=`. `rangeTo` habilita `..`. `contains`
//    habilita `in`. Juntos convierten un tipo propio en algo que se usa como un número.
//
//  POR QUÉ IMPORTA
//    Un tipo `Dinero` que se puede comparar, ordenar y meter en rangos evita el
//    clásico "un Int de céntimos por aquí y un Double de euros por allá". El tipo
//    hace imposible sumar euros con dólares por error.
//
//  ERRORES COMUNES
//    · Implementar `compareTo` sin que sea coherente con `equals`.
//    · Devolver `a - b` en `compareTo` y desbordar con valores grandes.
//    · Definir `contains` en algo que no es una colección ni un rango.
// =====================================================================================

/**
 * `compareTo` y los cuatro operadores que habilita.
 */
fun demoComparison() {
    section("Un solo método, cuatro operadores")

    val barato = Dinero(1_050, Moneda.EUR)     // 10,50 €
    val caro = Dinero(9_999, Moneda.EUR)       // 99,99 €

    show("barato", barato)
    show("caro", caro)
    show("barato < caro", barato < caro)
    show("barato > caro", barato > caro)
    show("barato <= barato", barato <= Dinero(1_050, Moneda.EUR))
    show("caro >= barato", caro >= barato)

    bullet("`a < b` se traduce a `a.compareTo(b) < 0`. Uno implementa, cuatro salen gratis.")

    section("Y también ordenar")

    val importes = listOf(
        Dinero(500, Moneda.EUR),
        Dinero(12_000, Moneda.EUR),
        Dinero(75, Moneda.EUR),
    )
    show("sin ordenar", importes)
    show("sorted()", importes.sorted())
    show("max()", importes.max())
    show("min()", importes.min())

    bullet("Implementar `Comparable` habilita `sorted`, `max`, `min`, `coerceIn`...")

    section("El error clásico: restar en compareTo")

    bullet("`override fun compareTo(o: X) = this.valor - o.valor`")
    bullet("Con Int.MAX_VALUE y valores negativos, esa resta DESBORDA y el signo")
    bullet("sale invertido. El orden queda mal y es dificilísimo de detectar.")
    bullet("Usa `this.valor.compareTo(o.valor)` o `compareValuesBy`.")

    show("con resta y valores extremos", compararMal(Int.MAX_VALUE, -1))
    show("con compareTo (correcto)", compararBien(Int.MAX_VALUE, -1))

    section("Coherencia con equals")

    bullet("Si `a.compareTo(b) == 0`, entonces `a == b` debería ser true.")
    bullet("Romperlo hace que un TreeSet y un HashSet discrepen sobre los duplicados.")

    val uno = Dinero(100, Moneda.EUR)
    val otro = Dinero(100, Moneda.EUR)
    show("compareTo == 0", uno.compareTo(otro) == 0)
    show("equals", uno == otro)
}

/**
 * `rangeTo` y `contains`.
 */
fun demoRangesAndContains() {
    section("rangeTo habilita `..`")

    val minimo = Dinero(1_000, Moneda.EUR)
    val maximo = Dinero(5_000, Moneda.EUR)
    val rango = minimo..maximo

    show("minimo..maximo", rango)

    section("contains habilita `in`")

    show("30,00 € in rango", Dinero(3_000, Moneda.EUR) in rango)
    show("80,00 € in rango", Dinero(8_000, Moneda.EUR) in rango)
    show("80,00 € !in rango", Dinero(8_000, Moneda.EUR) !in rango)

    bullet("`x in rango` se traduce a `rango.contains(x)`.")
    bullet("`ClosedRange<T>` ya trae `contains` si el tipo es Comparable: con")
    bullet("implementar `rangeTo` devolviendo un ClosedRange, `in` sale gratis.")

    section("Y en un when")

    listOf(500, 3_000, 8_000).forEach { centimos ->
        val importe = Dinero(centimos, Moneda.EUR)
        val categoria = when (importe) {
            in Dinero(0, Moneda.EUR)..Dinero(999, Moneda.EUR) -> "pequeño"
            in Dinero(1_000, Moneda.EUR)..Dinero(4_999, Moneda.EUR) -> "mediano"
            else -> "grande"
        }
        show("$importe", categoria)
    }

    section("contains sobre una colección propia")

    val cesta = Cesta(listOf("pan", "leche", "huevos"))
    show("\"pan\" in cesta", "pan" in cesta)
    show("\"caviar\" in cesta", "caviar" in cesta)
    show("\"caviar\" !in cesta", "caviar" !in cesta)

    bullet("Aquí `contains` recibe un String y comprueba pertenencia: es lo natural.")
    bullet("Definir `contains` en algo que no representa un conjunto confunde.")
}

/**
 * Igualdad: `equals` y por qué no lleva `operator`.
 */
fun demoEquality() {
    section("`==` llama a `equals`, pero no se declara con `operator`")

    bullet("`equals` ya está declarada como `operator` en `Any`.")
    bullet("Por eso al sobrescribirla basta con `override fun equals(other: Any?)`.")

    val a = Dinero(100, Moneda.EUR)
    val b = Dinero(100, Moneda.EUR)
    val c = Dinero(100, Moneda.USD)

    show("mismo importe y moneda", a == b)
    show("mismo importe, otra moneda", a == c)
    show("identidad", a === b)

    section("El tipo impide mezclar monedas")

    val sumaValida = a + b
    show("EUR + EUR", sumaValida)

    val sumaInvalida = try {
        (a + c).toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("EUR + USD", sumaInvalida)

    bullet("Con Int de céntimos, sumar euros y dólares compilaría sin rechistar.")
    bullet("Un tipo propio convierte ese bug en un error en ejecución... o, mejor,")
    bullet("en un error de compilación si haces la moneda parte del tipo genérico.")

    section("La comparación entre monedas distintas")

    val comparacionInvalida = try {
        (a < c).toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException"
    }
    show("EUR < USD", comparacionInvalida)
    bullet("Comparar peras con manzanas no tiene respuesta correcta: mejor lanzar.")
}

// -- Los tipos que usan las demos ------------------------------------------------------------

private enum class Moneda(val simbolo: String) { EUR("€"), USD("$") }

/**
 * Importe monetario en céntimos. Nunca en Double (capítulo 2.10).
 */
private data class Dinero(val centimos: Int, val moneda: Moneda) : Comparable<Dinero> {

    operator fun plus(otro: Dinero): Dinero {
        require(moneda == otro.moneda) { "no se pueden sumar $moneda y ${otro.moneda}" }
        return Dinero(centimos + otro.centimos, moneda)
    }

    operator fun minus(otro: Dinero): Dinero {
        require(moneda == otro.moneda) { "no se pueden restar $moneda y ${otro.moneda}" }
        return Dinero(centimos - otro.centimos, moneda)
    }

    operator fun times(veces: Int): Dinero = Dinero(centimos * veces, moneda)

    /**
     * Un solo método habilita <, >, <= y >=.
     *
     * Fíjate en que usa `compareTo` y no una resta: con valores extremos, la resta
     * desborda y devuelve el signo equivocado.
     */
    override fun compareTo(other: Dinero): Int {
        require(moneda == other.moneda) { "no se pueden comparar $moneda y ${other.moneda}" }
        return centimos.compareTo(other.centimos)
    }

    /** Habilita `a..b`. Como Dinero es Comparable, el `in` sale gratis. */
    operator fun rangeTo(fin: Dinero): ClosedRange<Dinero> = RangoDeDinero(this, fin)

    override fun toString(): String = "%.2f %s".format(centimos / 100.0, moneda.simbolo)
}

/** Implementación mínima de ClosedRange: `contains` viene de la interfaz. */
private class RangoDeDinero(
    override val start: Dinero,
    override val endInclusive: Dinero,
) : ClosedRange<Dinero> {
    override fun toString(): String = "$start..$endInclusive"
}

/** Una "colección" propia con `contains`. */
private class Cesta(private val productos: List<String>) {
    operator fun contains(producto: String): Boolean = producto in productos
}

// Demostración del desbordamiento en compareTo.
private fun compararMal(a: Int, b: Int): String {
    val resultado = a - b     // desborda
    return "a - b = $resultado → dice que ${if (resultado > 0) "a > b" else "a < b"}"
}

private fun compararBien(a: Int, b: Int): String {
    val resultado = a.compareTo(b)
    return "a.compareTo(b) = $resultado → dice que ${if (resultado > 0) "a > b" else "a < b"}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `centimos.compareTo(other.centimos)` por la resta y comprueba el fallo
//     con Int.MAX_VALUE.
//  2. Añade `operator fun div(partes: Int): List<Dinero>` que reparta sin perder céntimos.
//  3. Haz que la moneda sea un parámetro de tipo: `Dinero<EUR>` y `Dinero<USD>`.
//     Entonces el error de sumar monedas distintas sería de COMPILACIÓN.
//  4. Implementa `Cesta.contains` para que acepte también un producto parcial.
