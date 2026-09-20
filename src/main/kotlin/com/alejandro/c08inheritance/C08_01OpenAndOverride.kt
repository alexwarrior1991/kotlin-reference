package com.alejandro.c08inheritance

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  8.1 · open, override y super
//
//  QUÉ ES
//    Cómo se hereda en Kotlin. La diferencia de partida con Java: aquí las clases y
//    los métodos son FINALES por defecto; para poder heredar hay que abrirlos con
//    `open`.
//
//  POR QUÉ IMPORTA
//    Es la aplicación directa del consejo de Joshua Bloch: "diseña para la herencia o
//    prohíbela". Si una clase no se pensó para ser extendida, heredar de ella rompe
//    cosas en cuanto cambia su implementación. Kotlin convierte ese consejo en una
//    regla del compilador.
//
//  ERRORES COMUNES
//    · Abrir clases "por si acaso": cada `open` es un compromiso que hay que mantener.
//    · Llamar a un método `open` desde el constructor: la subclase todavía no está
//      inicializada y verá sus propiedades a null o a cero.
//    · Usar herencia para reutilizar código en lugar de composición (ver 8.3).
// =====================================================================================

/**
 * `final` por defecto, `open` para permitir la herencia.
 */
fun demoOpenAndFinal() {
    section("Una clase normal no se puede extender")

    // class Hija : Vehiculo()      // ERROR: This type is final, so it cannot be inherited from
    bullet("`class Vehiculo` es final: nadie puede heredar de ella.")
    bullet("`open class Vehiculo` sí lo permite.")

    section("Los métodos también")

    val coche = Coche("Ibiza", 4)
    show("coche.describir()", coche.describir())
    show("coche.arrancar()", coche.arrancar())

    bullet("`arrancar()` es `open` en Vehiculo y Coche la sobrescribe.")
    bullet("`matricular()` NO es open: aunque la clase lo sea, el método está cerrado.")

    section("La jerarquía completa")

    val vehiculos = listOf(
        Vehiculo("genérico"),
        Coche("Ibiza", 4),
        CocheElectrico("Model 3", 4, 75),
    )
    vehiculos.forEach { show(it.nombre, it.arrancar()) }

    bullet("Polimorfismo: la misma llamada ejecuta la implementación de cada clase.")
}

/**
 * `override`: obligatorio y explícito.
 */
fun demoOverride() {
    section("`override` no es opcional")

    bullet("En Java, @Override es una anotación opcional que sólo ayuda al compilador.")
    bullet("En Kotlin es una PALABRA CLAVE obligatoria: sin ella no compila.")
    bullet("Así es imposible sobrescribir algo por accidente, ni fallar al intentarlo.")

    section("Un método sobrescrito sigue siendo open")

    val electrico = CocheElectrico("Model 3", 4, 75)
    show("CocheElectrico.arrancar()", electrico.arrancar())

    bullet("`override fun arrancar()` en Coche sigue siendo `open` para CocheElectrico.")
    bullet("Para cerrar la cadena hay que escribir `final override fun arrancar()`.")

    show("frenar() está cerrado con final override", electrico.frenar())

    section("Sobrescribir propiedades")

    show("coche.ruedas", Coche("Ibiza", 4).ruedas)
    show("moto.ruedas (propiedad sobrescrita)", Moto("Vespa").ruedas)

    bullet("Un `val` se puede sobrescribir con un `var` (se le añade setter).")
    bullet("Al revés no: un `var` no se puede reducir a `val`.")
}

/**
 * `super`: llamar a la implementación del padre.
 */
fun demoSuper() {
    section("Extender en lugar de reemplazar")

    val electrico = CocheElectrico("Model 3", 4, 75)
    show("describir() encadenando super", electrico.describir())

    bullet("`super.describir()` ejecuta la versión de la clase padre.")
    bullet("Es lo que permite AÑADIR comportamiento en vez de sustituirlo.")

    section("super en el constructor")

    bullet("`class Coche(nombre: String) : Vehiculo(nombre)` ya es la llamada al super.")
    bullet("Con constructores secundarios se usa `: super(...)` explícitamente.")

    section("La trampa: métodos open en el constructor")

    // El constructor del padre se ejecuta ANTES de inicializar la subclase. Si llama
    // a un método open, se ejecuta la versión de la hija cuando sus propiedades
    // todavía no tienen valor.
    println()
    val peligrosa = SubclasePeligrosa()
    show("valor visto desde el constructor del padre", peligrosa.vistoEnConstruccion)
    show("valor real tras construir", peligrosa.etiqueta)

    bullet("El padre vio `null` porque la propiedad de la hija aún no existía.")
    bullet("Regla: NUNCA llames a un método `open` desde un constructor o un `init`.")
}

// -- La jerarquía que usan las demos --------------------------------------------------

/** `open` permite heredar. Sin ella, la clase sería final. */
private open class Vehiculo(val nombre: String) {

    /** Sólo los miembros marcados `open` se pueden sobrescribir. */
    open val ruedas: Int = 4

    open fun arrancar(): String = "$nombre arranca"

    open fun describir(): String = "$nombre con $ruedas ruedas"

    /** Sin `open`: cerrado aunque la clase esté abierta. */
    fun matricular(): String = "$nombre matriculado"
}

private open class Coche(nombre: String, override val ruedas: Int) : Vehiculo(nombre) {

    override fun arrancar(): String = "$nombre gira la llave y arranca"

    override fun describir(): String = "coche ${super.describir()}"

    /** `final override` cierra la cadena: nadie más puede sobrescribirlo. */
    open fun frenar(): String = "$nombre frena"
}

private class CocheElectrico(
    nombre: String,
    ruedas: Int,
    val kilovatios: Int,
) : Coche(nombre, ruedas) {

    override fun arrancar(): String = "$nombre arranca en silencio"

    /** Añade a lo que ya hacía el padre en vez de reemplazarlo. */
    override fun describir(): String = "${super.describir()} y $kilovatios kWh"

    /** A partir de aquí, nadie puede volver a sobrescribir `frenar`. */
    final override fun frenar(): String = "$nombre frena regenerando energía"
}

/** Sobrescribir una propiedad con un valor distinto. */
private class Moto(nombre: String) : Vehiculo(nombre) {
    override val ruedas: Int = 2
}

// -- La trampa del método open en el constructor --------------------------------------

private open class BasePeligrosa {

    /** Guardamos lo que el padre ve durante la construcción. */
    val vistoEnConstruccion: String

    init {
        // ¡Ojo! `calcularEtiqueta()` es open: se ejecutará la versión de la subclase,
        // cuyas propiedades TODAVÍA no están inicializadas.
        vistoEnConstruccion = calcularEtiqueta()
        println("      el constructor del padre vio: '$vistoEnConstruccion'")
    }

    open fun calcularEtiqueta(): String = "base"
}

private class SubclasePeligrosa : BasePeligrosa() {

    /** Se inicializa DESPUÉS de que corra el `init` del padre. */
    val etiqueta: String = "etiqueta de la subclase"

    /**
     * Concatenamos con `+` a propósito: cuando el padre llama a este método durante su
     * construcción, `etiqueta` todavía vale null aunque su tipo diga `String`, y la
     * concatenación lo imprime como "null" en lugar de lanzar NPE.
     */
    override fun calcularEtiqueta(): String = "[" + etiqueta + "]"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `open` de Vehiculo y mira cuántos errores aparecen de golpe.
//  2. Intenta sobrescribir `matricular()` en Coche y lee el error.
//  3. Añade `override fun frenar()` a una subclase de CocheElectrico: el `final` lo impide.
//  4. En SubclasePeligrosa, cambia `etiqueta` por un valor por defecto y observa que el
//     problema desaparece: el orden de inicialización es lo que lo causaba.
