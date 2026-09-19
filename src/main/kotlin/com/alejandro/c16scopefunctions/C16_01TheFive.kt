package com.alejandro.c16scopefunctions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  16.1 · Las cinco scope functions
//
//  QUÉ ES
//    `let`, `run`, `with`, `apply` y `also`. Las cinco ejecutan un bloque sobre un
//    objeto. Se diferencian en sólo DOS cosas:
//      · cómo se refiere uno al objeto dentro del bloque: `this` o `it`
//      · qué devuelve la llamada: el resultado del bloque, o el propio objeto
//
//  POR QUÉ IMPORTA
//    Son omnipresentes en el código Kotlin, y elegir la equivocada hace que el código
//    se lea peor que sin ninguna. Una vez memorizada la tabla, la elección es mecánica.
//
//  ERRORES COMUNES
//    · Usar `let` para todo, incluso cuando `apply` o `also` dirían mejor la intención.
//    · Encadenar tres scope functions y perder de vista qué es `this` y qué es `it`.
//    · Usar `run`/`apply` donde un simple `if (x != null)` era más claro.
// =====================================================================================

private class Configuracion {
    var host: String = ""
    var puerto: Int = 0
    val cabeceras = mutableMapOf<String, String>()
    override fun toString(): String = "$host:$puerto ${cabeceras.keys}"
}

/**
 * La tabla que hay que memorizar.
 */
fun demoTheTable() {
    section("Las dos preguntas que las distinguen")

    bullet("¿Cómo se accede al objeto?  → `this` (receptor) o `it` (parámetro)")
    bullet("¿Qué devuelve la llamada?   → el resultado del bloque, o el propio objeto")

    section("La tabla completa")

    bullet("función   objeto    devuelve              se llama como")
    bullet("───────   ───────   ───────────────────   ─────────────────")
    bullet("let       it        el bloque             obj.let { }")
    bullet("run       this      el bloque             obj.run { }")
    bullet("with      this      el bloque             with(obj) { }")
    bullet("apply     this      EL OBJETO             obj.apply { }")
    bullet("also      it        EL OBJETO             obj.also { }")

    section("La regla mnemotécnica")

    bullet("Las que devuelven el OBJETO acaban en -ly en inglés: apply, also.")
    bullet("De ésas, `apply` usa `this` y `also` usa `it`.")
    bullet("Las demás devuelven el bloque: `let` (it), `run` y `with` (this).")
}

/**
 * Las cinco sobre el mismo objeto, para verlas de golpe.
 */
fun demoSideBySide() {
    section("let: it, devuelve el bloque")

    val conLet = Configuracion().let {
        it.host = "api.ejemplo.com"
        it.puerto = 443
        "configurado ${it.host}"        // ← esto es lo que devuelve
    }
    show("tipo devuelto", "String")
    show("valor", conLet)

    section("run: this, devuelve el bloque")

    val conRun = Configuracion().run {
        host = "api.ejemplo.com"        // sin `it.`
        puerto = 443
        "configurado $host"
    }
    show("tipo devuelto", "String")
    show("valor", conRun)

    section("with: this, devuelve el bloque, pero NO es de extensión")

    val configuracion = Configuracion().apply { host = "ejemplo.com"; puerto = 80 }
    val conWith = with(configuracion) {
        "host=$host puerto=$puerto"
    }
    show("tipo devuelto", "String")
    show("valor", conWith)
    bullet("`with(obj) { }` en lugar de `obj.with { }`: es una función normal.")
    bullet("Por eso NO sirve para nulos: no hay `?.with`.")

    section("apply: this, devuelve EL OBJETO")

    val conApply = Configuracion().apply {
        host = "api.ejemplo.com"
        puerto = 443
        cabeceras["Accept"] = "application/json"
    }
    show("tipo devuelto", "Configuracion")
    show("valor", conApply)

    section("also: it, devuelve EL OBJETO")

    val traza = mutableListOf<String>()
    val conAlso = Configuracion()
        .apply { host = "api.ejemplo.com"; puerto = 443 }
        .also { traza.add("creada configuración para ${it.host}") }

    show("tipo devuelto", "Configuracion")
    show("valor", conAlso)
    show("traza", traza)
}

/**
 * Cuándo usar cada una.
 */
fun demoWhenToUseEach() {
    section("let → transformar, o ejecutar sólo si no es nulo")

    val texto: String? = "  Kotlin  "
    show("con ?.let", texto?.let { it.trim().uppercase() })

    val nulo: String? = null
    show("sobre null no ejecuta", nulo?.let { it.trim() })

    // También para no repetir una expresión larga.
    val mapa = mapOf("clave" to 42)
    show("evitar repetir la expresión", mapa["clave"]?.let { "valor $it, el doble es ${it * 2}" })

    bullet("Úsalo cuando: hay nulos de por medio, o transformas a OTRO tipo.")

    section("run → calcular algo con varias propiedades del objeto")

    val config = Configuracion().apply { host = "ejemplo.com"; puerto = 8080 }
    val url = config.run { "https://$host:$puerto/api" }
    show("config.run { }", url)

    // `run` también existe SIN objeto: sólo para agrupar y devolver un valor.
    val calculado = run {
        val a = 3
        val b = 4
        a * a + b * b
    }
    show("run { } sin receptor", calculado)

    bullet("Úsalo cuando: necesitas varias propiedades del objeto para producir un valor.")

    section("with → lo mismo que run, cuando el objeto no puede ser nulo")

    show("with(config) { }", with(config) { "$host:$puerto" })
    bullet("`with` y `obj.run` hacen lo mismo. Elige `with` cuando el objeto ya")
    bullet("está en una variable, y `run` cuando encadenas.")

    section("apply → configurar un objeto y quedártelo")

    val configurado = Configuracion().apply {
        host = "localhost"
        puerto = 3000
    }
    show("apply devuelve el objeto", configurado)
    bullet("Es EL caso de uso de `apply`: construir/configurar sin variable temporal.")

    section("also → hacer algo con el objeto sin cambiar la cadena")

    val numeros = listOf(3, 1, 2)
        .also { /* aquí iría una traza: "entrada: $it" */ }
        .sorted()
        .also { /* "ordenado: $it" */ }

    show("also en medio de una cadena", numeros)
    bullet("Úsalo para efectos secundarios: trazas, validaciones, registrar métricas.")
    bullet("El `it` explícito recuerda que el objeto NO es el protagonista del bloque.")
}

/**
 * Casos donde son intercambiables (y cuál leer mejor).
 */
fun demoEquivalences() {
    section("apply y also configuran igual")

    val conApply = Configuracion().apply { host = "a.com" }
    val conAlso = Configuracion().also { it.host = "a.com" }
    show("apply", conApply.host)
    show("also", conAlso.host)
    bullet("Para configurar, `apply` gana: no hay que escribir `it.` en cada línea.")

    section("run y with calculan igual")

    val config = Configuracion().apply { host = "a.com"; puerto = 80 }
    show("config.run { }", config.run { "$host:$puerto" })
    show("with(config) { }", with(config) { "$host:$puerto" })
    bullet("Idénticos. `run` encadena mejor; `with` se lee mejor suelto.")

    section("let y run transforman igual")

    val texto = "kotlin"
    show("texto.let { it.uppercase() }", texto.let { it.uppercase() })
    show("texto.run { uppercase() }", texto.run { uppercase() })
    bullet("Con `let` el objeto se ve (`it`); con `run` desaparece. Si el bloque")
    bullet("usa el objeto una sola vez, `let` es más explícito.")

    section("La elección en tres preguntas")

    bullet("1. ¿Quiero recuperar el objeto? → sí: apply/also · no: let/run/with")
    bullet("2. ¿Hay nulos? → sí: obligatoriamente `?.let` o `?.run` (with no vale)")
    bullet("3. ¿Prefiero ver el objeto o que desaparezca? → it: let/also · this: run/apply/with")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia un `apply` por un `also` y añade los `it.` que hagan falta.
//  2. Intenta usar `with` sobre una variable nulable y comprueba que no hay `?.with`.
//  3. Reescribe `config.run { }` como `with(config) { }` y decide cuál lees mejor.
//  4. Sustituye un `?.let { }` por un `if (x != null) { }` y compara.
