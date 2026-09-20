package com.alejandro.c26files

import com.alejandro.infra.chapter

/**
 * # Capítulo 26 · Ficheros y entrada/salida
 *
 * Leer y escribir ficheros en la JVM desde Kotlin. Lo que en Java eran diez líneas
 * con try-with-resources, aquí es una llamada — con el matiz de saber cuándo esa
 * comodidad deja de valer.
 *
 * ## Seguridad de las demos
 * **Ninguna demo de este capítulo escribe dentro del repositorio.** Todas crean un
 * directorio temporal, trabajan ahí y lo borran en un `finally`, pase lo que pase.
 * Ese patrón (`enDirectorioTemporal { }`) está al principio de cada fichero y merece
 * la pena copiarlo.
 *
 * ## Qué se cubre
 * - `File`: `writeText`, `readText`, `readLines`, `appendText`, `forEachLine`,
 *   `useLines`, `walkTopDown`, copiar, mover y borrar.
 * - `Path` y `kotlin.io.path`: el operador `/`, partes de una ruta,
 *   `createDirectories`, `listDirectoryEntries` con glob.
 * - Un caso completo: CSV con líneas rotas → objetos → informe escrito a disco.
 *
 * ## Lo que hay que llevarse sí o sí
 * - `readText()` y `readLines()` cargan el fichero **entero** en memoria. Para algo
 *   grande, `forEachLine` o `useLines`.
 * - `useLines` da una `Sequence` que **sólo vale dentro del bloque**: al salir, el
 *   fichero está cerrado. Por eso el bloque termina en `.toList()`.
 * - `writeText` **sobrescribe**; `appendText` añade. Usar el primero en un bucle deja
 *   sólo la última línea.
 * - Para escribir mucho, `bufferedWriter().use { }`: abre una vez, no en cada vuelta.
 * - Kotlin usa **UTF-8 por defecto** en estas funciones, a diferencia de Java.
 * - Construye rutas con `Path.of(...)` o el operador `/`, nunca concatenando `"/"`.
 * - Un fichero de datos reales **siempre** tiene líneas rotas: acumula los errores
 *   con su número de línea en lugar de lanzar en la primera.
 *
 * Siguiente paso: capítulo 27, interoperabilidad con Java.
 */
val chapter26 = chapter(
    number = 26,
    name = "Ficheros e IO",
    summary = "File, Path, lectura en streaming, y un caso completo de CSV a objetos",
) {
    demo("Escribir y leer ficheros", ::demoWriteAndRead)
    demo("Ficheros grandes: forEachLine y useLines", ::demoLargeFiles)
    demo("Listar, recorrer, copiar y borrar", ::demoFileOperations)
    demo("Construir rutas con Path", ::demoBuildingPaths)
    demo("Leer y escribir con Path", ::demoPathIo)
    demo("File o Path: cuál usar", ::demoFileVsPath)
    demo("Caso completo: parsear un CSV con errores", ::demoParseCsv)
    demo("Caso completo: agregar y escribir el informe", ::demoTransformAndWrite)
    demo("Las decisiones de diseño del parseo", ::demoDesignNotes)
}

/** Ejecuta el capítulo 26 completo. */
fun main() = chapter26.runAll()
