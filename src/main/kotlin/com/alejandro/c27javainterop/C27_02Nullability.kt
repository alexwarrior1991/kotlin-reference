package com.alejandro.c27javainterop

import com.alejandro.c27javainterop.legacy.TextoUtilJava
import com.alejandro.c27javainterop.legacy.UsuarioJava
import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  27.2 · Platform types: el agujero de la seguridad frente a nulos
//
//  QUÉ ES
//    Cuando un valor viene de Java sin anotaciones de nulabilidad, Kotlin no sabe si
//    puede ser null. En vez de suponer, crea un tipo especial `String!` que significa
//    "String o String?, tú decides".
//
//  POR QUÉ IMPORTA
//    Es el único sitio donde la garantía de Kotlin frente a los nulos se rompe. Si
//    trabajas con librerías Java sin anotar, aquí es donde aparecen los NPE que
//    Kotlin "no debería" permitir.
//
//  ERRORES COMUNES
//    · Asumir que todo lo que viene de Java es no nulo.
//    · No declarar el tipo al recibir el valor, que es la forma de protegerse.
//    · Culpar a Kotlin del NPE cuando el contrato lo rompió la librería Java.
// =====================================================================================

/**
 * Qué es un platform type.
 */
fun demoPlatformTypes() {
    section("El dilema de Kotlin")

    bullet("Java no distingue String de String?. Cualquier referencia puede ser null.")
    bullet("Opción A: tratar TODO lo de Java como nulable → `?.` en cada llamada a")
    bullet("           cada librería. Insoportable.")
    bullet("Opción B: tratar todo como no nulo → NPE constantes y silenciosos.")
    bullet("Kotlin eligió una tercera: el tipo plataforma `String!`.")

    section("Qué significa `String!`")

    bullet("«Yo no sé si esto puede ser null; decídelo tú.»")
    bullet("No te obliga a comprobar, pero tampoco te garantiza nada.")
    bullet("NUNCA lo escribes: sólo aparece en los mensajes de error y en el IDE.")

    section("En funcionamiento")

    val usuario = UsuarioJava("Ana", 34)

    // `getNombre()` no está anotado → `String!`. Kotlin deja usarlo directamente.
    val nombre = usuario.nombre
    show("usuario.nombre.length (sin ?.)", nombre.length)
    show("usuario.nombre.uppercase()", nombre.uppercase())

    bullet("Ni un solo `?.` y compila. Porque Kotlin no sabe que podría ser null.")

    section("Y aquí está la trampa")

    // `buscarSobrenombre()` devuelve null SIEMPRE, pero no está anotado.
    val sobrenombre = usuario.buscarSobrenombre()

    val explota = try {
        // Esto compila perfectamente... y revienta.
        sobrenombre.length.toString()
    } catch (e: NullPointerException) {
        "lanzó NullPointerException"
    }
    show("buscarSobrenombre().length", explota)

    bullet("Compiló sin un solo aviso y falló en ejecución.")
    bullet("Es EXACTAMENTE lo que Kotlin evita en su propio código, y no puede")
    bullet("evitar cuando el dato viene de Java sin anotar.")
}

/**
 * Cómo protegerse.
 */
fun demoProtecting() {
    val usuario = UsuarioJava("Ana", 34)

    section("1. Declara el tipo al recibir el valor")

    // Al escribir `String?`, Kotlin deja de tratarlo como platform type y empieza
    // a exigirte que lo compruebes. Es la protección más simple y más efectiva.
    val comoNulable: String? = usuario.buscarSobrenombre()
    show("val x: String? = ...", comoNulable)
    show("y ahora obliga a usar ?.", comoNulable?.length)
    show("con valor por defecto", comoNulable?.length ?: 0)

    bullet("Una sola anotación de tipo convierte el agujero en una comprobación.")
    bullet("Hazlo en la FRONTERA: donde el dato entra desde Java a tu código.")

    section("2. Y si declaras el tipo NO nulable, falla al instante")

    // Kotlin inserta una comprobación en la asignación: el NPE ocurre aquí, no
    // tres capas más adentro.
    val fallaPronto = try {
        @Suppress("UNUSED_VARIABLE")
        val comoNoNulable: String = usuario.buscarSobrenombre()
        "no falló"
    } catch (e: NullPointerException) {
        "lanzó NPE en la ASIGNACIÓN, no más tarde"
    }
    show("val x: String = ...", fallaPronto)

    bullet("Esto es lo que se llama 'fallar pronto': el error aparece donde está")
    bullet("la causa, no donde se manifiesta la consecuencia.")

    section("3. Usa librerías Java anotadas")

    // Si el método SÍ está anotado, Kotlin lo respeta y ya no hay platform type.
    val apodo: String? = usuario.apodo       // @Nullable → String?
    show("usuario.apodo (está @Nullable)", apodo)
    show("obliga a comprobarlo", apodo?.uppercase() ?: "(sin apodo)")

    val descripcion: String = usuario.descripcion()   // @NotNull → String
    show("usuario.descripcion() (está @NotNull)", descripcion)
    show("no hace falta ?. ni !!", descripcion.uppercase())

    bullet("Kotlin entiende @Nullable/@NotNull de JetBrains, JSR-305, Android,")
    bullet("Jakarta, Lombok y varias más. Si la librería anota, tú ganas.")

    section("4. Envuelve la API de Java en una capa Kotlin")

    // El patrón de la frontera: una función Kotlin que fija la nulabilidad de una
    // vez por todas, y el resto del código ya trabaja con tipos honestos.
    show("sobrenombreDe(usuario)", sobrenombreDe(usuario))
    show("recortarSeguro(\"ab\")", recortarSeguro("ab"))
    show("recortarSeguro(\"un texto normal\")", recortarSeguro("un texto normal"))

    bullet("Es lo que hacen las librerías Kotlin que envuelven APIs de Java.")
    bullet("Una capa fina, y a partir de ahí el `?` sólo aparece donde toca.")
}

/**
 * Lo que Kotlin sí garantiza en el otro sentido.
 */
fun demoKotlinSideGuarantees() {
    section("De Kotlin a Java: sí hay comprobación")

    bullet("Si una función Kotlin declara un parámetro `String` (no nulable) y")
    bullet("Java le pasa null, Kotlin lo detecta y lanza en la entrada.")

    // `buscarSobrenombre()` no está anotado, así que devuelve un platform type
    // `String!`. Kotlin DEJA pasarlo a un parámetro `String`... y comprueba en la
    // entrada de la función.
    val usuario = UsuarioJava("Ana", 34)
    val fallo = try {
        exigeNoNulo(usuario.buscarSobrenombre())
    } catch (e: NullPointerException) {
        "lanzó NullPointerException: ${e.message?.take(60)}"
    }
    show("pasar null a un parámetro no nulable", fallo)

    bullet("El compilador de Kotlin inserta un `Intrinsics.checkNotNullParameter`")
    bullet("al principio de cada función pública con parámetros no nulables.")
    bullet("Por eso el error dice exactamente qué parámetro y de qué función.")

    section("El resumen del capítulo, en cuatro líneas")

    bullet("Java → Kotlin: `T!`. Kotlin no te protege. Declara el tipo en la frontera.")
    bullet("Java anotado → Kotlin: `T` o `T?`. Kotlin sí te protege.")
    bullet("Kotlin → Java: Java puede pasarte null igualmente...")
    bullet("...pero Kotlin lo comprueba al entrar y falla con un mensaje claro.")
}

// -- La capa de frontera -----------------------------------------------------------------------

/**
 * Envuelve el método Java y fija la nulabilidad de una vez.
 *
 * A partir de aquí, el resto del código Kotlin trabaja con un `String?` honesto.
 */
private fun sobrenombreDe(usuario: UsuarioJava): String {
    val valor: String? = usuario.buscarSobrenombre()
    return valor ?: "(sin sobrenombre)"
}

/** Lo mismo con una utilidad estática. */
private fun recortarSeguro(texto: String): String {
    val recortado: String? = TextoUtilJava.recortar(texto)
    return recortado ?: "(demasiado corto)"
}

/**
 * Un parámetro no nulable: Kotlin inserta `Intrinsics.checkNotNullParameter` al
 * principio.
 *
 * No es `private` a propósito: el compilador sólo añade esa comprobación a las
 * funciones que puede llamar código de fuera (públicas y protegidas). En una función
 * privada se la ahorra, porque el propio compilador ya ha verificado a todos los que
 * la llaman.
 */
fun exigeNoNulo(valor: String): String = valor.uppercase()

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Pon el ratón sobre `usuario.nombre` en IntelliJ: verás el tipo `String!`.
//  2. Añade @NotNull a `buscarSobrenombre()` en el Java y mira cómo cambia todo
//     (y cómo el método pasa a mentir, porque devuelve null).
//  3. Cambia `val comoNulable: String?` por `val comoNulable = ...` y observa que
//     vuelve a ser un platform type.
//  4. Escribe una capa de frontera para TextoUtilJava entera.
