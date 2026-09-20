package com.alejandro.c28coroutines

import com.alejandro.infra.chapter

/**
 * # Capítulo 28 · Corrutinas
 *
 * El capítulo más largo del repositorio, y con razón: las corrutinas son la forma en
 * que Kotlin hace concurrencia, y traen consigo un modelo mental propio que no se
 * parece al de los hilos ni al de las promesas de otros lenguajes.
 *
 * ## La dependencia
 * `suspend` es parte del lenguaje, pero `launch`, `async`, `Flow` y todo lo demás
 * viven en una librería aparte. En `build.gradle.kts`:
 * ```
 * implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
 * ```
 * Y para testearlas (capítulo 30):
 * ```
 * testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
 * ```
 *
 * ## Qué se cubre
 * - Qué es una corrutina, `suspend`, `runBlocking` y por qué `delay` no es `sleep`.
 * - `launch` y `async`, `Job` y `Deferred`, secuencial frente a paralelo **medido**.
 * - Concurrencia estructurada: `coroutineScope`, `supervisorScope`, ámbitos propios.
 * - Cancelación cooperativa, `isActive`/`ensureActive()`/`yield()`, `NonCancellable`.
 * - `withTimeout` y `withTimeoutOrNull`.
 * - `CoroutineContext`, dispatchers, `withContext` y el caso de `Dispatchers.Main`.
 * - Excepciones: quién las recibe, `CoroutineExceptionHandler`, `SupervisorJob`.
 * - `Flow` frío: builders, operadores intermedios y terminales, errores, contrapresión.
 * - `StateFlow`, `SharedFlow` y `Channel`: los flujos calientes y las colas.
 * - Antipatrones, reglas de diseño y lista de comprobación.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Concurrencia estructurada**: toda corrutina tiene un padre, y el padre no
 *   termina hasta que ella termina. Si no sabes quién cancela una corrutina, el
 *   diseño no está acabado.
 * - **La cancelación es cooperativa**: un bucle sin puntos de suspensión ignora un
 *   `cancel()` por completo.
 * - **`catch (e: Exception)` y `runCatching` se tragan la `CancellationException`**
 *   y rompen la cancelación en silencio.
 * - **`launch` falla hacia arriba; `async` guarda el fallo hasta el `await()`.**
 * - **Un Flow frío es una receta**: sin operador terminal no se ejecuta nada, y cada
 *   colector lo ejecuta entero por su cuenta.
 * - **Estado → StateFlow. Eventos → SharedFlow. Trabajo a repartir → Channel.**
 * - El dispatcher decide el hilo; el `Job` decide la vida. El segundo importa más.
 *
 * ## Cómo estudiarlo
 * Son 43 demos: no intentes con todas de una sentada. Tres tandas naturales:
 * 1. **Lo imprescindible** (28.1 – 28.20): fundamentos, launch/async, ámbitos,
 *    cancelación y timeouts. Con esto ya se puede escribir código correcto.
 * 2. **Contexto y errores** (28.21 – 28.28): dispatchers y propagación de fallos.
 *    Es la parte donde más gente se equivoca en producción.
 * 3. **Flow** (28.29 – 28.43): flujos fríos, operadores, flujos calientes y el
 *    resumen de buenas prácticas.
 *
 * Ninguna demo se cuelga ni tarda más de unas décimas de segundo: todas usan
 * `delay` con tiempos cortos y acotan los flujos infinitos.
 *
 * Siguiente paso: capítulo 29, DSLs con lambdas con receptor.
 */
val chapter28 = chapter(
    number = 28,
    name = "Corrutinas",
    summary = "suspend, launch/async, ámbitos, cancelación, dispatchers, Flow y flujos calientes",
) {
    // -- Fundamentos -------------------------------------------------------------------
    demo("Por qué existen las corrutinas", ::demoWhyCoroutines)
    demo("suspend: qué significa exactamente", ::demoSuspend)
    demo("runBlocking: el puente entre los dos mundos", ::demoRunBlocking)
    demo("delay frente a Thread.sleep", ::demoDelayVsSleep)

    // -- launch y async ----------------------------------------------------------------
    demo("launch y el Job", ::demoLaunch)
    demo("async y el Deferred", ::demoAsync)
    demo("Secuencial frente a paralelo, medido", ::demoSequentialVsParallel)
    demo("LAZY, jerarquía de Jobs e invokeOnCompletion", ::demoJobDetails)

    // -- Concurrencia estructurada -----------------------------------------------------
    demo("Un ámbito espera a sus hijos", ::demoScopeWaits)
    demo("coroutineScope: paralelismo dentro de una suspend fun", ::demoCoroutineScope)
    demo("supervisorScope: hijos independientes", ::demoSupervisorScope)
    demo("Crear un CoroutineScope propio", ::demoCustomScope)

    // -- Cancelación -------------------------------------------------------------------
    demo("La cancelación es cooperativa", ::demoCooperativeCancellation)
    demo("isActive, ensureActive() y yield()", ::demoMakingItCooperative)
    demo("Limpiar al cancelar: finally y NonCancellable", ::demoCleanup)
    demo("No te tragues la CancellationException", ::demoDontSwallow)

    // -- Timeouts ----------------------------------------------------------------------
    demo("withTimeout", ::demoWithTimeout)
    demo("withTimeoutOrNull", ::demoWithTimeoutOrNull)
    demo("Un timeout es una cancelación", ::demoTimeoutIsCancellation)
    demo("Patrones con timeout y reintentos", ::demoTimeoutPatterns)

    // -- Contexto y dispatchers --------------------------------------------------------
    demo("El CoroutineContext y sus piezas", ::demoWhatIsAContext)
    demo("Los dispatchers: Default, IO y Unconfined", ::demoDispatchers)
    demo("withContext: cambiar de hilo", ::demoWithContext)
    demo("Dispatchers.Main y por qué aquí no existe", ::demoMainDispatcher)

    // -- Excepciones -------------------------------------------------------------------
    demo("launch frente a async ante un fallo", ::demoLaunchVsAsyncErrors)
    demo("CoroutineExceptionHandler", ::demoExceptionHandler)
    demo("Job frente a SupervisorJob", ::demoSupervisorJob)
    demo("Dónde poner el try/catch", ::demoTryCatchInCoroutines)

    // -- Flow: fundamentos -------------------------------------------------------------
    demo("Qué es un Flow", ::demoWhatIsAFlow)
    demo("Cómo se construye un Flow", ::demoFlowBuilders)
    demo("Flujos fríos: uno por colector", ::demoColdFlows)
    demo("Flow frente a Sequence", ::demoFlowVsSequence)

    // -- Flow: operadores --------------------------------------------------------------
    demo("Operadores intermedios", ::demoIntermediateOperators)
    demo("Operadores terminales", ::demoTerminalOperators)
    demo("Errores dentro de un Flow", ::demoFlowExceptions)
    demo("Contexto y contrapresión: flowOn, buffer, conflate", ::demoFlowContextAndBuffering)

    // -- Flujos calientes y canales ----------------------------------------------------
    demo("StateFlow: un valor observable", ::demoStateFlow)
    demo("SharedFlow: eventos para todos", ::demoSharedFlow)
    demo("Channel: una cola entre corrutinas", ::demoChannels)
    demo("Cuál usar para cada cosa", ::demoWhatToUseWhen)

    // -- Cierre ------------------------------------------------------------------------
    demo("Los cinco antipatrones", ::demoAntiPatterns)
    demo("Reglas de diseño", ::demoDesignRules)
    demo("Lista de comprobación", ::demoChecklist)
}

/** Ejecuta el capítulo 28 completo. */
fun main() = chapter28.runAll()
