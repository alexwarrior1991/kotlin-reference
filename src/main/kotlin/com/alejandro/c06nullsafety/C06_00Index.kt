package com.alejandro.c06nullsafety

import com.alejandro.infra.chapter

/**
 * # Capítulo 06 · Null safety
 *
 * La característica por la que mucha gente llega a Kotlin. `String` y `String?` son
 * tipos distintos, y el compilador no te deja confundirlos: el NullPointerException
 * pasa de ser un fallo en producción a un error de compilación.
 *
 * ## Qué se cubre
 * - Tipos nulables, su relación de subtipado y dónde colocar el `?` en un genérico.
 * - `?.`, `?:`, `?.let`, `!!` vistos como decisiones de diseño, no como símbolos.
 * - Smart casts y, sobre todo, **por qué a veces no se aplican**.
 * - `as` / `as?`, `lateinit` y los tipos plataforma que llegan de Java.
 * - Cómo diseñar para no necesitar nulos.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `List<String?>` y `List<String>?` son cosas distintas.
 * - El patrón estrella: `val x = algo ?: return` (o `?: throw`) al principio de la
 *   función; el resto del cuerpo ya trabaja con un valor garantizado.
 * - El smart cast falla con propiedades `var`, `open`, con getter propio o de otro
 *   módulo. La solución es siempre copiar a un `val` local.
 * - `requireNotNull(x) { "mensaje" }` es infinitamente mejor que `x!!`.
 * - Devuelve una colección vacía, no `null`.
 *
 * ## Errores típicos
 * - Repartir `!!` para callar al compilador.
 * - Anidar tres `?.let` en lugar de escribir tres guardas.
 * - Usar `null` para decir a la vez "no hay resultado" y "hubo un error".
 *
 * Siguiente paso: capítulo 07, clases y objetos.
 */
val chapter06 = chapter(
    number = 6,
    name = "Null safety",
    summary = "tipos nulables, ?., ?:, !!, smart casts, lateinit y diseño sin nulos",
) {
    demo("String frente a String?", ::demoNullableVsNonNullable)
    demo("Comprobar con if", ::demoNullCheck)
    demo("Kotlin frente a Optional de Java", ::demoVsOptional)
    demo("Dónde sí puede haber NPE en Kotlin", ::demoWhereNpeStillHappens)
    demo("Cadenas de llamadas seguras", ::demoSafeCallChains)
    demo("Los cuatro patrones con Elvis", ::demoElvisPatterns)
    demo("?.let: cuándo sí y cuándo no", ::demoSafeLet)
    demo("!! y sus alternativas, de mejor a peor", ::demoNotNullAssertionInDepth)
    demo("Smart casts", ::demoSmartCasts)
    demo("Cuándo el smart cast NO se aplica", ::demoSmartCastLimits)
    demo("as y as?", ::demoCasts)
    demo("lateinit", ::demoLateinit)
    demo("Platform types: lo que llega de Java", ::demoPlatformTypes)
    demo("Diseñar para no necesitar nulos", ::demoDesignWithoutNulls)
}

/** Ejecuta el capítulo 06 completo. */
fun main() = chapter06.runAll()
