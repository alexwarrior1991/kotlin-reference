package com.alejandro.c07classes

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  7.3 · Visibilidad: public, private, protected e internal
//
//  QUÉ ES
//    Los cuatro modificadores que deciden quién puede ver cada declaración. El valor
//    por defecto es `public`, al contrario que en Java (donde es "de paquete").
//
//  POR QUÉ IMPORTA
//    `internal` no existe en Java y es justo el que más falta hacía: "visible en todo
//    mi módulo, invisible para quien use mi librería". Es la herramienta para tener
//    una API pública pequeña con una implementación grande detrás.
//
//  ERRORES COMUNES
//    · Creer que `private` a nivel superior significa "privado de la clase": a nivel
//      superior significa "privado del FICHERO".
//    · Esperar la visibilidad de paquete de Java. En Kotlin no existe.
//    · Exponer como `public` cosas que sólo usa la implementación.
// =====================================================================================

/**
 * Los cuatro modificadores.
 */
fun demoVisibilityModifiers() {
    section("La tabla")

    bullet("public    (por defecto) → todo el mundo")
    bullet("internal              → cualquier código del MISMO MÓDULO")
    bullet("protected             → la clase y sus subclases (no a nivel superior)")
    bullet("private               → la clase, o el FICHERO si es de nivel superior")

    section("Lo que cambia respecto a Java")

    bullet("Por defecto Kotlin es `public`; Java es 'de paquete'.")
    bullet("Kotlin NO tiene visibilidad de paquete: el paquete no da privilegios.")
    bullet("Java no tiene `internal`; su equivalente aproximado son los módulos de JPMS.")
    bullet("En Kotlin `protected` NO incluye el paquete; en Java sí. Es más estricto.")
}

/**
 * Visibilidad a nivel superior (fuera de clases).
 */
fun demoTopLevelVisibility() {
    section("private a nivel superior = privado del fichero")

    // `secretoDelFichero` está declarado más abajo con `private`. Sólo este fichero
    // lo ve; ni siquiera otro fichero del mismo paquete `c07classes`.
    show("secretoDelFichero (mismo fichero)", secretoDelFichero())

    bullet("Por eso dos ficheros del mismo paquete pueden tener funciones privadas")
    bullet("con el MISMO nombre sin chocar: cada una sólo existe en su fichero.")
    bullet("Es el mecanismo que usa este repositorio para los ayudantes de cada demo.")

    section("internal a nivel superior = visible en todo el módulo")

    show("visibleEnTodoElModulo()", visibleEnTodoElModulo())
    bullet("Este proyecto entero es UN módulo Gradle, así que aquí `internal` se")
    bullet("comporta casi como `public`. La diferencia se notaría al publicar un .jar.")

    section("Qué es un 'módulo' exactamente")

    bullet("Un conjunto de ficheros compilados juntos: un source set de Gradle,")
    bullet("un módulo de Maven, un proyecto de IntelliJ.")
    bullet("Detalle útil: el source set de TEST ve las declaraciones `internal` del")
    bullet("de producción. Por eso se pueden testear cosas que no son API pública.")
}

/**
 * Visibilidad de los miembros de una clase.
 */
fun demoMemberVisibility() {
    section("Desde fuera de la clase")

    val cuenta = CuentaConVisibilidad("ES12 3456")

    show("iban (public)", cuenta.iban)
    show("saldoFormateado() (public)", cuenta.saldoFormateado())
    // cuenta.saldoEnCentimos          // ERROR: Cannot access 'saldoEnCentimos': it is private
    // cuenta.registrarMovimiento(1)   // ERROR: Cannot access ...: it is protected

    bullet("`private` en un miembro → sólo esa clase (¡ni siquiera las subclases!).")
    bullet("`protected` → esa clase y las que hereden de ella.")

    section("Desde una subclase")

    val premium = CuentaPremium("ES99 8888")
    show("la subclase sí ve el miembro protected", premium.ingresarConBonus(100_00))
    bullet("`ingresarConBonus` llama a `registrarMovimiento`, que es protected.")
    bullet("Pero NO puede tocar `saldoEnCentimos`, que es private del padre.")

    section("El caso del constructor privado")

    // Un constructor privado obliga a pasar por la factoría, que puede validar.
    show("Identificador.desde(\"abc-123\")", Identificador.desde("abc-123"))
    show("Identificador.desde(\"\")", Identificador.desde(""))
    // Identificador("cualquiera")     // ERROR: Cannot access '<init>': it is private
    bullet("Es el patrón para garantizar que todo objeto creado es válido.")
}

/**
 * Cómo elegir.
 */
fun demoVisibilityGuidelines() {
    section("La regla")

    bullet("Empieza por lo más restrictivo que funcione y abre sólo si hace falta.")
    bullet("Bajar la visibilidad después es un cambio incompatible; subirla, no.")

    section("En la práctica")

    bullet("private   → estado interno, ayudantes, todo lo que sea 'cómo' y no 'qué'.")
    bullet("internal  → clases de apoyo que usan varios ficheros de tu módulo.")
    bullet("protected → un punto de extensión pensado para las subclases.")
    bullet("public    → sólo lo que quieras mantener estable para siempre.")

    section("Una señal de alarma")

    bullet("Si tienes que poner `public` algo sólo para testearlo, marca `internal`:")
    bullet("los tests del mismo módulo lo verán y tu API pública no crece.")
}

// -- Declaraciones de nivel superior ---------------------------------------------------

/** Sólo visible dentro de este fichero. */
private fun secretoDelFichero(): String = "sólo C07_03Visibility.kt puede llamarme"

/** Visible en todo el módulo, pero no desde fuera si esto fuera una librería. */
internal fun visibleEnTodoElModulo(): String = "cualquier fichero de kotlin-reference me ve"

// -- Las clases que usan las demos ----------------------------------------------------

private open class CuentaConVisibilidad(val iban: String) {

    /** Estado interno: ni siquiera las subclases deberían tocarlo. */
    private var saldoEnCentimos: Long = 0

    /** Punto de extensión pensado para las subclases. */
    protected fun registrarMovimiento(centimos: Long) {
        saldoEnCentimos += centimos
    }

    /** La API pública: lo único que ve el resto del mundo. */
    fun saldoFormateado(): String = "%.2f €".format(saldoEnCentimos / 100.0)
}

private class CuentaPremium(iban: String) : CuentaConVisibilidad(iban) {

    /** Puede usar `registrarMovimiento` porque es `protected`. */
    fun ingresarConBonus(centimos: Long): String {
        registrarMovimiento(centimos)
        registrarMovimiento(centimos / 10)      // 10% de bonus
        // saldoEnCentimos = 0                  // ERROR: es private del padre
        return saldoFormateado()
    }
}

/** Constructor privado + factoría: imposible crear un identificador inválido. */
private class Identificador private constructor(val valor: String) {

    override fun toString(): String = "Identificador($valor)"

    companion object {
        fun desde(texto: String): String {
            val limpio = texto.trim()
            return if (limpio.isEmpty()) "rechazado: vacío" else Identificador(limpio).toString()
        }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `cuenta.saldoEnCentimos` y lee el error exacto.
//  2. Intenta acceder a `secretoDelFichero()` desde C07_02Properties.kt: no lo verá.
//  3. Cambia `protected` por `private` en registrarMovimiento y mira qué se rompe.
//  4. Haz público el constructor de Identificador y piensa qué invariante pierdes.
