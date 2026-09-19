package com.alejandro.c26files

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.useLines
import kotlin.io.path.writeText

// =====================================================================================
//  26.2 · java.nio.Path y kotlin.io.path
//
//  QUÉ ES
//    `Path` es la API moderna de rutas de la JVM (desde Java 7), y `kotlin.io.path`
//    le añade las mismas comodidades que Kotlin le da a `File`.
//
//  POR QUÉ IMPORTA
//    `Path` maneja bien los separadores de cada sistema, distingue rutas absolutas
//    de relativas y trae operaciones atómicas. Es lo que conviene usar en código
//    nuevo; `File` sigue ahí por compatibilidad.
//
//  ERRORES COMUNES
//    · Construir rutas concatenando cadenas con "/" y que falle en Windows.
//    · Mezclar `File` y `Path` sin convertir (`.toPath()` y `.toFile()`).
//    · Usar `Path.deleteRecursively()`, que todavía es experimental.
// =====================================================================================

/** El mismo patrón seguro del fichero anterior, esta vez con Path. */
private fun <T> enRutaTemporal(bloque: (Path) -> T): T {
    val directorio = Files.createTempDirectory("kotlin-ref-path-")
    return try {
        bloque(directorio)
    } finally {
        // Usamos la versión de File, que es estable.
        directorio.toFile().deleteRecursively()
    }
}

/**
 * Construir rutas.
 */
fun demoBuildingPaths() {
    section("El problema de concatenar cadenas")

    bullet("\"datos\" + \"/\" + \"2026\" + \"/\" + \"enero.txt\"")
    bullet("Funciona en Linux y macOS. En Windows el separador es \\.")
    bullet("Y si una parte ya acaba en \"/\", te queda una doble barra.")

    section("Path.of y el operador /")

    val ruta = Path.of("datos", "2026", "enero.txt")
    show("Path.of(\"datos\", \"2026\", \"enero.txt\")", ruta)

    // `kotlin.io.path` define el operador `/` para componer rutas: se lee igual que
    // una ruta de verdad y usa el separador correcto de cada sistema.
    val conOperador = Path.of("datos") / "2026" / "enero.txt"
    show("Path.of(\"datos\") / \"2026\" / \"enero.txt\"", conOperador)
    show("son iguales", ruta == conOperador)

    bullet("El `/` es un operador sobrecargado (capítulo 21) de kotlin.io.path.")

    section("Partes de una ruta")

    show("name", ruta.name)
    show("nameWithoutExtension", ruta.nameWithoutExtension)
    show("extension", ruta.extension)
    show("parent", ruta.parent)
    show("nº de partes", ruta.nameCount)
    show("primera parte", ruta.getName(0))

    section("Absoluta o relativa")

    show("isAbsolute", ruta.isAbsolute)
    show("normalize() sobre '../a/./b'", Path.of("../a/./b").normalize())

    bullet("`normalize()` resuelve los `.` y `..` sin tocar el disco.")
    bullet("`toAbsolutePath()` sí depende del directorio de trabajo actual.")
}

/**
 * Leer y escribir con Path.
 */
fun demoPathIo() {
    enRutaTemporal { directorio ->

        section("Las mismas operaciones que con File")

        val fichero = directorio / "notas.txt"
        fichero.writeText("primera línea\nsegunda línea\ntercera línea")

        show("exists()", fichero.exists())
        show("readText()", fichero.readText().replace("\n", " ⏎ "))
        show("readLines()", fichero.readLines())
        show("name", fichero.name)

        section("useLines, igual que con File")

        val conS = fichero.useLines { lineas ->
            lineas.filter { "s" in it }.toList()
        }
        show("líneas que contienen 's'", conS)

        section("Crear directorios")

        val anidado = directorio / "a" / "b" / "c"
        anidado.createDirectories()
        show("createDirectories()", anidado.exists())

        bullet("`createDirectories()` crea toda la cadena y no falla si ya existía.")
        bullet("`createDirectory()` sólo crea el último y falla si el padre no está.")

        section("Listar")

        (directorio / "uno.txt").writeText("1")
        (directorio / "dos.txt").writeText("2")

        val entradas = directorio.listDirectoryEntries().map { it.name }.sorted()
        show("listDirectoryEntries()", entradas)

        val soloTxt = directorio.listDirectoryEntries("*.txt").map { it.name }.sorted()
        show("listDirectoryEntries(\"*.txt\")", soloTxt)

        bullet("Acepta un patrón glob: mucho más cómodo que filtrar a mano.")

        section("Conversión entre File y Path")

        val comoFile = fichero.toFile()
        val deVueltaAPath = comoFile.toPath()

        show("Path → File → Path", deVueltaAPath == fichero)
        bullet("`.toFile()` y `.toPath()` convierten en los dos sentidos sin coste.")
    }
}

/**
 * Qué usar: File o Path.
 */
fun demoFileVsPath() {
    section("File: la API antigua")

    bullet("+ Más corta de escribir: `File(\"a.txt\").readText()`")
    bullet("+ Es lo que piden muchas APIs antiguas")
    bullet("- Métodos que devuelven `false` en vez de explicar el error")
    bullet("- `delete()` devuelve false y no dice por qué falló")
    bullet("- No distingue bien entre ficheros y enlaces simbólicos")

    section("Path: la API moderna")

    bullet("+ Errores con excepciones que explican qué pasó")
    bullet("+ Operaciones atómicas (`Files.move` con ATOMIC_MOVE)")
    bullet("+ Soporte real de enlaces simbólicos y sistemas de ficheros virtuales")
    bullet("+ El operador `/` de kotlin.io.path se lee muy bien")
    bullet("- Algunas extensiones de kotlin.io.path siguen siendo experimentales")

    section("La recomendación")

    bullet("Código nuevo → `Path` con `kotlin.io.path`.")
    bullet("Interoperar con APIs antiguas → convierte con `.toFile()` en el borde.")
    bullet("Y no te obsesiones: para leer un fichero de configuración pequeño,")
    bullet("`File(ruta).readText()` está perfectamente bien.")

    section("El aviso sobre lo experimental")

    bullet("`Path.deleteRecursively()` y `Path.walk()` siguen marcadas como")
    bullet("@ExperimentalPathApi: usarlas exige @OptIn y pueden cambiar.")
    bullet("Por eso este capítulo borra con `File.deleteRecursively()`, que es estable.")

    section("Lo que NO cambia entre las dos")

    bullet("Los ficheros grandes hay que leerlos en streaming igualmente.")
    bullet("Los recursos hay que cerrarlos igualmente (con `use`).")
    bullet("La codificación hay que tenerla en cuenta igualmente.")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Construye una ruta con `/` y otra concatenando cadenas, e imprime las dos.
//  2. Prueba `listDirectoryEntries("*.md")` en un directorio sin ficheros .md.
//  3. Añade `@OptIn(ExperimentalPathApi::class)` y usa `Path.walk()`.
//  4. Convierte una función tuya que use File para que use Path y compara.
