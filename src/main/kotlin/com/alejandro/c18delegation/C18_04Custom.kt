package com.alejandro.c18delegation

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// =====================================================================================
//  18.4 · Delegados propios y provideDelegate
//
//  QUÉ ES
//    Cualquier objeto que tenga `getValue` (y `setValue` si es `var`) puede usarse
//    como delegado con `by`. `provideDelegate` permite además hacer comprobaciones en
//    el momento de CREAR el delegado.
//
//  POR QUÉ IMPORTA
//    Es el mecanismo que hay detrás de `lazy`, `observable` y `by map`. Saber
//    escribirlos permite encapsular patrones que se repiten: valores con validación,
//    lectura de configuración, cachés con caducidad, contadores de acceso.
//
//  ERRORES COMUNES
//    · Escribir un delegado para algo que un setter personalizado resolvía mejor.
//    · Guardar el estado en el delegado sin darse cuenta de que se COMPARTE si el
//      delegado se reutiliza entre instancias.
//    · Olvidar que `provideDelegate` se ejecuta una vez por propiedad, al construir.
// =====================================================================================

/**
 * El contrato mínimo.
 */
fun demoTheContract() {
    section("Qué necesita un delegado")

    bullet("Para un `val`:  operator fun getValue(thisRef: R, property: KProperty<*>): T")
    bullet("Para un `var`:  además  operator fun setValue(thisRef: R, property: KProperty<*>, value: T)")
    bullet("No hace falta implementar ninguna interfaz: basta con esos métodos.")

    section("La forma cómoda: ReadOnlyProperty y ReadWriteProperty")

    bullet("`kotlin.properties.ReadOnlyProperty<R, T>`  para val")
    bullet("`kotlin.properties.ReadWriteProperty<R, T>` para var")
    bullet("Son interfaces con esos mismos métodos: ayudan a no equivocarse.")

    section("Qué recibe el delegado")

    val ejemplo = ConDelegadoInformativo()
    show("leer `titulo`", ejemplo.titulo)
    show("leer `subtitulo`", ejemplo.subtitulo)

    bullet("`property.name` da el nombre de la propiedad: muy útil para mensajes.")
    bullet("`thisRef` es el objeto que la contiene (o null si es de nivel superior).")
}

/**
 * Un delegado con validación.
 */
fun demoValidatingDelegate() {
    section("Encapsular una validación que se repite")

    val formulario = Formulario()

    formulario.nombre = "Ana"
    show("nombre", formulario.nombre)

    val fallo = try {
        formulario.nombre = ""
        "no lanzó"
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("asignar cadena vacía", fallo)
    show("el valor no cambió", formulario.nombre)

    formulario.edad = 34
    show("edad", formulario.edad)

    val fallo2 = try {
        formulario.edad = 200
        "no lanzó"
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("asignar edad 200", fallo2)

    bullet("La misma validación en tres propiedades, escrita una vez.")
    bullet("Compara con repetir el `require` en tres setters: es el mismo ahorro que")
    bullet("dan las funciones, pero aplicado a las propiedades.")
}

/**
 * Un delegado con estado propio.
 */
fun demoStatefulDelegate() {
    section("Contar accesos")

    val metricas = ServicioConMetricas()

    repeat(3) { metricas.configuracion }
    metricas.configuracion = "nueva"
    repeat(2) { metricas.configuracion }

    show("valor actual", metricas.configuracion)
    show("lecturas", metricas.lecturasDeConfiguracion())
    show("escrituras", metricas.escriturasDeConfiguracion())

    section("El aviso sobre el estado compartido")

    // Si el delegado guarda el valor y se crea UNA sola instancia compartida entre
    // objetos, todos verían el mismo valor. Aquí cada propiedad crea el suyo.
    val otro = ServicioConMetricas()
    show("otro servicio, lecturas", otro.lecturasDeConfiguracion())

    bullet("`by ContadorDeAccesos(...)` crea un delegado NUEVO por cada instancia,")
    bullet("porque la expresión se evalúa en el constructor de cada objeto.")
    bullet("Si en su lugar usaras un `object` singleton como delegado, el estado")
    bullet("sería compartido por todas las instancias. Casi nunca es lo que quieres.")
}

/**
 * Un delegado con caducidad.
 */
fun demoCachingDelegate() {
    section("Una caché que se recalcula cada N accesos")

    var vecesCalculado = 0
    val cache = ConCacheLimitada { vecesCalculado++; "valor-$vecesCalculado" }

    show("acceso 1", cache.dato)
    show("acceso 2", cache.dato)
    show("acceso 3", cache.dato)
    show("acceso 4 (caducó)", cache.dato)
    show("acceso 5", cache.dato)
    show("veces recalculado", vecesCalculado)

    bullet("Tres accesos con el mismo valor, y al cuarto se recalcula.")
    bullet("Una caché de verdad usaría tiempo en lugar de un contador, pero el")
    bullet("mecanismo es idéntico y el tiempo haría la demo no reproducible.")
}

/**
 * `provideDelegate`: comprobar al crear la propiedad.
 */
fun demoProvideDelegate() {
    section("El problema")

    bullet("Un delegado normal se entera del nombre de la propiedad al PRIMER acceso.")
    bullet("Si quieres validar algo sobre ese nombre (o registrarlo), sería tarde.")

    section("La solución")

    // `provideDelegate` se ejecuta al construir el objeto, una vez por propiedad.
    // Aquí lo usamos para exigir que el nombre de la propiedad exista en el mapa.
    val configuracionValida = mapOf("host" to "localhost", "puerto" to "8080")

    val correcta = ConfiguracionValidada(configuracionValida)
    show("correcta.host", correcta.host)
    show("correcta.puerto", correcta.puerto)

    section("Y falla AL CONSTRUIR, no al leer")

    val incompleta = mapOf("host" to "localhost")
    val error = try {
        ConfiguracionValidada(incompleta)
        "no lanzó"
    } catch (e: IllegalStateException) {
        "lanzó IllegalStateException: ${e.message}"
    }
    show("con el mapa incompleto", error)

    bullet("Compara con `by map` (demo 18.12), que falla al LEER la propiedad.")
    bullet("Fallar pronto y con un mensaje claro vale mucho en configuración.")

    section("Cómo se escribe")

    bullet("operator fun provideDelegate(thisRef: R, property: KProperty<*>): Delegado")
    bullet("Se llama UNA vez por propiedad, durante la construcción del objeto.")
    bullet("Devuelve el delegado real, que es el que tendrá getValue/setValue.")
}

/**
 * Cuándo escribir un delegado propio.
 */
fun demoWhenToWriteOne() {
    section("SÍ merece la pena cuando...")

    bullet("El mismo patrón de get/set se repite en varias propiedades o clases.")
    bullet("Hay estado asociado a la propiedad (contadores, cachés, historial).")
    bullet("Quieres que el nombre de la propiedad forme parte del comportamiento")
    bullet("(leer de un Map, de un fichero de propiedades, de variables de entorno).")

    section("NO merece la pena cuando...")

    bullet("Una sola propiedad necesita validación: un setter personalizado basta")
    bullet("y se lee mucho mejor (capítulo 7.6).")
    bullet("Sólo quieres diferir un cálculo: eso es `by lazy`, ya está escrito.")
    bullet("Sólo quieres enterarte de los cambios: eso es `Delegates.observable`.")

    section("Los que ya existen, antes de escribir el tuyo")

    bullet("lazy               → calcular una vez, al primer acceso")
    bullet("Delegates.observable → notificar después de cambiar")
    bullet("Delegates.vetoable   → rechazar antes de cambiar")
    bullet("Delegates.notNull    → lateinit para primitivos")
    bullet("by map / by mutableMap → respaldar en un Map")
}

// -- Los delegados que usan las demos -------------------------------------------------------

/** Enseña qué recibe un delegado. */
private class InformaSobreLaPropiedad : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String =
        "me llamo '${property.name}' y vivo en ${thisRef?.let { it::class.simpleName } ?: "ningún objeto"}"
}

private class ConDelegadoInformativo {
    val titulo: String by InformaSobreLaPropiedad()
    val subtitulo: String by InformaSobreLaPropiedad()
}

/** Delegado que valida antes de guardar. */
private class Validado<T>(
    private var valor: T,
    private val mensaje: String,
    private val esValido: (T) -> Boolean,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = valor

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        require(esValido(value)) { "${property.name} $mensaje (recibido: '$value')" }
        valor = value
    }
}

private class Formulario {
    var nombre: String by Validado("(vacío)", "no puede estar en blanco") { it.isNotBlank() }
    var edad: Int by Validado(0, "debe estar entre 0 y 130") { it in 0..130 }
    var email: String by Validado("", "debe contener @") { "@" in it || it.isEmpty() }
}

/** Delegado con estado: cuenta lecturas y escrituras. */
private class ContadorDeAccesos<T>(private var valor: T) : ReadWriteProperty<Any?, T> {
    var lecturas: Int = 0
        private set
    var escrituras: Int = 0
        private set

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        lecturas++
        return valor
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        escrituras++
        valor = value
    }
}

private class ServicioConMetricas {
    // Guardamos una referencia al delegado para poder consultar sus contadores.
    private val delegado = ContadorDeAccesos("inicial")

    var configuracion: String by delegado

    fun lecturasDeConfiguracion(): Int = delegado.lecturas
    fun escriturasDeConfiguracion(): Int = delegado.escrituras
}

/** Caché que caduca cada tres lecturas. */
private class CacheLimitada<T>(
    private val maximoAccesos: Int,
    private val calcular: () -> T,
) : ReadOnlyProperty<Any?, T> {

    private var valor: T? = null
    private var accesos = 0

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (valor == null || accesos >= maximoAccesos) {
            valor = calcular()
            accesos = 0
        }
        accesos++
        @Suppress("UNCHECKED_CAST")
        return valor as T
    }
}

private class ConCacheLimitada(calcular: () -> String) {
    val dato: String by CacheLimitada(maximoAccesos = 3, calcular = calcular)
}

// -- provideDelegate ---------------------------------------------------------------------

/**
 * `provideDelegate` se ejecuta al CONSTRUIR el objeto, una vez por propiedad.
 * Aquí lo aprovechamos para comprobar que la clave existe antes de que nadie lea.
 */
private class ClaveDeConfiguracion(private val origen: Map<String, String>) {

    operator fun provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): ReadOnlyProperty<Any?, String> {
        // Esta comprobación ocurre al construir, no al leer.
        check(property.name in origen) {
            "falta la clave '${property.name}' en la configuración"
        }
        val valor = origen.getValue(property.name)
        return ReadOnlyProperty { _, _ -> valor }
    }
}

private class ConfiguracionValidada(origen: Map<String, String>) {
    val host: String by ClaveDeConfiguracion(origen)
    val puerto: String by ClaveDeConfiguracion(origen)
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade una propiedad `telefono` a Formulario con su propia validación.
//  2. Haz que ContadorDeAccesos registre también el nombre de la propiedad.
//  3. Cambia CacheLimitada para que caduque por tiempo en lugar de por accesos.
//  4. Quita el `check` de provideDelegate y comprueba que el fallo se traslada a la
//     lectura, igual que con `by map`.
