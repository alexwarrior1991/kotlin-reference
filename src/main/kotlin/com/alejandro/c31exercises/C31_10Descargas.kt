package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.measureTime

// =====================================================================================
//  Ejercicio 10 · Descargas simuladas con corrutinas                       🔴 difícil
//
//  Repasa: suspend, async/awaitAll, supervisorScope, withTimeoutOrNull, reintentos,
//  cancelación cooperativa, Semaphore para limitar la concurrencia.
//  Capítulos: 28 (corrutinas), 30 (inyección para testear).
// =====================================================================================

/** Ejercicio 10: descargar muchas cosas a la vez, sin que una mala lo estropee todo. */
fun ejercicio10Descargas() {
    enunciado(
        "Descarga una lista de recursos en paralelo. No hay red: un `delay` la",
        "simula, igual que en todo el capítulo 28.",
        "",
        "1. `descargarTodo(urls, descargador)` en PARALELO, no una detrás de otra.",
        "2. Que una descarga falle no puede cancelar las demás.",
        "3. Cada descarga tiene su propio timeout.",
        "4. Reintentos con espera creciente para los fallos transitorios.",
        "5. El resultado conserva el ORDEN de las urls de entrada, aunque terminen",
        "   desordenadas.",
        "6. Limita la concurrencia: como mucho N descargas a la vez, para no",
        "   reventar al servidor.",
        "7. `Descargador` debe ser inyectable, para poder simular fallos en los tests.",
    )

    pistas(
        "`map { async { ... } }` y LUEGO `awaitAll()`. Si escribes",
        "   `map { async { ... }.await() }` vuelves a lo secuencial (demo 28.7).",
        "`supervisorScope` en vez de `coroutineScope`: con el segundo, un fallo",
        "   cancela a todas sus hermanas (demo 28.11).",
        "`withTimeoutOrNull` devuelve null al agotarse, que encaja con un `?:`.",
        "Como `awaitAll` conserva el orden de la lista de Deferred, el orden sale",
        "   gratis: no hace falta ordenar nada después.",
        "Para limitar la concurrencia, `Semaphore(n)` de kotlinx.coroutines y",
        "   `withPermit { }`: suspende en vez de bloquear.",
        "Relanza SIEMPRE `CancellationException` antes de capturar `Exception`,",
        "   o los reintentos seguirán corriendo tras cancelar (demo 28.16).",
    )

    solucionEnMarcha()

    val urls = listOf(
        "https://ejemplo.com/a",
        "https://ejemplo.com/lento",      // tarda más que el timeout
        "https://ejemplo.com/inestable",  // falla dos veces y luego funciona
        "https://ejemplo.com/roto",       // falla siempre
        "https://ejemplo.com/b",
    )

    section("Todo a la vez, tolerando fallos")

    val descargador = DescargadorSimulado()
    var resultados: List<ResultadoDescarga> = emptyList()

    val tiempo = measureTime {
        runBlocking {
            resultados = descargarTodo(urls, descargador, timeoutMs = 150, reintentos = 3)
        }
    }

    resultados.forEach { resultado -> show(corto(resultado.url), describirDescarga(resultado)) }
    show("tiempo total", "${tiempo.inWholeMilliseconds} ms")
    show("intentos realizados", descargador.intentos())

    bullet("Las cinco fueron a la vez: el total lo marca la url «lento», que agota")
    bullet("sus tres timeouts de 150 ms. En serie habría sido bastante más.")
    bullet("El orden de la salida es el de ENTRADA, aunque terminaran desordenadas.")

    section("Resumen")

    val exitos = resultados.filterIsInstance<ResultadoDescarga.Ok>()
    show("descargadas", exitos.size)
    show("fallidas", resultados.size - exitos.size)
    show("bytes totales", exitos.sumOf { it.contenido.length })

    section("Limitando la concurrencia a 2")

    val conLimite = DescargadorSimulado()
    val urlsLentas = List(6) { "https://ejemplo.com/tarda-$it" }

    val tiempoLimitado = measureTime {
        runBlocking { descargarTodo(urlsLentas, conLimite, concurrenciaMaxima = 2) }
    }
    val tiempoLibre = measureTime {
        runBlocking { descargarTodo(urlsLentas, DescargadorSimulado(), concurrenciaMaxima = 6) }
    }

    show("6 descargas de ~60 ms, de 2 en 2", "${tiempoLimitado.inWholeMilliseconds} ms")
    show("6 descargas de ~60 ms, todas a la vez", "${tiempoLibre.inWholeMilliseconds} ms")
    show("concurrencia máxima observada", conLimite.concurrenciaMaxima())

    bullet("El semáforo hace que nunca haya más de 2 descargas simultáneas.")
    bullet("Se tarda más, pero el servidor de enfrente sigue vivo.")

    section("Cancelar a mitad")

    runBlocking {
        val traza = mutableListOf<String>()
        val trabajo = launch {
            descargarTodo(List(20) { "https://ejemplo.com/tarda-$it" }, DescargadorSimulado())
            synchronized(traza) { traza.add("terminó (no debería)") }
        }
        delay(30)
        trabajo.cancel()
        trabajo.join()

        show("¿se canceló?", trabajo.isCancelled)
        show("¿llegó al final?", "terminó (no debería)" in traza)
    }

    bullet("La cancelación llega a TODAS las descargas: es concurrencia estructurada.")

    explicacion(
        "Este ejercicio junta las cuatro decisiones de cualquier código concurrente",
        "real, y cada una tiene su trampa.",
        "",
        "1. PARALELO DE VERDAD. `map { async { } }` y luego `awaitAll()`, en dos",
        "   pasos separados. El error de `map { async { }.await() }` parece",
        "   asíncrono y es exactamente igual de lento.",
        "",
        "2. AISLAR LOS FALLOS. `supervisorScope`: con `coroutineScope`, la url rota",
        "   cancelaría las otras cuatro. Aquí cada descarga es independiente, así que",
        "   el supervisor es lo correcto. Cuando las partes son necesarias entre sí,",
        "   lo correcto es lo contrario.",
        "",
        "3. TIMEOUT POR OPERACIÓN. Dentro del bucle, no fuera. Así una url lenta no",
        "   se come el presupuesto de tiempo de las demás.",
        "",
        "4. RELANZAR LA CANCELACIÓN. En `conReintentos`, el `catch (e: Exception)`",
        "   va DESPUÉS de `catch (e: CancellationException) { throw e }`. Sin eso,",
        "   cancelar el trabajo no pararía nada: los reintentos seguirían girando",
        "   sobre una corrutina ya muerta. Es el error más común del capítulo 28.",
        "",
        "Y el orden: `awaitAll` respeta el orden de la lista de `Deferred`, no el de",
        "finalización. Por eso no hay que ordenar nada al final. Si hubieras usado",
        "una lista compartida con `launch { lista += ... }` tendrías, además de un",
        "orden aleatorio, una carrera de datos (demo 28.41).",
    )

    varianteDificil(
        "1. Devuelve el progreso como un `Flow<Progreso>` que emita cada vez que",
        "   termina una descarga (el ejercicio 11 te da la mitad hecha).",
        "2. Reintenta sólo los errores transitorios: distingue `ErrorDeRed` de",
        "   `NoEncontrado` con una jerarquía sellada y no reintentes el segundo.",
        "3. Añade caché: si una url ya se descargó, no vuelvas a pedirla ni aunque",
        "   te la pidan diez veces a la vez (pista: `Deferred` compartido).",
        "4. Cancela las descargas pendientes en cuanto N hayan fallado.",
        "5. Añade un presupuesto GLOBAL de tiempo además del de cada descarga, y",
        "   devuelve lo que se haya conseguido cuando se agote.",
        "6. Testéalo con `runTest` y tiempo virtual: las esperas dejan de existir.",
    )

    testEn("DescargasTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/** El resultado de una descarga: éxito con contenido, o fallo con motivo. */
sealed interface ResultadoDescarga {
    val url: String

    data class Ok(override val url: String, val contenido: String, val intentos: Int) : ResultadoDescarga
    data class Fallo(override val url: String, val motivo: String, val intentos: Int) : ResultadoDescarga
}

/**
 * La dependencia con el exterior, aislada tras una interfaz de una sola función.
 *
 * En producción haría una petición HTTP; en los tests devuelve lo que tú quieras
 * (capítulo 30).
 */
fun interface Descargador {
    suspend fun descargar(url: String): String
}

/**
 * Descarga todas las urls en paralelo.
 *
 * @param concurrenciaMaxima cuántas descargas puede haber a la vez.
 * @param timeoutMs límite de tiempo POR INTENTO.
 * @param reintentos cuántas veces se intenta cada url antes de darla por perdida.
 * @return una lista con el mismo orden que [urls].
 */
suspend fun descargarTodo(
    urls: List<String>,
    descargador: Descargador,
    concurrenciaMaxima: Int = 4,
    timeoutMs: Long = 200,
    reintentos: Int = 3,
): List<ResultadoDescarga> = supervisorScope {
    // El semáforo limita cuántas corrutinas entran a la vez. Suspende, no bloquea.
    val permisos = Semaphore(concurrenciaMaxima)

    urls
        // 1. Arrancar TODAS...
        .map { url ->
            async {
                permisos.withPermit {
                    descargarConReintentos(url, descargador, timeoutMs, reintentos)
                }
            }
        }
        // 2. ...y sólo entonces esperar. `awaitAll` conserva el orden de la lista.
        .awaitAll()
}

/**
 * Una descarga con timeout por intento y espera creciente entre ellos.
 */
private suspend fun descargarConReintentos(
    url: String,
    descargador: Descargador,
    timeoutMs: Long,
    reintentos: Int,
): ResultadoDescarga {
    var espera = 20L
    var ultimoMotivo = "desconocido"

    for (intento in 1..reintentos) {
        try {
            val contenido = withTimeoutOrNull(timeoutMs) { descargador.descargar(url) }
            if (contenido != null) return ResultadoDescarga.Ok(url, contenido, intento)
            ultimoMotivo = "tiempo agotado (${timeoutMs} ms)"
        } catch (e: CancellationException) {
            // SIEMPRE antes del catch genérico: si no, cancelar no pararía nada.
            throw e
        } catch (e: Exception) {
            ultimoMotivo = e.message ?: e::class.simpleName.orEmpty()
        }

        // Nada de esperar después del último intento.
        if (intento < reintentos) {
            delay(espera)
            espera *= 2
        }
    }
    return ResultadoDescarga.Fallo(url, ultimoMotivo, reintentos)
}

/**
 * Un descargador de mentira, con comportamientos preparados por url.
 *
 * Además cuenta intentos y mide la concurrencia real: así la demo puede demostrar
 * que el semáforo hace su trabajo.
 */
class DescargadorSimulado : Descargador {

    private var intentos = 0
    private var enCurso = 0
    private var maximoEnCurso = 0
    private val fallosPorUrl = mutableMapOf<String, Int>()

    override suspend fun descargar(url: String): String {
        entrar()
        try {
            return when {
                "lento" in url -> {
                    delay(400)                      // más que cualquier timeout de la demo
                    "contenido lento"
                }

                "inestable" in url -> {
                    delay(20)
                    val fallosPrevios = synchronized(fallosPorUrl) {
                        fallosPorUrl.merge(url, 1, Int::plus)!!
                    }
                    if (fallosPrevios <= 2) error("error transitorio nº $fallosPrevios")
                    "contenido tras reintentos"
                }

                "roto" in url -> {
                    delay(10)
                    error("404: no encontrado")
                }

                "tarda" in url -> {
                    delay(60)
                    "contenido de $url"
                }

                else -> {
                    delay(30)
                    "contenido de $url"
                }
            }
        } finally {
            salir()
        }
    }

    fun intentos(): Int = synchronized(this) { intentos }

    fun concurrenciaMaxima(): Int = synchronized(this) { maximoEnCurso }

    private fun entrar() = synchronized(this) {
        intentos++
        enCurso++
        maximoEnCurso = maxOf(maximoEnCurso, enCurso)
    }

    private fun salir() = synchronized(this) { enCurso-- }
}

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun describirDescarga(resultado: ResultadoDescarga): String = when (resultado) {
    is ResultadoDescarga.Ok ->
        "✓ ${resultado.contenido} (${resultado.intentos} intento(s))"
    is ResultadoDescarga.Fallo ->
        "✗ ${resultado.motivo} tras ${resultado.intentos} intento(s)"
}

private fun corto(url: String): String = url.substringAfterLast('/')

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `supervisorScope` por `coroutineScope` y mira qué le pasa al resto de
//     descargas cuando la url rota falla.
//  2. Sustituye `map { async { } }.awaitAll()` por `map { async { }.await() }` y
//     compara el tiempo total.
//  3. Quita el `catch (e: CancellationException) { throw e }` y comprueba que la
//     última demo (cancelar a mitad) deja de funcionar.
//  4. Baja `concurrenciaMaxima` a 1 y observa que el tiempo se vuelve la suma de todo.
