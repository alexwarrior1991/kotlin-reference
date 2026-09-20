package com.alejandro.c10enums

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  10.1 · enum class: constantes con comportamiento
//
//  QUÉ ES
//    Un tipo con un conjunto FIJO y conocido de valores. En Kotlin (como en Java) un
//    enum es una clase de verdad: puede tener constructor, propiedades, métodos, e
//    incluso una implementación distinta por constante.
//
//  POR QUÉ IMPORTA
//    Sustituye a las constantes sueltas (`const val ROJO = 1`) por algo que el
//    compilador entiende: no se puede pasar un valor inválido, y el `when` sobre un
//    enum puede comprobarse exhaustivo.
//
//  ERRORES COMUNES
//    · Guardar `ordinal` en base de datos o en un fichero: cambia si reordenas.
//    · Usar `valueOf` sin capturar la excepción cuando el texto viene de fuera.
//    · Meter en un enum casos que llevan datos distintos: eso es `sealed` (cap. 11).
// =====================================================================================

/**
 * El enum más simple.
 */
fun demoSimpleEnum() {
    section("Declaración")

    bullet("enum class Direccion { NORTE, SUR, ESTE, OESTE }")

    val direccion = Direccion.NORTE
    show("Direccion.NORTE", direccion)
    show("su nombre", direccion.name)
    show("su posición", direccion.ordinal)

    section("Todas las constantes")

    show("Direccion.entries", Direccion.entries)
    show("cuántas hay", Direccion.entries.size)

    section("Son singletons")

    show("NORTE === NORTE", Direccion.NORTE === Direccion.NORTE)
    show("se comparan con ==", Direccion.NORTE == Direccion.NORTE)
    bullet("Cada constante existe una sola vez, así que `==` y `===` dan lo mismo.")
    bullet("Aun así, usa `==`: es lo idiomático y funciona igual.")
}

/**
 * Enums con propiedades: el caso más útil.
 */
fun demoEnumWithProperties() {
    section("Constructor y propiedades")

    // Cada constante pasa sus argumentos al constructor del enum.
    Planeta.entries.forEach { planeta ->
        show(planeta.name, "masa=${planeta.masaRelativa} radio=${planeta.radioRelativo}")
    }

    section("Métodos que usan esas propiedades")

    show("Tierra.gravedadRelativa()", "%.2f".format(Planeta.TIERRA.gravedadRelativa()))
    show("Marte.gravedadRelativa()", "%.2f".format(Planeta.MARTE.gravedadRelativa()))
    show("peso de 70 kg en Marte", "%.1f kg".format(Planeta.MARTE.pesoDe(70.0)))

    section("Propiedades calculadas")

    show("Planeta.TIERRA.esHabitable", Planeta.TIERRA.esHabitable)
    show("Planeta.JUPITER.esHabitable", Planeta.JUPITER.esHabitable)

    section("Otro ejemplo: códigos con significado")

    EstadoHttp.entries.forEach { estado ->
        show("${estado.codigo} ${estado.name}", "¿es error? ${estado.esError()}")
    }

    show("EstadoHttp.desdeCodigo(404)", EstadoHttp.desdeCodigo(404))
    show("EstadoHttp.desdeCodigo(999)", EstadoHttp.desdeCodigo(999))
    bullet("La búsqueda por código va en el `companion object` del enum.")
}

/**
 * Cuerpo propio por constante.
 */
fun demoConstantSpecificBodies() {
    section("Cada constante con su propia implementación")

    // Cuando el comportamiento cambia por constante, se puede sobrescribir el método
    // dentro de las llaves de cada una. Es el "patrón estrategia" sin clases extra.
    val a = 12.0
    val b = 4.0
    Operacion.entries.forEach { operacion ->
        show("$a ${operacion.simbolo} $b", operacion.aplicar(a, b))
    }

    section("Con un método abstracto")

    bullet("`abstract fun aplicar(...)` obliga a que TODAS las constantes lo definan.")
    bullet("Si añades una constante nueva y olvidas el cuerpo, no compila.")

    section("Cuándo usarlo")

    bullet("Sí: operaciones matemáticas, políticas de reintento, formatos de salida.")
    bullet("No: si el cuerpo pasa de unas pocas líneas, saca la lógica a otra clase.")
    bullet("No: si los casos necesitan DATOS distintos → `sealed` (capítulo 11).")
}

/**
 * Enums que implementan interfaces.
 */
fun demoEnumImplementingInterface() {
    section("Un enum puede implementar interfaces")

    // Así se puede usar allí donde se espere la interfaz, sin saber que es un enum.
    val niveles: List<Registrable> = Nivel.entries
    niveles.forEach { show(it.etiqueta(), it.prefijo()) }

    section("Combinado con cuerpo por constante")

    show("Nivel.ERROR.deberiaAlertar()", Nivel.ERROR.deberiaAlertar())
    show("Nivel.DEBUG.deberiaAlertar()", Nivel.DEBUG.deberiaAlertar())

    section("Un enum no puede heredar de una clase")

    bullet("Ya hereda de `Enum<T>`, y la JVM sólo permite una superclase.")
    bullet("Interfaces, todas las que quieras.")

    section("Los enums son Comparable por ordinal")

    show("Nivel.entries ordenados", Nivel.entries.sorted())
    show("DEBUG < ERROR", Nivel.DEBUG < Nivel.ERROR)
    bullet("El orden es el de DECLARACIÓN. Si te importa, decláralos en ese orden.")
    bullet("Y si te importa de verdad, mejor una propiedad explícita que el ordinal.")
}

// -- Los enums que usan las demos -----------------------------------------------------

private enum class Direccion { NORTE, SUR, ESTE, OESTE }

/** Enum con constructor, propiedades, métodos y propiedad calculada. */
private enum class Planeta(val masaRelativa: Double, val radioRelativo: Double) {
    // Cada constante invoca al constructor.
    MERCURIO(0.055, 0.383),
    TIERRA(1.0, 1.0),
    MARTE(0.107, 0.532),
    JUPITER(317.8, 11.21);      // ← el punto y coma es OBLIGATORIO si hay más miembros

    /** Método normal, disponible en todas las constantes. */
    fun gravedadRelativa(): Double = masaRelativa / (radioRelativo * radioRelativo)

    fun pesoDe(kilos: Double): Double = kilos * gravedadRelativa()

    /** Propiedad calculada. */
    val esHabitable: Boolean
        get() = this == TIERRA
}

/** Un enum con una fábrica en su companion object. */
private enum class EstadoHttp(val codigo: Int) {
    OK(200),
    NO_ENCONTRADO(404),
    ERROR_SERVIDOR(500);

    fun esError(): Boolean = codigo >= 400

    companion object {
        /** Devuelve null en lugar de lanzar: el código puede venir de fuera. */
        fun desdeCodigo(codigo: Int): EstadoHttp? =
            EstadoHttp.entries.find { it.codigo == codigo }
    }
}

/** Cuerpo propio por constante: el patrón estrategia sin clases extra. */
private enum class Operacion(val simbolo: String) {
    SUMA("+") {
        override fun aplicar(a: Double, b: Double): Double = a + b
    },
    RESTA("-") {
        override fun aplicar(a: Double, b: Double): Double = a - b
    },
    MULTIPLICACION("*") {
        override fun aplicar(a: Double, b: Double): Double = a * b
    },
    DIVISION("/") {
        // Cada constante puede tener su propia lógica, incluida la validación.
        override fun aplicar(a: Double, b: Double): Double =
            if (b == 0.0) Double.NaN else a / b
    };

    /** Abstracto: obliga a que toda constante nueva lo implemente. */
    abstract fun aplicar(a: Double, b: Double): Double
}

private interface Registrable {
    fun etiqueta(): String
    fun prefijo(): String
}

/** Un enum que implementa una interfaz y además tiene cuerpo por constante. */
private enum class Nivel(private val abreviatura: String) : Registrable {
    DEBUG("D"),
    INFO("I"),
    AVISO("W"),
    ERROR("E") {
        override fun deberiaAlertar(): Boolean = true
    };

    override fun etiqueta(): String = name.lowercase()
    override fun prefijo(): String = "[$abreviatura]"

    /** Por defecto no alerta; ERROR lo sobrescribe. */
    open fun deberiaAlertar(): Boolean = false
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade POTENCIA("^") a Operacion sin implementar `aplicar` y lee el error.
//  2. Quita el punto y coma tras JUPITER y comprueba que es obligatorio.
//  3. Reordena las constantes de Nivel y observa cómo cambia `sorted()`.
//  4. Intenta hacer `enum class X : AlgunaClase()` y lee por qué no se puede.
