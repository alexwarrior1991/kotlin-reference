package com.alejandro.c15lambdas

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  15.4 · Lambdas con receptor
//
//  QUÉ ES
//    Un tipo función de la forma `T.() -> R`. Dentro de la lambda, `this` es una
//    instancia de `T`, así que puedes llamar a sus miembros sin escribir el nombre
//    del objeto.
//
//  POR QUÉ IMPORTA
//    Es la característica sobre la que están construidos `apply`, `with`, `buildString`
//    y absolutamente todos los DSL de Kotlin (incluida la infraestructura de este
//    repositorio). Es lo que convierte una API normal en algo que se lee como un
//    lenguaje propio.
//
//  ERRORES COMUNES
//    · Confundir `T.() -> R` con `(T) -> R` (receptor frente a parámetro).
//    · Anidar receptores hasta no saber a qué `this` se refiere cada línea
//      (para eso está `@DslMarker`, capítulo 29).
//    · Usarlo donde una función normal se leería mejor.
// =====================================================================================

/**
 * La diferencia entre parámetro y receptor.
 */
fun demoParameterVsReceiver() {
    section("Los dos tipos, lado a lado")

    // (StringBuilder) -> Unit : el StringBuilder llega como parámetro → se usa `it`
    val comoParametro: (StringBuilder) -> Unit = { sb ->
        sb.append("hola")
        sb.append(" mundo")
    }

    // StringBuilder.() -> Unit : el StringBuilder es el receptor → se usa `this`
    val comoReceptor: StringBuilder.() -> Unit = {
        append("hola")
        append(" mundo")
    }

    val uno = StringBuilder().also(comoParametro)
    val dos = StringBuilder().apply(comoReceptor)

    show("con parámetro", uno.toString())
    show("con receptor", dos.toString())

    bullet("Mismo resultado. La diferencia es cómo se escribe DENTRO de la lambda.")
    bullet("Con receptor desaparece el prefijo repetido en cada línea.")

    section("`this` es opcional")

    val explicito: StringBuilder.() -> Unit = {
        this.append("con this explícito")
    }
    show("this.append(...)", StringBuilder().apply(explicito).toString())
    bullet("Se puede escribir `this.append()` o `append()`: es lo mismo.")

    section("Se pueden convertir el uno en el otro")

    // Una función con receptor se puede usar como función de un parámetro, y viceversa.
    val receptorComoParametro: (StringBuilder) -> Unit = comoReceptor
    show("un T.() -> R vale como (T) -> R", StringBuilder().also(receptorComoParametro).toString())
    bullet("Por debajo son lo mismo: el receptor es el primer parámetro oculto.")
}

/**
 * Por qué importa: el ruido que elimina.
 */
fun demoWhyItMatters() {
    section("Construir un objeto SIN receptor")

    show("versión con nombre repetido", construirSinReceptor())

    section("Construir el mismo objeto CON receptor")

    show("versión con receptor", construirConReceptor())

    bullet("Cinco líneas donde el nombre de la variable aparece cinco veces,")
    bullet("frente a cinco líneas donde no aparece ninguna.")

    section("La stdlib lo usa por todas partes")

    // buildString recibe StringBuilder.() -> Unit
    val texto = buildString {
        appendLine("Informe")
        append("- línea 1")
    }
    show("buildString { }", texto.replace("\n", " ⏎ "))

    // apply recibe T.() -> Unit y devuelve el objeto
    val lista = mutableListOf<Int>().apply {
        add(1)
        add(2)
        sort()
    }
    show("apply { }", lista)

    // with recibe el objeto y una T.() -> R
    val resumen = with(lista) {
        "tamaño=$size primero=${first()} suma=${sum()}"
    }
    show("with(obj) { }", resumen)

    bullet("Las cinco scope functions del capítulo 16 son variaciones de esta idea.")
}

/**
 * Escribir tu propia función con receptor.
 */
fun demoWritingYourOwn() {
    section("Un builder mínimo")

    val peticion = peticionHttp {
        metodo = "POST"
        url = "https://api.ejemplo.com/usuarios"
        cabecera("Content-Type", "application/json")
        cabecera("Accept", "application/json")
        cuerpo = """{"nombre": "Ana"}"""
    }

    show("método", peticion.metodo)
    show("url", peticion.url)
    show("cabeceras", peticion.cabeceras)
    show("cuerpo", peticion.cuerpo)

    bullet("Fíjate en que dentro de las llaves no aparece ningún nombre de variable.")
    bullet("Eso es lo que hace que parezca un lenguaje de configuración.")

    section("Cómo está hecho por dentro")

    bullet("1. Una clase mutable con las propiedades: PeticionBuilder.")
    bullet("2. Una función que recibe `PeticionBuilder.() -> Unit`.")
    bullet("3. Dentro: crear el builder, aplicarle el bloque, y construir el objeto final.")
    bullet("Son unas diez líneas. El capítulo 29 lo lleva bastante más lejos.")

    section("Devolver un objeto INMUTABLE desde un builder mutable")

    bullet("El builder es mutable para que la configuración sea cómoda de escribir.")
    bullet("El resultado es inmutable para que nadie lo toque después.")
    bullet("Es el mismo patrón de `buildList`: mutable dentro, de sólo lectura fuera.")
}

/**
 * Receptores anidados y `this@`.
 */
fun demoNestedReceivers() {
    section("Cuando hay varios receptores a la vez")

    val menu = menu {
        titulo = "Principal"
        opcion("Abrir") {
            atajo = "Ctrl+O"
            descripcion = "Abre un fichero"
        }
        opcion("Guardar") {
            atajo = "Ctrl+S"
            descripcion = "Guarda los cambios"
        }
    }

    show("título", menu.titulo)
    menu.opciones.forEach { show(it.etiqueta, "${it.atajo} · ${it.descripcion}") }

    section("Desambiguar con this@")

    bullet("Dentro del bloque de `opcion`, hay DOS receptores disponibles:")
    bullet("el de la opción (el más cercano) y el del menú (el de fuera).")
    bullet("`this@menu` y `this@opcion` permiten elegir explícitamente.")

    section("El problema de los receptores anidados")

    // Sin protección, dentro de `opcion { }` podrías llamar a `titulo = ...` y estarías
    // modificando el MENÚ sin darte cuenta, porque su receptor sigue disponible.
    bullet("Nada impide llamar a un método del receptor EXTERIOR por error.")
    bullet("En un DSL grande eso produce bugs silenciosos y difíciles de ver.")
    bullet("La solución es `@DslMarker`, que se explica en el capítulo 29.")
}

// -- Los tipos que usan las demos -------------------------------------------------------

private class Servidor {
    var host: String = ""
    var puerto: Int = 0
    var seguro: Boolean = false
    var tiempoEsperaMs: Long = 0
    var reintentos: Int = 0

    override fun toString(): String =
        "$host:$puerto seguro=$seguro espera=${tiempoEsperaMs}ms reintentos=$reintentos"
}

/** El nombre de la variable aparece en cada línea. */
private fun construirSinReceptor(): String {
    val servidor = Servidor()
    servidor.host = "api.ejemplo.com"
    servidor.puerto = 443
    servidor.seguro = true
    servidor.tiempoEsperaMs = 5_000
    servidor.reintentos = 3
    return servidor.toString()
}

/** Con `apply`, que recibe `Servidor.() -> Unit`, no aparece ni una vez. */
private fun construirConReceptor(): String = Servidor().apply {
    host = "api.ejemplo.com"
    puerto = 443
    seguro = true
    tiempoEsperaMs = 5_000
    reintentos = 3
}.toString()

// -- Un builder propio -------------------------------------------------------------------

private class Peticion(
    val metodo: String,
    val url: String,
    val cabeceras: Map<String, String>,
    val cuerpo: String?,
)

private class PeticionBuilder {
    var metodo: String = "GET"
    var url: String = ""
    var cuerpo: String? = null

    private val cabecerasInternas = mutableMapOf<String, String>()

    /** Un método del builder: se llama sin prefijo dentro del bloque. */
    fun cabecera(nombre: String, valor: String) {
        cabecerasInternas[nombre] = valor
    }

    internal fun construir(): Peticion = Peticion(metodo, url, cabecerasInternas.toMap(), cuerpo)
}

/**
 * El patrón completo en tres líneas: crear el builder, aplicar el bloque, construir.
 */
private fun peticionHttp(bloque: PeticionBuilder.() -> Unit): Peticion =
    PeticionBuilder().apply(bloque).construir()

// -- Receptores anidados -----------------------------------------------------------------

private class Opcion(val etiqueta: String, val atajo: String, val descripcion: String)
private class Menu(val titulo: String, val opciones: List<Opcion>)

private class OpcionBuilder(private val etiqueta: String) {
    var atajo: String = ""
    var descripcion: String = ""
    internal fun construir(): Opcion = Opcion(etiqueta, atajo, descripcion)
}

private class MenuBuilder {
    var titulo: String = ""
    private val opciones = mutableListOf<Opcion>()

    /** Recibe a su vez otra lambda con receptor: así se anidan los niveles. */
    fun opcion(etiqueta: String, bloque: OpcionBuilder.() -> Unit) {
        opciones.add(OpcionBuilder(etiqueta).apply(bloque).construir())
    }

    internal fun construir(): Menu = Menu(titulo, opciones.toList())
}

private fun menu(bloque: MenuBuilder.() -> Unit): Menu = MenuBuilder().apply(bloque).construir()

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `PeticionBuilder.() -> Unit` por `(PeticionBuilder) -> Unit` y arregla
//     la llamada: verás cuánto `it.` hay que escribir.
//  2. Dentro de un `opcion { }`, escribe `titulo = "otro"` y comprueba que compila
//     aunque no tenga sentido. Ése es el problema que resuelve @DslMarker.
//  3. Añade a `peticionHttp` una validación: que la url no pueda estar vacía.
//  4. Escribe un builder para una consulta SQL sencilla (select, from, where).
