package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// =====================================================================================
//  28.8 · Flow: qué es y cómo se construye
//
//  QUÉ ES
//    Un `Flow<T>` es una secuencia de valores que se producen a lo largo del tiempo y
//    que puede SUSPENDER entre uno y otro. Si una `suspend fun` devuelve un valor, un
//    `Flow` devuelve muchos.
//
//  POR QUÉ IMPORTA
//    Es la pieza que falta entre las corrutinas y las colecciones: todo lo que sea
//    "me van llegando cosas" (resultados paginados, eventos, lecturas de un sensor,
//    líneas de un fichero enorme) es un Flow.
//
//  LA COMPARACIÓN QUE LO ACLARA TODO
//    List<T>      → muchos valores, YA calculados, en memoria
//    Sequence<T>  → muchos valores, perezosos, pero SIN suspender
//    suspend fun  → UN valor, puede suspender
//    Flow<T>      → muchos valores, perezosos, pueden suspender   ← la esquina que faltaba
//
//  ERRORES COMUNES
//    · Creer que crear un Flow ya ejecuta algo (no: hasta el `collect`, no pasa nada).
//    · Recoger el mismo Flow dos veces esperando que el trabajo se comparta.
//    · Usar un Flow para un solo valor: ahí basta una `suspend fun`.
// =====================================================================================

/**
 * El concepto.
 */
fun demoWhatIsAFlow() {
    section("Un Flow es una receta, no un resultado")

    runBlocking {
        val traza = mutableListOf<String>()

        // Crear el flujo NO ejecuta nada: sólo guarda la receta.
        val numeros = flow {
            traza.add("el cuerpo del flow empieza")
            for (i in 1..3) {
                delay(20)           // aquí SÍ se puede suspender
                emit(i)             // `emit` entrega un valor a quien esté recogiendo
            }
            traza.add("el cuerpo del flow termina")
        }

        traza.add("flujo creado; todavía no ha pasado nada")
        show("¿el cuerpo ya se ejecutó?", "el cuerpo del flow empieza" in traza)

        // `collect` es lo que pone en marcha la receta.
        numeros.collect { valor -> traza.add("recibido $valor") }

        traza.forEach { bullet(it) }
    }

    bullet("Fíjate en el orden: 'flujo creado' sale ANTES de que el cuerpo arranque.")
    bullet("`collect` es el operador TERMINAL: sin él, un Flow no hace absolutamente nada.")

    section("emit y collect se alternan")

    runBlocking {
        val traza = mutableListOf<String>()

        flow {
            (1..3).forEach { i ->
                traza.add("emito $i")
                emit(i)
            }
        }.collect { valor ->
            traza.add("  proceso $valor")
            delay(10)
        }

        traza.forEach { bullet(it) }
    }

    bullet("No hay cola ni buffer por defecto: `emit` SUSPENDE hasta que quien recoge")
    bullet("termina con el valor anterior. Es contrapresión gratis (demo 28.36).")

    section("collect es una suspend fun")

    bullet("Por eso sólo se puede llamar desde una corrutina.")
    bullet("Y por eso `collect` no vuelve hasta que el flujo se agota.")
    bullet("Si el flujo es infinito, `collect` no vuelve NUNCA: hay que acotarlo")
    bullet("con `take(n)`, `first()` o cancelando la corrutina que lo recoge.")
}

/**
 * Las formas de crear un Flow.
 */
fun demoFlowBuilders() {
    section("1. flowOf: valores fijos")

    runBlocking {
        show("flowOf(1, 2, 3).toList()", flowOf(1, 2, 3).toList())
    }

    section("2. asFlow: desde algo que ya tienes")

    runBlocking {
        show("listOf(\"a\",\"b\").asFlow()", listOf("a", "b").asFlow().toList())
        show("(1..5).asFlow()", (1..5).asFlow().toList())
    }

    bullet("Útil para encajar datos que ya están en memoria en una cadena de Flow.")
    bullet("Si NO hay suspensión de por medio, una lista o una secuencia van mejor.")

    section("3. flow { }: el caso general")

    runBlocking {
        val paginas = flow {
            var pagina = 1
            while (pagina <= 3) {
                emit(descargarPagina(pagina))       // se puede suspender antes de emitir
                pagina++
            }
        }
        show("tres páginas", paginas.toList())
    }

    bullet("Es el único builder donde puedes llamar a funciones `suspend`.")
    bullet("Es el que usarás el 90% de las veces.")

    section("4. Flujos infinitos, siempre acotados")

    runBlocking {
        val reloj = flow {
            var tic = 0
            while (true) {              // infinito a propósito
                emit("tic ${tic++}")
                delay(10)
            }
        }

        // `take(4)` cancela el flujo en cuanto tiene sus cuatro valores.
        show("reloj.take(4)", reloj.take(4).toList())
        show("reloj.first()", reloj.first())
    }

    bullet("Un flujo infinito es perfectamente normal; lo que no puede faltar es")
    bullet("el operador que lo corta. `take`, `first` o la cancelación del colector.")

    section("5. Los que sólo se mencionan")

    bullet("`callbackFlow { }` → envuelve una API de callbacks (un listener, un")
    bullet("                     sensor, un WebSocket) y la convierte en Flow.")
    bullet("`channelFlow { }`  → permite emitir desde VARIAS corrutinas a la vez.")
    bullet("Los dos son el puente entre el mundo de los callbacks y el de Flow.")
    bullet("No se ejecutan aquí porque necesitan una fuente externa de eventos.")

    section("Qué NO es un builder de Flow")

    bullet("Una `suspend fun` que devuelve una lista NO es un Flow: devuelve todo de")
    bullet("golpe al final. Con Flow, quien recoge empieza a trabajar con el primer")
    bullet("elemento sin esperar al último.")
}

/**
 * Flujos fríos: cada colector arranca su propia ejecución.
 */
fun demoColdFlows() {
    section("Frío = la receta se ejecuta ENTERA por cada colector")

    runBlocking {
        var ejecuciones = 0

        val flujo = flow {
            ejecuciones++
            emit("valor")
        }

        flujo.collect { }
        flujo.collect { }
        flujo.collect { }

        show("veces que se ejecutó el cuerpo", ejecuciones)
    }

    bullet("Tres `collect` = tres ejecuciones completas. No se comparte nada.")

    section("Qué significa en la práctica")

    runBlocking {
        val consulta = flow {
            emit(consultarBaseDeDatosSimulada())
        }

        val tiempo = measureTime {
            // Cada colector paga su propia consulta.
            consulta.collect { }
            consulta.collect { }
        }
        show("dos collect de una consulta de 50 ms", "${tiempo.inWholeMilliseconds} ms")
    }

    bullet("Si el trabajo es caro, recogerlo dos veces lo hace dos veces.")
    bullet("Para COMPARTIR una ejecución entre varios colectores están los flujos")
    bullet("calientes: StateFlow y SharedFlow (demo 28.37).")

    section("Lo bueno de ser frío")

    bullet("El flujo no tiene estado: no hay valores perdidos ni recibidos a medias.")
    bullet("Cada colector ve la secuencia entera desde el principio.")
    bullet("Se puede recoger, cancelar y volver a recoger sin problemas.")
    bullet("Nada se ejecuta si nadie lo pide: cero trabajo desperdiciado.")

    section("Frío y caliente, en una frase")

    bullet("FRÍO    → el productor arranca cuando alguien recoge. Uno por colector.")
    bullet("CALIENTE → el productor ya está corriendo. Los colectores se asoman.")

    section("Cancelar un flujo frío")

    runBlocking {
        val traza = mutableListOf<String>()

        val trabajo = launch {
            flow {
                var i = 0
                while (true) {
                    emit(i++)
                    delay(20)
                }
            }.collect { valor ->
                synchronized(traza) { traza.add("recibido $valor") }
            }
        }

        delay(70)
        trabajo.cancelAndJoin()

        show("valores recibidos antes de cancelar", traza.size)
        show("¿el trabajo está cancelado?", trabajo.isCancelled)
    }

    bullet("Cancelar la corrutina que recoge para el flujo entero: el `emit` es un")
    bullet("punto de suspensión y respeta la cancelación como cualquier otro.")
}

/**
 * Flow frente a Sequence: cuándo cada uno.
 */
fun demoFlowVsSequence() {
    section("Los dos son perezosos, pero sólo uno puede suspender")

    runBlocking {
        // Sequence: perezosa, pero el bloque NO puede llamar a funciones suspend.
        val secuencia = sequence {
            yield(1)
            yield(2)
            // delay(10)  ← NO COMPILA: `delay` es suspend y aquí no se puede.
        }
        show("sequence { yield(1); yield(2) }", secuencia.toList())

        // Flow: perezoso Y puede suspender.
        val flujo = flow {
            emit(1)
            delay(10)          // ← esto sí compila
            emit(2)
        }
        show("flow { emit(1); delay(10); emit(2) }", flujo.toList())
    }

    bullet("Esa es LA diferencia. Todo lo demás se parece muchísimo.")

    section("Los operadores se llaman igual")

    runBlocking {
        val conSecuencia = (1..5).asSequence().map { it * 2 }.filter { it > 4 }.toList()
        val conFlujo = (1..5).asFlow().map { it * 2 }.filter { it > 4 }.toList()

        show("con Sequence", conSecuencia)
        show("con Flow", conFlujo)
    }

    bullet("`map`, `filter`, `take`, `first`, `toList`: los nombres son los mismos.")
    bullet("Lo que cambia es que en Flow la lambda puede ser `suspend`.")

    section("Cómo elegir")

    bullet("¿Los datos ya están en memoria y no hay esperas? → Sequence (o List).")
    bullet("¿Hay que esperar algo entre elemento y elemento? → Flow.")
    bullet("¿Un solo valor, aunque haya que esperar?          → suspend fun.")
    bullet("¿Muchos colectores sobre el mismo productor?      → SharedFlow/StateFlow.")

    section("El error de usar Flow de más")

    bullet("`flowOf(1, 2, 3).map { it * 2 }.toList()` funciona, pero es más lento y")
    bullet("más difícil de leer que `listOf(1, 2, 3).map { it * 2 }`.")
    bullet("Flow cuesta: máquina de estados, contexto, comprobaciones de cancelación.")
    bullet("Si no hay suspensión de por medio, no lo pagues.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/** Simula descargar una página de resultados. */
private suspend fun descargarPagina(numero: Int): String {
    delay(15)
    return "página-$numero"
}

/** Simula una consulta cara: 50 ms cada vez que se ejecuta. */
private suspend fun consultarBaseDeDatosSimulada(): String {
    delay(50)
    return "fila"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `take(4)` del reloj infinito y comprueba (con Ctrl+C a mano) que el
//     programa no termina nunca. Vuelve a ponerlo.
//  2. Añade un `delay(50)` dentro del `collect` y observa cómo se frena el `emit`.
//  3. Descomenta el `delay(10)` dentro de `sequence { }` y lee el error del compilador.
//  4. Recoge el mismo flujo desde dos corrutinas a la vez y cuenta las ejecuciones.
