package com.alejandro.c01basics

import com.alejandro.infra.chapter

/**
 * # Capítulo 01 · Introducción y sintaxis básica
 *
 * El punto de partida. Aquí no se aprende a programar, se aprende **cómo se escribe
 * Kotlin**: dónde va el código, qué devuelve cada cosa y qué se puede omitir.
 *
 * ## Qué se cubre
 * - `fun main()`, `println`, los tres tipos de comentario y el punto y coma opcional.
 * - Expresiones frente a sentencias: por qué `if`, `when` y `try` devuelven un valor
 *   y por qué Kotlin no necesita operador ternario.
 * - Inferencia de tipos: qué deduce el compilador y cuándo conviene escribir el tipo.
 * - Paquetes, imports, alias de import e imports por defecto.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `val` por defecto; `var` sólo cuando haga falta.
 * - Las plantillas de cadena (`"Hola, $nombre"`) sustituyen a la concatenación.
 * - Casi todo es una expresión; la asignación, no.
 * - `1.0` es `Double`, no `Float`.
 *
 * ## Errores típicos de quien empieza
 * - Escribir una clase para poder meter `main` dentro.
 * - Poner `;` al final de cada línea.
 * - Suponer que Kotlin convierte números entre sí automáticamente (no lo hace).
 *
 * Siguiente paso: capítulo 02, variables y tipos básicos.
 */
val chapter01 = chapter(
    number = 1,
    name = "Sintaxis básica",
    summary = "main, println, comentarios, expresiones vs sentencias, inferencia, paquetes e imports",
) {
    demo("Hola mundo: el ejemplo original, explicado", ::demoHelloWorld)
    demo("println, print y caracteres especiales", ::demoPrintln)
    demo("Comentarios: línea, bloque anidable y KDoc", ::demoComments)
    demo("El punto y coma opcional (y cuándo no lo es)", ::demoSemicolons)
    demo("Expresiones vs sentencias, y el tipo Unit", ::demoExpressionsVsStatements)
    demo("Inferencia de tipos: qué deduce el compilador", ::demoTypeInference)
    demo("Convenciones de nombrado", ::demoNamingConventions)
    demo("Paquetes, carpetas y clases fachada", ::demoPackages)
    demo("Imports: con nombre, cualificados y con asterisco", ::demoImports)
    demo("Alias de import con `as`", ::demoImportAliases)
    demo("Imports por defecto: lo que nunca escribes", ::demoDefaultImports)
}

/**
 * Ejecuta el capítulo 01 completo.
 *
 * Pulsa ▶ aquí en IntelliJ para ver todas las demos del capítulo seguidas.
 */
fun main() = chapter01.runAll()
