package com.alejandro.c29dsl

import com.alejandro.infra.chapter

/**
 * # Capítulo 29 · DSLs
 *
 * Un DSL (*domain specific language*) en Kotlin no es una extensión del lenguaje ni
 * un preprocesador: es código Kotlin normal que, gracias a las **lambdas con
 * receptor**, se lee como si fuera otra cosa.
 *
 * ## El mecanismo entero, en dos líneas
 * ```
 * fun correo(bloque: CorreoBuilder.() -> Unit): Correo =
 *     CorreoBuilder().apply(bloque).construir()
 * ```
 * Todo lo demás —`@DslMarker`, la jerarquía de tipos, los valores por defecto— son
 * mejoras sobre ese patrón.
 *
 * ## Qué se cubre
 * - Lambdas con receptor: `T.() -> R` frente a `(T) -> R`, y por qué `apply`,
 *   `buildString` o Gradle son exactamente esto.
 * - El camino de una función con muchos parámetros a un DSL, paso a paso, con la
 *   parada intermedia que resuelve la mayoría de los casos: **argumentos con nombre**.
 * - `@DslMarker`: qué fuga arregla y por qué sin ella un DSL anidado es peligroso.
 * - Un DSL de HTML con seguridad de tipos: `<li>` sólo dentro de `<ul>`, escapado
 *   automático, árbol de nodos en vez de concatenar cadenas.
 * - Un DSL de configuración completo, con bloques opcionales, listas, valores por
 *   defecto y validación.
 * - Cuándo merece la pena un DSL y cuándo es mejor un fichero de configuración.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Antes de escribir un DSL, prueba con argumentos con nombre y valores por
 *   defecto.** Resuelven la mayoría de los casos con cero código extra.
 * - Un DSL gana cuando hay **anidamiento** o **elementos repetidos**.
 * - El builder es mutable; **el resultado es inmutable**. Nunca devuelvas el builder.
 * - **La validación va en `construir()`**, no en cada asignación: así el orden de las
 *   líneas no importa.
 * - **`@DslMarker` en cuanto haya dos niveles.** Sin ella, una errata compila y hace
 *   algo distinto de lo que parece.
 * - La seguridad de tipos no sale sola: viene de tener **una clase por tipo de
 *   bloque**, no una clase genérica para todo.
 *
 * ## El precio
 * Un DSL de configuración obliga a recompilar para cambiar un valor. Si la
 * configuración la edita otra persona o cambia por entorno, el fichero externo sigue
 * siendo la respuesta correcta.
 *
 * Siguiente paso: capítulo 30, testing con kotlin.test.
 */
val chapter29 = chapter(
    number = 29,
    name = "DSLs",
    summary = "Lambdas con receptor, builders, @DslMarker, DSL de HTML y de configuración",
) {
    demo("Lambdas con receptor: el mecanismo", ::demoLambdaWithReceiver)
    demo("De una función normal a un DSL, paso a paso", ::demoFromFunctionToBuilder)
    demo("Las reglas de un builder decente", ::demoBuilderRules)
    demo("La fuga de receptores en un DSL anidado", ::demoScopeLeak)
    demo("@DslMarker: cerrar los ámbitos", ::demoDslMarker)
    demo("Un DSL de HTML con seguridad de tipos", ::demoHtmlDsl)
    demo("Cómo está hecho el DSL de HTML por dentro", ::demoHtmlDslInside)
    demo("Un DSL de configuración completo", ::demoConfigDsl)
    demo("Cuándo merece la pena un DSL", ::demoWhenToUseDsl)
}

/** Ejecuta el capítulo 29 completo. */
fun main() = chapter29.runAll()
