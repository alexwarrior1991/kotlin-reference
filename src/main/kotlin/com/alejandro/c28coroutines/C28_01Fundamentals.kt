package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// =====================================================================================
//  28.1 · Qué es una corrutina, `suspend` y `runBlocking`
//
//  QUÉ ES
//    Una corrutina es un trabajo que puede PAUSARSE y REANUDARSE sin bloquear el
//    hilo en el que corre. Mientras está pausada, ese hilo queda libre para otra cosa.
//
//  POR QUÉ IMPORTA
//    Un hilo del sistema cuesta ~1 MB de pila y crear uno es caro. Una corrutina
//    cuesta unos pocos bytes. Por eso se pueden tener cien mil corrutinas esperando
//    y no cien mil hilos.
//
//  DEPENDENCIA NECESARIA
//    `suspend` es parte del lenguaje, pero todo lo demás (launch, async, Flow...)
//    vive en una librería aparte. En build.gradle.kts:
//
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
//
//  ERRORES COMUNES
//    · Usar `Thread.sleep` dentro de una corrutina: bloquea el hilo y tira por
//      tierra toda la ventaja.
//    · Llamar a `runBlocking` desde dentro de otra corrutina.
//    · Creer que `suspend` hace algo asíncrono por sí solo (no: sólo marca que la
//      función PUEDE pausarse).
// =====================================================================================

/**
 * El problema que resuelven.
 */
fun demoWhyCoroutines() {
    section("El coste de un hilo")

    bullet("Un hilo de la JVM reserva ~1 MB de pila y su creación pasa por el SO.")
    bullet("Con 10.000 hilos esperando una respuesta de red, la máquina se ahoga.")
    bullet("Y el 99% del tiempo están sin hacer nada: sólo esperando.")

    section("El coste de una corrutina")

    bullet("Unas decenas de bytes: es un objeto en el montón, no una pila del SO.")
    bullet("Pausarla y reanudarla no pasa por el sistema operativo.")
    bullet("100.000 corrutinas esperando es perfectamente normal.")

    section("Compruébalo")

    // 50.000 corrutinas, cada una esperando. Con hilos esto no sería viable.
    val cuantas = 50_000
    val tiempo = measureTime {
        runBlocking {
            val trabajos = List(cuantas) {
                launch {
                    delay(20)
                }
            }
            trabajos.forEach { it.join() }
        }
    }

    show("corrutinas lanzadas", cuantas)
    show("todas esperaron 20 ms", "y el total fue mucho menos que ${cuantas * 20} ms")
    show("¿tardó menos de 5 segundos?", tiempo.inWholeSeconds < 5)

    bullet("Las 50.000 esperaron EN PARALELO sobre un puñado de hilos.")
    bullet("Con 50.000 hilos de verdad, la JVM se habría quedado sin memoria.")
}

/**
 * `suspend`: la palabra clave.
 */
fun demoSuspend() {
    section("Qué significa `suspend`")

    bullet("«Esta función PUEDE pausarse en algún punto.»")
    bullet("No dice que sea asíncrona, ni que use otro hilo, ni que sea lenta.")
    bullet("Sólo marca que puede ceder el hilo mientras espera.")

    section("La regla de oro")

    bullet("Una función `suspend` sólo se puede llamar desde:")
    bullet("  · otra función `suspend`")
    bullet("  · un bloque de corrutina (launch, async, runBlocking...)")
    bullet("El compilador lo comprueba. No hay forma de saltárselo.")

    section("En funcionamiento")

    runBlocking {
        val resultado = obtenerUsuario(1)
        show("obtenerUsuario(1)", resultado)

        val varios = (1..3).map { obtenerUsuario(it) }
        show("tres llamadas seguidas", varios)
    }

    section("Qué hace el compilador por debajo")

    bullet("Convierte la función en una máquina de estados con un parámetro extra")
    bullet("(la 'continuación'), que guarda por dónde iba para poder reanudar.")
    bullet("Por eso desde Java una `suspend fun` se ve con un parámetro Continuation.")
    bullet("No hace falta saberlo para usarlas, pero explica los stack traces raros.")
}

/**
 * `runBlocking`: el puente entre los dos mundos.
 */
fun demoRunBlocking() {
    section("Para qué sirve")

    bullet("Crea una corrutina y BLOQUEA el hilo actual hasta que termina.")
    bullet("Es el puente entre código normal y código suspendido.")

    section("Dónde se usa")

    bullet("En `main()`, para arrancar el programa.")
    bullet("En los tests (aunque ahí es mejor `runTest`, capítulo 30).")
    bullet("En las demos de este capítulo, para poder llamarlas desde el lanzador.")

    section("Dónde NO se usa")

    bullet("Dentro de otra corrutina: bloquearías el hilo que la ejecuta.")
    bullet("En una librería: obligas a quien la use a pagar un hilo bloqueado.")
    bullet("En código de interfaz de usuario: congelarías la pantalla.")

    section("La alternativa en una aplicación real")

    bullet("En vez de `runBlocking`, se crea un CoroutineScope con su ciclo de vida:")
    bullet("  · Android: `viewModelScope`, `lifecycleScope`")
    bullet("  · Servidor: el scope de la petición que ofrece el framework")
    bullet("  · main(): `runBlocking` está bien, es el borde del programa.")

    section("Funcionando")

    val salida = runBlocking {
        val antes = "antes de suspender"
        delay(50)
        val despues = "después de suspender"
        "$antes → $despues"
    }
    show("runBlocking { ... }", salida)

    bullet("El valor del bloque es el valor de `runBlocking`.")
}

/**
 * `delay` frente a `Thread.sleep`: la diferencia que lo explica todo.
 */
fun demoDelayVsSleep() {
    section("Los dos esperan... pero de forma muy distinta")

    bullet("`Thread.sleep(n)`  → BLOQUEA el hilo: no puede hacer nada más.")
    bullet("`delay(n)`         → SUSPENDE la corrutina: el hilo queda libre.")

    section("Con delay: las tres esperan a la vez")

    val conDelay = measureTime {
        runBlocking {
            val trabajos = List(3) {
                launch {
                    delay(200)
                }
            }
            trabajos.forEach { it.join() }
        }
    }
    show("tres corrutinas con delay(200)", "tardó ~200 ms, no 600")
    show("¿tardó menos de 500 ms?", conDelay.inWholeMilliseconds < 500)

    section("Con Thread.sleep: se ponen en cola")

    // `runBlocking` usa UN solo hilo. Si cada corrutina lo bloquea con sleep,
    // no puede empezar la siguiente hasta que acabe la anterior.
    val conSleep = measureTime {
        runBlocking {
            val trabajos = List(3) {
                launch {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Thread.sleep(200)
                }
            }
            trabajos.forEach { it.join() }
        }
    }
    show("tres corrutinas con Thread.sleep(200)", "tardó ~600 ms: una detrás de otra")
    show("¿tardó más de 500 ms?", conSleep.inWholeMilliseconds > 500)

    section("La conclusión")

    bullet("Bloquear el hilo dentro de una corrutina anula su razón de ser.")
    bullet("Nunca uses Thread.sleep, operaciones de fichero bloqueantes, ni llamadas")
    bullet("JDBC síncronas dentro de una corrutina sin cambiar de dispatcher.")
    bullet("Si no queda más remedio, `withContext(Dispatchers.IO)` (demo 28.23).")

    section("Cómo detectarlo")

    bullet("IntelliJ marca en amarillo las llamadas bloqueantes dentro de `suspend`:")
    bullet("'Inappropriate blocking method call'. Hazle caso SIEMPRE.")
}

// -- Las funciones suspendidas que usan las demos ----------------------------------------------

private data class UsuarioSimulado(val id: Int, val nombre: String)

/**
 * Una función `suspend` que simula una llamada de red.
 *
 * No hay red de por medio: `delay` sólo pausa la corrutina el tiempo indicado.
 */
private suspend fun obtenerUsuario(id: Int): String {
    delay(30)       // aquí es donde la corrutina se pausa y cede el hilo
    return UsuarioSimulado(id, "usuario-$id").nombre
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `delay(200)` por `Thread.sleep(200)` en la primera versión y mira los tiempos.
//  2. Sube las 50.000 corrutinas a 500.000 y comprueba que sigue funcionando.
//  3. Intenta llamar a `obtenerUsuario(1)` fuera de un bloque de corrutina y lee el error.
//  4. Quita `delay` de `obtenerUsuario` y observa el aviso de que `suspend` sobra.
