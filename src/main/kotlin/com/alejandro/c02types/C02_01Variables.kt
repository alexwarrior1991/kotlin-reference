package com.alejandro.c02types

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  2.1 · val, var y const val
//
//  QUÉ ES
//    Las tres formas de declarar algo con nombre en Kotlin, y en qué se diferencian.
//
//  POR QUÉ IMPORTA
//    La elección entre `val` y `var` es la decisión de diseño más frecuente que vas a
//    tomar. `val` por defecto no es una manía estética: una variable que no cambia es
//    una variable sobre la que puedes razonar sin seguirle la pista por todo el método.
//
//  ERRORES COMUNES
//    · Creer que `val` hace inmutable al OBJETO. Sólo congela la referencia.
//    · Usar `const val` donde no se puede (sólo admite tipos primitivos y String, y
//      sólo a nivel superior o dentro de un `object`/`companion object`).
//    · Declarar `var` por costumbre y acabar con un método imposible de seguir.
// =====================================================================================

// `const val` a nivel superior: el valor se conoce al COMPILAR y el compilador lo
// sustituye allí donde se use (como una constante de Java, `static final`).
const val IVA_GENERAL = 0.21
const val NOMBRE_APP = "kotlin-reference"

// `val` normal a nivel superior: se calcula al arrancar, no al compilar.
val ARRANQUE_DESCRIPCION = "App $NOMBRE_APP con IVA del ${IVA_GENERAL * 100}%"

/**
 * La diferencia básica: `val` no se puede reasignar, `var` sí.
 */
fun demoValVsVar() {
    section("val: una sola asignación")

    val nombre = "Alejandro"
    // nombre = "Otro"   // ERROR: Val cannot be reassigned
    show("val nombre", nombre)

    section("var: se puede reasignar")

    var contador = 0
    contador = contador + 1
    contador += 1
    show("var contador tras dos incrementos", contador)

    section("Por qué `val` es la opción por defecto")

    bullet("Si no cambia, no tienes que comprobar en qué punto cambió.")
    bullet("Permite razonar en paralelo: lo que no muta no se corrompe entre hilos.")
    bullet("IntelliJ te avisa con un aviso cuando un `var` podría ser `val`.")
    bullet("Regla práctica: escribe `val` siempre; cámbialo a `var` sólo si no compila.")
}

/**
 * El matiz más importante de `val`: congela la **referencia**, no el **contenido**.
 */
fun demoValIsNotImmutable() {
    section("Un `val` que cambia por dentro")

    // La lista es `val`: no puedo apuntarla a otra lista...
    val compras = mutableListOf("pan", "leche")

    // ...pero nada me impide modificar la lista a la que apunta.
    compras.add("huevos")
    compras.remove("pan")

    show("val compras (mutada por dentro)", compras)
    // compras = mutableListOf()   // ERROR: Val cannot be reassigned

    section("Cómo conseguir inmutabilidad de verdad")

    // Para que no se pueda modificar el contenido hay que elegir un tipo que no lo
    // permita. `listOf` devuelve una List de sólo lectura: no tiene `add` ni `remove`.
    val compraFija = listOf("pan", "leche")
    show("listOf(...) no tiene add()", compraFija)

    bullet("val   → la referencia no cambia")
    bullet("List  → el contenido no se puede cambiar A TRAVÉS de esta referencia")
    bullet("Los dos juntos son lo que normalmente quieres. Más en el capítulo 13.")
}

/**
 * `const val`: constantes resueltas en tiempo de compilación.
 */
fun demoConstVal() {
    section("const val vs val")

    show("const val IVA_GENERAL", IVA_GENERAL)
    show("const val NOMBRE_APP", NOMBRE_APP)
    show("val ARRANQUE_DESCRIPCION", ARRANQUE_DESCRIPCION)

    section("Qué puede y qué no puede ser const")

    bullet("Sí: Int, Long, Double, Float, Boolean, Char, String... y nada más.")
    bullet("Sí: a nivel superior, o dentro de `object` / `companion object`.")
    bullet("No: dentro de una función. `const val` no existe como variable local.")
    bullet("No: nada que haya que calcular al arrancar (listOf(...), LocalDate.now()...).")

    section("Por qué molestarse")

    // El compilador SUSTITUYE el valor allí donde se usa (inlining), así que no hay
    // ni acceso a un campo en tiempo de ejecución. Además se puede usar en anotaciones,
    // donde un `val` normal no vale.
    bullet("Se inserta directamente en el código: cero coste en ejecución.")
    bullet("Se puede usar dentro de anotaciones, por ejemplo @Deprecated(MENSAJE).")
}

/**
 * Declarar el tipo explícitamente y sombrear nombres.
 */
fun demoTypesAndShadowing() {
    section("Tipo explícito")

    // Normalmente se omite (capítulo 1.6), pero a veces se escribe a propósito:
    val edad: Int = 34                 // para dejar claro el tipo de un literal ambiguo
    val precio: Double = 10.0          // para que no se cuele un Int por error
    val etiquetas: List<String> = mutableListOf("a", "b")  // expone List, aunque dentro sea mutable

    show("val edad: Int", edad)
    show("val precio: Double", precio)
    show("val etiquetas: List<String>", etiquetas)

    section("Declarar ahora, asignar después")

    // Se puede declarar un `val` sin valor si el compilador puede demostrar que se
    // asigna exactamente una vez antes de usarse.
    val categoria: String
    if (edad >= 18) {
        categoria = "adulto"
    } else {
        categoria = "menor"
    }
    show("val asignado en un if", categoria)

    // Aunque, puestos a hacerlo, esto se lee mejor (el `if` es una expresión):
    val categoriaIdiomatica = if (edad >= 18) "adulto" else "menor"
    show("la misma idea, idiomática", categoriaIdiomatica)

    section("Sombrear (shadowing)")

    val limite = 3
    for (i in 1..2) {
        // Este `limite` TAPA al de fuera dentro del bucle. Compila, pero IntelliJ
        // lo marca con un aviso ("Name shadowed") y con razón: quien lee el bucle
        // puede creer que está usando el de arriba.
        @Suppress("NAME_SHADOWING")
        val limite = i * 10
        show("  dentro del bucle (i=$i), limite vale", limite)
    }
    show("fuera del bucle limite sigue valiendo", limite)

    bullet("Sombrear compila, pero es una fuente clásica de confusión: pon otro nombre.")
}

/**
 * Cuándo `var` es la respuesta correcta, y cuándo es pereza.
 */
fun demoWhenVarIsFine() {
    section("Pereza: acumular a mano lo que ya hace la biblioteca")

    val numeros = listOf(3, 9, 4, 1, 7)

    // Versión con `var`. Funciona, pero hay que leer el bucle entero para saber
    // qué acaba valiendo `suma`.
    var suma = 0
    for (n in numeros) {
        suma += n
    }
    show("con var + bucle", suma)

    // Versión declarativa: el nombre de la función ya dice qué sale. Capítulo 13.
    val sumaIdiomatica = numeros.sum()
    show("con .sum()", sumaIdiomatica)

    val soloPares = numeros.filter { it % 2 == 0 }
    show("con .filter { }", soloPares)

    section("Legítimo: estado que de verdad evoluciona")

    // Aquí `var` es lo natural: representamos algo que cambia con el tiempo.
    var intentosRestantes = 3
    while (intentosRestantes > 0) {
        intentosRestantes--
    }
    show("intentos restantes tras el bucle", intentosRestantes)

    bullet("var para estado real (contadores, reintentos, posición de un cursor).")
    bullet("val + operaciones de colección para transformar datos.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `nombre = \"Otro\"` y lee el error exacto del compilador.
//  2. Cambia `val compraFija = listOf(...)` por `mutableListOf(...)` y prueba a añadir.
//  3. Pon `const val` dentro de una función y comprueba que no está permitido.
//  4. Quita el @Suppress del shadowing y mira el aviso que aparece en el IDE.
