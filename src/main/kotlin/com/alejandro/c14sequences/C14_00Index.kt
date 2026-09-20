package com.alejandro.c14sequences

import com.alejandro.infra.chapter

/**
 * # Capítulo 14 · Secuencias
 *
 * Las mismas operaciones del capítulo 13, pero evaluadas de forma **perezosa**: nada
 * ocurre hasta que pides el resultado, y entonces cada elemento recorre toda la cadena
 * de una vez en lugar de crear una lista intermedia por operación.
 *
 * ## Qué se cubre
 * - El orden real de evaluación de una lista frente a una secuencia, trazado en
 *   ejecución para que se vea.
 * - Operaciones intermedias (perezosas) y terminales (las que disparan el trabajo).
 * - Cortocircuito: por qué `first` sobre una secuencia hace 6 operaciones y sobre una
 *   lista hace un millón.
 * - Crear secuencias: `asSequence`, `sequenceOf`, `generateSequence`, `sequence { }`.
 * - Secuencias infinitas: Fibonacci, primos, backoff exponencial.
 * - Cuándo compensan y cuándo no, con mediciones orientativas.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Lista: **por operaciones** (los cuatro `map`, después los cuatro `filter`).
 *   Secuencia: **por elementos** (cada uno atraviesa toda la cadena).
 * - Sin operación **terminal** una secuencia no hace absolutamente nada.
 * - Nunca llames a `toList()`, `count()` o `sum()` sobre una secuencia infinita.
 * - `sorted` es intermedia pero tiene que materializar todo: ponla lo más tarde posible.
 * - Regla de bolsillo: menos de ~1.000 elementos o una sola operación → colección.
 *   Muchos elementos y varias operaciones encadenadas → secuencia.
 *
 * ## Errores típicos
 * - Usar secuencias para tres elementos y perder rendimiento.
 * - Olvidar la terminal y creer que el código "no hace nada".
 * - Guardar una secuencia y recorrerla dos veces.
 *
 * Siguiente paso: capítulo 15, lambdas y funciones de orden superior.
 */
val chapter14 = chapter(
    number = 14,
    name = "Secuencias",
    summary = "evaluación perezosa, operaciones intermedias y terminales, infinitas",
) {
    demo("El orden de evaluación: eager frente a lazy", ::demoEvaluationOrder)
    demo("Operaciones intermedias y terminales", ::demoIntermediateAndTerminal)
    demo("Cortocircuito: la gran ventaja", ::demoShortCircuiting)
    demo("Limitaciones de las secuencias", ::demoSequenceLimitations)
    demo("Las cuatro formas de crear una secuencia", ::demoCreatingSequences)
    demo("Secuencias infinitas", ::demoInfiniteSequences)
    demo("Cuándo usarlas y cuándo no", ::demoWhenToUse)
    demo("Comparación de rendimiento (orientativa)", ::demoPerformanceComparison)
}

/** Ejecuta el capítulo 14 completo. */
fun main() = chapter14.runAll()
