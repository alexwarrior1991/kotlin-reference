package com.alejandro.c24reflection

import com.alejandro.infra.chapter

/**
 * # Capítulo 24 · Reflexión
 *
 * Inspeccionar el programa desde dentro: qué clase es esto, qué propiedades tiene,
 * qué anotaciones lleva. Es cómo funcionan por dentro Jackson, Spring y JUnit.
 *
 * ## Qué se cubre
 * - `::class` sobre valores y sobre tipos; `KClass` frente a `Class` de Java.
 * - Referencias a funciones, a constructores y a propiedades (incluidas las ligadas).
 * - `memberProperties`, `primaryConstructor`, `createInstance`, `callBy`.
 * - Un mini-serializador completo que respeta una anotación `@NoSerializar`.
 * - Los cuatro límites y las alternativas, por orden de preferencia.
 *
 * ## La división que casi nadie conoce
 * ```
 * SIN kotlin-reflect          CON kotlin-reflect
 * ::class                     memberProperties
 * simpleName, qualifiedName   primaryConstructor
 * isInstance                  createInstance, callBy
 * ::funcion, Clase::propiedad findAnnotation
 * referencia.name             isData, isAbstract, parameters
 * ```
 * Si falta la dependencia, lo de la derecha lanza `KotlinReflectionNotSupportedError`
 * **en ejecución**. El compilador no avisa.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `::class` sobre un VALOR da su clase real; sobre un TIPO, la declarada.
 * - `.java` y `.kotlin` convierten entre `Class` y `KClass`.
 * - Una referencia a propiedad aporta el valor **y el nombre**: por eso permite
 *   mensajes de error genéricos que se actualizan solos al renombrar.
 * - La reflexión es lenta y desactiva las garantías del compilador.
 *
 * ## Cuándo es la respuesta correcta
 * Serializadores, inyección de dependencias, frameworks de test, herramientas de
 * desarrollo. Fíjate en el patrón: **librerías que no conocen tus clases**. En código
 * de aplicación casi siempre hay algo mejor: una interfaz, una `sealed`, o generación
 * de código al compilar.
 *
 * Siguiente paso: capítulo 25, biblioteca estándar.
 */
val chapter24 = chapter(
    number = 24,
    name = "Reflexión",
    summary = "KClass, referencias, memberProperties, un mini-serializador y sus límites",
) {
    demo("KClass y ::class", ::demoKClass)
    demo("Referencias a funciones y constructores", ::demoFunctionReferences)
    demo("Referencias a propiedades", ::demoPropertyReferences)
    demo("Recorrer las propiedades de una clase", ::demoMemberProperties)
    demo("Leer y usar el constructor", ::demoConstructor)
    demo("Un mini-serializador", ::demoMiniSerializer)
    demo("Límites y alternativas", ::demoLimitsAndAlternatives)
}

/** Ejecuta el capítulo 24 completo. */
fun main() = chapter24.runAll()
