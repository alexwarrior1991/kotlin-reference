package com.alejandro.c11sealed

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  11.1 · sealed class y sealed interface
//
//  QUÉ ES
//    Una jerarquía CERRADA: el compilador conoce todas las subclases posibles porque
//    sólo pueden declararse en el mismo módulo y paquete. Es un enum en el que cada
//    caso puede llevar datos distintos.
//
//  POR QUÉ IMPORTA
//    Un enum dice "hay cuatro estados". Una sealed dice "hay cuatro estados, y cada
//    uno lleva la información que le corresponde". Con `when` exhaustivo, el
//    compilador garantiza que ningún caso se queda sin tratar.
//
//  ERRORES COMUNES
//    · Poner `else` en el `when` y perder justo lo que hacía valiosa la sealed.
//    · Usar sealed cuando todos los casos son iguales y sin datos (eso es un enum).
//    · Olvidar que las subclases deben estar en el mismo módulo y paquete.
// =====================================================================================

/**
 * La idea básica.
 */
fun demoSealedBasics() {
    section("Declaración")

    bullet("sealed interface Figura")
    bullet("data class Circulo(val radio: Double) : Figura")
    bullet("data class Rectangulo(val ancho: Double, val alto: Double) : Figura")
    bullet("data object Punto : Figura")

    section("Cada caso lleva SUS datos")

    val figuras: List<Figura> = listOf(
        Circulo(2.0),
        Rectangulo(3.0, 4.0),
        Triangulo(3.0, 4.0, 5.0),
        Punto,
    )

    figuras.forEach { figura ->
        show(figura.toString(), "área=%.2f".format(area(figura)))
    }

    bullet("Un enum no podría hacer esto: sus constantes comparten la misma forma.")

    section("El when exhaustivo, sin else")

    show("descripcion(Circulo(1.0))", descripcion(Circulo(1.0)))
    show("descripcion(Punto)", descripcion(Punto))

    bullet("`area` y `descripcion` no tienen `else`: el compilador comprueba que")
    bullet("los cuatro casos están cubiertos. Si añades uno, dejan de compilar.")

    section("Smart cast dentro de cada rama")

    // Dentro de `is Circulo ->`, la variable ya ES un Circulo: se accede a `radio`
    // sin ningún cast.
    bullet("En la rama `is Circulo`, el compilador ya sabe el tipo: `it.radio` directo.")
    bullet("Es el capítulo 20 aplicado: `is` + smart cast.")
}

/**
 * `sealed class` frente a `sealed interface`.
 */
fun demoSealedClassVsInterface() {
    section("sealed class: puede tener estado compartido")

    val eventos: List<EventoConEstado> = listOf(
        EventoConEstado.Click(10, 20),
        EventoConEstado.Scroll(100),
    )
    eventos.forEach { show(it::class.simpleName ?: "?", "instante=${it.instante} ${it}") }

    bullet("`instante` está en la sealed class: lo comparten todos los eventos.")
    bullet("Una sealed CLASS puede tener constructor y propiedades con estado.")

    section("sealed interface: permite jerarquías que se cruzan")

    // Una clase sólo puede heredar de una sealed class, pero puede implementar
    // varias sealed interfaces. Eso permite clasificar por dos criterios a la vez.
    val respuestas: List<Respuesta> = listOf(
        Ok("datos"),
        NoEncontrado,
        SinPermiso,
        ErrorDeRed(java.io.IOException("timeout")),
    )

    respuestas.forEach { respuesta ->
        val esError = respuesta is ErrorHttp
        show(respuesta::class.simpleName ?: "?", "¿es un error HTTP? $esError")
    }

    bullet("NoEncontrado y SinPermiso son Respuesta Y ErrorHttp a la vez.")
    bullet("ErrorDeRed es Respuesta pero NO ErrorHttp: no llegó a haber respuesta.")

    section("Cuál elegir")

    bullet("sealed interface → por defecto. Más flexible, permite varias jerarquías.")
    bullet("sealed class → si todos los casos comparten estado (un id, un timestamp).")
}

/**
 * Las reglas del "sellado".
 */
fun demoSealedRules() {
    section("Dónde pueden declararse las subclases")

    bullet("Desde Kotlin 1.5: en cualquier fichero del MISMO paquete y módulo.")
    bullet("Antes de 1.5: sólo dentro del mismo fichero. Por eso se ven jerarquías")
    bullet("antiguas con todo anidado dentro de la sealed.")

    section("Las subclases pueden ser de cualquier forma")

    bullet("data class   → el caso con datos (lo más habitual)")
    bullet("data object  → el caso sin datos (un singleton)")
    bullet("class        → si necesitas identidad en vez de igualdad por valor")
    bullet("sealed class → para anidar una sub-jerarquía (ver 11.5)")

    section("La sealed en sí no se puede instanciar")

    bullet("`sealed class X` es abstracta de forma implícita.")
    bullet("Su constructor es `protected` por defecto: sólo las subclases lo usan.")

    section("Dos formas de organizar el código")

    // 1) Anidadas: el nombre lleva el contexto (EventoConEstado.Click).
    show("anidada", EventoConEstado.Click(1, 2).toString())
    // 2) Al mismo nivel: nombres más cortos (Ok, NoEncontrado).
    show("al mismo nivel", Ok("x").toString())

    bullet("Anidadas: mejor si los nombres son genéricos (Exito, Error, Cargando).")
    bullet("Al mismo nivel: mejor si los nombres ya son específicos por sí solos.")
}

// -- Jerarquía de figuras --------------------------------------------------------------

private sealed interface Figura

private data class Circulo(val radio: Double) : Figura
private data class Rectangulo(val ancho: Double, val alto: Double) : Figura
private data class Triangulo(val a: Double, val b: Double, val c: Double) : Figura
private data object Punto : Figura

/** `when` exhaustivo: sin `else` y sin posibilidad de olvidar un caso. */
private fun area(figura: Figura): Double = when (figura) {
    is Circulo -> Math.PI * figura.radio * figura.radio
    is Rectangulo -> figura.ancho * figura.alto
    is Triangulo -> {
        // Fórmula de Herón.
        val s = (figura.a + figura.b + figura.c) / 2
        Math.sqrt(s * (s - figura.a) * (s - figura.b) * (s - figura.c))
    }
    Punto -> 0.0            // para un `object` se compara con ==, no con `is`
}

private fun descripcion(figura: Figura): String = when (figura) {
    is Circulo -> "círculo de radio ${figura.radio}"
    is Rectangulo -> "rectángulo ${figura.ancho}x${figura.alto}"
    is Triangulo -> "triángulo de lados ${figura.a}, ${figura.b}, ${figura.c}"
    Punto -> "un punto, sin dimensiones"
}

// -- sealed class con estado compartido -------------------------------------------------

/** Una sealed CLASS puede tener constructor y propiedades comunes. */
private sealed class EventoConEstado(val instante: Long) {
    class Click(val x: Int, val y: Int) : EventoConEstado(instante = 1_000) {
        override fun toString(): String = "Click($x, $y)"
    }

    class Scroll(val desplazamiento: Int) : EventoConEstado(instante = 2_000) {
        override fun toString(): String = "Scroll($desplazamiento)"
    }
}

// -- sealed interfaces que se cruzan ----------------------------------------------------

private sealed interface Respuesta

/** Segunda jerarquía: sólo algunos casos de Respuesta son además ErrorHttp. */
private sealed interface ErrorHttp {
    val codigo: Int
}

private data class Ok(val cuerpo: String) : Respuesta

private data object NoEncontrado : Respuesta, ErrorHttp {
    override val codigo: Int = 404
}

private data object SinPermiso : Respuesta, ErrorHttp {
    override val codigo: Int = 403
}

/** Es una Respuesta, pero no un error HTTP: la petición no llegó a completarse. */
private data class ErrorDeRed(val causa: Exception) : Respuesta

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `data class Elipse(val a: Double, val b: Double) : Figura` y mira qué
//     funciones dejan de compilar: ésa es la ayuda que te da una sealed.
//  2. Pon un `else -> 0.0` en `area` y repite el ejercicio anterior. Ya no avisa.
//  3. Intenta declarar una subclase de Figura en otro paquete y lee el error.
//  4. Convierte Figura en `sealed class` con una propiedad `nombre` compartida.
