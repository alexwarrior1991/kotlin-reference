package com.alejandro.c01basics

// Los imports van SIEMPRE justo después del `package` y antes de cualquier código.
// IntelliJ los ordena solo (alfabéticamente) al formatear.
import com.alejandro.infra.bullet
import com.alejandro.infra.quoted
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.math.PI
import kotlin.math.max

// Un import con alias. Sirve para dos cosas: resolver choques de nombres y dar un
// nombre más claro a algo que se usa mucho en este fichero.
import kotlin.math.abs as valorAbsoluto

// =====================================================================================
//  1.3 · Paquetes e imports
//
//  QUÉ ES
//    `package` dice a qué espacio de nombres pertenece el fichero; `import` trae
//    nombres de otros paquetes para poder usarlos sin escribir su ruta completa.
//
//  POR QUÉ IMPORTA
//    Es lo que permite que dos clases se llamen igual sin pisarse, y lo que hace que
//    el árbol de carpetas de este repositorio se lea como un índice.
//
//  ERRORES COMUNES
//    · Creer que la carpeta DEBE coincidir con el paquete. En Kotlin no es obligatorio,
//      pero hacerlo es la convención y el IDE se queja si no lo haces.
//    · Abusar de los imports con `*`, que esconden de dónde sale cada nombre.
//    · No saber que `kotlin.*`, `kotlin.collections.*` y varios más ya vienen puestos.
// =====================================================================================

/**
 * Qué hace la declaración `package` y cómo se relaciona con las carpetas.
 */
fun demoPackages() {
    section("Este fichero")

    bullet("package  com.alejandro.c01basics")
    bullet("carpeta  src/main/kotlin/com/alejandro/c01basics/")
    bullet("fichero  C01_03PackagesAndImports.kt")

    section("Carpeta y paquete: la convención")

    // A diferencia de Java, Kotlin NO obliga a que la carpeta refleje el paquete:
    // este proyecto nació con un `Main.kt` que declaraba `package com.alejandro` pero
    // vivía en la raíz de src/main/kotlin, y compilaba perfectamente.
    //
    // Aun así, se hace siempre, por tres motivos:
    bullet("El IDE avisa (y ofrece mover el fichero) si no coinciden.")
    bullet("Buscar un fichero por su paquete se vuelve trivial.")
    bullet("Las herramientas de build y empaquetado lo dan por supuesto.")

    section("Qué genera el compilador")

    // Las funciones de nivel superior no pueden existir sueltas en la JVM, así que el
    // compilador las mete en una clase "fachada" cuyo nombre es el del fichero + "Kt".
    bullet("C01_03PackagesAndImports.kt  →  clase com.alejandro.c01basics.C01_03PackagesAndImportsKt")
    bullet("Por eso `application { mainClass.set(\"com.alejandro.MainKt\") }` en el build.")
    bullet("Ese nombre depende del PAQUETE y del NOMBRE DEL FICHERO, no de la carpeta.")

    section("Consecuencia práctica: un solo main() por paquete")

    // Dos funciones de nivel superior con el mismo nombre y la misma firma en el mismo
    // paquete son un error de compilación ("Conflicting overloads"), aunque estén en
    // ficheros distintos. Por eso en este repositorio cada capítulo tiene UN `main()`,
    // el de su fichero `CNN_00Index.kt`, y el resto son funciones `demoXxx()`.
    bullet("fun main() en A.kt y en B.kt del mismo paquete → Conflicting overloads.")
}

/**
 * Importar, o no importar.
 */
fun demoImports() {
    section("Con import")

    // `max` y `PI` están importados arriba, así que se usan por su nombre corto.
    show("max(3, 9)", max(3, 9))
    show("PI", PI)

    section("Sin import: nombre completamente cualificado")

    // Siempre puedes escribir la ruta entera y ahorrarte el import. Se hace cuando la
    // usas una sola vez, o para desambiguar en un punto concreto.
    show("kotlin.math.sqrt(16.0)", kotlin.math.sqrt(16.0))
    show("kotlin.math.min(3, 9)", kotlin.math.min(3, 9))

    section("Imports con asterisco")

    // `import kotlin.math.*` trae todo el paquete. Es cómodo, pero:
    bullet("Escondes de dónde viene cada nombre.")
    bullet("Si el paquete añade una función, puede chocar con una tuya.")
    bullet("Regla práctica: importa nombre a nombre y deja que el IDE lo gestione.")

    section("Qué se puede importar en Kotlin (y en Java no)")

    // En Kotlin el import no se limita a tipos: también trae funciones y propiedades
    // de nivel superior, objetos, constantes de enum y funciones de extensión.
    bullet("Clases e interfaces         → import kotlin.text.Regex")
    bullet("Funciones de nivel superior → import kotlin.math.max")
    bullet("Propiedades y constantes    → import kotlin.math.PI")
    bullet("Funciones de extensión      → import com.alejandro.infra.show")
    bullet("Constantes de un enum       → import java.time.DayOfWeek.MONDAY")
}

/**
 * Alias de import: la salida cuando dos nombres chocan.
 */
fun demoImportAliases() {
    section("Renombrar al importar con `as`")

    // Arriba tenemos: import kotlin.math.abs as valorAbsoluto
    show("valorAbsoluto(-42)", valorAbsoluto(-42))
    show("valorAbsoluto(-3.5)", valorAbsoluto(-3.5))

    section("Para qué sirve de verdad")

    // El caso clásico: dos clases con el mismo nombre en paquetes distintos.
    // Sin alias no podrías importar las dos a la vez:
    //
    //     import com.miapp.modelo.Usuario
    //     import com.miapp.api.Usuario as UsuarioApi
    //
    //     fun aModelo(dto: UsuarioApi): Usuario = ...
    bullet("Resolver choques de nombres entre paquetes.")
    bullet("Dar un nombre más legible en un contexto concreto (valorAbsoluto vs abs).")
    bullet("Distinguir el DTO de la API del modelo de dominio, que suelen llamarse igual.")
}

/**
 * Los imports que no hay que escribir: están puestos en todos los ficheros.
 */
fun demoDefaultImports() {
    section("Importados automáticamente en todo fichero Kotlin")

    bullet("kotlin.*               → Int, String, Pair, lazy, TODO...")
    bullet("kotlin.annotation.*    → @Target, @Retention...")
    bullet("kotlin.collections.*   → List, Map, listOf, filter, map...")
    bullet("kotlin.comparisons.*   → compareBy, maxOf, minOf...")
    bullet("kotlin.io.*            → println, readln, File.readText...")
    bullet("kotlin.ranges.*        → IntRange, downTo, step...")
    bullet("kotlin.sequences.*     → Sequence, generateSequence...")
    bullet("kotlin.text.*          → trim, split, Regex, StringBuilder...")

    section("Y además, sólo en la JVM")

    bullet("java.lang.*            → Thread, Math, System, Exception...")
    bullet("kotlin.jvm.*           → @JvmStatic, @JvmOverloads, @JvmField...")

    section("Por eso...")

    // Esto explica por qué `println`, `listOf` o `TODO()` funcionan sin tocar nada.
    show("listOf(1, 2, 3).sum()  (kotlin.collections)", listOf(1, 2, 3).sum())
    show("\"  hola  \".trim()     (kotlin.text)", quoted("  hola  ".trim()))
    show("maxOf(3, 9)            (kotlin.comparisons)", maxOf(3, 9))
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Borra `import kotlin.math.max` y arregla el error de dos formas: con el nombre
//     completo `kotlin.math.max(3, 9)` y dejando que IntelliJ reimporte con Alt+Enter.
//  2. Cambia el alias `valorAbsoluto` por otro nombre y comprueba que sólo afecta a
//     este fichero.
//  3. Crea otro fichero en este mismo paquete con `fun main()` y observa el error
//     "Conflicting overloads" que se describe en demoPackages().
