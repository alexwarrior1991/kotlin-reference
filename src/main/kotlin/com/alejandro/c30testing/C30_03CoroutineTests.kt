package com.alejandro.c30testing

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

// =====================================================================================
//  30.3 · Testear corrutinas y flujos
//
//  LA DEPENDENCIA
//    En `build.gradle.kts`:
//
//        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
//
//  LA IDEA CLAVE: EL TIEMPO VIRTUAL
//    Dentro de `runTest { }`, `delay(10.minutes)` no espera diez minutos: el
//    planificador adelanta el reloj al instante. Un test que simula una hora de
//    reintentos tarda milisegundos.
//
//  ERRORES COMUNES
//    · Usar `runBlocking` en los tests: pierdes el tiempo virtual y la suite tarda.
//    · `Thread.sleep` o `delay` "para dar tiempo a que termine": es una carrera.
//    · Escribir a fuego `Dispatchers.IO` dentro de la clase: ya no se puede sustituir.
//    · Afirmar sobre el ORDEN de ejecución de corrutinas paralelas: no es determinista.
// =====================================================================================

/**
 * `runTest` y el tiempo virtual.
 */
fun demoRunTest() {
    section("El problema")

    bullet("Una función con reintentos espera 1 s, luego 2 s, luego 4 s.")
    bullet("Probarla de verdad significa un test que tarda 7 segundos.")
    bullet("Con veinte tests así, la suite tarda dos minutos y deja de ejecutarse.")

    section("La solución: runTest")

    imprimirCodigo(
        """
        import kotlinx.coroutines.test.runTest
        import kotlin.test.Test
        import kotlin.test.assertEquals

        class ReintentosTest {

            @Test
            fun `reintenta hasta que la operacion funciona`() = runTest {
                var intentos = 0

                val resultado = reintentarConEspera(intentos = 3) { numero ->
                    intentos++
                    if (numero < 3) error("todavía no") else "conseguido"
                }

                assertEquals("conseguido", resultado)
                assertEquals(3, intentos)
                // Las esperas de 1 s y 2 s han ocurrido en tiempo VIRTUAL:
                // este test tarda milisegundos.
            }
        }
        """.trimIndent(),
    )

    bullet("`runTest { }` sustituye a `runBlocking` en los tests y trae consigo un")
    bullet("planificador con reloj propio. Cualquier `delay` de dentro es instantáneo.")

    section("Compruébalo: lo mismo, con tiempo REAL")

    // Aquí no hay `runTest` (está en testImplementation), así que las esperas son
    // de verdad. Por eso se usan milisegundos ridículos.
    val conEsperasCortas = measureTime {
        runBlocking {
            var intentos = 0
            val resultado = reintentarConEspera(intentos = 3, esperaInicialMs = 20) { numero ->
                intentos++
                if (numero < 3) error("todavía no") else "conseguido en el intento $numero"
            }
            show("resultado", resultado)
            show("intentos", intentos)
        }
    }
    show("con esperas de 20 y 40 ms", "${conEsperasCortas.inWholeMilliseconds} ms")

    bullet("Con las esperas reales de 1 s y 2 s, esto tardaría más de 3 segundos.")
    bullet("Dentro de `runTest`, cero. Y sin tocar el código de producción.")

    section("Las herramientas que trae kotlinx-coroutines-test")

    bullet("runTest { }            → el sustituto de runBlocking en los tests")
    bullet("advanceTimeBy(ms)      → adelanta el reloj virtual exactamente ese rato")
    bullet("advanceUntilIdle()     → adelanta hasta que no quede nada pendiente")
    bullet("currentTime            → el instante virtual actual, en ms")
    bullet("StandardTestDispatcher → el dispatcher a inyectar en tus clases")
    bullet("UnconfinedTestDispatcher → ejecuta sin encolar; útil para casos simples")
    bullet("runCurrent()           → ejecuta lo que ya está listo, sin adelantar")

    section("Comprobar CUÁNDO pasa algo, no sólo qué")

    imprimirCodigo(
        """
        @Test
        fun `la cuenta atras emite un valor por segundo`() = runTest {
            val emitidos = mutableListOf<Int>()

            val trabajo = launch {
                cuentaAtras(desde = 3).collect { emitidos += it }
            }

            advanceTimeBy(1_500)    // un segundo y medio virtual
            runCurrent()
            assertEquals(listOf(3, 2), emitidos)

            advanceUntilIdle()
            assertEquals(listOf(3, 2, 1, 0), emitidos)
            trabajo.join()
        }
        """.trimIndent(),
    )

    bullet("Con tiempo virtual se puede afirmar sobre el CALENDARIO de las emisiones,")
    bullet("cosa que con esperas reales sería una carrera y un test intermitente.")

    section("La versión con tiempo real, para que se vea el flujo")

    runBlocking {
        val valores = cuentaAtras(desde = 3, intervaloMs = 10).toList()
        show("cuentaAtras(3, intervaloMs = 10)", valores)
    }

    section("Inyectar el dispatcher")

    imprimirCodigo(
        """
        class ServicioDeNoticias(
            private val fuente: FuenteDeTitulares,
            private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
        )

        @Test
        fun `devuelve solo los titulares pedidos`() = runTest {
            val servicio = ServicioDeNoticias(
                fuente = { listOf("uno", "dos", "tres") },   // fun interface: una lambda
                dispatcher = StandardTestDispatcher(testScheduler),
            )

            assertEquals(listOf("uno", "dos"), servicio.titularesRecientes(limite = 2))
        }
        """.trimIndent(),
    )

    bullet("Sin el parámetro `dispatcher`, el `withContext(Dispatchers.Default)` de")
    bullet("dentro saldría del tiempo virtual y el test volvería a ser una carrera.")
    bullet("Es la regla de la demo 30.2 aplicada a corrutinas.")

    section("Funcionando de verdad")

    runBlocking {
        val servicio = ServicioDeNoticias(fuente = { listOf("uno", "dos", "tres") })
        show("titularesRecientes(2)", servicio.titularesRecientes(limite = 2))
        show("titularesRecientes(10)", servicio.titularesRecientes(limite = 10))

        val malo = runCatching { servicio.titularesRecientes(0) }
        show("titularesRecientes(0)", malo.exceptionOrNull()?.message)
    }

    section("Testear un Flow")

    bullet("Flujo finito  → `flujo.toList()` y compara con `assertEquals`.")
    bullet("Flujo infinito → acótalo con `take(n)` antes del `toList()`.")
    bullet("Flujo caliente → recógelo en un `launch` y usa `advanceUntilIdle()`.")
    bullet("Si haces mucho de esto, la librería Turbine (`awaitItem()`,")
    bullet("`awaitComplete()`) lo deja mucho más legible.")

    section("Las tres reglas")

    bullet("1. `runTest`, nunca `runBlocking`, en los tests de corrutinas.")
    bullet("2. Inyecta el dispatcher; nunca lo escribas a fuego dentro de la clase.")
    bullet("3. Nada de `delay` 'para dar tiempo': o tiempo virtual, o `join()`.")
}

/**
 * Los tests de este propio repositorio.
 */
fun demoTestingThisRepo() {
    section("Qué hay en src/test/kotlin")

    bullet("infra/RegistryTest.kt    → el registro de capítulos es coherente")
    bullet("infra/SmokeTest.kt       → TODAS las demos se ejecutan sin lanzar")
    bullet("infra/LauncherTest.kt    → el lanzador entiende sus comandos")
    bullet("c30testing/*Test.kt      → el código de producción de este capítulo")
    bullet("c30testing/CorrutinasTest.kt → corrutinas con runTest y tiempo virtual")
    bullet("c31exercises/*Test.kt    → las soluciones propuestas de los ejercicios")

    section("RegistryTest: la red de seguridad barata")

    bullet("Comprueba que los capítulos van del 1 al último sin huecos ni repetidos,")
    bullet("que ninguno está vacío, que todos los identificadores de demo son únicos")
    bullet("y que ningún título está en blanco.")
    bullet("")
    bullet("Suena a poco, pero es lo que detecta al instante el error más probable")
    bullet("de un repositorio con treinta y un capítulos: olvidarse de registrar uno")
    bullet("o copiar un índice y no cambiarle el número.")

    section("SmokeTest: el que de verdad protege")

    imprimirCodigo(
        """
        @Test
        fun `ninguna demo lanza excepciones`() {
            val fallos = mutableListOf<String>()

            silenciandoLaSalida {
                for (capitulo in chapters) {
                    for (demo in capitulo.demos) {
                        if (demo.skipInSmokeTest) continue
                        try {
                            demo.action()
                        } catch (e: Throwable) {
                            fallos += "${'$'}{demo.id} ${'$'}{demo.title}: ${'$'}e"
                        }
                    }
                }
            }

            assertTrue(fallos.isEmpty(), "demos que fallan:\n" + fallos.joinToString("\n"))
        }
        """.trimIndent(),
    )

    bullet("Llama a `demo.action()` y NO a `runDemo(demo)`: `runDemo` captura las")
    bullet("excepciones para que una demo rota no corte el recorrido, y eso aquí")
    bullet("escondería justo lo que queremos detectar.")
    bullet("")
    bullet("Con esto, cuatrocientas demos dejan de ser código que 'compila' y pasan")
    bullet("a ser código VERIFICADO en cada `./gradlew build`.")

    section("El detalle de silenciar la salida")

    bullet("Las demos imprimen miles de líneas. El test redirige `System.out` a un")
    bullet("buffer y lo restaura en un `finally`, para que el informe siga legible.")
    bullet("Si no restauras en `finally`, un fallo deja la consola rota para el resto")
    bullet("de la suite: un ejemplo perfecto de por qué existe `try/finally`.")

    section("Lo que NO se testea aquí, y por qué")

    bullet("La salida exacta por consola: cambiaría con cada retoque de redacción")
    bullet("y no aporta nada. Se comprueba que no falla, no lo que imprime.")
    bullet("Los tiempos de las demos de corrutinas: dependen de la máquina.")
    bullet("El orden de las corrutinas en paralelo: no es determinista.")

    section("Prueba a romperlo")

    bullet("Mete un `error(\"boom\")` en cualquier demo y ejecuta `./gradlew test`:")
    bullet("verás el identificador exacto de la demo en el mensaje de fallo.")
}

// =====================================================================================
//  EL CÓDIGO DE PRODUCCIÓN QUE PRUEBAN LOS TESTS
// =====================================================================================

/**
 * Reintenta una operación con espera creciente.
 *
 * Los tiempos por defecto son los reales (1 s, 2 s, 4 s). Un test con `runTest` los
 * recorre en tiempo virtual y tarda milisegundos; por eso NO hace falta inventarse
 * tiempos cortos "para poder testear".
 */
suspend fun reintentarConEspera(
    intentos: Int = 3,
    esperaInicialMs: Long = 1_000,
    operacion: suspend (numeroDeIntento: Int) -> String,
): String {
    require(intentos > 0) { "hay que permitir al menos un intento, no $intentos" }

    var espera = esperaInicialMs
    var ultimoFallo: Throwable? = null

    repeat(intentos) { indice ->
        try {
            return operacion(indice + 1)
        } catch (e: CancellationException) {
            throw e                      // la cancelación NUNCA se reintenta (cap. 28.16)
        } catch (e: Exception) {
            ultimoFallo = e
        }
        // No se espera después del último intento: sería tiempo tirado.
        if (indice < intentos - 1) {
            delay(espera)
            espera *= 2
        }
    }
    throw IllegalStateException("fallaron los $intentos intentos", ultimoFallo)
}

/**
 * Una cuenta atrás que emite un valor por intervalo.
 *
 * El intervalo es un parámetro con valor por defecto para poder acelerarlo en una
 * demo; en un test con tiempo virtual ni siquiera hace falta tocarlo.
 */
fun cuentaAtras(desde: Int, intervaloMs: Long = 1_000): Flow<Int> = flow {
    require(desde >= 0) { "la cuenta atrás no puede empezar en negativo: $desde" }
    for (valor in desde downTo 0) {
        emit(valor)
        if (valor > 0) delay(intervaloMs)
    }
}

/** La fuente de datos, aislada tras una interfaz de una sola función. */
fun interface FuenteDeTitulares {
    suspend fun cargar(): List<String>
}

/**
 * El dispatcher entra por el constructor: es lo que permite meterlo en tiempo virtual.
 */
class ServicioDeNoticias(
    private val fuente: FuenteDeTitulares,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend fun titularesRecientes(limite: Int): List<String> {
        require(limite > 0) { "el límite debe ser positivo, no $limite" }
        return withContext(dispatcher) { fuente.cargar().take(limite) }
    }
}

// -- Utilidad de la demo -----------------------------------------------------------------------

/** Imprime un bloque de código con sangría, sin los adornos de `bullet`. */
private fun imprimirCodigo(texto: String) {
    texto.trimEnd().lines().forEach { println("      $it") }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Abre `src/test/kotlin/com/alejandro/c30testing/CorrutinasTest.kt` y cambia
//     `runTest` por `runBlocking`: el test pasa a tardar segundos de verdad.
//  2. Escribe un test con `advanceTimeBy` que compruebe cuántos valores ha emitido
//     `cuentaAtras` a los 2,5 segundos virtuales.
//  3. Quita el parámetro `dispatcher` de `ServicioDeNoticias` y comprueba qué le pasa
//     al test que lo usaba.
//  4. Haz que `reintentarConEspera` acepte un `esperaMaximaMs` y testéalo.
