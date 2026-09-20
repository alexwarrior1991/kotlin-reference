package com.alejandro.c03operators

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  3.1 · Operadores aritméticos, asignación compuesta y precedencia
//
//  QUÉ ES
//    Los operadores de siempre (+ - * / %), sus versiones con asignación (+=, -=...),
//    los incrementos (++, --) y las operaciones bit a bit.
//
//  POR QUÉ IMPORTA
//    En Kotlin cada operador es azúcar sintáctico para una FUNCIÓN con nombre fijo:
//    `a + b` es literalmente `a.plus(b)`. Entender eso es la puerta de entrada a la
//    sobrecarga de operadores (capítulo 21) y explica por qué puedes sumar un Duration
//    a un instante, o concatenar listas con `+`.
//
//  ERRORES COMUNES
//    · Usar `++` sobre un `val` (no compila) o esperar que devuelva lo que no devuelve.
//    · Escribir `a & b` para bits: en Kotlin es `a and b`.
//    · Fiarse de la precedencia en expresiones largas en lugar de poner paréntesis.
// =====================================================================================

/**
 * Cada operador aritmético es una llamada a una función con nombre convenido.
 */
fun demoArithmeticOperators() {
    val a = 17
    val b = 5

    section("El operador y la función que hay detrás")

    show("a + b   →  a.plus(b)", "${a + b}  ==  ${a.plus(b)}")
    show("a - b   →  a.minus(b)", "${a - b}  ==  ${a.minus(b)}")
    show("a * b   →  a.times(b)", "${a * b}  ==  ${a.times(b)}")
    show("a / b   →  a.div(b)", "${a / b}  ==  ${a.div(b)}")
    show("a % b   →  a.rem(b)", "${a % b}  ==  ${a.rem(b)}")
    show("-a      →  a.unaryMinus()", "${-a}  ==  ${a.unaryMinus()}")

    bullet("No es una metáfora: el compilador traduce `a + b` a `a.plus(b)` tal cual.")
    bullet("Por eso puedes definir `plus` en TU clase y usar `+` con ella (capítulo 21).")

    section("El mismo operador con otros tipos")

    // `+` está definido en muchos sitios de la biblioteca estándar.
    show("\"Kot\" + \"lin\"", "Kot" + "lin")
    show("listOf(1, 2) + listOf(3)", listOf(1, 2) + listOf(3))
    show("listOf(1, 2) + 3", listOf(1, 2) + 3)
    show("setOf(1, 2) - 2", setOf(1, 2) - 2)
    show("mapOf(\"a\" to 1) + (\"b\" to 2)", mapOf("a" to 1) + ("b" to 2))
    show("'a' + 1", 'a' + 1)
}

/**
 * Asignación compuesta: `+=` y familia.
 */
fun demoCompoundAssignment() {
    section("Sobre un var")

    var total = 10
    total += 5
    show("total += 5", total)
    total -= 3
    show("total -= 3", total)
    total *= 2
    show("total *= 2", total)
    total /= 4
    show("total /= 4", total)
    total %= 4
    show("total %= 4", total)

    section("El matiz con las colecciones")

    // Con una colección MUTABLE, `+=` la modifica en el sitio (llama a plusAssign).
    val mutable = mutableListOf(1, 2)
    mutable += 3
    show("val mutable (MutableList) += 3", mutable)
    bullet("Aquí `+=` llamó a plusAssign(): modificó la lista existente.")

    // Con una colección de sólo lectura declarada como `var`, `+=` crea una lista
    // NUEVA y reasigna la variable (llama a plus).
    var soloLectura = listOf(1, 2)
    soloLectura += 3
    show("var soloLectura (List) += 3", soloLectura)
    bullet("Aquí `+=` llamó a plus() y reasignó: se creó una lista nueva.")

    bullet("Misma sintaxis, coste muy distinto. Dentro de un bucle grande, importa.")
}

/**
 * Incremento y decremento, con la diferencia entre prefijo y sufijo.
 */
fun demoIncrementDecrement() {
    section("++ y -- sólo sobre `var`")

    var contador = 0
    contador++
    ++contador
    show("tras contador++ y ++contador", contador)

    // val fijo = 0; fijo++   // ERROR: Val cannot be reassigned

    section("Prefijo vs sufijo: qué VALOR devuelve la expresión")

    var i = 5
    val conSufijo = i++     // devuelve 5 y LUEGO incrementa
    show("val x = i++  → x", conSufijo)
    show("             → i", i)

    var j = 5
    val conPrefijo = ++j    // incrementa y LUEGO devuelve 6
    show("val y = ++j  → y", conPrefijo)
    show("             → j", j)

    bullet("Sufijo: usa el valor viejo. Prefijo: usa el nuevo.")
    bullet("Si no aprovechas el valor devuelto, da igual: usa el que prefieras.")
    bullet("Mezclar ++ dentro de una expresión larga es una receta para los bugs.")
}

/**
 * Operaciones bit a bit. En Kotlin son funciones infijas con nombre, no símbolos.
 */
fun demoBitwiseOperators() {
    section("Nombres en lugar de símbolos")

    val a = 0b1100    // 12
    val b = 0b1010    // 10

    show("a = 0b1100", a)
    show("b = 0b1010", b)
    show("a and b   (Java: a & b)", (a and b).toString(2).padStart(4, '0'))
    show("a or b    (Java: a | b)", (a or b).toString(2).padStart(4, '0'))
    show("a xor b   (Java: a ^ b)", (a xor b).toString(2).padStart(4, '0'))
    show("a.inv()   (Java: ~a)", a.inv())

    section("Desplazamientos")

    show("1 shl 4   (Java: 1 << 4)", 1 shl 4)
    show("16 shr 2  (Java: 16 >> 2)", 16 shr 2)
    show("-16 shr 2  (mantiene el signo)", -16 shr 2)
    show("-16 ushr 28 (rellena con ceros)", -16 ushr 28)

    bullet("Kotlin usó nombres para dejar los símbolos libres y evitar la precedencia rara de C.")
    bullet("Sólo existen para Int y Long. Para Boolean se usan `and`/`or` sin cortocircuito.")
}

/**
 * Precedencia: lo justo para no equivocarse.
 */
fun demoPrecedence() {
    section("De más a menos prioridad")

    bullet("1. sufijos:  ++  --  .  ?.  ()  []")
    bullet("2. prefijos: -  +  ++  --  !")
    bullet("3. multiplicativos:  *  /  %")
    bullet("4. aditivos:  +  -")
    bullet("5. rango:  ..  ..<")
    bullet("6. infijas con nombre:  and, or, shl, step, downTo, to...")
    bullet("7. elvis:  ?:")
    bullet("8. comparación:  <  >  <=  >=")
    bullet("9. igualdad:  ==  !=  ===  !==")
    bullet("10. conjunción:  &&")
    bullet("11. disyunción:  ||")
    bullet("12. asignación:  =  +=  -=  *=  /=  %=")

    section("Casos en los que la gente se equivoca")

    show("2 + 3 * 4", 2 + 3 * 4)
    show("(2 + 3) * 4", (2 + 3) * 4)

    // El rango tiene MENOS prioridad que la suma: `1..n + 1` es `1..(n + 1)`.
    val n = 3
    show("1..n + 1   se lee 1..(n+1)", (1..n + 1).toList())
    show("(1..n) + 1 es otra cosa", (1..n).toList() + 1)

    // Las funciones infijas tienen menos prioridad que la aritmética.
    show("1..10 step 2 + 1   se lee step (2+1)", (1..10 step 2 + 1).toList())

    section("La recomendación")

    bullet("Memoriza sólo que `*` va antes que `+`. Para el resto, pon paréntesis.")
    bullet("Los paréntesis no cuestan nada y evitan discusiones en la revisión de código.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `val mutable = mutableListOf(1, 2)` por `listOf(1, 2)` y mira qué falla.
//  2. Prueba `var x = 5; val y = x++ + ++x` y razona el resultado antes de ejecutarlo.
//  3. Escribe `1..3 + 1` y `(1..3) + 1` y compara las salidas.
//  4. Convierte `a xor b` a binario con toString(2) y comprueba bit a bit el resultado.
