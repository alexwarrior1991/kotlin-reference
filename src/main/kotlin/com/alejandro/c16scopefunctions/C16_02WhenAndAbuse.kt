package com.alejandro.c16scopefunctions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  16.2 · Encadenar, y cuándo NO usar una scope function
//
//  QUÉ ES
//    La otra mitad del capítulo: los idiomas habituales, y sobre todo los casos en los
//    que una scope function EMPEORA el código.
//
//  POR QUÉ IMPORTA
//    Son tan cómodas que se acaban usando por reflejo. Pero `?.let { }` sobre un valor
//    que no es nulable, o tres scope functions anidadas, se leen peor que el código
//    normal que sustituyen.
//
//  ERRORES COMUNES
//    · `?.let { }` donde bastaba un `if (x != null)` con smart cast.
//    · Usar una scope function sólo para "que quede moderno".
//    · Anidar `let` dentro de `apply` dentro de `run` hasta que nadie sabe qué es `it`.
// =====================================================================================

private data class Usuario(val nombre: String, val email: String?, val edad: Int)

private val usuarios = listOf(
    Usuario("Ana", "ana@ejemplo.com", 34),
    Usuario("Luis", null, 28),
    Usuario("Marta", "marta@ejemplo.com", 41),
)

/**
 * Los idiomas que sí merecen la pena.
 */
fun demoGoodIdioms() {
    section("1. ?.let para 'sólo si existe'")

    usuarios.forEach { usuario ->
        val dominio = usuario.email?.let { it.substringAfter('@') } ?: "(sin email)"
        show(usuario.nombre, dominio)
    }

    section("2. apply para construir")

    val builder = StringBuilder().apply {
        append("Usuarios: ")
        append(usuarios.size)
    }
    show("StringBuilder().apply { }", builder.toString())

    section("3. also para trazas en medio de una cadena")

    val pasos = mutableListOf<String>()
    val resultado = usuarios
        .also { pasos.add("entrada: ${it.size} usuarios") }
        .filter { it.email != null }
        .also { pasos.add("con email: ${it.size}") }
        .map { it.nombre }
        .also { pasos.add("nombres: $it") }

    show("resultado", resultado)
    pasos.forEach { bullet(it) }

    bullet("Quitar las tres líneas de traza no cambia nada: la cadena sigue igual.")

    section("4. run para agrupar un cálculo con nombre")

    val informe = run {
        val conEmail = usuarios.count { it.email != null }
        val edadMedia = usuarios.map { it.edad }.average()
        "$conEmail con email, edad media ${"%.1f".format(edadMedia)}"
    }
    show("run { } agrupando variables temporales", informe)
    bullet("Las variables `conEmail` y `edadMedia` no ensucian el ámbito de fuera.")

    section("5. takeIf / takeUnless para filtrar un valor suelto")

    // No son scope functions estrictamente, pero se usan con ellas constantemente.
    val entrada = "  texto  "
    show("takeIf { }", entrada.trim().takeIf { it.isNotEmpty() } ?: "(vacío)")
    show("takeIf que no pasa", "   ".trim().takeIf { it.isNotEmpty() } ?: "(vacío)")
    show("takeUnless { }", entrada.trim().takeUnless { it.length > 100 })

    bullet("`takeIf` devuelve el valor o null: encaja perfectamente antes de un `?:`.")
}

/**
 * Cuándo NO usarlas.
 */
fun demoWhenNotToUse() {
    section("1. ?.let sobre algo que NO es nulable")

    val nombre = "Ana"      // no nulable

    // Innecesario: no hay nulo del que protegerse.
    show("con let (sobra)", nombre.let { it.uppercase() })
    show("directo (mejor)", nombre.uppercase())

    bullet("Si el valor no puede ser nulo, `let` sólo añade ruido.")

    section("2. ?.let cuando un `if` se lee mejor")

    val email: String? = "ana@ejemplo.com"

    // Con `let`, si necesitas un `else`, hay que poner el `?:` y ya no se lee.
    val conLet = email?.let { "enviando a $it" } ?: "sin email"

    // Con `if`, el smart cast hace el trabajo y el `else` es natural.
    val conIf = if (email != null) "enviando a $email" else "sin email"

    show("con ?.let ... ?:", conLet)
    show("con if / else", conIf)

    bullet("Para una propiedad local, `if` gana casi siempre: hay smart cast.")
    bullet("`?.let` gana cuando el valor viene de una EXPRESIÓN que no quieres repetir.")

    section("3. apply para hacer cosas que no configuran el objeto")

    // `apply` anuncia "estoy configurando este objeto". Si dentro haces otra cosa,
    // el lector se confunde.
    bullet("MAL:  lista.apply { println(\"tamaño: \$size\") }   ← no configura nada")
    bullet("BIEN: lista.also { println(\"tamaño: \${it.size}\") }")

    section("4. Encadenar hasta perder el hilo")

    bullet("MAL:")
    bullet("  usuario.let { u -> u.email?.let { e -> e.split(\"@\").let { p -> p.first() } } }")
    bullet("BIEN:")
    bullet("  usuario.email?.substringBefore('@')")

    show("la versión mala", usuarios[0].let { u -> u.email?.let { e -> e.split("@").let { p -> p.first() } } })
    show("la versión buena", usuarios[0].email?.substringBefore('@'))

    bullet("Tres `let` anidados y tres `it` distintos. El resultado es el mismo.")

    section("5. Cuando una variable con nombre dice más")

    // Una variable bien nombrada documenta; un `it` no.
    val conNombre = run {
        val usuariosActivos = usuarios.filter { it.email != null }
        val nombresOrdenados = usuariosActivos.map { it.nombre }.sorted()
        nombresOrdenados.joinToString()
    }
    show("con variables con nombre", conNombre)
    bullet("No todo tiene que ser una cadena. A veces tres `val` se leen mejor.")
}

/**
 * Encadenar con cabeza.
 */
fun demoChaining() {
    section("Una cadena legible")

    val salida = usuarios
        .filter { it.edad >= 30 }
        .sortedBy { it.nombre }
        .map { it.nombre.uppercase() }
        .joinToString(" · ")

    show("cadena de operaciones de colección", salida)
    bullet("Esto NO son scope functions: son operaciones de colección (capítulo 13).")
    bullet("Se encadenan sin límite porque cada una hace UNA cosa con nombre claro.")

    section("Mezclando scope functions con la cadena")

    val pasos = mutableListOf<String>()
    val procesado = usuarios
        .filter { it.email != null }
        .also { pasos.add("filtrados: ${it.size}") }
        .map { it.nombre }
        .let { nombres -> nombres to nombres.size }

    show("resultado", procesado)
    show("pasos", pasos)

    bullet("Una o dos scope functions en una cadena están bien.")
    bullet("A partir de ahí, corta la cadena en dos con una variable con nombre.")

    section("El límite práctico")

    bullet("Si tienes que contar los `it` para saber a qué se refiere cada uno,")
    bullet("la cadena es demasiado larga. Parte en dos y ponle nombre al intermedio.")

    section("Nombrar el parámetro en vez de usar `it`")

    // En lambdas anidadas, dar nombre a cada parámetro elimina toda la ambigüedad.
    val claro = usuarios.mapNotNull { usuario ->
        usuario.email?.let { correo -> "${usuario.nombre} <$correo>" }
    }
    show("con parámetros nombrados", claro)
    bullet("`usuario` y `correo` en lugar de dos `it`: cuesta 10 caracteres y")
    bullet("ahorra diez minutos al siguiente que lo lea.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Reescribe la "versión mala" de los tres `let` paso a paso hasta la buena.
//  2. Cambia un `also` con traza por un `apply` y observa qué se rompe.
//  3. Coge una cadena de tu propio código con más de 5 eslabones y pártela en dos.
//  4. Sustituye un `?.let { } ?: ...` por `if/else` y quédate con el que leas mejor.
