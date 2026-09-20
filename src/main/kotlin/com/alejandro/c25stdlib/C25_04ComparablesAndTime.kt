package com.alejandro.c25stdlib

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

// =====================================================================================
//  25.4 · Comparable, Comparator, use, medición del tiempo y Random
//
//  QUÉ ES
//    El resto de piezas de la biblioteca estándar que conviene tener a mano: orden,
//    gestión de recursos, `kotlin.time` y números aleatorios reproducibles.
//
//  POR QUÉ IMPORTA
//    `use` es la forma correcta de cerrar cualquier recurso y casi nadie la conoce
//    hasta que se lo enseñan. Y `Duration` de `kotlin.time` sustituye a los `Long`
//    de milisegundos que nunca se sabe si son milisegundos o segundos.
//
//  ERRORES COMUNES
//    · Cerrar recursos a mano en un `finally` en lugar de usar `use`.
//    · Medir con `System.currentTimeMillis()`, que puede saltar hacia atrás si el
//      reloj del sistema se ajusta.
//    · Usar `Random` sin semilla en un test y tener fallos intermitentes.
// =====================================================================================

private data class Cancion(val titulo: String, val artista: String, val duracionSegundos: Int)

private val playlist = listOf(
    Cancion("Bohemian Rhapsody", "Queen", 355),
    Cancion("Imagine", "Lennon", 183),
    Cancion("Hey Jude", "Beatles", 431),
    Cancion("Yesterday", "Beatles", 125),
)

/**
 * `Comparable` y `Comparator`.
 */
fun demoComparables() {
    section("Comparable: el orden NATURAL del tipo")

    // Implementar Comparable define "el" orden del tipo: el que se usa por defecto.
    val versiones = listOf(Version(1, 2, 3), Version(1, 10, 0), Version(1, 2, 10))
    show("sin ordenar", versiones)
    show("sorted()", versiones.sorted())
    show("max()", versiones.max())

    bullet("Fíjate en que 1.10.0 va DESPUÉS de 1.2.10: comparamos números, no texto.")
    bullet("Ordenar versiones como cadenas es un bug clásico: \"1.10\" < \"1.2\".")
    show("como texto (mal)", listOf("1.2.3", "1.10.0", "1.2.10").sorted())

    section("Comparator: órdenes ALTERNATIVOS")

    show("por título", playlist.sortedBy { it.titulo }.map { it.titulo })
    show("por duración desc", playlist.sortedByDescending { it.duracionSegundos }.map { it.titulo })

    section("Comparadores compuestos")

    val porArtistaYDuracion = compareBy<Cancion> { it.artista }
        .thenByDescending { it.duracionSegundos }

    playlist.sortedWith(porArtistaYDuracion).forEach {
        show(it.artista, "${it.titulo} (${it.duracionSegundos}s)")
    }

    bullet("`thenBy` sólo entra en juego cuando el criterio anterior empata.")

    section("Utilidades de comparación")

    show("compareValues(3, 9)", compareValues(3, 9))
    show("compareValuesBy por longitud", compareValuesBy("abc", "de") { it.length })
    show("maxOf con comparador", maxOf(playlist[0], playlist[1], compareBy { it.duracionSegundos }).titulo)
    show("naturalOrder<Int>()", listOf(3, 1, 2).sortedWith(naturalOrder()))
    show("reverseOrder<Int>()", listOf(3, 1, 2).sortedWith(reverseOrder()))
}

/**
 * `use`: cerrar recursos sin olvidarse.
 */
fun demoUse() {
    section("El problema")

    bullet("Todo recurso (fichero, conexión, socket) hay que cerrarlo SIEMPRE,")
    bullet("incluso si el código que lo usa lanza una excepción.")

    section("A mano, con try/finally")

    val recursoManual = RecursoSimulado("manual")
    val contenidoManual = try {
        recursoManual.leer()
    } finally {
        recursoManual.close()
    }
    show("resultado", contenidoManual)
    show("¿se cerró?", recursoManual.estaCerrado)

    bullet("Funciona, pero hay que acordarse. Y con dos recursos anidados, el")
    bullet("try/finally doble se vuelve ilegible.")

    section("Con use")

    val recurso = RecursoSimulado("con-use")
    val contenido = recurso.use { it.leer() }
    show("resultado", contenido)
    show("¿se cerró?", recurso.estaCerrado)

    bullet("`use` cierra al salir del bloque, pase lo que pase.")

    section("Cierra incluso si el bloque lanza")

    val conFallo = RecursoSimulado("con-fallo")
    val resultado = runCatching {
        conFallo.use { error("fallo dentro del bloque") }
    }
    show("el bloque lanzó", resultado.exceptionOrNull()?.message)
    show("¿se cerró igualmente?", conFallo.estaCerrado)

    bullet("Es lo que hace que `use` sea de verdad seguro.")

    section("Varios recursos anidados")

    val entrada = RecursoSimulado("entrada")
    val salida = RecursoSimulado("salida")

    val copiado = entrada.use { origen ->
        salida.use { destino ->
            destino.escribir(origen.leer())
        }
    }
    show("copiado", copiado)
    show("los dos cerrados", "${entrada.estaCerrado} y ${salida.estaCerrado}")

    bullet("Se cierran en orden inverso, igual que con try-with-resources de Java.")
    bullet("El capítulo 26 lo aplica a ficheros de verdad.")
}

/**
 * `kotlin.time`: Duration y medición.
 */
fun demoTime() {
    section("Duration: una duración con unidades")

    val corta = 250.milliseconds
    val larga = 2.seconds

    show("250.milliseconds", corta)
    show("2.seconds", larga)
    show("suma", corta + larga)
    show("comparación", corta < larga)
    show("en milisegundos", larga.inWholeMilliseconds)
    show("en segundos (enteros)", larga.inWholeSeconds)

    bullet("Un `Long` de milisegundos no dice si son milis, micros o segundos.")
    bullet("Un `Duration` lo lleva dentro y no se puede confundir.")

    section("measureTime: cuánto tardó")

    val transcurrido = measureTime {
        (1..100_000).sum()
    }
    // No mostramos el número exacto: variaría en cada ejecución.
    show("measureTime { }", if (transcurrido.inWholeNanoseconds >= 0) "devolvió una Duration" else "?")
    show("¿tardó menos de un segundo?", transcurrido < 1.seconds)

    section("measureTimedValue: el tiempo Y el resultado")

    val medido = measureTimedValue {
        (1..100_000).sum()
    }
    show("valor calculado", medido.value)
    show("¿se midió el tiempo?", medido.duration >= 0.milliseconds)

    bullet("`measureTime` devuelve sólo la duración; `measureTimedValue`, las dos cosas.")

    section("TimeSource: para medir en varios puntos")

    val marca = TimeSource.Monotonic.markNow()
    (1..10_000).sum()
    val primeraLectura = marca.elapsedNow()
    (1..10_000).sum()
    val segundaLectura = marca.elapsedNow()

    show("la segunda lectura es mayor o igual", segundaLectura >= primeraLectura)

    bullet("`Monotonic` es un reloj que SÓLO avanza: no le afectan los ajustes de")
    bullet("hora del sistema ni el cambio horario.")
    bullet("Por eso nunca midas con `System.currentTimeMillis()`: ése puede saltar")
    bullet("hacia atrás y darte duraciones negativas.")

    section("El aviso de siempre sobre las mediciones")

    bullet("La JVM optimiza sobre la marcha: la primera ejecución es más lenta.")
    bullet("Para medir en serio, JMH. `measureTime` sirve para hacerse una idea.")
}

/**
 * `Random` reproducible.
 */
fun demoRandom() {
    section("Random con semilla: siempre la misma secuencia")

    // Con semilla fija, la secuencia es idéntica en cada ejecución. Es lo que hace
    // que esta demo (y todo este repositorio) tenga una salida reproducible.
    val aleatorio = Random(42)

    show("nextInt()", aleatorio.nextInt(1, 100))
    show("nextInt()", aleatorio.nextInt(1, 100))
    show("nextInt()", aleatorio.nextInt(1, 100))

    val otroConLaMismaSemilla = Random(42)
    show("otro Random(42), primer valor", otroConLaMismaSemilla.nextInt(1, 100))

    bullet("Misma semilla, misma secuencia. Imprescindible en tests.")

    section("Sin semilla: distinto en cada ejecución")

    bullet("`Random.nextInt()` usa una semilla del sistema.")
    bullet("En un test eso produce fallos intermitentes imposibles de reproducir.")
    bullet("Regla: en tests y en demos, SIEMPRE con semilla.")

    section("Otras operaciones")

    val conSemilla = Random(7)
    show("nextDouble()", "%.4f".format(conSemilla.nextDouble()))
    show("nextBoolean()", conSemilla.nextBoolean())
    show("lista.random(Random(1))", listOf("a", "b", "c", "d").random(Random(1)))
    show("lista.shuffled(Random(1))", listOf(1, 2, 3, 4, 5).shuffled(Random(1)))
    show("(1..100).random(Random(1))", (1..100).random(Random(1)))

    bullet("`random()` y `shuffled()` aceptan un Random: úsalo para reproducibilidad.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

/** Orden natural correcto: compara número a número, no como texto. */
private data class Version(val mayor: Int, val menor: Int, val parche: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int = compareValuesBy(
        this, other,
        { it.mayor },
        { it.menor },
        { it.parche },
    )

    override fun toString(): String = "$mayor.$menor.$parche"
}

/** Un recurso de juguete: demuestra `use` sin tocar el disco. */
private class RecursoSimulado(private val nombre: String) : java.io.Closeable {

    var estaCerrado: Boolean = false
        private set

    private var contenido: String = "datos de $nombre"

    fun leer(): String = contenido

    fun escribir(texto: String): String {
        contenido = texto
        return "escrito en $nombre: '$texto'"
    }

    override fun close() {
        estaCerrado = true
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia la semilla de Random(42) y comprueba que la secuencia cambia entera.
//  2. Quita el `use` y el `finally` de un recurso y mira que nunca se cierra.
//  3. Ordena `playlist` por artista ascendente y título descendente.
//  4. Sustituye un `Long` de milisegundos de tu código por un `Duration`.
