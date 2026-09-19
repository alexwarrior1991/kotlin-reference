package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.math.roundToInt

// =====================================================================================
//  Ejercicio 3 · Conversor de temperaturas                                 🟢 fácil
//
//  Repasa: enum con propiedades, when exhaustivo, require, sobrecarga de operadores,
//  Comparable, funciones de extensión, value class.
//  Capítulos: 10 (enum), 21 (operadores), 25 (Comparable), 09 (value class).
// =====================================================================================

/** Ejercicio 3: conversor de temperaturas. */
fun ejercicio03Temperaturas() {
    enunciado(
        "Construye un conversor entre Celsius, Fahrenheit y Kelvin.",
        "",
        "1. Un `enum class Escala` con el símbolo de cada una (°C, °F, K).",
        "2. `convertir(valor, desde, hasta)` que funcione entre las tres, incluido",
        "   convertir a la misma escala (debe devolver el mismo número).",
        "3. Rechazar temperaturas por debajo del cero absoluto: -273,15 °C,",
        "   -459,67 °F, 0 K. Eso SÍ es un error de programación: lanza.",
        "4. Un tipo `Temperatura` que guarde valor y escala, se pueda comparar",
        "   con otra de escala distinta y se imprima bonito.",
        "5. Extensiones cómodas: `25.celsius`, `77.0.fahrenheit`.",
    )

    pistas(
        "El truco para no escribir nueve conversiones: pasa SIEMPRE por una escala",
        "   intermedia. De X a Celsius, y de Celsius a Y. Tres más tres, no nueve.",
        "Guarda el cero absoluto de cada escala como propiedad del enum: así la",
        "   validación es una sola línea para las tres.",
        "Para comparar temperaturas de escalas distintas, implementa `Comparable`",
        "   y compara siempre en Kelvin (capítulo 25).",
        "`operator fun plus` y `operator fun minus` hacen que `a + b` funcione",
        "   (capítulo 21); decide en qué escala sale el resultado.",
        "Una propiedad de extensión `val Int.celsius: Temperatura` es la forma",
        "   idiomática de escribir `25.celsius` (capítulo 17).",
    )

    solucionEnMarcha()

    section("Las conversiones de siempre")

    listOf(0.0, 25.0, 37.0, 100.0).forEach { grados ->
        val f = convertir(grados, Escala.CELSIUS, Escala.FAHRENHEIT)
        val k = convertir(grados, Escala.CELSIUS, Escala.KELVIN)
        show("$grados °C", "${redondear(f)} °F · ${redondear(k)} K")
    }

    section("Y al revés")

    show("98.6 °F → °C", redondear(convertir(98.6, Escala.FAHRENHEIT, Escala.CELSIUS)))
    show("300 K → °C", redondear(convertir(300.0, Escala.KELVIN, Escala.CELSIUS)))
    show("0 K → °F", redondear(convertir(0.0, Escala.KELVIN, Escala.FAHRENHEIT)))
    show("20 °C → °C (misma escala)", convertir(20.0, Escala.CELSIUS, Escala.CELSIUS))

    section("Por debajo del cero absoluto: eso no existe")

    listOf(
        Triple(-300.0, Escala.CELSIUS, "°C"),
        Triple(-500.0, Escala.FAHRENHEIT, "°F"),
        Triple(-1.0, Escala.KELVIN, "K"),
    ).forEach { (valor, escala, simbolo) ->
        val fallo = runCatching { convertir(valor, escala, Escala.KELVIN) }
        show("convertir($valor $simbolo)", "✗ ${fallo.exceptionOrNull()?.message}")
    }

    section("El tipo Temperatura")

    val enCasa = Temperatura(21.0, Escala.CELSIUS)
    val fiebre = 38.5.celsius
    val ambiente = 77.fahrenheit

    show("enCasa", enCasa)
    show("fiebre", fiebre)
    show("ambiente (77 °F)", ambiente)
    show("ambiente en Celsius", ambiente.en(Escala.CELSIUS))

    section("Comparar entre escalas distintas")

    show("21 °C vs 77 °F", if (enCasa > ambiente) "más calor en casa" else "más calor fuera")
    show("¿son iguales 0 °C y 32 °F?", Temperatura(0.0, Escala.CELSIUS) mismoCalorQue 32.fahrenheit)
    show("ordenadas", listOf(fiebre, enCasa, ambiente, 0.celsius).sorted())

    section("Aritmética")

    show("21 °C + 5 grados", enCasa + 5.0)
    show("21 °C - 25 grados", enCasa - 25.0)
    show("diferencia 38,5 °C y 21 °C", "${redondear(fiebre.diferenciaCon(enCasa))} grados")

    explicacion(
        "La idea que ahorra la mitad del código: pasar por una escala PIVOTE.",
        "Con tres escalas, convertir de todas a todas son nueve casos; con pivote",
        "son tres (a Kelvin) más tres (desde Kelvin). Con cinco escalas serían",
        "veinticinco casos frente a diez. La diferencia crece deprisa.",
        "",
        "Se ha elegido Kelvin como pivote porque es donde vive el cero absoluto: la",
        "validación se convierte en «tras pasar a Kelvin, ¿es negativo?».",
        "",
        "Aquí sí se LANZA, al contrario que en los ejercicios 1 y 2. El motivo: una",
        "temperatura bajo el cero absoluto no es un dato malo del usuario que haya",
        "que explicar, es un error de programación. Ésa es la línea de la tabla del",
        "capítulo 19: dato esperado → tipo de resultado; bug → excepción.",
        "",
        "`Temperatura` implementa `Comparable` comparando en Kelvin, así que `sorted()`,",
        "`maxOrNull()` y `in` funcionan entre escalas distintas sin escribir nada más.",
        "Eso es lo que da implementar una interfaz de la biblioteca estándar en vez de",
        "inventarse un `esMayorQue`.",
    )

    varianteDificil(
        "1. Haz `Temperatura` un `@JvmInline value class` sobre un Double en Kelvin:",
        "   ocupa lo mismo que un Double y sigue siendo un tipo distinto (capítulo 09).",
        "2. Añade Rankine y Réaumur sin tocar `convertir`: sólo el enum.",
        "3. Implementa `rangeTo` para poder escribir `10.celsius..30.celsius` y",
        "   preguntar `if (hoy in comodo)` (capítulo 21).",
        "4. Añade `DiferenciaDeTemperatura` como tipo aparte: restar dos temperaturas",
        "   NO da una temperatura, da una diferencia. Es un error de modelado clásico.",
        "5. Parsea texto: `\"21°C\"`, `\"-4 F\"`, `\"300K\"` → Temperatura?",
    )

    testEn("TemperaturasTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/**
 * Las escalas, con el dato que hace falta para validar: su cero absoluto.
 *
 * Meter esa constante en el enum evita un `when` de validación repetido por ahí suelto.
 */
enum class Escala(val simbolo: String, val ceroAbsoluto: Double) {
    CELSIUS("°C", -273.15),
    FAHRENHEIT("°F", -459.67),
    KELVIN("K", 0.0),
}

/**
 * Convierte entre escalas pasando por Kelvin.
 *
 * @throws IllegalArgumentException si el valor está por debajo del cero absoluto.
 */
fun convertir(valor: Double, desde: Escala, hasta: Escala): Double {
    require(valor >= desde.ceroAbsoluto) {
        "$valor ${desde.simbolo} está por debajo del cero absoluto " +
            "(${desde.ceroAbsoluto} ${desde.simbolo})"
    }
    // Atajo: convertir a la misma escala no debe introducir error de redondeo.
    if (desde == hasta) return valor

    return desdeKelvin(aKelvin(valor, desde), hasta)
}

/** X → Kelvin. Tres casos, no nueve. */
private fun aKelvin(valor: Double, desde: Escala): Double = when (desde) {
    Escala.KELVIN -> valor
    Escala.CELSIUS -> valor + 273.15
    Escala.FAHRENHEIT -> (valor - 32) * 5 / 9 + 273.15
}

/** Kelvin → X. Los otros tres. */
private fun desdeKelvin(kelvin: Double, hasta: Escala): Double = when (hasta) {
    Escala.KELVIN -> kelvin
    Escala.CELSIUS -> kelvin - 273.15
    Escala.FAHRENHEIT -> (kelvin - 273.15) * 9 / 5 + 32
}

/**
 * Una temperatura con su escala.
 *
 * Implementa `Comparable` comparando en Kelvin: así `sorted()`, `maxOrNull()` y los
 * operadores `<`, `>` funcionan entre escalas distintas sin escribir nada más.
 */
data class Temperatura(val valor: Double, val escala: Escala) : Comparable<Temperatura> {

    init {
        require(valor >= escala.ceroAbsoluto) {
            "$valor ${escala.simbolo} está por debajo del cero absoluto"
        }
    }

    /** El mismo calor, expresado en otra escala. */
    fun en(otra: Escala): Temperatura = Temperatura(convertir(valor, escala, otra), otra)

    /** El valor en Kelvin, que es lo único comparable entre escalas. */
    val enKelvin: Double get() = convertir(valor, escala, Escala.KELVIN)

    override fun compareTo(other: Temperatura): Int = enKelvin.compareTo(other.enKelvin)

    /** Sumar o restar GRADOS de la propia escala; el resultado conserva la escala. */
    operator fun plus(grados: Double): Temperatura = Temperatura(valor + grados, escala)

    operator fun minus(grados: Double): Temperatura = Temperatura(valor - grados, escala)

    /** La diferencia entre dos temperaturas, en grados de la escala de ESTA. */
    fun diferenciaCon(otra: Temperatura): Double = valor - otra.en(escala).valor

    override fun toString(): String = "${formatearGrados(valor)} ${escala.simbolo}"
}

/**
 * ¿Es el mismo calor, aunque esté escrito en otra escala?
 *
 * No se usa `==` porque `Temperatura` es una `data class`: su `equals` compara valor
 * Y escala, así que `0 °C != 32 °F`. Los dos comportamientos son útiles, pero hay
 * que poder distinguirlos (capítulo 09).
 */
infix fun Temperatura.mismoCalorQue(otra: Temperatura): Boolean =
    kotlin.math.abs(enKelvin - otra.enKelvin) < 1e-9

/** `25.celsius` se lee mejor que `Temperatura(25.0, Escala.CELSIUS)`. */
val Int.celsius: Temperatura get() = Temperatura(toDouble(), Escala.CELSIUS)
val Int.fahrenheit: Temperatura get() = Temperatura(toDouble(), Escala.FAHRENHEIT)
val Int.kelvin: Temperatura get() = Temperatura(toDouble(), Escala.KELVIN)

val Double.celsius: Temperatura get() = Temperatura(this, Escala.CELSIUS)
val Double.fahrenheit: Temperatura get() = Temperatura(this, Escala.FAHRENHEIT)
val Double.kelvin: Temperatura get() = Temperatura(this, Escala.KELVIN)

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun redondear(valor: Double): Double = (valor * 100).roundToInt() / 100.0

private fun formatearGrados(valor: Double): String {
    val redondeado = redondear(valor)
    return if (redondeado == redondeado.toLong().toDouble()) {
        redondeado.toLong().toString()
    } else {
        redondeado.toString()
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `0.celsius == 32.fahrenheit` y luego `0.celsius mismoCalorQue 32.fahrenheit`.
//     Entender por qué dan cosas distintas es media data class aprendida.
//  2. Añade `operator fun rangeTo` y prueba `if (25.celsius in 18.celsius..24.celsius)`.
//  3. Quita el atajo `if (desde == hasta) return valor` y comprueba el error de
//     redondeo que aparece al convertir de Fahrenheit a Fahrenheit.
