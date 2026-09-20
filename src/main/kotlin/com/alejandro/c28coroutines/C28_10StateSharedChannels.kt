package com.alejandro.c28coroutines

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

// =====================================================================================
//  28.10 · StateFlow, SharedFlow y Channel
//
//  QUÉ ES
//    Las tres formas de compartir valores entre corrutinas que YA están corriendo,
//    frente al flujo frío del fichero anterior, que arranca cuando alguien lo recoge.
//
//  POR QUÉ IMPORTA
//    Un flujo frío no sirve para "el estado actual de la pantalla" ni para "los
//    eventos que van llegando": esas cosas existen aunque nadie las mire.
//
//  LA REGLA DE TRES
//    StateFlow  → un ESTADO. Siempre tiene un valor. Todos ven el mismo.
//    SharedFlow → EVENTOS. No guarda nada (salvo `replay`). Todos ven cada evento.
//    Channel    → una COLA. Cada elemento lo recibe UN solo consumidor.
//
//  ERRORES COMUNES
//    · Usar StateFlow para eventos: se pierden los repetidos y los intermedios.
//    · `MutableSharedFlow()` sin buffer: `emit` suspende si hay un colector lento.
//    · Exponer el `Mutable...` en la API pública en vez de la vista de sólo lectura.
//    · Usar un Channel cuando lo que querías era un SharedFlow (y al revés).
// =====================================================================================

/**
 * `StateFlow`: un valor observable.
 */
fun demoStateFlow() {
    section("Siempre tiene un valor")

    runBlocking {
        val contador = MutableStateFlow(0)

        show("value inicial", contador.value)
        contador.value = 5
        show("tras asignar 5", contador.value)
        show("first() devuelve el actual", contador.first())
    }

    bullet("A diferencia de un Flow normal, se puede leer sin recoger: `.value`.")
    bullet("Y nunca está vacío: hay que darle un valor inicial al construirlo.")

    section("Quien se suscribe recibe el valor actual, y luego los cambios")

    runBlocking {
        val contador = MutableStateFlow(5)
        val recibidos = mutableListOf<Int>()

        val trabajo = launch {
            contador.collect { registrar(recibidos, it) }
        }

        delay(20)                 // que le dé tiempo a suscribirse
        contador.value = 1
        delay(20)
        contador.value = 2
        delay(20)
        contador.value = 2        // el MISMO valor: no se emite
        delay(20)

        trabajo.cancelAndJoin()
        show("recibidos", recibidos)
    }

    bullet("El primero es el valor que había al suscribirse (5), no una emisión nueva.")
    bullet("El segundo 2 NO llega: StateFlow lleva `distinctUntilChanged` de serie.")
    bullet("Eso está muy bien para un estado y muy mal para un evento repetible.")

    section("Está conflado: se pueden perder valores intermedios")

    runBlocking {
        val contador = MutableStateFlow(0)
        val vistos = mutableListOf<Int>()

        val trabajo = launch {
            contador.collect { valor ->
                registrar(vistos, valor)
                delay(60)                      // colector lento a propósito
            }
        }

        delay(20)
        repeat(5) { contador.value = it + 1 }  // cinco cambios muy seguidos
        delay(200)
        trabajo.cancelAndJoin()

        show("valores emitidos", "0, 1, 2, 3, 4, 5")
        show("valores vistos por el colector", vistos)
        show("¿los vio todos?", vistos.size == 6)
    }

    bullet("Sólo se garantiza el ÚLTIMO valor, no todos los intermedios.")
    bullet("Para un estado es exactamente lo correcto: nadie quiere repintar la")
    bullet("pantalla con estados que ya han quedado obsoletos.")

    section("Encapsular: mutable dentro, sólo lectura fuera")

    val contador = ContadorDeVisitas()
    runBlocking {
        show("tipo expuesto", "StateFlow<Int> (sin `value =` disponible)")
        show("valor inicial", contador.visitas.value)
        contador.registrarVisita()
        contador.registrarVisita()
        show("tras dos visitas", contador.visitas.value)
    }

    bullet("`asStateFlow()` devuelve una vista de sólo lectura del mismo flujo.")
    bullet("Sin eso, cualquiera podría escribir `contador.visitas.value = 99`.")
    bullet("Es el mismo patrón que `List` frente a `MutableList` (capítulo 13).")

    section("Cuándo usar StateFlow")

    bullet("El estado de una pantalla, de un formulario, de una conexión.")
    bullet("Un valor de configuración que puede cambiar en caliente.")
    bullet("Cualquier cosa de la que tenga sentido preguntar '¿y ahora cuánto vale?'.")
}

/**
 * `SharedFlow`: eventos para todos.
 */
fun demoSharedFlow() {
    section("Todos los suscriptores reciben cada evento")

    runBlocking {
        val eventos = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val deA = mutableListOf<String>()
        val deB = mutableListOf<String>()

        val a = launch { eventos.collect { registrar(deA, it) } }
        val b = launch { eventos.collect { registrar(deB, it) } }

        delay(20)                     // que se suscriban los dos
        eventos.emit("guardado")
        eventos.emit("cerrado")
        delay(50)

        a.cancelAndJoin()
        b.cancelAndJoin()

        show("colector A", deA)
        show("colector B", deB)
    }

    bullet("Los dos reciben LO MISMO. Esa es la diferencia con un Channel, donde")
    bullet("cada elemento se lo lleva un solo consumidor.")

    section("Sin suscriptores, el evento se pierde")

    runBlocking {
        val eventos = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val recibidos = mutableListOf<String>()

        eventos.emit("nadie escuchaba: este se pierde")

        val trabajo = launch { eventos.collect { registrar(recibidos, it) } }
        delay(20)
        eventos.emit("ahora sí")
        delay(20)
        trabajo.cancelAndJoin()

        show("recibidos", recibidos)
        show("suscriptores actuales", eventos.subscriptionCount.value)
    }

    bullet("Un SharedFlow no guarda historial: lo que se emite sin público, se va.")

    section("replay: los últimos N para quien llegue tarde")

    runBlocking {
        val eventos = MutableSharedFlow<String>(replay = 2, extraBufferCapacity = 8)

        eventos.emit("a")
        eventos.emit("b")
        eventos.emit("c")

        // Se suscribe DESPUÉS de las tres emisiones.
        val tardio = eventos.take(2).toList()
        show("suscriptor tardío con replay=2", tardio)
    }

    bullet("Guarda los 2 últimos y se los entrega a quien se suscriba después.")
    bullet("Con `replay = 1` tienes casi un StateFlow, pero sin `distinctUntilChanged`")
    bullet("y sin la propiedad `.value`.")

    section("La trampa del buffer: emit puede SUSPENDER")

    runBlocking {
        // Sin buffer ni replay: `emit` espera a que TODOS los colectores acepten.
        val sinBuffer = MutableSharedFlow<String>()
        val recibidos = mutableListOf<String>()

        val trabajo = launch {
            sinBuffer.collect { valor ->
                delay(60)                         // colector lento
                registrar(recibidos, valor)
            }
        }
        delay(20)

        val tiempo = measureTime {
            sinBuffer.emit("uno")
            sinBuffer.emit("dos")
        }

        show("tiempo de dos emit con colector lento", "${tiempo.inWholeMilliseconds} ms")
        show("¿emit suspendió?", tiempo.inWholeMilliseconds > 50)

        delay(80)
        trabajo.cancelAndJoin()
        show("recibidos", recibidos)
    }

    bullet("Un colector lento FRENA al emisor. A veces es lo que quieres (no perder")
    bullet("eventos) y a veces es un bloqueo inesperado en medio de tu código.")

    section("tryEmit: emitir sin suspender")

    runBlocking {
        val sinBuffer = MutableSharedFlow<String>()
        val conBuffer = MutableSharedFlow<String>(extraBufferCapacity = 2)

        // Sin nadie escuchando, no hay a quién entregar: se acepta y se descarta.
        show("sin colectores", sinBuffer.tryEmit("se pierde"))

        val lentoA = launch { sinBuffer.collect { delay(200) } }
        val lentoB = launch { conBuffer.collect { delay(200) } }
        delay(20)

        show("con un colector y SIN buffer", sinBuffer.tryEmit("a"))

        val conBufferResultados = (1..4).map { conBuffer.tryEmit("e$it") }
        show("con extraBufferCapacity=2, cuatro intentos", conBufferResultados)

        lentoA.cancelAndJoin()
        lentoB.cancelAndJoin()
    }

    bullet("`tryEmit` devuelve `false` si no hay sitio, en vez de suspender.")
    bullet("Sin buffer NUNCA hay sitio en cuanto existe un colector: el rendezvous")
    bullet("exige esperar. Con buffer, se acepta hasta llenarlo y luego falla.")
    bullet("Útil desde código no suspendido, pero hay que MIRAR el booleano:")
    bullet("ignorarlo es perder eventos en silencio.")

    section("Encapsular, igual que con StateFlow")

    val bus = BusDeEventos()
    runBlocking {
        val recogidos = mutableListOf<String>()
        val trabajo = launch { bus.eventos.collect { registrar(recogidos, it) } }
        delay(20)
        bus.publicar("usuario-creado")
        bus.publicar("usuario-borrado")
        delay(40)
        trabajo.cancelAndJoin()
        show("eventos recogidos", recogidos)
    }

    bullet("`asSharedFlow()` esconde el `emit`: sólo el dueño del bus publica.")
}

/**
 * `Channel`: una cola entre corrutinas.
 */
fun demoChannels() {
    section("Enviar y recibir")

    runBlocking {
        val canal = Channel<Int>()

        launch {
            for (i in 1..3) canal.send(i)
            canal.close()                 // sin esto, el `for` de abajo no termina
        }

        val recibidos = mutableListOf<Int>()
        for (valor in canal) {            // termina solo cuando el canal se cierra
            recibidos.add(valor)
        }

        show("recibidos", recibidos)
        show("¿cerrado para recibir?", canal.isClosedForReceive)
    }

    bullet("Cerrar el canal es OBLIGATORIO: es lo que hace terminar al consumidor.")
    bullet("Es el error nº 1 con canales: un `for` que se queda esperando para siempre.")

    section("Cada elemento va a UN solo consumidor")

    runBlocking {
        val tareas = Channel<Int>(Channel.UNLIMITED)
        repeat(9) { tareas.trySend(it) }
        tareas.close()

        val hechas = mutableListOf<String>()
        val obreros = (1..3).map { numero ->
            launch(Dispatchers.Default) {
                for (tarea in tareas) {
                    delay(10)
                    registrar(hechas, "obrero-$numero → tarea $tarea")
                }
            }
        }
        obreros.joinAll()

        show("tareas repartidas", hechas.size)
        show("¿alguna se hizo dos veces?", hechas.size != hechas.toSet().size)
    }

    bullet("Esto se llama 'fan-out': tres trabajadores repartiéndose una cola.")
    bullet("Con un SharedFlow los TRES habrían hecho las 9 tareas. Elige bien.")

    section("Las capacidades")

    runBlocking {
        // RENDEZVOUS (por defecto, 0): el send espera a que alguien reciba.
        val cita = Channel<Int>()
        show("Channel() → trySend sin receptor", cita.trySend(1).isSuccess)
        cita.close()

        // BUFFERED / capacidad explícita: cabe algo antes de suspender.
        val conCapacidad = Channel<Int>(capacity = 2)
        show("Channel(2) → primer trySend", conCapacidad.trySend(1).isSuccess)
        show("Channel(2) → segundo trySend", conCapacidad.trySend(2).isSuccess)
        show("Channel(2) → tercer trySend", conCapacidad.trySend(3).isSuccess)
        conCapacidad.close()

        // CONFLATED: siempre acepta, quedándose sólo con el último.
        val conflado = Channel<Int>(Channel.CONFLATED)
        conflado.trySend(1)
        conflado.trySend(2)
        conflado.trySend(3)
        show("Channel(CONFLATED) → lo que queda", conflado.receive())
        conflado.close()

        // UNLIMITED: nunca suspende (y nunca avisa si te quedas sin memoria).
        val ilimitado = Channel<Int>(Channel.UNLIMITED)
        repeat(1_000) { ilimitado.trySend(it) }
        show("Channel(UNLIMITED) → mil enviados sin suspender", true)
        ilimitado.close()
    }

    bullet("RENDEZVOUS → cita a ciegas: nadie avanza hasta que ambos están.")
    bullet("N          → cola de N elementos; al llenarse, el emisor espera.")
    bullet("CONFLATED  → sólo importa el último. Nunca suspende.")
    bullet("UNLIMITED  → nunca suspende. Cuidado: si el consumidor no da abasto,")
    bullet("             la cola crece hasta quedarte sin memoria.")

    section("Un canal es de un solo uso")

    runBlocking {
        val canal = Channel<Int>(1)
        canal.send(1)
        canal.close()

        val trasCerrar = canal.trySend(2)
        show("trySend tras close()", trasCerrar.isSuccess)
        show("lo que quedaba dentro sigue disponible", canal.receive())
    }

    bullet("Cerrado es cerrado: no se reabre. Si necesitas algo permanente que")
    bullet("varios puedan observar una y otra vez, eso es un SharedFlow.")
}

/**
 * Cuál usar para cada cosa.
 */
fun demoWhatToUseWhen() {
    section("La tabla de decisión")

    bullet("¿Hay un VALOR ACTUAL que tenga sentido consultar?      → StateFlow")
    bullet("¿Son EVENTOS que todos los interesados deben ver?      → SharedFlow")
    bullet("¿Es TRABAJO que debe hacer uno solo de los consumidores? → Channel")
    bullet("¿Se produce a demanda y no hay nada que compartir?      → Flow frío")

    section("Las mismas preguntas, con ejemplos")

    bullet("StateFlow  → usuario conectado, filtros seleccionados, ¿hay conexión?")
    bullet("SharedFlow → 'se ha guardado', 'pulsa aquí', 'ha llegado una notificación'")
    bullet("Channel    → trabajos pendientes, peticiones a procesar, cola de envíos")
    bullet("Flow frío  → leer un fichero, paginar una API, consultar la base de datos")

    section("Frío y caliente, otra vez")

    runBlocking {
        var ejecucionesDelFrio = 0
        val frio = kotlinx.coroutines.flow.flow {
            ejecucionesDelFrio++
            emit(1)
        }
        frio.first()
        frio.first()
        show("un Flow frío recogido dos veces", "$ejecucionesDelFrio ejecuciones")

        val caliente = MutableStateFlow(1)
        caliente.first()
        caliente.first()
        show("un StateFlow recogido dos veces", "0 ejecuciones: el valor ya estaba ahí")
    }

    bullet("El frío produce bajo demanda; el caliente ya existe y tú te asomas.")

    section("El error más caro: StateFlow para eventos")

    runBlocking {
        val avisos = MutableStateFlow("")
        val vistos = mutableListOf<String>()

        val trabajo = launch { avisos.collect { registrar(vistos, it) } }
        delay(20)

        avisos.value = "error de red"
        delay(20)
        avisos.value = "error de red"     // el MISMO aviso, otra vez
        delay(20)

        trabajo.cancelAndJoin()
        show("avisos emitidos", "2 (el mismo texto dos veces)")
        show("avisos vistos", vistos.filter { it.isNotEmpty() })
    }

    bullet("El segundo aviso NO llega: `distinctUntilChanged` lo considera repetido.")
    bullet("Si el usuario pierde la conexión dos veces, sólo ve un mensaje.")
    bullet("Para eventos: SharedFlow. Siempre.")

    section("Y el error opuesto: SharedFlow para estado")

    bullet("Un suscriptor que llega tarde no sabe cuál es el estado actual, porque")
    bullet("un SharedFlow sin `replay` no guarda nada. Acabas añadiendo `replay = 1`")
    bullet("y reinventando el StateFlow, pero peor.")

    section("Convertir un flujo frío en caliente")

    bullet("`stateIn(scope, iniciado, valorInicial)` y `shareIn(scope, iniciado)`")
    bullet("toman un Flow frío y lo comparten entre varios colectores, ejecutando")
    bullet("el origen UNA sola vez. Es el patrón de un repositorio que sirve a")
    bullet("varias pantallas. Necesitan un scope con ciclo de vida, así que aquí")
    bullet("sólo se mencionan: son la forma correcta de unir los dos mundos.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/**
 * Añade una línea a una lista compartida entre corrutinas de varios hilos.
 */
private fun <T> registrar(destino: MutableList<T>, valor: T) {
    synchronized(destino) { destino.add(valor) }
}

/**
 * El patrón de encapsulado de StateFlow: mutable privado, vista pública inmutable.
 */
private class ContadorDeVisitas {

    private val _visitas = MutableStateFlow(0)

    /** Lo que ve el resto del mundo: se puede leer y recoger, pero no escribir. */
    val visitas: StateFlow<Int> = _visitas.asStateFlow()

    fun registrarVisita() {
        // `update` sería lo correcto con varios hilos; aquí basta con esto.
        _visitas.value += 1
    }
}

/**
 * El mismo patrón con SharedFlow: un bus de eventos de toda la vida.
 */
private class BusDeEventos {

    private val _eventos = MutableSharedFlow<String>(extraBufferCapacity = 16)

    val eventos: SharedFlow<String> = _eventos.asSharedFlow()

    suspend fun publicar(evento: String) {
        _eventos.emit(evento)
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia el StateFlow de avisos por un MutableSharedFlow y comprueba que ahora
//     sí llegan los dos mensajes repetidos.
//  2. Quita el `canal.close()` de la primera demo de canales: el programa se queda
//     colgado. Vuelve a ponerlo (y aprende el porqué de memoria).
//  3. Sustituye el Channel del reparto de tareas por un SharedFlow y cuenta cuántas
//     veces se hace cada tarea.
//  4. Sube `extraBufferCapacity` en la demo del emisor frenado y mira cómo cambia
//     el tiempo de los dos `emit`.
