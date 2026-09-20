package com.alejandro.c06nullsafety

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  6.2 · Los operadores del día a día: ?. ?: ?.let y !!
//
//  QUÉ ES
//    Las herramientas concretas para trabajar con tipos nulables. El capítulo 3.10 ya
//    las presentó como operadores; aquí se ven como HERRAMIENTAS DE DISEÑO: cuál
//    elegir en cada situación.
//
//  POR QUÉ IMPORTA
//    Elegir bien entre `?.let`, `?:` y un `if` es la diferencia entre código que se
//    lee y una escalera de comprobaciones.
//
//  ERRORES COMUNES
//    · Usar `?.let { }` donde bastaba un `if (x != null)`.
//    · Encadenar `?.let` anidados en lugar de una guarda con `?: return`.
//    · Repartir `!!` para callar al compilador.
// =====================================================================================

/**
 * `?.` en cadena, y el patrón que sustituye a los if anidados.
 */
fun demoSafeCallChains() {
    section("Los datos")

    val completo = Pedido("P-1", Cliente("Ana", Direccion("Calle Mayor", "Madrid")))
    val sinDireccion = Pedido("P-2", Cliente("Luis", null))
    val sinCliente = Pedido("P-3", null)

    section("Sin ?. : una escalera")

    show("ciudadConIfs(completo)", ciudadConIfs(completo))
    show("ciudadConIfs(sinDireccion)", ciudadConIfs(sinDireccion))
    show("ciudadConIfs(sinCliente)", ciudadConIfs(sinCliente))

    section("Con ?. : una línea")

    show("ciudadConSafeCalls(completo)", ciudadConSafeCalls(completo))
    show("ciudadConSafeCalls(sinDireccion)", ciudadConSafeCalls(sinDireccion))
    show("ciudadConSafeCalls(sinCliente)", ciudadConSafeCalls(sinCliente))

    bullet("Misma lógica, tres líneas menos y sin posibilidad de olvidar un caso.")

    section("El resultado siempre es nulable")

    // Aunque `ciudad` no sea nulable, `pedido.cliente?.direccion?.ciudad` sí lo es:
    // basta con que un eslabón falle.
    show("tipo del resultado", "String? (aunque ciudad sea String)")
    show("por eso se remata con ?:", completo.cliente?.direccion?.ciudad ?: "desconocida")
}

/**
 * Elvis: valores por defecto y guardas.
 */
fun demoElvisPatterns() {
    section("1. Valor por defecto")

    val sinNombre: String? = null
    show("sinNombre ?: \"invitado\"", sinNombre ?: "invitado")

    section("2. Guarda con return (el patrón más útil)")

    // Convierte un `if` anidado en una salida temprana. El resto de la función
    // trabaja ya con un valor garantizado.
    show("longitudDeCiudad(pedidoCompleto)", longitudDeCiudad(Pedido("P-1", Cliente("Ana", Direccion("X", "Madrid")))))
    show("longitudDeCiudad(sinCliente)", longitudDeCiudad(Pedido("P-2", null)))

    section("3. Guarda con throw")

    val fallo = try {
        exigirCliente(Pedido("P-3", null))
    } catch (e: IllegalStateException) {
        "lanzó IllegalStateException: ${e.message}"
    }
    show("exigirCliente(sinCliente)", fallo)
    show("exigirCliente(completo)", exigirCliente(Pedido("P-4", Cliente("Ana", null))))

    section("4. Encadenar varios Elvis")

    // Se evalúan de izquierda a derecha y gana el primero que no sea null.
    val preferido: String? = null
    val alternativo: String? = null
    val ultimo = "por defecto"
    show("preferido ?: alternativo ?: ultimo", preferido ?: alternativo ?: ultimo)

    section("5. Elvis con una expresión a la derecha")

    val entrada: String? = null
    show("entrada?.trim()?.ifEmpty { null } ?: \"vacío\"", entrada?.trim()?.ifEmpty { null } ?: "vacío")
}

/**
 * `?.let`: cuándo sí y cuándo no.
 */
fun demoSafeLet() {
    section("Para qué sirve")

    val texto: String? = "  Kotlin  "

    // `?.let { }` ejecuta el bloque sólo si hay valor, y devuelve su resultado.
    val procesado = texto?.let {
        val limpio = it.trim()
        "$limpio (${limpio.length} caracteres)"
    }
    show("texto?.let { ... }", procesado)

    val nulo: String? = null
    show("nulo?.let { ... }", nulo?.let { it.trim() })

    section("Cuándo `let` gana al `if`")

    // Cuando el valor viene de una EXPRESIÓN que no quieres repetir ni guardar.
    val usuarios = mapOf("ana" to 34)
    show("mapa[clave]?.let { ... }", usuarios["ana"]?.let { "edad: $it" } ?: "no está")

    bullet("Con `if` tendrías que meter `usuarios[\"ana\"]` en una variable antes.")

    section("Cuándo el `if` gana al `let`")

    val nombre: String? = "Ana"

    // Con una propiedad local simple, el `if` se lee mejor y hace smart cast.
    if (nombre != null) {
        show("if (nombre != null)", nombre.uppercase())
    }

    bullet("`?.let { }` sobre un `val` local no aporta nada frente a `if != null`.")
    bullet("Y si dentro necesitas `else`, el `if` es claramente mejor.")

    section("El antipatrón: let anidados")

    // Esto es lo que NO hay que escribir:
    //     a?.let { x ->
    //         b?.let { y ->
    //             c?.let { z -> ... }
    //         }
    //     }
    bullet("Tres `?.let` anidados = la escalera que veníamos a evitar.")
    bullet("La alternativa: guardas con `?: return` una detrás de otra.")

    show("combinar(\"a\", \"b\", \"c\")", combinar("a", "b", "c"))
    show("combinar(\"a\", null, \"c\")", combinar("a", null, "c"))
}

/**
 * `!!`: el último recurso.
 */
fun demoNotNullAssertionInDepth() {
    section("Qué significa de verdad")

    bullet("`x!!` le dice al compilador: 'confía en mí, aquí no hay null'.")
    bullet("Si te equivocas, NPE en ejecución. El compilador ya no te protege.")

    section("Las alternativas, de mejor a peor")

    val configuracion: String? = "valor"

    show("1. ?:  con valor por defecto", configuracion ?: "por defecto")
    show("2. ?: return / throw (guarda)", leerConfiguracion(configuracion))
    show("3. requireNotNull con mensaje", requireNotNull(configuracion) { "falta la configuración" })
    show("4. checkNotNull con mensaje", checkNotNull(configuracion) { "estado inconsistente" })
    show("5. !! (sin mensaje, sin contexto)", configuracion!!)

    section("La diferencia práctica")

    val nulo: String? = null

    val conBang = try {
        nulo!!
    } catch (e: NullPointerException) {
        "NPE sin ninguna pista de qué falló"
    }
    show("nulo!!", conBang)

    val conRequire = try {
        requireNotNull(nulo) { "el host del servidor no estaba configurado" }
    } catch (e: IllegalArgumentException) {
        "IllegalArgumentException: ${e.message}"
    }
    show("requireNotNull(nulo) { ... }", conRequire)

    bullet("El mismo fallo, pero uno te dice QUÉ faltaba y el otro no.")

    section("La única regla sobre !!")

    bullet("Si escribes `!!`, escribe al lado un comentario explicando por qué es seguro.")
    bullet("Si no puedes explicarlo, no es seguro.")
    bullet("`a!!.b!!.c` nunca es la respuesta: el modelo de datos está mal.")
}

// -- Tipos y funciones auxiliares -----------------------------------------------------

private class Direccion(val calle: String, val ciudad: String)
private class Cliente(val nombre: String, val direccion: Direccion?)
private class Pedido(val referencia: String, val cliente: Cliente?)

/** La escalera de comprobaciones, para comparar. */
private fun ciudadConIfs(pedido: Pedido): String {
    val cliente = pedido.cliente
    if (cliente != null) {
        val direccion = cliente.direccion
        if (direccion != null) {
            return direccion.ciudad
        }
    }
    return "desconocida"
}

/** Lo mismo con llamadas seguras y Elvis. */
private fun ciudadConSafeCalls(pedido: Pedido): String =
    pedido.cliente?.direccion?.ciudad ?: "desconocida"

/** Guarda con `?: return`: el resto de la función ya trabaja con valores seguros. */
private fun longitudDeCiudad(pedido: Pedido): Int {
    val ciudad = pedido.cliente?.direccion?.ciudad ?: return -1
    return ciudad.length
}

/** Guarda con `?: throw`. */
private fun exigirCliente(pedido: Pedido): String {
    val cliente = pedido.cliente ?: error("el pedido ${pedido.referencia} no tiene cliente")
    return cliente.nombre
}

/** Varias guardas seguidas en lugar de `let` anidados. */
private fun combinar(a: String?, b: String?, c: String?): String {
    val primero = a ?: return "falta el primero"
    val segundo = b ?: return "falta el segundo"
    val tercero = c ?: return "falta el tercero"
    return primero + segundo + tercero
}

private fun leerConfiguracion(valor: String?): String {
    val seguro = valor ?: return "(sin configuración)"
    return seguro.uppercase()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Reescribe `combinar` con tres `?.let` anidados y compara qué se lee mejor.
//  2. Quita el `?:` final de `ciudadConSafeCalls` y observa cómo cambia el tipo devuelto.
//  3. Cambia `error(...)` por `throw IllegalStateException(...)`: son lo mismo (cap. 25).
//  4. Sustituye un `!!` de tu propio código por `requireNotNull` con un buen mensaje.
