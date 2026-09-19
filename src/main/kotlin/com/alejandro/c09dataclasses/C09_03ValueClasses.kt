package com.alejandro.c09dataclasses

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  9.3 · value class: cuando la data class es demasiado
//
//  QUÉ ES
//    `@JvmInline value class` envuelve UN solo valor y, en tiempo de ejecución,
//    desaparece: el compilador usa el valor de dentro directamente. Tienes la
//    seguridad de tipos sin pagar un objeto.
//
//  POR QUÉ IMPORTA
//    Resuelve la "obsesión por los primitivos": funciones con cinco parámetros String
//    donde es cuestión de tiempo que alguien cambie el orden. Con value class, el
//    compilador lo impide y no cuesta memoria.
//
//  ERRORES COMUNES
//    · Usar `typealias` creyendo que da seguridad de tipos. No la da.
//    · Olvidar `@JvmInline` (en la JVM es obligatorio).
//    · No saber que a veces sí se envuelve el valor (boxing) y sorprenderse.
// =====================================================================================

/**
 * El problema: obsesión por los primitivos.
 */
fun demoPrimitiveObsession() {
    section("Una firma peligrosa")

    bullet("fun registrar(nombre: String, email: String, telefono: String)")
    bullet("Los tres son String. Nada impide llamarla con el orden cambiado.")

    // Esto compila perfectamente y está mal:
    show("llamada con el orden cambiado", registrarMal("ana@ejemplo.com", "Ana", "600"))
    bullet("Compila, se ejecuta, y el dato queda mal guardado. Ni un aviso.")

    section("Lo mismo con tipos propios")

    // Ahora el compilador NO deja mezclarlos.
    show("llamada correcta", registrarBien(Nombre("Ana"), Email("ana@ejemplo.com")))
    // registrarBien(Email("x"), Nombre("y"))   // ERROR: Type mismatch
    bullet("`registrarBien(Email(...), Nombre(...))` ya no compila. Ése es todo el valor.")
}

/**
 * `value class` frente a las alternativas.
 */
fun demoValueClassVsAlternatives() {
    section("1. typealias: NO da seguridad de tipos")

    // Un typealias es sólo otro nombre para el mismo tipo. Son intercambiables.
    val correo: CorreoAlias = "ana@ejemplo.com"
    val nombre: NombreAlias = "Ana"
    show("typealias: ¿se pueden intercambiar?", aceptaCorreoAlias(nombre))

    bullet("`typealias Email = String` documenta, pero no protege.")
    bullet("Sirve para acortar tipos largos (`(String) -> Unit`), no para crear tipos.")

    section("2. data class: protege, pero crea un objeto")

    val conDataClass = EmailData("ana@ejemplo.com")
    show("data class", conDataClass)
    bullet("Seguridad de tipos, sí. Pero cada instancia es un objeto en memoria.")
    bullet("Con millones de ellos (un parser, un bucle apretado), se nota.")

    section("3. value class: protege y no cuesta nada")

    val conValueClass = Email("ana@ejemplo.com")
    show("value class", conValueClass)
    show("el valor de dentro", conValueClass.valor)
    bullet("En el bytecode, la mayoría de las veces esto es un String a secas.")
    bullet("Seguridad de tipos en compilación, coste cero en ejecución.")
}

/**
 * Qué se puede meter dentro.
 */
fun demoValueClassCapabilities() {
    section("Propiedades calculadas y métodos")

    val email = Email("Ana@Ejemplo.COM")
    show("email.valor", email.valor)
    show("email.dominio    (propiedad calculada)", email.dominio)
    show("email.normalizado() (método)", email.normalizado())
    show("email.esValido()", email.esValido())

    section("Validación en el init")

    val invalido = try {
        Email("no-soy-un-email").toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("Email(\"no-soy-un-email\")", invalido)

    bullet("Con `init` validando, un Email mal formado no puede existir.")
    bullet("Eso es más fuerte que validar en cada función que lo reciba.")

    section("Puede implementar interfaces")

    val importes = listOf(Dinero(1500), Dinero(300), Dinero(990))
    show("ordenados (implementa Comparable)", importes.sorted())
    show("suma", Dinero(importes.sumOf { it.centimos }))

    section("Las restricciones")

    bullet("Exactamente UNA propiedad en el constructor primario, y debe ser `val`.")
    bullet("No puede tener otras propiedades con backing field.")
    bullet("No puede ser `open`, ni heredar de una clase (sí implementar interfaces).")
    bullet("En la JVM hace falta la anotación `@JvmInline`.")
    bullet("No puede ser una clase local ni `inner`.")
}

/**
 * Cuándo sí se envuelve el valor (boxing).
 */
fun demoBoxing() {
    section("El compilador NO siempre puede eliminar la envoltura")

    val email = Email("ana@ejemplo.com")

    // Uso directo: no hay objeto, se pasa el String.
    show("uso directo", email.dominio)

    // Metido en una colección genérica: aquí SÍ se crea el objeto.
    val lista: List<Email> = listOf(email)
    show("dentro de una List<Email>", lista.first().valor)

    section("Cuándo se envuelve")

    bullet("Al guardarlo en una colección o en cualquier genérico.")
    bullet("Al usarlo como `Any` o como un tipo nulable (`Email?`).")
    bullet("Al pasarlo donde se espera una interfaz que implementa.")

    section("Cuándo NO se envuelve")

    bullet("Como parámetro y valor de retorno de funciones normales.")
    bullet("Como variable local.")
    bullet("Como propiedad de otra clase, si el tipo es exactamente el value class.")

    section("¿Importa?")

    bullet("Casi nunca: aun envolviéndose a veces, nunca es PEOR que una data class.")
    bullet("La seguridad de tipos la tienes siempre; la optimización, cuando se puede.")
}

// -- Las clases que usan las demos ----------------------------------------------------

// 1) typealias: sólo otro nombre. No crea un tipo nuevo.
private typealias CorreoAlias = String
private typealias NombreAlias = String

private fun aceptaCorreoAlias(correo: CorreoAlias): String = "aceptó '$correo' sin rechistar"

// 2) data class: crea un tipo, y también un objeto.
private data class EmailData(val valor: String)

// 3) value class: crea un tipo, pero normalmente no crea objeto.

@JvmInline
private value class Nombre(val valor: String)

@JvmInline
private value class Email(val valor: String) {

    init {
        require("@" in valor) { "'$valor' no parece un email" }
    }

    /** Propiedad calculada: permitida (no tiene backing field). */
    val dominio: String get() = valor.substringAfter('@')

    fun normalizado(): String = valor.lowercase()

    fun esValido(): Boolean = dominio.contains('.')

    override fun toString(): String = "Email($valor)"
}

/** Un value class que implementa una interfaz. */
@JvmInline
private value class Dinero(val centimos: Int) : Comparable<Dinero> {
    override fun compareTo(other: Dinero): Int = centimos - other.centimos
    override fun toString(): String = "%.2f €".format(centimos / 100.0)
}

/** La versión peligrosa: tres String seguidos. */
private fun registrarMal(nombre: String, email: String, telefono: String): String =
    "nombre='$nombre' email='$email' teléfono='$telefono'"

/** La versión protegida: los tipos no se pueden confundir. */
private fun registrarBien(nombre: Nombre, email: Email): String =
    "nombre='${nombre.valor}' email='${email.valor}'"

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Intenta llamar a `registrarBien(Email("a@b.c"), Nombre("Ana"))` y lee el error.
//  2. Quita `@JvmInline` de Email y comprueba que el compilador lo exige.
//  3. Añade una segunda propiedad al constructor de Email: tampoco está permitido.
//  4. Convierte Dinero en data class y piensa qué ganas y qué pierdes.
