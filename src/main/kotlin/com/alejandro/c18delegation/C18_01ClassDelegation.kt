package com.alejandro.c18delegation

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  18.1 · Delegación de clases con `by`
//
//  QUÉ ES
//    `class A(private val b: B) : Interfaz by b` implementa TODA la interfaz
//    delegando en `b`. El compilador escribe los métodos de reenvío por ti.
//
//  POR QUÉ IMPORTA
//    Es lo que hace que la composición sea más barata de escribir que la herencia
//    (capítulo 8.9). Sin `by`, decorar un `List` significa escribir a mano 25 métodos
//    que sólo hacen `return interno.loQueSea()`.
//
//  ERRORES COMUNES
//    · Esperar que la clase delegada llame a TUS overrides (no lo hace: ver más abajo).
//    · Delegar en un objeto que después cambia (la delegación se fija al construir).
//    · Usar `by` para heredar comportamiento cuando en realidad querías una subclase.
// =====================================================================================

/**
 * Lo básico.
 */
fun demoBasicDelegation() {
    section("Sin `by`: escribir todos los métodos a mano")

    bullet("class MiLista(val interna: List<String>) : List<String> {")
    bullet("    override val size get() = interna.size")
    bullet("    override fun get(index: Int) = interna.get(index)")
    bullet("    override fun contains(element: String) = interna.contains(element)")
    bullet("    ... y otros 20 métodos idénticos")
    bullet("}")

    section("Con `by`: una línea")

    val registro = ListaConRegistro(listOf("a", "b", "c"))

    show("size        (generado)", registro.size)
    show("get(1)      (generado)", registro[1])
    show("contains    (generado)", registro.contains("b"))
    show("isEmpty     (generado)", registro.isEmpty())
    show("joinToString (extensión sobre List)", registro.joinToString())

    show("accesos registrados (nuestro)", registro.accesos)

    bullet("Hemos escrito UN método; el compilador ha generado los demás.")
    bullet("Y la clase es una `List<String>` de verdad: sirve donde se pida una.")

    section("Sirve donde se espere la interfaz")

    show("pasada a una función que pide List<String>", contar(registro))
}

/**
 * Sobrescribir parte de la delegación.
 */
fun demoOverridingDelegated() {
    section("Cambiar sólo lo que interese")

    val mayusculas = ConjuntoEnMayusculas()
    mayusculas.add("kotlin")
    mayusculas.add("Java")
    mayusculas.add("KOTLIN")      // duplicado tras normalizar

    show("contenido", mayusculas.toList().sorted())
    show("size", mayusculas.size)
    show("contains(\"kotlin\")", mayusculas.contains("KOTLIN"))

    bullet("Sólo se ha sobrescrito `add`: todo lo demás se delega.")

    section("LA TRAMPA: la delegada no ve tus overrides")

    // `addAll` NO está sobrescrito, así que se delega al conjunto interno. Y ese
    // conjunto llama a SU PROPIO `add`, no al nuestro. Resultado: los elementos
    // añadidos con addAll NO pasan por nuestra normalización.
    val conAddAll = ConjuntoEnMayusculas()
    conAddAll.addAll(listOf("kotlin", "java"))
    show("tras addAll", conAddAll.toList().sorted())

    bullet("¡No están en mayúsculas! `addAll` fue directo al conjunto interno.")
    bullet("Es el MISMO problema del contador roto del capítulo 8.8, pero al revés:")
    bullet("allí la herencia veía de más; aquí la delegación ve de menos.")

    section("La solución")

    val completo = ConjuntoCompleto()
    completo.addAll(listOf("kotlin", "java"))
    show("con addAll sobrescrito también", completo.toList().sorted())

    bullet("Si sobrescribes un método, revisa qué OTROS métodos de la interfaz")
    bullet("deberían pasar por él, y sobrescríbelos también.")
    bullet("La delegación no es magia: es reenvío directo al objeto interno.")
}

/**
 * Delegar en varias interfaces.
 */
fun demoMultipleDelegation() {
    section("Componer comportamientos de varias fuentes")

    val servicio = ServicioCompuesto(
        registrador = RegistradorEnMemoria(),
        cache = CacheSimple(),
    )

    servicio.registrar("arrancando")
    servicio.guardar("clave", "valor")

    show("leer de la caché", servicio.leer("clave"))
    show("leer algo que no está", servicio.leer("otra"))
    show("mensajes registrados", servicio.mensajes())

    bullet("Una clase, dos interfaces, cero métodos de reenvío escritos a mano.")
    bullet("Con herencia esto sería imposible: sólo se hereda de UNA clase.")

    section("Cambiar la implementación en el constructor")

    val conOtroRegistrador = ServicioCompuesto(
        registrador = RegistradorQueIgnora(),
        cache = CacheSimple(),
    )
    conOtroRegistrador.registrar("esto se pierde")
    show("con un registrador que ignora", conOtroRegistrador.mensajes())

    bullet("Es inyección de dependencias sin framework: se pasa por constructor.")
    bullet("Para los tests, se inyecta un doble y listo.")
}

/**
 * Cuándo usar `by` y cuándo no.
 */
fun demoWhenToDelegate() {
    section("SÍ: decorar")

    bullet("Añadir trazas, caché, métricas o validación a algo que ya existe,")
    bullet("manteniendo su interfaz intacta.")

    section("SÍ: adaptar")

    bullet("Exponer un objeto con la interfaz que tu código necesita,")
    bullet("ocultando la que de verdad tiene.")

    section("SÍ: componer varias capacidades")

    bullet("Una clase que es Registrable Y Cacheable Y Serializable, cada una")
    bullet("implementada por un colaborador distinto.")

    section("NO: cuando en realidad quieres una subclase")

    bullet("Si la relación es 'es un' de verdad y la clase base está diseñada para")
    bullet("extenderse, la herencia es más directa.")

    section("NO: cuando sólo usas dos métodos de la interfaz")

    bullet("Si tu clase implementa List<T> pero sólo se usan `size` y `get`,")
    bullet("plantéate exponer esos dos y no toda la interfaz.")

    section("El detalle de implementación que conviene saber")

    bullet("El objeto delegado se guarda en un campo al CONSTRUIR.")
    bullet("Si la propiedad del constructor se reasignara (no se puede con `val`),")
    bullet("la delegación seguiría apuntando al objeto original.")
    bullet("Por eso el delegado casi siempre es un `val` del constructor primario.")
}

// -- Las clases que usan las demos --------------------------------------------------------

/**
 * Delega TODA la interfaz List<String> en `interna`, y sólo añade un contador.
 */
private class ListaConRegistro(
    private val interna: List<String>,
) : List<String> by interna {

    var accesos: Int = 0
        private set

    /** El único método que escribimos: el resto lo genera el compilador. */
    override fun get(index: Int): String {
        accesos++
        return interna[index]
    }
}

private fun contar(lista: List<String>): Int = lista.size

/** Normaliza a mayúsculas al añadir... pero sólo en `add`. */
private class ConjuntoEnMayusculas(
    private val interno: MutableSet<String> = mutableSetOf(),
) : MutableSet<String> by interno {

    override fun add(element: String): Boolean = interno.add(element.uppercase())
}

/** La versión correcta: también sobrescribe `addAll`. */
private class ConjuntoCompleto(
    private val interno: MutableSet<String> = mutableSetOf(),
) : MutableSet<String> by interno {

    override fun add(element: String): Boolean = interno.add(element.uppercase())

    override fun addAll(elements: Collection<String>): Boolean =
        elements.map { add(it) }.any { it }
}

// -- Delegación múltiple --------------------------------------------------------------------

private interface Registrador {
    fun registrar(mensaje: String)
    fun mensajes(): List<String>
}

private interface Cache {
    fun guardar(clave: String, valor: String)
    fun leer(clave: String): String?
}

private class RegistradorEnMemoria : Registrador {
    private val interno = mutableListOf<String>()
    override fun registrar(mensaje: String) {
        interno.add(mensaje)
    }

    override fun mensajes(): List<String> = interno.toList()
}

private class RegistradorQueIgnora : Registrador {
    override fun registrar(mensaje: String) = Unit
    override fun mensajes(): List<String> = emptyList()
}

private class CacheSimple : Cache {
    private val interno = mutableMapOf<String, String>()
    override fun guardar(clave: String, valor: String) {
        interno[clave] = valor
    }

    override fun leer(clave: String): String? = interno[clave]
}

/** Delega en DOS colaboradores a la vez. */
private class ServicioCompuesto(
    registrador: Registrador,
    cache: Cache,
) : Registrador by registrador, Cache by cache

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `remove` a ConjuntoEnMayusculas y comprueba si `removeAll` pasa por él.
//  2. Escribe una ListaSoloLectura que delegue en una List y lance al intentar modificar.
//  3. Cambia RegistradorEnMemoria por otro que añada una marca de tiempo.
//  4. Intenta delegar dos interfaces que declaren el mismo método y resuelve el conflicto.
