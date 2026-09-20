package com.alejandro.c15lambdas

import com.alejandro.infra.chapter

/**
 * # Capítulo 15 · Lambdas y funciones de orden superior
 *
 * El capítulo 05 presentó la sintaxis; éste se centra en el USO: los patrones reales,
 * los cierres y las lambdas con receptor, que son la puerta de entrada a los DSL.
 *
 * ## Qué se cubre
 * - Las cinco abreviaturas que convierten `map({ n: Int -> n * 2 })` en `map { it * 2 }`.
 * - Parámetros, `it`, `_`, desestructuración y la lambda final fuera de los paréntesis.
 * - Patrones: estrategia, callbacks, envolver operaciones (reintentos, medición,
 *   registro), composición, validadores encadenados y memoización.
 * - Cierres: qué se captura, la trampa del bucle y el coste en memoria.
 * - Lambdas con receptor (`T.() -> R`) y cómo escribir un builder propio.
 *
 * ## Lo que hay que llevarse sí o sí
 * - El valor de una lambda es su **última expresión**; un `return` a secas saldría de
 *   la función que la rodea.
 * - Una lambda captura la **variable**, no su valor: ve los cambios posteriores.
 * - La variable de un `for` es nueva en cada vuelta; una `var` declarada fuera, no.
 *   Si guardas lambdas dentro de un bucle, captura un `val` local.
 * - `T.() -> R` (receptor, se usa `this`) frente a `(T) -> R` (parámetro, se usa `it`).
 *   Son intercambiables, pero dentro se escriben muy distinto.
 * - Al diseñar una función de orden superior, **pon la lambda al final**: es lo que
 *   permite la sintaxis `funcion(args) { ... }`.
 *
 * ## Errores típicos
 * - Callbacks anidados hasta formar una escalera (la respuesta son las corrutinas).
 * - `it` en lambdas anidadas.
 * - Guardar una lambda que captura un objeto grande y no soltarla nunca.
 *
 * Siguiente paso: capítulo 16, scope functions.
 */
val chapter15 = chapter(
    number = 15,
    name = "Lambdas y orden superior",
    summary = "sintaxis, patrones, cierres y lambdas con receptor",
) {
    demo("De la forma larga a `{ it * 2 }`", ::demoSyntaxSteps)
    demo("El valor de una lambda", ::demoLambdaValue)
    demo("Parámetros, `it`, `_` y desestructuración", ::demoParameters)
    demo("La lambda final fuera de los paréntesis", ::demoTrailingLambda)
    demo("Patrón estrategia", ::demoStrategy)
    demo("Callbacks", ::demoCallbacks)
    demo("Envolver operaciones: reintentos, medición, registro", ::demoWrapping)
    demo("Componer, validar y memoizar", ::demoComposition)
    demo("Capturar el entorno", ::demoBasicCapture)
    demo("La trampa del bucle", ::demoLoopCaptureTrap)
    demo("Qué cuesta capturar", ::demoCaptureCost)
    demo("Cierres en la práctica", ::demoPracticalClosures)
    demo("Parámetro frente a receptor", ::demoParameterVsReceiver)
    demo("El ruido que elimina un receptor", ::demoWhyItMatters)
    demo("Escribir tu propio builder", ::demoWritingYourOwn)
    demo("Receptores anidados", ::demoNestedReceivers)
}

/** Ejecuta el capítulo 15 completo. */
fun main() = chapter15.runAll()
