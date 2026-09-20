package com.alejandro.c21operatoroverloading

import com.alejandro.infra.chapter

/**
 * # Capítulo 21 · Sobrecarga de operadores
 *
 * El capítulo 03 explicó que cada operador es una función con nombre convenido
 * (`a + b` es `a.plus(b)`). Aquí se escribe esa función para clases propias.
 *
 * ## Qué se cubre
 * - Aritméticos con `Vector2D`: plus, minus, times, div, unarios, y la diferencia
 *   entre `plus` (inmutable) y `plusAssign` (mutable).
 * - `inc` / `dec` y por qué deben devolver un objeto nuevo.
 * - Comparación con `Dinero`: `compareTo` habilita cuatro operadores de golpe,
 *   `rangeTo` habilita `..` y `contains` habilita `in`.
 * - `get`, `set` con varios índices, `invoke`, `iterator` y `componentN` con `Matriz`.
 * - La tabla completa y la regla de oro para decidir cuándo sobrecargar.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Sin la palabra `operator`, la función existe pero el símbolo no funciona.
 * - Inmutable → define `plus`. Mutable → define `plusAssign`. **Nunca los dos**, o
 *   tendrás "Assignment operators ambiguity".
 * - En `compareTo`, **nunca restes**: con valores extremos la resta desborda y
 *   devuelve el signo contrario. Usa `compareTo` o `compareValuesBy`.
 * - `m[fila, columna]` con varios índices es algo que Java no tiene y que hace que
 *   el código matricial se lea como la fórmula.
 * - `componentN` e `iterator` se pueden **añadir desde fuera** con extensiones.
 *
 * ## La regla de oro
 * Enseña la expresión a alguien que no conozca tu clase. ¿Adivina qué hace?
 * Adelante. ¿Tiene que preguntar? Función con nombre.
 *
 * Siguiente paso: capítulo 22, desestructuración.
 */
val chapter21 = chapter(
    number = 21,
    name = "Sobrecarga de operadores",
    summary = "plus, times, compareTo, rangeTo, get/set, invoke, iterator y componentN",
) {
    demo("Aritméticos: el caso Vector2D", ::demoBasicArithmetic)
    demo("plus frente a plusAssign", ::demoCompoundAssignment)
    demo("inc y dec", ::demoIncrement)
    demo("compareTo: cuatro operadores por uno", ::demoComparison)
    demo("rangeTo y contains", ::demoRangesAndContains)
    demo("Igualdad y tipos que impiden errores", ::demoEquality)
    demo("get y set con varios índices", ::demoGetAndSet)
    demo("invoke: el objeto que se llama", ::demoInvoke)
    demo("iterator: recorrer con for", ::demoIterator)
    demo("componentN: desestructurar", ::demoComponentN)
    demo("La regla de oro y la tabla completa", ::demoTheRule)
}

/** Ejecuta el capítulo 21 completo. */
fun main() = chapter21.runAll()
