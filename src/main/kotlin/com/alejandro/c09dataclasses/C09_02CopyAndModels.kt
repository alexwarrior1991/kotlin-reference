package com.alejandro.c09dataclasses

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  9.2 · copy(), data object y modelos reales
//
//  QUÉ ES
//    `copy()` crea un objeto nuevo cambiando sólo lo que indiques. `data object` es la
//    versión sin datos. Y aquí se ve cómo se usan las data class en un modelo real.
//
//  POR QUÉ IMPORTA
//    `copy()` es lo que hace práctica la inmutabilidad: en lugar de mutar un objeto,
//    produces otro con el cambio aplicado. Es la base del estado inmutable que usan
//    Compose, Redux y cualquier arquitectura con estado unidireccional.
//
//  ERRORES COMUNES
//    · Creer que `copy()` es una copia profunda. No lo es: las referencias se comparten.
//    · Usar `copy()` dentro de un bucle grande sin pensar en el coste.
//    · Modelar con data class algo que tiene identidad (una entidad de base de datos).
// =====================================================================================

/**
 * `copy()`: la alternativa a mutar.
 */
fun demoCopy() {
    section("Cambiar una propiedad sin tocar el original")

    val original = Usuario("Ana", "ana@ejemplo.com", activo = true)
    val conNuevoEmail = original.copy(email = "ana@nueva.com")

    show("original", original)
    show("copia con otro email", conNuevoEmail)
    show("el original NO cambió", original.email)

    section("Se pueden cambiar varias a la vez, o ninguna")

    show("copy() sin argumentos", original.copy())
    show("copy() sin argumentos == original", original.copy() == original)
    show("copy() sin argumentos === original", original.copy() === original)
    show("copy(nombre, activo)", original.copy(nombre = "Ana María", activo = false))

    bullet("`copy()` siempre crea un objeto NUEVO, aunque no cambies nada.")
    bullet("Usa argumentos nombrados: `copy(email = ...)`, nunca posicionales.")

    section("El patrón de estado inmutable")

    // En lugar de `estado.contador++`, se produce un estado nuevo. Así siempre se
    // puede comparar el anterior con el siguiente, guardar el historial, deshacer...
    var estado = EstadoPantalla(cargando = false, elementos = emptyList(), error = null)
    show("estado inicial", estado)

    estado = estado.copy(cargando = true)
    show("empieza la carga", estado)

    estado = estado.copy(cargando = false, elementos = listOf("a", "b"))
    show("datos recibidos", estado)

    estado = estado.copy(error = "se cayó la red")
    show("con error", estado)

    bullet("Cada paso es un objeto independiente: no hay estado a medio construir.")
}

/**
 * `copy()` es SUPERFICIAL. La trampa más importante del capítulo.
 */
fun demoShallowCopy() {
    section("Con propiedades inmutables, no hay problema")

    val a = Usuario("Ana", "ana@ejemplo.com", activo = true)
    val b = a.copy(nombre = "Berta")
    show("a.nombre", a.nombre)
    show("b.nombre", b.nombre)

    section("Con una colección MUTABLE dentro, sí lo hay")

    val original = CarritoMutable("c-1", mutableListOf("teclado"))
    val copia = original.copy(referencia = "c-2")

    // `copy` copió la REFERENCIA a la lista, no la lista. Las dos apuntan a la misma.
    copia.productos.add("ratón")

    show("original.productos", original.productos)
    show("copia.productos", copia.productos)
    show("¿comparten la misma lista?", original.productos === copia.productos)

    bullet("Modificar la copia modificó el original. Ése es el peligro.")

    section("La solución: que el contenido sea inmutable")

    val seguro = CarritoInmutable("c-1", listOf("teclado"))
    val copiaSegura = seguro.copy(productos = seguro.productos + "ratón")

    show("original", seguro.productos)
    show("copia", copiaSegura.productos)
    show("¿comparten lista?", seguro.productos === copiaSegura.productos)

    bullet("Con `List` (de sólo lectura) no se puede modificar por accidente.")
    bullet("Regla: dentro de una data class, todo `val` y todo inmutable.")

    section("Si necesitas copia profunda, hazla explícita")

    val profunda = original.copy(productos = original.productos.toMutableList())
    profunda.productos.add("monitor")
    show("original tras copia profunda", original.productos)
    show("copia profunda", profunda.productos)
}

/**
 * `data object`: la data class sin datos.
 */
fun demoDataObject() {
    section("El problema que resuelve")

    // Un `object` normal tiene un toString() poco útil.
    show("object normal", ObjetoNormal.toString().substringBefore('@') + "@...")
    show("data object", ObjetoDeDatos.toString())

    bullet("`data object` genera un toString() con el nombre, y un equals/hashCode")
    bullet("coherentes con que sea un singleton.")

    section("Para qué se usa de verdad")

    // Su sitio natural es dentro de una jerarquía sealed, para los casos sin datos.
    // Capítulo 11.
    val estados: List<EstadoCarga> = listOf(
        EstadoCarga.Inactivo,
        EstadoCarga.Cargando,
        EstadoCarga.Exito(listOf("a", "b")),
        EstadoCarga.Error("404"),
    )
    estados.forEach { show(it::class.simpleName ?: "?", it) }

    bullet("Sin `data`, `Inactivo` se imprimiría como 'Inactivo@3f2a1b'.")
    bullet("Disponible desde Kotlin 1.9. Antes se escribía `object` y un toString a mano.")
}

/**
 * Cuándo usar data class y cuándo no.
 */
fun demoWhenToUseDataClass() {
    section("Sí: objetos de valor")

    bullet("DTO de una API, respuesta de red, fila de un CSV.")
    bullet("Coordenadas, dinero, rangos de fechas, configuración.")
    bullet("Estados de pantalla, eventos, mensajes.")
    bullet("La prueba: dos objetos con los mismos datos, ¿son el mismo? → sí → data.")

    section("No: entidades con identidad")

    // Dos usuarios distintos pueden tener el mismo nombre y email y aun así ser
    // registros diferentes. Lo que los distingue es su id.
    bullet("Una fila de base de datos con clave primaria.")
    bullet("Algo con ciclo de vida propio (una conexión, un hilo, una sesión).")
    bullet("La prueba: ¿dos objetos con los mismos datos son registros distintos?")
    bullet("→ sí → NO es una data class; compara por id.")

    show("dos usuarios con mismos datos pero distinto id", UsuarioEntidad(1, "Ana") == UsuarioEntidad(2, "Ana"))

    section("No: clases con lógica de negocio pesada")

    bullet("`data` anuncia 'esto son datos'. Si la clase tiene 20 métodos, miente.")

    section("Un modelo realista")

    val pedido = Pedido(
        referencia = "P-2026-001",
        cliente = Cliente("Ana", "ana@ejemplo.com"),
        lineas = listOf(
            Linea("teclado", 1, 4999),
            Linea("ratón", 2, 1550),
        ),
    )

    show("pedido.total()", "%.2f €".format(pedido.total() / 100.0))
    show("pedido.resumen()", pedido.resumen())
    show("con descuento (copy)", "%.2f €".format(pedido.conDescuento(10).total() / 100.0))

    bullet("Las data class llevan datos; los cálculos derivados van en funciones.")
    bullet("`conDescuento` devuelve un pedido NUEVO: nada se muta.")
}

// -- Las clases que usan las demos ----------------------------------------------------

private data class Usuario(val nombre: String, val email: String, val activo: Boolean)

private data class EstadoPantalla(
    val cargando: Boolean,
    val elementos: List<String>,
    val error: String?,
)

/** Con una lista MUTABLE dentro: copy() la comparte. */
private data class CarritoMutable(val referencia: String, val productos: MutableList<String>)

/** Con una lista de sólo lectura: seguro. */
private data class CarritoInmutable(val referencia: String, val productos: List<String>)

private object ObjetoNormal
private data object ObjetoDeDatos

/** Jerarquía sealed con `data object` para los casos sin datos. Capítulo 11. */
private sealed interface EstadoCarga {
    data object Inactivo : EstadoCarga
    data object Cargando : EstadoCarga
    data class Exito(val elementos: List<String>) : EstadoCarga
    data class Error(val codigo: String) : EstadoCarga
}

/** Una entidad: la identidad la da el id, no los datos. */
private class UsuarioEntidad(val id: Long, val nombre: String) {
    override fun equals(other: Any?): Boolean = other is UsuarioEntidad && other.id == id
    override fun hashCode(): Int = id.hashCode()
}

// -- Modelo realista ------------------------------------------------------------------

private data class Cliente(val nombre: String, val email: String)

private data class Linea(val producto: String, val unidades: Int, val precioUnitarioCentimos: Int) {
    /** Cálculo derivado: función, no propiedad almacenada. */
    fun subtotal(): Int = unidades * precioUnitarioCentimos
}

private data class Pedido(
    val referencia: String,
    val cliente: Cliente,
    val lineas: List<Linea>,
) {
    fun total(): Int = lineas.sumOf { it.subtotal() }

    fun resumen(): String = "$referencia · ${cliente.nombre} · ${lineas.size} líneas"

    /** Devuelve un pedido NUEVO con el descuento aplicado. */
    fun conDescuento(porcentaje: Int): Pedido = copy(
        lineas = lineas.map { linea ->
            linea.copy(precioUnitarioCentimos = linea.precioUnitarioCentimos * (100 - porcentaje) / 100)
        },
    )
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. En demoShallowCopy, cambia MutableList por List y comprueba que ya no compila
//     la línea que añade un producto: el compilador te protege.
//  2. Añade un campo `descuento: Int = 0` a Pedido y usa `copy` para aplicarlo.
//  3. Convierte ObjetoDeDatos en `object` normal y compara los dos toString.
//  4. Escribe `EstadoCarga.Exito(emptyList()) == EstadoCarga.Exito(emptyList())`.
