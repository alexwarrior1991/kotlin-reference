package com.alejandro.c29dsl

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  29.3 · Un DSL de HTML con seguridad de tipos
//
//  QUÉ ES
//    El ejemplo clásico de "type-safe builder": construir un documento HTML con
//    código Kotlin en el que el COMPILADOR impide escribir HTML inválido.
//
//  POR QUÉ IMPORTA
//    Enseña las tres técnicas juntas: lambdas con receptor para anidar, jerarquía de
//    tipos para decidir qué puede ir dentro de qué, y `@DslMarker` para cerrar los
//    ámbitos. Con eso ya se puede escribir cualquier DSL.
//
//  ERRORES COMUNES
//    · Una sola clase `Etiqueta` para todo: se pierde la seguridad de tipos y el DSL
//      deja de guiar (podrías poner un <li> suelto en el <body>).
//    · Olvidar escapar el texto: un DSL de HTML que no escapa es un agujero.
//    · Construir el HTML concatenando cadenas dentro del builder en vez de un árbol.
// =====================================================================================

/**
 * El DSL en uso.
 */
fun demoHtmlDsl() {
    section("El código que se escribe")

    imprimirCodigo(
        """
        html {
            head {
                title("Referencia de Kotlin")
                meta(nombre = "charset", contenido = "UTF-8")
            }
            body {
                h1 { +"Capítulo 29" }
                p {
                    +"Los DSL se construyen con "
                    a(href = "…/type-safe-builders.html") { +"lambdas con receptor" }
                    +"."
                }
                div(clase = "aviso") {
                    p { +"El texto se escapa solo: 5 < 10 & 10 > 5" }
                }
                ul {
                    li { +"Lambdas con receptor" }
                    li { +"@DslMarker" }
                    li { +"Jerarquía de tipos" }
                }
            }
        }
        """.trimIndent(),
    )

    val pagina = html {
        head {
            title("Referencia de Kotlin")
            meta(nombre = "charset", contenido = "UTF-8")
        }
        body {
            h1 { +"Capítulo 29" }

            p {
                +"Los DSL se construyen con "
                a(href = "https://kotlinlang.org/docs/type-safe-builders.html") {
                    +"lambdas con receptor"
                }
                +"."
            }

            div(clase = "aviso") {
                p { +"El texto se escapa solo: 5 < 10 & 10 > 5" }
            }

            ul {
                li { +"Lambdas con receptor" }
                li { +"@DslMarker" }
                li { +"Jerarquía de tipos" }
            }
        }
    }

    section("El HTML que sale")

    imprimirCodigo(pagina.renderizar())

    section("Fíjate en dos cosas")

    bullet("El `<` y el `&` del aviso han salido escapados como &lt; y &amp;.")
    bullet("Las etiquetas con un solo texto dentro se han quedado en una línea.")
}

/**
 * Cómo está hecho por dentro.
 */
fun demoHtmlDslInside() {
    section("1. Un árbol de nodos, no una cadena")

    bullet("El builder NO concatena texto: construye un árbol de objetos `Nodo`.")
    bullet("Sólo al final, `renderizar()` recorre el árbol y produce el texto.")
    bullet("Eso permite inspeccionarlo, transformarlo o renderizarlo de otra forma.")

    val pagina = html {
        body {
            h1 { +"Título" }
            p { +"Párrafo" }
        }
    }
    show("el objeto en memoria", pagina::class.simpleName)
    show("etiquetas del árbol", pagina.etiquetasAnidadas())

    section("2. `+\"texto\"` es un operador de extensión")

    bullet("Dentro de una etiqueta que admite texto hay declarado:")
    bullet("")
    bullet("    operator fun String.unaryPlus() { hijos += Texto(this) }")
    bullet("")
    bullet("Es una extensión de String declarada DENTRO de la clase, así que sólo")
    bullet("existe cuando esa etiqueta es el receptor. Por eso `+\"hola\"` funciona en")
    bullet("un `p { }` y no en un `body { }`.")

    section("3. La seguridad de tipos viene de la jerarquía")

    bullet("EtiquetaDeTexto  → admite `+\"texto\"` y enlaces → p, h1, li, a, title")
    bullet("EtiquetaDeBloque → admite h1, p, ul, div       → body, div")
    bullet("Ul               → admite SÓLO li")
    bullet("Head             → admite SÓLO title y meta")
    bullet("")
    bullet("Estas líneas no compilan, y ése es justo el objetivo:")
    bullet("    body { li { } }        ← 'li' no existe en EtiquetaDeBloque")
    bullet("    ul { p { } }           ← 'p' no existe en Ul")
    bullet("    head { h1 { } }        ← 'h1' no existe en Head")

    section("4. @DslMarker cierra los receptores")

    bullet("Sin la anotación, dentro de un `li { }` seguiría visible el `li` del `ul`")
    bullet("exterior, y `ul { li { li { } } }` compilaría produciendo algo absurdo.")

    section("5. El patrón `anidar`, que se repite en cada etiqueta")

    bullet("    protected fun <T : Etiqueta> anidar(hija: T, bloque: T.() -> Unit): T {")
    bullet("        hija.bloque()      // 1. se aplica el bloque con la hija de receptor")
    bullet("        hijos += hija      // 2. se cuelga del árbol")
    bullet("        return hija")
    bullet("    }")
    bullet("")
    bullet("Todas las funciones del DSL (`p`, `ul`, `div`…) son una línea que llama")
    bullet("a `anidar` con la clase correspondiente.")

    section("6. Los atributos")

    val conAtributos = html {
        body {
            div(clase = "tarjeta destacada") {
                p { +"Con atributos" }
            }
        }
    }
    imprimirCodigo(conAtributos.renderizar())

    bullet("Se guardan en un LinkedHashMap para que el orden de salida sea estable:")
    bullet("con un HashMap normal, el HTML cambiaría entre ejecuciones.")

    section("7. Es Kotlin: se pueden usar bucles y condicionales")

    val tareas = listOf("Leer el capítulo" to true, "Hacer los ejercicios" to false)
    val dinamica = html {
        body {
            ul {
                for ((texto, hecha) in tareas) {          // un bucle normal
                    li { +(if (hecha) "✓ $texto" else "· $texto") }
                }
            }
        }
    }
    imprimirCodigo(dinamica.renderizar())

    bullet("Ésta es la ventaja sobre una plantilla de texto: dentro del DSL tienes")
    bullet("el lenguaje entero, con su compilador y su autocompletado.")
}

// -- El DSL ---------------------------------------------------------------------------------------

/** Marcador del DSL de HTML: cierra los receptores anidados (demo 29.5). */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class HtmlDsl

/** Cualquier cosa que sepa escribirse a sí misma. */
sealed interface Nodo {
    fun renderizar(salida: StringBuilder, sangria: String)
}

/** Texto suelto. Se escapa al renderizar, nunca antes. */
class Texto(val valor: String) : Nodo {
    override fun renderizar(salida: StringBuilder, sangria: String) {
        salida.append(sangria).append(escaparHtml(valor)).append('\n')
    }
}

/**
 * La base de todas las etiquetas.
 *
 * `@HtmlDsl` va aquí, en la raíz de la jerarquía: como todas las etiquetas heredan,
 * todas quedan marcadas sin tener que acordarse una por una.
 */
@HtmlDsl
abstract class Etiqueta(private val nombre: String) : Nodo {

    // LinkedHashMap: conserva el orden de inserción, así la salida es estable.
    private val atributos = linkedMapOf<String, String>()
    protected val hijos = mutableListOf<Nodo>()

    /** El patrón que usan TODAS las funciones del DSL. */
    protected fun <T : Etiqueta> anidar(hija: T, bloque: T.() -> Unit): T {
        hija.bloque()
        hijos += hija
        return hija
    }

    protected fun atributo(nombre: String, valor: String?) {
        if (valor != null) atributos[nombre] = valor
    }

    /** Los nombres de las etiquetas del árbol, para poder inspeccionarlo. */
    fun etiquetasAnidadas(): List<String> =
        listOf(nombre) + hijos.filterIsInstance<Etiqueta>().flatMap { it.etiquetasAnidadas() }

    /** Punto de entrada cómodo: renderiza el árbol entero. */
    fun renderizar(): String = StringBuilder().also { renderizar(it, "") }.toString()

    override fun renderizar(salida: StringBuilder, sangria: String) {
        val apertura = atributos.entries.joinToString("") { (clave, valor) ->
            " $clave=\"${escaparHtml(valor)}\""
        }
        salida.append(sangria).append("<").append(nombre).append(apertura)

        if (hijos.isEmpty()) {
            // Etiquetas sin contenido, como <meta>: se cierran solas.
            salida.append(" />\n")
            return
        }
        salida.append(">")

        val unicoTexto = hijos.singleOrNull() as? Texto
        if (unicoTexto != null) {
            // <li>uno</li> en una sola línea: se lee mucho mejor.
            salida.append(escaparHtml(unicoTexto.valor)).append("</").append(nombre).append(">\n")
        } else {
            salida.append('\n')
            hijos.forEach { it.renderizar(salida, "$sangria  ") }
            salida.append(sangria).append("</").append(nombre).append(">\n")
        }
    }
}

/** Etiquetas que admiten texto y enlaces dentro. */
abstract class EtiquetaDeTexto(nombre: String) : Etiqueta(nombre) {

    /**
     * Una extensión de String declarada DENTRO de la clase: sólo existe mientras
     * esta etiqueta sea el receptor. Ése es el truco de `+"texto"`.
     */
    operator fun String.unaryPlus() {
        hijos += Texto(this)
    }

    fun a(href: String, bloque: A.() -> Unit): A = anidar(A(href), bloque)
}

/** Etiquetas que admiten otros bloques dentro. */
abstract class EtiquetaDeBloque(nombre: String) : Etiqueta(nombre) {
    fun h1(bloque: H1.() -> Unit): H1 = anidar(H1(), bloque)
    fun p(bloque: P.() -> Unit): P = anidar(P(), bloque)
    fun ul(bloque: Ul.() -> Unit): Ul = anidar(Ul(), bloque)
    fun div(clase: String? = null, bloque: Div.() -> Unit): Div = anidar(Div(clase), bloque)
}

class H1 : EtiquetaDeTexto("h1")
class P : EtiquetaDeTexto("p")
class Li : EtiquetaDeTexto("li")
class Title : EtiquetaDeTexto("title")

class A(href: String) : EtiquetaDeTexto("a") {
    init {
        atributo("href", href)
    }
}

class Div(clase: String?) : EtiquetaDeBloque("div") {
    init {
        atributo("class", clase)
    }
}

class Body : EtiquetaDeBloque("body")

/** Un `ul` sólo admite `li`: eso lo garantiza el tipo, no un comentario. */
class Ul : Etiqueta("ul") {
    fun li(bloque: Li.() -> Unit): Li = anidar(Li(), bloque)
}

class Head : Etiqueta("head") {
    fun title(texto: String): Title = anidar(Title()) { +texto }

    fun meta(nombre: String, contenido: String) {
        anidar(Meta(nombre, contenido)) { }
    }
}

class Meta(nombre: String, contenido: String) : Etiqueta("meta") {
    init {
        atributo("name", nombre)
        atributo("content", contenido)
    }
}

class Html : Etiqueta("html") {
    fun head(bloque: Head.() -> Unit): Head = anidar(Head(), bloque)
    fun body(bloque: Body.() -> Unit): Body = anidar(Body(), bloque)
}

/** La puerta de entrada. Igual que en los otros DSL del capítulo. */
fun html(bloque: Html.() -> Unit): Html = Html().apply(bloque)

// -- Utilidades ------------------------------------------------------------------------------------

/**
 * Escapa los cuatro caracteres que rompen el HTML.
 *
 * Un DSL que no escapa es tan peligroso como concatenar SQL a mano: cualquier texto
 * que venga de fuera puede inyectar etiquetas.
 */
private fun escaparHtml(texto: String): String = texto
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

/** Imprime un bloque de código con sangría, sin los adornos de `bullet`. */
private fun imprimirCodigo(texto: String) {
    texto.trimEnd().lines().forEach { println("      $it") }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `body { li { +"suelto" } }` y lee el error: eso es la seguridad de tipos.
//  2. Añade una etiqueta `img(src, alt)` que no tenga hijos ni cierre.
//  3. Quita el escapado de `escaparHtml` y mete un `<script>` en un texto.
//  4. Añade un `renderizarMinificado()` que no ponga saltos de línea ni sangría:
//     el árbol es el mismo, sólo cambia el recorrido.
