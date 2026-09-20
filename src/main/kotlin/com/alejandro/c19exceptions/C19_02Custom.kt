package com.alejandro.c19exceptions

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  19.2 · Excepciones propias
//
//  QUÉ ES
//    Declarar tus propios tipos de excepción, normalmente heredando de `Exception` o
//    de `RuntimeException`, y organizarlos en una jerarquía.
//
//  POR QUÉ IMPORTA
//    Una excepción propia permite a quien llama capturar EXACTAMENTE lo que sabe
//    tratar, y llevar dentro los datos del fallo (qué campo, qué id, qué intento)
//    en lugar de meterlos en un texto que luego hay que parsear.
//
//  ERRORES COMUNES
//    · Crear una excepción por cada mensaje distinto (acabas con cuarenta clases).
//    · Mensajes inútiles: "error", "fallo", "algo salió mal".
//    · Usar excepciones para errores esperados en lugar de un tipo de resultado.
// =====================================================================================

/**
 * Declarar una excepción.
 */
fun demoDeclaring() {
    section("Lo mínimo")

    bullet("class MiError(mensaje: String) : Exception(mensaje)")
    bullet("Eso es todo. No hace falta nada más.")

    val basica = try {
        throw ErrorSimple("algo concreto ha fallado")
    } catch (e: ErrorSimple) {
        "capturada: ${e.message}"
    }
    show("ErrorSimple", basica)

    section("Los cuatro constructores habituales")

    bullet("Exception()                          → sin mensaje")
    bullet("Exception(mensaje)                   → el caso normal")
    bullet("Exception(mensaje, causa)            → envolviendo otra excepción")
    bullet("Exception(causa)                     → sólo propagando")

    val conCausa = try {
        try {
            error("la base de datos no responde")
        } catch (e: IllegalStateException) {
            throw ErrorCompleto("no se pudo guardar el pedido", e)
        }
    } catch (e: ErrorCompleto) {
        "«${e.message}» ← «${e.cause?.message}»"
    }
    show("con causa", conCausa)

    section("¿Exception o RuntimeException?")

    bullet("En Java la diferencia era enorme: Exception era comprobada.")
    bullet("En Kotlin NO hay diferencia práctica: ninguna se comprueba.")
    bullet("Convención: hereda de Exception salvo que tu código también se use desde")
    bullet("Java, donde RuntimeException evita obligar a los `throws`.")

    section("Con datos dentro")

    // Lo que de verdad aporta una excepción propia: llevar la información del fallo
    // de forma estructurada, no dentro del texto del mensaje.
    val conDatos = try {
        validarEdad(-5)
    } catch (e: ErrorDeValidacion) {
        "campo='${e.campo}' valor='${e.valorRecibido}' regla='${e.regla}'"
    }
    show("ErrorDeValidacion", conDatos)

    bullet("Quien captura puede usar `e.campo` para marcar ese input en un formulario.")
    bullet("Si sólo hubiera un mensaje, tendría que parsearlo. Eso siempre acaba mal.")
}

/**
 * Jerarquías de excepciones.
 */
fun demoHierarchies() {
    section("Una raíz por dominio")

    // Con una raíz común, quien quiera puede capturar todo el dominio de golpe,
    // o afinar al caso concreto.
    val casos = listOf(
        { procesarPedido("", 1) },
        { procesarPedido("P-1", 0) },
        { procesarPedido("P-2", 9999) },
        { procesarPedido("P-3", 2) },
    )

    casos.forEach { caso ->
        val salida = try {
            caso()
        } catch (e: PedidoNoEncontrado) {
            "no encontrado: ${e.referencia}"
        } catch (e: StockInsuficiente) {
            "sin stock: pedidas ${e.solicitadas}, quedan ${e.disponibles}"
        } catch (e: ErrorDePedido) {
            "otro error de pedido: ${e.message}"
        }
        show("caso", salida)
    }

    section("Capturar toda la familia de una vez")

    val soloLaRaiz = try {
        procesarPedido("P-9", 9999)
    } catch (e: ErrorDePedido) {
        "capturado por la raíz: ${e::class.simpleName}"
    }
    show("catch (e: ErrorDePedido)", soloLaRaiz)

    bullet("Quien llama decide el nivel de detalle que necesita.")

    section("Cuándo crear una clase nueva")

    bullet("SÍ: cuando alguien va a querer capturar ESE caso en concreto.")
    bullet("SÍ: cuando el fallo lleva datos propios (un id, un campo, un límite).")
    bullet("NO: si sólo cambia el texto del mensaje. Para eso está el mensaje.")

    section("Excepción sellada: lo mejor de los dos mundos")

    // Con `sealed`, el `when` sobre el tipo de excepción puede ser exhaustivo.
    listOf(
        ErrorDeRed.SinConexion,
        ErrorDeRed.TiempoAgotado(5_000),
        ErrorDeRed.RespuestaInvalida(500),
    ).forEach { error ->
        show(error::class.simpleName ?: "?", explicar(error))
    }

    bullet("`sealed class X : Exception()` permite el `when` exhaustivo del capítulo 11")
    bullet("sin renunciar a poder lanzarla y capturarla.")
}

/**
 * Escribir buenos mensajes.
 */
fun demoGoodMessages() {
    section("Un mensaje malo")

    bullet("\"Error\"                      → ¿qué error?")
    bullet("\"Valor inválido\"             → ¿qué valor? ¿por qué es inválido?")
    bullet("\"No se pudo procesar\"        → ¿qué? ¿por qué? ¿qué hago ahora?")

    section("Un mensaje bueno")

    bullet("Dice QUÉ pasó, CON QUÉ datos, y si se puede, QUÉ se esperaba.")

    show("malo", mensajeMalo(-5))
    show("bueno", mensajeBueno(-5))

    section("La plantilla que funciona")

    bullet("«No se pudo <acción> porque <motivo>. Recibido: <valor>. Esperado: <regla>.»")

    show("aplicando la plantilla", mensajeConPlantilla(campo = "edad", valor = "-5"))

    section("Los `require`/`check` ya la fomentan")

    // El mensaje de require se construye SÓLO si la condición falla, así que puede
    // ser todo lo detallado que quieras sin coste en el camino feliz.
    val conRequire = try {
        configurarPuerto(99_999)
    } catch (e: IllegalArgumentException) {
        e.message ?: "(sin mensaje)"
    }
    show("require con mensaje detallado", conRequire)

    bullet("La lambda del mensaje sólo se evalúa si la condición es falsa.")
    bullet("Por eso puedes interpolar valores sin preocuparte por el rendimiento.")
    bullet("Se ve a fondo en el capítulo 25.")
}

// -- Las excepciones y funciones que usan las demos -----------------------------------------

private class ErrorSimple(mensaje: String) : Exception(mensaje)

private class ErrorCompleto(mensaje: String, causa: Throwable?) : Exception(mensaje, causa)

/** Una excepción que lleva datos, no sólo texto. */
private class ErrorDeValidacion(
    val campo: String,
    val valorRecibido: String,
    val regla: String,
) : Exception("el campo '$campo' no cumple '$regla' (recibido: '$valorRecibido')")

private fun validarEdad(edad: Int): String {
    if (edad !in 0..130) {
        throw ErrorDeValidacion(campo = "edad", valorRecibido = "$edad", regla = "entre 0 y 130")
    }
    return "edad válida"
}

// Jerarquía con raíz común.
private open class ErrorDePedido(mensaje: String) : Exception(mensaje)

private class PedidoNoEncontrado(val referencia: String) :
    ErrorDePedido("no existe el pedido '$referencia'")

private class StockInsuficiente(val solicitadas: Int, val disponibles: Int) :
    ErrorDePedido("se pidieron $solicitadas unidades y sólo hay $disponibles")

private class CantidadInvalida(val cantidad: Int) :
    ErrorDePedido("la cantidad debe ser positiva, recibido $cantidad")

private fun procesarPedido(referencia: String, cantidad: Int): String = when {
    referencia.isBlank() -> throw PedidoNoEncontrado("(vacía)")
    cantidad <= 0 -> throw CantidadInvalida(cantidad)
    cantidad > 100 -> throw StockInsuficiente(cantidad, 100)
    else -> "pedido $referencia procesado ($cantidad unidades)"
}

/** Una jerarquía sellada que además es lanzable. */
private sealed class ErrorDeRed(mensaje: String) : Exception(mensaje) {
    data object SinConexion : ErrorDeRed("no hay conexión")
    class TiempoAgotado(val milisegundos: Int) : ErrorDeRed("agotados $milisegundos ms")
    class RespuestaInvalida(val codigo: Int) : ErrorDeRed("código HTTP $codigo")
}

/** `when` exhaustivo sobre una jerarquía de excepciones. */
private fun explicar(error: ErrorDeRed): String = when (error) {
    is ErrorDeRed.SinConexion -> "comprueba el cable"
    is ErrorDeRed.TiempoAgotado -> "sube el timeout por encima de ${error.milisegundos} ms"
    is ErrorDeRed.RespuestaInvalida ->
        if (error.codigo >= 500) "es culpa del servidor" else "es culpa de la petición"
}

private fun mensajeMalo(edad: Int): String = "Valor inválido"

private fun mensajeBueno(edad: Int): String =
    "No se pudo crear el usuario porque la edad está fuera de rango. " +
        "Recibido: $edad. Esperado: entre 0 y 130."

private fun mensajeConPlantilla(campo: String, valor: String): String =
    "No se pudo validar el formulario porque el campo '$campo' es inválido. " +
        "Recibido: '$valor'. Esperado: un número entre 0 y 130."

private fun configurarPuerto(puerto: Int): String {
    require(puerto in 1..65_535) {
        "el puerto debe estar entre 1 y 65535, pero se recibió $puerto"
    }
    return "escuchando en el puerto $puerto"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `PedidoYaEnviado` a la jerarquía y captúralo por separado.
//  2. Convierte ErrorDePedido en `sealed` y escribe un `when` exhaustivo sobre ella.
//  3. Quita la `cause` de ErrorCompleto y compara las trazas de pila.
//  4. Reescribe los mensajes de un proyecto tuyo siguiendo la plantilla de la demo.
