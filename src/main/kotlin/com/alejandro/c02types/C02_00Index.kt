package com.alejandro.c02types

import com.alejandro.infra.chapter

/**
 * # Capítulo 02 · Variables y tipos básicos
 *
 * El vocabulario mínimo: cómo se declara algo y de qué tipos dispones.
 *
 * ## Qué se cubre
 * - `val`, `var` y `const val`, y por qué `val` es la opción por defecto.
 * - Los seis tipos numéricos, sus literales y el desbordamiento silencioso.
 * - `String`, plantillas de cadena, cadenas en crudo y `Char`.
 * - `Array`, rangos (`1..10`, `1..<10`) y progresiones (`step`, `downTo`).
 *
 * ## Lo que hay que llevarse sí o sí
 * - `val` congela la referencia, **no** el objeto: un `val` con una lista mutable
 *   dentro sigue pudiendo crecer.
 * - **No hay conversiones numéricas implícitas**: `Int` → `Long` se escribe a mano.
 * - `7 / 2` es `3`. La división entre enteros es entera.
 * - `==` compara contenido en Kotlin (al revés que en Java); `===` compara identidad.
 * - Para arrays, `==` no vale: usa `contentEquals`.
 *
 * ## Errores típicos
 * - Usar `Double` para dinero.
 * - Concatenar cadenas dentro de un bucle en lugar de usar `buildString`.
 * - Esperar que `10..1` recorra hacia atrás (está vacío; se usa `downTo`).
 *
 * Siguiente paso: capítulo 03, operadores.
 */
val chapter02 = chapter(
    number = 2,
    name = "Variables y tipos",
    summary = "val/var/const, numéricos, String, Char, arrays, rangos y progresiones",
) {
    demo("val vs var: la decisión más frecuente", ::demoValVsVar)
    demo("val NO significa inmutable", ::demoValIsNotImmutable)
    demo("const val: constantes de compilación", ::demoConstVal)
    demo("Tipo explícito y sombreado de nombres", ::demoTypesAndShadowing)
    demo("Cuándo `var` es la respuesta correcta", ::demoWhenVarIsFine)
    demo("Tipos enteros y sus rangos", ::demoIntegerTypes)
    demo("Literales: bases, guiones bajos y decimales", ::demoNumericLiterals)
    demo("Sin conversiones implícitas: la familia toXxx()", ::demoNoImplicitConversions)
    demo("Aritmética: división entera, módulo y desbordamiento", ::demoArithmetic)
    demo("Coma flotante, NaN y por qué el dinero no va en Double", ::demoFloatingPoint)
    demo("Tipos sin signo: UInt y compañía", ::demoUnsignedTypes)
    demo("Plantillas de cadena", ::demoStringTemplates)
    demo("Cadenas en crudo, trimIndent y trimMargin", ::demoRawStrings)
    demo("Igualdad de cadenas: == frente a ===", ::demoStringEquality)
    demo("Operaciones de cadena del día a día", ::demoStringOperations)
    demo("El tipo Char", ::demoChars)
    demo("Arrays: cuándo sí y cuándo no", ::demoArrays)
    demo("Rangos: .. y ..<", ::demoRanges)
    demo("Progresiones: step, downTo y reversed", ::demoProgressions)
}

/** Ejecuta el capítulo 02 completo. */
fun main() = chapter02.runAll()
