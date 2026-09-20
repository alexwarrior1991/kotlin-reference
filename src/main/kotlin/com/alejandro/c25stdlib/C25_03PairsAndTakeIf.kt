package com.alejandro.c25stdlib

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  25.3 · Pair, Triple, takeIf, takeUnless y utilidades sueltas
//
//  QUÉ ES
//    Un repaso de las funciones pequeñas de la biblioteca estándar que aparecen
//    constantemente y que, bien usadas, quitan bastantes líneas.
//
//  POR QUÉ IMPORTA
//    `takeIf` es la pieza que faltaba para escribir "dame este valor sólo si cumple
//    algo, y si no, null". Combinada con `?:` resuelve en una línea lo que con un
//    `if` ocupa cuatro.
//
//  ERRORES COMUNES
//    · Usar `Triple` en una API pública: nadie sabe qué es cada componente.
//    · Confundir `takeIf` (devuelve el valor o null) con `filter` (devuelve colección).
//    · Encadenar `takeIf` sobre valores que no son nulables y complicar la lectura.
// =====================================================================================

private data class Usuario(val nombre: String, val edad: Int, val activo: Boolean)

private val usuarios = listOf(
    Usuario("Ana", 34, true),
    Usuario("Luis", 17, true),
    Usuario("Marta", 41, false),
)

/**
 * `Pair` y `Triple`.
 */
fun demoPairAndTriple() {
    section("Crear un Pair")

    val conTo = "clave" to 42
    val conConstructor = Pair("clave", 42)

    show("\"clave\" to 42", conTo)
    show("Pair(\"clave\", 42)", conConstructor)
    show("son iguales", conTo == conConstructor)

    bullet("`to` es una función infija de la stdlib, no sintaxis especial.")

    section("Acceder a sus partes")

    show(".first", conTo.first)
    show(".second", conTo.second)

    val (clave, valor) = conTo
    show("desestructurado", "$clave=$valor")

    show(".toList()", conTo.toList())

    section("Triple")

    val triple = Triple("Ana", 34, true)
    show("Triple(...)", triple)
    show(".third", triple.third)

    section("Dónde aparecen de forma natural")

    // Cuando los dos elementos NO tienen nombre propio en el dominio.
    show("construir un Map", mapOf("a" to 1, "b" to 2))
    show("zip devuelve pares", listOf(1, 2).zip(listOf("a", "b")))
    show("partition devuelve un par", listOf(1, 2, 3, 4).partition { it % 2 == 0 })
    show("withIndex se desestructura como par", listOf("x", "y").withIndex().map { (i, v) -> "$i$v" })

    section("Cuándo NO usarlos")

    bullet("`Triple<String, Int, Boolean>` como tipo de retorno público: ilegible.")
    bullet("Compara: `fun analizar(): Triple<String, Int, Boolean>`")
    bullet("con      `fun analizar(): Analisis` y `data class Analisis(...)`.")
    bullet("La segunda se documenta sola y permite añadir un campo sin romper nada.")

    show("con Triple", analizarConTriple("hola mundo"))
    show("con data class", analizarConDataClass("hola mundo"))

    bullet("Regla: Pair para pares evidentes (clave/valor, mín/máx) y para uso")
    bullet("interno. Todo lo demás, data class con nombres.")
}

/**
 * `takeIf` y `takeUnless`.
 */
fun demoTakeIf() {
    section("Qué hacen")

    bullet("`x.takeIf { predicado }`     → x si cumple, null si no")
    bullet("`x.takeUnless { predicado }` → x si NO cumple, null si sí")

    show("42.takeIf { it > 0 }", 42.takeIf { it > 0 })
    show("(-1).takeIf { it > 0 }", (-1).takeIf { it > 0 })
    show("42.takeUnless { it > 0 }", 42.takeUnless { it > 0 })

    section("Su compañero natural: el Elvis")

    val entrada = "   "
    show("texto en blanco", entrada.trim().takeIf { it.isNotEmpty() } ?: "(vacío)")
    show("texto con contenido", "  hola  ".trim().takeIf { it.isNotEmpty() } ?: "(vacío)")

    bullet("`valor.takeIf { }? : porDefecto` es el patrón completo.")

    section("Frente al if equivalente")

    val numero = 42

    // Con `if` hay que nombrar el valor o repetir la expresión.
    val conIf = if (numero > 0) numero else null

    // Con `takeIf` la expresión aparece una sola vez.
    val conTakeIf = numero.takeIf { it > 0 }

    show("con if", conIf)
    show("con takeIf", conTakeIf)

    bullet("La ventaja se nota cuando el valor viene de una EXPRESIÓN larga:")
    bullet("  buscarUsuario(id)?.takeIf { it.activo } ?: usuarioInvitado")
    bullet("Con `if` tendrías que meter la búsqueda en una variable antes.")

    section("Un caso real: validar y transformar en una cadena")

    listOf("ana@ejemplo.com", "no-es-un-email", "").forEach { entrada ->
        val dominio = entrada
            .takeIf { it.isNotBlank() }
            ?.takeIf { "@" in it }
            ?.substringAfter('@')
            ?: "(inválido)"
        show("'$entrada'", dominio)
    }

    section("Cuándo NO usarlo")

    bullet("Si no hay un `?:` o un `?.` detrás, `takeIf` sólo añade un nulo que")
    bullet("luego hay que tratar. En ese caso, un `if` normal es más directo.")
    bullet("Y dentro de un `filter` no hace falta: ya filtra él.")
}

/**
 * Utilidades sueltas que conviene conocer.
 */
fun demoMiscUtilities() {
    section("Comprobaciones de cadena")

    show("\"\".isEmpty()", "".isEmpty())
    show("\"  \".isBlank()", "  ".isBlank())
    val nulo: String? = null
    show("null.isNullOrEmpty()", nulo.isNullOrEmpty())
    show("null.orEmpty()", "'" + nulo.orEmpty() + "'")
    show("\"\".ifEmpty { \"defecto\" }", "".ifEmpty { "defecto" })
    show("\"  \".ifBlank { \"defecto\" }", "  ".ifBlank { "defecto" })

    section("Lo mismo para colecciones")

    val listaNula: List<Int>? = null
    show("listaNula.orEmpty()", listaNula.orEmpty())
    show("emptyList<Int>().ifEmpty { listOf(0) }", emptyList<Int>().ifEmpty { listOf(0) })

    section("Conversiones seguras")

    show("\"42\".toIntOrNull()", "42".toIntOrNull())
    show("\"abc\".toIntOrNull()", "abc".toIntOrNull())
    show("\"3.5\".toDoubleOrNull()", "3.5".toDoubleOrNull())
    show("\"true\".toBooleanStrictOrNull()", "true".toBooleanStrictOrNull())
    show("\"TRUE\".toBooleanStrictOrNull()", "TRUE".toBooleanStrictOrNull())

    bullet("Todas tienen versión sin `OrNull` que LANZA. Prefiere la segura.")

    section("Acceso seguro a colecciones")

    val lista = listOf("a", "b", "c")
    show("lista.getOrNull(1)", lista.getOrNull(1))
    show("lista.getOrNull(99)", lista.getOrNull(99))
    show("lista.getOrElse(99) { \"?\" }", lista.getOrElse(99) { "?" })

    val mapa = mapOf("x" to 1)
    show("mapa.getOrDefault(\"y\", 0)", mapa.getOrDefault("y", 0))
    show("mapa.getOrElse(\"y\") { 0 }", mapa.getOrElse("y") { 0 })

    section("Filtrar usuarios con todo junto")

    val mayoresActivos = usuarios
        .filter { it.edad >= 18 }
        .filter { it.activo }
        .map { it.nombre }
    show("mayores y activos", mayoresActivos)

    show(
        "el primero que cumpla, o un invitado",
        usuarios.firstOrNull { it.activo && it.edad >= 18 }?.nombre ?: "invitado",
    )
}

// -- Lo que usan las demos ---------------------------------------------------------------------

private fun analizarConTriple(texto: String): Triple<Int, Int, Boolean> =
    Triple(texto.length, texto.split(" ").size, texto.isBlank())

private data class Analisis(val caracteres: Int, val palabras: Int, val vacio: Boolean)

private fun analizarConDataClass(texto: String): Analisis =
    Analisis(
        caracteres = texto.length,
        palabras = texto.split(" ").size,
        vacio = texto.isBlank(),
    )

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Reescribe la cadena de `takeIf` de la demo con `if` anidados y compara.
//  2. Cambia `analizarConTriple` por la versión con data class en tu propio código.
//  3. Busca en tu proyecto un `if (x != null && x.cumple())` y conviértelo en
//     `x?.takeIf { it.cumple() }`.
//  4. Comprueba qué devuelve `"".ifEmpty { null }` y qué tipo tiene.
