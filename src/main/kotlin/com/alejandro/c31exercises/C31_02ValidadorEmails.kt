package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 2 · Validador de emails                                       🟢 fácil
//
//  Repasa: String, condiciones, buildList, sealed, colecciones, funciones de extensión.
//  Capítulos: 02 (String), 06 (nulabilidad), 11 (sealed), 13 (colecciones).
// =====================================================================================

/** Ejercicio 2: validador de direcciones de correo. */
fun ejercicio02ValidadorEmails() {
    enunciado(
        "Valida direcciones de correo devolviendo TODOS los motivos por los que no",
        "valen, no sólo el primero.",
        "",
        "Reglas mínimas:",
        "1. No puede estar en blanco.",
        "2. Tiene que haber exactamente una arroba.",
        "3. La parte de antes de la arroba no puede estar vacía.",
        "4. El dominio tiene que tener al menos un punto, y ni empezar ni acabar por él.",
        "5. La extensión final (lo de después del último punto) debe tener 2 o más letras.",
        "6. No puede haber espacios en ninguna parte.",
        "7. No puede haber dos puntos seguidos.",
        "",
        "Además: `normalizarEmail` que recorte espacios y pase a minúsculas, y",
        "`agruparPorDominio` que reparta una lista de emails válidos por su dominio.",
    )

    pistas(
        "`count { it == '@' }` te dice de golpe si hay cero, una o varias arrobas.",
        "`buildList { if (...) add(...) }` acumula motivos sin tener que crear una",
        "   lista mutable a mano (capítulo 25).",
        "`substringBefore('@')` y `substringAfterLast('.')` ahorran mucho `indexOf`.",
        "Cuidado con el orden: si no hay arroba, las reglas del dominio no tienen",
        "   sentido y conviene no añadirlas para no ahogar al usuario en mensajes.",
        "Para agrupar, `groupBy { it.substringAfter('@') }` (capítulo 13).",
    )

    solucionEnMarcha()

    section("Direcciones válidas")

    listOf(
        "ana@ejemplo.com",
        "ana.garcia@correo.ejemplo.es",
        "a@b.io",
        "usuario+etiqueta@ejemplo.org",
    ).forEach { show(it, describirEmail(validarEmail(it))) }

    section("Direcciones inválidas")

    listOf(
        "",
        "   ",
        "sinarroba.com",
        "dos@@arrobas.com",
        "@ejemplo.com",
        "ana@sinpunto",
        "ana@ejemplo.c",
        "ana garcia@ejemplo.com",
        "ana@ejemplo..com",
        "ana@.com",
        "ana@ejemplo.",
    ).forEach { show(quotedCorto(it), describirEmail(validarEmail(it))) }

    section("Normalizar antes de validar")

    listOf("  ANA@Ejemplo.COM  ", "Ana.Garcia@Correo.ES").forEach { crudo ->
        val normalizado = normalizarEmail(crudo)
        show(quotedCorto(crudo), "→ \"$normalizado\" (${describirEmail(validarEmail(normalizado))})")
    }

    section("Agrupar por dominio")

    val cartera = listOf(
        "ana@ejemplo.com",
        "luis@ejemplo.com",
        "marta@otra.es",
        "sinarroba.com",              // se descarta: no es válido
        "  PEDRO@Ejemplo.com  ",      // se normaliza antes de agrupar
    )
    agruparPorDominio(cartera).forEach { (dominio, usuarios) ->
        show(dominio, usuarios)
    }

    explicacion(
        "Dos decisiones marcan este ejercicio.",
        "",
        "PRIMERA: devolver la lista de motivos en vez de un booleano. Un formulario",
        "que dice «email inválido» es inútil; uno que dice «falta la arroba y hay un",
        "espacio» se puede arreglar a la primera. Y en los tests puedes comprobar",
        "exactamente qué regla saltó (capítulo 30).",
        "",
        "SEGUNDA: no encadenar comprobaciones que dependen de otras. Si no hay",
        "arroba, no se evalúan las reglas del dominio: serían ruido. Eso obliga a",
        "estructurar la función en dos tramos, y por eso hay un `return` temprano.",
        "",
        "Sobre la expresión regular: se podría hacer todo con una, pero la regex de",
        "verdad del estándar RFC 5322 tiene más de 400 caracteres, no la entiende",
        "nadie y aun así no garantiza que el buzón exista. La validación útil de un",
        "email es ENVIARLE un mensaje; lo demás es filtrar erratas evidentes.",
    )

    varianteDificil(
        "1. Añade una regla configurable: lista de dominios permitidos o prohibidos.",
        "2. Devuelve los motivos como un `enum` o un `sealed` en vez de texto, y",
        "   traduce a español sólo al mostrarlos (internacionalización).",
        "3. Soporta la parte local entre comillas: `\"raro pero valido\"@ejemplo.com`.",
        "4. Escribe un `sugerirCorreccion` que detecte erratas típicas: gmial.com →",
        "   gmail.com (pista: distancia de Levenshtein).",
        "5. Haz que `agruparPorDominio` devuelva también los descartados y por qué.",
    )

    testEn("ValidadorEmailsTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

sealed interface ResultadoEmail {
    data object Valido : ResultadoEmail
    data class Invalido(val motivos: List<String>) : ResultadoEmail
}

/**
 * Valida una dirección de correo y devuelve TODOS los motivos por los que no vale.
 *
 * Las reglas del dominio sólo se evalúan si hay exactamente una arroba: sin ella no
 * tendrían sentido y sólo añadirían ruido al mensaje de error.
 */
fun validarEmail(texto: String): ResultadoEmail {
    if (texto.isBlank()) return ResultadoEmail.Invalido(listOf("no puede estar en blanco"))

    val basicos = buildList {
        if (texto.any { it.isWhitespace() }) add("no puede contener espacios")
        when (texto.count { it == '@' }) {
            0 -> add("le falta la arroba")
            1 -> Unit
            else -> add("sólo puede haber una arroba")
        }
        if (".." in texto) add("no puede tener dos puntos seguidos")
    }

    // Si no hay una arroba y sólo una, parar aquí: el resto de reglas no aplican.
    if (texto.count { it == '@' } != 1) return ResultadoEmail.Invalido(basicos)

    val local = texto.substringBefore('@')
    val dominio = texto.substringAfter('@')

    val motivos = basicos + buildList {
        if (local.isEmpty()) add("falta la parte anterior a la arroba")
        if (dominio.isEmpty()) {
            add("falta el dominio")
        } else {
            if ('.' !in dominio) add("el dominio necesita al menos un punto")
            if (dominio.startsWith('.')) add("el dominio no puede empezar por punto")
            if (dominio.endsWith('.')) add("el dominio no puede acabar en punto")

            val extension = dominio.substringAfterLast('.', missingDelimiterValue = "")
            if (extension.length in 1..1) add("la extensión «$extension» es demasiado corta")
            if (extension.isNotEmpty() && !extension.all { it.isLetter() }) {
                add("la extensión «$extension» debe ser sólo letras")
            }
        }
    }

    return if (motivos.isEmpty()) ResultadoEmail.Valido else ResultadoEmail.Invalido(motivos)
}

/** Recorta y pasa a minúsculas: lo que se hace SIEMPRE antes de validar o guardar. */
fun normalizarEmail(texto: String): String = texto.trim().lowercase()

/** `true` si la dirección pasa todas las reglas. Azúcar para quien sólo quiera el sí/no. */
fun esEmailValido(texto: String): Boolean = validarEmail(texto) is ResultadoEmail.Valido

/**
 * Agrupa direcciones por dominio, descartando las que no son válidas.
 *
 * Normaliza primero, para que "ANA@Ejemplo.com" y "ana@ejemplo.com" caigan juntas.
 */
fun agruparPorDominio(emails: List<String>): Map<String, List<String>> = emails
    .map { normalizarEmail(it) }
    .filter { esEmailValido(it) }
    .groupBy { it.substringAfter('@') }

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun describirEmail(resultado: ResultadoEmail): String = when (resultado) {
    is ResultadoEmail.Valido -> "✓ válido"
    is ResultadoEmail.Invalido -> "✗ " + resultado.motivos.joinToString("; ")
}

private fun quotedCorto(texto: String): String = "\"$texto\""

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade la regla "la parte local no puede empezar ni acabar en punto".
//  2. Comprueba qué pasa con "ana@ejemplo.com." y decide si la regla actual basta.
//  3. Mide cuántos emails de una lista de 10.000 pasan la validación, con `count`.
