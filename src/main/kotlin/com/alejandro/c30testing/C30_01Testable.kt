package com.alejandro.c30testing

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  30.1 · Código testeable
//
//  QUÉ ES
//    Escribir el código de forma que probarlo sea trivial: funciones que dependen
//    sólo de sus parámetros, y dependencias (reloj, azar, red, ficheros) que entran
//    por el constructor en vez de estar escritas a fuego.
//
//  POR QUÉ IMPORTA
//    El 90% de "esto es difícil de testear" no es un problema de tests: es el diseño
//    diciéndote que hay demasiadas cosas mezcladas. Si te cuesta escribir el test,
//    normalmente lo que hay que cambiar es el código.
//
//  ERRORES COMUNES
//    · Llamar a `System.currentTimeMillis()` o `Random()` dentro de la lógica.
//    · Mezclar cálculo y entrada/salida en la misma función.
//    · Testear a través de cinco capas cuando la lógica estaba en una función pura.
//
//  DÓNDE ESTÁN LOS TESTS
//    En `src/test/kotlin/com/alejandro/c30testing/`. Todo lo que se declara en este
//    fichero está probado allí de verdad: `./gradlew test`.
// =====================================================================================

/**
 * Funciones puras: la forma más fácil de testear algo.
 */
fun demoPureFunctions() {
    section("Qué es una función pura")

    bullet("Mismos parámetros → mismo resultado, SIEMPRE.")
    bullet("No lee ni escribe nada de fuera: ni reloj, ni azar, ni disco, ni red.")
    bullet("Testearla es escribir una tabla de entradas y salidas esperadas.")

    section("El cálculo de un pedido")

    val lineas = listOf(
        LineaPedido("teclado", cantidad = 1, precioUnitario = 45.0),
        LineaPedido("ratón", cantidad = 2, precioUnitario = 12.5),
    )

    val subtotal = calcularSubtotal(lineas)
    val conDescuento = aplicarDescuento(subtotal, porcentaje = 10)
    val total = anadirIva(conDescuento)

    show("subtotal", subtotal)
    show("con 10% de descuento", conDescuento)
    show("con IVA (21%)", total)

    bullet("Tres funciones pequeñas en lugar de una `calcularPedido` de treinta líneas:")
    bullet("cada una se prueba por separado y los fallos se localizan solos.")

    section("Los casos límite son los que importan")

    show("lista vacía", calcularSubtotal(emptyList()))
    show("descuento del 0%", aplicarDescuento(100.0, 0))
    show("descuento del 100%", aplicarDescuento(100.0, 100))

    val fuera = runCatching { aplicarDescuento(100.0, 150) }
    show("descuento del 150%", fuera.exceptionOrNull()?.message)

    bullet("Un test por cada caso límite: vacío, cero, el máximo, y lo inválido.")
    bullet("El camino feliz casi nunca es donde están los bugs.")

    section("Un validador que devuelve motivos, no un booleano")

    listOf("abc", "contraseña", "Contrasena1", "C0ntraseña!").forEach { intento ->
        show("validarContrasena(\"$intento\")", describir(validarContrasena(intento)))
    }

    bullet("Devolver la LISTA de motivos en vez de `false` hace el test más útil:")
    bullet("se comprueba qué falló, no sólo que falló.")

    section("Un parser: entrada rara garantizada")

    listOf("1h30m", "45s", "2h", "90m", "", "hola", "1h2x").forEach { texto ->
        show("parsearDuracionMs(\"$texto\")", parsearDuracionMs(texto))
    }

    bullet("Devuelve `null` en vez de lanzar: el caso 'texto inválido' es esperado,")
    bullet("no excepcional (la tabla de decisión del capítulo 19).")
}

/**
 * Inyectar las dependencias para poder controlarlas en el test.
 */
fun demoInjectDependencies() {
    section("El problema: el tiempo")

    bullet("Una sesión que caduca a los 30 minutos es imposible de testear si la")
    bullet("clase llama a `System.currentTimeMillis()` por su cuenta: tendrías que")
    bullet("esperar media hora de verdad.")

    section("La solución: una interfaz de una sola función")

    bullet("fun interface Reloj { fun ahoraMs(): Long }")
    bullet("")
    bullet("En producción se le pasa `RelojDelSistema`; en el test, un reloj falso")
    bullet("que devuelve lo que tú quieras.")

    section("En funcionamiento, con un reloj falso")

    // Un reloj que podemos mover a mano: aquí está toda la gracia.
    val reloj = RelojManipulable(inicioMs = 1_000_000)
    val sesiones = GestorDeSesiones(reloj, duracionMs = 30 * 60 * 1000L)

    val token = sesiones.abrir("ana")
    show("token", token)
    show("¿activa recién abierta?", sesiones.estaActiva(token))

    reloj.avanzarMinutos(29)
    show("a los 29 minutos", sesiones.estaActiva(token))

    reloj.avanzarMinutos(2)
    show("a los 31 minutos", sesiones.estaActiva(token))

    bullet("31 minutos simulados en microsegundos. Eso es lo que da inyectar el reloj.")

    section("Y el cierre explícito")

    val otro = sesiones.abrir("berta")
    show("¿activa?", sesiones.estaActiva(otro))
    sesiones.cerrar(otro)
    show("tras cerrar", sesiones.estaActiva(otro))
    show("token inventado", sesiones.estaActiva("no-existe"))

    section("Las cuatro dependencias que SIEMPRE hay que inyectar")

    bullet("El tiempo      → `Reloj` (o `kotlin.time.TimeSource`)")
    bullet("El azar        → `Random` (y en los tests, `Random(semilla)`)")
    bullet("El dispatcher  → `CoroutineDispatcher` (demo 30.7)")
    bullet("El exterior    → red, disco, base de datos: detrás de una interfaz")

    section("La pinta que tiene en el código")

    bullet("class Servicio(")
    bullet("    private val repositorio: Repositorio,")
    bullet("    private val reloj: Reloj = RelojDelSistema,")
    bullet("    private val azar: Random = Random.Default,")
    bullet(")")
    bullet("")
    bullet("Con valores por defecto, en producción se escribe `Servicio(repo)` y")
    bullet("en el test `Servicio(repoFalso, relojFalso, Random(42))`.")
    bullet("Coste: una línea. Beneficio: tests deterministas e instantáneos.")
}

/**
 * Qué merece la pena testear.
 */
fun demoWhatToTest() {
    section("Lo que SÍ")

    bullet("Lógica de negocio: cálculos, reglas, validaciones, transiciones de estado.")
    bullet("Parsers y formateadores: son un imán de casos raros.")
    bullet("Los casos límite: vacío, uno, muchos, el máximo, negativo, nulo.")
    bullet("Los bugs que ya han aparecido: un test por cada uno, para que no vuelva.")
    bullet("Los contratos públicos de tus módulos.")

    section("Lo que normalmente NO")

    bullet("Getters y setters generados (`data class`, propiedades sin lógica).")
    bullet("La biblioteca estándar: `listOf` ya está probada.")
    bullet("Detalles internos privados: te atan a la implementación y estorban al")
    bullet("refactorizar. Testea el COMPORTAMIENTO, no cómo está hecho.")
    bullet("Los frameworks: probar que Spring inyecta no prueba tu código.")

    section("La pregunta que lo decide")

    bullet("«Si esto se rompiera, ¿me enteraría?»")
    bullet("Si la respuesta es 'no hasta que lo vea un usuario', escribe el test.")

    section("La pirámide, en una frase")

    bullet("Muchos tests unitarios (rápidos, de una función o clase).")
    bullet("Algunos de integración (varias piezas juntas, quizá con base de datos).")
    bullet("Muy pocos de extremo a extremo (lentos, frágiles, pero insustituibles).")
    bullet("Si la pirámide se te pone del revés, la suite tardará media hora y")
    bullet("acabarás ignorándola. Ése es el verdadero fracaso de una suite de tests.")

    section("La cobertura no es el objetivo")

    bullet("Un 100% de cobertura con asertos vacíos no prueba nada.")
    bullet("Un 60% bien elegido en la lógica de negocio vale muchísimo más.")
    bullet("Usa la cobertura para ENCONTRAR huecos, no como nota del examen.")

    section("En este repositorio")

    bullet("`src/test/kotlin/com/alejandro/c30testing/` prueba lo de este capítulo.")
    bullet("`src/test/kotlin/com/alejandro/infra/` prueba el propio lanzador y")
    bullet("ejecuta TODAS las demos para comprobar que ninguna lanza (demo 30.8).")
    bullet("`src/test/kotlin/com/alejandro/c31exercises/` valida las soluciones")
    bullet("propuestas de los ejercicios.")
}

// =====================================================================================
//  EL CÓDIGO DE PRODUCCIÓN
//
//  Todo lo que hay de aquí abajo está probado en
//  `src/test/kotlin/com/alejandro/c30testing/`.
// =====================================================================================

// -- Cálculo de un pedido (funciones puras) --------------------------------------------------------

data class LineaPedido(val articulo: String, val cantidad: Int, val precioUnitario: Double)

/** Suma de `cantidad × precio`. Con lista vacía, 0.0. */
fun calcularSubtotal(lineas: List<LineaPedido>): Double =
    lineas.sumOf { it.cantidad * it.precioUnitario }

/**
 * Aplica un descuento en porcentaje.
 *
 * Un porcentaje fuera de 0..100 es un error de programación, no un dato malo del
 * usuario: por eso lanza en vez de devolver null (capítulo 19).
 */
fun aplicarDescuento(subtotal: Double, porcentaje: Int): Double {
    require(porcentaje in 0..100) { "el descuento debe estar entre 0 y 100, no $porcentaje" }
    return subtotal * (100 - porcentaje) / 100
}

/** Añade el IVA. El tipo es un parámetro para poder probar varios sin tocar el código. */
fun anadirIva(base: Double, ivaPorCiento: Int = 21): Double {
    require(ivaPorCiento >= 0) { "el IVA no puede ser negativo: $ivaPorCiento" }
    return base * (100 + ivaPorCiento) / 100
}

// -- Validación (devuelve motivos, no un booleano) -------------------------------------------------

sealed interface ResultadoValidacion {
    data object Valida : ResultadoValidacion
    data class Invalida(val motivos: List<String>) : ResultadoValidacion
}

/**
 * Comprueba una contraseña y devuelve TODOS los motivos por los que no vale.
 *
 * Devolver la lista completa (en vez de parar en el primer fallo) es mejor para
 * quien la usa y hace los tests mucho más expresivos.
 */
fun validarContrasena(valor: String): ResultadoValidacion {
    val motivos = buildList {
        if (valor.length < 8) add("debe tener al menos 8 caracteres")
        if (valor.none { it.isDigit() }) add("debe contener algún dígito")
        if (valor.none { it.isUpperCase() }) add("debe contener alguna mayúscula")
        if (valor.none { !it.isLetterOrDigit() }) add("debe contener algún símbolo")
    }
    return if (motivos.isEmpty()) ResultadoValidacion.Valida else ResultadoValidacion.Invalida(motivos)
}

// -- Un parser tolerante ----------------------------------------------------------------------------

private val PATRON_DURACION = Regex("""^(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$""")

/**
 * Convierte textos como `"1h30m"`, `"45s"` o `"2h"` en milisegundos.
 *
 * @return los milisegundos, o `null` si el texto no encaja o está vacío.
 */
fun parsearDuracionMs(texto: String): Long? {
    val limpio = texto.trim().lowercase()
    if (limpio.isEmpty()) return null

    val coincidencia = PATRON_DURACION.matchEntire(limpio) ?: return null
    val (horas, minutos, segundos) = coincidencia.destructured

    // Si el patrón encaja pero no hay ningún grupo, el texto era basura sin unidades.
    if (horas.isEmpty() && minutos.isEmpty() && segundos.isEmpty()) return null

    val total = (horas.toLongOrNull() ?: 0) * 3_600_000 +
        (minutos.toLongOrNull() ?: 0) * 60_000 +
        (segundos.toLongOrNull() ?: 0) * 1_000
    return total
}

// -- El reloj inyectable ----------------------------------------------------------------------------

/**
 * La dependencia con el tiempo, aislada en una interfaz de una sola función.
 *
 * `fun interface` permite escribir `Reloj { 1_000L }` en un test (capítulo 05).
 */
fun interface Reloj {
    fun ahoraMs(): Long
}

/** La implementación de producción. */
object RelojDelSistema : Reloj {
    override fun ahoraMs(): Long = System.currentTimeMillis()
}

/**
 * Un reloj que se mueve a mano.
 *
 * Vive en el código de producción porque esta demo lo usa, pero en un proyecto real
 * estaría en `src/test`: es una herramienta de test, no parte de la aplicación.
 */
class RelojManipulable(private var inicioMs: Long) : Reloj {
    override fun ahoraMs(): Long = inicioMs

    fun avanzarMinutos(minutos: Int) {
        inicioMs += minutos * 60_000L
    }
}

/**
 * Sesiones que caducan. La clase NO sabe qué hora es: se la dan.
 */
class GestorDeSesiones(
    private val reloj: Reloj = RelojDelSistema,
    private val duracionMs: Long = 30 * 60 * 1000L,
) {
    private val aperturas = mutableMapOf<String, Long>()

    /** El token es determinista a propósito: así el test puede comprobarlo. */
    fun abrir(usuario: String): String {
        val token = "$usuario-${reloj.ahoraMs()}"
        aperturas[token] = reloj.ahoraMs()
        return token
    }

    fun estaActiva(token: String): Boolean {
        val abierta = aperturas[token] ?: return false
        return reloj.ahoraMs() - abierta < duracionMs
    }

    fun cerrar(token: String) {
        aperturas.remove(token)
    }

    fun sesionesAbiertas(): Int = aperturas.size
}

// -- Utilidad de la demo -----------------------------------------------------------------------------

private fun describir(resultado: ResultadoValidacion): String = when (resultado) {
    is ResultadoValidacion.Valida -> "válida"
    is ResultadoValidacion.Invalida -> resultado.motivos.joinToString("; ")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade una regla a `validarContrasena` y comprueba qué test de
//     `src/test/kotlin/com/alejandro/c30testing/ValidacionTest.kt` se pone rojo.
//  2. Haz que `parsearDuracionMs("1d")` devuelva los milisegundos de un día.
//  3. Cambia `GestorDeSesiones` para que use `System.currentTimeMillis()` directamente
//     e intenta escribir el test de caducidad: ahí se ve por qué se inyecta.
//  4. Escribe un `RelojCongelado(ms)` con `fun interface`: `Reloj { 0L }` en una línea.
