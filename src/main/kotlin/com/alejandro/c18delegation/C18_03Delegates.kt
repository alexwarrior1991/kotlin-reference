package com.alejandro.c18delegation

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.properties.Delegates

// =====================================================================================
//  18.3 · observable, vetoable, notNull y `by map`
//
//  QUÉ ES
//    Los delegados que trae la biblioteca estándar en `kotlin.properties.Delegates`,
//    más el que permite leer las propiedades de un `Map`.
//
//  POR QUÉ IMPORTA
//    `observable` da notificaciones de cambio sin escribir un setter a mano ni montar
//    un patrón observador. `vetoable` permite rechazar un valor. Y `by map` convierte
//    un JSON ya parseado en un objeto tipado sin escribir el mapeo campo a campo.
//
//  ERRORES COMUNES
//    · Confundir `observable` (avisa DESPUÉS) con `vetoable` (decide ANTES).
//    · Usar `Delegates.notNull()` donde `lateinit` es más apropiado (o al revés).
//    · Olvidar que en `by map` la clave debe llamarse EXACTAMENTE como la propiedad.
// =====================================================================================

/**
 * `observable`: enterarse de los cambios.
 */
fun demoObservable() {
    section("Un callback en cada asignación")

    val cambios = mutableListOf<String>()
    val usuario = UsuarioObservable(cambios)

    usuario.nombre = "Ana"
    usuario.nombre = "Ana María"
    usuario.email = "ana@ejemplo.com"

    show("nombre actual", usuario.nombre)
    cambios.forEach { bullet(it) }

    bullet("El callback recibe la propiedad, el valor ANTERIOR y el NUEVO.")
    bullet("Se ejecuta DESPUÉS de asignar: el cambio ya está hecho.")

    section("También se dispara aunque el valor no cambie")

    cambios.clear()
    usuario.nombre = "Ana María"      // el mismo valor que ya tenía
    show("asignando el mismo valor", cambios.size)
    bullet("`observable` no compara: notifica toda asignación.")
    bullet("Si quieres filtrar, compara tú dentro del callback.")

    section("Para qué se usa")

    bullet("Marcar un modelo como 'sucio' cuando cambia algo.")
    bullet("Invalidar una caché derivada de esa propiedad.")
    bullet("Registrar una traza de auditoría de los cambios.")
    bullet("Notificar a la interfaz de usuario (en Android, antes de Compose).")
}

/**
 * `vetoable`: poder decir que no.
 */
fun demoVetoable() {
    section("El callback decide si el valor se acepta")

    val producto = ProductoConValidacion()

    show("precio inicial", producto.precioCentimos)

    producto.precioCentimos = 1_500
    show("tras asignar 1500", producto.precioCentimos)

    producto.precioCentimos = -100
    show("tras asignar -100 (rechazado)", producto.precioCentimos)

    producto.precioCentimos = 2_000
    show("tras asignar 2000", producto.precioCentimos)

    show("intentos rechazados", producto.rechazos)

    bullet("El callback devuelve `true` para aceptar y `false` para rechazar.")
    bullet("Se ejecuta ANTES de asignar: si devuelve false, el valor no llega a entrar.")

    section("observable frente a vetoable")

    bullet("observable: (propiedad, viejo, nuevo) -> Unit      · avisa después")
    bullet("vetoable:   (propiedad, viejo, nuevo) -> Boolean   · decide antes")

    section("vetoable frente a un setter con require")

    bullet("`require` LANZA: el programa se entera con una excepción.")
    bullet("`vetoable` IGNORA en silencio: el valor simplemente no cambia.")
    bullet("Elige según lo que deba pasar: ¿es un error del programador (require)")
    bullet("o una entrada del usuario que simplemente se descarta (vetoable)?")

    val conRequire = try {
        ProductoConRequire().apply { precioCentimos = -100 }
        "no lanzó"
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("la versión con require", conRequire)
}

/**
 * `notNull`: como lateinit, pero para tipos primitivos.
 */
fun demoNotNull() {
    section("El hueco que llena")

    bullet("`lateinit` NO funciona con Int, Long, Double, Boolean...")
    bullet("`Delegates.notNull<Int>()` sí: es la alternativa para esos tipos.")

    val configuracion = ConfiguracionDiferida()

    val antes = try {
        configuracion.puerto.toString()
    } catch (e: IllegalStateException) {
        "lanzó IllegalStateException: ${e.message}"
    }
    show("leer antes de asignar", antes)

    configuracion.puerto = 8080
    configuracion.modoDepuracion = true
    show("tras asignar, puerto", configuracion.puerto)
    show("tras asignar, modoDepuracion", configuracion.modoDepuracion)

    section("Cómo elegir entre las tres opciones")

    bullet("Tipo de referencia (String, List, tu clase) → `lateinit var`")
    bullet("Tipo primitivo (Int, Boolean, Double)       → `by Delegates.notNull()`")
    bullet("Se puede calcular solo                       → `by lazy`")
    bullet("Hay un valor por defecto razonable           → asígnalo y olvídate")
}

/**
 * `by map`: propiedades respaldadas por un Map.
 */
fun demoByMap() {
    section("De un Map a un objeto tipado")

    // El caso típico: has parseado un JSON o leído un fichero de configuración y
    // tienes un Map<String, Any?>. En vez de escribir el mapeo campo a campo...
    val datos = mapOf(
        "nombre" to "Ana",
        "edad" to 34,
        "email" to "ana@ejemplo.com",
    )

    val usuario = UsuarioDesdeMap(datos)
    show("usuario.nombre", usuario.nombre)
    show("usuario.edad", usuario.edad)
    show("usuario.email", usuario.email)

    bullet("Las propiedades leen del Map usando SU PROPIO NOMBRE como clave.")

    section("Si falta una clave, falla al LEER esa propiedad")

    val incompleto = UsuarioDesdeMap(mapOf("nombre" to "Luis"))
    show("nombre (sí está)", incompleto.nombre)

    val falta = try {
        incompleto.edad.toString()
    } catch (e: NoSuchElementException) {
        "lanzó NoSuchElementException"
    }
    show("edad (no está)", falta)

    bullet("No falla al construir: falla al leer la propiedad que falta.")
    bullet("Es tarde y lejos de la causa. Por eso no conviene para datos de fuera.")

    section("Con un MutableMap, las propiedades también se pueden escribir")

    val mutable = mutableMapOf<String, Any?>("contador" to 0)
    val estado = EstadoMutable(mutable)
    show("contador inicial", estado.contador)

    estado.contador = 42
    show("tras estado.contador = 42", estado.contador)
    show("y el mapa de debajo", mutable)

    bullet("La escritura va directa al Map: es una vista tipada sobre él.")

    section("Cuándo usarlo de verdad")

    bullet("SÍ: prototipos, scripts, configuración interna donde controlas los datos.")
    bullet("NO: datos de una API o de un usuario. Ahí quieres una data class y un")
    bullet("    parseo que valide y falle PRONTO, con un mensaje claro.")
    bullet("Para eso está kotlinx.serialization, que comprueba en compilación.")
}

// -- Las clases que usan las demos --------------------------------------------------------

private class UsuarioObservable(private val destino: MutableList<String>) {

    var nombre: String by Delegates.observable("(sin nombre)") { propiedad, viejo, nuevo ->
        destino.add("${propiedad.name}: '$viejo' → '$nuevo'")
    }

    var email: String by Delegates.observable("") { propiedad, viejo, nuevo ->
        destino.add("${propiedad.name}: '$viejo' → '$nuevo'")
    }
}

private class ProductoConValidacion {

    var rechazos: Int = 0
        private set

    /** Devolver false rechaza la asignación. */
    var precioCentimos: Int by Delegates.vetoable(0) { _, _, nuevo ->
        val valido = nuevo >= 0
        if (!valido) rechazos++
        valido
    }
}

private class ProductoConRequire {
    var precioCentimos: Int = 0
        set(valor) {
            require(valor >= 0) { "el precio no puede ser negativo: $valor" }
            field = valor
        }
}

private class ConfiguracionDiferida {
    /** `lateinit` no admite Int; `notNull` sí. */
    var puerto: Int by Delegates.notNull()
    var modoDepuracion: Boolean by Delegates.notNull()
}

/** Las propiedades leen del Map por su propio nombre. */
private class UsuarioDesdeMap(datos: Map<String, Any?>) {
    val nombre: String by datos
    val edad: Int by datos
    val email: String by datos
}

/** Con MutableMap, también se puede escribir. */
private class EstadoMutable(datos: MutableMap<String, Any?>) {
    var contador: Int by datos
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Haz que el observable de `nombre` ignore las asignaciones con el mismo valor.
//  2. Cambia `vetoable` por `observable` en ProductoConValidacion y mira qué se rompe.
//  3. Renombra `edad` a `anios` en UsuarioDesdeMap y observa que deja de encontrarla.
//  4. Escribe una clase con `lateinit var nombre: String` y `var edad: Int by notNull()`.
