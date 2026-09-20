package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

// =====================================================================================
//  28.7 · Excepciones en corrutinas
//
//  QUÉ ES
//    Cómo viaja un fallo por el árbol de corrutinas: quién lo recibe, a quién cancela
//    y dónde se puede capturar.
//
//  POR QUÉ IMPORTA
//    Es la parte de las corrutinas donde más se equivoca todo el mundo, porque las
//    reglas NO son las de un try/catch normal: un `launch` que falla no lanza en el
//    sitio donde lo escribiste, sino en su padre.
//
//  ERRORES COMUNES
//    · `try { launch { ... } } catch` — no captura nada: `launch` ya volvió.
//    · Esperar que `CoroutineExceptionHandler` funcione con `async` (no lo hace).
//    · Pasar un `SupervisorJob()` a `launch` creyendo que protege a los hermanos:
//      lo que hace es romper la concurrencia estructurada.
//    · `runCatching` alrededor de código suspendido: se traga la cancelación.
// =====================================================================================

/**
 * La diferencia fundamental entre `launch` y `async`.
 */
fun demoLaunchVsAsyncErrors() {
    section("launch: la excepción sube al padre EN CUANTO ocurre")

    runBlocking {
        val traza = mutableListOf<String>()

        try {
            coroutineScope {
                launch { delay(10); error("el launch falló") }
                launch { delay(200); traza.add("el hermano terminó") }
            }
        } catch (e: IllegalStateException) {
            traza.add("capturada en el coroutineScope: ${e.message}")
        }

        traza.add("¿el hermano llegó a terminar? ${"el hermano terminó" in traza}")
        traza.forEach { bullet(it) }
    }

    bullet("Nadie llamó a nada: la excepción viajó sola hasta el ámbito y canceló")
    bullet("al hermano por el camino. Eso es lo que hace `launch`.")

    section("async: la excepción espera guardada hasta el await")

    runBlocking {
        val salida = try {
            supervisorScope {
                val diferido = async<String> { delay(10); error("el async falló") }

                delay(60)
                bullet("han pasado 60 ms desde el fallo y no ha saltado nada")

                try {
                    diferido.await()
                } catch (e: IllegalStateException) {
                    "capturada en await(): ${e.message}"
                }
            }
        } catch (e: IllegalStateException) {
            "salió del supervisorScope: ${e.message}"
        }
        show("resultado", salida)
    }

    bullet("El `Deferred` guarda la excepción y la relanza cuando pides el valor.")
    bullet("Piensa en `await()` como en abrir el sobre: hasta entonces no explota.")

    section("Y si nunca llamas a await, se pierde")

    runBlocking {
        val salida = try {
            supervisorScope {
                val olvidado = async<String> { delay(10); error("nadie se entera") }
                delay(60)

                show("¿el Deferred ha terminado?", olvidado.isCompleted)
                show("¿ha terminado con error?", olvidado.isCancelled)
                "el programa siguió como si nada"
            }
        } catch (e: IllegalStateException) {
            "propagó: ${e.message}"
        }
        show("resultado", salida)
    }

    bullet("Ni excepción, ni log, ni rastro. Un `async` sin `await` es un error")
    bullet("silencioso: si no vas a usar el resultado, usa `launch`.")

    section("La tabla")

    bullet("launch  → falla YA, cancela a sus hermanos, sube al ámbito.")
    bullet("          Si nadie lo captura → CoroutineExceptionHandler (demo 28.26).")
    bullet("async   → falla al `await()`. Si hay ámbito normal, además lo cancela.")
    bullet("          Sin `await`, la excepción desaparece.")
}

/**
 * `CoroutineExceptionHandler`: la red de última hora.
 */
fun demoExceptionHandler() {
    section("Qué es")

    bullet("Una pieza del contexto que recibe las excepciones que nadie capturó.")
    bullet("Es el equivalente a `Thread.setUncaughtExceptionHandler`.")
    bullet("Sirve para REGISTRAR el fallo, no para recuperarse de él.")

    val capturadas = mutableListOf<String>()
    val manejador = CoroutineExceptionHandler { contexto, fallo ->
        val quien = contexto[CoroutineName]?.name ?: "sin nombre"
        synchronized(capturadas) {
            capturadas.add("$quien → ${fallo::class.simpleName}: ${fallo.message}")
        }
    }

    section("1. Con launch en la raíz del ámbito: funciona")

    runBlocking {
        val ambito = CoroutineScope(SupervisorJob() + manejador)
        ambito.launch(CoroutineName("tarea-A")) { delay(10); error("A falló") }.join()
        ambito.cancel()
    }

    capturadas.forEach { bullet(it) }

    section("2. Con async: NO funciona")

    runBlocking {
        val ambito = CoroutineScope(SupervisorJob() + manejador)
        val diferido = ambito.async<String> { delay(10); error("B falló") }
        delay(60)

        show("¿el manejador recibió algo de B?", capturadas.any { "B falló" in it })

        val enAwait = try {
            diferido.await()
        } catch (e: IllegalStateException) {
            "en await(): ${e.message}"
        }
        show("dónde aparece el error de B", enAwait)
        ambito.cancel()
    }

    bullet("Con `async` la excepción es TUYA: el manejador ni se entera.")

    section("3. En un hijo que no es la raíz: se ignora")

    val delHijo = mutableListOf<String>()
    val manejadorDelHijo = CoroutineExceptionHandler { _, _ ->
        synchronized(delHijo) { delHijo.add("lo recogió el manejador del hijo") }
    }

    runBlocking {
        val ambito = CoroutineScope(SupervisorJob() + manejador)
        ambito.launch(CoroutineName("raíz")) {
            // Este manejador está en un hijo, no en la raíz: se ignora.
            launch(manejadorDelHijo) { delay(10); error("C falló") }
        }.join()
        ambito.cancel()
    }

    show("¿lo recogió el manejador del hijo?", delHijo.isNotEmpty())
    show("¿lo recogió el manejador de la raíz?", capturadas.any { "C falló" in it })

    bullet("El fallo subió hasta la raíz y allí se consultó SU manejador.")

    section("La regla")

    bullet("El manejador sólo se consulta en la corrutina RAÍZ: la que es hija")
    bullet("directa de un CoroutineScope o de un SupervisorJob.")
    bullet("En cualquier otro punto del árbol se ignora, aunque lo pongas.")
    bullet("Y nunca con `async`.")

    section("Cuándo usarlo de verdad")

    bullet("Para registrar en el log y avisar: 'esta tarea de fondo se ha caído'.")
    bullet("NO para recuperarte: cuando el manejador se ejecuta, la corrutina ya")
    bullet("está muerta y sus hermanas, según el Job, también.")
    bullet("Si quieres recuperarte, try/catch donde ocurre el fallo.")
}

/**
 * `Job` frente a `SupervisorJob`.
 */
fun demoSupervisorJob() {
    val fallos = mutableListOf<String>()
    val manejador = CoroutineExceptionHandler { _, fallo ->
        synchronized(fallos) { fallos.add(fallo.message ?: "?") }
    }

    section("Con Job() normal: uno falla y caen todos")

    val conJobNormal = mutableListOf<String>()
    runBlocking {
        val ambito = CoroutineScope(Job() + manejador)
        val tareas = listOf(
            ambito.launch { delay(80); registrar(conJobNormal, "A terminó") },
            ambito.launch { delay(10); error("B falló") },
            ambito.launch { delay(80); registrar(conJobNormal, "C terminó") },
        )
        tareas.joinAll()
        ambito.cancel()
    }
    show("terminaron", conJobNormal.sorted())

    bullet("B canceló el Job del ámbito, y con él a A y a C.")

    section("Con SupervisorJob(): cada hijo va por su cuenta")

    val conSupervisor = mutableListOf<String>()
    runBlocking {
        val ambito = CoroutineScope(SupervisorJob() + manejador)
        val tareas = listOf(
            ambito.launch { delay(80); registrar(conSupervisor, "A terminó") },
            ambito.launch { delay(10); error("B falló") },
            ambito.launch { delay(80); registrar(conSupervisor, "C terminó") },
        )
        tareas.joinAll()
        ambito.cancel()
    }
    show("terminaron", conSupervisor.sorted())
    show("fallos registrados por el manejador", fallos.size)

    bullet("Mismo código, un solo cambio, resultado opuesto.")

    section("Ojo: el supervisor sólo protege HACIA ABAJO")

    bullet("Cancelar el SupervisorJob cancela a todos sus hijos, como siempre.")
    bullet("Lo que no ocurre es lo contrario: un hijo que falla no tumba al padre.")

    section("El error clásico: SupervisorJob dentro de launch")

    runBlocking {
        val traza = mutableListOf<String>()

        coroutineScope {
            // MAL: al pasar un Job nuevo, esta corrutina deja de ser hija del ámbito.
            launch(SupervisorJob()) {
                delay(100)
                registrar(traza, "la huérfana terminó")
            }
            traza.add("el coroutineScope ya puede volver: no la cuenta como hija")
        }

        traza.add("tras coroutineScope: ¿terminó? ${"la huérfana terminó" in traza}")
        delay(200)
        traza.add("200 ms más tarde: ¿terminó? ${"la huérfana terminó" in traza}")

        traza.forEach { bullet(it) }
    }

    bullet("`launch(SupervisorJob())` NO 'hace supervisor' al ámbito: le pone a la")
    bullet("corrutina un padre nuevo, sin relación con el ámbito. Resultado: nadie")
    bullet("la espera, nadie la cancela. Es una fuga, aunque el código parezca correcto.")

    section("Lo que sí se hace")

    bullet("¿Quieres un ámbito supervisor? → CoroutineScope(SupervisorJob() + ...)")
    bullet("¿Quieres un bloque supervisor? → supervisorScope { ... }")
    bullet("Nunca pases un Job como contexto a `launch` o `async`.")
}

/**
 * Dónde poner el try/catch.
 */
fun demoTryCatchInCoroutines() {
    val ignorados = mutableListOf<String>()
    val manejador = CoroutineExceptionHandler { _, fallo ->
        synchronized(ignorados) { ignorados.add(fallo.message ?: "?") }
    }

    section("1. try/catch ALREDEDOR de launch: no captura nada")

    runBlocking {
        val traza = mutableListOf<String>()
        val ambito = CoroutineScope(SupervisorJob() + manejador)

        try {
            ambito.launch { delay(10); error("falla más tarde") }
            traza.add("el try terminó sin capturar nada")
        } catch (e: IllegalStateException) {
            traza.add("esto NUNCA se ejecuta")
        }

        delay(60)
        ambito.cancel()
        traza.forEach { bullet(it) }
    }

    bullet("`launch` devuelve inmediatamente: cuando el fallo ocurre, el `try` ya")
    bullet("hace rato que terminó. El catch no tiene nada que hacer ahí.")

    section("2. try/catch DENTRO de la corrutina: sí funciona")

    runBlocking {
        val traza = mutableListOf<String>()

        launch {
            try {
                delay(10)
                error("falla dentro")
            } catch (e: IllegalStateException) {
                registrar(traza, "capturada dentro: ${e.message}")
            }
        }.join()

        traza.forEach { bullet(it) }
    }

    bullet("Aquí el try/catch está en el mismo sitio donde ocurre el fallo.")
    bullet("Es lo normal y lo que debes hacer el 90% de las veces.")

    section("3. try/catch alrededor de coroutineScope o de await: sí")

    runBlocking {
        val porScope = try {
            coroutineScope { launch { delay(10); error("desde dentro del scope") } }
            "no lanzó"
        } catch (e: IllegalStateException) {
            "capturada: ${e.message}"
        }
        show("try alrededor de coroutineScope", porScope)

        val porAwait = try {
            supervisorScope {
                async<String> { delay(10); error("desde dentro del async") }.await()
            }
        } catch (e: IllegalStateException) {
            "capturada: ${e.message}"
        }
        show("try alrededor de await()", porAwait)
    }

    bullet("`coroutineScope` y `await()` SUSPENDEN, así que relanzan en tu línea:")
    bullet("un try/catch normal alrededor funciona exactamente como esperas.")

    section("4. La trampa de runCatching")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            repeat(5) { i ->
                runCatching { delay(20) }      // ← se traga la CancellationException
                registrar(traza, "vuelta $i")
            }
        }

        delay(30)
        trabajo.cancelAndJoin()

        show("vueltas completadas pese a cancelar", traza.size)
    }

    bullet("`runCatching` captura `Throwable`, y CancellationException lo es.")
    bullet("Dentro de una corrutina, rompe la cancelación igual que un catch genérico.")
    bullet("Regla: `runCatching` sólo alrededor de código NO suspendido.")

    section("Resumen de dónde va el try/catch")

    bullet("Dentro de launch/async           → donde ocurre el fallo. Lo habitual.")
    bullet("Alrededor de coroutineScope      → para todo un grupo de tareas.")
    bullet("Alrededor de await()             → para el resultado de un async.")
    bullet("Alrededor de launch              → INÚTIL. No captura nada.")
    bullet("CoroutineExceptionHandler        → red de última hora, sólo para el log.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/**
 * Añade una línea a una lista compartida entre varias corrutinas.
 *
 * `MutableList` no es segura para hilos, y `Dispatchers.Default` usa varios. El
 * `synchronized` cuesta casi nada aquí y evita una corrupción difícil de depurar.
 */
private fun registrar(destino: MutableList<String>, linea: String) {
    synchronized(destino) { destino.add(linea) }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `Job()` por `SupervisorJob()` en la primera demo del ámbito y compara.
//  2. Quita el `manejador` de un ámbito con un launch que falla: verás la traza
//     completa en la salida de error, que es lo que hace el manejador por defecto.
//  3. Sustituye `runCatching { delay(20) }` por try/catch con relanzado y comprueba
//     que la cancelación vuelve a funcionar.
//  4. Pon un `CoroutineExceptionHandler` en un `async` y confirma que nunca se llama.
