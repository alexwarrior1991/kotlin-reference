package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.time.measureTime

// =====================================================================================
//  28.6 · CoroutineContext, dispatchers y withContext
//
//  QUÉ ES
//    Cada corrutina lleva un CONTEXTO: un conjunto de piezas (el Job, el dispatcher,
//    el nombre, el manejador de excepciones) que se heredan del padre y se pueden
//    sustituir. El DISPATCHER es la pieza que decide en qué hilo se ejecuta.
//
//  POR QUÉ IMPORTA
//    Elegir mal el dispatcher es el error de rendimiento más caro de las corrutinas:
//    una lectura de fichero en `Dispatchers.Default` puede dejar sin hilos a todo el
//    trabajo de CPU de la aplicación.
//
//  ERRORES COMUNES
//    · Hacer entrada/salida bloqueante en `Dispatchers.Default`.
//    · Cambiar de dispatcher en cada llamada "por si acaso": cada salto cuesta.
//    · Usar `Dispatchers.Unconfined` sin entender que el hilo cambia a mitad.
//    · Pasar un dispatcher a `launch` cuando lo que querías era `withContext`.
// =====================================================================================

/**
 * El contexto: qué lleva dentro una corrutina.
 */
fun demoWhatIsAContext() {
    section("Un contexto es un conjunto de piezas")

    bullet("Job                      → quién es el padre, cómo se cancela")
    bullet("CoroutineDispatcher      → en qué hilo se ejecuta")
    bullet("CoroutineName            → nombre para los logs y la depuración")
    bullet("CoroutineExceptionHandler → qué hacer si falla (demo 28.26)")

    section("Se combinan con el operador +")

    runBlocking {
        val trabajo = launch(Dispatchers.Default + CoroutineName("descarga")) {
            show("nombre", coroutineContext[CoroutineName]?.name)
            show("dispatcher", coroutineContext[ContinuationInterceptor])
            show("¿tiene Job?", coroutineContext[Job] != null)
            show("isActive", isActive)
        }
        trabajo.join()
    }

    bullet("`Dispatchers.Default + CoroutineName(\"x\")` es un contexto de dos piezas.")
    bullet("El `+` no suma: SUSTITUYE la pieza del mismo tipo si ya existía.")

    section("El contexto se hereda")

    runBlocking {
        launch(CoroutineName("padre")) {
            show("en el padre", coroutineContext[CoroutineName]?.name)

            launch {
                // Sin indicar nada, hereda el nombre y el dispatcher del padre.
                show("en el hijo (hereda)", coroutineContext[CoroutineName]?.name)
            }.join()

            launch(CoroutineName("hijo propio")) {
                show("en el hijo (sustituye)", coroutineContext[CoroutineName]?.name)
            }.join()
        }.join()
    }

    bullet("Todo se hereda MENOS el Job: cada corrutina tiene el suyo, hijo del padre.")
    bullet("Por eso cancelar al padre cancela a los hijos, pero no al revés.")

    section("Para qué sirve CoroutineName")

    bullet("Sale en los stack traces y en el depurador de IntelliJ.")
    bullet("Con `-Dkotlinx.coroutines.debug` sale también en el nombre del hilo, en")
    bullet("forma de sufijo '@coroutine#N'. Este proyecto arranca con `-ea`, y eso")
    bullet("activa el modo depuración solo: por eso lo verás en la demo siguiente.")
    bullet("No cuesta nada y ahorra mucho tiempo cuando algo va mal en producción.")
}

/**
 * Los dispatchers que trae la librería.
 */
fun demoDispatchers() {
    section("Los tres que se usan en consola y servidor")

    bullet("Dispatchers.Default → CPU. Tantos hilos como núcleos (mínimo 2).")
    bullet("                      Para cálculo, ordenación, parseo, compresión.")
    bullet("Dispatchers.IO      → Espera bloqueante. Hasta 64 hilos por defecto.")
    bullet("                      Para ficheros, JDBC, librerías HTTP síncronas.")
    bullet("Dispatchers.Unconfined → No confina a ningún hilo. Casi nunca se usa.")

    section("Y el cuarto, que aquí no existe")

    bullet("Dispatchers.Main → el hilo de interfaz. Necesita un módulo de plataforma")
    bullet("(kotlinx-coroutines-android, -javafx, -swing). En consola NO está.")
    bullet("Se ve en detalle en la demo siguiente.")

    section("En qué hilo se ejecuta cada uno")

    runBlocking {
        show("runBlocking (hereda el hilo llamante)", nombreDeHiloCorto())

        withContext(Dispatchers.Default) {
            show("Dispatchers.Default", nombreDeHiloCorto())
        }
        withContext(Dispatchers.IO) {
            show("Dispatchers.IO", nombreDeHiloCorto())
        }
    }

    bullet("Los nombres exactos cambian entre ejecuciones: no dependas de ellos.")

    section("Default e IO comparten hilos")

    bullet("No son dos pozos separados: IO es una VISTA del mismo pozo de hilos")
    bullet("con un límite de paralelismo distinto. Cambiar de Default a IO suele")
    bullet("reutilizar el mismo hilo sin pagar un salto real.")

    section("Por qué importa no mezclarlos")

    // Default tiene tantos hilos como núcleos. Si los bloqueas, no queda ninguno
    // libre para el trabajo de CPU de verdad.
    val nucleos = Runtime.getRuntime().availableProcessors()
    show("núcleos de esta máquina", nucleos)
    show("hilos de Dispatchers.Default", "≈ $nucleos")
    show("hilos de Dispatchers.IO", "hasta 64")

    bullet("Con 8 núcleos, 8 lecturas de fichero bloqueantes en Default dejan a CERO")
    bullet("los hilos disponibles para calcular. La aplicación entera se para.")
    bullet("En IO, en cambio, hay margen de sobra para esperas bloqueantes.")

    section("Unconfined: la curiosidad")

    runBlocking {
        val traza = mutableListOf<String>()

        launch(Dispatchers.Unconfined) {
            traza.add("antes de delay: ${nombreDeHiloCorto()}")
            delay(10)
            // Tras el delay, sigue en el hilo que se encargó de reanudarlo,
            // que es el del temporizador, no el de antes.
            traza.add("después de delay: ${nombreDeHiloCorto()}")
        }.join()

        traza.forEach { bullet(it) }
    }

    bullet("El hilo CAMBIA a mitad de la corrutina. Es correcto, pero desconcertante.")
    bullet("Úsalo sólo en tests muy concretos; en código normal, nunca.")

    section("Limitar el paralelismo")

    bullet("`Dispatchers.IO.limitedParallelism(4)` crea una vista con como mucho")
    bullet("4 hilos a la vez: útil para no saturar una base de datos con 64 conexiones.")
    bullet("(No se ejecuta aquí porque todavía requiere una anotación de opt-in.)")
}

/**
 * `withContext`: cambiar de dispatcher.
 */
fun demoWithContext() {
    section("Qué hace")

    bullet("Ejecuta un bloque en OTRO contexto y devuelve su resultado.")
    bullet("Suspende hasta que el bloque termina: no lanza nada en paralelo.")

    section("El patrón: la función se hace segura por dentro")

    runBlocking {
        // Quien llama no necesita saber nada de dispatchers: la función se ocupa.
        show("leerFicheroSimulado()", leerFicheroSimulado())
        show("calcularPesado(30)", calcularPesado(30))
    }

    bullet("Esta es LA regla: una `suspend fun` debe poder llamarse desde cualquier")
    bullet("dispatcher sin bloquear. Se llama 'main-safety'.")
    bullet("El cambio de contexto es responsabilidad de la función, no de quien llama.")

    section("withContext frente a launch/async")

    runBlocking {
        val secuencial = measureTime {
            withContext(Dispatchers.Default) { delay(100) }
            withContext(Dispatchers.Default) { delay(100) }
        }
        show("dos withContext seguidos", "~200 ms (son secuenciales)")
        show("¿más de 150 ms?", secuencial.inWholeMilliseconds > 150)

        val paralelo = measureTime {
            listOf(
                async(Dispatchers.Default) { delay(100) },
                async(Dispatchers.Default) { delay(100) },
            ).awaitAll()
        }
        show("dos async en paralelo", "~100 ms")
        show("¿menos de 150 ms?", paralelo.inWholeMilliseconds < 150)
    }

    bullet("`withContext` = cambiar de hilo, sin concurrencia.")
    bullet("`async`/`launch` = concurrencia, y de paso puedes cambiar de hilo.")
    bullet("Si sólo quieres cambiar de hilo, `withContext`: es más barato y más claro.")

    section("Devuelve valor, como cualquier expresión")

    runBlocking {
        val suma = withContext(Dispatchers.Default) {
            (1..1_000).sum()
        }
        show("withContext { (1..1000).sum() }", suma)
    }

    section("El coste de cambiar")

    runBlocking {
        val conSaltos = measureTime {
            repeat(1_000) {
                withContext(Dispatchers.Default) { /* nada */ }
            }
        }
        show("1.000 cambios de contexto", "${conSaltos.inWholeMilliseconds} ms")
    }

    bullet("Cada salto tiene un coste. Es pequeño, pero mil llamadas se notan.")
    bullet("Cambia de dispatcher en el BORDE (una vez por operación), no dentro")
    bullet("de un bucle. Si el bloque es diminuto, el salto cuesta más que el trabajo.")

    section("Un matiz: withContext no crea una corrutina hija normal")

    bullet("Reutiliza la corrutina actual con otro contexto, así que una excepción")
    bullet("dentro de `withContext` sale por donde esperas: un try/catch alrededor")
    bullet("del `withContext` SÍ la captura (a diferencia de `launch`, demo 28.28).")
}

/**
 * `Dispatchers.Main`: explicado y comprobado.
 */
fun demoMainDispatcher() {
    section("Qué es")

    bullet("El dispatcher del hilo de interfaz de usuario. Todo lo que toque una")
    bullet("pantalla tiene que ejecutarse ahí, y sólo ahí.")

    section("Por qué aquí no funciona")

    bullet("`Dispatchers.Main` es un hueco que rellena un módulo de plataforma:")
    bullet("  · Android → kotlinx-coroutines-android")
    bullet("  · Swing   → kotlinx-coroutines-swing")
    bullet("  · JavaFX  → kotlinx-coroutines-javafx")
    bullet("Este proyecto es de consola: no hay ninguno, así que el hueco está vacío.")

    section("Compruébalo (sin que reviente nada)")

    val resultado = runCatching {
        runBlocking {
            withContext(Dispatchers.Main) { "se ejecutó en el hilo de UI" }
        }
    }.fold(
        onSuccess = { "funcionó: hay un módulo de plataforma cargado" },
        onFailure = { fallo ->
            val primeraLinea = fallo.message?.lineSequence()?.firstOrNull().orEmpty()
            "${fallo::class.simpleName}: ${primeraLinea.take(70)}…"
        },
    )
    show("withContext(Dispatchers.Main)", resultado)

    bullet("Falla en EJECUCIÓN, no al compilar: el tipo existe, la implementación no.")

    section("Cómo se usa en una aplicación real")

    bullet("El patrón de Android, escrito para que se entienda (aquí no se ejecuta):")
    bullet("")
    bullet("  viewModelScope.launch {                 // arranca en Main")
    bullet("      estado.value = Cargando")
    bullet("      val datos = repositorio.cargar()    // la función se hace main-safe")
    bullet("      estado.value = Listo(datos)         // vuelve a Main solo")
    bullet("  }")
    bullet("")
    bullet("Fíjate en que NO hay ningún `withContext` a la vista: el repositorio")
    bullet("se encarga por dentro. Eso es exactamente la regla de la demo anterior.")

    section("El error clásico de Android")

    bullet("`withContext(Dispatchers.Main) { }` para 'volver al hilo de UI' después")
    bullet("de cada operación. No hace falta: al terminar un `withContext(IO)`,")
    bullet("la corrutina vuelve SOLA al dispatcher que tenía antes.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/**
 * Nombre del hilo actual, recortado para que la salida quepa en una línea.
 */
private fun nombreDeHiloCorto(): String = Thread.currentThread().name.take(45)

/**
 * Simula leer un fichero: trabajo de ESPERA → `Dispatchers.IO`.
 *
 * La función es "main-safe": quien la llame puede estar en cualquier dispatcher.
 */
private suspend fun leerFicheroSimulado(): String = withContext(Dispatchers.IO) {
    delay(30)                       // aquí iría la lectura bloqueante de verdad
    "contenido en ${nombreDeHiloCorto()}"
}

/**
 * Simula un cálculo largo: trabajo de CPU → `Dispatchers.Default`.
 */
private suspend fun calcularPesado(n: Int): String = withContext(Dispatchers.Default) {
    val resultado = (1..n).fold(1L) { acumulado, i -> acumulado * i % 1_000_003 }
    "$resultado (calculado en ${nombreDeHiloCorto()})"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `Dispatchers.IO` por `Dispatchers.Default` en `leerFicheroSimulado` y
//     razona qué pasaría si hubiera cien lecturas a la vez.
//  2. Lanza 100 corrutinas con `Thread.sleep(100)` en Default y mide; repite en IO.
//  3. Añade `CoroutineName` a una corrutina que falle y mira si sale en el error.
//  4. Sube a 10.000 los cambios de contexto de la última medición.
