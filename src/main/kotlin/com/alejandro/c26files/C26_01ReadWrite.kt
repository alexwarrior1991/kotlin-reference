package com.alejandro.c26files

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import java.io.File

// =====================================================================================
//  26.1 · Leer y escribir ficheros
//
//  QUÉ ES
//    Las extensiones de Kotlin sobre `java.io.File`: `readText`, `writeText`,
//    `readLines`, `appendText`, `forEachLine`.
//
//  POR QUÉ IMPORTA
//    En Java leer un fichero entero eran diez líneas con try-with-resources. En
//    Kotlin es `File(ruta).readText()`. Pero hay que saber cuándo esa comodidad
//    deja de valer (ficheros grandes) y cuándo la codificación importa.
//
//  ERRORES COMUNES
//    · `readText()` sobre un fichero de 2 GB: se lo carga entero en memoria.
//    · Olvidar la codificación y que los acentos se rompan en otra máquina.
//    · Escribir en rutas relativas y no saber dónde acabó el fichero.
//
//  NOTA IMPORTANTE SOBRE ESTE CAPÍTULO
//    Todas las demos trabajan en un directorio TEMPORAL que se crea al empezar y se
//    borra al terminar. Nunca escriben dentro del repositorio ni en tu disco.
// =====================================================================================

/**
 * Crea un directorio temporal, ejecuta el bloque y lo borra siempre.
 *
 * Es el patrón que hace seguras todas las demos de este capítulo: pase lo que pase
 * dentro del bloque, el `finally` limpia.
 */
private fun <T> enDirectorioTemporal(bloque: (File) -> T): T {
    val directorio = java.nio.file.Files.createTempDirectory("kotlin-ref-").toFile()
    return try {
        bloque(directorio)
    } finally {
        // `deleteRecursively` es estable en kotlin.io (la versión de java.nio.Path
        // todavía es experimental).
        directorio.deleteRecursively()
    }
}

/**
 * Escribir y leer el fichero entero.
 */
fun demoWriteAndRead() {
    enDirectorioTemporal { directorio ->

        section("Escribir")

        val fichero = File(directorio, "saludo.txt")
        fichero.writeText("Hola desde Kotlin\ncon acentos: áéíóú ñ")

        show("¿existe?", fichero.exists())
        show("tamaño en bytes", fichero.length())
        show("nombre", fichero.name)
        show("extensión", fichero.extension)
        show("nombre sin extensión", fichero.nameWithoutExtension)

        section("Leer el contenido entero")

        show("readText()", fichero.readText().replace("\n", " ⏎ "))

        section("Leer línea a línea")

        show("readLines()", fichero.readLines())
        show("nº de líneas", fichero.readLines().size)

        section("Añadir al final")

        fichero.appendText("\nlínea añadida")
        show("tras appendText", fichero.readLines())

        bullet("`writeText` SOBRESCRIBE el fichero entero; `appendText` añade.")
        bullet("Es un error clásico usar writeText en un bucle y quedarse con la")
        bullet("última línea nada más.")

        section("La codificación")

        // Kotlin usa UTF-8 por defecto en todas estas funciones, a diferencia de
        // Java, que usaba la del sistema hasta JDK 18.
        show("bytes con UTF-8 (por defecto)", fichero.readText().length)
        show("explícita", fichero.readText(Charsets.UTF_8).take(20))

        bullet("Kotlin usa UTF-8 por defecto: los acentos viajan bien entre sistemas.")
        bullet("Si lees un fichero antiguo en ISO-8859-1, pásalo explícitamente.")

        section("Escribir bytes")

        val binario = File(directorio, "datos.bin")
        binario.writeBytes(byteArrayOf(1, 2, 3, 4))
        show("writeBytes / readBytes", binario.readBytes().toList())
    }

    section("Y al salir, todo limpio")
    bullet("El directorio temporal ya no existe: lo borró el `finally`.")
}

/**
 * Ficheros grandes: no cargarlos enteros.
 */
fun demoLargeFiles() {
    enDirectorioTemporal { directorio ->

        section("Generamos un fichero con muchas líneas")

        val grande = File(directorio, "grande.csv")
        grande.bufferedWriter().use { escritor ->
            repeat(1_000) { i ->
                escritor.write("registro-$i,valor-${i * 2}")
                escritor.newLine()
            }
        }
        show("líneas escritas", 1_000)
        show("tamaño aproximado", "${grande.length()} bytes")

        section("MAL: readText / readLines sobre algo grande")

        bullet("`readText()` y `readLines()` cargan el fichero ENTERO en memoria.")
        bullet("Con 2 GB, eso es un OutOfMemoryError garantizado.")
        bullet("Regla: sólo para ficheros pequeños y de tamaño conocido.")

        section("BIEN: forEachLine, que va línea a línea")

        var contadas = 0
        var sumaValores = 0L
        grande.forEachLine { linea ->
            contadas++
            sumaValores += linea.substringAfterLast("-").toLong()
        }
        show("líneas procesadas", contadas)
        show("suma de los valores", sumaValores)

        bullet("`forEachLine` abre, recorre y CIERRA el fichero por ti.")
        bullet("En memoria sólo hay una línea cada vez.")

        section("useLines: una secuencia perezosa")

        // Lo mejor de los dos mundos: la API de secuencias (capítulo 14) sobre un
        // fichero que no cabe en memoria.
        val primerosPares = grande.useLines { lineas ->
            lineas
                .map { it.substringBefore(",") }
                .filter { it.endsWith("0") }
                .take(5)
                .toList()
        }
        show("useLines + map + filter + take", primerosPares)

        bullet("`useLines` da una Sequence<String> y cierra el fichero al salir.")
        bullet("Con `take(5)` sólo se leen las líneas necesarias, no el fichero entero.")

        section("El aviso sobre useLines")

        bullet("La secuencia SÓLO es válida dentro del bloque: al salir, el fichero")
        bullet("está cerrado. Si devuelves la Sequence, fallará al consumirla.")
        bullet("Por eso el bloque termina en `.toList()`.")

        section("Escribir mucho: bufferedWriter")

        bullet("`writeText` en un bucle abre y cierra el fichero en cada vuelta.")
        bullet("`bufferedWriter().use { }` lo abre una vez y acumula en un buffer.")
    }
}

/**
 * Información y operaciones sobre ficheros.
 */
fun demoFileOperations() {
    enDirectorioTemporal { directorio ->

        section("Crear estructura")

        val subdirectorio = File(directorio, "datos/2026")
        show("mkdirs()", subdirectorio.mkdirs())
        show("¿es directorio?", subdirectorio.isDirectory)

        File(subdirectorio, "enero.txt").writeText("datos de enero")
        File(subdirectorio, "febrero.txt").writeText("datos de febrero")
        File(directorio, "raiz.txt").writeText("en la raíz")

        section("Listar")

        show("listFiles() en la raíz", directorio.listFiles()?.map { it.name }?.sorted())
        show("listFiles() en datos/2026", subdirectorio.listFiles()?.map { it.name }?.sorted())

        section("Recorrer recursivamente")

        val todos = directorio.walkTopDown()
            .filter { it.isFile }
            .map { it.name }
            .sorted()
            .toList()
        show("walkTopDown()", todos)

        bullet("`walkTopDown()` devuelve una Sequence: no carga el árbol entero.")
        bullet("También existen `walkBottomUp()` y `walk(direccion)`.")

        section("Filtrar por extensión")

        val soloTxt = directorio.walkTopDown()
            .filter { it.isFile && it.extension == "txt" }
            .map { it.nameWithoutExtension }
            .sorted()
            .toList()
        show("sólo los .txt", soloTxt)

        section("Copiar, mover y borrar")

        val origen = File(subdirectorio, "enero.txt")
        val copia = File(directorio, "copia-enero.txt")

        origen.copyTo(copia, overwrite = true)
        show("tras copyTo", copia.readText())

        val renombrado = File(directorio, "renombrado.txt")
        show("renameTo", copia.renameTo(renombrado))
        show("la copia ya no existe", copia.exists())
        show("el nuevo sí", renombrado.exists())

        show("delete()", renombrado.delete())
        show("tras borrar, ¿existe?", renombrado.exists())

        section("Comprobaciones antes de tocar nada")

        val inexistente = File(directorio, "no-existe.txt")
        show("exists()", inexistente.exists())
        show("canRead()", inexistente.canRead())

        // Leer algo que no existe lanza. Siempre comprobar, o capturar.
        val fallo = try {
            inexistente.readText()
        } catch (e: java.io.FileNotFoundException) {
            "lanzó FileNotFoundException"
        }
        show("readText() sobre un fichero inexistente", fallo)

        bullet("Alternativa sin excepciones: `if (f.exists()) f.readText() else null`")
        bullet("o `runCatching { f.readText() }.getOrNull()` (capítulo 19.8).")
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `appendText` por `writeText` en la primera demo y mira qué pasa.
//  2. Sube el fichero grande a 1.000.000 de líneas y compara readLines con useLines
//     (pista: vigila la memoria).
//  3. Devuelve la Sequence de `useLines` fuera del bloque y observa el error.
//  4. Escribe una función que cuente cuántos ficheros .txt hay bajo un directorio.
