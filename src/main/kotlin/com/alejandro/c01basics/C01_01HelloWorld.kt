package com.alejandro.c01basics

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  1.1 · Hola mundo, println y comentarios
//
//  QUÉ ES
//    El programa más pequeño que se puede escribir en Kotlin: una función `main` de
//    nivel superior. No hace falta una clase que la envuelva.
//
//  POR QUÉ IMPORTA
//    Marca la primera diferencia grande con Java: en Kotlin las funciones pueden vivir
//    directamente en un fichero, sin clase contenedora. El compilador crea por detrás
//    una clase (`C01_01HelloWorldKt`), pero tú no la escribes ni la ves.
//
//  ERRORES COMUNES
//    · Escribir `public static void main(String[] args)` por inercia.
//    · Poner punto y coma al final de cada línea: es legal, pero nadie lo hace.
//    · Usar `+` para concatenar cuando existen las plantillas de cadena ("$nombre").
// =====================================================================================

/**
 * El ejemplo original que generó IntelliJ al crear este proyecto, conservado tal cual
 * pero ya comentado en condiciones.
 *
 * Aquí caben, en ocho líneas, cuatro cosas que iremos desmenuzando: una función, una
 * variable inmutable, una plantilla de cadena y un bucle sobre un rango.
 */
fun demoHelloWorld() {
    section("El ejemplo con el que nace todo proyecto Kotlin")

    // `val` declara algo que no se va a reasignar. Es la opción por defecto en Kotlin:
    // se usa `val` siempre, y sólo se cambia a `var` cuando hace falta de verdad.
    val name = "Kotlin"

    // Concatenación "a la Java". Funciona, pero no es lo idiomático.
    println("Hello, " + name + "!")

    // La forma idiomática: una plantilla de cadena. `$name` inserta el valor.
    // Se lee mejor y evita crear objetos intermedios.
    println("Hello, $name!")

    // `1..5` es un rango CERRADO: incluye el 5. Volveremos a los rangos en 2.4.
    for (i in 1..5) {
        println("i = $i")
    }
}

/**
 * `println` y `print`: la diferencia es el salto de línea, y se nota más de lo que
 * parece cuando quieres construir una línea por partes.
 */
fun demoPrintln() {
    section("println vs print")

    // println añade un salto de línea al final; print, no.
    print("Esto ")
    print("sale ")
    print("todo ")
    println("en la misma línea.")

    section("println acepta cualquier tipo, no sólo cadenas")

    // No hay que convertir nada a String: println llama a toString() por ti.
    println(42)
    println(3.14)
    println(true)
    println('K')
    println(listOf("a", "b", "c"))

    // ...incluso null, que se imprime literalmente como "null".
    val nada: String? = null
    println(nada)

    section("Saltos de línea y caracteres especiales")

    // \n salto de línea, \t tabulador, \\ barra invertida, \" comilla doble.
    println("Primera línea\nSegunda línea")
    println("Columna1\tColumna2")
    println("Una \"cita\" entre comillas")
    println("Una barra invertida: \\")

    // Un matiz que sorprende: para imprimir un dólar literal hay que escaparlo,
    // porque `$` empieza una plantilla de cadena.
    println("Precio: 100\$")
}

/**
 * Los tres tipos de comentario de Kotlin.
 *
 * Este bloque que estás leyendo es KDoc: empieza con una barra y DOS asteriscos, y
 * documenta la declaración que viene justo debajo. IntelliJ lo muestra al pasar el
 * ratón por encima o con F1.
 *
 * Curiosidad que acabo de sufrir escribiendo este fichero: como los comentarios de
 * bloque de Kotlin SÍ se anidan, escribir la secuencia de apertura literalmente aquí
 * dentro abriría un comentario nuevo y dejaría el fichero sin cerrar. Por eso esta
 * explicación está escrita con palabras y no con símbolos.
 */
fun demoComments() {
    section("Tipos de comentario")

    // Comentario de una línea. El más usado con diferencia.

    /* Comentario de bloque.
       Puede ocupar varias líneas. */

    /* Y en Kotlin los bloques SÍ se pueden anidar:
       /* este comentario está dentro del anterior */
       ...y el compilador lo entiende sin problema. En Java esto es un error. */

    bullet("//          comentario de línea")
    bullet("/* ... */   comentario de bloque (anidable, a diferencia de Java)")
    bullet("/** ... */  KDoc: documenta la declaración siguiente")

    section("Cuándo comentar")

    // La regla práctica: el código dice QUÉ hace; el comentario dice POR QUÉ.
    // Un comentario que repite el código sobra y además envejece mal.

    bullet("Mal:  // sumamos uno a i        (el código ya lo dice)")
    bullet("Bien: // +1 porque el índice del informe empieza en 1, no en 0")
}

/**
 * El punto y coma es opcional. Saber cuándo sí hace falta evita un par de sorpresas.
 */
fun demoSemicolons() {
    section("El punto y coma sobra... casi siempre")

    val a = 1
    val b = 2 // esto es legal, pero el `;` final se omite siempre

    // El único caso real en el que hace falta: varias sentencias en la MISMA línea.
    val x = 10; val y = 20

    show("a + b", a + b)
    show("x + y", x + y)

    section("Dónde sí importa")

    // También hace falta al separar las constantes de un enum de sus miembros,
    // algo que veremos en el capítulo 10:
    //
    //     enum class Color {
    //         ROJO, VERDE, AZUL;          <- este punto y coma es obligatorio
    //         fun esCalido() = this == ROJO
    //     }

    bullet("Entre dos sentencias en la misma línea: obligatorio.")
    bullet("Separando las constantes de un enum de sus métodos: obligatorio.")
    bullet("En cualquier otro sitio: sobra, y el formateador te lo quitará.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `for (i in 1..5)` por `for (i in 1..<5)` y mira qué deja de imprimirse.
//  2. Quita la barra invertida de "Precio: 100\$" y observa el error del compilador.
//  3. Escribe `val z = 1 val w = 2` en una sola línea sin el `;` y lee lo que dice.
