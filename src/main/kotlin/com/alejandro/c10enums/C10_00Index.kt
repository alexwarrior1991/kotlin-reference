package com.alejandro.c10enums

import com.alejandro.infra.chapter

/**
 * # Capítulo 10 · Enum classes
 *
 * Un tipo con un conjunto fijo y conocido de valores. En Kotlin un enum es una clase
 * de verdad: tiene constructor, propiedades, métodos, y puede dar una implementación
 * distinta a cada constante.
 *
 * ## Qué se cubre
 * - Declaración, `name`, `ordinal` y `entries`.
 * - Enums con propiedades, métodos y fábricas en el `companion object`.
 * - Cuerpo propio por constante (el patrón estrategia sin clases extra).
 * - Enums que implementan interfaces, y su orden natural.
 * - `entries` frente a `values()`, `valueOf` y sus alternativas seguras.
 * - El `when` exhaustivo, que es la razón principal para usar un enum.
 * - `EnumMap` y `EnumSet`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Usa `entries` (Kotlin 1.9+), no `values()`: devuelve siempre la misma lista en
 *   lugar de crear un array nuevo en cada llamada.
 * - **Nunca persistas `ordinal`**: reordenar las constantes corrompe los datos en
 *   silencio. Guarda el `name` o un código explícito.
 * - `valueOf` lanza `IllegalArgumentException`; para texto de fuera usa
 *   `entries.find { it.name.equals(texto, ignoreCase = true) }`.
 * - En un `when` sobre un enum, **no pongas `else`**: sin él, añadir una constante
 *   nueva rompe la compilación allí donde hay que actualizar algo. Con `else`, el
 *   fallo aparece en ejecución.
 *
 * ## Cuándo NO usar un enum
 * - Cuando cada caso lleva **datos distintos**. Eso es una jerarquía `sealed`, que es
 *   justo el capítulo siguiente.
 *
 * Siguiente paso: capítulo 11, sealed classes e interfaces.
 */
val chapter10 = chapter(
    number = 10,
    name = "Enum classes",
    summary = "constantes con comportamiento, entries, valueOf, when exhaustivo",
) {
    demo("El enum más simple", ::demoSimpleEnum)
    demo("Enums con propiedades y métodos", ::demoEnumWithProperties)
    demo("Cuerpo propio por constante", ::demoConstantSpecificBodies)
    demo("Enums que implementan interfaces", ::demoEnumImplementingInterface)
    demo("entries frente a values()", ::demoEntriesVsValues)
    demo("valueOf y conversión segura desde texto", ::demoValueOf)
    demo("ordinal: por qué nunca debe persistirse", ::demoOrdinal)
    demo("El when exhaustivo", ::demoExhaustiveWhen)
    demo("EnumMap y EnumSet", ::demoEnumCollections)
}

/** Ejecuta el capítulo 10 completo. */
fun main() = chapter10.runAll()
