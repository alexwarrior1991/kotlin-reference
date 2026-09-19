package com.alejandro.c05functions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  5.2 · Valores por defecto y argumentos nombrados
//
//  QUÉ ES
//    Dar un valor por defecto a un parámetro para no tener que pasarlo, y poder
//    llamar a una función indicando el nombre de cada argumento.
//
//  POR QUÉ IMPORTA
//    Entre los dos eliminan la necesidad de escribir cinco sobrecargas de la misma
//    función, que es lo que se hace en Java. Y en una llamada con varios booleanos,
//    los nombres son la diferencia entre código legible y adivinanza.
//
//  ERRORES COMUNES
//    · Duplicar sobrecargas en lugar de usar valores por defecto.
//    · Olvidar `@JvmOverloads` cuando la función se va a llamar desde Java.
//    · Poner el parámetro con valor por defecto en medio, obligando a nombrar el resto.
// =====================================================================================

/**
 * Valores por defecto.
 */
fun demoDefaultArguments() {
    section("Una sola función, muchas formas de llamarla")

    show("conectar(\"localhost\")", conectar("localhost"))
    show("conectar(\"localhost\", 8080)", conectar("localhost", 8080))
    show("conectar(\"localhost\", 8080, false)", conectar("localhost", 8080, false))
    show("conectar(\"localhost\", timeoutMs = 500)", conectar("localhost", timeoutMs = 500))

    section("En Java harían falta cuatro sobrecargas")

    bullet("public Conexion conectar(String host)")
    bullet("public Conexion conectar(String host, int puerto)")
    bullet("public Conexion conectar(String host, int puerto, boolean seguro)")
    bullet("...y cada una delegando en la siguiente. Aquí es una sola firma.")

    section("El valor por defecto se evalúa en cada llamada")

    // No es una constante: es una expresión que se ejecuta cuando no pasas el valor.
    show("idPorDefecto() #1", generarId())
    show("idPorDefecto() #2", generarId())
    bullet("Cada llamada sin argumento vuelve a evaluar la expresión por defecto.")

    section("Un valor por defecto puede usar los parámetros anteriores")

    show("rango(1)", rango(1))
    show("rango(1, 10)", rango(1, 10))
    show("rango(5)   (el fin depende del inicio)", rango(5))
}

/**
 * Argumentos nombrados.
 */
fun demoNamedArguments() {
    section("El problema que resuelven")

    // ¿Qué significa este `true, false, true`? Imposible saberlo sin abrir la función.
    show("formatear(\"texto\", true, false, true)", formatear("texto", true, false, true))

    // Con nombres, la llamada se explica sola.
    show(
        "con nombres",
        formatear("texto", mayusculas = true, recortar = false, entreComillas = true),
    )

    bullet("Regla práctica: si un argumento es un booleano suelto, nómbralo.")
    bullet("Y si tu función tiene tres booleanos seguidos, quizá le sobra alguno.")

    section("Los nombres permiten cambiar el orden")

    show("orden normal", presentar("Ana", 34))
    show("orden invertido, con nombres", presentar(edad = 34, nombre = "Ana"))

    section("Mezclar posicionales y nombrados")

    // Desde Kotlin 1.4 se pueden poner posicionales DESPUÉS de nombrados, siempre
    // que sigan en su posición natural.
    show("presentar(\"Ana\", edad = 34)", presentar("Ana", edad = 34))
    bullet("Lo que no se puede: nombrar uno y luego saltarse el orden de los siguientes.")

    section("Saltarse parámetros de en medio")

    // Sin nombres sólo puedes omitir los del final. Con nombres, cualquiera.
    show("conectar(\"host\", seguro = false)", conectar("host", seguro = false))
    bullet("Por eso conviene ordenar los parámetros: primero los obligatorios,")
    bullet("luego los opcionales, y los menos usados al final.")
}

/**
 * Valores por defecto frente a sobrecargas.
 */
fun demoDefaultsVsOverloads() {
    section("Cuándo usar valores por defecto")

    bullet("Cuando es la MISMA operación con algún ajuste: conectar con o sin puerto.")
    bullet("Es una sola firma, un solo cuerpo, una sola documentación.")

    section("Cuándo usar sobrecargas de verdad")

    // Aquí los tipos de entrada son distintos; no hay un valor por defecto que valga.
    show("areaDe(3.0)         (círculo)", areaDe(3.0))
    show("areaDe(3.0, 4.0)    (rectángulo)", areaDe(3.0, 4.0))
    show("areaDe(\"cuadrado\", 5.0)", areaDe("cuadrado", 5.0))

    bullet("Cuando cambian los TIPOS de los parámetros, no hay más remedio.")
    bullet("Si dos sobrecargas hacen lo mismo con distinto número de argumentos,")
    bullet("casi siempre es mejor una función con valores por defecto.")

    section("Ojo a la ambigüedad")

    // Si una sobrecarga y un valor por defecto pueden encajar con la misma llamada,
    // gana la sobrecarga SIN valores por defecto. Es una fuente de sorpresas.
    bullet("Evita mezclar sobrecargas y valores por defecto en la misma familia.")
}

/**
 * Interoperabilidad con Java: @JvmOverloads.
 */
fun demoJvmOverloads() {
    section("El problema")

    // Java no tiene valores por defecto. Desde Java, `conectar("host")` NO existe:
    // sólo se ve la firma completa `conectar(String, int, boolean, long)`.
    bullet("Kotlin compila UNA función con todos los parámetros.")
    bullet("Desde Java habría que pasar siempre todos los argumentos.")

    section("La solución")

    // @JvmOverloads genera las sobrecargas intermedias para que Java las vea.
    show("saludoConOverloads(\"Ana\")", saludoConOverloads("Ana"))
    show("saludoConOverloads(\"Ana\", \"Buenos días\")", saludoConOverloads("Ana", "Buenos días"))

    bullet("@JvmOverloads genera saludo(String) y saludo(String, String) para Java.")
    bullet("Sólo hace falta si la función se va a llamar desde Java. Capítulo 27.")
}

// -- Las funciones que usan las demos -------------------------------------------------

/**
 * Parámetros ordenados de más a menos importantes: el obligatorio primero.
 */
private fun conectar(
    host: String,
    puerto: Int = 443,
    seguro: Boolean = true,
    timeoutMs: Long = 3_000,
): String {
    val esquema = if (seguro) "https" else "http"
    return "$esquema://$host:$puerto (timeout ${timeoutMs}ms)"
}

private var contadorDeIds = 0

/** El valor por defecto es una EXPRESIÓN: se evalúa en cada llamada. */
private fun generarId(prefijo: String = "id", numero: Int = ++contadorDeIds): String =
    "$prefijo-$numero"

/** El valor por defecto de `fin` usa el parámetro anterior. */
private fun rango(inicio: Int, fin: Int = inicio + 3): List<Int> = (inicio..fin).toList()

private fun formatear(
    texto: String,
    mayusculas: Boolean = false,
    recortar: Boolean = true,
    entreComillas: Boolean = false,
): String {
    var resultado = texto
    if (recortar) resultado = resultado.trim()
    if (mayusculas) resultado = resultado.uppercase()
    if (entreComillas) resultado = "\"$resultado\""
    return resultado
}

private fun presentar(nombre: String, edad: Int): String = "$nombre tiene $edad años"

// Sobrecargas legítimas: cambian los tipos, no sólo el número de argumentos.
private fun areaDe(radio: Double): Double = Math.PI * radio * radio
private fun areaDe(base: Double, altura: Double): Double = base * altura
private fun areaDe(forma: String, lado: Double): String = "$forma de lado $lado → ${lado * lado}"

/**
 * @JvmOverloads genera las sobrecargas intermedias para quien llame desde Java.
 *
 * No es `private` a propósito: la anotación no tiene sentido (ni efecto) sobre algo
 * que desde Java no se puede ver.
 */
@JvmOverloads
fun saludoConOverloads(nombre: String, saludo: String = "Hola"): String = "$saludo, $nombre"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Llama a `conectar` pasando sólo el timeout y comprueba que necesitas nombrarlo.
//  2. Intenta `presentar(nombre = "Ana", 34)` y lee por qué no compila.
//  3. Cambia `numero: Int = ++contadorDeIds` por `= 0` y observa cómo dejan de variar.
//  4. Quita `@JvmOverloads` y piensa qué firma vería un programa Java.
