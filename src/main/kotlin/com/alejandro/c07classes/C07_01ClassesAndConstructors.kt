package com.alejandro.c07classes

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  7.1 · Clases, constructores e inicialización
//
//  QUÉ ES
//    Cómo se declara una clase en Kotlin: constructor primario en la propia cabecera,
//    bloques `init`, y constructores secundarios cuando de verdad hacen falta.
//
//  POR QUÉ IMPORTA
//    El constructor primario declara los parámetros Y las propiedades en la misma
//    línea. Lo que en Java son 20 líneas de campos, constructor y asignaciones, aquí
//    es una. Pero el ORDEN de inicialización tiene una trampa clásica que conviene
//    conocer antes de tropezar con ella.
//
//  ERRORES COMUNES
//    · Olvidar el `val`/`var` en el constructor primario: entonces el parámetro no se
//      guarda como propiedad y sólo se puede usar en los `init`.
//    · Escribir constructores secundarios donde bastaban valores por defecto.
//    · Llamar a un método `open` desde el constructor (la subclase aún no está lista).
// =====================================================================================

/**
 * El constructor primario.
 */
fun demoPrimaryConstructor() {
    section("La forma más corta")

    // `class Punto(val x: Int, val y: Int)` declara la clase, el constructor y dos
    // propiedades de sólo lectura. En Java serían unas 20 líneas.
    val punto = Punto(3, 4)
    show("Punto(3, 4)", punto)
    show("punto.x", punto.x)

    section("val, var o nada")

    // val → propiedad de sólo lectura
    // var → propiedad modificable
    // sin nada → sólo un parámetro del constructor, NO se guarda
    val contador = Contador(inicial = 10)
    contador.incrementar()
    contador.incrementar()
    show("Contador(10) tras dos incrementos", contador.valor)
    show("el parámetro `etiqueta` sí se usó...", contador.descripcion)
    bullet("`etiqueta` no lleva val/var: sólo vive durante la construcción.")

    section("Valores por defecto en el constructor")

    show("Servidor()", Servidor())
    show("Servidor(\"api.ejemplo.com\")", Servidor("api.ejemplo.com"))
    show("Servidor(puerto = 8080)", Servidor(puerto = 8080))

    bullet("Valores por defecto + argumentos nombrados evitan media docena de constructores.")

    section("Cuando el constructor lleva anotaciones o visibilidad")

    // Normalmente se escribe `class X(...)`. Si hace falta una anotación o un
    // modificador de visibilidad, aparece la palabra `constructor`:
    bullet("class Repositorio private constructor(val url: String)")
    bullet("Eso obliga a construir el objeto desde una factoría (ver 7.7).")
}

/**
 * Bloques `init` y orden de inicialización.
 */
fun demoInitBlocks() {
    section("Para qué sirve init")

    // El constructor primario no tiene cuerpo: si hay que validar o calcular algo,
    // se hace en un bloque `init`.
    val valido = Temperatura(25.0)
    show("Temperatura(25.0)", valido)

    val invalido = try {
        Temperatura(-500.0).toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("Temperatura(-500.0)", invalido)

    section("El orden: declaración de arriba abajo")

    // Los inicializadores de propiedad y los bloques `init` se ejecutan INTERCALADOS,
    // en el orden en el que aparecen escritos. No todos los init primero.
    println()
    val trazado = OrdenDeInicializacion("Ana")
    show("resultado final", trazado.resumen)

    section("La trampa clásica")

    // Una propiedad no puede usar otra que se declare MÁS ABAJO: en ese momento
    // todavía no tiene valor.
    //
    //     class Roto {
    //         val doble = base * 2   // ERROR: Variable 'base' must be initialized
    //         val base = 10
    //     }
    bullet("Una propiedad sólo puede usar las declaradas ANTES que ella.")
    bullet("El compilador lo detecta dentro de la clase, pero no siempre a través de")
    bullet("llamadas a métodos: ahí es donde aparecen los nulos inesperados.")

    show("Perezoso().doble (usando by lazy)", Perezoso().doble)
    bullet("`by lazy` (capítulo 18) resuelve el caso en el que el orden es inevitable.")
}

/**
 * Constructores secundarios.
 */
fun demoSecondaryConstructors() {
    section("Cuándo hacen falta de verdad")

    // Los valores por defecto cubren el 90% de los casos. Un constructor secundario
    // se justifica cuando hay que TRANSFORMAR la entrada, no sólo rellenar huecos.
    val desdeComponentes = Fecha(2026, 9, 19)
    val desdeTexto = Fecha("2026-09-19")

    show("Fecha(2026, 9, 19)", desdeComponentes)
    show("Fecha(\"2026-09-19\")", desdeTexto)
    show("¿son iguales?", desdeComponentes.toString() == desdeTexto.toString())

    section("Deben delegar en el primario")

    bullet("constructor(texto: String) : this(año, mes, día) { ... }")
    bullet("El `: this(...)` es obligatorio si hay constructor primario.")
    bullet("Así la validación del `init` se ejecuta siempre, vengas por donde vengas.")

    section("El orden completo")

    println()
    OrdenConSecundario("desde el secundario", extra = 42)

    bullet("1. Inicializadores de propiedad e `init`, en orden de declaración.")
    bullet("2. Cuerpo del constructor secundario.")
    bullet("Es decir: el `init` se ejecuta ANTES que el cuerpo del secundario.")

    section("La alternativa idiomática: funciones de fábrica")

    // En vez de un constructor secundario, muchas veces se prefiere una función en el
    // companion object: puede tener nombre, devolver null y hasta cachear. Ver 7.7.
    show("Fecha.hoyFicticia()", Fecha.hoyFicticia())
    show("Fecha.desdeTextoONull(\"basura\")", Fecha.desdeTextoONull("basura"))
    show("Fecha.desdeTextoONull(\"2020-01-02\")", Fecha.desdeTextoONull("2020-01-02"))
}

// -- Las clases que usan las demos ----------------------------------------------------

/** Constructor primario con dos propiedades de sólo lectura. */
private class Punto(val x: Int, val y: Int) {
    override fun toString(): String = "($x, $y)"
}

/**
 * `inicial` sin val/var: sólo existe durante la construcción.
 * `etiqueta` igual, pero se usa para calcular una propiedad.
 */
private class Contador(inicial: Int, etiqueta: String = "contador") {
    var valor: Int = inicial
        private set                                  // sólo la clase puede cambiarlo

    val descripcion: String = "$etiqueta empezó en $inicial"

    fun incrementar() {
        valor++
    }
}

private class Servidor(val host: String = "localhost", val puerto: Int = 443) {
    override fun toString(): String = "$host:$puerto"
}

/** Validación en `init`: si el objeto no puede existir, que no se construya. */
private class Temperatura(val grados: Double) {
    init {
        require(grados >= -273.15) { "$grados °C está por debajo del cero absoluto" }
    }

    override fun toString(): String = "$grados °C"
}

/** Demuestra que inicializadores y bloques init se intercalan en orden de escritura. */
private class OrdenDeInicializacion(nombre: String) {

    private val pasos = mutableListOf<String>()

    // 1) Inicializador de propiedad
    private val primera = registrar("1. inicializador de `primera`")

    // 2) Bloque init
    init {
        registrar("2. primer bloque init (nombre='$nombre')")
    }

    // 3) Otro inicializador
    private val segunda = registrar("3. inicializador de `segunda`")

    // 4) Otro init
    init {
        registrar("4. segundo bloque init")
    }

    val resumen: String get() = "${pasos.size} pasos, en este orden"

    private fun registrar(paso: String): String {
        pasos.add(paso)
        println("      $paso")
        return paso
    }
}

/** `by lazy` difiere el cálculo hasta el primer uso, esquivando el problema de orden. */
private class Perezoso {
    // `doble` se calcula la primera vez que se lee, cuando `base` ya existe.
    val doble: Int by lazy { base * 2 }
    val base = 10
}

/** Constructor secundario que transforma la entrada. */
private class Fecha(val anio: Int, val mes: Int, val dia: Int) {

    init {
        require(mes in 1..12) { "mes inválido: $mes" }
        require(dia in 1..31) { "día inválido: $dia" }
    }

    /** Debe delegar en el primario con `: this(...)`. */
    constructor(texto: String) : this(
        texto.substringBefore('-').toInt(),
        texto.substringAfter('-').substringBefore('-').toInt(),
        texto.substringAfterLast('-').toInt(),
    )

    override fun toString(): String =
        "%04d-%02d-%02d".format(anio, mes, dia)

    companion object {
        /** Una fábrica puede tener nombre; un constructor, no. */
        fun hoyFicticia(): Fecha = Fecha(2026, 9, 19)

        /** Y puede devolver null, cosa que un constructor no puede hacer. */
        fun desdeTextoONull(texto: String): Fecha? = runCatching { Fecha(texto) }.getOrNull()
    }
}

/** Muestra que el `init` va antes que el cuerpo del constructor secundario. */
private class OrdenConSecundario(descripcion: String) {

    init {
        println("      1. init (siempre primero)")
    }

    constructor(descripcion: String, extra: Int) : this(descripcion) {
        println("      3. cuerpo del secundario con extra=$extra")
    }

    init {
        println("      2. segundo init, con descripcion='$descripcion'")
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `val` de `class Punto(val x: Int, ...)` y mira qué deja de compilar.
//  2. Mueve el `val base = 10` de Perezoso encima de `doble` y quita el `by lazy`.
//  3. Cambia el orden de los init de OrdenDeInicializacion y vuelve a ejecutar la demo.
//  4. Añade un tercer constructor a Fecha que acepte un timestamp en Long.
