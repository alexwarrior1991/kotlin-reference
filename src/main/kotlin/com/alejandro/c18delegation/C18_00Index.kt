package com.alejandro.c18delegation

import com.alejandro.infra.chapter

/**
 * # Capítulo 18 · Delegación
 *
 * Dos mecanismos que comparten la palabra clave `by` y poco más: delegar la
 * implementación de una **interfaz** en otro objeto, y delegar el get/set de una
 * **propiedad** en otro objeto.
 *
 * ## Qué se cubre
 * - Delegación de clases: `class A(val b: B) : I by b`, y la trampa de que la clase
 *   delegada no ve tus overrides.
 * - `by lazy`: modos de sincronización, casos de uso y comparación con getter,
 *   `lateinit` e inicialización directa.
 * - `Delegates.observable`, `Delegates.vetoable`, `Delegates.notNull` y `by map`.
 * - Delegados propios con `ReadOnlyProperty` / `ReadWriteProperty`, y `provideDelegate`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `by` hace que **componer sea más corto que heredar**: es lo que quita la excusa
 *   para usar herencia "porque escribo menos".
 * - La delegación es **reenvío directo**: si sobrescribes `add`, el `addAll` delegado
 *   NO pasará por él. Es el espejo del contador roto del capítulo 8.
 * - `lazy` **no reevalúa nunca**. Si el valor depende de algo que cambia, usa un
 *   getter, no `lazy`.
 * - `observable` avisa **después**; `vetoable` decide **antes**.
 * - `Delegates.notNull()` es el `lateinit` de los tipos primitivos.
 * - `by map` falla al **leer**, no al construir: para datos de fuera es tarde.
 *   `provideDelegate` permite fallar al construir.
 *
 * ## Errores típicos
 * - Escribir un delegado propio donde un setter personalizado bastaba.
 * - Usar un `object` singleton como delegado y compartir estado sin querer.
 * - `lazy(NONE)` sin estar seguro de que sólo hay un hilo.
 *
 * Siguiente paso: capítulo 19, excepciones.
 */
val chapter18 = chapter(
    number = 18,
    name = "Delegación",
    summary = "by para clases, lazy, observable, vetoable, notNull, by map y delegados propios",
) {
    demo("Delegación de clases con `by`", ::demoBasicDelegation)
    demo("La trampa: la delegada no ve tus overrides", ::demoOverridingDelegated)
    demo("Delegar en varias interfaces", ::demoMultipleDelegation)
    demo("Cuándo delegar y cuándo no", ::demoWhenToDelegate)
    demo("by lazy: lo básico", ::demoLazyBasics)
    demo("Para qué sirve lazy de verdad", ::demoLazyUseCases)
    demo("Los tres modos de sincronización", ::demoLazyModes)
    demo("lazy frente a getter, lateinit e inicialización directa", ::demoLazyVsAlternatives)
    demo("Delegates.observable", ::demoObservable)
    demo("Delegates.vetoable", ::demoVetoable)
    demo("Delegates.notNull", ::demoNotNull)
    demo("Propiedades respaldadas por un Map", ::demoByMap)
    demo("El contrato de un delegado", ::demoTheContract)
    demo("Un delegado con validación", ::demoValidatingDelegate)
    demo("Un delegado con estado", ::demoStatefulDelegate)
    demo("Un delegado con caché", ::demoCachingDelegate)
    demo("provideDelegate: comprobar al construir", ::demoProvideDelegate)
    demo("Cuándo escribir un delegado propio", ::demoWhenToWriteOne)
}

/** Ejecuta el capítulo 18 completo. */
fun main() = chapter18.runAll()
