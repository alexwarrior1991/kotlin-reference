package com.alejandro.c27javainterop

import com.alejandro.c27javainterop.legacy.ProcesadorJava
import com.alejandro.c27javainterop.legacy.TextoUtilJava
import com.alejandro.c27javainterop.legacy.UsuarioJava
import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  27.1 · Llamar a Java desde Kotlin
//
//  QUÉ ES
//    Usar clases Java desde Kotlin sin ninguna ceremonia: se importan y se usan.
//    Kotlin además las "kotliniza" un poco por el camino.
//
//  POR QUÉ IMPORTA
//    Toda la plataforma JVM es Java: la biblioteca estándar, Spring, Hibernate,
//    los drivers de base de datos. Sin interoperabilidad de verdad, Kotlin no
//    serviría para nada.
//
//  ERRORES COMUNES
//    · Llamar a `getNombre()` en vez de usar la propiedad `nombre`.
//    · No darse cuenta de que un método Java puede devolver null aunque Kotlin
//      no lo exija (demo 27.2).
//    · Pelearse con los tipos "raw" de Java antiguo.
//
//  NOTA
//    Las clases Java de este capítulo están en src/main/java/.../legacy/ y se
//    compilan junto con el Kotlin, sin ninguna configuración extra en Gradle.
// =====================================================================================

/**
 * Getters y setters se ven como propiedades.
 */
fun demoPropertyAccess() {
    section("Un POJO de Java desde Kotlin")

    val usuario = UsuarioJava("Ana", 34)

    // `getNombre()` y `setNombre(...)` se usan como si fueran una propiedad.
    show("usuario.nombre  (llama a getNombre())", usuario.nombre)
    show("usuario.edad    (llama a getEdad())", usuario.edad)

    usuario.nombre = "Ana María"        // llama a setNombre(...)
    show("tras usuario.nombre = ...", usuario.nombre)

    bullet("Kotlin detecta el patrón JavaBean (getX/setX) y lo expone como propiedad.")
    bullet("Se puede seguir llamando a `usuario.getNombre()`, pero no es idiomático.")

    section("Sólo funciona con la convención JavaBean")

    // `obtenerResumen()` no empieza por get/is, así que sigue siendo una función.
    show("usuario.obtenerResumen()", usuario.obtenerResumen())
    bullet("Un método que no se llama getX() o isX() se queda como función normal.")

    section("Métodos que sí son funciones")

    show("usuario.descripcion()", usuario.descripcion())
}

/**
 * Miembros estáticos y constantes.
 */
fun demoStatics() {
    section("Métodos estáticos: se llaman sin más")

    show("TextoUtilJava.enMayusculas(\"kotlin\")", TextoUtilJava.enMayusculas("kotlin"))
    show("TextoUtilJava.recortar(\"ab\")", TextoUtilJava.recortar("ab"))
    show("TextoUtilJava.recortar(null)", TextoUtilJava.recortar(null))
    show(
        "recortar un texto largo",
        TextoUtilJava.recortar("un texto bastante más largo de la cuenta"),
    )

    bullet("Los estáticos de Java se llaman igual que en Java: Clase.metodo().")
    bullet("No hay `Companion` de por medio: eso es sólo en el sentido contrario.")

    section("Constantes")

    show("TextoUtilJava.SEPARADOR", "'${TextoUtilJava.SEPARADOR}'")
    show("TextoUtilJava.LONGITUD_MAXIMA", TextoUtilJava.LONGITUD_MAXIMA)

    section("Varargs de Java")

    show("unir(\"a\", \"b\", \"c\")", TextoUtilJava.unir("a", "b", "c"))

    // El operador de propagación funciona igual que con un vararg de Kotlin.
    val partes = arrayOf("uno", "dos", "tres")
    show("unir(*partes)", TextoUtilJava.unir(*partes))

    bullet("Un `String...` de Java es un `vararg` de Kotlin a todos los efectos.")
    bullet("Hace falta un Array, no una List: `lista.toTypedArray()` si tienes una.")

    show("desde una List", TextoUtilJava.unir(*listOf("x", "y").toTypedArray()))
}

/**
 * Conversión SAM: pasar lambdas a interfaces Java.
 */
fun demoSamConversion() {
    section("El problema en Java")

    bullet("procesador.procesar(\"x\", new Transformacion() {")
    bullet("    public String aplicar(String e) { return e.toUpperCase(); }")
    bullet("});")

    section("En Kotlin: una lambda")

    val procesador = ProcesadorJava("proc")

    // `Transformacion` es una interfaz Java con un único método abstracto (SAM).
    // Kotlin convierte la lambda automáticamente.
    show("procesar { it.uppercase() }", procesador.procesar("kotlin") { it.uppercase() })
    show("procesar { it.reversed() }", procesador.procesar("kotlin") { it.reversed() })

    section("Con dos parámetros")

    show(
        "combinar { a, b -> ... }",
        procesador.combinar("uno", "dos") { a, b -> "$a+$b" },
    )

    section("También se puede escribir el tipo explícitamente")

    val transformacion = ProcesadorJava.Transformacion { entrada -> "[$entrada]" }
    show("SAM constructor explícito", procesador.procesar("kotlin", transformacion))

    bullet("`Interfaz { ... }` es el 'SAM constructor': útil cuando quieres guardar")
    bullet("la implementación en una variable o pasarla a varios sitios.")

    section("El matiz: sólo para interfaces de JAVA")

    bullet("La conversión SAM automática funciona con interfaces Java.")
    bullet("Para una interfaz de KOTLIN hay que declararla `fun interface`.")
    bullet("Motivo: en Kotlin lo normal es usar un tipo función `(String) -> String`,")
    bullet("que ya es una lambda sin necesidad de interfaz.")

    show("con una fun interface de Kotlin", aplicar("kotlin") { it.uppercase() })
}

/**
 * Excepciones comprobadas y arrays.
 */
fun demoCheckedExceptionsAndArrays() {
    section("Kotlin NO te obliga a capturar las checked exceptions")

    val procesador = ProcesadorJava("proc")

    // `leerRecurso` declara `throws IOException`. En Java habría que capturarla
    // o declararla. En Kotlin, esto compila tal cual:
    show("leerRecurso(\"datos.txt\")", procesador.leerRecurso("datos.txt"))

    // Pero la excepción SIGUE EXISTIENDO. Si no la tratas, se propaga.
    val fallo = try {
        procesador.leerRecurso("error.txt")
    } catch (e: java.io.IOException) {
        "lanzó IOException: ${e.message}"
    }
    show("leerRecurso(\"error.txt\")", fallo)

    bullet("Ésta es la cara B de no tener checked exceptions: nadie te avisa de que")
    bullet("`leerRecurso` puede fallar. Hay que leer la documentación.")
    bullet("El IDE sí lo muestra al pasar el ratón: fíjate en el `throws` del KDoc.")

    section("Arrays de Java")

    val elementos = arrayOf("a", "b", "c")
    show("contar(arrayOf(...))", procesador.contar(elementos))

    // Un `List` de Kotlin NO es un array de Java: hay que convertir.
    val lista = listOf("x", "y")
    show("desde una List", procesador.contar(lista.toTypedArray()))

    bullet("`Array<String>` de Kotlin es `String[]` de Java: son lo mismo.")
    bullet("`List<String>` es `java.util.List<String>`: NO es un array.")

    section("Tipos 'raw' de Java antiguo")

    // Un `List` sin genéricos llega como `List<Any!>!`: se puede usar, pero pierdes
    // toda la seguridad de tipos.
    val raw = TextoUtilJava.sinGenericos()
    show("sinGenericos()", raw)
    show("su primer elemento", raw.first())
    show("¿de qué tipo es cada uno?", raw.map { it?.let { v -> v::class.simpleName } })

    val tipada = TextoUtilJava.conGenericos()
    show("conGenericos()", tipada)
    show("aquí sí se puede usar como String", tipada.map { it.uppercase() })

    bullet("Con un tipo raw, Kotlin no puede ayudarte: todo es `Any!`.")
    bullet("Si escribes Java que va a usarse desde Kotlin, usa SIEMPRE genéricos.")
}

/** Una `fun interface` de Kotlin: habilita la conversión SAM en el lado Kotlin. */
private fun interface TransformacionKotlin {
    fun aplicar(entrada: String): String
}

private fun aplicar(texto: String, transformacion: TransformacionKotlin): String =
    transformacion.aplicar(texto)

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Llama a `usuario.getNombre()` en vez de `usuario.nombre`: compila, pero el IDE
//     te sugiere la forma idiomática.
//  2. Quita `fun` de `fun interface TransformacionKotlin` y mira qué deja de compilar.
//  3. Pasa una `List` directamente a `procesador.contar(...)` y lee el error.
//  4. Añade un método estático nuevo a TextoUtilJava y úsalo desde aquí.
