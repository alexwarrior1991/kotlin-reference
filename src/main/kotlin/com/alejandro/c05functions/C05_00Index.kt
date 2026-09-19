package com.alejandro.c05functions

import com.alejandro.infra.chapter

/**
 * # Capítulo 05 · Funciones
 *
 * El capítulo más largo de los fundamentos, porque en Kotlin la función es la unidad
 * básica: no hace falta una clase para tener una, pueden anidarse, recibirse y
 * devolverse.
 *
 * ## Qué se cubre
 * - Declaración, parámetros, `Unit`, cuerpo de expresión y el tipo `Nothing`.
 * - Valores por defecto y argumentos nombrados (y por qué sustituyen a las sobrecargas).
 * - Funciones locales, `infix`, `tailrec` y `vararg`.
 * - Tipos función, lambdas, referencias `::`, funciones que devuelven funciones.
 * - `inline`, `noinline`, `crossinline` y las lambdas con receptor.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Los parámetros son `val`: no se reasignan.
 * - Con cuerpo de bloque hay que declarar el tipo de retorno; con cuerpo de expresión,
 *   se infiere (pero escríbelo en las funciones públicas).
 * - Valores por defecto + argumentos nombrados > cuatro sobrecargas.
 * - Si el último parámetro es una función, la lambda se saca fuera de los paréntesis:
 *   eso es lo que hace que `lista.map { }` parezca sintaxis del lenguaje.
 * - `inline` no es sólo velocidad: habilita el `return` no local y `reified`.
 * - `T.() -> R` (receptor) es la base de `apply`, `buildString` y todos los DSL.
 *
 * ## Errores típicos
 * - `tailrec` sobre una recursión que no está en la última posición.
 * - Pasar una lista a un `vararg` olvidando el `*`.
 * - Usar `it` en lambdas anidadas hasta no saber a qué se refiere.
 *
 * Siguiente paso: capítulo 06, null safety.
 */
val chapter05 = chapter(
    number = 5,
    name = "Funciones",
    summary = "parámetros, defaults, infix, tailrec, vararg, orden superior, inline y receptores",
) {
    demo("Declaración, parámetros y tipo de retorno", ::demoBasicDeclaration)
    demo("Unit: el void que sí es un tipo", ::demoUnit)
    demo("Cuerpo de expresión", ::demoExpressionBody)
    demo("Nothing: lo que nunca devuelve", ::demoNothing)
    demo("Valores por defecto", ::demoDefaultArguments)
    demo("Argumentos nombrados", ::demoNamedArguments)
    demo("Valores por defecto frente a sobrecargas", ::demoDefaultsVsOverloads)
    demo("@JvmOverloads para Java", ::demoJvmOverloads)
    demo("Funciones locales", ::demoLocalFunctions)
    demo("Funciones infijas", ::demoInfixFunctions)
    demo("tailrec: recursión sin desbordar la pila", ::demoTailrec)
    demo("vararg y el operador de propagación *", ::demoVararg)
    demo("Tipos función y typealias", ::demoFunctionTypes)
    demo("Funciones que reciben funciones", ::demoFunctionsAsParameters)
    demo("Funciones que devuelven funciones", ::demoFunctionsAsReturnValues)
    demo("Referencias a funciones con ::", ::demoFunctionReferences)
    demo("Lambda frente a función anónima", ::demoAnonymousFunctions)
    demo("inline: qué hace y qué habilita", ::demoInline)
    demo("noinline", ::demoNoinline)
    demo("crossinline", ::demoCrossinline)
    demo("Lambdas con receptor: T.() -> R", ::demoLambdasWithReceiver)
}

/** Ejecuta el capítulo 05 completo. */
fun main() = chapter05.runAll()
