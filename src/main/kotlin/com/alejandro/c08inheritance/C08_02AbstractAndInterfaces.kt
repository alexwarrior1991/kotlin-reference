package com.alejandro.c08inheritance

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  8.2 · Clases abstractas e interfaces
//
//  QUÉ ES
//    `abstract` declara una clase que no se puede instanciar y que puede dejar
//    miembros sin implementar. Una `interface` declara un contrato que, en Kotlin,
//    también puede traer implementaciones y propiedades.
//
//  POR QUÉ IMPORTA
//    Como las interfaces de Kotlin pueden llevar código, la frontera con las clases
//    abstractas se estrecha: la diferencia real es que una interfaz NO puede guardar
//    estado y que se pueden implementar varias.
//
//  ERRORES COMUNES
//    · Poner una propiedad con valor inicial en una interfaz (no se puede: no hay
//      backing field).
//    · Elegir clase abstracta por costumbre cuando una interfaz bastaba.
//    · No saber resolver el conflicto cuando dos interfaces traen el mismo método.
// =====================================================================================

/**
 * Clases abstractas.
 */
fun demoAbstractClasses() {
    section("No se pueden instanciar")

    // val figura = Figura("x")     // ERROR: Cannot create an instance of an abstract class
    bullet("`abstract class` existe para ser heredada, no para usarse directamente.")
    bullet("Los miembros `abstract` no llevan cuerpo y son `open` automáticamente.")

    section("Mezcla de miembros abstractos y concretos")

    val figuras = listOf(
        Circulo(radio = 2.0),
        Rectangulo(ancho = 3.0, alto = 4.0),
    )

    figuras.forEach { figura ->
        // `area()` es abstracta: cada figura la implementa.
        // `describir()` es concreta: la hereda todo el mundo.
        show(figura.nombre, figura.describir())
    }

    section("Pueden guardar estado")

    // Ésta es la diferencia grande con una interfaz.
    val circulo = Circulo(2.0)
    circulo.marcarComoDibujada()
    circulo.marcarComoDibujada()
    show("veces dibujada (estado en la clase abstracta)", circulo.vecesDibujada)

    bullet("Una clase abstracta SÍ puede tener propiedades con backing field.")
    bullet("Una interfaz no: por eso el estado compartido va en la clase abstracta.")
}

/**
 * Interfaces con implementación.
 */
fun demoInterfaces() {
    section("Un contrato, con o sin código")

    val pato = Pato()
    show("pato.nadar()   (implementado en la interfaz)", pato.nadar())
    show("pato.volar()   (implementado en la interfaz)", pato.volar())
    show("pato.hablar()  (implementado en Pato)", pato.hablar())

    bullet("`nadar()` y `volar()` traen cuerpo por defecto desde la interfaz.")
    bullet("`hablar()` es abstracto: cada implementación debe darlo.")

    section("Varias interfaces a la vez")

    // Aquí está la razón principal para preferir interfaces: se pueden combinar.
    val pinguino = Pinguino()
    show("pinguino.nadar()", pinguino.nadar())
    show("pinguino.hablar()", pinguino.hablar())

    // Ojo al detalle: preguntamos sobre una referencia declarada como `Animal`.
    // Si preguntáramos sobre el tipo concreto `Pinguino`, el compilador ya sabría la
    // respuesta y lo RECHAZARÍA con "Check for instance is always 'false'".
    val comoAnimal: Animal = pinguino
    show("¿es Volador?", comoAnimal is Volador)
    show("¿es Nadador?", comoAnimal is Nadador)

    bullet("Una clase hereda de UNA clase pero implementa TANTAS interfaces como quiera.")
    bullet("Kotlin rechaza los `is` que él mismo puede demostrar imposibles.")

    section("Propiedades en interfaces")

    show("pato.patas   (valor por defecto vía getter)", pato.patas)
    show("pinguino.patas", Pinguino().patas)
    show("pato.nombreComun (sobrescrita)", pato.nombreComun)

    bullet("Una propiedad de interfaz NO puede tener valor inicial: no hay backing field.")
    bullet("Sí puede tener un getter por defecto, que es lo que hace `patas`.")
    bullet("La clase que la implementa es quien decide dónde guardar el valor.")
}

/**
 * Conflictos entre interfaces.
 */
fun demoInterfaceConflicts() {
    section("El problema del diamante")

    // Si dos interfaces traen el MISMO método con cuerpo, el compilador no elige por
    // ti: te obliga a sobrescribirlo y a decir explícitamente cuál quieres.
    val dispositivo = Movil()

    show("dispositivo.encender()", dispositivo.encender())
    show("dispositivo.identificar()", dispositivo.identificar())

    bullet("Sin el `override` explícito, el error es:")
    bullet("  'Class Movil inherits multiple interface methods of the name encender'")

    section("super<Interfaz>: elegir una implementación concreta")

    bullet("super<Telefono>.encender()  → la de Telefono")
    bullet("super<Camara>.encender()    → la de Camara")
    bullet("O las dos, o ninguna: tú decides dentro del override.")

    section("Cuándo aparece de verdad")

    bullet("Es raro con métodos de negocio; es habitual con `toString`, `equals`")
    bullet("o métodos utilitarios que varias interfaces intentan aportar.")
}

/**
 * Cómo elegir entre clase abstracta e interfaz.
 */
fun demoAbstractVsInterface() {
    section("La tabla")

    bullet("                        interface      abstract class")
    bullet("¿Varias a la vez?          sí              no")
    bullet("¿Constructor?              no              sí")
    bullet("¿Estado (backing field)?   no              sí")
    bullet("¿Métodos con cuerpo?       sí              sí")
    bullet("¿Visibilidad de miembros?  sólo pública    cualquiera")

    section("La regla práctica")

    bullet("Empieza SIEMPRE por una interfaz. Es más flexible y más fácil de testear.")
    bullet("Pasa a clase abstracta sólo si necesitas guardar estado compartido")
    bullet("o un constructor que valide algo para todas las subclases.")

    section("El patrón habitual: las dos")

    // Una interfaz define el contrato (lo que ve el resto del mundo) y una clase
    // abstracta ofrece una base cómoda para quien quiera implementarla.
    show("ambos son Repositorio", "${RepositorioEnMemoria()} y ${RepositorioConCache()}")
    show("RepositorioConCache reutiliza la base", RepositorioConCache().buscar("k"))

    bullet("interface Repositorio           ← el contrato")
    bullet("abstract class RepositorioBase  ← la comodidad opcional")
    bullet("Así nadie está obligado a heredar para poder implementar el contrato.")
}

// -- Clases abstractas ----------------------------------------------------------------

private abstract class Figura(val nombre: String) {

    /** Estado compartido: esto NO se podría hacer en una interfaz. */
    var vecesDibujada: Int = 0
        private set

    /** Abstracto: sin cuerpo, y `open` de forma implícita. */
    abstract fun area(): Double

    /** Concreto: lo heredan todas las figuras. */
    open fun describir(): String = "$nombre de área %.2f".format(area())

    fun marcarComoDibujada() {
        vecesDibujada++
    }
}

private class Circulo(val radio: Double) : Figura("círculo") {
    override fun area(): Double = Math.PI * radio * radio
}

private class Rectangulo(val ancho: Double, val alto: Double) : Figura("rectángulo") {
    override fun area(): Double = ancho * alto
    override fun describir(): String = "${super.describir()} (${ancho}x$alto)"
}

// -- Interfaces -----------------------------------------------------------------------

private interface Nadador {
    /** Método con implementación por defecto. */
    fun nadar(): String = "nada"
}

private interface Volador {
    fun volar(): String = "vuela"
}

private interface Animal {
    /** Propiedad sin valor: la implementación decide de dónde sale. */
    val nombreComun: String

    /** Propiedad con getter por defecto: tampoco guarda nada. */
    val patas: Int
        get() = 4

    /** Abstracto: obligatorio implementarlo. */
    fun hablar(): String
}

private class Pato : Animal, Nadador, Volador {
    override val nombreComun: String = "pato"
    override val patas: Int = 2          // aquí sí hay backing field: lo pone la clase
    override fun hablar(): String = "cuac"
}

private class Pinguino : Animal, Nadador {
    override val nombreComun: String = "pingüino"
    override val patas: Int = 2
    override fun hablar(): String = "graznido"
    override fun nadar(): String = "nada muy rápido"   // sobrescribe el valor por defecto
}

// -- Conflicto entre interfaces -------------------------------------------------------

private interface Telefono {
    fun encender(): String = "enciende la pantalla"
    fun identificar(): String = "teléfono"
}

private interface Camara {
    fun encender(): String = "enciende el sensor"
    fun identificar(): String = "cámara"
}

private class Movil : Telefono, Camara {

    /** Obligatorio: las dos interfaces aportan `encender()` con cuerpo. */
    override fun encender(): String =
        "${super<Telefono>.encender()} y ${super<Camara>.encender()}"

    /** Aquí elegimos sólo una. */
    override fun identificar(): String = super<Telefono>.identificar()
}

// -- Interfaz + clase abstracta de apoyo ----------------------------------------------

private interface Repositorio {
    fun buscar(clave: String): String
}

/** Base opcional: aporta comodidad, pero no es obligatoria para cumplir el contrato. */
private abstract class RepositorioBase : Repositorio {
    protected val almacen = mutableMapOf<String, String>()
    override fun buscar(clave: String): String = almacen[clave] ?: "(no encontrado)"
}

/** Implementa el contrato directamente, sin heredar de la base. */
private class RepositorioEnMemoria : Repositorio {
    override fun buscar(clave: String): String = "valor directo de $clave"
    override fun toString(): String = "RepositorioEnMemoria"
}

/** Aprovecha la base y le añade algo. */
private class RepositorioConCache : RepositorioBase() {
    init {
        almacen["k"] = "valor cacheado"
    }

    override fun toString(): String = "RepositorioConCache"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `val color: String = "rojo"` a la interfaz Animal y lee el error.
//  2. Quita el `override fun encender()` de Movil y lee el error del diamante.
//  3. Haz que Pinguino implemente también Volador y decide qué devolver en volar().
//  4. Convierte Figura en interfaz y comprueba qué dejas de poder hacer (el estado).
