package com.alejandro.c12generics

import com.alejandro.infra.chapter

/**
 * # Capítulo 12 · Genéricos
 *
 * El capítulo más difícil de los fundamentos, y el que más veces habrá que releer.
 * Merece la pena: es lo que sostiene toda la biblioteca de colecciones y lo que hace
 * que los errores de tipos aparezcan al compilar y no en producción.
 *
 * ## Qué se cubre
 * - Clases y funciones genéricas, y cuándo hay que escribir el tipo.
 * - Restricciones: un límite superior, y varios con `where`.
 * - **Varianza**: invariancia por defecto, `out` (produce), `in` (consume),
 *   proyecciones en el punto de uso y la proyección estrella `*`.
 * - Borrado de tipos y `reified`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - El límite por defecto de `<T>` es `Any?`: acepta nulos. Para excluirlos, `<T : Any>`.
 * - Dos o más restricciones sobre el mismo `T` exigen `where`, no una coma.
 * - **La regla de la varianza**: si el tipo sólo PRODUCE `T`, márcalo `out`; si sólo
 *   lo CONSUME, `in`; si hace las dos cosas, se queda invariante. Por eso `List<out E>`
 *   es covariante y `MutableList<E>` no.
 * - En la JVM los parámetros de tipo **se borran**: `x is List<String>` no compila,
 *   y un `as List<Int>` no comprueba nada (por eso el aviso "Unchecked cast").
 * - `reified` recupera el tipo, pero **sólo funciona con `inline`** y nunca en clases.
 *
 * ## Errores típicos
 * - Marcar `out` una clase que también recibe `T` como parámetro.
 * - Fiarse de un cast genérico: el fallo aparecerá mucho después y lejos de la causa.
 * - Restringir de más y dejar fuera casos de uso legítimos.
 *
 * Siguiente paso: capítulo 13, colecciones (donde todo esto se usa constantemente).
 */
val chapter12 = chapter(
    number = 12,
    name = "Genéricos",
    summary = "clases y funciones genéricas, restricciones, varianza, reified y erasure",
) {
    demo("Por qué existen los genéricos", ::demoWhyGenerics)
    demo("Clases genéricas", ::demoGenericClasses)
    demo("Funciones genéricas", ::demoGenericFunctions)
    demo("Sin restricción, T es Any?", ::demoNoConstraint)
    demo("Límite superior: <T : Algo>", ::demoUpperBound)
    demo("Varias restricciones con where", ::demoWhereClause)
    demo("Restricciones en clases genéricas", ::demoConstrainedClasses)
    demo("Invariancia: el valor por defecto", ::demoInvariance)
    demo("out: covarianza para los que producen", ::demoCovariance)
    demo("in: contravarianza para los que consumen", ::demoContravariance)
    demo("Proyecciones y la estrella *", ::demoUseSiteVariance)
    demo("Borrado de tipos", ::demoTypeErasure)
    demo("reified: recuperar el tipo", ::demoReified)
    demo("Los límites de reified", ::demoReifiedLimits)
    demo("reified en la práctica", ::demoReifiedInPractice)
}

/** Ejecuta el capítulo 12 completo. */
fun main() = chapter12.runAll()
