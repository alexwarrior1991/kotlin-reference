package com.alejandro.c11sealed

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  11.3 · Estados de pantalla, máquinas de estados y sealed frente a enum
//
//  QUÉ ES
//    El otro gran uso de las sealed: representar en qué situación está algo, con los
//    datos que corresponden a cada situación. Y la comparación definitiva con enum.
//
//  POR QUÉ IMPORTA
//    Modelar el estado con una sealed hace IMPOSIBLES los estados inválidos. El
//    clásico "cargando = true y a la vez hay error y además la lista está vacía" deja
//    de poder existir, porque sólo hay un estado a la vez.
//
//  ERRORES COMUNES
//    · Modelar el estado con tres booleanos independientes.
//    · Usar sealed donde un enum bastaba (todos los casos sin datos).
//    · Usar enum donde hacía falta sealed (y acabar con propiedades nulables).
// =====================================================================================

/**
 * El antipatrón: banderas booleanas sueltas.
 */
fun demoFlagsAntipattern() {
    section("Una clase de estado con banderas")

    // Con tres booleanos y dos nulables, hay 2*2*2*2*2 = 32 combinaciones posibles.
    // Sólo cuatro tienen sentido. Las otras 28 son bugs esperando a ocurrir.
    val imposible = EstadoConBanderas(
        cargando = true,
        error = "se cayó la red",
        elementos = listOf("a", "b"),
        vacio = true,
    )
    show("un estado imposible que SÍ compila", imposible)

    bullet("¿Está cargando o hay error? ¿Hay elementos o está vacío?")
    bullet("El tipo permite construirlo, así que alguien acabará construyéndolo.")
    bullet("Y la pantalla tendrá que decidir con una cadena de ifs frágil.")

    section("Cuántos estados válidos hay de verdad")

    bullet("32 combinaciones posibles · 4 con sentido · 28 bugs potenciales.")
}

/**
 * La solución: un estado, un tipo.
 */
fun demoUiState() {
    section("Cada estado con exactamente sus datos")

    val estados: List<EstadoPantalla> = listOf(
        EstadoPantalla.Cargando,
        EstadoPantalla.Vacio("No hay resultados para 'kotlin'"),
        EstadoPantalla.Contenido(listOf("Kotlin in Action", "Atomic Kotlin"), total = 2),
        EstadoPantalla.Error("Sin conexión", reintentable = true),
    )

    estados.forEach { estado ->
        show(estado::class.simpleName ?: "?", pintar(estado))
    }

    section("Los estados imposibles ya no se pueden escribir")

    bullet("No existe `Contenido` con error dentro: son tipos distintos.")
    bullet("No existe `Cargando` con elementos: `Cargando` no tiene campos.")
    bullet("El compilador ha convertido 28 bugs en errores de compilación.")

    section("Y la pantalla se escribe sola")

    bullet("Un `when` exhaustivo y cada rama sabe exactamente qué datos tiene.")
    bullet("Si mañana añades `SinPermiso`, el compilador te lleva a `pintar`.")
}

/**
 * Máquina de estados: transiciones válidas.
 */
fun demoStateMachine() {
    section("El ciclo de vida de un pedido")

    // Las transiciones válidas se codifican en el propio tipo: desde `Nuevo` sólo se
    // puede ir a `Pagado` o `Cancelado`, y eso lo impone la función, no un comentario.
    var estado: EstadoPedido = EstadoPedido.Nuevo(referencia = "P-1")
    show("inicial", estado)

    estado = avanzar(estado, Accion.Pagar("tarjeta"))
    show("tras Pagar", estado)

    estado = avanzar(estado, Accion.Enviar("SEUR-123"))
    show("tras Enviar", estado)

    estado = avanzar(estado, Accion.Entregar)
    show("tras Entregar", estado)

    section("Las transiciones inválidas se rechazan")

    val yaEntregado = estado
    show("intentar pagar algo ya entregado", avanzar(yaEntregado, Accion.Pagar("tarjeta")))
    show("el estado no cambió", yaEntregado)

    section("Cada estado lleva la información de SU momento")

    val enviado = avanzar(
        avanzar(EstadoPedido.Nuevo("P-2"), Accion.Pagar("bizum")),
        Accion.Enviar("MRW-999"),
    )
    if (enviado is EstadoPedido.Enviado) {
        show("número de seguimiento", enviado.seguimiento)
        show("método de pago conservado", enviado.metodoPago)
    }

    bullet("Un enum no podría guardar el número de seguimiento sólo en un estado.")
    bullet("Tendría que ser una propiedad nulable de la clase entera: el problema de siempre.")
}

/**
 * La comparación definitiva.
 */
fun demoSealedVsEnum() {
    section("La tabla")

    bullet("                              enum          sealed")
    bullet("Nº de casos fijo y conocido     sí             sí")
    bullet("when exhaustivo sin else        sí             sí")
    bullet("Datos distintos por caso        NO             SÍ")
    bullet("Varias instancias por caso      no (singleton) sí")
    bullet("Parámetros de tipo genéricos    no             sí")
    bullet("Se puede iterar (`entries`)     SÍ             no*")
    bullet("valueOf desde texto             sí             no")
    bullet("Implementar varias jerarquías   no             sí (interfaces)")

    bullet("* con `sealedSubclasses` por reflexión, pero no es lo habitual.")

    section("La pregunta que decide")

    bullet("¿Cada caso necesita DATOS PROPIOS?")
    bullet("   No  → enum. Más simple, iterable, convertible desde texto.")
    bullet("   Sí  → sealed.")

    section("Ejemplos del lado enum")

    show("días de la semana", DiaSemana.entries.map { it.name.take(3) })
    show("se pueden iterar", DiaSemana.entries.count { it.esLaborable })
    show("y convertir desde texto", DiaSemana.valueOf("LUNES"))

    bullet("Todos los casos tienen la misma forma: nombre y si es laborable.")

    section("Ejemplos del lado sealed")

    bullet("Resultado<T>: Exito lleva un valor, Error lleva un motivo.")
    bullet("EstadoPantalla: Contenido lleva la lista, Error lleva el mensaje.")
    bullet("Expresiones de un intérprete: Suma tiene dos hijos, Numero tiene un valor.")

    section("Se pueden combinar")

    // Un enum dentro de una sealed: lo fijo va en el enum, lo variable en la sealed.
    val notificaciones: List<Notificacion> = listOf(
        Notificacion.Sistema(Severidad.AVISO),
        Notificacion.Mensaje(de = "Ana", texto = "¿comemos?"),
    )
    notificaciones.forEach { show(it::class.simpleName ?: "?", resumir(it)) }

    bullet("El enum aporta la lista cerrada de severidades, iterable y convertible.")
    bullet("La sealed aporta que cada tipo de notificación lleve datos distintos.")
}

// -- El antipatrón ----------------------------------------------------------------------

private data class EstadoConBanderas(
    val cargando: Boolean,
    val error: String?,
    val elementos: List<String>,
    val vacio: Boolean,
)

// -- Estado de pantalla ------------------------------------------------------------------

private sealed interface EstadoPantalla {
    data object Cargando : EstadoPantalla
    data class Vacio(val mensaje: String) : EstadoPantalla
    data class Contenido(val elementos: List<String>, val total: Int) : EstadoPantalla
    data class Error(val mensaje: String, val reintentable: Boolean) : EstadoPantalla
}

private fun pintar(estado: EstadoPantalla): String = when (estado) {
    EstadoPantalla.Cargando -> "girando la ruedecita"
    is EstadoPantalla.Vacio -> "mensaje vacío: '${estado.mensaje}'"
    is EstadoPantalla.Contenido -> "lista de ${estado.total}: ${estado.elementos.joinToString()}"
    is EstadoPantalla.Error -> {
        val boton = if (estado.reintentable) " [Reintentar]" else ""
        "error: ${estado.mensaje}$boton"
    }
}

// -- Máquina de estados ------------------------------------------------------------------

private sealed interface EstadoPedido {
    val referencia: String

    data class Nuevo(override val referencia: String) : EstadoPedido
    data class Pagado(override val referencia: String, val metodoPago: String) : EstadoPedido
    data class Enviado(
        override val referencia: String,
        val metodoPago: String,
        val seguimiento: String,
    ) : EstadoPedido

    data class Entregado(override val referencia: String, val seguimiento: String) : EstadoPedido
    data class Cancelado(override val referencia: String, val motivo: String) : EstadoPedido
}

private sealed interface Accion {
    data class Pagar(val metodo: String) : Accion
    data class Enviar(val seguimiento: String) : Accion
    data object Entregar : Accion
    data class Cancelar(val motivo: String) : Accion
}

/**
 * Las transiciones válidas viven en el código, no en un diagrama que se desactualiza.
 * Cualquier combinación no contemplada devuelve el estado sin tocar.
 */
private fun avanzar(estado: EstadoPedido, accion: Accion): EstadoPedido = when {
    estado is EstadoPedido.Nuevo && accion is Accion.Pagar ->
        EstadoPedido.Pagado(estado.referencia, accion.metodo)

    estado is EstadoPedido.Pagado && accion is Accion.Enviar ->
        EstadoPedido.Enviado(estado.referencia, estado.metodoPago, accion.seguimiento)

    estado is EstadoPedido.Enviado && accion is Accion.Entregar ->
        EstadoPedido.Entregado(estado.referencia, estado.seguimiento)

    estado is EstadoPedido.Nuevo && accion is Accion.Cancelar ->
        EstadoPedido.Cancelado(estado.referencia, accion.motivo)

    // Transición no permitida: el estado se queda como estaba.
    else -> estado
}

// -- enum + sealed combinados --------------------------------------------------------------

private enum class Severidad { INFO, AVISO, GRAVE }

private enum class DiaSemana(val esLaborable: Boolean) {
    LUNES(true), MARTES(true), MIERCOLES(true), JUEVES(true),
    VIERNES(true), SABADO(false), DOMINGO(false),
}

private sealed interface Notificacion {
    data class Sistema(val severidad: Severidad) : Notificacion
    data class Mensaje(val de: String, val texto: String) : Notificacion
}

private fun resumir(notificacion: Notificacion): String = when (notificacion) {
    is Notificacion.Sistema -> "sistema [${notificacion.severidad}]"
    is Notificacion.Mensaje -> "${notificacion.de}: ${notificacion.texto}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `data object SinPermiso : EstadoPantalla` y mira que `pintar` deja de compilar.
//  2. Intenta crear `EstadoPedido.Enviado` sin seguimiento: el tipo no te deja.
//  3. Añade la transición Pagado → Cancelar con reembolso y comprueba que es una línea.
//  4. Convierte EstadoPantalla en enum y cuenta cuántas propiedades nulables te hacen falta.
