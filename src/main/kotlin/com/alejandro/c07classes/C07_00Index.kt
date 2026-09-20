package com.alejandro.c07classes

import com.alejandro.infra.chapter

/**
 * # Capítulo 07 · Clases y objetos
 *
 * Cómo se declara una clase, cómo se inicializa y cómo se controla lo que se ve desde
 * fuera. Es donde más código se ahorra respecto a Java: el constructor primario declara
 * parámetros y propiedades en la misma línea.
 *
 * ## Qué se cubre
 * - Constructor primario, bloques `init`, orden de inicialización y constructores
 *   secundarios (y por qué muchas veces sobran).
 * - Propiedades: getters y setters personalizados, `field`, propiedades calculadas y
 *   el patrón `private set`.
 * - Visibilidad: `public`, `private`, `protected` e `internal`.
 * - `companion object`, `object` como singleton y objetos anónimos.
 * - Clases anidadas frente a `inner`, y clases locales.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Sin `val`/`var` en el constructor primario, el parámetro NO se guarda.
 * - Los inicializadores de propiedad y los bloques `init` se ejecutan intercalados,
 *   en orden de escritura, y siempre antes del cuerpo de un constructor secundario.
 * - `private` a nivel superior significa **privado del fichero**, no de la clase.
 * - Kotlin no tiene visibilidad de paquete; sí tiene `internal` (por módulo).
 * - Una clase anidada NO ve la externa: para eso hay que marcarla `inner`. Es lo
 *   contrario que en Java, y evita fugas de memoria.
 * - `object X { }` es siempre el mismo objeto; `object : T { }` crea uno nuevo cada vez.
 *
 * ## Errores típicos
 * - Usar el nombre de una propiedad dentro de su propio getter (recursión infinita).
 * - Guardar estado mutable en un `object`: es una variable global.
 * - Marcar `inner` sin necesitarlo.
 *
 * Siguiente paso: capítulo 08, herencia e interfaces.
 */
val chapter07 = chapter(
    number = 7,
    name = "Clases y objetos",
    summary = "constructores, init, propiedades, visibilidad, companion, object, inner",
) {
    demo("Constructor primario", ::demoPrimaryConstructor)
    demo("Bloques init y orden de inicialización", ::demoInitBlocks)
    demo("Constructores secundarios y fábricas", ::demoSecondaryConstructors)
    demo("Propiedades, no campos", ::demoProperties)
    demo("Propiedades calculadas", ::demoComputedProperties)
    demo("Setters personalizados y `field`", ::demoCustomSetters)
    demo("Propiedades de nivel superior", ::demoTopLevelProperties)
    demo("Los cuatro modificadores de visibilidad", ::demoVisibilityModifiers)
    demo("Visibilidad a nivel superior", ::demoTopLevelVisibility)
    demo("Visibilidad de los miembros", ::demoMemberVisibility)
    demo("Cómo elegir la visibilidad", ::demoVisibilityGuidelines)
    demo("companion object", ::demoCompanionObject)
    demo("object: el singleton", ::demoObjectDeclaration)
    demo("Objetos anónimos", ::demoObjectExpression)
    demo("Clases anidadas", ::demoNestedClass)
    demo("Clases inner", ::demoInnerClass)
    demo("Clases locales", ::demoLocalClass)
}

/** Ejecuta el capítulo 07 completo. */
fun main() = chapter07.runAll()
