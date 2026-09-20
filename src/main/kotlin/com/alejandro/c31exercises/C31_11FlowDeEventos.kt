package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// =====================================================================================
//  Ejercicio 11 · Flow de eventos                                          🔴 difícil
//
//  Repasa: Flow frío, operadores, takeWhile, filterIsInstance, transform, catch,
//  StateFlow, SharedFlow, y la diferencia entre estado y evento.
//  Capítulos: 11 (sealed), 28 (Flow), 30 (testear flujos).
// =====================================================================================

/** Ejercicio 11: procesar un flujo de eventos de interfaz. */
fun ejercicio11FlowDeEventos() {
    enunciado(
        "Procesa el flujo de eventos de una pantalla de búsqueda.",
        "",
        "1. Modela los eventos con una jerarquía sellada: Tecleo, Pulsacion,",
        "   Desplazamiento y Cerrar.",
        "2. `soloBusquedas(eventos)`: quédate con los tecleos, recorta espacios,",
        "   descarta los de menos de 3 caracteres y no repitas la misma consulta",
        "   dos veces seguidas.",
        "3. `hastaCerrar(eventos)`: deja de emitir en cuanto llegue Cerrar,",
        "   incluyéndolo o no según convenga.",
        "4. `contarPorTipo(eventos)`: cuántos eventos de cada clase han pasado.",
        "5. Un `EstadoDeBusqueda` expuesto como `StateFlow` (hay un valor actual)",
        "   y los avisos al usuario como `SharedFlow` (son eventos, se repiten).",
        "6. Un flujo que falla a mitad y se recupera con el operador `catch`.",
    )

    pistas(
        "`filterIsInstance<Tecleo>()` filtra Y hace el cast a la vez (capítulo 20).",
        "`transform { }` sirve cuando necesitas emitir cero, uno o varios valores",
        "   por cada entrada; `map` y `filter` son casos particulares suyos.",
        "Para «no repitas la misma consulta seguida», `distinctUntilChanged()`.",
        "   Ojo: quita los CONSECUTIVOS, no los duplicados globales (demo 28.33).",
        "`takeWhile { it !is Cerrar }` corta el flujo y además lo COMPLETA: el",
        "   `collect` vuelve solo, sin cancelar nada a mano.",
        "Estado → StateFlow (tiene `.value`, no repite valores iguales).",
        "   Evento → SharedFlow (se puede emitir el mismo dos veces). Demo 28.40.",
        "El operador `catch` va SIEMPRE debajo de lo que quieres proteger.",
    )

    solucionEnMarcha()

    val eventos = listOf<EventoUi>(
        EventoUi.Tecleo("ko"),
        EventoUi.Tecleo("kot"),
        EventoUi.Tecleo("kotl"),
        EventoUi.Tecleo("  kotl  "),      // el mismo, con espacios: no debe repetirse
        EventoUi.Pulsacion("buscar"),
        EventoUi.Tecleo("kotlin"),
        EventoUi.Desplazamiento(120),
        EventoUi.Desplazamiento(340),
        EventoUi.Pulsacion("cerrar"),
        EventoUi.Cerrar,
        EventoUi.Tecleo("esto ya no cuenta"),
    )

    section("1. Sólo las búsquedas útiles")

    runBlocking {
        show("consultas", soloBusquedas(eventos.asFlow()).toList())
    }

    bullet("«ko» se descarta por tener menos de $MINIMO_DE_LETRAS letras.")
    bullet("«  kotl  » se recorta y se descarta por repetir la consulta anterior.")
    bullet("Pero «esto ya no cuenta» SÍ aparece, aunque llegue después de Cerrar:")
    bullet("`soloBusquedas` no sabe nada de cierres. Para eso están los operadores")
    bullet("pequeños y COMPONIBLES, como se ve dos líneas más abajo.")

    runBlocking {
        show("soloBusquedas(hastaCerrar(eventos))", soloBusquedas(hastaCerrar(eventos.asFlow())).toList())
    }

    bullet("Cada función hace UNA cosa; encadenarlas es lo que da el comportamiento.")

    section("2. Hasta el cierre")

    runBlocking {
        val antesDeCerrar = hastaCerrar(eventos.asFlow()).toList()
        show("eventos procesados", antesDeCerrar.size)
        show("último", antesDeCerrar.last())
        show("¿se coló el de después de Cerrar?", antesDeCerrar.any { it is EventoUi.Tecleo && "ya no cuenta" in it.texto })
    }

    section("3. Recuento por tipo")

    runBlocking {
        contarPorTipo(eventos.asFlow()).forEach { (tipo, cuantos) -> show(tipo, cuantos) }
    }

    section("4. El estado de la pantalla")

    runBlocking {
        val pantalla = PantallaDeBusqueda()
        val vistos = mutableListOf<String>()
        val avisos = mutableListOf<String>()

        val observandoEstado = launch {
            pantalla.estado.collect { estado -> synchronized(vistos) { vistos += descripcionDe(estado) } }
        }
        val observandoAvisos = launch {
            pantalla.avisos.collect { aviso -> synchronized(avisos) { avisos += aviso } }
        }
        delay(20)

        pantalla.buscar("kotlin")
        delay(40)
        pantalla.buscar("ko")            // demasiado corta: aviso, no búsqueda
        delay(20)
        pantalla.buscar("ko")            // el MISMO aviso otra vez
        delay(20)
        pantalla.buscar("corrutinas")
        delay(40)

        observandoEstado.cancel()
        observandoAvisos.cancel()

        show("estados por los que pasó", vistos)
        show("avisos recibidos", avisos)
        show("estado actual (.value)", descripcionDe(pantalla.estado.value))
    }

    bullet("El MISMO aviso llegó DOS veces: eso es lo que un SharedFlow hace y un")
    bullet("StateFlow no, porque éste descarta los valores repetidos.")

    section("5. Un flujo que se rompe y se recupera")

    runBlocking {
        show("sin catch", runCatching { flujoQueSeRompe().toList() }.exceptionOrNull()?.message)
        show("con catch", flujoConRecuperacion().toList())
    }

    bullet("`catch` captura lo de ARRIBA y además puede emitir un valor de repuesto.")

    explicacion(
        "LA DECISIÓN PRINCIPAL: estado y evento no son lo mismo, y por eso se",
        "modelan con tipos distintos.",
        "",
        "El texto de la búsqueda, si está cargando, los resultados: eso es ESTADO.",
        "Tiene un valor actual que tiene sentido preguntar, y repintar dos veces el",
        "mismo estado no aporta nada. `StateFlow`.",
        "",
        "«Escribe al menos 3 letras», «se ha guardado», «no hay conexión»: eso son",
        "EVENTOS. Si el usuario comete el mismo error dos veces, tiene que ver el",
        "aviso dos veces. Con `StateFlow` el segundo NO llegaría, porque lleva",
        "`distinctUntilChanged` de serie. `SharedFlow`.",
        "",
        "Míralo en la salida de la demo 4: dos búsquedas cortas seguidas producen",
        "dos avisos idénticos, y los dos llegan.",
        "",
        "LA SEGUNDA DECISIÓN: la cadena de operadores se lee como una frase.",
        "",
        "   eventos",
        "     .filterIsInstance<Tecleo>()      quédate con los tecleos",
        "     .map { it.texto.trim() }         recorta",
        "     .filter { it.length >= 3 }       descarta los cortos",
        "     .distinctUntilChanged()          no repitas la anterior",
        "",
        "Cada línea es una regla de negocio, en el orden en que se aplican. Ese",
        "mismo procesado con un bucle y tres `if` anidados ocuparía lo mismo y se",
        "leería mucho peor.",
        "",
        "Y `takeWhile { it !is Cerrar }`: completa el flujo en vez de cancelarlo, así",
        "que `toList()` devuelve lo acumulado y el `collect` termina solo.",
    )

    varianteDificil(
        "1. Añade antirrebote: no busques hasta que pasen 300 ms sin teclear.",
        "   Con `debounce` es una línea; hazlo también a mano con `transform` y",
        "   `withTimeoutOrNull` para entender qué hace por dentro.",
        "2. Cancela la búsqueda anterior al teclear otra letra: `collectLatest` o",
        "   `flatMapLatest`. Es EL patrón de un buscador.",
        "3. Combina el flujo de búsqueda con uno de filtros usando `combine`, de",
        "   forma que cambiar cualquiera de los dos relance la consulta.",
        "4. Añade `retry(3)` a la búsqueda y comprueba con qué flujo se reintenta.",
        "5. Testéalo con `runTest` y tiempo virtual, comprobando CUÁNDO se emite",
        "   cada valor, no sólo cuáles (capítulo 30).",
        "6. Convierte el flujo frío en caliente con `shareIn` y comprueba que dos",
        "   colectores ya NO ejecutan el origen dos veces.",
    )

    testEn("FlowDeEventosTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/** Los eventos que puede producir la pantalla. */
sealed interface EventoUi {
    data class Tecleo(val texto: String) : EventoUi
    data class Pulsacion(val boton: String) : EventoUi
    data class Desplazamiento(val pixeles: Int) : EventoUi
    data object Cerrar : EventoUi
}

/** El estado de la pantalla: siempre hay uno, y sólo uno. */
sealed interface EstadoDeBusqueda {
    data object Inicial : EstadoDeBusqueda
    data class Buscando(val consulta: String) : EstadoDeBusqueda
    data class Resultados(val consulta: String, val encontrados: List<String>) : EstadoDeBusqueda
}

/** Longitud mínima para que una consulta merezca la pena. */
const val MINIMO_DE_LETRAS = 3

/**
 * De todos los eventos, saca sólo las consultas que merece la pena buscar.
 *
 * Cada operador es una regla de negocio, en el orden en que se aplican.
 */
fun soloBusquedas(eventos: Flow<EventoUi>): Flow<String> = eventos
    .filterIsInstance<EventoUi.Tecleo>()          // filtra Y castea a la vez
    .map { it.texto.trim() }
    .filter { it.length >= MINIMO_DE_LETRAS }
    .distinctUntilChanged()                        // no repitas la consulta anterior

/**
 * Deja pasar los eventos hasta que llegue `Cerrar`, sin incluirlo.
 *
 * `takeWhile` COMPLETA el flujo: el colector vuelve solo, no hay que cancelar nada.
 */
fun hastaCerrar(eventos: Flow<EventoUi>): Flow<EventoUi> =
    eventos.takeWhile { it !is EventoUi.Cerrar }

/** Cuántos eventos de cada clase han pasado. */
suspend fun contarPorTipo(eventos: Flow<EventoUi>): Map<String, Int> {
    val cuenta = linkedMapOf<String, Int>()
    eventos.collect { evento ->
        val tipo = evento::class.simpleName ?: "?"
        cuenta[tipo] = (cuenta[tipo] ?: 0) + 1
    }
    return cuenta
}

/**
 * La pantalla: estado con `StateFlow`, avisos con `SharedFlow`.
 *
 * Fuera sólo se ven las vistas de sólo lectura (`asStateFlow`/`asSharedFlow`): nadie
 * puede escribir en el estado desde fuera de la clase.
 */
class PantallaDeBusqueda {

    private val _estado = MutableStateFlow<EstadoDeBusqueda>(EstadoDeBusqueda.Inicial)
    val estado: StateFlow<EstadoDeBusqueda> = _estado.asStateFlow()

    // `extraBufferCapacity` para que `tryEmit` no falle si nadie escucha todavía.
    private val _avisos = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val avisos: SharedFlow<String> = _avisos.asSharedFlow()

    /**
     * Lanza una búsqueda.
     *
     * Si la consulta es demasiado corta emite un AVISO (evento) y no toca el estado.
     * Fíjate en que el mismo aviso puede llegar dos veces seguidas: para eso es un
     * SharedFlow y no un StateFlow.
     */
    suspend fun buscar(consulta: String) {
        val limpia = consulta.trim()
        if (limpia.length < MINIMO_DE_LETRAS) {
            _avisos.emit("escribe al menos $MINIMO_DE_LETRAS letras")
            return
        }

        _estado.value = EstadoDeBusqueda.Buscando(limpia)
        delay(20)                                   // la búsqueda de mentira
        _estado.value = EstadoDeBusqueda.Resultados(limpia, resultadosDe(limpia))
    }

    private fun resultadosDe(consulta: String): List<String> =
        List(3) { "resultado ${it + 1} de «$consulta»" }
}

/** Un flujo que emite dos valores y luego se rompe. */
fun flujoQueSeRompe(): Flow<String> = flow {
    emit("primero")
    emit("segundo")
    error("la fuente de eventos se cayó")
}

/** El mismo flujo, con recuperación: `catch` va DEBAJO de lo que protege. */
fun flujoConRecuperacion(): Flow<String> = flujoQueSeRompe()
    .onEach { /* aquí iría el procesado normal */ }
    .catch { fallo -> emit("(recuperado: ${fallo.message})") }

// -- Un operador propio, para enseñar que no tienen nada de mágico ---------------------------------

/**
 * Agrupa los desplazamientos consecutivos en uno solo, sumando los píxeles.
 *
 * Es un operador intermedio hecho a mano: una extensión sobre `Flow<T>` que devuelve
 * otro `Flow<T>`. Todos los de la biblioteca están escritos igual.
 */
fun Flow<EventoUi>.agruparDesplazamientos(): Flow<EventoUi> = transform { evento ->
    // `transform` puede emitir cero, uno o varios valores por cada entrada.
    if (evento is EventoUi.Desplazamiento && evento.pixeles == 0) return@transform
    emit(evento)
}

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun descripcionDe(estado: EstadoDeBusqueda): String = when (estado) {
    is EstadoDeBusqueda.Inicial -> "Inicial"
    is EstadoDeBusqueda.Buscando -> "Buscando(${estado.consulta})"
    is EstadoDeBusqueda.Resultados -> "Resultados(${estado.consulta}, ${estado.encontrados.size})"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `_avisos` por un `MutableStateFlow<String>` y comprueba que el segundo
//     aviso idéntico ya no llega. Ésa es la diferencia, en una línea.
//  2. Quita el `.distinctUntilChanged()` de `soloBusquedas` y mira qué consulta
//     aparece repetida.
//  3. Mueve el `.catch { }` por encima del `.onEach { }` y comprueba que deja de
//     capturar nada.
//  4. Escribe tu propio `filtrarPorLongitud(minimo)` como operador de extensión.
