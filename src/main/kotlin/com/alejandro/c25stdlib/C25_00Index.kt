package com.alejandro.c25stdlib

import com.alejandro.infra.chapter

/**
 * # Capítulo 25 · Biblioteca estándar
 *
 * Un repaso a las funciones pequeñas que aparecen constantemente y que, bien usadas,
 * quitan bastantes líneas. Muchas ya han ido saliendo en capítulos anteriores; aquí
 * se juntan y se comparan.
 *
 * ## Qué se cubre
 * - Contratos: `require`, `requireNotNull`, `check`, `checkNotNull`, `error`,
 *   `assert` y `TODO()`.
 * - Builders: `buildList`, `buildSet`, `buildMap`, `buildString`, `repeat`, `lazy`.
 * - `Pair`, `Triple`, `takeIf`, `takeUnless` y las conversiones seguras `...OrNull`.
 * - `Comparable`, `Comparator`, `use`, `kotlin.time` y `Random` reproducible.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`require` → la culpa es de quien llama. `check` → la culpa es del estado.**
 *   Esa diferencia le dice a quien lee la traza dónde buscar.
 * - El mensaje de `require`/`check` va en una lambda: **sólo se construye si falla**,
 *   así que puede ser todo lo detallado que quieras.
 * - **`assert` no hace nada sin `-ea`**. Si la condición importa, usa `check`.
 * - `buildList` deja claro en el tipo que sale una colección de sólo lectura.
 * - `buildString` para bucles; una plantilla para tres partes.
 * - `takeIf { } ?: porDefecto` es el patrón completo; sin el `?:` sólo añade un nulo.
 * - **`use` cierra siempre**, incluso si el bloque lanza. Nunca cierres a mano.
 * - Mide con `measureTime` / `TimeSource.Monotonic`, nunca con
 *   `System.currentTimeMillis()`, que puede saltar hacia atrás.
 * - `Random(semilla)` en tests y demos: sin semilla tendrás fallos intermitentes.
 *
 * ## Errores típicos
 * - `Triple<String, Int, Boolean>` en una API pública: nadie sabe qué es cada cosa.
 * - Dejar un `TODO()` en producción.
 * - Ordenar versiones como texto: `"1.10" < "1.2"`.
 *
 * Siguiente paso: capítulo 26, ficheros y entrada/salida.
 */
val chapter25 = chapter(
    number = 25,
    name = "Biblioteca estándar",
    summary = "require/check/assert/TODO, builders, Pair, takeIf, use, tiempo y Random",
) {
    demo("require: los argumentos", ::demoRequire)
    demo("check y error: el estado", ::demoCheck)
    demo("assert y por qué casi nunca hace nada", ::demoAssert)
    demo("TODO(): el hueco que compila", ::demoTodo)
    demo("buildList, buildSet y buildMap", ::demoCollectionBuilders)
    demo("buildString", ::demoBuildString)
    demo("repeat y lazy", ::demoRepeatAndLazy)
    demo("Otras utilidades de construcción", ::demoOtherUtilities)
    demo("Pair y Triple", ::demoPairAndTriple)
    demo("takeIf y takeUnless", ::demoTakeIf)
    demo("Utilidades sueltas que conviene conocer", ::demoMiscUtilities)
    demo("Comparable y Comparator", ::demoComparables)
    demo("use: cerrar recursos sin olvidarse", ::demoUse)
    demo("kotlin.time: Duration y medición", ::demoTime)
    demo("Random reproducible", ::demoRandom)
}

/** Ejecuta el capítulo 25 completo. */
fun main() = chapter25.runAll()
