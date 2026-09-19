package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// =====================================================================================
//  28.2 · launch, async, Job y Deferred
//
//  QUÉ ES
//    Las dos formas de arrancar una corrutina: `launch` para "haz esto" (no devuelve
//    resultado) y `async` para "calcula esto" (devuelve un `Deferred<T>`).
//
//  POR QUÉ IMPORTA
//    La elección entre secuencial y paralelo se reduce a DÓNDE pones el `await`. Y
//    la diferencia entre `launch` y `async` decide además cómo se propagan los
//    errores (demo 28.25).
//
//  ERRORES COMUNES
//    · `async { }.await()` seguido, que es exactamente igual de secuencial.
//    · Usar `async` cuando no necesitas el resultado (usa `launch`).
//    · Olvidar `await` en un `async` y que la excepción se pierda en silencio.
// =====================================================================================

/**
 * `launch`: dispara y sigue.
 */
fun demoLaunch() {
    section("Arranca una corrutina y devuelve un Job")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            traza.add("dentro: empiezo")
            delay(100)
            traza.add("dentro: termino")
        }

        traza.add("fuera: launch ya ha vuelto")
        trabajo.join()                  // esperamos a que acabe
        traza.add("fuera: tras join()")

        traza.forEach { bullet(it) }
    }

    bullet("`launch` NO espera: vuelve inmediatamente con un Job.")
    bullet("`join()` es lo que espera a que termine.")

    section("El Job y su estado")

    runBlocking {
        val trabajo = launch {
            delay(50)
        }

        show("isActive antes de terminar", trabajo.isActive)
        show("isCompleted antes de terminar", trabajo.isCompleted)

        trabajo.join()

        show("isActive después", trabajo.isActive)
        show("isCompleted después", trabajo.isCompleted)
        show("isCancelled", trabajo.isCancelled)
    }

    section("Varios a la vez")

    runBlocking {
        val resultados = mutableListOf<Int>()

        val trabajos = (1..5).map { numero ->
            launch {
                delay((6L - numero) * 20)       // los últimos terminan antes
                synchronized(resultados) { resultados.add(numero) }
            }
        }

        trabajos.joinAll()
        show("orden de finalización", resultados)
    }

    bullet("`joinAll()` espera a todos. El orden de finalización NO es el de arranque.")
    bullet("Ese orden depende de los tiempos: nunca escribas código que dependa de él.")
}

/**
 * `async`: calcula y devuelve.
 */
fun demoAsync() {
    section("Devuelve un Deferred<T>")

    runBlocking {
        val diferido: Deferred<String> = async {
            delay(50)
            "resultado calculado"
        }

        show("tipo devuelto", "Deferred<String>")
        show("await()", diferido.await())
    }

    bullet("`Deferred<T>` es un `Job` que además tiene un valor: es una promesa.")
    bullet("`await()` suspende hasta que el valor esté listo.")

    section("Varios resultados")

    runBlocking {
        val uno = async { delay(50); 1 }
        val dos = async { delay(50); 2 }
        val tres = async { delay(50); 3 }

        show("suma de los tres", uno.await() + dos.await() + tres.await())
        show("con awaitAll", listOf(uno, dos, tres).awaitAll())
    }

    bullet("`awaitAll()` espera a todos y devuelve la lista de resultados.")
    bullet("Si uno falla, `awaitAll` propaga esa excepción.")

    section("launch o async: cómo elegir")

    bullet("¿Necesitas el resultado? → async")
    bullet("¿Sólo quieres que se haga? → launch")
    bullet("Usar `async` y no llamar a `await` es un error: la excepción se traga.")
}

/**
 * Secuencial frente a paralelo: dónde poner el `await`.
 */
fun demoSequentialVsParallel() {
    section("Tres operaciones de 200 ms cada una")

    section("1. Secuencial: una detrás de otra")

    val secuencial = measureTime {
        runBlocking {
            val a = cargarDato("A")
            val b = cargarDato("B")
            val c = cargarDato("C")
            show("resultado", listOf(a, b, c))
        }
    }
    show("tiempo", "~600 ms")
    show("¿más de 500 ms?", secuencial.inWholeMilliseconds > 500)

    bullet("Cada `cargarDato` suspende hasta terminar antes de empezar la siguiente.")
    bullet("A veces es lo que quieres: si B necesita el resultado de A.")

    section("2. Paralelo: async y await al final")

    val paralelo = measureTime {
        runBlocking {
            val a = async { cargarDato("A") }
            val b = async { cargarDato("B") }
            val c = async { cargarDato("C") }
            show("resultado", listOf(a.await(), b.await(), c.await()))
        }
    }
    show("tiempo", "~200 ms")
    show("¿menos de 500 ms?", paralelo.inWholeMilliseconds < 500)

    bullet("Las tres arrancan, y luego se espera a las tres. Tardan lo que la más lenta.")

    section("3. El error: async con await inmediato")

    val falsoParalelo = measureTime {
        runBlocking {
            val a = async { cargarDato("A") }.await()
            val b = async { cargarDato("B") }.await()
            val c = async { cargarDato("C") }.await()
            show("resultado", listOf(a, b, c))
        }
    }
    show("tiempo", "~600 ms otra vez")
    show("¿más de 500 ms?", falsoParalelo.inWholeMilliseconds > 500)

    bullet("`async { }.await()` en la misma línea es EXACTAMENTE igual de secuencial.")
    bullet("El paralelismo viene de arrancar TODAS antes de esperar a NINGUNA.")
    bullet("Es el error más común del capítulo: parece asíncrono y no lo es.")

    section("4. Con una lista de tareas")

    val conLista = measureTime {
        runBlocking {
            val resultados = listOf("A", "B", "C")
                .map { async { cargarDato(it) } }   // primero arrancar todas...
                .awaitAll()                          // ...y luego esperar
            show("resultado", resultados)
        }
    }
    show("¿menos de 500 ms?", conLista.inWholeMilliseconds < 500)

    bullet("Fíjate en los dos pasos separados: `map { async { } }` y luego `awaitAll()`.")
    bullet("Si escribieras `map { async { }.await() }`, volverías a lo secuencial.")
}

/**
 * `start = LAZY` y otros detalles del Job.
 */
fun demoJobDetails() {
    section("Un Deferred se puede arrancar más tarde")

    runBlocking {
        val perezoso = async(start = kotlinx.coroutines.CoroutineStart.LAZY) {
            delay(30)
            "calculado sólo si alguien lo pide"
        }

        show("¿ha arrancado?", perezoso.isActive)
        show("al llamar a await(), arranca", perezoso.await())
    }

    bullet("Con `CoroutineStart.LAZY`, la corrutina espera a `await()` o `start()`.")
    bullet("Útil si el resultado puede no hacer falta.")

    section("Jerarquía de Jobs")

    runBlocking {
        val padre = launch {
            launch { delay(50) }
            launch { delay(50) }
            delay(10)
        }

        padre.join()
        show("el padre terminó", padre.isCompleted)
    }

    bullet("Un `launch` dentro de otro crea un Job HIJO.")
    bullet("El padre no se considera terminado hasta que TODOS sus hijos acaban,")
    bullet("aunque su propio cuerpo haya llegado al final. Eso es la concurrencia")
    bullet("estructurada, que es la demo siguiente.")

    section("invokeOnCompletion")

    runBlocking {
        val traza = mutableListOf<String>()
        val trabajo = launch {
            delay(30)
            traza.add("cuerpo terminado")
        }
        trabajo.invokeOnCompletion { causa ->
            traza.add("callback: causa=${causa?.let { it::class.simpleName } ?: "ninguna"}")
        }
        trabajo.join()
        traza.forEach { bullet(it) }
    }

    bullet("Se ejecuta al terminar, tanto si acabó bien como si falló o se canceló.")
    bullet("El parámetro `causa` dice cuál de las tres cosas pasó.")
}

/** Simula una operación que tarda 200 ms. No hay red: sólo `delay`. */
private suspend fun cargarDato(nombre: String): String {
    delay(200)
    return "dato-$nombre"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `map { async { } }.awaitAll()` por `map { async { }.await() }` y
//     compara los tiempos: es el error de la demo 3.
//  2. Quita el `join()` de la primera demo y observa que `runBlocking` espera igual
//     (por la concurrencia estructurada).
//  3. Haz que una de las tres tareas paralelas lance y mira qué pasa con las otras.
//  4. Usa `CoroutineStart.LAZY` y nunca llames a `await()`: comprueba que no se ejecuta.
