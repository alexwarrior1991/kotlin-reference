package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// =====================================================================================
//  28.9 · Operadores de Flow
//
//  QUÉ ES
//    Las funciones que transforman un Flow en otro (INTERMEDIAS) y las que lo
//    consumen y devuelven un valor (TERMINALES).
//
//  POR QUÉ IMPORTA
//    La regla de oro: las intermedias son perezosas y no ejecutan nada; sólo la
//    terminal pone la cadena en marcha. Es igual que en `Sequence` (capítulo 14),
//    y confundirlo lleva a preguntarse por qué "no se ejecuta el map".
//
//  ERRORES COMUNES
//    · Encadenar operadores y olvidar el terminal: no pasa nada, sin aviso.
//    · Usar try/catch alrededor del `collect` cuando lo que hace falta es `catch`.
//    · `emit` desde otro contexto dentro de `flow { }`: falla en ejecución.
//    · Poner `flowOn` al final creyendo que afecta al colector (no: afecta arriba).
// =====================================================================================

/**
 * Operadores intermedios: devuelven otro Flow.
 */
fun demoIntermediateOperators() {
    section("Son perezosos: sin terminal no se ejecuta nada")

    runBlocking {
        var vecesQueSeEjecuto = 0

        val cadena = (1..5).asFlow()
            .map { vecesQueSeEjecuto++; it * 2 }
            .filter { it > 4 }

        show("¿se ejecutó algo al construir la cadena?", vecesQueSeEjecuto)

        val resultado = cadena.toList()           // ← el terminal
        show("tras toList()", resultado)
        show("veces que corrió el map", vecesQueSeEjecuto)
    }

    section("Los básicos")

    runBlocking {
        val numeros = (1..6).asFlow()

        show("map { it * it }", numeros.map { it * it }.toList())
        show("filter { it % 2 == 0 }", numeros.filter { it % 2 == 0 }.toList())
        show("take(3)", numeros.take(3).toList())
        show("drop(4)", numeros.drop(4).toList())
        show("takeWhile { it < 4 }", numeros.takeWhile { it < 4 }.toList())
    }

    bullet("Los mismos nombres que en las colecciones. La diferencia: la lambda")
    bullet("puede ser `suspend`, así que dentro de un `map` puedes llamar a la red.")

    section("map con suspensión, que es el caso real")

    runBlocking {
        val enriquecidos = listOf(1, 2, 3).asFlow()
            .map { id -> ampliarConDetalle(id) }      // ← una `suspend fun` dentro
            .toList()
        show("map { ampliarConDetalle(it) }", enriquecidos)
    }

    section("onEach: mirar sin tocar")

    runBlocking {
        val traza = mutableListOf<String>()

        val resultado = (1..3).asFlow()
            .onEach { traza.add("antes: $it") }
            .map { it * 10 }
            .onEach { traza.add("  después: $it") }
            .toList()

        traza.forEach { bullet(it) }
        show("resultado", resultado)
    }

    bullet("`onEach` devuelve el mismo valor: sirve para registrar o depurar.")
    bullet("Fíjate en que se intercalan: cada elemento recorre la cadena ENTERA")
    bullet("antes de que empiece el siguiente. Igual que en las secuencias.")

    section("transform: el operador general")

    runBlocking {
        // `transform` puede emitir cero, uno o varios valores por cada entrada.
        val duplicados = (1..3).asFlow().transform { valor ->
            emit("$valor-a")
            emit("$valor-b")
        }
        show("dos emisiones por entrada", duplicados.toList())

        val soloPares = (1..6).asFlow().transform { valor ->
            if (valor % 2 == 0) emit(valor)        // cero emisiones para los impares
        }
        show("cero emisiones para los impares", soloPares.toList())
    }

    bullet("`map` y `filter` están implementados con `transform`. Si necesitas algo")
    bullet("que no encaja en ninguno de los dos, `transform` es la salida.")

    section("distinctUntilChanged: quitar repetidos SEGUIDOS")

    runBlocking {
        val conRepetidos = flowOf(1, 1, 2, 2, 2, 3, 1, 1)
        show("original", conRepetidos.toList())
        show("distinctUntilChanged()", conRepetidos.distinctUntilChanged().toList())
    }

    bullet("Ojo: quita los CONSECUTIVOS, no los duplicados globales. El 1 del final")
    bullet("vuelve a aparecer. Para un estado de interfaz es justo lo que quieres:")
    bullet("no repintar si el valor no ha cambiado respecto al anterior.")

    section("Combinar dos flujos: zip y combine")

    runBlocking {
        val letras = flowOf("a", "b", "c").onEach { delay(30) }
        val numeros = flowOf(1, 2, 3).onEach { delay(50) }

        show("zip", letras.zip(numeros) { l, n -> "$l$n" }.toList())
    }

    bullet("`zip` empareja por POSICIÓN y espera a los dos: 1º con 1º, 2º con 2º.")
    bullet("Termina cuando se agota el más corto.")

    runBlocking {
        val rapido = flow { emit("r1"); delay(40); emit("r2") }
        val lento = flow { delay(20); emit("L1"); delay(60); emit("L2") }

        show("combine", rapido.combine(lento) { a, b -> "$a+$b" }.toList())
    }

    bullet("`combine` emite cada vez que cambia CUALQUIERA de los dos, usando el")
    bullet("último valor conocido del otro. Es el operador de 'estado combinado':")
    bullet("filtro + orden + página → consulta.")

    section("Los que se mencionan pero no se ejecutan")

    bullet("`flatMapConcat`, `flatMapMerge`, `flatMapLatest` → un Flow por cada")
    bullet("elemento, aplanados de tres formas distintas (en orden, en paralelo,")
    bullet("cancelando el anterior). `flatMapLatest` es EL operador de un buscador.")
    bullet("`debounce`, `sample` → filtrar por tiempo.")
    bullet("Aquí no se ejecutan porque todavía piden una anotación de opt-in.")
}

/**
 * Operadores terminales: consumen el flujo.
 */
fun demoTerminalOperators() {
    section("collect: el básico")

    runBlocking {
        val recogidos = mutableListOf<Int>()
        (1..3).asFlow().collect { recogidos.add(it) }
        show("collect { }", recogidos)
    }

    section("Los que devuelven una colección")

    runBlocking {
        show("toList()", flowOf(3, 1, 2, 1).toList())
        show("count()", flowOf(3, 1, 2, 1).count())
    }

    section("Los que devuelven un elemento")

    runBlocking {
        show("first()", (1..5).asFlow().first())
        show("first { it > 3 }", (1..5).asFlow().first { it > 3 })
        show("firstOrNull { it > 99 }", (1..5).asFlow().firstOrNull { it > 99 })
    }

    bullet("`first()` CANCELA el flujo en cuanto tiene su valor: no espera al resto.")
    bullet("Por eso es seguro con flujos infinitos, y `single()` no lo es.")

    section("Los que agregan")

    runBlocking {
        show("reduce { a, b -> a + b }", (1..5).asFlow().reduce { a, b -> a + b })
        show("fold(100) { a, b -> a + b }", (1..5).asFlow().fold(100) { a, b -> a + b })
    }

    bullet("`reduce` falla con un flujo vacío; `fold` no, porque tiene valor inicial.")

    section("El error silencioso")

    runBlocking {
        var seEjecuto = false

        // Sin terminal, esto no hace NADA. Ni error, ni aviso en ejecución.
        (1..3).asFlow().map { seEjecuto = true; it }

        show("cadena sin terminal: ¿se ejecutó?", seEjecuto)

        // El compilador sí avisa: "The expression is unused".
        bullet("IntelliJ lo marca como expresión sin usar. Hazle caso.")
    }

    section("Cuál usar")

    bullet("¿Necesitas todos los valores?          → toList()")
    bullet("¿Sólo el primero que cumpla algo?      → first { } / firstOrNull { }")
    bullet("¿Procesarlos según llegan?             → collect { }")
    bullet("¿Un único valor agregado?              → reduce / fold / count")
    bullet("¿Quedarte sólo con el último de cada ráfaga? → collectLatest (demo 28.36)")
}

/**
 * Excepciones dentro de un Flow.
 */
fun demoFlowExceptions() {
    section("1. try/catch alrededor del collect: funciona")

    runBlocking {
        val salida = try {
            flujoQueFalla().collect { }
            "no lanzó"
        } catch (e: IllegalStateException) {
            "capturada fuera: ${e.message}"
        }
        show("try alrededor del collect", salida)
    }

    bullet("Funciona, pero mezcla el manejo del error con el consumo del flujo.")

    section("2. El operador catch: la forma idiomática")

    runBlocking {
        val recogidos = mutableListOf<String>()

        flujoQueFalla()
            .catch { fallo -> recogidos.add("catch: ${fallo.message}") }
            .collect { recogidos.add("valor $it") }

        recogidos.forEach { bullet(it) }
    }

    bullet("`catch` captura lo que ocurra AGUAS ARRIBA de él, nunca aguas abajo.")
    bullet("Es declarativo: el manejo del error viaja con la cadena, no con el colector.")

    section("3. catch puede emitir un valor de repuesto")

    runBlocking {
        val conRepuesto = flujoQueFalla()
            .map { "ok-$it" }
            .catch { emit("valor por defecto") }       // ← `catch` puede emitir
            .toList()
        show("catch { emit(...) }", conRepuesto)
    }

    section("4. La posición de catch importa")

    runBlocking {
        // El `map` que falla está DEBAJO del catch: éste no lo ve.
        val salida = try {
            (1..3).asFlow()
                .catch { bullet("esto no se ejecuta") }
                .map { if (it == 2) error("falla el map") else it }
                .toList()
                .toString()
        } catch (e: IllegalStateException) {
            "el fallo salió por fuera: ${e.message}"
        }
        show("catch colocado demasiado arriba", salida)
    }

    bullet("Colócalo SIEMPRE al final de la parte que quieres proteger.")

    section("5. onCompletion: el finally de los flujos")

    runBlocking {
        val traza = mutableListOf<String>()

        flowOf(1, 2, 3)
            .onCompletion { causa -> traza.add("completado, causa=${causa ?: "ninguna"}") }
            .collect { traza.add("valor $it") }

        flujoQueFalla()
            .onCompletion { causa -> traza.add("completado con ${causa?.message}") }
            .catch { }
            .collect { }

        traza.forEach { bullet(it) }
    }

    bullet("Se ejecuta siempre: al terminar bien, al fallar y al cancelar.")
    bullet("El parámetro dice cuál de las tres cosas fue. Para cerrar recursos,")
    bullet("parar un cronómetro o registrar el final, es lo que quieres.")

    section("6. retry: reintentar la cadena entera")

    runBlocking {
        var intentos = 0

        val conReintentos = flow {
            intentos++
            if (intentos < 3) error("fallo transitorio nº $intentos")
            emit("conseguido en el intento $intentos")
        }.retry(retries = 3) { fallo ->
            // Devolver `true` reintenta; `false` deja pasar la excepción.
            fallo is IllegalStateException
        }

        show("retry(3)", conReintentos.first())
        show("intentos gastados", intentos)
    }

    bullet("`retry` vuelve a ejecutar el flujo DESDE EL PRINCIPIO. Con flujos que")
    bullet("ya han emitido valores, el colector los verá repetidos: tenlo en cuenta.")

    section("7. La transparencia de excepciones")

    runBlocking {
        // Capturar dentro del propio `flow { }` el fallo del COLECTOR está prohibido.
        val salida = try {
            flow {
                try {
                    emit(1)
                } catch (e: Throwable) {
                    // El colector falló y aquí nos lo tragamos: eso rompe la cadena.
                }
                emit(2)              // ← al volver a emitir, la librería lo detecta
            }.collect { valor ->
                if (valor == 1) error("falla el colector")
            }
            "no lanzó"
        } catch (e: IllegalStateException) {
            "${e::class.simpleName}: ${e.message?.lineSequence()?.firstOrNull()}"
        }
        show("try/catch alrededor de emit", salida)
    }

    bullet("La librería lo detecta y lanza: un Flow debe dejar pasar hacia arriba")
    bullet("los fallos del colector. Si necesitas proteger tu código, ponlo FUERA")
    bullet("del `emit`, o usa el operador `catch`.")
}

/**
 * Contexto y contrapresión.
 */
fun demoFlowContextAndBuffering() {
    section("La regla: el flujo se ejecuta en el contexto del colector")

    runBlocking {
        flow {
            show("el flujo corre en", Thread.currentThread().name.take(45))
            emit(1)
        }.collect {
            show("el colector corre en", Thread.currentThread().name.take(45))
        }
    }

    bullet("Por defecto, productor y colector comparten corrutina y, con ella, hilo.")

    section("Cambiar de contexto a mano está prohibido")

    runBlocking {
        val salida = try {
            flow {
                // PROHIBIDO: emitir desde otra corrutina rompe la cadena.
                kotlinx.coroutines.withContext(Dispatchers.Default) { emit(1) }
            }.collect { }
            "no lanzó"
        } catch (e: IllegalStateException) {
            "${e::class.simpleName}: ${e.message?.lineSequence()?.firstOrNull()}"
        }
        show("withContext alrededor de emit", salida)
    }

    bullet("La librería lo detecta y lo prohíbe expresamente.")

    section("flowOn: la forma correcta")

    runBlocking {
        flow {
            show("el productor corre en", Thread.currentThread().name.take(45))
            emit(1)
        }
            .flowOn(Dispatchers.Default)        // ← afecta a lo que hay ARRIBA
            .collect {
                show("el colector corre en", Thread.currentThread().name.take(45))
            }
    }

    bullet("`flowOn` cambia el contexto de todo lo que está ENCIMA de él en la cadena.")
    bullet("Lo de debajo, y el colector, siguen donde estaban. Por eso se lee de")
    bullet("abajo arriba: 'esto de aquí para arriba, en Default'.")

    section("Contrapresión: por defecto se alternan")

    runBlocking {
        val sinBuffer = measureTime {
            flowOf(1, 2, 3).onEach { delay(40) }        // producir cuesta 40 ms
                .collect { delay(40) }                   // consumir cuesta 40 ms
        }
        show("sin buffer", "${sinBuffer.inWholeMilliseconds} ms (≈ 3 × 80)")
    }

    bullet("Producir y consumir se turnan: el total es la SUMA de los dos tiempos.")

    section("buffer: producir y consumir a la vez")

    runBlocking {
        val conBuffer = measureTime {
            flowOf(1, 2, 3).onEach { delay(40) }
                .buffer()                                // ← corrutinas separadas
                .collect { delay(40) }
        }
        show("con buffer()", "${conBuffer.inWholeMilliseconds} ms: se solapan")
    }

    bullet("`buffer` ejecuta productor y colector en corrutinas distintas, con una")
    bullet("cola en medio. Ahora se solapan y el total baja al del más lento.")

    section("conflate: quédate con el último")

    runBlocking {
        val recogidos = mutableListOf<Int>()

        flowOf(1, 2, 3, 4, 5).onEach { delay(20) }
            .conflate()                                  // descarta los intermedios
            .collect { valor ->
                recogidos.add(valor)
                delay(80)                                // el colector es lento
            }

        show("conflate()", recogidos)
    }

    bullet("Los valores que llegan mientras el colector está ocupado se DESCARTAN,")
    bullet("quedándose sólo el último. Perfecto para un indicador de progreso o una")
    bullet("posición del ratón: no quieres los intermedios, quieres el actual.")

    section("collectLatest: cancela el procesado anterior")

    runBlocking {
        val terminados = mutableListOf<Int>()

        flowOf(1, 2, 3).onEach { delay(20) }
            .collectLatest { valor ->
                delay(50)                                // tarda más que la emisión
                terminados.add(valor)                    // sólo llega el último
            }

        show("collectLatest", terminados)
    }

    bullet("Cada valor nuevo CANCELA el procesado del anterior a medias.")
    bullet("Es el operador de un buscador: al teclear otra letra, la búsqueda")
    bullet("anterior deja de tener sentido y se cancela.")

    section("Cuál elegir")

    bullet("buffer()       → los quiero todos, pero sin turnarme con el productor.")
    bullet("conflate()     → sólo me importa el último valor. Descarta los demás.")
    bullet("collectLatest  → procesa el último; si llega otro, cancela y empieza.")
    bullet("(nada)         → contrapresión natural: el productor espera al colector.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/** Simula enriquecer un identificador con datos de otro servicio. */
private suspend fun ampliarConDetalle(id: Int): String {
    delay(10)
    return "id=$id/detalle"
}

/** Un flujo que emite dos valores y luego falla. */
private fun flujoQueFalla(): Flow<Int> = flow {
    emit(1)
    emit(2)
    error("el flujo se rompió en el tercero")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `zip` por `combine` en los dos flujos con delays y compara el resultado.
//  2. Mueve el `catch` de sitio en la cadena y comprueba qué deja de capturar.
//  3. Sustituye `conflate()` por `buffer()` en la penúltima demo y mira qué cambia.
//  4. Pon `flowOn(Dispatchers.IO)` en distintos puntos de una cadena y observa qué
//     parte cambia de hilo cada vez.
