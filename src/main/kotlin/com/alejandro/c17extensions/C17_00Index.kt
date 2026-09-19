package com.alejandro.c17extensions

import com.alejandro.infra.chapter

/**
 * # Capítulo 17 · Funciones y propiedades de extensión
 *
 * Añadir comportamiento a un tipo que no controlas, sin heredar ni modificarlo. Toda
 * la biblioteca de colecciones de Kotlin está construida así: `map`, `filter` y
 * `first` no están dentro de `java.util.List`, se le añaden desde fuera.
 *
 * ## Qué se cubre
 * - Funciones de extensión, y qué son en realidad (funciones estáticas con el
 *   receptor como primer parámetro).
 * - **Resolución estática**: el ejemplo que sorprende a todo el mundo.
 * - El miembro siempre gana sobre la extensión.
 * - Dónde declararlas, extensiones dentro de una clase (dos receptores) y visibilidad.
 * - Propiedades de extensión y por qué no pueden tener backing field.
 * - Receptores nulables (`fun String?.foo()`), que es cómo funciona `isNullOrEmpty`.
 * - Los cinco límites, y una pequeña biblioteca de extensiones útiles.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Una extensión **no es polimórfica**: se elige por el tipo DECLARADO de la
 *   variable, no por el tipo real del objeto.
 * - Si existe un miembro con la misma firma, **gana el miembro**, sin aviso. Si la
 *   librería lo añade mañana, tu extensión deja de usarse en silencio.
 * - Una extensión **no ve los miembros privados** de su receptor.
 * - Una propiedad de extensión **no puede tener valor inicial**: siempre `get()`.
 * - Receptor nulable si null tiene un resultado razonable (`orEmpty()`); si no, deja
 *   que quien llame escriba el `?.`, que se ve.
 *
 * ## Errores típicos
 * - Esperar polimorfismo de una extensión.
 * - Declarar el receptor nulable "por si acaso".
 * - Llenar el autocompletado de `String` con extensiones que usa un solo fichero.
 *
 * Siguiente paso: capítulo 18, delegación.
 */
val chapter17 = chapter(
    number = 17,
    name = "Extensiones",
    summary = "funciones y propiedades de extensión, resolución estática, receptores nulables",
) {
    demo("Funciones de extensión", ::demoBasics)
    demo("La resolución es estática", ::demoStaticResolution)
    demo("El miembro siempre gana", ::demoMemberWins)
    demo("Dónde declararlas y cómo importarlas", ::demoScopeAndImports)
    demo("Propiedades de extensión", ::demoExtensionProperties)
    demo("¿Propiedad o función?", ::demoPropertyOrFunction)
    demo("Propiedades de extensión mutables", ::demoMutableExtensionProperties)
    demo("Extensiones sobre el companion object", ::demoCompanionExtensions)
    demo("Receptores nulables", ::demoNullableReceivers)
    demo("Los cinco límites", ::demoLimitations)
    demo("Una biblioteca de extensiones útiles", ::demoUsefulExtensions)
}

/** Ejecuta el capítulo 17 completo. */
fun main() = chapter17.runAll()
