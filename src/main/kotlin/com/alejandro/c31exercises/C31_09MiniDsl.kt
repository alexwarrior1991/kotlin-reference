package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 9 · Mini DSL de informes                                      🔴 difícil
//
//  Repasa: lambdas con receptor, builders, @DslMarker, jerarquía de tipos, validación.
//  Capítulos: 15 (lambdas), 17 (extensiones), 29 (DSLs).
// =====================================================================================

/** Ejercicio 9: un DSL para construir informes de texto. */
fun ejercicio09MiniDsl() {
    enunciado(
        "Construye un DSL para escribir informes, que luego se puedan volcar a",
        "texto plano o a Markdown SIN tocar el código del informe.",
        "",
        "Debe permitir escribir algo así:",
        "",
        "    informe {",
        "        titulo = \"Ventas del trimestre\"",
        "        autor = \"Equipo comercial\"",
        "",
        "        seccion(\"Resumen\") {",
        "            parrafo(\"Buen trimestre en general.\")",
        "            lista { punto(\"Norte: +12%\"); punto(\"Sur: -3%\") }",
        "        }",
        "        seccion(\"Detalle\") {",
        "            tabla { columnas(\"Región\", \"Total\"); fila(\"Norte\", \"725,50 €\") }",
        "        }",
        "    }",
        "",
        "Requisitos:",
        "1. El informe construido es INMUTABLE; los builders, internos.",
        "2. `@DslMarker` para que dentro de `lista { }` no se cuele `parrafo(...)`.",
        "3. Validación al construir: sin título no hay informe; una tabla sin",
        "   columnas no vale; una fila con más celdas que columnas, tampoco.",
        "4. Dos renderizadores distintos sobre el MISMO árbol: texto y Markdown.",
    )

    pistas(
        "El patrón entero es: `Builder().apply(bloque).construir()`. Todo lo demás",
        "   son detalles (capítulo 29).",
        "Modela el contenido como una jerarquía sellada: Parrafo, Lista y Tabla.",
        "   Renderizar pasa a ser un `when` exhaustivo, uno por formato.",
        "Cada nivel tiene SU builder, y cada builder sólo ofrece lo que cabe ahí:",
        "   `ListaBuilder` sólo tiene `punto`, `TablaBuilder` sólo `columnas` y `fila`.",
        "Marca todos los builders con la misma anotación `@InformeDsl`, o mejor,",
        "   pon la anotación en una clase base de la que hereden.",
        "Construye el árbol primero y renderiza al final: si concatenas texto dentro",
        "   del builder, el segundo formato te obligará a reescribirlo todo.",
    )

    solucionEnMarcha()

    val informe = informe {
        titulo = "Ventas del trimestre"
        autor = "Equipo comercial"

        seccion("Resumen") {
            parrafo("El trimestre cierra por encima del objetivo, con el Norte tirando del carro.")
            lista {
                punto("Norte: +12% respecto al trimestre anterior")
                punto("Sur: -3%, afectado por la caída de mayo")
                punto("Este: estable")
            }
        }

        seccion("Detalle por región") {
            parrafo("Cifras cerradas a 30 de junio.")
            tabla {
                columnas("Región", "Ventas", "Total")
                fila("Norte", "4", "725,50 €")
                fila("Sur", "5", "380,00 €")
                fila("Este", "4", "455,00 €")
            }
        }

        seccion("Próximos pasos") {
            lista {
                punto("Revisar la previsión del Sur")
                punto("Preparar la campaña de otoño")
            }
        }
    }

    section("Renderizado como texto plano")

    imprimirBloque(informe.comoTexto())

    section("El MISMO informe, como Markdown")

    imprimirBloque(informe.comoMarkdown())

    section("El árbol que hay detrás")

    show("secciones", informe.secciones.size)
    show("tipos de bloque", informe.secciones.flatMap { it.bloques }.map { it::class.simpleName }.distinct())
    show("puntos de lista en total", informe.secciones.flatMap { it.bloques }.filterIsInstance<Bloque.Lista>().sumOf { it.puntos.size })

    section("Validación")

    listOf<Pair<String, () -> Informe>>(
        "sin título" to { informe { seccion("x") { parrafo("y") } } },
        "sección sin contenido" to {
            informe { titulo = "t"; seccion("vacía") { } }
        },
        "tabla sin columnas" to {
            informe { titulo = "t"; seccion("s") { tabla { fila("a") } } }
        },
        "fila con celdas de más" to {
            informe {
                titulo = "t"
                seccion("s") { tabla { columnas("A", "B"); fila("1", "2", "3") } }
            }
        },
    ).forEach { (caso, construir) ->
        val fallo = runCatching { construir() }.exceptionOrNull()
        show(caso, "✗ ${fallo?.message}")
    }

    explicacion(
        "LA DECISIÓN QUE LO CAMBIA TODO: construir un ÁRBOL, no texto.",
        "",
        "La tentación es que `parrafo(\"hola\")` haga `salida.append(\"hola\\n\")`. Con",
        "eso funciona el primer formato y sólo el primero: el día que pidan Markdown",
        "hay que reescribir los builders enteros. Con un árbol de `Bloque`, el",
        "informe no sabe nada de cómo se pinta, y añadir HTML o PDF es una función",
        "más, sin tocar nada de lo anterior.",
        "",
        "Es la misma separación de siempre: representar los datos por un lado y",
        "presentarlos por otro.",
        "",
        "LA SEGUNDA: una clase de builder por nivel, con sólo las funciones que caben",
        "ahí. `ListaBuilder` no tiene `parrafo`, así que no se puede escribir. Eso es",
        "seguridad de tipos, y sale gratis por modelar bien.",
        "",
        "LA TERCERA: `@DslMarker`. Sin ella, dentro de `lista { }` seguiría visible",
        "el `parrafo(...)` de la sección de fuera, y `lista { parrafo(\"x\") }`",
        "compilaría añadiendo el párrafo a la SECCIÓN. Un bug silencioso de libro",
        "(demo 29.4).",
        "",
        "Y la validación en `construir()`, con el mensaje diciendo qué sección o qué",
        "tabla está mal: un DSL que falla con «IllegalStateException» a secas es",
        "peor que no tener DSL.",
    )

    varianteDificil(
        "1. Añade `comoHtml()` reutilizando el DSL de HTML del capítulo 29.",
        "2. Permite secciones ANIDADAS (`seccion` dentro de `seccion`) con numeración",
        "   automática: 1, 1.1, 1.2, 2...",
        "3. Añade `tablaDe(lista) { columna(\"Nombre\") { it.nombre } }`: una tabla",
        "   generada a partir de objetos, con las columnas declaradas por lambdas.",
        "4. Alinea las columnas de la tabla de texto al ancho de su contenido más",
        "   largo (ya está hecho: mira `anchosDe` y quítalo para ver la diferencia).",
        "5. Añade un índice automático al principio, calculado del propio árbol.",
        "6. Haz que el informe se pueda serializar a JSON sin librerías externas.",
    )

    testEn("MiniDslTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/** El marcador del DSL: cierra los receptores anidados (capítulo 29). */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class InformeDsl

// -- El modelo: inmutable y sin saber nada de formatos ---------------------------------------------

/** Los tipos de contenido que puede llevar una sección. */
sealed interface Bloque {
    data class Parrafo(val texto: String) : Bloque
    data class Lista(val puntos: List<String>) : Bloque
    data class Tabla(val columnas: List<String>, val filas: List<List<String>>) : Bloque
}

data class Seccion(val titulo: String, val bloques: List<Bloque>)

data class Informe(val titulo: String, val autor: String?, val secciones: List<Seccion>)

// -- Los builders ----------------------------------------------------------------------------------

@InformeDsl
class InformeBuilder internal constructor() {

    var titulo: String = ""
    var autor: String? = null

    private val secciones = mutableListOf<Seccion>()

    fun seccion(titulo: String, bloque: SeccionBuilder.() -> Unit) {
        secciones += SeccionBuilder(titulo).apply(bloque).construir()
    }

    internal fun construir(): Informe {
        require(titulo.isNotBlank()) { "el informe necesita un `titulo`" }
        require(secciones.isNotEmpty()) { "el informe «$titulo» no tiene ninguna sección" }
        return Informe(titulo, autor?.takeIf { it.isNotBlank() }, secciones.toList())
    }
}

@InformeDsl
class SeccionBuilder internal constructor(private val titulo: String) {

    private val bloques = mutableListOf<Bloque>()

    fun parrafo(texto: String) {
        require(texto.isNotBlank()) { "un párrafo vacío en la sección «$titulo»" }
        bloques += Bloque.Parrafo(texto)
    }

    fun lista(bloque: ListaBuilder.() -> Unit) {
        bloques += ListaBuilder(titulo).apply(bloque).construir()
    }

    fun tabla(bloque: TablaBuilder.() -> Unit) {
        bloques += TablaBuilder(titulo).apply(bloque).construir()
    }

    internal fun construir(): Seccion {
        require(bloques.isNotEmpty()) { "la sección «$titulo» está vacía" }
        return Seccion(titulo, bloques.toList())
    }
}

/** Sólo tiene `punto`: dentro de `lista { }` no cabe nada más. */
@InformeDsl
class ListaBuilder internal constructor(private val seccion: String) {

    private val puntos = mutableListOf<String>()

    fun punto(texto: String) {
        require(texto.isNotBlank()) { "un punto vacío en la sección «$seccion»" }
        puntos += texto
    }

    internal fun construir(): Bloque.Lista {
        require(puntos.isNotEmpty()) { "una lista sin puntos en la sección «$seccion»" }
        return Bloque.Lista(puntos.toList())
    }
}

@InformeDsl
class TablaBuilder internal constructor(private val seccion: String) {

    private var columnas: List<String> = emptyList()
    private val filas = mutableListOf<List<String>>()

    fun columnas(vararg nombres: String) {
        require(nombres.isNotEmpty()) { "una tabla sin columnas en la sección «$seccion»" }
        columnas = nombres.toList()
    }

    fun fila(vararg celdas: String) {
        filas += celdas.toList()
    }

    internal fun construir(): Bloque.Tabla {
        require(columnas.isNotEmpty()) { "la tabla de la sección «$seccion» no declara columnas" }

        val malas = filas.filter { it.size > columnas.size }
        require(malas.isEmpty()) {
            "en la sección «$seccion» hay ${malas.size} fila(s) con más celdas " +
                "(${malas.first().size}) que columnas (${columnas.size})"
        }

        // Las filas cortas se rellenan: es más amable que fallar por una celda vacía.
        val normalizadas = filas.map { fila -> fila + List(columnas.size - fila.size) { "" } }
        return Bloque.Tabla(columnas, normalizadas)
    }
}

/** La única puerta de entrada del DSL. */
fun informe(bloque: InformeBuilder.() -> Unit): Informe =
    InformeBuilder().apply(bloque).construir()

// -- Los renderizadores: dos recorridos del mismo árbol ---------------------------------------------

/** Texto plano, con las tablas alineadas al contenido. */
fun Informe.comoTexto(): String = buildString {
    appendLine(titulo.uppercase())
    appendLine("=".repeat(titulo.length))
    if (autor != null) appendLine("por $autor")

    secciones.forEachIndexed { indice, seccion ->
        appendLine()
        appendLine("${indice + 1}. ${seccion.titulo}")
        appendLine("-".repeat(seccion.titulo.length + 3))

        seccion.bloques.forEach { bloque ->
            when (bloque) {
                is Bloque.Parrafo -> appendLine(bloque.texto)

                is Bloque.Lista -> bloque.puntos.forEach { appendLine("  · $it") }

                is Bloque.Tabla -> {
                    val anchos = anchosDe(bloque)
                    appendLine(formatearFila(bloque.columnas, anchos))
                    appendLine(anchos.joinToString("-+-") { "-".repeat(it) })
                    bloque.filas.forEach { appendLine(formatearFila(it, anchos)) }
                }
            }
        }
    }
}

/** El mismo árbol, en Markdown. Ni una línea del informe cambia. */
fun Informe.comoMarkdown(): String = buildString {
    appendLine("# $titulo")
    if (autor != null) appendLine("_por ${autor}_")

    secciones.forEach { seccion ->
        appendLine()
        appendLine("## ${seccion.titulo}")
        appendLine()

        seccion.bloques.forEach { bloque ->
            when (bloque) {
                is Bloque.Parrafo -> {
                    appendLine(bloque.texto)
                    appendLine()
                }

                is Bloque.Lista -> {
                    bloque.puntos.forEach { appendLine("- $it") }
                    appendLine()
                }

                is Bloque.Tabla -> {
                    appendLine(bloque.columnas.joinToString(" | ", "| ", " |"))
                    appendLine(bloque.columnas.joinToString(" | ", "| ", " |") { "---" })
                    bloque.filas.forEach { appendLine(it.joinToString(" | ", "| ", " |")) }
                    appendLine()
                }
            }
        }
    }
}

/** El ancho de cada columna: el del texto más largo que haya en ella. */
private fun anchosDe(tabla: Bloque.Tabla): List<Int> = tabla.columnas.indices.map { columna ->
    val enFilas = tabla.filas.maxOfOrNull { it.getOrElse(columna) { "" }.length } ?: 0
    maxOf(tabla.columnas[columna].length, enFilas)
}

private fun formatearFila(celdas: List<String>, anchos: List<Int>): String =
    celdas.mapIndexed { indice, celda -> celda.padEnd(anchos[indice]) }.joinToString(" | ")

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun imprimirBloque(texto: String) {
    texto.trimEnd().lines().forEach { println("      $it") }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `lista { parrafo("x") }` y lee el error: eso es `@DslMarker` trabajando.
//  2. Quita `@InformeDsl` de `ListaBuilder` y comprueba que ahora sí compila... y que
//     el párrafo acaba FUERA de la lista.
//  3. Añade `Bloque.Cita(texto)` y deja que el compilador te lleve a los dos `when`.
