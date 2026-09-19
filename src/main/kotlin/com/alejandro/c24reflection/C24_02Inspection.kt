package com.alejandro.c24reflection

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

// =====================================================================================
//  24.2 · Inspección con kotlin-reflect, y cuándo NO usar reflexión
//
//  QUÉ ES
//    La API pesada: recorrer propiedades, leer el constructor, crear instancias,
//    buscar anotaciones. Todo esto necesita la dependencia `kotlin-reflect`.
//
//  POR QUÉ IMPORTA
//    Es cómo funcionan por dentro los serializadores, los frameworks de inyección y
//    los mapeadores a base de datos. Entenderlo desmitifica la "magia" de Spring o
//    Jackson. Y saber lo que cuesta ayuda a no usarla donde no toca.
//
//  ERRORES COMUNES
//    · Olvidar la dependencia y encontrarse el fallo en producción, no al compilar.
//    · Usar reflexión en un bucle caliente sin cachear nada.
//    · Resolver con reflexión algo que una interfaz resolvía mejor.
// =====================================================================================

internal data class Producto(
    val id: Int,
    val nombre: String,
    val precioCentimos: Int,
    val disponible: Boolean,
)

@Target(AnnotationTarget.PROPERTY)
internal annotation class NoSerializar

internal data class UsuarioConSecreto(
    val nombre: String,
    val email: String,
    @property:NoSerializar val contrasena: String,
)

internal class SinArgumentos {
    var configurado: String = "por defecto"
}

/**
 * Recorrer las propiedades.
 */
fun demoMemberProperties() {
    section("Listar las propiedades de una clase")

    val nombres = Producto::class.memberProperties.map { it.name }.sorted()
    show("nombres de las propiedades", nombres)

    section("Y leer sus valores en un objeto concreto")

    val producto = Producto(7, "Teclado", 4_999, true)

    Producto::class.memberProperties.sortedBy { it.name }.forEach { propiedad ->
        show(propiedad.name, propiedad.get(producto))
    }

    bullet("`memberProperties` necesita kotlin-reflect. Sin la dependencia, esto")
    bullet("lanza KotlinReflectionNotSupportedError EN EJECUCIÓN.")

    section("Con el tipo de cada una")

    Producto::class.memberProperties.sortedBy { it.name }.forEach { propiedad ->
        show(propiedad.name, propiedad.returnType.toString())
    }

    section("declaredMemberProperties frente a memberProperties")

    bullet("`memberProperties`         → las propias Y las heredadas")
    bullet("`declaredMemberProperties` → sólo las declaradas en esta clase")

    section("Funciones")

    val funciones = Producto::class.declaredMemberFunctions.map { it.name }.sorted()
    show("funciones declaradas", funciones)
    bullet("Una data class declara copy, componentN, equals, hashCode y toString.")
}

/**
 * El constructor.
 */
fun demoConstructor() {
    section("Leer el constructor primario")

    val constructor = Producto::class.primaryConstructor
    show("nº de parámetros", constructor?.parameters?.size)

    constructor?.parameters?.forEach { parametro ->
        show("parámetro ${parametro.index}", "${parametro.name}: ${parametro.type}")
    }

    bullet("`parameters` incluye el nombre, el tipo y si tiene valor por defecto.")
    bullet("Es lo que permite a un serializador reconstruir el objeto desde un JSON.")

    section("Crear una instancia sin argumentos")

    val instancia = SinArgumentos::class.createInstance()
    show("createInstance()", instancia.configurado)

    bullet("`createInstance()` sólo funciona si TODOS los parámetros tienen valor")
    bullet("por defecto o no hay ninguno.")

    val fallo = try {
        Producto::class.createInstance().toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException (Producto necesita argumentos)"
    }
    show("Producto::class.createInstance()", fallo)

    section("Crear pasando los argumentos por nombre")

    val porNombre = constructor?.let { ctor ->
        val argumentos = ctor.parameters.associateWith { parametro ->
            when (parametro.name) {
                "id" -> 99
                "nombre" -> "Creado por reflexión"
                "precioCentimos" -> 1_000
                "disponible" -> false
                else -> null
            }
        }
        ctor.callBy(argumentos)
    }
    show("callBy(mapa de argumentos)", porNombre)

    bullet("`callBy` es lo que usan los deserializadores: van rellenando por nombre.")
    bullet("Y respeta los valores por defecto de los parámetros que no pases.")
}

/**
 * Un mini-serializador.
 */
fun demoMiniSerializer() {
    section("De objeto a texto, sin saber nada del tipo")

    val producto = Producto(7, "Teclado", 4_999, true)
    show("serializar(producto)", serializar(producto))

    val usuario = UsuarioConSecreto("Ana", "ana@ejemplo.com", "supersecreta")
    show("serializar(usuario)", serializar(usuario))

    bullet("La contraseña no aparece: lleva @NoSerializar y el serializador la salta.")
    bullet("Es exactamente el mecanismo de @JsonIgnore de Jackson o @Transient de JPA.")

    section("Funciona con cualquier clase")

    show("con otra clase distinta", serializar(Punto(3, 4)))

    bullet("El serializador no conoce Producto, ni Usuario, ni Punto.")
    bullet("Ésa es toda la gracia de la reflexión: código genérico de verdad.")

    section("Y por eso mismo tiene un coste")

    bullet("En cada llamada: leer los metadatos, recorrer las propiedades, invocar")
    bullet("los getters uno a uno. Es órdenes de magnitud más lento que `toString()`.")
    bullet("Por eso kotlinx.serialization genera el código AL COMPILAR en lugar de")
    bullet("usar reflexión: mismo resultado declarativo, sin coste en ejecución.")
}

/**
 * Los límites y cuándo no usarla.
 */
fun demoLimitsAndAlternatives() {
    section("1. Hace falta la dependencia, y el fallo es en EJECUCIÓN")

    bullet("implementation(kotlin(\"reflect\"))  ← en build.gradle.kts")
    bullet("Sin ella, `::class.simpleName` funciona pero `memberProperties` revienta.")
    bullet("El compilador no avisa: es un error de ejecución, y suele aparecer tarde.")

    section("2. El borrado de tipos sigue ahí")

    bullet("`List<String>` y `List<Int>` son la misma clase en ejecución (cap. 12.12).")
    bullet("La reflexión de Kotlin SÍ puede leer los tipos genéricos declarados en")
    bullet("la firma (`returnType.arguments`), porque están en los metadatos.")

    val tipoDeLista = ConGenericos::class.memberProperties.first { it.name == "numeros" }
    show("tipo declarado de `numeros`", tipoDeLista.returnType.toString())
    bullet("Pero de un VALOR concreto en ejecución, no: ahí el tipo está borrado.")

    section("3. Es lenta")

    bullet("Leer los metadatos de Kotlin y resolver miembros cuesta mucho más que")
    bullet("una llamada normal. En un bucle de millones de vueltas, se nota.")
    bullet("Mitigación: calcula las propiedades UNA vez por clase y guárdalas.")

    section("4. Rompe las garantías del compilador")

    bullet("Con reflexión puedes leer propiedades privadas, saltarte la validación")
    bullet("de un setter y crear objetos en estados imposibles.")
    bullet("El compilador deja de poder ayudarte. Un renombrado que el IDE haría")
    bullet("sin problema rompe el código que busca por cadena de texto.")

    section("Las alternativas, por orden de preferencia")

    bullet("1. Una interfaz: el compilador la comprueba y no cuesta nada.")
    bullet("2. Una `sealed` + `when` exhaustivo: cubre los casos conocidos.")
    bullet("3. Generación de código al compilar (KSP, kotlinx.serialization).")
    bullet("4. Reflexión: sólo cuando el conjunto de tipos NO se conoce al compilar.")

    section("Cuándo la reflexión es la respuesta correcta")

    bullet("Serializadores y mapeadores genéricos.")
    bullet("Inyección de dependencias.")
    bullet("Frameworks de test que descubren los métodos de prueba.")
    bullet("Herramientas de desarrollo: inspectores, consolas, depuradores.")
    bullet("Fíjate en el patrón: todos son LIBRERÍAS que no conocen tus clases.")
    bullet("En código de aplicación, casi siempre hay algo mejor.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

internal data class Punto(val x: Int, val y: Int)

internal class ConGenericos {
    val numeros: List<Int> = listOf(1, 2, 3)
}

/**
 * Serializa cualquier objeto recorriendo sus propiedades, saltando las marcadas
 * con `@NoSerializar`.
 */
private fun serializar(objeto: Any): String {
    val clase: KClass<*> = objeto::class

    val campos = clase.memberProperties
        .filter { it.findAnnotation<NoSerializar>() == null }
        .sortedBy { it.name }
        .joinToString(", ") { propiedad ->
            val valor = propiedad.getter.call(objeto)
            val texto = if (valor is String) "\"$valor\"" else valor.toString()
            "\"${propiedad.name}\": $texto"
        }

    return "{$campos}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `implementation(kotlin("reflect"))` de build.gradle.kts y ejecuta este
//     capítulo: verás el KotlinReflectionNotSupportedError en ejecución.
//  2. Añade `@NoSerializar` al email y comprueba que desaparece del JSON.
//  3. Escribe el deserializador inverso usando `callBy`.
//  4. Cachea `clase.memberProperties` en un Map y compara los tiempos con 100.000
//     llamadas a `serializar`.
