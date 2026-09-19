package com.alejandro.c08inheritance

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  8.3 · Composición frente a herencia
//
//  QUÉ ES
//    Dos formas de reutilizar código: heredar de una clase (ser un X) o guardar una
//    instancia de otra clase y delegar en ella (tener un X).
//
//  POR QUÉ IMPORTA
//    La herencia acopla tu clase a los DETALLES INTERNOS de la clase padre, no sólo a
//    su API. Si el padre cambia cómo implementa un método, tu subclase se rompe sin
//    que nadie haya tocado tu código. Este fichero lo demuestra con el ejemplo clásico.
//
//  ERRORES COMUNES
//    · Heredar sólo para reutilizar tres métodos.
//    · Extender clases de una librería que no fueron diseñadas para extenderse.
//    · Creer que la delegación es más verbosa: en Kotlin, `by` la hace más corta.
// =====================================================================================

/**
 * El ejemplo clásico: un conjunto que cuenta intentos de inserción.
 */
fun demoFragileBaseClass() {
    section("Por herencia: parece correcto")

    // Sobrescribimos `add` y `addAll` para ir contando. Se lee perfectamente.
    val porHerencia = ContadorPorHerencia<String>()
    porHerencia.add("uno")
    porHerencia.addAll(listOf("dos", "tres", "cuatro"))

    show("elementos insertados", porHerencia.size)
    show("intentos contados (¡esperábamos 4!)", porHerencia.intentos)

    section("Qué ha pasado")

    bullet("`HashSet.addAll` está implementado LLAMANDO a `add` para cada elemento.")
    bullet("Nuestro `addAll` suma 3, y luego el `add` heredado suma otros 3.")
    bullet("El error no está en nuestro código: está en una decisión interna del padre.")
    bullet("Si mañana HashSet cambia esa implementación, el resultado cambia otra vez.")

    section("Por composición: correcto y estable")

    val porComposicion = ContadorPorComposicion<String>()
    porComposicion.add("uno")
    porComposicion.addAll(listOf("dos", "tres", "cuatro"))

    show("elementos insertados", porComposicion.size)
    show("intentos contados", porComposicion.intentos)

    bullet("Aquí llamamos al conjunto interno por su API pública, no por su interior.")
    bullet("Cómo implemente `addAll` por dentro deja de importarnos.")
}

/**
 * La delegación de clase de Kotlin: `by`.
 */
fun demoDelegation() {
    section("El problema de la composición en Java")

    bullet("Para 'tener un Set' hay que implementar los ~15 métodos de Set...")
    bullet("...y escribir en cada uno `return interno.loQueSea()`. Puro ruido.")
    bullet("Por eso mucha gente hereda: es más corto. Kotlin quita esa excusa.")

    section("`by` genera todos esos métodos")

    // `class X(private val y: Set<T>) : Set<T> by y` implementa TODA la interfaz
    // delegando en `y`. Sólo escribes los métodos que quieras cambiar.
    val contador = ContadorPorComposicion<Int>()
    contador.addAll(listOf(1, 2, 3))
    contador.add(4)

    show("size          (delegado)", contador.size)
    show("contains(2)   (delegado)", contador.contains(2))
    show("isEmpty()     (delegado)", contador.isEmpty())
    show("intentos      (nuestro)", contador.intentos)

    bullet("Hemos escrito 2 métodos; el compilador ha generado el resto.")
    bullet("La delegación de propiedades y de clases se ve a fondo en el capítulo 18.")
}

/**
 * Cuándo sí conviene heredar.
 */
fun demoWhenToInherit() {
    section("Herencia: sólo si es verdad que 'es un'")

    bullet("¿CocheElectrico ES UN Coche? Sí → herencia tiene sentido.")
    bullet("¿ContadorDeInserciones ES UN HashSet? No: TIENE UN HashSet.")
    bullet("La prueba: ¿puedes sustituir el hijo por el padre en cualquier sitio")
    bullet("sin que nada se rompa? (Principio de sustitución de Liskov.)")

    section("Las tres condiciones para heredar con tranquilidad")

    bullet("1. La relación es realmente 'es un', no 'tiene un'.")
    bullet("2. La clase padre se diseñó para ser extendida (por eso el `open` de Kotlin).")
    bullet("3. Está documentado qué métodos llaman a cuáles (el 'auto-uso').")

    section("Composición: el resto de los casos")

    bullet("Reutilizar código → composición.")
    bullet("Añadir comportamiento a algo existente → composición (decorador).")
    bullet("Poder cambiar la implementación en tiempo de ejecución → composición.")
    bullet("Testear con un doble → composición (se inyecta el colaborador).")

    section("Un ejemplo de decorador por composición")

    val basico = NotificadorEmail()
    val conRegistro = NotificadorConRegistro(basico)
    val conReintento = NotificadorConReintento(conRegistro)

    show("notificador básico", basico.enviar("hola"))
    show("envuelto en registro", conRegistro.enviar("hola"))
    show("y además con reintento", conReintento.enviar("hola"))

    bullet("Cada capa hace una cosa y se pueden combinar en cualquier orden.")
    bullet("Con herencia harían falta clases para cada combinación posible.")
}

// -- El ejemplo del contador ----------------------------------------------------------

/**
 * Versión por HERENCIA. Cuenta mal, y no por culpa nuestra.
 */
private class ContadorPorHerencia<T> : HashSet<T>() {

    var intentos: Int = 0
        private set

    override fun add(element: T): Boolean {
        intentos++
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        intentos += elements.size
        // `super.addAll` llama internamente a `add`, que vuelve a incrementar.
        return super.addAll(elements)
    }
}

/**
 * Versión por COMPOSICIÓN, con delegación `by`.
 *
 * `MutableSet<T> by interno` implementa toda la interfaz delegando en `interno`.
 * Sólo sobrescribimos los dos métodos que nos interesan.
 */
private class ContadorPorComposicion<T>(
    private val interno: MutableSet<T> = mutableSetOf(),
) : MutableSet<T> by interno {

    var intentos: Int = 0
        private set

    override fun add(element: T): Boolean {
        intentos++
        return interno.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        intentos += elements.size
        return interno.addAll(elements)
    }
}

// -- Decoradores por composición ------------------------------------------------------

private interface Notificador {
    fun enviar(mensaje: String): String
}

private class NotificadorEmail : Notificador {
    override fun enviar(mensaje: String): String = "email('$mensaje')"
}

/** Envuelve a otro notificador y le añade una traza. */
private class NotificadorConRegistro(private val interno: Notificador) : Notificador {
    override fun enviar(mensaje: String): String = "registro[${interno.enviar(mensaje)}]"
}

/** Envuelve a otro notificador y le añade reintento. */
private class NotificadorConReintento(private val interno: Notificador) : Notificador {
    override fun enviar(mensaje: String): String = "reintento(${interno.enviar(mensaje)})"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `override fun addAll` de ContadorPorHerencia y vuelve a ejecutar:
//     el contador pasa a ser correcto... por accidente.
//  2. Cambia el orden de los decoradores y observa cómo cambia el resultado.
//  3. Quita `by interno` de ContadorPorComposicion y cuenta cuántos métodos tendrías
//     que escribir a mano.
//  4. Añade un NotificadorConFiltro que no envíe si el mensaje está vacío.
