package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

// =====================================================================================
//  28.3 · Concurrencia estructurada
//
//  QUÉ ES
//    La regla de que toda corrutina pertenece a un ÁMBITO (scope), y que ese ámbito
//    no termina hasta que todas sus corrutinas han terminado.
//
//  POR QUÉ IMPORTA
//    Es la idea central de las corrutinas de Kotlin, y lo que las diferencia de los
//    hilos y de las promesas de otros lenguajes. Sin ella tendrías trabajos huérfanos
//    corriendo por ahí sin que nadie los espere ni los cancele.
//
//  ERRORES COMUNES
//    · Usar `GlobalScope` y perder toda la estructura (demo 28.41).
//    · Confundir `coroutineScope` (un fallo cancela a todos) con `supervisorScope`
//      (cada hijo es independiente).
//    · Crear un CoroutineScope y no cancelarlo nunca.
// =====================================================================================

/**
 * La regla básica: el ámbito espera.
 */
fun demoScopeWaits() {
    section("Un ámbito no termina hasta que sus hijos terminan")

    val traza = mutableListOf<String>()

    runBlocking {
        traza.add("runBlocking: empiezo")

        launch {
            delay(100)
            traza.add("hijo 1: termino")
        }
        launch {
            delay(50)
            traza.add("hijo 2: termino")
        }

        traza.add("runBlocking: mi cuerpo ha llegado al final")
        // Aquí el bloque de runBlocking ya ha acabado... pero NO vuelve todavía.
    }

    traza.add("después de runBlocking")
    traza.forEach { bullet(it) }

    bullet("Fíjate en el orden: 'mi cuerpo ha llegado al final' sale ANTES que los")
    bullet("hijos, pero 'después de runBlocking' sale DESPUÉS de todos.")
    bullet("No hace falta ningún `join()`: el ámbito lo hace por ti.")

    section("Qué garantiza esto")

    bullet("Ninguna corrutina se queda huérfana.")
    bullet("Si cancelas el ámbito, se cancelan todos sus hijos.")
    bullet("Si un hijo falla, el ámbito se entera (y por defecto cancela al resto).")
    bullet("Cuando la función vuelve, no hay nada corriendo por detrás.")
}

/**
 * `coroutineScope`: agrupar trabajo dentro de una función suspend.
 */
fun demoCoroutineScope() {
    section("Para qué sirve")

    bullet("Crea un ámbito hijo DENTRO de una función `suspend`.")
    bullet("La función no vuelve hasta que todo lo lanzado dentro ha terminado.")
    bullet("Es la forma de escribir una función que hace cosas en paralelo pero que,")
    bullet("vista desde fuera, se comporta como una llamada normal.")

    section("En funcionamiento")

    runBlocking {
        val resultado = cargarPerfilCompleto(7)
        show("cargarPerfilCompleto(7)", resultado)
    }

    bullet("Por dentro lanza tres tareas en paralelo; por fuera es una `suspend fun`")
    bullet("normal que devuelve un valor. Quien la llama no se entera de nada.")

    section("Si un hijo falla, se cancelan TODOS")

    runBlocking {
        val salida = try {
            cargarConUnFallo()
        } catch (e: IllegalStateException) {
            "el ámbito propagó: ${e.message}"
        }
        show("cargarConUnFallo()", salida)
    }

    bullet("La tarea que falló cancela a sus hermanas y la excepción sube.")
    bullet("Es lo que quieres cuando todas las partes son necesarias: si falta una,")
    bullet("seguir con las demás es tirar trabajo.")

    section("coroutineScope frente a runBlocking")

    bullet("runBlocking   → BLOQUEA el hilo. Para el borde del programa.")
    bullet("coroutineScope → SUSPENDE. Para dentro de código ya suspendido.")
    bullet("Los dos esperan a sus hijos; la diferencia es si bloquean un hilo o no.")
}

/**
 * `supervisorScope`: hijos independientes.
 */
fun demoSupervisorScope() {
    section("La diferencia con coroutineScope")

    bullet("coroutineScope  → un hijo falla, se cancelan todos y sube la excepción.")
    bullet("supervisorScope → un hijo falla, los demás siguen.")

    section("Con coroutineScope: todo o nada")

    runBlocking {
        val salida = try {
            coroutineScope {
                val a = async { delay(30); "A ok" }
                val b = async<String> { delay(10); error("B falló") }
                val c = async { delay(30); "C ok" }
                listOf(a.await(), b.await(), c.await())
            }.toString()
        } catch (e: IllegalStateException) {
            "todo cancelado por culpa de: ${e.message}"
        }
        show("coroutineScope", salida)
    }

    section("Con supervisorScope: lo que se pueda")

    runBlocking {
        val resultados = supervisorScope {
            val tareas = listOf(
                async { delay(30); "A ok" },
                async<String> { delay(10); error("B falló") },
                async { delay(30); "C ok" },
            )
            // Cada `await` se protege por separado.
            tareas.map { tarea ->
                runCatching { tarea.await() }.getOrElse { "(falló: ${it.message})" }
            }
        }
        show("supervisorScope", resultados)
    }

    bullet("A y C terminaron bien aunque B fallara.")
    bullet("Fíjate en que hay que capturar el fallo de CADA `await` por separado.")

    section("Cuándo usar cada uno")

    bullet("coroutineScope  → las partes son necesarias: un perfil sin sus pedidos")
    bullet("                  no sirve de nada. Falla rápido y no malgastes trabajo.")
    bullet("supervisorScope → las partes son independientes: enviar 100 notificaciones")
    bullet("                  y que una falle no debe impedir las otras 99.")

    section("El matiz de la propagación")

    bullet("En `supervisorScope`, un `launch` que falle NO cancela a sus hermanos,")
    bullet("pero su excepción tiene que ir a algún sitio: al CoroutineExceptionHandler")
    bullet("(demo 28.26). Con `async`, la excepción espera guardada hasta el `await`.")
}

/**
 * Crear un scope propio.
 */
fun demoCustomScope() {
    section("Cuando el trabajo sobrevive a la función que lo lanza")

    // Un scope propio tiene ciclo de vida: se crea, se usa y SE CANCELA.
    val servicio = ServicioConScope()

    runBlocking {
        servicio.arrancarTareaDeFondo()
        delay(60)
        show("tareas completadas mientras tanto", servicio.completadas())

        // Sin este `cerrar()`, las corrutinas seguirían vivas indefinidamente.
        servicio.cerrar()
        delay(30)
        show("tras cerrar, ¿el scope sigue activo?", servicio.estaActivo())
    }

    bullet("Un CoroutineScope propio SIEMPRE necesita un punto de cancelación.")
    bullet("Si no lo cancelas, tienes una fuga: corrutinas vivas para siempre.")

    section("Dónde está ese punto en la vida real")

    bullet("Android: `viewModelScope` se cancela solo al destruir el ViewModel.")
    bullet("Servidor: el scope de la petición se cancela al terminar la respuesta.")
    bullet("Aplicación de consola: `runBlocking` en `main()`, que espera a todo.")
    bullet("Si estás creando un scope a mano, pregúntate quién lo va a cancelar.")

    section("La regla de oro de la concurrencia estructurada")

    bullet("Toda corrutina debe pertenecer a un ámbito con un ciclo de vida claro.")
    bullet("Si no sabes decir cuándo se cancela, todavía no has terminado el diseño.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/**
 * Por fuera es una `suspend fun` normal; por dentro trabaja en paralelo.
 *
 * `coroutineScope` garantiza que no vuelve hasta que las tres tareas terminan.
 */
private suspend fun cargarPerfilCompleto(id: Int): String = coroutineScope {
    val nombre = async { delay(40); "usuario-$id" }
    val pedidos = async { delay(60); listOf("P-1", "P-2") }
    val puntos = async { delay(30); 120 }

    "${nombre.await()} · ${pedidos.await().size} pedidos · ${puntos.await()} puntos"
}

/** Una de las tres tareas falla: el ámbito cancela a las demás y propaga. */
private suspend fun cargarConUnFallo(): String = coroutineScope {
    val ok = async { delay(100); "esto no llega a usarse" }
    val falla = async<String> { delay(20); error("la segunda tarea falló") }

    listOf(ok.await(), falla.await()).joinToString()
}

/**
 * Un servicio con su propio scope y su punto de cancelación.
 *
 * El contexto se construye sumando dos piezas: un `SupervisorJob` (para que el fallo
 * de una tarea no tumbe las demás) y un dispatcher (demo 28.22).
 */
private class ServicioConScope {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var contador = 0

    fun arrancarTareaDeFondo() {
        scope.launch {
            repeat(3) {
                delay(15)
                synchronized(this@ServicioConScope) { contador++ }
            }
        }
    }

    fun completadas(): Int = synchronized(this) { contador }

    fun estaActivo(): Boolean = scope.coroutineContext[Job]?.isActive ?: false

    /**
     * El punto de cancelación: sin esto, las corrutinas vivirían para siempre.
     * `cancel()` es una extensión de CoroutineScope que trae kotlinx.coroutines.
     */
    fun cerrar() {
        scope.cancel()
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `coroutineScope` por `supervisorScope` en `cargarConUnFallo` y observa
//     qué pasa con la tarea que sí iba a terminar.
//  2. Quita el `servicio.cerrar()` y razona por qué eso es una fuga.
//  3. Añade un cuarto `async` a `cargarPerfilCompleto` y comprueba que el tiempo
//     total sigue siendo el de la tarea más lenta.
//  4. Lanza una corrutina dentro de otra y comprueba que el padre espera a la nieta.
