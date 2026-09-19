package com.alejandro.c04controlflow

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  4.2 · when: el switch que sí sirve
//
//  QUÉ ES
//    La construcción de selección múltiple de Kotlin. Sustituye al `switch` de Java y
//    también a las cadenas largas de `if / else if`.
//
//  POR QUÉ IMPORTA
//    Es más potente que un `switch` en todo: acepta cualquier tipo, condiciones
//    arbitrarias, rangos, comprobaciones de tipo con smart cast, y devuelve un valor.
//    Con `sealed` y `enum` el compilador además comprueba que no te dejas ningún caso
//    (capítulos 10 y 11), que es donde `when` pasa de cómodo a imprescindible.
//
//  ERRORES COMUNES
//    · Poner `break` al final de cada rama. No existe ni hace falta: no hay fallthrough.
//    · Olvidar el `else` cuando se usa como expresión.
//    · Usar `when` con sujeto cuando cada rama compara cosas distintas (ahí va sin sujeto).
// =====================================================================================

/**
 * `when` con sujeto: el caso más parecido a un switch.
 */
fun demoWhenWithSubject() {
    section("Valores sueltos")

    listOf(1, 2, 5).forEach { dia ->
        val nombre = when (dia) {
            1 -> "lunes"
            2 -> "martes"
            5 -> "viernes"
            else -> "otro día"
        }
        show("día $dia", nombre)
    }

    bullet("Sin `break`: sólo se ejecuta la primera rama que coincide. No hay fallthrough.")

    section("Varios valores en una misma rama")

    listOf(1, 6, 7).forEach { dia ->
        val tipo = when (dia) {
            6, 7 -> "fin de semana"        // la coma es un OR
            1, 2, 3, 4, 5 -> "laborable"
            else -> "número de día inválido"
        }
        show("día $dia", tipo)
    }

    section("Ramas con bloque")

    val codigo = 404
    val mensaje = when (codigo) {
        200 -> "todo bien"
        404 -> {
            // Un bloque puede hacer varias cosas; vale su última expresión.
            val recurso = "/usuarios/42"
            "no encontrado: $recurso"
        }
        else -> "código $codigo"
    }
    show("código $codigo", mensaje)
}

/**
 * `when` con rangos y con colecciones, usando `in`.
 */
fun demoWhenWithRanges() {
    section("Rangos")

    listOf(-5, 0, 7, 45, 120).forEach { n ->
        val descripcion = when (n) {
            in Int.MIN_VALUE..-1 -> "negativo"
            0 -> "cero"
            in 1..9 -> "un dígito"
            in 10..99 -> "dos dígitos"
            else -> "grande"
        }
        show("n = $n", descripcion)
    }

    bullet("Compara `in 10..99` con `n >= 10 && n <= 99`: la primera se lee de un vistazo.")

    section("Pertenencia a una colección")

    val vocales = setOf('a', 'e', 'i', 'o', 'u')
    listOf('a', 'k', '7').forEach { c ->
        val tipo = when (c) {
            in vocales -> "vocal"
            in 'a'..'z' -> "consonante"
            in '0'..'9' -> "dígito"
            else -> "otro"
        }
        show("carácter '$c'", tipo)
    }

    section("Negación")

    listOf(5, 50).forEach { n ->
        val descripcion = when (n) {
            !in 1..9 -> "no es un dígito"
            else -> "es un dígito"
        }
        show("n = $n", descripcion)
    }
}

/**
 * `when` con comprobación de tipos: `is`.
 */
fun demoWhenWithTypes() {
    section("is + smart cast")

    val cosas: List<Any> = listOf(42, "Kotlin", 3.14, listOf(1, 2, 3), true, 'x')

    cosas.forEach { cosa ->
        // Dentro de cada rama, `cosa` ya está convertida al tipo comprobado:
        // se puede llamar a `.length` o a `.size` sin ningún cast.
        val descripcion = when (cosa) {
            is Int -> "Int; su doble es ${cosa * 2}"
            is String -> "String de ${cosa.length} caracteres"
            is Double -> "Double redondeado a ${Math.round(cosa)}"
            is List<*> -> "Lista de ${cosa.size} elementos"
            is Boolean -> "Boolean negado: ${!cosa}"
            else -> "algo de tipo ${cosa::class.simpleName}"
        }
        show("$cosa", descripcion)
    }

    bullet("El smart cast es automático: nada de `(cosa as String).length`.")
    bullet("Esto se estudia a fondo en el capítulo 20.")
}

/**
 * `when` sin sujeto: sustituye a las cadenas de `if / else if`.
 */
fun demoWhenWithoutSubject() {
    section("Cada rama es una condición independiente")

    listOf(95, 72, 45).forEach { nota ->
        // Sin sujeto, cada rama es un Boolean cualquiera. Aquí podrían mezclarse
        // comparaciones sobre variables distintas, llamadas a funciones, lo que sea.
        val calificacion = when {
            nota >= 90 -> "sobresaliente"
            nota >= 70 -> "notable"
            nota >= 50 -> "aprobado"
            else -> "suspenso"
        }
        show("nota $nota", calificacion)
    }

    bullet("Compara con la cadena de if/else if de la demo 4.2: misma lógica, mejor forma.")

    section("Mezclando condiciones sobre cosas distintas")

    val usuario = "ana"
    val intentos = 3
    val bloqueado = false

    val estado = when {
        bloqueado -> "cuenta bloqueada"
        intentos >= 3 -> "demasiados intentos"
        usuario.isBlank() -> "falta el usuario"
        else -> "acceso permitido"
    }
    show("estado", estado)

    bullet("Esto NO se puede expresar con `when (sujeto)`: cada rama mira algo distinto.")
    bullet("Regla: ¿comparas siempre la misma variable? con sujeto. ¿No? sin sujeto.")

    section("El orden importa")

    // Se evalúan de arriba abajo y gana la primera que se cumple. Si pusiéramos
    // `nota >= 50` antes que `nota >= 90`, un 95 saldría "aprobado".
    bullet("Ordena de la condición más específica a la más general.")
}

/**
 * Capturar el sujeto en el propio `when`.
 */
fun demoWhenWithSubjectCapture() {
    section("when (val x = ...)")

    // Así el resultado se calcula UNA vez y sólo existe dentro del when.
    val respuesta = when (val longitud = calcularAlgoCostoso()) {
        0 -> "vacío"
        in 1..10 -> "corto ($longitud)"
        else -> "largo ($longitud)"
    }
    show("when (val longitud = ...)", respuesta)

    bullet("Sin la captura tendrías que declarar la variable fuera y ensuciar el ámbito.")
    bullet("O peor: llamar dos veces a la función. Aquí se llama una sola.")

    section("Sujeto de cualquier tipo")

    val par = 3 to "tres"
    val descripcion = when (par) {
        1 to "uno" -> "el primero"
        3 to "tres" -> "el tercero"
        else -> "otro"
    }
    show("when sobre un Pair", descripcion)
    bullet("La comparación de cada rama es `==`, así que funciona con cualquier tipo.")
}

/**
 * Exhaustividad: cuándo el compilador exige cubrir todos los casos.
 */
fun demoWhenExhaustiveness() {
    section("Como EXPRESIÓN, el else es obligatorio...")

    val n = 5
    val texto = when (n) {
        1 -> "uno"
        2 -> "dos"
        else -> "otro"      // sin esto no compila: faltaría valor para el resto de Ints
    }
    show("when como expresión", texto)

    section("...salvo que el compilador pueda demostrar que están todos")

    // Con Boolean sólo hay dos valores posibles, así que no hace falta `else`.
    val activo = true
    val etiqueta = when (activo) {
        true -> "activo"
        false -> "inactivo"
    }
    show("when sobre Boolean, sin else", etiqueta)

    // Lo mismo ocurre con `enum` (capítulo 10) y con `sealed` (capítulo 11), y ahí
    // es donde de verdad merece la pena: si mañana añades un caso nuevo al enum, el
    // compilador te señala TODOS los `when` que hay que actualizar.
    bullet("enum y sealed son los casos que hacen valiosa la exhaustividad.")
    bullet("Por eso, con ellos, es mejor NO poner `else`: te quedarías sin ese aviso.")

    section("Como SENTENCIA")

    // Si no usas el valor, `else` no hace falta para tipos abiertos como Int.
    when (n) {
        1 -> show("sentencia", "era uno")
        5 -> show("sentencia", "era cinco")
    }

    bullet("Ojo: desde Kotlin 1.7, un `when` SENTENCIA sobre enum o sealed también")
    bullet("debe ser exhaustivo. Es un error de compilación dejarse un caso.")
}

/** Simula una operación que no queremos repetir. */
private fun calcularAlgoCostoso(): Int = "Kotlin es expresivo".length

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Borra el `else` del primer `when` usado como expresión y lee el error.
//  2. Cambia el orden de las ramas de las notas (pon `nota >= 50` la primera) y observa
//     cómo un 95 pasa a ser "aprobado".
//  3. Añade `is Char ->` a demoWhenWithTypes y comprueba que ya no cae en el `else`.
//  4. Quita una rama del `when` sobre Boolean y mira cómo deja de compilar.
