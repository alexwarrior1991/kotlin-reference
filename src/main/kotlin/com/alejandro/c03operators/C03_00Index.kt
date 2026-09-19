package com.alejandro.c03operators

import com.alejandro.infra.chapter

/**
 * # Capítulo 03 · Operadores
 *
 * La idea que lo vertebra todo: en Kotlin **cada operador es una función con nombre
 * convenido**. `a + b` es `a.plus(b)`, `a < b` es `a.compareTo(b) < 0`, `x in lista`
 * es `lista.contains(x)`. Saberlo explica por qué puedes sumar listas con `+` y
 * prepara el capítulo 21, donde se definen operadores para clases propias.
 *
 * ## Qué se cubre
 * - Aritméticos, asignación compuesta, incrementos y operaciones bit a bit.
 * - Precedencia, con los casos concretos en los que la gente se equivoca.
 * - Igualdad estructural (`==`) frente a referencial (`===`), y el contrato
 *   `equals`/`hashCode`.
 * - Comparación vía `compareTo` y `Comparable`.
 * - Lógicos con cortocircuito (`&&`, `||`) y sin él (`and`, `or`).
 * - Nulos: `?.`, `?:`, `!!`. Pertenencia: `in`, `!in`.
 * - Qué operadores se pueden sobrecargar y cuáles no.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `==` es contenido y `===` es identidad: **justo al revés que en Java**.
 * - `==` nunca lanza NPE, ni siquiera con el receptor a null.
 * - `===` sobre números miente por la caché de enteros de la JVM (-128..127).
 * - Si sobrescribes `equals`, sobrescribe `hashCode` con los mismos campos.
 * - `&&`/`||` cortocircuitan; `and`/`or` no.
 *
 * Siguiente paso: capítulo 04, control de flujo.
 */
val chapter03 = chapter(
    number = 3,
    name = "Operadores",
    summary = "aritméticos, igualdad, comparación, lógicos, nulos, `in` y sobrecarga",
) {
    demo("Cada operador es una función con nombre", ::demoArithmeticOperators)
    demo("Asignación compuesta: plus vs plusAssign", ::demoCompoundAssignment)
    demo("Incremento y decremento, prefijo y sufijo", ::demoIncrementDecrement)
    demo("Operaciones bit a bit: and, or, shl, ushr", ::demoBitwiseOperators)
    demo("Precedencia y dónde se equivoca la gente", ::demoPrecedence)
    demo("== frente a ===, y la caché de enteros", ::demoStructuralVsReferential)
    demo("El contrato equals / hashCode", ::demoEqualsContract)
    demo("Comparación: compareTo y Comparable", ::demoComparison)
    demo("Lógicos: cortocircuito de && y ||", ::demoLogicalOperators)
    demo("Llamada segura: ?.", ::demoSafeCall)
    demo("El operador Elvis: ?:", ::demoElvis)
    demo("La aserción !! y por qué evitarla", ::demoNotNullAssertion)
    demo("Pertenencia: in y !in", ::demoMembership)
    demo("Qué se puede sobrecargar y qué no", ::demoOverloadableOperators)
}

/** Ejecuta el capítulo 03 completo. */
fun main() = chapter03.runAll()
