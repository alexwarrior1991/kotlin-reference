package com.alejandro.c07classes

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  7.5 · Clases anidadas, internas y locales
//
//  QUÉ ES
//    Una clase declarada dentro de otra. En Kotlin, por defecto es ANIDADA (no tiene
//    acceso a la instancia externa). Para que lo tenga hay que marcarla `inner`.
//
//  POR QUÉ IMPORTA
//    Es justo al revés que en Java, donde por defecto la clase interna guarda una
//    referencia al objeto externo. Esa referencia implícita es una causa clásica de
//    fugas de memoria; Kotlin te obliga a pedirla explícitamente.
//
//  ERRORES COMUNES
//    · Marcar `inner` por costumbre de Java sin necesitar el acceso al externo.
//    · No saber que `this@Externa` existe cuando dos clases tienen el mismo miembro.
//    · Usar una clase anidada para algo que no tiene nada que ver con la externa.
// =====================================================================================

/**
 * Anidada (por defecto): no ve la instancia externa.
 */
fun demoNestedClass() {
    section("Se crea sin necesitar la clase externa")

    // Fíjate: NO hace falta un Formulario para crear un Campo.
    val campo = Formulario.Campo("email", obligatorio = true)
    show("Formulario.Campo(\"email\", true)", campo)

    section("No ve las propiedades de la externa")

    // Dentro de Campo, escribir `titulo` sería un error: no hay ninguna instancia de
    // Formulario asociada.
    bullet("Una clase anidada es, en la práctica, una clase normal con el nombre")
    bullet("cualificado: sólo indica a quién pertenece conceptualmente.")

    section("Para qué sirve")

    val formulario = Formulario("Alta de usuario")
    formulario.agregar(Formulario.Campo("nombre", obligatorio = true))
    formulario.agregar(Formulario.Campo("apodo", obligatorio = false))
    show("formulario", formulario)
    show("campos obligatorios sin rellenar", formulario.faltantes())

    bullet("Agrupa un tipo auxiliar junto al que lo usa, sin ensuciar el paquete.")
    bullet("Equivale a `static class` en Java. Es el caso normal.")
}

/**
 * `inner`: sí ve la instancia externa.
 */
fun demoInnerClass() {
    section("Necesita una instancia de la externa para existir")

    val carrito = Carrito("carrito-42")

    // La sintaxis lo deja claro: la línea se crea A PARTIR de un carrito.
    val linea = carrito.Linea("teclado", 2, 30.0)
    show("carrito.Linea(\"teclado\", 2, 30.0)", linea)

    // La línea puede leer el identificador del carrito que la contiene.
    show("linea.resumen()  (usa datos del carrito)", linea.resumen())

    section("this@Externa: desambiguar")

    val pedido = Pedido("P-100", "Ana")
    show("pedido.Envio(\"Calle Mayor\").etiqueta()", pedido.Envio("Calle Mayor").etiqueta())

    bullet("`Pedido` y `Envio` tienen los dos una propiedad `referencia`.")
    bullet("Dentro de Envio, `referencia` es la suya; `this@Pedido.referencia` es la del padre.")

    section("El coste oculto")

    bullet("Cada objeto `inner` guarda una referencia al externo.")
    bullet("Si el interno vive más que el externo, impide que el GC lo libere.")
    bullet("En Android fue la causa número uno de fugas: un Handler interno que")
    bullet("sobrevivía a su Activity.")

    section("Cómo elegir")

    bullet("¿La clase interna necesita datos del objeto externo? → `inner`.")
    bullet("¿No? → déjala anidada (el valor por defecto). Es más barato y más seguro.")
}

/**
 * Clases locales: declaradas dentro de una función.
 */
fun demoLocalClass() {
    section("Una clase que sólo existe dentro de una función")

    // Igual que las funciones locales (5.9), sirve para lógica de un solo uso.
    show("analizar(listOf(4, 8, 15, 16, 23, 42))", analizar(listOf(4, 8, 15, 16, 23, 42)))

    section("Capturan variables locales")

    bullet("Una clase local puede leer (y modificar) las variables de la función.")
    bullet("Como los objetos anónimos, pero con nombre y con varios constructores.")

    section("Cuándo usarlas")

    bullet("Casi nunca. Suelen ser una señal de que la función hace demasiado.")
    bullet("Si necesitas una clase con nombre dentro de una función, plantéate sacarla.")
    bullet("La alternativa habitual: una `data class` privada del fichero.")
}

// -- Las clases que usan las demos ----------------------------------------------------

/**
 * `Campo` es ANIDADA: no necesita un Formulario para existir y no ve sus datos.
 */
private class Formulario(val titulo: String) {

    private val campos = mutableListOf<Campo>()

    /** Sin `inner`: es una clase normal que vive dentro del espacio de nombres. */
    class Campo(val nombre: String, val obligatorio: Boolean) {
        var valor: String = ""
        override fun toString(): String =
            "$nombre${if (obligatorio) "*" else ""}"
    }

    fun agregar(campo: Campo) {
        campos.add(campo)
    }

    fun faltantes(): List<String> =
        campos.filter { it.obligatorio && it.valor.isBlank() }.map { it.nombre }

    override fun toString(): String = "$titulo ${campos.map { it.toString() }}"
}

/**
 * `Linea` es INNER: necesita el carrito y puede leer sus propiedades.
 */
private class Carrito(val identificador: String) {

    inner class Linea(val producto: String, val unidades: Int, val precio: Double) {

        val total: Double get() = unidades * precio

        /** `identificador` es del Carrito; se accede sin prefijo porque es inner. */
        fun resumen(): String = "[$identificador] $producto x$unidades = $total €"

        override fun toString(): String = "$producto x$unidades"
    }
}

/**
 * Las dos clases tienen una propiedad `referencia`: hace falta `this@Pedido`.
 */
private class Pedido(val referencia: String, val cliente: String) {

    inner class Envio(val direccion: String) {

        /** La referencia del envío, distinta de la del pedido. */
        val referencia: String = "ENV-${direccion.take(3).uppercase()}"

        fun etiqueta(): String =
            "envío $referencia del pedido ${this@Pedido.referencia} para ${this@Pedido.cliente} → $direccion"
    }
}

/** Una clase local, declarada dentro de la función que la usa. */
private fun analizar(numeros: List<Int>): String {

    // Sólo tiene sentido aquí dentro.
    class Resumen(val minimo: Int, val maximo: Int) {
        val amplitud: Int get() = maximo - minimo
        override fun toString(): String = "min=$minimo max=$maximo amplitud=$amplitud"
    }

    if (numeros.isEmpty()) return "lista vacía"
    return Resumen(numeros.min(), numeros.max()).toString()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `inner` de `Linea` y lee el error al intentar usar `identificador`.
//  2. Añade `inner` a `Campo` y comprueba que ya no puedes escribir `Formulario.Campo(...)`.
//  3. En `Envio`, quita el `this@Pedido.` y observa qué referencia se usa entonces.
//  4. Saca `Resumen` fuera de la función y conviértela en `private data class`.
