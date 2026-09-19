package com.alejandro.infra

// =====================================================================================
//  El registro de demos del repositorio.
//
//  Cada capítulo declara su lista de demos con el builder `chapter { ... }` y el
//  lanzador (Launcher.kt) las agrega todas. Si te fijas, este fichero es en sí mismo
//  un ejemplo de "lambda con receptor" y de DSL: exactamente lo que se explica en los
//  capítulos 15 y 29. Cuando llegues allí, vuelve a leerlo.
// =====================================================================================

/**
 * Una demo ejecutable: el trozo de código más pequeño que se puede lanzar por separado.
 *
 * @property id identificador estable con forma `"13.4"` (capítulo . posición). Lo
 *   genera [ChapterBuilder]; nunca se escribe a mano, y por eso no puede duplicarse.
 * @property title lo que se ve en el índice y en el menú.
 * @property skipInSmokeTest marca las demos que el test de humo no debe ejecutar
 *   (por ejemplo, una que dependa del entorno). Deberían ser poquísimas.
 * @property action el código del ejemplo.
 *
 * Fíjate en que [action] **no** es `suspend`: las demos de corrutinas abren su propio
 * `runBlocking` dentro. Es deliberado, para que la frontera entre código bloqueante y
 * código suspendido se vea siempre de forma explícita (capítulo 28).
 */
class Demo(
    val id: String,
    val title: String,
    val skipInSmokeTest: Boolean,
    val action: () -> Unit,
)

/**
 * Un capítulo del recorrido: un tema con sus demos.
 *
 * @property number 1..31, el orden pedagógico.
 * @property name título corto del capítulo.
 * @property summary una línea describiendo qué se cubre; se imprime al ejecutarlo.
 */
class Chapter(
    val number: Int,
    val name: String,
    val summary: String,
    val demos: List<Demo>,
) {
    /** `"07"`: el número con dos dígitos, para que el índice quede alineado. */
    val id: String get() = number.toString().padStart(2, '0')

    /** Busca una demo por su identificador completo (`"13.4"`). */
    fun find(demoId: String): Demo? = demos.firstOrNull { it.id == demoId }

    /** Ejecuta todas las demos del capítulo, en orden. */
    fun runAll() {
        heading("Capítulo $id · $name")
        println("  $summary")
        demos.forEach { runDemo(it) }
    }
}

/**
 * Ejecuta una demo aislando los fallos.
 *
 * El `runCatching` es importante: con ~150 demos, una sola excepción no controlada
 * abortaría el recorrido completo (`--args="all"`) y dejaría el resto sin ejecutar.
 * Aun así, la regla del repositorio es que **ninguna demo debe llegar aquí lanzando**:
 * los ejemplos que enseñan excepciones las capturan ellos mismos.
 */
fun runDemo(demo: Demo) {
    demoHeader(demo.id, demo.title)
    runCatching { demo.action() }
        .onFailure { e ->
            println()
            println("    ✗ La demo ${demo.id} lanzó ${e::class.simpleName}: ${e.message}")
        }
}

/**
 * Constructor de capítulos.
 *
 * `internal` en el constructor: se instancia sólo desde [chapter], nunca directamente.
 */
class ChapterBuilder internal constructor(private val number: Int) {

    private val demos = mutableListOf<Demo>()

    /**
     * Registra una demo del capítulo.
     *
     * El identificador se calcula solo a partir de la posición: `"13.1"`, `"13.2"`…
     * Escribirlos a mano en 31 ficheros índice sería una fuente segura de duplicados.
     */
    fun demo(title: String, action: () -> Unit, skipInSmokeTest: Boolean = false) {
        demos += Demo(
            id = "$number.${demos.size + 1}",
            title = title,
            skipInSmokeTest = skipInSmokeTest,
            action = action,
        )
    }

    internal fun build(name: String, summary: String): Chapter =
        Chapter(number, name, summary, demos.toList())
}

/**
 * Declara un capítulo.
 *
 * ```
 * val chapter13 = chapter(13, "Colecciones", "List, Set y Map") {
 *     demo("Creación y tipos", ::demoCreation)
 *     demo("Transformación", ::demoTransformation)
 * }
 * ```
 *
 * `block` es una **lambda con receptor** (`ChapterBuilder.() -> Unit`): dentro de las
 * llaves, `this` es el builder, por eso puedes llamar a `demo(...)` sin prefijo.
 */
fun chapter(
    number: Int,
    name: String,
    summary: String,
    block: ChapterBuilder.() -> Unit,
): Chapter = ChapterBuilder(number).apply(block).build(name, summary)
