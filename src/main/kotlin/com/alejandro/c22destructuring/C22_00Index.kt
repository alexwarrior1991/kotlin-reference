package com.alejandro.c22destructuring

import com.alejandro.infra.chapter

/**
 * # Capítulo 22 · Declaraciones de desestructuración
 *
 * `val (a, b) = objeto` reparte un objeto en varias variables llamando a
 * `component1()` y `component2()`. Un capítulo corto con una trampa importante.
 *
 * ## Qué se cubre
 * - Desestructurar data classes, `Pair`, `Triple`, `Map.Entry` y listas.
 * - En bucles (`for ((k, v) in mapa)`), en lambdas y para devolver varios valores.
 * - `componentN` escrito a mano y añadido desde fuera con extensiones.
 * - La trampa posicional y las cinco limitaciones.
 *
 * ## Lo que hay que llevarse sí o sí
 * - La desestructuración es **por posición, no por nombre**. Si alguien reordena dos
 *   propiedades del mismo tipo, tu código **sigue compilando** y asigna al revés.
 *   Es el bug más silencioso de la característica.
 * - Úsala cuando el orden sea parte del concepto (`x/y`, `clave/valor`, `mín/máx`).
 *   Con objetos de dominio de muchos campos, usa las propiedades por nombre.
 * - Sólo entran las propiedades del **constructor primario**, igual que en `equals`.
 * - No hay anidamiento: `val ((a, b), c) = ...` no existe.
 * - Desestructurar una lista es cómodo pero frágil: si tiene menos elementos de los
 *   que pides, lanza en ejecución.
 *
 * ## El consejo práctico
 * Para devolver varios valores, `Pair` sólo si los dos son obvios. En cuanto haya
 * tres, o dos del mismo tipo, usa una data class con nombres: la firma se explica
 * sola y quien llame puede seguir usando `.propiedad`.
 *
 * Siguiente paso: capítulo 23, anotaciones.
 */
val chapter22 = chapter(
    number = 22,
    name = "Desestructuración",
    summary = "val (a, b) = objeto, componentN, bucles, lambdas y la trampa posicional",
) {
    demo("Lo básico", ::demoBasics)
    demo("En bucles", ::demoInLoops)
    demo("En lambdas", ::demoInLambdas)
    demo("Devolver varios valores", ::demoReturningMultipleValues)
    demo("componentN escrito a mano", ::demoManualComponents)
    demo("componentN añadido con extensiones", ::demoExtensionComponents)
    demo("La trampa posicional", ::demoPositionalTrap)
    demo("Las cinco limitaciones", ::demoLimitations)
}

/** Ejecuta el capítulo 22 completo. */
fun main() = chapter22.runAll()
