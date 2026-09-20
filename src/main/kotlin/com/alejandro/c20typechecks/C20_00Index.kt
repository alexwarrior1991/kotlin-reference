package com.alejandro.c20typechecks

import com.alejandro.infra.chapter

/**
 * # Capítulo 20 · Comprobaciones de tipo y casts
 *
 * `is`, `!is`, `as`, `as?` y el smart cast. Un capítulo corto porque, en Kotlin bien
 * escrito, los casts explícitos casi no aparecen: el compilador los hace por ti.
 *
 * ## Qué se cubre
 * - `is` / `!is` y el smart cast, también en condiciones compuestas y en `when`.
 * - Los cinco casos en los que el smart cast NO se aplica.
 * - `as` (lanza) frente a `as?` (devuelve null), y el patrón `as? ... ?:`.
 * - Casts de genéricos y el aviso "Unchecked cast".
 * - Por qué con una jerarquía `sealed` los casts sobran.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Tras `if (x is T)`, dentro del bloque `x` **ya es T**: nada de castear a mano.
 * - El smart cast falla con propiedades `var`, `open`, con getter propio, de otro
 *   módulo, o variables locales que una lambda puede modificar. La solución siempre
 *   es la misma: **copiar a un `val` local**.
 * - `as` es una afirmación tuya, como `!!`. `as?` es una comprobación.
 * - Un cast de genéricos **no comprueba nada**: el fallo llega después y lejos.
 *   Para validar de verdad, `filterIsInstance` o comprobar elemento a elemento.
 *
 * ## El orden de preferencia
 * ```
 * 1. sealed + when exhaustivo   → no hace falta ningún cast
 * 2. is + smart cast            → el compilador convierte por ti
 * 3. as? + ?:                   → conversión con camino de fallo explícito
 * 4. as                         → sólo afirmando una invariante propia
 * ```
 *
 * Siguiente paso: capítulo 21, sobrecarga de operadores.
 */
val chapter20 = chapter(
    number = 20,
    name = "Comprobaciones y casts",
    summary = "is, !is, smart casts, as, as? y jerarquías sealed",
) {
    demo("is, !is y smart casts", ::demoIsBasics)
    demo("is dentro de un when", ::demoIsInWhen)
    demo("Cuándo el smart cast NO funciona", ::demoSmartCastLimits)
    demo("as: el cast inseguro", ::demoUnsafeCast)
    demo("as?: el cast seguro", ::demoSafeCast)
    demo("Casts de genéricos y el aviso Unchecked cast", ::demoGenericCasts)
    demo("Casts en jerarquías sealed", ::demoSealedHierarchies)
}

/** Ejecuta el capítulo 20 completo. */
fun main() = chapter20.runAll()
