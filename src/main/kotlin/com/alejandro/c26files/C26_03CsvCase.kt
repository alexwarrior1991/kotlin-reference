package com.alejandro.c26files

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import java.io.File
import java.nio.file.Files

// =====================================================================================
//  26.3 · Un caso completo: CSV a objetos y vuelta
//
//  QUÉ ES
//    Todo lo del capítulo junto en un caso realista: leer un CSV con errores dentro,
//    convertirlo a objetos, informar de las líneas malas, calcular algo y escribir
//    el resultado.
//
//  POR QUÉ IMPORTA
//    Un fichero de datos reales SIEMPRE tiene líneas rotas. La diferencia entre un
//    parseo de juguete y uno de verdad es qué hace con ellas: si lanza a la primera,
//    un solo registro corrupto tira el proceso entero.
//
//  ERRORES COMUNES
//    · Parsear con `split(",")` un CSV que puede llevar comas dentro de comillas.
//    · Lanzar en la primera línea mala en vez de acumular los errores.
//    · Cargar en memoria un fichero que no cabe.
// =====================================================================================

private data class Venta(
    val fecha: String,
    val producto: String,
    val unidades: Int,
    val importeCentimos: Int,
)

/** Resultado del parseo: lo que salió bien y lo que no. */
private data class ResultadoParseo(
    val ventas: List<Venta>,
    val errores: List<String>,
)

private const val CSV_DE_EJEMPLO = """fecha,producto,unidades,importe
2026-01-15,Teclado,2,4999
2026-01-16,Ratón,5,1550
2026-01-17,Monitor,1,18900
2026-01-18,SIN_UNIDADES,,2000
2026-01-19,Teclado,3,4999
linea rota sin suficientes columnas
2026-01-20,Monitor,dos,18900
2026-01-21,Ratón,10,1550"""

/** El patrón seguro de siempre: directorio temporal con limpieza garantizada. */
private fun <T> conCsvTemporal(bloque: (File) -> T): T {
    val directorio = Files.createTempDirectory("kotlin-ref-csv-").toFile()
    return try {
        val fichero = File(directorio, "ventas.csv")
        fichero.writeText(CSV_DE_EJEMPLO)
        bloque(directorio)
    } finally {
        directorio.deleteRecursively()
    }
}

/**
 * Leer y parsear, informando de los errores.
 */
fun demoParseCsv() {
    conCsvTemporal { directorio ->

        val fichero = File(directorio, "ventas.csv")

        section("El fichero de partida")

        fichero.readLines().forEachIndexed { indice, linea ->
            bullet("${(indice + 1).toString().padStart(2)}: $linea")
        }

        section("Parseo tolerante a errores")

        val resultado = parsear(fichero)

        show("líneas válidas", resultado.ventas.size)
        show("líneas con error", resultado.errores.size)

        section("Lo que salió bien")

        resultado.ventas.forEach { venta ->
            show(venta.fecha, "${venta.producto} x${venta.unidades} = ${euros(venta.importeCentimos)}")
        }

        section("Lo que salió mal, con el número de línea")

        resultado.errores.forEach { bullet(it) }

        bullet("Nada de esto ha lanzado: los errores son DATOS, no excepciones.")
        bullet("Quien llama decide si aborta, si reintenta o si avisa y sigue.")

        section("Por qué importa")

        bullet("Con `split(\",\")[2].toInt()` a pelo, la línea 5 habría lanzado")
        bullet("NumberFormatException y las tres siguientes no se habrían leído.")
        bullet("Un fichero de 100.000 registros se perdería por uno malo.")
    }
}

/**
 * Calcular y escribir el resultado.
 */
fun demoTransformAndWrite() {
    conCsvTemporal { directorio ->

        val entrada = File(directorio, "ventas.csv")
        val resultado = parsear(entrada)

        section("Agregar por producto")

        val porProducto = resultado.ventas
            .groupBy { it.producto }
            .mapValues { (_, ventas) ->
                Resumen(
                    unidades = ventas.sumOf { it.unidades },
                    totalCentimos = ventas.sumOf { it.unidades * it.importeCentimos },
                )
            }
            .toList()
            .sortedByDescending { (_, resumen) -> resumen.totalCentimos }

        porProducto.forEach { (producto, resumen) ->
            show(producto, "${resumen.unidades} uds · ${euros(resumen.totalCentimos)}")
        }

        section("Escribir el informe")

        val salida = File(directorio, "informe.csv")
        salida.bufferedWriter().use { escritor ->
            escritor.write("producto,unidades,total_centimos")
            escritor.newLine()
            porProducto.forEach { (producto, resumen) ->
                escritor.write("$producto,${resumen.unidades},${resumen.totalCentimos}")
                escritor.newLine()
            }
        }

        show("fichero escrito", salida.name)
        show("tamaño", "${salida.length()} bytes")

        section("Y comprobamos releyéndolo")

        salida.readLines().forEach { bullet(it) }

        bullet("`bufferedWriter().use { }` abre una vez, escribe todo y cierra siempre.")
        bullet("Con `appendText` en un bucle abriríamos el fichero en cada vuelta.")

        section("El total general")

        show("ventas válidas", resultado.ventas.size)
        show("unidades totales", resultado.ventas.sumOf { it.unidades })
        show("facturación", euros(resultado.ventas.sumOf { it.unidades * it.importeCentimos }))
    }
}

/**
 * Las decisiones de diseño del parseo.
 */
fun demoDesignNotes() {
    section("1. Devolver errores en vez de lanzar")

    bullet("`ResultadoParseo(ventas, errores)` permite procesar lo bueno y")
    bullet("reportar lo malo. Con una excepción sólo tendrías lo primero que falló.")
    bullet("Es el capítulo 11 aplicado: modelar el fallo como dato.")

    section("2. Incluir el número de línea en el error")

    bullet("«línea 7: se esperaban 4 columnas y había 1» se puede arreglar.")
    bullet("«NumberFormatException: For input string: \"dos\"» no dice dónde mirar.")

    section("3. Usar useLines para no cargar el fichero entero")

    bullet("Así el mismo código vale para 10 líneas y para 10 millones.")

    section("4. Céntimos en Int, nunca euros en Double")

    bullet("Capítulo 2.10: 0.1 + 0.2 no es 0.3. Con dinero eso es inaceptable.")
    bullet("Se guarda el entero y se formatea sólo al mostrar.")

    section("5. Lo que este parser NO hace (y un CSV real necesita)")

    bullet("Comillas: `\"Teclado, mecánico\",2,4999` rompería el split.")
    bullet("Saltos de línea dentro de un campo entrecomillado.")
    bullet("Separadores distintos (`;` en configuraciones españolas de Excel).")
    bullet("Codificaciones que no son UTF-8, y el BOM al principio del fichero.")

    section("La conclusión práctica")

    bullet("Para un CSV controlado por ti, `split` está bien.")
    bullet("Para CSV de fuera, usa una librería (Apache Commons CSV, kotlin-csv).")
    bullet("El valor de escribirlo a mano es entender qué hacen esas librerías")
    bullet("y por qué son más complicadas de lo que parece.")
}

// -- El parseo ---------------------------------------------------------------------------------

private data class Resumen(val unidades: Int, val totalCentimos: Int)

private fun euros(centimos: Int): String = "%.2f €".format(centimos / 100.0)

/**
 * Parsea el CSV línea a línea, acumulando errores en lugar de lanzar.
 *
 * Usa `useLines`, así que el fichero se lee en streaming y se cierra solo.
 */
private fun parsear(fichero: File): ResultadoParseo {
    val ventas = mutableListOf<Venta>()
    val errores = mutableListOf<String>()

    fichero.useLines { lineas ->
        lineas.forEachIndexed { indice, linea ->
            val numeroDeLinea = indice + 1

            // Saltamos la cabecera y las líneas en blanco.
            if (numeroDeLinea == 1 || linea.isBlank()) return@forEachIndexed

            val columnas = linea.split(",")
            if (columnas.size != 4) {
                errores += "línea $numeroDeLinea: se esperaban 4 columnas y había ${columnas.size}"
                return@forEachIndexed
            }

            val unidades = columnas[2].toIntOrNull()
            val importe = columnas[3].toIntOrNull()

            when {
                unidades == null ->
                    errores += "línea $numeroDeLinea: unidades no numéricas ('${columnas[2]}')"

                importe == null ->
                    errores += "línea $numeroDeLinea: importe no numérico ('${columnas[3]}')"

                else -> ventas += Venta(
                    fecha = columnas[0],
                    producto = columnas[1],
                    unidades = unidades,
                    importeCentimos = importe,
                )
            }
        }
    }

    return ResultadoParseo(ventas.toList(), errores.toList())
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade una línea con un producto entrecomillado que lleve coma dentro y
//     comprueba que este parser la parte mal.
//  2. Haz que `parsear` acepte el separador como parámetro (`,` o `;`).
//  3. Cambia `useLines` por `readLines` y razona qué se pierde con un fichero grande.
//  4. Añade validación de la fecha y un error específico cuando no tenga formato.
