package com.alejandro.c07classes

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  7.4 · companion object, object declaration y object expression
//
//  QUÉ ES
//    Kotlin no tiene la palabra `static`. En su lugar hay tres construcciones:
//      · `companion object` — lo "estático" de una clase concreta.
//      · `object X { }`     — un singleton con nombre.
//      · `object : T { }`   — un objeto anónimo, creado sobre la marcha.
//
//  POR QUÉ IMPORTA
//    Al ser objetos de verdad (y no miembros estáticos), pueden implementar
//    interfaces, heredar, recibirse como parámetro y tener extensiones. Un `static`
//    de Java no puede hacer nada de eso.
//
//  ERRORES COMUNES
//    · Usar un `object` como cajón desastre de funciones sueltas: para eso están las
//      funciones de nivel superior.
//    · Guardar estado mutable en un `object` (es global y compartido entre hilos).
//    · Olvidar `@JvmStatic` cuando Java tiene que llamar al companion.
// =====================================================================================

/**
 * `companion object`: lo estático de una clase.
 */
fun demoCompanionObject() {
    section("Constantes y fábricas")

    show("Usuario.EDAD_MINIMA", Usuario.EDAD_MINIMA)
    show("Usuario.crear(\"Ana\", 34)", Usuario.crear("Ana", 34))
    show("Usuario.crear(\"Bebé\", 1)", Usuario.crear("Bebé", 1))
    show("Usuario.anonimo()", Usuario.anonimo())

    bullet("Se llaman como si fueran estáticos: `Usuario.crear(...)`.")
    bullet("Pero por debajo hay un OBJETO real dentro de la clase.")

    section("Por qué una fábrica y no un constructor")

    bullet("Puede tener nombre: `desdeJson`, `vacio`, `porDefecto`.")
    bullet("Puede devolver null o un tipo distinto (una subclase, una caché).")
    bullet("Puede no crear nada: devolver una instancia ya existente.")
    bullet("Un constructor no puede hacer ninguna de las tres cosas.")

    section("El companion es un objeto de verdad")

    // Se puede referenciar, pasar como parámetro e implementar interfaces.
    show("Usuario.Companion::class.simpleName", Usuario.Companion::class.simpleName)
    show("pasarlo como parámetro: describir(Usuario)", describir(Usuario))
    bullet("`describir` espera un Describible y le pasamos la CLASE: funciona porque")
    bullet("el nombre `Usuario` en posición de valor significa 'su companion object'.")

    section("Con nombre propio")

    show("Configuracion.Predeterminada.host", Configuracion.Predeterminada.host)
    bullet("Un companion con nombre se usa igual: `Configuracion.host` también vale.")

    section("Para Java: @JvmStatic")

    bullet("Sin @JvmStatic, Java escribe `Usuario.Companion.crear(...)`.")
    bullet("Con @JvmStatic, Java escribe `Usuario.crear(...)`. Capítulo 27.")
}

/**
 * `object` declaration: un singleton.
 */
fun demoObjectDeclaration() {
    section("Una única instancia, creada al primer uso")

    show("Registro.contar()", Registro.contar())
    Registro.anotar("primer evento")
    Registro.anotar("segundo evento")
    show("tras dos anotaciones", Registro.contar())
    show("Registro.ultimo()", Registro.ultimo())

    // Siempre es el MISMO objeto, desde cualquier parte del programa.
    show("Registro === Registro", Registro === Registro)

    section("Un object puede implementar interfaces y heredar")

    // Esto es lo que lo diferencia de una clase con métodos estáticos.
    show("comparar con OrdenPorLongitud", listOf("kotlin", "es", "conciso").sortedWith(OrdenPorLongitud))

    section("Cuándo usar object y cuándo no")

    bullet("Sí: una estrategia sin estado (un Comparator, un serializador).")
    bullet("Sí: una constante compleja compartida.")
    bullet("No: como cajón de funciones sueltas → usa funciones de nivel superior.")
    bullet("Cuidado: un `object` con estado mutable es una variable global.")

    section("La inicialización es perezosa y segura entre hilos")

    bullet("El objeto se crea la primera vez que se toca, no al arrancar.")
    bullet("La JVM garantiza que sólo se crea una vez, aunque haya varios hilos.")
}

/**
 * `object` expression: objetos anónimos.
 */
fun demoObjectExpression() {
    section("Implementar una interfaz sobre la marcha")

    // Equivale a la clase anónima de Java, pero puede implementar VARIAS interfaces
    // y puede tener estado y miembros propios.
    val saludador = object : Saludo {
        override fun saludar(nombre: String) = "¡Hola, $nombre!"
    }
    show("object : Saludo { ... }", saludador.saludar("Ana"))

    section("Con estado propio")

    // Un objeto anónimo puede declarar sus propias propiedades.
    val contador = object {
        var veces = 0
        fun siguiente(): Int = ++veces
    }
    contador.siguiente()
    contador.siguiente()
    show("objeto anónimo con estado", contador.siguiente())

    bullet("Sólo funciona porque el tipo es local: fuera de aquí el tipo anónimo no existe.")
    bullet("Si lo devuelves desde una función pública, se ve como `Any`.")

    section("Captura variables del entorno (closure)")

    var totalAcumulado = 0
    val acumulador = object : Consumidor {
        override fun consumir(valor: Int) {
            totalAcumulado += valor      // modifica una variable local de fuera
        }
    }
    listOf(10, 20, 30).forEach(acumulador::consumir)
    show("total acumulado por el objeto anónimo", totalAcumulado)

    bullet("En Java la variable capturada tendría que ser `final`. Aquí no.")

    section("Implementar varias interfaces a la vez")

    val doble = object : Saludo, Consumidor {
        var ultimo = 0
        override fun saludar(nombre: String) = "hola $nombre (último: $ultimo)"
        override fun consumir(valor: Int) {
            ultimo = valor
        }
    }
    doble.consumir(42)
    show("un objeto, dos interfaces", doble.saludar("Luis"))

    section("La diferencia clave con `object` declaration")

    // Una expresión se evalúa CADA VEZ: dos llamadas, dos objetos distintos.
    show("crearAnonimo() === crearAnonimo()", crearAnonimo() === crearAnonimo())
    show("Registro === Registro", Registro === Registro)

    bullet("`object : T { }` (expresión) → un objeto nuevo en cada evaluación.")
    bullet("`object X { }`   (declaración) → siempre el mismo.")

    section("Cuándo usar cada cosa")

    bullet("Interfaz de un solo método → mejor una lambda (conversión SAM, capítulo 27).")
    bullet("Varios métodos o estado propio → objeto anónimo.")
    bullet("Lo mismo repetido en varios sitios → dale nombre: clase u `object`.")
}

// -- Tipos auxiliares -----------------------------------------------------------------

private interface Saludo {
    fun saludar(nombre: String): String
}

private interface Consumidor {
    fun consumir(valor: Int)
}

private interface Describible {
    val descripcion: String
}

private fun describir(algo: Describible): String = algo.descripcion

/** Companion con constantes, fábricas, y que además implementa una interfaz. */
private class Usuario private constructor(val nombre: String, val edad: Int) {

    override fun toString(): String = "Usuario($nombre, $edad)"

    companion object : Describible {
        const val EDAD_MINIMA = 18

        override val descripcion: String = "fábrica de usuarios"

        /** Devuelve un texto en lugar del objeto para poder informar del rechazo. */
        fun crear(nombre: String, edad: Int): String =
            if (edad < EDAD_MINIMA) "rechazado: menor de $EDAD_MINIMA"
            else Usuario(nombre, edad).toString()

        /** Una instancia compartida: la fábrica no siempre construye algo nuevo. */
        private val ANONIMO = Usuario("anónimo", EDAD_MINIMA)

        fun anonimo(): String = ANONIMO.toString()
    }
}

/** Companion con nombre propio. */
private class Configuracion {
    companion object Predeterminada {
        val host: String = "localhost"
    }
}

/** Singleton con estado: útil, pero recuerda que es global. */
private object Registro {
    private val eventos = mutableListOf<String>()

    fun anotar(evento: String) {
        eventos.add(evento)
    }

    fun contar(): Int = eventos.size
    fun ultimo(): String = eventos.lastOrNull() ?: "(vacío)"
}

/** Un `object` puede implementar una interfaz: aquí, un Comparator reutilizable. */
private object OrdenPorLongitud : Comparator<String> {
    override fun compare(a: String, b: String): Int = a.length - b.length
}

/** Cada llamada devuelve un objeto NUEVO. */
private fun crearAnonimo(): Any = object {}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade a Usuario una fábrica `desdeTexto("Ana:34")` que devuelva null si falla.
//  2. Intenta llamar a `Usuario("Ana", 34)` directamente: el constructor es privado.
//  3. Convierte OrdenPorLongitud en `Comparator<String> { a, b -> ... }` (lambda SAM).
//  4. Añade una propiedad `var` a Registro y piensa qué pasaría con dos hilos.
