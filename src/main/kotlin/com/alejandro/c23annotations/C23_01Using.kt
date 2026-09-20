package com.alejandro.c23annotations

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  23.1 · Usar anotaciones
//
//  QUÉ ES
//    Metadatos que se adjuntan a una declaración. No cambian el comportamiento por sí
//    mismas: alguien (el compilador, un framework, una herramienta) las lee y actúa.
//
//  POR QUÉ IMPORTA
//    Media plataforma Java funciona con anotaciones (Spring, JPA, JUnit, Jackson), y
//    las de Kotlin son las que controlan cómo se ve tu código desde Java. Saber
//    dónde se aplica cada una evita mucha pelea.
//
//  ERRORES COMUNES
//    · No saber que una anotación sobre una propiedad puede ir al campo, al getter
//      o al parámetro del constructor, y poner el "use-site target" equivocado.
//    · Usar `@Suppress` para tapar un aviso en lugar de arreglarlo.
//    · Olvidar `@JvmStatic`/`@JvmOverloads` y que la API desde Java quede incómoda.
// =====================================================================================

/**
 * Las anotaciones que se usan a diario.
 */
fun demoCommonAnnotations() {
    section("@Deprecated")

    // Marca algo como obsoleto. El IDE lo tacha y puede aplicar el reemplazo solo.
    @Suppress("DEPRECATION")
    val viejo = calcularViejo(10)
    show("función obsoleta", viejo)
    show("la nueva", calcularNuevo(10))

    bullet("@Deprecated(mensaje, ReplaceWith(\"nueva(x)\"), nivel)")
    bullet("`ReplaceWith` permite que Alt+Enter haga la migración automáticamente.")
    bullet("Niveles: WARNING (aviso), ERROR (no compila), HIDDEN (ni se ve).")

    section("@Suppress")

    bullet("Silencia un aviso concreto en el ámbito donde se pone.")
    bullet("Se puede poner en una expresión, una función, una clase o un fichero.")
    bullet("Úsalo cuando SABES que el aviso no aplica, y deja un comentario diciéndolo.")

    section("@JvmStatic, @JvmOverloads, @JvmField, @JvmName")

    bullet("Son las que controlan cómo se ve tu Kotlin desde Java. Capítulo 27.")
    bullet("@JvmStatic  → un método del companion se ve como estático de la clase")
    bullet("@JvmOverloads → genera las sobrecargas de los valores por defecto")
    bullet("@JvmField   → expone un campo público en lugar de getter/setter")
    bullet("@JvmName    → cambia el nombre con que Java ve la función o el fichero")

    show("desde Kotlin no cambia nada", Utilidades.duplicar(21))

    section("@Throws")

    bullet("Declara qué excepciones lanza, PARA JAVA: Kotlin no las comprueba.")
    bullet("Sin ella, Java no puede capturar una excepción comprobada que venga")
    bullet("de una función Kotlin, porque no sabe que existe.")

    section("@Volatile y @Synchronized")

    bullet("Los equivalentes de `volatile` y `synchronized` de Java.")
    bullet("En Kotlin son anotaciones porque el lenguaje no tiene esas palabras clave.")

    section("Anotaciones con y sin paréntesis")

    bullet("Sin argumentos: `@Deprecated` no vale; hace falta `@Deprecated(\"motivo\")`")
    bullet("porque tiene un parámetro obligatorio.")
    bullet("Con un solo argumento llamado `value`, se puede omitir el nombre.")
    bullet("Si es la única anotación de una lambda o expresión, van sin paréntesis.")
}

/**
 * Use-site targets: dónde se aplica realmente.
 */
fun demoUseSiteTargets() {
    section("El problema")

    // Una propiedad de Kotlin genera hasta CUATRO cosas en el bytecode: un campo,
    // un getter, un setter y (si está en el constructor) un parámetro. ¿A cuál va
    // la anotación?
    bullet("val nombre: String  →  campo + getter (+ setter si es var)")
    bullet("Si la anotación es válida en varios sitios, Kotlin elige uno por defecto,")
    bullet("y no siempre es el que quieres.")

    section("El orden de preferencia por defecto")

    bullet("1. param   (si es parámetro del constructor)")
    bullet("2. property (sólo visible desde Kotlin)")
    bullet("3. field")

    section("Los targets explícitos")

    bullet("@field:Anotacion      → al campo")
    bullet("@get:Anotacion        → al getter")
    bullet("@set:Anotacion        → al setter")
    bullet("@param:Anotacion      → al parámetro del constructor")
    bullet("@property:Anotacion   → a la propiedad (sólo Kotlin)")
    bullet("@setparam:Anotacion   → al parámetro del setter")
    bullet("@receiver:Anotacion   → al receptor de una extensión")
    bullet("@delegate:Anotacion   → al campo del delegado")
    bullet("@file:Anotacion       → al fichero entero (va antes del `package`)")

    section("El caso real donde más duele")

    bullet("Con librerías de serialización o inyección que leen ANOTACIONES DE CAMPO,")
    bullet("poner `@Inject val x` puede no funcionar y `@field:Inject val x` sí.")
    bullet("Si una anotación 'no hace nada', lo primero que hay que mirar es el target.")

    show("la clase compila con targets explícitos", Etiquetado("valor").campo)

    section("@file: va antes del package")

    bullet("@file:JvmName(\"UtilidadesTexto\")")
    bullet("package com.ejemplo")
    bullet("Cambia el nombre de la clase fachada que ve Java (capítulo 1.8).")
}

/**
 * Dónde se pueden poner.
 */
fun demoWhereTheyGo() {
    section("Prácticamente en cualquier declaración")

    bullet("clases, interfaces, objetos")
    bullet("funciones y sus parámetros")
    bullet("propiedades, getters y setters")
    bullet("constructores")
    bullet("expresiones y tipos")
    bullet("ficheros completos (@file:)")

    section("En expresiones y lambdas")

    // Anotar una expresión concreta, sin afectar al resto de la función.
    val resultado = @Suppress("UNUSED_EXPRESSION") run {
        val calculado = 6 * 7
        calculado
    }
    show("anotación sobre una expresión", resultado)

    section("En tipos")

    bullet("`val lista: List<@Anotacion String>` anota el argumento de tipo.")
    bullet("Se usa poco fuera de librerías de validación.")

    section("Varias a la vez")

    bullet("Se pueden apilar una debajo de otra, o entre corchetes:")
    bullet("  @[Anotacion1 Anotacion2] fun f() { }")
    bullet("La forma apilada es la habitual; los corchetes casi no se ven.")

    section("Anotaciones repetibles")

    bullet("Con @Repeatable, la misma anotación se puede poner varias veces.")
    bullet("Se usa para reglas acumulativas: varias validaciones sobre un campo.")
}

// -- Lo que usan las demos --------------------------------------------------------------------

@Deprecated(
    message = "usa calcularNuevo, que además valida la entrada",
    replaceWith = ReplaceWith("calcularNuevo(valor)"),
    level = DeprecationLevel.WARNING,
)
private fun calcularViejo(valor: Int): Int = valor * 2

private fun calcularNuevo(valor: Int): Int {
    require(valor >= 0) { "el valor debe ser positivo" }
    return valor * 2
}

/**
 * No es `private` a propósito: `@JvmStatic` sólo tiene sentido en algo que Java
 * pueda ver.
 */
object Utilidades {
    /** Desde Java, `Utilidades.duplicar(x)` en vez de `Utilidades.INSTANCE.duplicar(x)`. */
    @JvmStatic
    fun duplicar(valor: Int): Int = valor * 2
}

/** Ejemplo de targets explícitos sobre las tres piezas de una propiedad. */
private class Etiquetado(
    @param:Marca("va al parámetro del constructor")
    @field:Marca("va al campo")
    @get:Marca("va al getter")
    val campo: String,
)

/** Una anotación mínima, sólo para poder poner los targets del ejemplo. */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
)
private annotation class Marca(val nota: String)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el @Suppress("DEPRECATION") y mira el aviso; prueba Alt+Enter para que
//     IntelliJ aplique el ReplaceWith automáticamente.
//  2. Cambia el nivel de @Deprecated a ERROR y comprueba que ya no compila.
//  3. Quita `AnnotationTarget.FIELD` de @Marca y observa qué target deja de valer.
//  4. Añade `@file:JvmName("MiFichero")` arriba del todo (antes del package).
