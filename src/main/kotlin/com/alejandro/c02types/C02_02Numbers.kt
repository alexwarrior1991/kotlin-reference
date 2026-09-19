package com.alejandro.c02types

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.math.roundToInt

// =====================================================================================
//  2.2 · Tipos numéricos
//
//  QUÉ ES
//    Los seis tipos numéricos con signo (Byte, Short, Int, Long, Float, Double), sus
//    literales, y las reglas de conversión entre ellos.
//
//  POR QUÉ IMPORTA
//    Kotlin NO hace conversiones numéricas implícitas. Es la diferencia práctica más
//    visible con Java y la causa del 90% de los "Type mismatch" del primer día.
//    Y el desbordamiento es silencioso: si no lo sabes, te muerde en producción.
//
//  ERRORES COMUNES
//    · `val x: Long = unInt` — no compila; hace falta `unInt.toLong()`.
//    · Esperar 0.5 de `1 / 2`, que es división ENTERA y da 0.
//    · Usar Double para dinero. Usa céntimos en Long, o BigDecimal.
//    · Creer que `1.0` es Float. Es Double.
// =====================================================================================

/**
 * Los tipos enteros y sus rangos.
 */
fun demoIntegerTypes() {
    section("Los cuatro tipos enteros con signo")

    show("Byte   (8 bits)", "${Byte.MIN_VALUE} .. ${Byte.MAX_VALUE}")
    show("Short  (16 bits)", "${Short.MIN_VALUE} .. ${Short.MAX_VALUE}")
    show("Int    (32 bits)", "${Int.MIN_VALUE} .. ${Int.MAX_VALUE}")
    show("Long   (64 bits)", "${Long.MIN_VALUE} .. ${Long.MAX_VALUE}")

    section("Qué tipo se elige solo")

    // Un literal entero sin sufijo es Int... salvo que no quepa, en cuyo caso es Long.
    val pequeno = 100
    val enorme = 10_000_000_000       // no cabe en Int → se infiere Long
    show("val pequeno = 100", pequeno::class.simpleName)
    show("val enorme = 10_000_000_000", enorme::class.simpleName)

    // Para forzar Long cuando el número sí cabría en Int, sufijo L.
    val forzadoLong = 100L
    show("val forzadoLong = 100L", forzadoLong::class.simpleName)

    bullet("Byte y Short casi nunca se usan en código normal: sólo en formatos binarios.")
}

/**
 * Cómo se escriben los literales.
 */
fun demoNumericLiterals() {
    section("Bases")

    show("decimal        123", 123)
    show("hexadecimal    0xFF", 0xFF)
    show("binario        0b1010_1010", 0b1010_1010)

    // Kotlin NO tiene literales octales. `010` es simplemente diez, no ocho.
    show("¡ojo! 010 NO es octal", 10)
    bullet("Kotlin eliminó los literales octales justamente por lo confusos que eran.")

    section("Guiones bajos para separar")

    // Puramente visual: el compilador los ignora.
    val millon = 1_000_000
    val tarjeta = 1234_5678_9012_3456L
    val mascara = 0xFF_EC_DE_5E
    show("1_000_000", millon)
    show("1234_5678_9012_3456L", tarjeta)
    show("0xFF_EC_DE_5E", mascara)

    section("Decimales")

    show("3.14      → Double (por defecto)", (3.14)::class.simpleName)
    show("3.14f     → Float  (sufijo f)", (3.14f)::class.simpleName)
    show("3.14e2    → notación científica", 3.14e2)
    show("1_000.5   → también admite guiones", 1_000.5)
}

/**
 * La regla de oro: **no hay conversiones implícitas**.
 */
fun demoNoImplicitConversions() {
    section("Lo que en Java funciona y aquí no")

    val unInt: Int = 42

    // val unLong: Long = unInt       // ERROR: Type mismatch. Required Long, found Int
    val unLong: Long = unInt.toLong() // así sí
    show("unInt.toLong()", unLong)

    // Ni siquiera hacia un tipo MÁS ancho, que sería seguro. Kotlin prefiere que lo
    // escribas para que la conversión se vea en el código.
    bullet("Int → Long, Int → Double: seguras, pero hay que escribirlas igualmente.")

    section("Excepción: los literales sí se adaptan")

    // Esto ya lo vimos en 1.6: el literal se comprueba al compilar.
    val literalComoLong: Long = 42
    val literalComoByte: Byte = 42
    show("val x: Long = 42   (literal)", literalComoLong)
    show("val y: Byte = 42   (literal)", literalComoByte)

    // ...pero sólo si cabe:
    // val noCabe: Byte = 300   // ERROR: The value is out of range

    section("La familia toXxx()")

    val numero = 3.99
    val trescientos = 300
    show("3.99.toInt()   (trunca, NO redondea)", numero.toInt())
    show("(-3.99).toInt()", (-numero).toInt())
    show("3.99.roundToInt()", numero.roundToInt())
    show("42.toDouble()", 42.toDouble())
    show("Char(65)       (código Unicode → carácter)", Char(65))
    show("300.toByte()   (se pierde información)", trescientos.toByte())

    bullet("toInt() TRUNCA hacia cero: 3.99 → 3, y -3.99 → -3.")
    bullet("Para redondear usa roundToInt() de kotlin.math.")
    bullet("toByte()/toShort() recortan bits en silencio: 300.toByte() da 44.")
}

/**
 * Aritmética: división entera, módulo y desbordamiento.
 */
fun demoArithmetic() {
    section("División: entera o decimal según los operandos")

    show("7 / 2      (Int / Int)", 7 / 2)
    show("7.0 / 2    (Double / Int)", 7.0 / 2)
    show("7 / 2.0", 7 / 2.0)
    show("7 / 2 * 2  (el resto se perdió antes)", 7 / 2 * 2)

    bullet("Si los dos operandos son enteros, la división es ENTERA. Casi siempre es el bug.")
    bullet("Para forzar decimales convierte uno: `a.toDouble() / b`.")

    section("Resto")

    show("7 % 3", 7 % 3)
    show("-7 % 3     (el signo lo pone el dividendo)", -7 % 3)
    show("Math.floorMod(-7, 3)  (resto siempre positivo)", Math.floorMod(-7, 3))

    bullet("Para índices circulares usa floorMod: `%` con negativos devuelve negativo.")

    section("División por cero")

    // Entera: excepción. La capturamos, porque en este repositorio ninguna demo
    // puede propagar una excepción hacia fuera.
    // (Van en variables a propósito: `7 / 0` escrito tal cual es una constante y el
    // compilador lo rechaza antes de llegar a ejecutarse.)
    val siete = 7
    val cero = 0
    val resultadoEntero = try {
        (siete / cero).toString()
    } catch (e: ArithmeticException) {
        "lanzó ArithmeticException: ${e.message}"
    }
    show("7 / 0      (enteros)", resultadoEntero)

    // Decimal: no lanza, devuelve infinito.
    show("7.0 / 0.0  (decimales)", 7.0 / 0.0)
    show("-7.0 / 0.0", -7.0 / 0.0)
    show("0.0 / 0.0", 0.0 / 0.0)

    section("Desbordamiento: silencioso")

    // No hay excepción. El valor da la vuelta.
    val maximo = Int.MAX_VALUE
    val minimo = Int.MIN_VALUE
    show("Int.MAX_VALUE", maximo)
    show("Int.MAX_VALUE + 1", maximo + 1)
    show("Int.MIN_VALUE - 1", minimo - 1)

    // Trampa clásica: multiplicar Ints y guardar el resultado en un Long NO salva
    // nada, porque la multiplicación ya se hizo en Int.
    val cienMil = 100_000
    val bien: Long = cienMil * 100_000L            // un operando es Long → todo en Long
    val mal: Long = (cienMil * cienMil).toLong()   // desbordó ANTES de convertir
    show("100_000 * 100_000L            (bien)", bien)
    show("(100_000 * 100_000).toLong()  (mal)", mal)

    // Si quieres que avise en lugar de dar la vuelta:
    val conAviso = try {
        Math.addExact(Int.MAX_VALUE, 1).toString()
    } catch (e: ArithmeticException) {
        "Math.addExact lanzó ${e.message}"
    }
    show("Math.addExact(Int.MAX_VALUE, 1)", conAviso)
}

/**
 * Coma flotante: por qué 0.1 + 0.2 no es 0.3.
 */
fun demoFloatingPoint() {
    section("La sorpresa de siempre")

    show("0.1 + 0.2", 0.1 + 0.2)
    show("0.1 + 0.2 == 0.3", 0.1 + 0.2 == 0.3)

    // No es un fallo de Kotlin: Double usa base 2 y 0.1 no tiene representación exacta,
    // igual que 1/3 no la tiene en base 10.
    bullet("Pasa en todos los lenguajes con IEEE-754: Java, C, Python, JavaScript...")

    section("Cómo comparar decimales")

    val a = 0.1 + 0.2
    val b = 0.3
    val tolerancia = 1e-9
    show("Math.abs(a - b) < 1e-9", Math.abs(a - b) < tolerancia)

    section("Dinero: nunca en Double")

    // 10 céntimos + 20 céntimos tiene que dar 30 céntimos, exactos.
    val enCentimos = 10L + 20L
    show("céntimos en Long (exacto)", enCentimos)

    val conBigDecimal = java.math.BigDecimal("0.1").add(java.math.BigDecimal("0.2"))
    show("BigDecimal(\"0.1\") + BigDecimal(\"0.2\")", conBigDecimal)

    bullet("Guarda importes como Long de céntimos, o usa BigDecimal creado DESDE String.")
    bullet("BigDecimal(0.1) con un Double dentro arrastra el mismo error: usa el String.")

    section("Float vs Double")

    show("Float  (32 bits, ~7 dígitos)", 0.1f + 0.2f)
    show("Double (64 bits, ~16 dígitos)", 0.1 + 0.2)
    bullet("Usa Double salvo que tengas una razón muy concreta (gráficos, memoria).")

    section("NaN e infinitos")

    val noEsNumero = 0.0 / 0.0
    show("0.0 / 0.0", noEsNumero)
    show("noEsNumero.isNaN()", noEsNumero.isNaN())
    show("noEsNumero == noEsNumero  (¡false!)", noEsNumero == noEsNumero)
    show("Double.POSITIVE_INFINITY", Double.POSITIVE_INFINITY)
    bullet("NaN no es igual ni a sí mismo: comprueba siempre con isNaN().")
}

/**
 * Tipos sin signo. Existen desde Kotlin 1.5 y son estables.
 */
fun demoUnsignedTypes() {
    section("UByte, UShort, UInt, ULong")

    val sinSigno: UInt = 3_000_000_000u   // no cabría en Int
    show("val sinSigno: UInt = 3_000_000_000u", sinSigno)
    show("UInt.MAX_VALUE", UInt.MAX_VALUE)
    show("UByte.MAX_VALUE", UByte.MAX_VALUE)

    section("Operaciones")

    show("10u - 3u", 10u - 3u)
    show("sinSigno.toLong()", sinSigno.toLong())

    // También dan la vuelta, igual que los con signo:
    val ceroSinSigno = 0u
    show("0u - 1u  (desborda por abajo)", ceroSinSigno - 1u)

    section("Cuándo usarlos")

    bullet("Formatos binarios, protocolos de red, hashes: donde el bit de signo estorba.")
    bullet("En lógica de negocio normal, quédate con Int/Long.")
    bullet("Su interoperabilidad con Java es incómoda: Java no tiene enteros sin signo.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `7.0 / 2` por `7 / 2` en demoArithmetic y observa cómo cambia el resultado.
//  2. Prueba `val noCabe: Byte = 300` y lee el error "The value is out of range".
//  3. Descomenta `val unLong: Long = unInt` y comprueba que Kotlin no convierte solo.
//  4. Calcula 0.1 + 0.2 + 0.3 y luego 0.3 + 0.2 + 0.1: el orden cambia el resultado.
