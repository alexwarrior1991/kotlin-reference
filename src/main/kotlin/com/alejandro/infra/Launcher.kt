package com.alejandro.infra

import kotlin.time.Duration
import kotlin.time.measureTime

// =====================================================================================
//  El lanzador: convierte la lista de capítulos en un programa navegable.
//
//  Sin argumentos abre un menú interactivo; con argumentos ejecuta directamente lo que
//  le pidas. Ver `printUsage()` más abajo para la lista completa de comandos.
// =====================================================================================

/** Palabras que cierran el menú interactivo. */
private val QUIT_WORDS = setOf("q", "quit", "exit", "salir")

/** Palabras que vuelven al menú anterior. */
private val BACK_WORDS = setOf("b", "back", "atras", "atrás", "0")

class Launcher(private val chapters: List<Chapter>) {

    private val totalDemos: Int get() = chapters.sumOf { it.demos.size }

    /** "1 capítulo" / "31 capítulos". Detalle tonto, pero se lee en cada arranque. */
    private val chapterCount: String
        get() = "${chapters.size} capítulo" + if (chapters.size == 1) "" else "s"

    /** Punto de entrada. `args` viene tal cual de `fun main(args: Array<String>)`. */
    fun start(args: Array<String>) {
        if (args.isEmpty()) {
            interactiveMenu()
            return
        }
        when (val command = args.first().trim().lowercase().removePrefix("--")) {
            "help", "-h", "ayuda" -> printUsage()
            "list", "-l", "indice", "índice" -> printIndex()
            "all", "todo" -> runEverything()
            "search", "buscar" -> search(args.drop(1).joinToString(" "))
            else -> runTarget(command)
        }
    }

    // -- Ejecución -------------------------------------------------------------------

    /** Ejecuta un capítulo (`"13"`) o una demo concreta (`"13.4"`). */
    private fun runTarget(target: String) {
        if ('.' in target) {
            val chapterNumber = target.substringBefore('.').toIntOrNull()
            val demo = chapters.firstOrNull { it.number == chapterNumber }?.find(target)
            if (demo == null) {
                println("No existe la demo '$target'.")
                println("Prueba con `list` para ver el índice completo.")
                return
            }
            runDemo(demo)
        } else {
            val chapter = chapters.firstOrNull { it.number == target.toIntOrNull() }
            if (chapter == null) {
                println("No existe el capítulo '$target'. Los capítulos van del 1 al ${chapters.size}.")
                println("Prueba con `list` para ver el índice completo.")
                return
            }
            chapter.runAll()
        }
    }

    /**
     * Ejecuta absolutamente todo y cronometra cada capítulo.
     *
     * Es la comprobación de que el repositorio entero funciona: si alguna demo se
     * colgase o lanzase, se vería aquí. (`measureTime` es de `kotlin.time` y se explica
     * en el capítulo 25.)
     */
    private fun runEverything() {
        heading("kotlin-reference · ejecutando las $totalDemos demos")
        val times = mutableListOf<Pair<Chapter, Duration>>()
        val total = measureTime {
            chapters.forEach { chapter ->
                times += chapter to measureTime { chapter.runAll() }
            }
        }

        heading("Resumen de tiempos")
        times.forEach { (chapter, elapsed) ->
            show("${chapter.id} · ${chapter.name}", elapsed)
        }
        separator()
        show("TOTAL ($totalDemos demos en $chapterCount)", total)
    }

    // -- Consultas -------------------------------------------------------------------

    /** Índice completo: todos los capítulos con todas sus demos. */
    fun printIndex() {
        heading("kotlin-reference · índice de demos")
        chapters.forEach { chapter ->
            println()
            println("  ${chapter.id}. ${chapter.name} — ${chapter.summary}")
            chapter.demos.forEach { demo ->
                println("        ${demo.id.padEnd(7)} ${demo.title}")
            }
        }
        println()
        println("  $totalDemos demos en $chapterCount.")
        println("  Ejecuta una con:  ./gradlew run -q --console=plain --args=\"13.4\"")
    }

    /** Busca texto en los títulos de capítulos y demos. */
    private fun search(query: String) {
        val needle = query.trim()
        if (needle.isEmpty()) {
            println("Uso: search <texto>   (por ejemplo: search flow)")
            return
        }

        heading("Resultados para \"$needle\"")
        var hits = 0
        chapters.forEach { chapter ->
            val matches = chapter.demos.filter { it.title.contains(needle, ignoreCase = true) }
            val chapterMatches = chapter.name.contains(needle, ignoreCase = true) ||
                chapter.summary.contains(needle, ignoreCase = true)

            if (matches.isNotEmpty() || chapterMatches) {
                println()
                println("  ${chapter.id}. ${chapter.name}")
                // Si lo que coincide es el capítulo, enseñamos todas sus demos.
                val toShow = if (matches.isEmpty()) chapter.demos else matches
                toShow.forEach { println("        ${it.id.padEnd(7)} ${it.title}") }
                hits += toShow.size
            }
        }
        if (hits == 0) println("  Sin resultados.")
    }

    private fun printUsage() {
        heading("kotlin-reference · cómo ejecutar")
        println(
            """
            |
            |  Desde la línea de comandos (el prefijo es siempre
            |  `./gradlew run -q --console=plain --args="..."`):
            |
            |      list          índice completo de capítulos y demos
            |      all           ejecuta TODAS las demos, con tiempos
            |      13            ejecuta el capítulo 13 entero
            |      13.4          ejecuta sólo la demo 13.4
            |      search flow   busca "flow" en los títulos
            |      help          esta ayuda
            |
            |  Sin argumentos se abre un menú interactivo.
            |
            |  Desde IntelliJ: pulsa ▶ en el `main()` de cualquier fichero `CNN_00Index.kt`
            |  para ejecutar ese capítulo, o en `Main.kt` para abrir el menú.
            """.trimMargin()
        )
    }

    // -- Menú interactivo ------------------------------------------------------------

    /**
     * Menú de dos niveles: capítulo y después demo.
     *
     * Detalle importante: [readCommand] devuelve `null` cuando la entrada estándar
     * está cerrada (CI, una tubería vacía, `gradlew run` sin terminal). En ese caso
     * salimos ordenadamente en lugar de girar en un bucle infinito.
     */
    private fun interactiveMenu() {
        heading("kotlin-reference · $totalDemos demos en $chapterCount")
        println("  Escribe `help` para ver todos los comandos.")

        while (true) {
            printChapterList()
            val line = readCommand("Capítulo (1-${chapters.size}), demo (13.4), `all`, `search <texto>`, `q` para salir")
                ?: return

            when {
                line.isEmpty() -> continue
                line.lowercase() in QUIT_WORDS -> {
                    println("\n¡Hasta luego! Sigue practicando.")
                    return
                }
                line.lowercase() in setOf("help", "ayuda") -> printUsage()
                line.lowercase() in setOf("list", "indice", "índice") -> printIndex()
                line.lowercase() in setOf("all", "todo") -> runEverything()
                line.lowercase().startsWith("search ") ||
                    line.lowercase().startsWith("buscar ") -> search(line.substringAfter(' '))
                '.' in line -> runTarget(line)
                else -> {
                    val chapter = chapters.firstOrNull { it.number == line.toIntOrNull() }
                    if (chapter == null) {
                        println("  No reconozco '$line'. Escribe `help` si te pierdes.")
                    } else if (!chapterMenu(chapter)) {
                        return // entrada estándar cerrada dentro del submenú
                    }
                }
            }
        }
    }

    /**
     * Submenú de un capítulo.
     *
     * @return `false` si la entrada estándar se cerró y hay que salir del programa.
     */
    private fun chapterMenu(chapter: Chapter): Boolean {
        while (true) {
            heading("Capítulo ${chapter.id} · ${chapter.name}")
            println("  ${chapter.summary}")
            println()
            chapter.demos.forEachIndexed { index, demo ->
                println("      ${(index + 1).toString().padStart(2)}. ${demo.title}   [${demo.id}]")
            }

            val line = readCommand("Demo (1-${chapter.demos.size}), `a` para todas, `0` para volver")
                ?: return false

            when {
                line.isEmpty() -> continue
                line.lowercase() in QUIT_WORDS -> return false
                line.lowercase() in BACK_WORDS -> return true
                line.lowercase() in setOf("a", "all", "todas") -> chapter.runAll()
                else -> {
                    val demo = line.toIntOrNull()?.let { chapter.demos.getOrNull(it - 1) }
                    if (demo == null) println("  No reconozco '$line'.") else runDemo(demo)
                }
            }
        }
    }

    private fun printChapterList() {
        println()
        separator()
        chapters.forEach { chapter ->
            println("  ${chapter.id}. ${chapter.name.padEnd(28)} (${chapter.demos.size} demos)")
        }
        separator()
    }

    /**
     * Lee una orden del usuario.
     *
     * @return el texto introducido, o `null` si no hay entrada interactiva disponible.
     */
    private fun readCommand(hint: String): String? {
        println()
        println(hint)
        print("> ")
        System.out.flush()

        val line = readlnOrNull()
        if (line == null) {
            // `readlnOrNull()` devuelve null en EOF. Pasa al ejecutar en CI, al hacer
            // `echo "" | gradlew run`, o si se olvida `standardInput = System.in`.
            println()
            println("  (no hay entrada interactiva disponible)")
            println("  Te dejo el índice; usa --args=\"13.4\" para ir directo a una demo.")
            printIndex()
            return null
        }
        return line.trim()
    }
}
