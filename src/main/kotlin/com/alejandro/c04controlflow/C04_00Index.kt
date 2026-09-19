package com.alejandro.c04controlflow

import com.alejandro.infra.chapter

/**
 * # Capítulo 04 · Control de flujo
 *
 * Condicionales, selección múltiple, bucles y saltos. La novedad respecto a Java no
 * está en la sintaxis sino en que **`if`, `when` y `try` devuelven un valor**, lo que
 * cambia cómo se escribe el código que los usa.
 *
 * ## Qué se cubre
 * - `if` como sentencia y como expresión; por qué no hace falta el operador ternario.
 * - `when` con y sin sujeto, con rangos, con tipos, con captura del sujeto, y la
 *   exhaustividad que el compilador puede comprobar.
 * - `for`, `while`, `do-while` y `repeat`; cuándo un bucle no es la mejor herramienta.
 * - `break`, `continue`, etiquetas, `return@lambda` y `try` como expresión.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Como expresión, `if` y `when` necesitan `else` (salvo que el compilador pueda
 *   demostrar que los casos están cubiertos).
 * - `when` no tiene *fallthrough*: nada de `break` en cada rama.
 * - `for` siempre recorre algo iterable; el `for` clásico de tres partes no existe.
 * - `return` dentro de una lambda sale de la **función**, no de la vuelta: para saltar
 *   un elemento se usa `return@forEach`.
 * - Para romper un bucle anidado, etiquetas; no banderas booleanas.
 *
 * ## Errores típicos
 * - Usar excepciones (`try`) para el flujo normal en lugar de `toIntOrNull()`.
 * - Modificar una lista mientras se recorre.
 * - Escribir cadenas largas de `if / else if` que piden a gritos un `when`.
 *
 * Siguiente paso: capítulo 05, funciones.
 */
val chapter04 = chapter(
    number = 4,
    name = "Control de flujo",
    summary = "if, when, for, while, break, continue, etiquetas y try como expresión",
) {
    demo("if como sentencia", ::demoIfAsStatement)
    demo("if como expresión", ::demoIfAsExpression)
    demo("Por qué no hay operador ternario", ::demoNoTernaryOperator)
    demo("when con sujeto", ::demoWhenWithSubject)
    demo("when con rangos y colecciones", ::demoWhenWithRanges)
    demo("when con tipos y smart cast", ::demoWhenWithTypes)
    demo("when sin sujeto: adiós a if/else if", ::demoWhenWithoutSubject)
    demo("when (val x = ...): capturar el sujeto", ::demoWhenWithSubjectCapture)
    demo("Exhaustividad: cuándo hace falta else", ::demoWhenExhaustiveness)
    demo("for sobre rangos, colecciones y mapas", ::demoForLoop)
    demo("while y do-while", ::demoWhileLoops)
    demo("repeat: una función, no una palabra clave", ::demoRepeat)
    demo("Bucle explícito vs operaciones de colección", ::demoLoopVsCollectionOperations)
    demo("break y continue", ::demoBreakAndContinue)
    demo("Etiquetas para bucles anidados", ::demoLabels)
    demo("return dentro de una lambda", ::demoReturnInLambdas)
    demo("try como expresión", ::demoTryAsExpression)
}

/** Ejecuta el capítulo 04 completo. */
fun main() = chapter04.runAll()
