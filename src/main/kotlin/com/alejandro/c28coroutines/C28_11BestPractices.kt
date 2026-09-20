package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTime

// =====================================================================================
//  28.11 · Buenas prácticas y antipatrones
//
//  QUÉ ES
//    El resumen práctico del capítulo: lo que hay que hacer, lo que no, y por qué.
//
//  POR QUÉ IMPORTA
//    Las corrutinas son fáciles de escribir y difíciles de escribir BIEN. Casi todos
//    los problemas de producción con corrutinas son uno de los cinco antipatrones
//    de la primera demo.
//
//  LA IDEA QUE LO RESUME TODO
//    Si no sabes decir quién cancela una corrutina, todavía no has terminado de
//    diseñarla.
// =====================================================================================

/**
 * Los cinco antipatrones.
 */
@OptIn(DelicateCoroutinesApi::class)
fun demoAntiPatterns() {
    section("1. GlobalScope: corrutinas sin dueño")

    runBlocking {
        val traza = mutableListOf<String>()

        val padre = launch {
            // MAL: esta corrutina NO es hija de `padre`. Nadie la espera ni la cancela.
            GlobalScope.launch {
                delay(60)
                anotar(traza, "la de GlobalScope siguió viva pese a la cancelación")
            }

            // BIEN: ésta sí es hija, y cae con el padre.
            launch {
                delay(60)
                anotar(traza, "la hija de verdad terminó")
            }

            delay(200)
        }

        delay(20)
        padre.cancel()        // cancelamos al padre
        delay(150)            // damos tiempo a que se vea quién sobrevive

        traza.forEach { bullet(it) }
        show("¿sobrevivió la de GlobalScope?", traza.any { "GlobalScope" in it })
        show("¿sobrevivió la hija de verdad?", traza.any { "hija de verdad" in it })
    }

    bullet("`GlobalScope` vive lo que viva el proceso: ni se cancela con nada, ni")
    bullet("nadie lo espera. Es la forma más rápida de tener una fuga.")
    bullet("Está marcado como `@DelicateCoroutinesApi` precisamente por eso.")
    bullet("Alternativa: un CoroutineScope con un ciclo de vida claro (demo 28.12).")

    section("2. Bloquear el hilo en Dispatchers.Default")

    runBlocking {
        val nucleos = Runtime.getRuntime().availableProcessors()
        val cuantas = maxOf(nucleos, 2) * 3

        val enDefault = measureTime {
            List(cuantas) {
                launch(Dispatchers.Default) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Thread.sleep(50)
                }
            }.joinAll()
        }

        val enIo = measureTime {
            List(cuantas) {
                launch(Dispatchers.IO) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Thread.sleep(50)
                }
            }.joinAll()
        }

        show("núcleos", nucleos)
        show("$cuantas esperas bloqueantes en Default", "${enDefault.inWholeMilliseconds} ms")
        show("$cuantas esperas bloqueantes en IO", "${enIo.inWholeMilliseconds} ms")
        show("¿Default tardó más?", enDefault > enIo)
    }

    bullet("Default tiene tantos hilos como núcleos: al bloquearlos todos, el resto")
    bullet("de tareas hace cola. IO tiene hasta 64, pensados justo para esperar.")
    bullet("Y lo ideal ni siquiera es eso: usar la versión suspendida de la librería.")

    section("3. async sin await")

    runBlocking {
        val registrados = mutableListOf<String>()

        supervisorScope {
            // MAL: nadie va a llamar a `await()`, así que el fallo se evapora.
            async<String> { delay(10); error("fallo que nadie verá") }
            delay(50)
        }
        show("errores registrados con async sin await", registrados.size)

        // BIEN: con `launch`, el fallo llega al manejador del ámbito.
        val manejador = CoroutineExceptionHandler { _, fallo ->
            anotar(registrados, fallo.message ?: "?")
        }
        val ambito = CoroutineScope(SupervisorJob() + manejador)
        ambito.launch { delay(10); error("fallo que sí se registra") }.join()
        ambito.cancel()

        show("errores registrados usando launch", registrados)
    }

    bullet("Si no vas a llamar a `await()`, usa `launch`: al menos el fallo llega")
    bullet("al CoroutineExceptionHandler en vez de evaporarse.")

    section("4. Tragarse la CancellationException")

    bullet("`catch (e: Exception)` y `runCatching` capturan la cancelación.")
    bullet("La corrutina sigue trabajando después de haberla cancelado (demo 28.16).")
    bullet("Regla: captura tipos concretos, o relanza CancellationException primero.")

    section("5. Creer que 'con corrutinas no hay problemas de concurrencia'")

    runBlocking {
        var contadorInseguro = 0
        val contadorSeguro = AtomicInteger(0)
        val vueltas = 1_000
        val corrutinas = 50

        withContext(Dispatchers.Default) {
            List(corrutinas) {
                launch {
                    repeat(vueltas) {
                        contadorInseguro++              // ← carrera de datos
                        contadorSeguro.incrementAndGet()
                    }
                }
            }.joinAll()
        }

        val esperado = corrutinas * vueltas
        show("esperado", esperado)
        show("var normal compartida", contadorInseguro)
        show("¿se perdieron incrementos?", contadorInseguro != esperado)
        show("AtomicInteger", contadorSeguro.get())
    }

    bullet("`contador++` son tres operaciones (leer, sumar, escribir) y dos hilos")
    bullet("pueden entrelazarlas. Las corrutinas NO te protegen de eso.")
    bullet("Soluciones: no compartir estado mutable; si hay que hacerlo, usar tipos")
    bullet("atómicos, `Mutex.withLock`, o confinar el estado a UNA sola corrutina.")

    section("Y uno más, que aquí no se puede medir")

    bullet("`runBlocking` dentro de una corrutina. Bloquea el hilo del dispatcher")
    bullet("mientras espera, así que anula la ventaja entera de las corrutinas y,")
    bullet("en dispatchers con pocos hilos, puede provocar un interbloqueo.")
    bullet("`runBlocking` va SÓLO en `main()` y en los tests que no usen `runTest`.")
}

/**
 * Las reglas de diseño.
 */
fun demoDesignRules() {
    section("1. Toda suspend fun debe ser segura desde cualquier dispatcher")

    runBlocking {
        show("repositorio.cargar(7)", RepositorioSimulado().cargar(7))
    }

    bullet("Quien llama no debería tener que escribir `withContext(IO)` alrededor.")
    bullet("El cambio de contexto es responsabilidad de la función, no de quien llama.")

    section("2. Inyecta el dispatcher si quieres poder testear")

    runBlocking {
        // En un test se inyecta un dispatcher de prueba y el tiempo es virtual.
        val conDispatcherInyectado = RepositorioSimulado(Dispatchers.Default)
        show("con otro dispatcher", conDispatcherInyectado.cargar(9))
    }

    bullet("Un `Dispatchers.IO` escrito a fuego dentro de la clase la hace imposible")
    bullet("de testear con tiempo virtual. Un parámetro con valor por defecto cuesta")
    bullet("una línea y te deja sustituirlo en los tests (capítulo 30).")

    section("3. Una suspend fun no crea su propio scope: usa coroutineScope")

    runBlocking {
        show("cargarTodo()", cargarTodo())
    }

    bullet("MAL:  suspend fun f() { CoroutineScope(...).launch { ... } }   ← fuga")
    bullet("BIEN: suspend fun f() = coroutineScope { launch { ... } }      ← esperado")
    bullet("La segunda no vuelve hasta que todo lo suyo ha terminado, y se cancela")
    bullet("con quien la llamó. La primera no cumple ninguna de las dos cosas.")

    section("4. Expón Flow, nunca Channel")

    bullet("Un Channel es de un solo uso y de un solo consumidor: si lo devuelves")
    bullet("desde una API, quien lo reciba puede consumirlo una vez y ya.")
    bullet("Un Flow se puede recoger las veces que haga falta, se compone con")
    bullet("operadores y no obliga a nadie a cerrarlo.")

    section("5. Ponle nombre a las corrutinas de fondo")

    bullet("`launch(CoroutineName(\"sincronización\"))` cuesta nada y aparece en los")
    bullet("stack traces y en el depurador. La primera vez que algo falle de noche,")
    bullet("lo agradecerás.")

    section("6. Elige el ámbito antes que el dispatcher")

    bullet("La pregunta importante no es '¿en qué hilo corre esto?' sino '¿quién lo")
    bullet("cancela y cuándo?'. El dispatcher es una optimización; el ámbito es")
    bullet("corrección.")

    section("7. Cancela siempre los ámbitos que crees")

    bullet("Si escribes `CoroutineScope(...)`, escribe también dónde va el `cancel()`.")
    bullet("Normalmente es un `close()`, un `onDestroy()` o el final de una petición.")
}

/**
 * La lista de comprobación final.
 */
fun demoChecklist() {
    section("Antes de dar por buena una corrutina")

    bullet("□ ¿A qué ámbito pertenece? ¿Quién lo cancela y cuándo?")
    bullet("□ Si es un bucle largo, ¿comprueba `isActive` o llama a `ensureActive()`?")
    bullet("□ ¿Hay algún `catch (e: Exception)` o `runCatching` alrededor de código")
    bullet("  suspendido? Si lo hay, ¿relanza CancellationException?")
    bullet("□ ¿Toda llamada a un sistema externo tiene timeout?")
    bullet("□ ¿Hay estado mutable compartido entre corrutinas? ¿Cómo está protegido?")
    bullet("□ ¿El dispatcher es el adecuado: Default para CPU, IO para esperas?")
    bullet("□ ¿Hay algún `async` cuyo `await()` no se llame nunca?")
    bullet("□ ¿Los recursos se cierran en un `finally` o con `use`?")

    section("Antes de dar por bueno un Flow")

    bullet("□ ¿Tiene operador terminal? Sin él no se ejecuta nada.")
    bullet("□ Si es infinito, ¿quién lo corta: `take`, `first` o la cancelación?")
    bullet("□ ¿Los errores se tratan con el operador `catch`, y está bien colocado?")
    bullet("□ ¿`flowOn` está donde debe (afecta a lo de ARRIBA)?")
    bullet("□ ¿Frío o caliente? ¿Recogerlo dos veces duplica el trabajo?")
    bullet("□ Si es caliente: ¿StateFlow para estado, SharedFlow para eventos?")
    bullet("□ ¿Se expone la vista de sólo lectura (`asStateFlow`/`asSharedFlow`)?")

    section("Los cinco conceptos que hay que tener claros")

    bullet("1. Concurrencia estructurada: toda corrutina tiene padre y el padre espera.")
    bullet("2. La cancelación es cooperativa: si no la compruebas, no ocurre.")
    bullet("3. `launch` falla hacia arriba; `async` guarda el fallo hasta el `await`.")
    bullet("4. Un Flow frío es una receta; un StateFlow/SharedFlow es un valor vivo.")
    bullet("5. El dispatcher decide el hilo; el Job decide la vida.")

    section("Qué practicar después")

    bullet("Los ejercicios 10 y 11 del capítulo 31 (descargas y eventos).")
    bullet("El capítulo 30: testear corrutinas con `runTest` y tiempo virtual.")
    bullet("Y, sobre todo, romper los ejemplos de este capítulo a propósito:")
    bullet("quitar un `join`, tragarse una cancelación, mover un `catch` de sitio.")
    bullet("Entender por qué el resultado cambia vale más que leerlo diez veces.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/** Añade una línea a una lista que tocan varias corrutinas a la vez. */
private fun anotar(destino: MutableList<String>, linea: String) {
    synchronized(destino) { destino.add(linea) }
}

/**
 * El patrón correcto: la función se hace segura por dentro y el dispatcher se
 * inyecta con un valor por defecto, para poder sustituirlo en los tests.
 */
private class RepositorioSimulado(
    private val dispatcherDeEspera: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun cargar(id: Int): String = withContext(dispatcherDeEspera) {
        delay(20)
        "registro-$id"
    }
}

/**
 * Trabajo en paralelo dentro de una `suspend fun`, sin crear ningún ámbito propio.
 *
 * `coroutineScope` hereda el Job de quien llama, así que se cancela con él y no
 * vuelve hasta que sus dos hijas han terminado.
 */
private suspend fun cargarTodo(): String = coroutineScope {
    val uno = async { delay(30); "A" }
    val dos = async { delay(30); "B" }
    "${uno.await()}+${dos.await()}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Sube las corrutinas de la carrera de datos a 200 y mira cuánto se pierde.
//  2. Sustituye el `var` por un `Mutex` con `withLock` y comprueba que ya cuadra.
//  3. Cambia `GlobalScope.launch` por `launch` en la primera demo: la corrutina pasa
//     a morir con su padre, que es justo lo que quieres.
//  4. Repite la medición de Default frente a IO con `delay(50)` en vez de
//     `Thread.sleep(50)`: los dos tiempos se igualan, porque ya no bloqueas nada.
