package com.alejandro.c29dsl

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  29.2 · @DslMarker: cerrar los receptores anidados
//
//  QUÉ ES
//    Una anotación que impide llamar, sin nombrarlo, a un receptor EXTERIOR desde
//    dentro de un bloque anidado.
//
//  POR QUÉ IMPORTA
//    Al anidar bloques con receptor, los receptores se acumulan: dentro del bloque
//    interior siguen visibles los métodos del exterior. Eso convierte una errata en
//    código que COMPILA y hace algo distinto de lo que parece.
//
//  ERRORES COMUNES
//    · Publicar un DSL anidado sin `@DslMarker` (el fallo aparece en producción).
//    · Anotar sólo una de las clases del DSL: hay que anotarlas TODAS, o mejor,
//      anotar una interfaz o clase base común.
//    · Creer que impide también el acceso explícito: `this@exterior.x()` sigue valiendo,
//      y está bien que así sea.
// =====================================================================================

/**
 * El problema, sin la anotación.
 */
fun demoScopeLeak() {
    section("Un DSL anidado de dos niveles")

    bullet("menu { seccion(\"…\") { plato(\"…\", 5.0) } }")
    bullet("Dentro de `seccion { }` hay DOS receptores implícitos: el de la sección")
    bullet("(el más cercano) y, por detrás, el del menú entero.")

    section("La errata que compila")

    val conFuga = menuSinMarcador {
        nota("Menú del día")

        seccion("Entrantes") {
            plato("Sopa", 5.0)
            plato("Ensalada", 6.5)

            // ¡AQUÍ! `nota` no existe en SeccionBuilder, así que Kotlin sube al
            // receptor exterior y se la añade AL MENÚ, no a la sección.
            nota("sin gluten")
        }

        seccion("Postres") {
            plato("Flan", 4.0)
        }
    }

    show("notas de la SECCIÓN 'Entrantes'", conFuga.secciones.first().notas)
    show("notas del MENÚ", conFuga.notas)

    bullet("La nota 'sin gluten' quería ser de los entrantes y ha acabado en el menú.")
    bullet("El compilador no se ha quejado: es una resolución de nombres perfectamente")
    bullet("legal. Por eso el error sólo se ve mirando la salida.")

    section("Por qué pasa")

    bullet("Dentro de una lambda con receptor, `this` implícito es el más CERCANO,")
    bullet("pero si un nombre no existe allí, la búsqueda sigue hacia fuera.")
    bullet("Es la misma regla que con las variables de un bloque anidado, sólo que")
    bullet("aquí los 'bloques' son objetos distintos y la confusión es mucho más fácil.")

    section("El caso todavía peor")

    val anidadoRaro = menuSinMarcador {
        seccion("Entrantes") {
            plato("Sopa", 5.0)
            // Nada impide abrir otra sección DENTRO de una sección: el receptor
            // exterior sigue ahí y `seccion` es suyo.
            seccion("¿Una sección dentro de otra?") {
                plato("Croquetas", 7.0)
            }
        }
    }
    show("secciones del menú", anidadoRaro.secciones.map { it.nombre })

    bullet("La sección 'de dentro' es en realidad hermana de la de fuera.")
    bullet("Y fíjate en el ORDEN: sale antes que 'Entrantes', porque se añadió al")
    bullet("menú mientras el bloque de 'Entrantes' todavía se estaba ejecutando.")
    bullet("El DSL admite algo que no tiene sentido en su modelo.")
}

/**
 * La solución.
 */
fun demoDslMarker() {
    section("La anotación")

    bullet("@DslMarker")
    bullet("@Retention(AnnotationRetention.BINARY)")
    bullet("@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)")
    bullet("annotation class MenuDsl")
    bullet("")
    bullet("Y luego se marca CADA clase del DSL con `@MenuDsl`.")

    section("Qué cambia")

    bullet("Con las dos clases marcadas, esto YA NO COMPILA:")
    bullet("")
    bullet("  menu {")
    bullet("      seccion(\"Entrantes\") {")
    bullet("          nota(\"sin gluten\")   // ← error de compilación")
    bullet("      }")
    bullet("  }")
    bullet("")
    bullet("El mensaje es claro: 'can't be called in this context by implicit")
    bullet("receiver. Use the explicit one if necessary'.")

    section("El DSL marcado, funcionando")

    val seguro = menu {
        nota("Menú del día")

        seccion("Entrantes") {
            plato("Sopa", 5.0)
            plato("Ensalada", 6.5)
            notaDeSeccion("sin gluten")      // el método correcto, el de la sección
        }

        seccion("Postres") {
            plato("Flan", 4.0)
        }
    }

    show("notas de 'Entrantes'", seguro.secciones.first().notas)
    show("notas del menú", seguro.notas)
    show("total de platos", seguro.secciones.sumOf { it.platos.size })

    bullet("Ahora cada nota está donde tiene que estar, y el compilador lo garantiza.")

    section("El acceso explícito sigue permitido")

    val conAccesoExplicito = menu {
        seccion("Entrantes") {
            plato("Sopa", 5.0)
            // Si DE VERDAD quieres tocar el receptor exterior, se nombra y ya está.
            this@menu.nota("nota del menú escrita desde dentro de una sección")
        }
    }
    show("notas del menú", conAccesoExplicito.notas)

    bullet("`@DslMarker` no prohíbe: OBLIGA A SER EXPLÍCITO. La intención queda escrita.")

    section("Cómo aplicarlo en la práctica")

    bullet("1. Declara la anotación una vez por DSL (no una por clase).")
    bullet("2. Márcala con retención BINARY: es lo que espera `@DslMarker`.")
    bullet("3. Anota TODAS las clases receptoras del DSL. Si olvidas una, la fuga")
    bullet("   vuelve por ahí.")
    bullet("4. Truco: crea una clase base o interfaz marcada y haz que todas hereden")
    bullet("   de ella; así no se te olvida ninguna.")

    section("Cuándo NO hace falta")

    bullet("Si tu DSL tiene un solo nivel (como el `correo { }` de la demo anterior),")
    bullet("no hay receptores anidados y `@DslMarker` no cambia nada.")
    bullet("En cuanto aparezca el segundo nivel, ponla. Cuesta cuatro líneas.")
}

// -- Versión SIN marcador, para que se vea la fuga ------------------------------------------------

class MenuSinMarcador(val notas: List<String>, val secciones: List<SeccionSinMarcador>)
class SeccionSinMarcador(val nombre: String, val platos: List<Plato>, val notas: List<String>)

data class Plato(val nombre: String, val precio: Double)

class MenuBuilderSinMarcador internal constructor() {
    private val notas = mutableListOf<String>()
    private val secciones = mutableListOf<SeccionSinMarcador>()

    fun nota(texto: String) {
        notas += texto
    }

    fun seccion(nombre: String, bloque: SeccionBuilderSinMarcador.() -> Unit) {
        secciones += SeccionBuilderSinMarcador(nombre).apply(bloque).construir()
    }

    internal fun construir() = MenuSinMarcador(notas.toList(), secciones.toList())
}

class SeccionBuilderSinMarcador internal constructor(private val nombre: String) {
    private val platos = mutableListOf<Plato>()
    private val notas = mutableListOf<String>()

    fun plato(nombre: String, precio: Double) {
        platos += Plato(nombre, precio)
    }

    fun notaDeSeccion(texto: String) {
        notas += texto
    }

    internal fun construir() = SeccionSinMarcador(nombre, platos.toList(), notas.toList())
}

fun menuSinMarcador(bloque: MenuBuilderSinMarcador.() -> Unit): MenuSinMarcador =
    MenuBuilderSinMarcador().apply(bloque).construir()

// -- Versión CON marcador ------------------------------------------------------------------------

/**
 * La anotación del DSL.
 *
 * `@DslMarker` es una meta-anotación: marca a OTRA anotación como delimitadora de
 * ámbito. Lo que hace el compilador es, dentro de un bloque cuyo receptor está
 * marcado con `@MenuDsl`, ocultar los demás receptores implícitos marcados igual.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class MenuDsl

class Menu(val notas: List<String>, val secciones: List<Seccion>) {
    val platos: List<Plato> get() = secciones.flatMap { it.platos }
    val precioTotal: Double get() = platos.sumOf { it.precio }
}

class Seccion(val nombre: String, val platos: List<Plato>, val notas: List<String>)

@MenuDsl
class MenuBuilder internal constructor() {
    private val notas = mutableListOf<String>()
    private val secciones = mutableListOf<Seccion>()

    fun nota(texto: String) {
        notas += texto
    }

    fun seccion(nombre: String, bloque: SeccionBuilder.() -> Unit) {
        secciones += SeccionBuilder(nombre).apply(bloque).construir()
    }

    internal fun construir() = Menu(notas.toList(), secciones.toList())
}

@MenuDsl
class SeccionBuilder internal constructor(private val nombre: String) {
    private val platos = mutableListOf<Plato>()
    private val notas = mutableListOf<String>()

    fun plato(nombre: String, precio: Double) {
        platos += Plato(nombre, precio)
    }

    fun notaDeSeccion(texto: String) {
        notas += texto
    }

    internal fun construir() = Seccion(nombre, platos.toList(), notas.toList())
}

fun menu(bloque: MenuBuilder.() -> Unit): Menu =
    MenuBuilder().apply(bloque).construir()

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe `nota("x")` dentro de un `seccion { }` del DSL marcado y lee el error.
//  2. Quita `@MenuDsl` de `SeccionBuilder` (dejándola en MenuBuilder) y comprueba que
//     la fuga vuelve: hay que anotarlas todas.
//  3. Sustituye `this@menu.nota(...)` por `nota(...)` y observa la diferencia.
//  4. Añade un tercer nivel (`plato("...") { alergeno("gluten") }`) y comprueba que
//     `@DslMarker` sigue cerrando los dos niveles superiores.
