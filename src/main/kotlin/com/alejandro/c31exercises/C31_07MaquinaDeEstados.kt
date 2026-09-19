package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 7 · Modelado con sealed: la vida de un pedido                 🟡 medio
//
//  Repasa: sealed class/interface, data object, when exhaustivo sobre pares,
//  máquinas de estados, modelar con tipos en vez de con banderas.
//  Capítulos: 09 (data class), 10 (enum), 11 (sealed), 20 (comprobaciones de tipo).
// =====================================================================================

/** Ejercicio 7: la máquina de estados de un pedido. */
fun ejercicio07MaquinaDeEstados() {
    enunciado(
        "Modela la vida de un pedido de forma que los estados IMPOSIBLES no se",
        "puedan ni escribir.",
        "",
        "Estados: Borrador → Confirmado → Enviado → Entregado, y Cancelado desde",
        "cualquiera menos Entregado.",
        "",
        "1. Cada estado lleva SUS datos y sólo los suyos:",
        "   · Confirmado, el instante de confirmación,",
        "   · Enviado, además el número de seguimiento,",
        "   · Entregado, además quién firmó,",
        "   · Cancelado, el motivo.",
        "2. Eventos: Confirmar, Enviar(seguimiento), Entregar(firmante), Cancelar(motivo).",
        "3. `transicion(estado, evento)` devuelve el estado nuevo o explica por qué",
        "   esa transición no es válida.",
        "4. Un pedido entregado es FINAL: no se puede cancelar ni reenviar.",
        "5. Un historial con todas las transiciones que sí ocurrieron.",
    )

    pistas(
        "La clave es NO usar un enum con campos opcionales. Si el estado fuera",
        "   `enum Estado` más `var seguimiento: String?`, nada impediría un pedido",
        "   en Borrador con número de seguimiento. Con `sealed`, eso no se escribe.",
        "`when (estado to evento)` no funciona bien; usa `when (estado)` por fuera y",
        "   `when (evento)` por dentro: así el compilador comprueba los dos niveles.",
        "Los estados sin datos propios son `data object`; los que llevan datos,",
        "   `data class` (capítulo 09).",
        "Para el instante, inyecta un reloj en vez de llamar al sistema: el ejercicio",
        "   se vuelve testeable de golpe (capítulo 30).",
        "El historial es una lista inmutable a la que se va añadiendo con `+`.",
    )

    solucionEnMarcha()

    section("El camino feliz")

    val reloj = RelojDePedidos(ahoraMs = 1_700_000_000_000)
    var pedido = Pedido.nuevo("PED-1")
    show("inicio", pedido.estado)

    listOf<EventoPedido>(
        EventoPedido.Confirmar,
        EventoPedido.Enviar("ES-99887766"),
        EventoPedido.Entregar("A. García"),
    ).forEach { evento ->
        when (val resultado = pedido.aplicar(evento, reloj)) {
            is ResultadoTransicion.Ok -> {
                pedido = resultado.pedido
                show(evento::class.simpleName!!, "→ ${pedido.estado}")
            }
            is ResultadoTransicion.NoPermitida -> show(evento::class.simpleName!!, "✗ ${resultado.motivo}")
        }
        reloj.avanzarHoras(6)
    }

    section("Lo que ya no se puede hacer")

    listOf<EventoPedido>(
        EventoPedido.Cancelar("me he arrepentido"),
        EventoPedido.Enviar("OTRO-123"),
        EventoPedido.Confirmar,
    ).forEach { evento ->
        show(evento::class.simpleName!!, describirTransicion(pedido.aplicar(evento, reloj)))
    }

    section("El historial")

    pedido.historial.forEach { bullet(it) }

    section("Un pedido cancelado a medias")

    var otro = Pedido.nuevo("PED-2")
    otro = (otro.aplicar(EventoPedido.Confirmar, reloj) as ResultadoTransicion.Ok).pedido
    show("tras confirmar", otro.estado)

    val cancelado = otro.aplicar(EventoPedido.Cancelar("sin stock"), reloj)
    show("cancelar", describirTransicion(cancelado))

    otro = (cancelado as ResultadoTransicion.Ok).pedido
    show("¿se puede reactivar?", describirTransicion(otro.aplicar(EventoPedido.Confirmar, reloj)))

    section("Consultas que el tipo hace gratis")

    show("¿PED-1 está entregado?", pedido.estado is EstadoPedido.Entregado)
    show("seguimiento de PED-1", (pedido.estado as? EstadoPedido.Entregado)?.seguimiento)
    show("¿PED-2 sigue vivo?", otro.estado.esFinal.not())
    show("motivo de PED-2", (otro.estado as? EstadoPedido.Cancelado)?.motivo)

    explicacion(
        "EL PUNTO CENTRAL: los datos viven DENTRO del estado al que pertenecen.",
        "",
        "La alternativa que todo el mundo escribe la primera vez es:",
        "",
        "   enum class Estado { BORRADOR, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO }",
        "   class Pedido(var estado: Estado, var seguimiento: String?, var motivo: String?)",
        "",
        "Con eso, un pedido en BORRADOR con `seguimiento = \"ES-1\"` y `motivo =",
        "\"cancelado\"` compila perfectamente. Nada te avisa. Y acabas escribiendo",
        "comprobaciones defensivas por todas partes: `if (estado == ENVIADO &&",
        "seguimiento != null)`.",
        "",
        "Con `sealed`, `EstadoPedido.Enviado` EXIGE el seguimiento en su constructor",
        "y `EstadoPedido.Borrador` no tiene dónde guardarlo. El estado imposible no",
        "es que esté prohibido: es que no se puede escribir.",
        "",
        "La segunda idea: `transicion` devuelve un resultado, no muta nada. `Pedido`",
        "es inmutable y `aplicar` devuelve uno nuevo. Así el historial es fiable y",
        "no hay forma de que dos partes del código se pisen el estado.",
        "",
        "Y fíjate en `when (estado)` con `when (evento)` anidado: el compilador",
        "comprueba que has cubierto TODAS las combinaciones. Añade un estado y",
        "sabrás exactamente qué falta rellenar.",
    )

    varianteDificil(
        "1. Añade `Devuelto` (sólo desde Entregado, y con plazo de 14 días: ahí es",
        "   donde el reloj inyectado empieza a pagar de verdad).",
        "2. Haz que cada estado guarde el anterior, para poder reconstruir la",
        "   trazabilidad sin historial aparte.",
        "3. Separa los EFECTOS: que `transicion` devuelva también una lista de",
        "   acciones a ejecutar (enviar email, cobrar, avisar al almacén).",
        "4. Escribe una función que dibuje el diagrama de la máquina en texto a",
        "   partir de las transiciones válidas.",
        "5. Añade estados con tiempo: un Confirmado que caduca si no se envía en 48 h.",
    )

    testEn("MaquinaDeEstadosTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/**
 * Los estados de un pedido. Cada uno lleva SUS datos, y sólo los suyos.
 *
 * Eso hace imposible escribir un pedido en borrador con número de seguimiento.
 */
sealed interface EstadoPedido {

    /** Un estado final es aquel del que ya no se sale. */
    val esFinal: Boolean get() = false

    data object Borrador : EstadoPedido

    data class Confirmado(val confirmadoEnMs: Long) : EstadoPedido

    data class Enviado(val confirmadoEnMs: Long, val seguimiento: String) : EstadoPedido

    data class Entregado(
        val confirmadoEnMs: Long,
        val seguimiento: String,
        val firmadoPor: String,
        val entregadoEnMs: Long,
    ) : EstadoPedido {
        override val esFinal: Boolean get() = true
    }

    data class Cancelado(val motivo: String, val canceladoEnMs: Long) : EstadoPedido {
        override val esFinal: Boolean get() = true
    }
}

sealed interface EventoPedido {
    data object Confirmar : EventoPedido
    data class Enviar(val seguimiento: String) : EventoPedido
    data class Entregar(val firmadoPor: String) : EventoPedido
    data class Cancelar(val motivo: String) : EventoPedido
}

sealed interface ResultadoTransicion {
    data class Ok(val pedido: Pedido) : ResultadoTransicion
    data class NoPermitida(val motivo: String) : ResultadoTransicion
}

/** El reloj, inyectado para que los tests no dependan de la hora real (capítulo 30). */
class RelojDePedidos(private var ahoraMs: Long) {
    fun ahora(): Long = ahoraMs

    fun avanzarHoras(horas: Int) {
        ahoraMs += horas * 3_600_000L
    }
}

/**
 * Un pedido inmutable: `aplicar` no cambia nada, devuelve un pedido nuevo.
 */
data class Pedido(
    val referencia: String,
    val estado: EstadoPedido,
    val historial: List<String>,
) {
    fun aplicar(evento: EventoPedido, reloj: RelojDePedidos): ResultadoTransicion {
        val nuevo = transicion(estado, evento, reloj.ahora())
            ?: return ResultadoTransicion.NoPermitida(
                "no se puede ${nombreDe(evento)} un pedido en estado ${nombreDe(estado)}",
            )

        return ResultadoTransicion.Ok(
            copy(
                estado = nuevo,
                historial = historial + "${nombreDe(estado)} --${nombreDe(evento)}--> ${nombreDe(nuevo)}",
            ),
        )
    }

    companion object {
        fun nuevo(referencia: String) = Pedido(referencia, EstadoPedido.Borrador, emptyList())
    }
}

/**
 * La tabla de transiciones.
 *
 * `when` sobre el estado por fuera y sobre el evento por dentro: el compilador
 * comprueba los dos niveles, así que añadir un estado o un evento rompe la
 * compilación hasta que decidas qué pasa en cada caso.
 *
 * @return el estado nuevo, o `null` si la transición no es válida.
 */
fun transicion(estado: EstadoPedido, evento: EventoPedido, ahoraMs: Long): EstadoPedido? =
    when (estado) {
        is EstadoPedido.Borrador -> when (evento) {
            is EventoPedido.Confirmar -> EstadoPedido.Confirmado(ahoraMs)
            is EventoPedido.Cancelar -> EstadoPedido.Cancelado(evento.motivo, ahoraMs)
            is EventoPedido.Enviar, is EventoPedido.Entregar -> null
        }

        is EstadoPedido.Confirmado -> when (evento) {
            is EventoPedido.Enviar -> EstadoPedido.Enviado(estado.confirmadoEnMs, evento.seguimiento)
            is EventoPedido.Cancelar -> EstadoPedido.Cancelado(evento.motivo, ahoraMs)
            is EventoPedido.Confirmar, is EventoPedido.Entregar -> null
        }

        is EstadoPedido.Enviado -> when (evento) {
            is EventoPedido.Entregar -> EstadoPedido.Entregado(
                confirmadoEnMs = estado.confirmadoEnMs,
                seguimiento = estado.seguimiento,
                firmadoPor = evento.firmadoPor,
                entregadoEnMs = ahoraMs,
            )
            is EventoPedido.Cancelar -> EstadoPedido.Cancelado(evento.motivo, ahoraMs)
            is EventoPedido.Confirmar, is EventoPedido.Enviar -> null
        }

        // Los estados finales no aceptan NADA. Se escribe una vez y se acabó.
        is EstadoPedido.Entregado, is EstadoPedido.Cancelado -> null
    }

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun nombreDe(valor: Any): String = valor::class.simpleName ?: "?"

private fun describirTransicion(resultado: ResultadoTransicion): String = when (resultado) {
    is ResultadoTransicion.Ok -> "✓ ${resultado.pedido.estado}"
    is ResultadoTransicion.NoPermitida -> "✗ ${resultado.motivo}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `EstadoPedido.Devuelto` y mira cuántos sitios deja de compilar. Cada uno
//     es una decisión que habrías olvidado con un enum.
//  2. Cambia `Enviado` para que NO guarde `confirmadoEnMs` y observa qué información
//     se pierde por el camino.
//  3. Escribe `transicion` con `when (estado to evento)` y compara: el compilador
//     deja de ayudarte y necesitas un `else`.
