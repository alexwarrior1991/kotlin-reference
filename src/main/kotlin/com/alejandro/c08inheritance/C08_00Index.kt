package com.alejandro.c08inheritance

import com.alejandro.infra.chapter

/**
 * # Capítulo 08 · Herencia e interfaces
 *
 * Cómo se relacionan los tipos entre sí. La decisión de partida de Kotlin —todo es
 * `final` salvo que digas lo contrario— convierte en regla del compilador el consejo
 * clásico: "diseña para la herencia o prohíbela".
 *
 * ## Qué se cubre
 * - `open`, `override`, `final override` y `super`.
 * - La trampa de llamar a un método `open` desde un constructor.
 * - Clases abstractas e interfaces (que en Kotlin también llevan código).
 * - Resolución de conflictos con `super<Interfaz>`.
 * - Composición frente a herencia, con el ejemplo clásico del contador roto.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `override` es obligatorio, no una anotación opcional.
 * - Un método sobrescrito sigue siendo `open`: para cerrarlo hace falta
 *   `final override`.
 * - Una interfaz no puede guardar estado; una clase abstracta sí. Ésa es la
 *   diferencia real entre las dos.
 * - Heredar te acopla a los DETALLES INTERNOS del padre, no sólo a su API.
 * - `by` hace que la composición sea más corta que la herencia, así que ya no hay
 *   excusa para heredar "porque escribo menos".
 *
 * ## Errores típicos
 * - Abrir clases "por si acaso".
 * - Llamar a métodos `open` desde el constructor.
 * - Poner un valor inicial a una propiedad de interfaz.
 *
 * Siguiente paso: capítulo 09, data classes.
 */
val chapter08 = chapter(
    number = 8,
    name = "Herencia e interfaces",
    summary = "open/override/super, abstractas, interfaces, conflictos y composición",
) {
    demo("final por defecto, open para heredar", ::demoOpenAndFinal)
    demo("override es obligatorio", ::demoOverride)
    demo("super y la trampa del constructor", ::demoSuper)
    demo("Clases abstractas", ::demoAbstractClasses)
    demo("Interfaces con implementación y propiedades", ::demoInterfaces)
    demo("Conflictos entre interfaces: super<T>", ::demoInterfaceConflicts)
    demo("Clase abstracta o interfaz: cómo elegir", ::demoAbstractVsInterface)
    demo("El contador roto: por qué la herencia es frágil", ::demoFragileBaseClass)
    demo("Delegación de clase con `by`", ::demoDelegation)
    demo("Cuándo heredar y cuándo componer", ::demoWhenToInherit)
}

/** Ejecuta el capítulo 08 completo. */
fun main() = chapter08.runAll()
