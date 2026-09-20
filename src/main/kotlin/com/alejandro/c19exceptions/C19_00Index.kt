package com.alejandro.c19exceptions

import com.alejandro.infra.chapter

/**
 * # Capítulo 19 · Excepciones
 *
 * El mecanismo de la JVM, con una diferencia importante: en Kotlin **no hay
 * excepciones comprobadas**. Nadie te obliga a capturar nada, así que decidir qué
 * lanzar y qué capturar pasa a ser una decisión de diseño.
 *
 * ## Qué se cubre
 * - `throw`, `try`/`catch`/`finally` y `try` como expresión.
 * - Qué capturar y qué no, y cómo relanzar conservando la causa.
 * - Excepciones propias: con datos dentro, en jerarquía, y selladas.
 * - Cómo escribir mensajes que sirvan a quien los lea a las tres de la mañana.
 * - `Result` y `runCatching`: map, mapCatching, recover, fold, onSuccess/onFailure.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Captura el tipo **más específico** que sepas tratar. Si no sabes qué hacer con
 *   una excepción, no la captures.
 * - `catch (e: Exception)` se traga también los **bugs** (NPE, IndexOutOfBounds) que
 *   deberían llegar arriba.
 * - Al relanzar, pasa la original como `cause`. Si no, pierdes la traza.
 * - Nunca pongas `return` dentro de un `finally`: descarta la excepción en curso.
 * - **`runCatching` captura `Throwable`, incluida `CancellationException`**. En
 *   corrutinas eso rompe la cancelación en silencio (capítulo 28).
 * - Si existe una variante `...OrNull`, úsala: construir una excepción no es gratis.
 *
 * ## La tabla de decisión
 * ```
 * Error esperado, un solo motivo      → tipo nulable (T?)
 * Error esperado, varios motivos      → sealed propia (capítulo 11)
 * Envolver código ajeno que lanza     → runCatching / Result
 * Error de programación (un bug)      → excepción, y que suba
 * ```
 *
 * Siguiente paso: capítulo 20, comprobaciones de tipo y casts.
 */
val chapter19 = chapter(
    number = 19,
    name = "Excepciones",
    summary = "throw, try/catch/finally, excepciones propias, Result y runCatching",
) {
    demo("throw y la ausencia de checked exceptions", ::demoThrow)
    demo("try como expresión", ::demoTryAsExpression)
    demo("finally y la gestión de recursos", ::demoFinally)
    demo("Qué capturar y qué no", ::demoWhatToCatch)
    demo("Declarar excepciones propias", ::demoDeclaring)
    demo("Jerarquías de excepciones", ::demoHierarchies)
    demo("Escribir buenos mensajes", ::demoGoodMessages)
    demo("Result y runCatching", ::demoBasics)
    demo("Transformar y encadenar un Result", ::demoTransforming)
    demo("La trampa de CancellationException", ::demoCancellationTrap)
    demo("Cuándo usar Result y cuándo no", ::demoWhenToUse)
}

/** Ejecuta el capítulo 19 completo. */
fun main() = chapter19.runAll()
