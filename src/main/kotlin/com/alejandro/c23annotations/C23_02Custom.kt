package com.alejandro.c23annotations

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

// =====================================================================================
//  23.2 · Anotaciones propias
//
//  QUÉ ES
//    Declarar tus propias anotaciones con `annotation class`, decidir DÓNDE se pueden
//    poner (`@Target`) y HASTA CUÁNDO existen (`@Retention`).
//
//  POR QUÉ IMPORTA
//    Una anotación por sí sola no hace nada: necesita a alguien que la lea. Ese
//    "alguien" suele ser reflexión (capítulo 24) o un procesador de anotaciones. Por
//    eso la retención es la decisión más importante: si no llega a ejecución, la
//    reflexión no la verá.
//
//  ERRORES COMUNES
//    · Dejar la retención por defecto de Java (CLASS) y que la reflexión no la vea.
//      En Kotlin el defecto es RUNTIME, justo al revés. Conviene saberlo.
//    · Poner parámetros que no son constantes de compilación (no está permitido).
//    · Crear una anotación donde una interfaz o un parámetro habrían bastado.
// =====================================================================================

/**
 * Declarar una anotación.
 */
fun demoDeclaring() {
    section("Lo mínimo")

    bullet("annotation class Experimental")
    bullet("Y ya se puede usar: `@Experimental fun f() { }`")

    section("Con parámetros")

    bullet("annotation class Autor(val nombre: String, val anio: Int)")
    bullet("`@Autor(\"Ana\", 2026)`")

    section("Qué tipos admiten los parámetros")

    bullet("Tipos primitivos, String")
    bullet("KClass (`Usuario::class`)")
    bullet("Otras anotaciones")
    bullet("Enums")
    bullet("Arrays de todo lo anterior")
    bullet("Y nada más: deben ser CONSTANTES DE COMPILACIÓN.")

    bullet("Por eso no puedes poner `val fecha: LocalDate` ni una lambda.")

    section("Leyendo la anotación en ejecución")

    val anotacion = ServicioAnotado::class.findAnnotation<Autor>()
    show("@Autor de ServicioAnotado", "${anotacion?.nombre} (${anotacion?.anio})")
    show("¿tiene @Experimental?", ServicioAnotado::class.findAnnotation<Experimental>() != null)

    bullet("`findAnnotation<T>()` viene de kotlin-reflect. Capítulo 24.")
}

/**
 * `@Target`: dónde se puede poner.
 */
fun demoTarget() {
    section("Sin @Target, vale casi en todas partes")

    bullet("Si no lo declaras, la anotación se puede poner en casi cualquier sitio.")
    bullet("Es cómodo al principio y una fuente de confusión después.")

    section("Los targets disponibles")

    bullet("CLASS · ANNOTATION_CLASS · TYPE_PARAMETER")
    bullet("PROPERTY · FIELD · LOCAL_VARIABLE · VALUE_PARAMETER")
    bullet("CONSTRUCTOR · FUNCTION · PROPERTY_GETTER · PROPERTY_SETTER")
    bullet("TYPE · EXPRESSION · FILE · TYPEALIAS")

    section("Restringir dice qué significa la anotación")

    // `@Columna` sólo tiene sentido en una propiedad. Declararlo lo documenta Y lo
    // hace cumplir: ponerla en una función es un error de compilación.
    bullet("@Target(AnnotationTarget.PROPERTY)")
    bullet("annotation class Columna(val nombre: String)")

    val columnas = Usuario::class.memberProperties.mapNotNull { propiedad ->
        propiedad.findAnnotation<Columna>()?.let { "${propiedad.name} → ${it.nombre}" }
    }
    show("columnas declaradas", columnas.sorted())

    bullet("Si intentas `@Columna` sobre una función, el compilador lo rechaza.")
}

/**
 * `@Retention`: hasta cuándo existe.
 */
fun demoRetention() {
    section("Los tres niveles")

    bullet("SOURCE  → sólo para el compilador; desaparece al generar el bytecode")
    bullet("BINARY  → queda en el .class, pero la reflexión NO la ve")
    bullet("RUNTIME → queda en el .class y la reflexión SÍ la ve (el valor por defecto)")

    section("El matiz que hay que saber")

    bullet("En Java, la retención por defecto es CLASS (equivalente a BINARY).")
    bullet("En Kotlin, la retención por defecto es RUNTIME.")
    bullet("Es decir: en Kotlin, si no dices nada, la reflexión SÍ la ve.")
    bullet("Viniendo de Java es fácil asumir lo contrario y perder tiempo.")

    section("Comprobándolo")

    show("@Autor es RUNTIME (por defecto)", ServicioAnotado::class.findAnnotation<Autor>() != null)
    show("@SoloCompilacion es SOURCE", ServicioAnotado::class.findAnnotation<SoloCompilacion>() != null)

    bullet("La segunda devuelve false porque la anotación ya no existe en ejecución.")
    bullet("Estaba en el código y el compilador la vio, pero no llegó al .class.")

    section("Cuál elegir")

    bullet("SOURCE  → para el IDE, para procesadores de anotaciones (KSP/kapt),")
    bullet("          o para documentar. Ejemplos: @Suppress, @DslMarker.")
    bullet("RUNTIME → si algo va a leerla con reflexión: serializadores, inyección")
    bullet("          de dependencias, mapeo a base de datos, validación.")
    bullet("BINARY  → raro; para herramientas que analizan bytecode sin cargarlo.")

    section("@MustBeDocumented y @Repeatable")

    bullet("@MustBeDocumented → aparece en la documentación generada.")
    bullet("@Repeatable       → se puede poner varias veces sobre lo mismo.")
}

/**
 * Un ejemplo completo: un validador minúsculo.
 */
fun demoCompleteExample() {
    section("Las anotaciones")

    bullet("@NoVacio                → el String no puede estar en blanco")
    bullet("@Rango(min, max)        → el Int debe estar dentro")
    bullet("Las dos con @Target(PROPERTY) y retención RUNTIME (por defecto).")

    section("El validador")

    bullet("Recorre las propiedades con reflexión, busca las anotaciones,")
    bullet("y comprueba cada regla. Unas 15 líneas.")

    section("Funcionando")

    val correcto = Formulario(nombre = "Ana", edad = 34, apodo = "")
    show("formulario correcto", validar(correcto))

    val conErrores = Formulario(nombre = "   ", edad = 200, apodo = "cualquier cosa")
    show("formulario con errores", validar(conErrores))

    bullet("`apodo` no lleva anotaciones, así que no se valida: eso es lo que")
    bullet("hace que este enfoque sea declarativo.")

    section("Lo que hay que saber antes de hacer esto en serio")

    bullet("La reflexión es lenta comparada con el código normal. Si validas")
    bullet("millones de objetos, precalcula las reglas una vez por clase.")
    bullet("Y para casos reales ya existen librerías: jakarta.validation, Konform...")
    bullet("El valor de escribirlo es entender qué hacen por dentro.")

    section("Cuándo NO crear una anotación")

    bullet("Si sólo la vas a leer tú en un sitio, un parámetro normal es más simple.")
    bullet("Si el comportamiento cambia según el tipo, una interfaz es más directa")
    bullet("y el compilador la comprueba (una anotación no).")
    bullet("Regla: anotación cuando el dato es DECLARATIVO y lo lee un mecanismo")
    bullet("genérico que no conoce tus clases.")
}

// -- Las anotaciones y tipos que usan las demos ------------------------------------------------

internal annotation class Experimental

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@MustBeDocumented
internal annotation class Autor(val nombre: String, val anio: Int)

/** Desaparece al compilar: la reflexión nunca la verá. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
private annotation class SoloCompilacion

@Autor(nombre = "Alejandro", anio = 2026)
@SoloCompilacion
internal class ServicioAnotado

@Target(AnnotationTarget.PROPERTY)
internal annotation class Columna(val nombre: String)

internal class Usuario(
    @property:Columna("usuario_id") val id: Int,
    @property:Columna("nombre_completo") val nombre: String,
    val noPersistido: String = "",
)

// -- El validador declarativo --------------------------------------------------------------

@Target(AnnotationTarget.PROPERTY)
internal annotation class NoVacio

@Target(AnnotationTarget.PROPERTY)
internal annotation class Rango(val minimo: Int, val maximo: Int)

internal class Formulario(
    @property:NoVacio val nombre: String,
    @property:Rango(0, 130) val edad: Int,
    val apodo: String,
)

/**
 * Recorre las propiedades del objeto, busca las anotaciones y aplica las reglas.
 *
 * `memberProperties` y `findAnnotation` necesitan `kotlin-reflect`, que este proyecto
 * ya tiene como dependencia (ver build.gradle.kts).
 */
private fun validar(objeto: Any): String {
    val errores = objeto::class.memberProperties.mapNotNull { propiedad ->
        val valor = propiedad.getter.call(objeto)

        when {
            propiedad.findAnnotation<NoVacio>() != null && (valor as? String).isNullOrBlank() ->
                "${propiedad.name}: no puede estar vacío"

            else -> propiedad.findAnnotation<Rango>()?.let { rango ->
                val numero = valor as? Int
                if (numero == null || numero !in rango.minimo..rango.maximo) {
                    "${propiedad.name}: debe estar entre ${rango.minimo} y ${rango.maximo}"
                } else {
                    null
                }
            }
        }
    }

    return if (errores.isEmpty()) "válido" else errores.sorted().joinToString("; ")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia la retención de @Autor a SOURCE y comprueba que la reflexión deja de verla.
//  2. Pon `@Columna` sobre una función y lee el error del @Target.
//  3. Añade `@Email` al validador, con su regla correspondiente.
//  4. Quita el `@property:` de las anotaciones de Formulario y mira si sigue validando
//     (pista: el target por defecto de un parámetro del constructor es `param`).
