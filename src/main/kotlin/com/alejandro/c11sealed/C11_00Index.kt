package com.alejandro.c11sealed

import com.alejandro.infra.chapter

/**
 * # Capítulo 11 · Sealed classes e interfaces
 *
 * Una jerarquía **cerrada**: el compilador conoce todas las subclases posibles. Es un
 * enum en el que cada caso puede llevar datos distintos, y es la herramienta con la
 * que se modelan resultados, errores y estados en Kotlin.
 *
 * ## Qué se cubre
 * - `sealed class` frente a `sealed interface`, y las reglas del "sellado".
 * - Modelar resultados: `Resultado<out T>` con `Exito` y `Error : Resultado<Nothing>`.
 * - Errores estructurados: el motivo del fallo también es un tipo, con sus datos.
 * - Encadenar con `map`, `flatMap`, `getOrElse` y `fold` escritos a mano.
 * - Comparación con excepciones, con `null` y con `kotlin.Result`.
 * - Estados de pantalla, máquinas de estados y la tabla sealed frente a enum.
 *
 * ## Lo que hay que llevarse sí o sí
 * - La pregunta que decide: **¿cada caso necesita datos propios?** No → enum.
 *   Sí → sealed.
 * - Modelar el estado con una sealed hace **imposibles** los estados inválidos: se
 *   acabó lo de "cargando y con error y vacío a la vez".
 * - `Error : Resultado<Nothing>` funciona porque `Nothing` es subtipo de todo y el
 *   tipo es covariante (`out T`). Eso se estudia en el capítulo 12.
 * - No pongas `else` en un `when` sobre una sealed: perderías la única razón por la
 *   que la has usado.
 * - Una excepción no aparece en la firma; un `Resultado<T>` sí.
 *
 * ## Errores típicos
 * - Modelar estado con tres booleanos independientes (32 combinaciones, 4 válidas).
 * - Usar `String` como motivo de error y acabar comparando cadenas.
 * - Hacer `as Exito` para "atajar" y tirar por tierra todas las garantías.
 *
 * Siguiente paso: capítulo 12, genéricos.
 */
val chapter11 = chapter(
    number = 11,
    name = "Sealed classes",
    summary = "jerarquías cerradas, resultados, errores estructurados y estados",
) {
    demo("sealed class y sealed interface", ::demoSealedBasics)
    demo("Cuál elegir: class o interface", ::demoSealedClassVsInterface)
    demo("Las reglas del sellado", ::demoSealedRules)
    demo("Modelar un resultado: Exito o Error", ::demoResultBasics)
    demo("Errores estructurados con sus propios datos", ::demoStructuredErrors)
    demo("Encadenar con map, flatMap y fold", ::demoChaining)
    demo("Frente a excepciones, null y kotlin.Result", ::demoVsAlternatives)
    demo("El antipatrón de las banderas booleanas", ::demoFlagsAntipattern)
    demo("Estado de pantalla bien modelado", ::demoUiState)
    demo("Una máquina de estados", ::demoStateMachine)
    demo("La tabla: sealed frente a enum", ::demoSealedVsEnum)
}

/** Ejecuta el capítulo 11 completo. */
fun main() = chapter11.runAll()
