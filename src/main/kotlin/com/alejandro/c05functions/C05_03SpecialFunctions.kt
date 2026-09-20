package com.alejandro.c05functions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  5.3 · Funciones locales, infijas, recursivas de cola y vararg
//
//  QUÉ ES
//    Cuatro variantes con sintaxis propia: funciones dentro de funciones, funciones
//    que se llaman sin punto ni paréntesis, recursión que el compilador convierte en
//    bucle, y funciones con un número variable de argumentos.
//
//  POR QUÉ IMPORTA
//    Las locales evitan ensuciar el fichero con ayudantes privados de un solo uso.
//    Las infijas son la base de `to`, `downTo`, `step` y de muchos DSL (capítulo 29).
//    `tailrec` convierte un desbordamiento de pila en un bucle.
//
//  ERRORES COMUNES
//    · Marcar `tailrec` una función cuya recursión NO está en la última posición: el
//      compilador avisa, pero si ignoras el aviso sigues con el mismo problema.
//    · Usar `infix` por capricho: sólo se justifica si se lee como una frase.
//    · Pasar una lista a un `vararg` sin el operador de propagación `*`.
// =====================================================================================

/**
 * Funciones locales: declaradas dentro de otra función.
 */
fun demoLocalFunctions() {
    section("El problema: lógica repetida dentro de una función")

    show("validarUsuarioMal(\"\", \"\")", validarUsuarioMal("", ""))
    show("validarUsuarioMal(\"ana\", \"\")", validarUsuarioMal("ana", ""))

    section("La solución: una función local")

    show("validarUsuario(\"\", \"\")", validarUsuario("", ""))
    show("validarUsuario(\"ana\", \"\")", validarUsuario("ana", ""))
    show("validarUsuario(\"ana\", \"secreto\")", validarUsuario("ana", "secreto"))

    section("Ventaja clave: ven las variables de la función que las contiene")

    // La función local `acumular` usa `total` sin necesidad de recibirlo. Es un
    // closure (capítulo 15).
    show("sumarTodo(listOf(1,2,3), listOf(10,20))", sumarTodo(listOf(1, 2, 3), listOf(10, 20)))

    section("Cuándo usarlas")

    bullet("El ayudante sólo tiene sentido dentro de esta función.")
    bullet("Necesita acceso a las variables locales o a los parámetros.")
    bullet("Si crece o hace falta en otro sitio, sácala a función privada del fichero.")
    bullet("Se pueden anidar, pero más de un nivel ya se lee mal.")
}

/**
 * Funciones infijas: se llaman sin punto ni paréntesis.
 */
fun demoInfixFunctions() {
    section("Las que ya usas sin saberlo")

    show("1 to \"uno\"        (to es infix)", 1 to "uno")
    show("10 downTo 1", (10 downTo 1).first)
    show("1..10 step 2", (1..10 step 2).toList())
    show("true and false", true and false)

    section("Definir una propia")

    show("5 veces \"ab\"", 5 veces "ab")
    show("\"Kotlin\" empiezaPor \"Kot\"", "Kotlin" empiezaPor "Kot")

    // También se puede llamar con la sintaxis normal.
    show("la misma, con punto", 5.veces("ab"))

    section("Los tres requisitos")

    bullet("1. Debe ser método de una clase o función de extensión.")
    bullet("2. Debe tener EXACTAMENTE un parámetro.")
    bullet("3. Ese parámetro no puede ser `vararg` ni tener valor por defecto.")

    section("Cuidado con la precedencia")

    // Las funciones infijas tienen MENOS prioridad que la aritmética, pero MÁS que
    // las comparaciones y que los operadores booleanos.
    show("1 + 2 veces \"x\"   se lee (1+2) veces \"x\"", 1 + 2 veces "x")
    bullet("En caso de duda, pon paréntesis: nadie se acuerda de esta tabla.")

    section("Cuándo NO usar infix")

    bullet("Sólo si la llamada se lee como una frase en inglés o en español.")
    bullet("`usuario tiene permiso` sí; `lista procesar elemento` no.")
    bullet("Si dudas, deja la función normal: siempre se puede leer.")
}

/**
 * `tailrec`: recursión que el compilador convierte en bucle.
 */
fun demoTailrec() {
    section("Recursión normal: la pila tiene límite")

    // Con pocas vueltas no hay problema.
    show("sumaRecursiva(100)", sumaRecursiva(100))

    // Con muchas, la pila se agota. Capturamos el error para que la demo siga.
    val desbordada = try {
        sumaRecursiva(200_000).toString()
    } catch (e: StackOverflowError) {
        "lanzó StackOverflowError"
    }
    show("sumaRecursiva(200_000)", desbordada)

    section("La misma función con tailrec")

    // El compilador la reescribe como un bucle: no crece la pila.
    show("sumaTailrec(100)", sumaTailrec(100))
    show("sumaTailrec(200_000)", sumaTailrec(200_000))
    show("sumaTailrec(1_000_000)", sumaTailrec(1_000_000))

    section("El requisito: la llamada recursiva debe ser lo ÚLTIMO")

    // Esto NO es recursión de cola, porque después de llamarse aún hay que multiplicar:
    //     fun factorial(n: Int): Int = if (n <= 1) 1 else n * factorial(n - 1)
    //                                                    ^^^ queda trabajo pendiente
    // Con `tailrec` el compilador avisa: "A function is marked as tail-recursive but
    // no tail calls are found". El truco es pasar el resultado parcial como parámetro.
    show("factorialTailrec(10)", factorialTailrec(10))

    bullet("Si hay una operación DESPUÉS de la llamada, no es recursión de cola.")
    bullet("La solución: llevar el resultado acumulado en un parámetro (un 'acumulador').")
    bullet("Tampoco vale si la llamada está dentro de un try/catch.")
}

/**
 * `vararg`: número variable de argumentos.
 */
fun demoVararg() {
    section("Llamar con cualquier número de argumentos")

    show("media()", media())
    show("media(10.0)", media(10.0))
    show("media(10.0, 20.0, 30.0)", media(10.0, 20.0, 30.0))

    section("Dentro de la función, un vararg es un array")

    show("describir(\"a\", \"b\", \"c\")", describir("a", "b", "c"))

    section("El operador de propagación: *")

    // Para pasar una colección que ya tienes, hay que convertirla a array y
    // desplegarla con `*`. Sin el `*`, estarías pasando UN argumento que es el array.
    val valores = doubleArrayOf(1.0, 2.0, 3.0)
    show("media(*valores)", media(*valores))

    val lista = listOf(4.0, 5.0, 6.0)
    show("media(*lista.toDoubleArray())", media(*lista.toDoubleArray()))

    bullet("`*` sólo se puede usar al llamar a una función con vararg.")
    bullet("Con una List hay que convertirla antes: toTypedArray() o toDoubleArray().")

    section("Mezclar sueltos y desplegados")

    show("media(0.0, *valores, 100.0)", media(0.0, *valores, 100.0))

    section("Reglas")

    bullet("Sólo puede haber UN parámetro vararg por función.")
    bullet("Si no es el último, los que vayan detrás hay que pasarlos con nombre.")
    bullet("`listOf`, `setOf`, `arrayOf` y `println` son varargs de la biblioteca.")
}

// -- Las funciones que usan las demos -------------------------------------------------

/** Versión con la validación repetida tres veces. */
private fun validarUsuarioMal(nombre: String, clave: String): String {
    if (nombre.isBlank()) return "error: nombre vacío"
    if (nombre.length < 3) return "error: nombre demasiado corto"
    if (clave.isBlank()) return "error: clave vacía"
    if (clave.length < 3) return "error: clave demasiado corta"
    return "válido"
}

/** La misma lógica, con una función local que centraliza la comprobación. */
private fun validarUsuario(nombre: String, clave: String): String {
    // `campo` es el nombre que aparecerá en el mensaje; así el error se explica solo.
    fun validar(valor: String, campo: String): String? = when {
        valor.isBlank() -> "error: $campo vacío"
        valor.length < 3 -> "error: $campo demasiado corto"
        else -> null
    }

    return validar(nombre, "nombre") ?: validar(clave, "clave") ?: "válido"
}

/** La función local `acumular` lee y escribe `total`, que es local a `sumarTodo`. */
private fun sumarTodo(vararg listas: List<Int>): Int {
    var total = 0

    fun acumular(lista: List<Int>) {
        for (n in lista) total += n     // usa `total` sin recibirlo
    }

    for (lista in listas) acumular(lista)
    return total
}

/** Función infija propia: repite un texto. */
private infix fun Int.veces(texto: String): String = texto.repeat(this)

/** Otra infija: se lee como una frase. */
private infix fun String.empiezaPor(prefijo: String): Boolean = this.startsWith(prefijo)

/** Recursión normal: cada llamada ocupa un marco de pila. */
private fun sumaRecursiva(n: Int): Long = if (n <= 0) 0 else n + sumaRecursiva(n - 1)

/** Recursión de cola: la llamada es lo último que ocurre. */
private tailrec fun sumaTailrec(n: Int, acumulado: Long = 0): Long =
    if (n <= 0) acumulado else sumaTailrec(n - 1, acumulado + n)

/** Factorial con acumulador para que la recursión sea de cola. */
private tailrec fun factorialTailrec(n: Int, acumulado: Long = 1): Long =
    if (n <= 1) acumulado else factorialTailrec(n - 1, acumulado * n)

/** vararg de Double: dentro de la función es un DoubleArray. */
private fun media(vararg numeros: Double): Double =
    if (numeros.isEmpty()) 0.0 else numeros.sum() / numeros.size

/** vararg de String: dentro es un Array<out String>. */
private fun describir(vararg elementos: String): String =
    "${elementos.size} elementos: ${elementos.joinToString()}"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `tailrec` de sumaTailrec y comprueba que vuelve a desbordar la pila.
//  2. Marca `sumaRecursiva` con `tailrec` y lee el aviso del compilador.
//  3. Llama a `media(valores)` sin el `*` y lee el error de tipos.
//  4. Escribe una infija `infix fun Int.entre(otro: Int)` y decide si se lee bien.
