@file:JvmName("ApiParaJava")

package com.alejandro.c27javainterop

import com.alejandro.c27javainterop.legacy.ConsumidorDeKotlinJava
import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import java.io.IOException

// =====================================================================================
//  27.3 · El sentido contrario: Kotlin visto desde Java
//
//  QUÉ ES
//    Las anotaciones que controlan qué aspecto tiene tu código Kotlin cuando lo usa
//    alguien desde Java: @JvmName, @JvmStatic, @JvmOverloads, @JvmField y @Throws.
//
//  POR QUÉ IMPORTA
//    Si tu Kotlin lo va a consumir código Java (una librería, un proyecto mixto, un
//    framework), sin estas anotaciones la API resultante es incómoda: clases con
//    sufijo Kt, `Companion` por todas partes y cero valores por defecto.
//
//  CÓMO SE VERIFICA
//    En src/main/java/.../legacy/ConsumidorDeKotlinJava.java hay código Java REAL
//    que usa todo lo de este fichero. Que el proyecto compile es la prueba de que
//    funciona: si quitas cualquiera de estas anotaciones, ese Java deja de compilar.
//
//  ERRORES COMUNES
//    · Publicar una librería Kotlin sin @JvmOverloads y que Java tenga que pasar
//      todos los argumentos siempre.
//    · Olvidar @JvmStatic y obligar a escribir `Clase.Companion.metodo()`.
//    · No poner @Throws y que Java no pueda capturar tu excepción comprobada.
// =====================================================================================

// -- @file:JvmName -----------------------------------------------------------------------------
//
// Sin la anotación de la primera línea, las funciones de nivel superior de este
// fichero vivirían en una clase llamada `C27_03KotlinSideJavaKt`. Con ella, se llama
// `ApiParaJava`, que es bastante más presentable desde Java.

/** Una función de nivel superior. Desde Java: `ApiParaJava.saludar("Java")`. */
fun saludar(nombre: String): String = "Hola, $nombre"

/**
 * Valores por defecto + @JvmOverloads.
 *
 * Kotlin compila UNA sola función con los tres parámetros. Java no tiene valores por
 * defecto, así que tendría que pasarlos siempre. `@JvmOverloads` genera además las
 * sobrecargas intermedias:
 *   conectar(String)
 *   conectar(String, int)
 *   conectar(String, int, boolean)
 */
@JvmOverloads
fun conectar(host: String, puerto: Int = 443, seguro: Boolean = true): String {
    val esquema = if (seguro) "https" else "http"
    return "$esquema://$host:$puerto"
}

/**
 * @Throws declara la excepción PARA JAVA.
 *
 * Kotlin no tiene excepciones comprobadas, así que sin esta anotación el bytecode no
 * lleva la cláusula `throws` y javac cree que esta función nunca lanza IOException.
 * Consecuencia: un `catch (IOException e)` alrededor de ella NO compilaría en Java.
 */
@Throws(IOException::class)
fun validarRuta(ruta: String): String {
    if ("mala" in ruta) throw IOException("ruta inválida: $ruta")
    return "ruta válida: $ruta"
}

// -- Clases con companion object ---------------------------------------------------------------

/**
 * Una clase Kotlin normal, con las anotaciones que la hacen cómoda desde Java.
 */
class ConfiguracionKotlin internal constructor(val nombre: String) {

    /**
     * Con @JvmField, esto es un CAMPO público en el bytecode, no un getter.
     * Desde Java: `configuracion.version` en lugar de `configuracion.getVersion()`.
     */
    @JvmField
    val version: Int = 2

    /** Sin @JvmField: propiedad normal, con getter. Desde Java: `getNombre()`. */
    val descripcion: String get() = "$nombre v$version"

    companion object {
        /** Un `const val` del companion es un campo estático: `ConfiguracionKotlin.PREFIJO`. */
        const val PREFIJO = "cfg"
    }

    override fun toString(): String = "$PREFIJO:$nombre v$version"
}

/**
 * Una fábrica, para ver la diferencia entre tener @JvmStatic y no tenerlo.
 */
class FabricaKotlin private constructor() {

    companion object {
        /** Con @JvmStatic, desde Java: `FabricaKotlin.crear("x")`. */
        @JvmStatic
        fun crear(nombre: String): ConfiguracionKotlin = ConfiguracionKotlin(nombre)

        /** Sin ella, desde Java: `FabricaKotlin.Companion.crearSinJvmStatic("x")`. */
        fun crearSinJvmStatic(nombre: String): ConfiguracionKotlin = ConfiguracionKotlin(nombre)
    }
}

// -- Las demos ---------------------------------------------------------------------------------

/**
 * Cómo se ve cada cosa desde Java.
 */
fun demoJvmAnnotations() {
    section("@file:JvmName")

    bullet("Sin ella:  C27_03KotlinSideJavaKt.saludar(\"Java\")")
    bullet("Con ella:  ApiParaJava.saludar(\"Java\")")
    show("desde Kotlin no cambia nada", saludar("Kotlin"))

    section("@JvmOverloads")

    bullet("Kotlin compila UNA función con tres parámetros.")
    bullet("Java no tiene valores por defecto: tendría que pasarlos todos.")
    bullet("@JvmOverloads genera conectar(String), conectar(String,int) y la completa.")

    show("conectar(\"localhost\")", conectar("localhost"))
    show("conectar(\"localhost\", 8080)", conectar("localhost", 8080))
    show("conectar(\"localhost\", 8080, false)", conectar("localhost", 8080, false))

    section("@JvmStatic")

    bullet("Sin ella:  FabricaKotlin.Companion.crear(\"x\")")
    bullet("Con ella:  FabricaKotlin.crear(\"x\")")
    show("desde Kotlin las dos se llaman igual", FabricaKotlin.crear("desde-kotlin"))

    section("@JvmField")

    val configuracion = FabricaKotlin.crear("demo")
    show("version (es @JvmField)", configuracion.version)
    show("descripcion (propiedad normal)", configuracion.descripcion)

    bullet("Desde Java: `c.version` frente a `c.getDescripcion()`.")
    bullet("@JvmField quita el getter: úsalo sólo en datos simples e inmutables.")

    section("const val en el companion")

    show("ConfiguracionKotlin.PREFIJO", ConfiguracionKotlin.PREFIJO)
    bullet("Desde Java también es `ConfiguracionKotlin.PREFIJO`: un campo estático.")

    section("@Throws")

    show("validarRuta(\"ruta/buena\")", validarRuta("ruta/buena"))
    val fallo = runCatching { validarRuta("ruta/mala") }
    show("validarRuta(\"ruta/mala\")", "lanzó ${fallo.exceptionOrNull()?.javaClass?.simpleName}")
    bullet("Sin @Throws, Java no podría escribir `catch (IOException e)` alrededor.")
}

/**
 * La prueba: código Java real llamando a todo lo anterior.
 */
fun demoJavaCallingKotlin() {
    section("Esto lo ejecuta código Java de verdad")

    bullet("src/main/java/.../legacy/ConsumidorDeKotlinJava.java")
    bullet("Se compila junto con el Kotlin, en el mismo módulo Gradle.")

    section("Funciones de nivel superior")

    show("desde Java", ConsumidorDeKotlinJava.llamarFuncionesDeNivelSuperior())
    bullet("Las tres llamadas a `conectar` sólo existen gracias a @JvmOverloads.")

    section("Companion object")

    show("desde Java", ConsumidorDeKotlinJava.llamarCompanion())
    bullet("La primera usa @JvmStatic; la segunda pasa por `.Companion`.")

    section("Propiedades y campos")

    show("desde Java", ConsumidorDeKotlinJava.leerPropiedades())
    bullet("getNombre() por el getter, .version por el @JvmField, PREFIJO por el const.")

    section("Excepciones")

    show("desde Java", ConsumidorDeKotlinJava.capturarExcepcionDeKotlin())
    bullet("Ese `catch (IOException e)` del lado Java compila gracias a @Throws.")

    section("Por qué esto es una prueba y no una promesa")

    bullet("Si quitas @JvmOverloads, @JvmStatic, @JvmField, @JvmName o @Throws,")
    bullet("el fichero Java DEJA DE COMPILAR y la build falla.")
    bullet("Pruébalo: es la mejor forma de entender qué hace cada una.")
}

/**
 * Buenas prácticas al escribir Kotlin que consumirá Java.
 */
fun demoBestPractices() {
    section("Lo que conviene hacer")

    bullet("@file:JvmName en los ficheros de utilidades, para no tener clases '...Kt'.")
    bullet("@JvmOverloads en las funciones públicas con valores por defecto.")
    bullet("@JvmStatic en las fábricas del companion object.")
    bullet("@Throws en lo que lance excepciones que Java deba capturar.")
    bullet("Anota tus tipos: desde Java, un `String` de Kotlin ya es no nulo, pero")
    bullet("un `String?` se ve como `@Nullable String` sólo si la herramienta lo lee.")

    section("Lo que conviene evitar")

    bullet("`internal`: desde Java SÍ se ve (con el nombre alterado). No es privacidad.")
    bullet("Nombres de función que choquen al borrar genéricos (el error")
    bullet("'platform declaration clash' aparece justo por eso).")
    bullet("Propiedades de extensión: desde Java son funciones estáticas incómodas.")
    bullet("Valores por defecto en constructores sin @JvmOverloads.")

    section("Cuando el proyecto es sólo Kotlin")

    bullet("Nada de esto hace falta. Son anotaciones para la FRONTERA con Java.")
    bullet("Ponerlas 'por si acaso' sólo añade ruido al código.")

    section("Para migrar un proyecto Java a Kotlin")

    bullet("Se puede hacer fichero a fichero: los dos lenguajes conviven.")
    bullet("IntelliJ trae un conversor automático (Code → Convert Java File to Kotlin).")
    bullet("Convierte bien la sintaxis, pero deja `!!` por todas partes: revisa la")
    bullet("nulabilidad a mano después, que es justo lo que el conversor no sabe.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `@file:JvmName("ApiParaJava")` de la primera línea y mira cómo falla la
//     compilación del fichero Java.
//  2. Quita `@JvmOverloads` de `conectar` y comprueba qué tres llamadas Java se rompen.
//  3. Quita `@Throws` de `validarRuta` y lee el error de javac sobre el catch.
//  4. Añade una función Kotlin nueva y úsala desde ConsumidorDeKotlinJava.
