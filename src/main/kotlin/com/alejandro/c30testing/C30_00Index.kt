package com.alejandro.c30testing

import com.alejandro.infra.chapter

/**
 * # Capítulo 30 · Testing
 *
 * Testear no es "escribir tests después": es **diseñar el código de forma que
 * probarlo sea trivial**. Por eso el capítulo empieza por el diseño y sólo después
 * llega a `assertEquals`.
 *
 * ## Las dependencias
 * En `build.gradle.kts`:
 * ```
 * testImplementation(kotlin("test"))
 * testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
 *
 * tasks.test { useJUnitPlatform() }
 * ```
 * `kotlin.test` es una fachada sobre JUnit: escribes `@Test` y `assertEquals` sin
 * importar nada de JUnit, y funciona igual en Kotlin/JVM, Native y JS.
 *
 * ## Qué se cubre
 * - Funciones puras y por qué son gratis de testear.
 * - Inyectar el reloj, el azar, el dispatcher y el exterior para poder controlarlos.
 * - Qué merece la pena testear y qué no; la pirámide; por qué la cobertura no es la nota.
 * - Anatomía de un test: dónde van los ficheros, AAA, nombres entre acentos graves,
 *   `@BeforeTest`/`@AfterTest`, cómo ejecutarlos.
 * - El catálogo de asertos de `kotlin.test`, incluido `assertFailsWith`, la
 *   tolerancia en los `Double` y `assertContentEquals` para arrays.
 * - Dobles de test: dummy, stub, fake, mock y spy, y por qué en Kotlin casi siempre
 *   basta con un fake escrito a mano.
 * - Corrutinas con `runTest` y **tiempo virtual**: un test que simula una hora de
 *   reintentos tarda milisegundos.
 * - Los tests de este propio repositorio, incluido el de humo que ejecuta TODAS las demos.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Si un test es difícil de escribir, el problema está en el código**, no en el test.
 * - Nunca llames a `System.currentTimeMillis()`, `Random()` o `Dispatchers.IO` dentro
 *   de la lógica: **inyéctalos**, con un valor por defecto para producción.
 * - **`assertEquals(esperado, real)`**: el esperado va primero, y el mensaje de fallo
 *   depende de ello.
 * - Un test prueba **un comportamiento**, no una función: `aplicarDescuento` tiene cuatro.
 * - En corrutinas, **`runTest`, nunca `runBlocking`**, y jamás un `delay` "para dar tiempo".
 * - Un **fake** de diez líneas se lee mejor y envejece mejor que un mock.
 *
 * ## Los tests de verdad
 * Todo el código de producción de este capítulo está probado en
 * `src/test/kotlin/com/alejandro/c30testing/`. Ejecútalos con:
 * ```
 * ./gradlew test
 * ./gradlew test --tests '*PreciosTest'
 * ```
 *
 * Siguiente paso: capítulo 31, los ejercicios.
 */
val chapter30 = chapter(
    number = 30,
    name = "Testing",
    summary = "Código testeable, kotlin.test, dobles de test y corrutinas con tiempo virtual",
) {
    demo("Funciones puras: lo que se testea solo", ::demoPureFunctions)
    demo("Inyectar el reloj y las demás dependencias", ::demoInjectDependencies)
    demo("Qué merece la pena testear", ::demoWhatToTest)
    demo("Anatomía de un test", ::demoAnatomyOfATest)
    demo("El catálogo de asertos", ::demoAssertions)
    demo("Dobles de test: dummy, stub, fake, mock, spy", ::demoTestDoubles)
    demo("runTest y el tiempo virtual", ::demoRunTest)
    demo("Los tests de este repositorio", ::demoTestingThisRepo)
}

/** Ejecuta el capítulo 30 completo. */
fun main() = chapter30.runAll()
