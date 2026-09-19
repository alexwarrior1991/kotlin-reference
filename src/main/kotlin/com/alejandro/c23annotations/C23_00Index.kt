package com.alejandro.c23annotations

import com.alejandro.infra.chapter

/**
 * # Capítulo 23 · Anotaciones
 *
 * Metadatos adjuntos a una declaración. Por sí solas no hacen nada: alguien —el
 * compilador, un framework, la reflexión— tiene que leerlas y actuar.
 *
 * ## Qué se cubre
 * - Las que se usan a diario: `@Deprecated` (con `ReplaceWith`), `@Suppress`,
 *   `@JvmStatic`, `@JvmOverloads`, `@Throws`, `@Volatile`.
 * - **Use-site targets**: `@field:`, `@get:`, `@param:`, `@property:`, `@file:`.
 * - Anotaciones propias: `@Target` y `@Retention`.
 * - Un validador declarativo completo, escrito con anotaciones + reflexión.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Una propiedad de Kotlin genera hasta cuatro cosas (parámetro, propiedad, campo,
 *   getter). Si una anotación "no hace nada", **lo primero que hay que mirar es el
 *   use-site target**. `@field:Inject` y `@Inject` pueden comportarse distinto.
 * - **La retención por defecto en Kotlin es `RUNTIME`**, al contrario que en Java,
 *   donde es `CLASS`. Si vienes de Java es fácil asumir lo contrario.
 * - Una anotación `SOURCE` no existe en ejecución: la reflexión nunca la verá.
 * - Los parámetros de una anotación deben ser **constantes de compilación**:
 *   primitivos, String, KClass, enums, otras anotaciones y arrays de ésos.
 *
 * ## Cuándo NO crear una anotación
 * Si sólo la vas a leer tú en un sitio, un parámetro normal es más simple. Si el
 * comportamiento depende del tipo, una interfaz es más directa y además la comprueba
 * el compilador. La anotación gana cuando el dato es **declarativo** y lo lee un
 * mecanismo genérico que no conoce tus clases.
 *
 * Siguiente paso: capítulo 24, reflexión.
 */
val chapter23 = chapter(
    number = 23,
    name = "Anotaciones",
    summary = "@Deprecated, @Suppress, use-site targets, @Target, @Retention y anotaciones propias",
) {
    demo("Las anotaciones del día a día", ::demoCommonAnnotations)
    demo("Use-site targets: @field:, @get:, @param:", ::demoUseSiteTargets)
    demo("Dónde se pueden poner", ::demoWhereTheyGo)
    demo("Declarar una anotación propia", ::demoDeclaring)
    demo("@Target: dónde se puede poner", ::demoTarget)
    demo("@Retention: hasta cuándo existe", ::demoRetention)
    demo("Un validador declarativo completo", ::demoCompleteExample)
}

/** Ejecuta el capítulo 23 completo. */
fun main() = chapter23.runAll()
