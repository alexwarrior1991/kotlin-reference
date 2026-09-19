package com.alejandro.c09dataclasses

import com.alejandro.infra.chapter

/**
 * # Capítulo 09 · Data classes
 *
 * La construcción que más código elimina de todo Kotlin: `equals`, `hashCode`,
 * `toString`, `copy` y `componentN` escritos por el compilador y siempre sincronizados
 * con las propiedades.
 *
 * ## Qué se cubre
 * - Qué genera exactamente una `data class` y qué no.
 * - Por qué sólo cuentan las propiedades del constructor primario.
 * - Requisitos, limitaciones y el problema de meter un array dentro.
 * - `copy()`, el patrón de estado inmutable y la trampa de la copia superficial.
 * - `data object` y su sitio natural dentro de una jerarquía `sealed`.
 * - Cuándo NO usar data class (entidades con identidad).
 * - `value class` como alternativa sin coste para envolver un solo valor.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Sólo las propiedades **del constructor primario** entran en equals/hashCode/
 *   toString/copy. Las del cuerpo, no.
 * - `copy()` es **superficial**: las referencias se comparten. Con un `MutableList`
 *   dentro, modificar la copia modifica el original.
 * - Regla de oro: dentro de una data class, todo `val` y todo inmutable.
 * - ¿Dos objetos con los mismos datos son el mismo? Sí → data class. No (tienen id)
 *   → clase normal con equals por identidad.
 * - `typealias` NO da seguridad de tipos; `value class` sí, y sin coste.
 *
 * ## Errores típicos
 * - Poner un array en una data class y esperar que `equals` compare el contenido.
 * - Usar data class para entidades de base de datos.
 * - Dejar la contraseña en el `toString` generado y que acabe en los logs.
 *
 * Siguiente paso: capítulo 10, enum classes.
 */
val chapter09 = chapter(
    number = 9,
    name = "Data classes",
    summary = "equals/hashCode/toString/copy generados, data object y value class",
) {
    demo("Qué genera el compilador", ::demoWhatDataClassGenerates)
    demo("Sólo cuenta el constructor primario", ::demoOnlyPrimaryConstructorCounts)
    demo("Requisitos y limitaciones", ::demoRequirementsAndLimits)
    demo("Sobrescribir lo generado", ::demoOverridingGenerated)
    demo("copy() y el estado inmutable", ::demoCopy)
    demo("copy() es superficial: la trampa", ::demoShallowCopy)
    demo("data object", ::demoDataObject)
    demo("Cuándo usar (y no usar) data class", ::demoWhenToUseDataClass)
    demo("Obsesión por los primitivos", ::demoPrimitiveObsession)
    demo("value class frente a typealias y data class", ::demoValueClassVsAlternatives)
    demo("Qué puede llevar dentro un value class", ::demoValueClassCapabilities)
    demo("Cuándo se envuelve el valor (boxing)", ::demoBoxing)
}

/** Ejecuta el capítulo 09 completo. */
fun main() = chapter09.runAll()
