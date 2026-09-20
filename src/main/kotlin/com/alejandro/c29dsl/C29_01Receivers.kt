package com.alejandro.c29dsl

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  29.1 · Lambdas con receptor: la pieza que hace posibles los DSL
//
//  QUÉ ES
//    Un tipo de función que, además de sus parámetros, tiene un RECEPTOR: dentro de
//    la lambda, `this` es ese receptor y sus miembros se pueden usar sin nombrarlo.
//
//        (Usuario) -> Unit      →  se recibe como parámetro:  { u -> u.nombre = "Ana" }
//        Usuario.() -> Unit     →  se recibe como `this`:     { nombre = "Ana" }
//
//  POR QUÉ IMPORTA
//    Es LA diferencia entre una API normal y un DSL. Todo lo que parece "sintaxis
//    especial" de Kotlin (`apply`, `buildString`, los tests, Gradle Kotlin DSL) es
//    esto y nada más.
//
//  ERRORES COMUNES
//    · Usar `let`/`run` con parámetro cuando querías receptor (o al revés).
//    · Construir un DSL para algo que se resolvía con argumentos con nombre.
//    · Anidar receptores sin `@DslMarker` (demo 29.4).
// =====================================================================================

/**
 * Qué es exactamente una lambda con receptor.
 */
fun demoLambdaWithReceiver() {
    section("Los dos tipos, uno al lado del otro")

    // Sin receptor: el valor llega como parámetro y hay que nombrarlo.
    val sinReceptor: (String) -> Int = { texto -> texto.length }

    // Con receptor: el valor es `this` y sus miembros están disponibles directamente.
    val conReceptor: String.() -> Int = { length }

    show("sinReceptor(\"kotlin\")", sinReceptor("kotlin"))
    show("\"kotlin\".conReceptor()", "kotlin".conReceptor())

    bullet("Fíjate en el cuerpo: `texto.length` frente a `length` a secas.")

    section("Una lambda con receptor se puede llamar de dos formas")

    show("como método de extensión", "kotlin".conReceptor())
    show("como función normal", conReceptor("kotlin"))

    val comoParametro: (String) -> Int = conReceptor
    show("asignado a (String) -> Int", comoParametro("hola"))

    bullet("`A.() -> B` y `(A) -> B` son tipos compatibles: Kotlin los convierte solo.")
    bullet("El receptor es azúcar sintáctico; por debajo es el primer parámetro.")

    section("Por qué esto permite escribir DSL")

    // Con receptor, quien escribe el bloque no tiene que repetir el nombre del objeto.
    val conNombre = StringBuilder().apply {
        append("con ")
        append("receptor")          // `this` implícito: no hace falta `sb.append`
    }

    val sinNombre = StringBuilder().let { sb ->
        sb.append("sin ")
        sb.append("receptor")       // hay que nombrar `sb` cada vez
        sb
    }

    show("apply (receptor)", conNombre.toString())
    show("let (parámetro)", sinNombre.toString())

    bullet("En dos líneas la diferencia es cosmética; en un bloque de veinte, no.")

    section("Las funciones de la biblioteca que ya lo usan")

    show("apply", StringBuilder().apply { append("a") }.toString())
    show("with", with(StringBuilder()) { append("b"); toString() })
    show("buildString", buildString { append("c"); append("d") })
    show("buildList", buildList { add(1); add(2) })

    bullet("Todas reciben un `T.() -> R`. No hay nada mágico en `buildString`:")
    bullet("es `StringBuilder().apply(bloque).toString()` con otro nombre.")

    section("El truco completo, en una línea")

    // Esta función de tres líneas es el 90% de lo que hace un DSL.
    show("construirTexto { ... }", construirTexto { append("hecho a mano") })

    bullet("1. Creas el objeto mutable.")
    bullet("2. Le aplicas el bloque con el objeto como receptor.")
    bullet("3. Devuelves el resultado inmutable.")
    bullet("Todo lo demás son detalles de comodidad.")
}

/**
 * De una función normal a un DSL, paso a paso.
 */
fun demoFromFunctionToBuilder() {
    section("El problema: una función con demasiados parámetros")

    // Paso 0: la llamada no dice nada. ¿Qué es `true`? ¿Y el segundo `null`?
    val paso0 = enviarCorreoPosicional("ana@ejemplo.com", "Hola", "Qué tal", true, null)
    show("posicional", paso0)

    bullet("Con cinco parámetros ya no se sabe qué es cada cosa en el punto de llamada.")

    section("Paso 1: argumentos con nombre (y valores por defecto)")

    val paso1 = enviarCorreoPosicional(
        para = "ana@ejemplo.com",
        asunto = "Hola",
        cuerpo = "Qué tal",
        urgente = true,
    )
    show("con nombres", paso1)

    bullet("Esto ya resuelve el 80% de los casos y NO hace falta ningún DSL.")
    bullet("Antes de construir uno, pregúntate si esto no era suficiente.")

    section("Paso 2: un constructor encadenado, estilo Java")

    val paso2 = CorreoEncadenado()
        .para("ana@ejemplo.com")
        .asunto("Hola")
        .cuerpo("Qué tal")
        .urgente()
        .construir()
    show("encadenado", paso2)

    bullet("Funciona, pero cada método tiene que devolver `this` y el punto final")
    bullet("(`.construir()`) es fácil de olvidar: la llamada compila sin él.")

    section("Paso 3: un bloque con receptor, que es el DSL")

    val paso3 = correo {
        para = "ana@ejemplo.com"
        asunto = "Hola"
        cuerpo = "Qué tal"
        urgente = true
    }
    show("DSL", paso3)

    bullet("Ahora el bloque es una lista de asignaciones legibles, el orden da igual")
    bullet("y no se puede olvidar el `construir()`: lo hace la propia función `correo`.")

    section("Y donde el DSL gana de verdad: lo anidado y lo repetido")

    val conAdjuntos = correo {
        para = "equipo@ejemplo.com"
        asunto = "Informe"
        cuerpo = "Adjunto va todo."
        adjuntar("informe.pdf", 2_048)
        adjuntar("anexo.csv", 512)
    }
    show("con adjuntos", conAdjuntos)

    bullet("Una lista de adjuntos como parámetro obligaría a construir una List de")
    bullet("objetos a mano. Aquí es una línea por adjunto, y se pueden repetir.")

    section("La regla para decidir")

    bullet("¿Pocos valores, planos, sin repetición?      → argumentos con nombre.")
    bullet("¿Estructura anidada o elementos repetidos?   → merece la pena un DSL.")
    bullet("¿Lo van a escribir personas no programadoras? → probablemente no sea DSL,")
    bullet("                                                sino un fichero de config.")
}

/**
 * Las reglas de un builder decente.
 */
fun demoBuilderRules() {
    section("1. El builder es mutable; el resultado, inmutable")

    val creado = correo {
        para = "ana@ejemplo.com"
        asunto = "Inmutable"
        cuerpo = "El builder muta; el Correo no."
    }

    show("tipo devuelto", creado::class.simpleName)
    show("¿se puede cambiar después?", "no: todas sus propiedades son `val`")

    bullet("Si devolvieras el propio builder, cualquiera podría seguir cambiándolo")
    bullet("después de 'construir'. El paso a un tipo inmutable cierra esa puerta.")

    section("2. La validación va en el momento de construir")

    val sinDestinatario = runCatching {
        correo {
            asunto = "Sin destinatario"
        }
    }
    show("correo { } sin `para`", sinDestinatario.exceptionOrNull()?.message)

    bullet("`require` dentro de `construir()` es el sitio natural: en ese punto ya")
    bullet("está todo escrito y aún no ha salido nada del DSL.")
    bullet("Validar en cada `set` sería peor: el orden de las líneas pasaría a importar.")

    section("3. Las funciones del DSL son la API; los campos internos, no")

    bullet("El builder se declara `class ... internal constructor` o directamente")
    bullet("privado, y se expone sólo la función de entrada (`correo { }`).")
    bullet("Así nadie puede construir un builder suelto y dejarlo a medias.")

    section("4. Un `inline` bien puesto y nada más")

    bullet("La función de entrada suele ser `inline` para que el bloque no cree un")
    bullet("objeto lambda. En un DSL que se usa una vez al arrancar da igual; en uno")
    bullet("que se llama en un bucle, se nota.")

    section("5. Lo que NO debe hacer un DSL")

    bullet("Depender del ORDEN de las líneas salvo que sea evidente (como en HTML).")
    bullet("Aceptar estados a medias y fallar más tarde, lejos del error.")
    bullet("Esconder efectos: un bloque que además escribe en disco o llama a la red.")
    bullet("Inventar símbolos raros para ahorrar tres letras.")

    section("Compara: el mismo correo de las cuatro formas")

    bullet("posicional  → enviarCorreo(\"ana@…\", \"Hola\", \"Qué tal\", true, null)")
    bullet("con nombres → enviarCorreo(para = \"ana@…\", asunto = \"Hola\", …)")
    bullet("encadenado  → CorreoEncadenado().para(\"ana@…\").asunto(\"Hola\")…construir()")
    bullet("DSL         → correo { para = \"ana@…\"; asunto = \"Hola\" }")
    bullet("Ninguna es la mejor siempre. La segunda es la que menos código pide.")
}

// -- El modelo y los cuatro estilos --------------------------------------------------------------

/** El resultado: inmutable, sin valores a medias. */
data class Correo(
    val para: String,
    val asunto: String,
    val cuerpo: String,
    val urgente: Boolean,
    val adjuntos: List<Adjunto>,
) {
    override fun toString(): String = buildString {
        append("Correo(para=$para, asunto='$asunto'")
        if (urgente) append(", URGENTE")
        if (adjuntos.isNotEmpty()) append(", adjuntos=${adjuntos.map { it.nombre }}")
        append(")")
    }
}

data class Adjunto(val nombre: String, val bytes: Int)

/** Estilo 0 y 1: una función con muchos parámetros. */
private fun enviarCorreoPosicional(
    para: String,
    asunto: String,
    cuerpo: String,
    urgente: Boolean = false,
    respuestaA: String? = null,
): String = "para=$para, asunto='$asunto', urgente=$urgente, respuestaA=$respuestaA"

/** Estilo 2: encadenado, como los builders de Java. */
class CorreoEncadenado {
    private var para: String = ""
    private var asunto: String = ""
    private var cuerpo: String = ""
    private var urgente: Boolean = false

    fun para(valor: String): CorreoEncadenado = apply { para = valor }
    fun asunto(valor: String): CorreoEncadenado = apply { asunto = valor }
    fun cuerpo(valor: String): CorreoEncadenado = apply { cuerpo = valor }
    fun urgente(): CorreoEncadenado = apply { urgente = true }

    fun construir(): Correo = Correo(para, asunto, cuerpo, urgente, emptyList())
}

/**
 * Estilo 3: el builder del DSL.
 *
 * El constructor es `internal` para que nadie lo cree por su cuenta: la única puerta
 * de entrada es la función [correo].
 */
class CorreoBuilder internal constructor() {

    var para: String = ""
    var asunto: String = ""
    var cuerpo: String = ""
    var urgente: Boolean = false

    private val adjuntos = mutableListOf<Adjunto>()

    /** Se puede llamar tantas veces como haga falta: eso es lo que un parámetro no da. */
    fun adjuntar(nombre: String, bytes: Int) {
        adjuntos += Adjunto(nombre, bytes)
    }

    /**
     * Aquí se valida: en este punto el bloque ya ha terminado de escribir.
     */
    internal fun construir(): Correo {
        require(para.isNotBlank()) { "el correo necesita un destinatario (`para`)" }
        require(asunto.isNotBlank()) { "el correo necesita un asunto" }
        return Correo(para, asunto, cuerpo, urgente, adjuntos.toList())
    }
}

/**
 * La puerta de entrada del DSL.
 *
 * Tres líneas: crear el builder, aplicarle el bloque como receptor y construir.
 * Ese patrón se repite igual en TODOS los DSL de este capítulo.
 */
fun correo(bloque: CorreoBuilder.() -> Unit): Correo =
    CorreoBuilder().apply(bloque).construir()

/** La versión mínima del patrón, para la primera demo. */
private fun construirTexto(bloque: StringBuilder.() -> Unit): String =
    StringBuilder().apply(bloque).toString()

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `String.() -> Int` por `(String) -> Int` en `conReceptor` y arregla el
//     cuerpo de la lambda: verás exactamente qué aporta el receptor.
//  2. Añade al DSL un bloque anidado `remitente { nombre = ...; correo = ... }`.
//  3. Quita el `require` de `construir()` y comprueba dónde falla entonces el error.
//  4. Haz `inline` la función `correo` y mira el bytecode con Tools → Kotlin →
//     Show Kotlin Bytecode → Decompile.
