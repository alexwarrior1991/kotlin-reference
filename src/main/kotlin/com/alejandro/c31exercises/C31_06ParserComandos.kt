package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 6 · Parser de comandos                                        🟡 medio
//
//  Repasa: sealed, when, String, expresiones regulares, listas, errores como datos.
//  Capítulos: 04 (when), 11 (sealed), 13 (colecciones), 19 (errores), 25 (stdlib).
// =====================================================================================

/** Ejercicio 6: intérprete de una mini consola de tareas. */
fun ejercicio06ParserComandos() {
    enunciado(
        "Escribe el intérprete de una consola de tareas que acepta líneas de texto.",
        "",
        "Comandos que hay que entender:",
        "   añadir \"Comprar pan\" --prioridad alta",
        "   listar [--todas]",
        "   hecha 3",
        "   borrar 3",
        "   ayuda",
        "",
        "Reglas:",
        "1. El texto entre comillas es UN solo argumento, aunque lleve espacios.",
        "2. Las opciones empiezan por `--` y pueden llevar valor o no.",
        "3. Una línea vacía o sólo espacios no es un error: no hace nada.",
        "4. Un comando desconocido debe sugerir el más parecido.",
        "5. Nada de excepciones: devuelve un `sealed` con el comando o con el error.",
        "6. Ejecuta los comandos sobre una lista de tareas en memoria.",
    )

    pistas(
        "Divide el problema en dos: primero TROCEAR la línea en palabras",
        "   respetando las comillas, y después INTERPRETAR esos trozos.",
        "Para trocear, recorre carácter a carácter con una bandera `dentroDeComillas`.",
        "   Una expresión regular también vale, pero se lee peor.",
        "Separa las opciones (`--x`) de los argumentos sueltos con `partition`.",
        "Para sugerir el comando parecido, `startsWith` o comparar los primeros",
        "   caracteres basta; la distancia de Levenshtein es la versión seria.",
        "`toIntOrNull()` para los identificadores: un `\"tres\"` no debe reventar nada.",
    )

    solucionEnMarcha()

    section("Trocear respetando las comillas")

    listOf(
        "añadir \"Comprar pan\" --prioridad alta",
        "añadir Comprar pan",
        "listar   --todas",
        "  ",
    ).forEach { linea ->
        show("«$linea»", trocear(linea))
    }

    section("Interpretar")

    listOf(
        "añadir \"Comprar pan\" --prioridad alta",
        "añadir \"Regar las plantas\"",
        "listar",
        "listar --todas",
        "hecha 1",
        "borrar 2",
        "ayuda",
        "",
        "añadir",
        "hecha",
        "hecha tres",
        "añadir \"x\" --prioridad urgentísima",
        "aladir \"x\"",
        "bailar",
    ).forEach { linea ->
        show("«$linea»", describirParseo(parsearComando(linea)))
    }

    section("La consola, funcionando")

    val consola = ConsolaDeTareas()
    listOf(
        "añadir \"Comprar pan\" --prioridad alta",
        "añadir \"Regar las plantas\"",
        "añadir \"Llamar al fontanero\" --prioridad baja",
        "listar",
        "hecha 2",
        "listar",
        "listar --todas",
        "borrar 3",
        "listar --todas",
        "borrar 99",
        "bailar",
    ).forEach { linea ->
        section("> $linea")
        consola.ejecutar(linea).lines().forEach { bullet(it) }
    }

    explicacion(
        "La separación en DOS pasos es lo que hace este ejercicio manejable:",
        "",
        "   texto  →[trocear]→  List<String>  →[parsearComando]→  Comando",
        "",
        "Cada paso se prueba por separado. `trocear` no sabe nada de comandos y",
        "`parsearComando` no sabe nada de comillas. Si mañana la entrada llega ya",
        "troceada (por ejemplo desde `args` de `main`), el segundo paso vale igual.",
        "",
        "Los comandos son un `sealed interface` con un `data object` para los que no",
        "llevan datos (`Listar`, `Ayuda`) y `data class` para los que sí. Así el",
        "ejecutor es un `when` exhaustivo sin `else`, y añadir un comando nuevo no",
        "compila hasta que decides qué hacer con él (capítulo 11).",
        "",
        "El error también es un tipo, no una excepción: una línea mal escrita en una",
        "consola es lo más normal del mundo, y quien la escribe necesita el motivo.",
        "",
        "Detalle de usabilidad que cuesta cinco líneas y se agradece muchísimo: al",
        "no reconocer un comando, buscar el más parecido y sugerirlo.",
    )

    varianteDificil(
        "1. Opciones con valor pegado: `--prioridad=alta`, y cortas: `-p alta`.",
        "2. Escapes dentro de las comillas: `\"dijo \\\"hola\\\"\"`.",
        "3. Comandos con subcomandos: `tarea añadir`, `tarea listar`, `etiqueta crear`.",
        "4. Autocompletado: dado un prefijo, devuelve los comandos que encajan.",
        "5. Un `ayuda <comando>` que describa cada uno, generado a partir de la",
        "   jerarquía sellada en lugar de escrito a mano.",
        "6. Distancia de Levenshtein de verdad para la sugerencia.",
    )

    testEn("ParserComandosTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

enum class Prioridad { ALTA, NORMAL, BAJA }

/** Los comandos que entiende la consola. */
sealed interface Comando {
    data class Anadir(val texto: String, val prioridad: Prioridad) : Comando
    data class Listar(val incluirHechas: Boolean) : Comando
    data class MarcarHecha(val id: Int) : Comando
    data class Borrar(val id: Int) : Comando
    data object Ayuda : Comando
    data object Vacio : Comando
}

sealed interface ResultadoParseo {
    data class Ok(val comando: Comando) : ResultadoParseo
    data class Error(val mensaje: String) : ResultadoParseo
}

private val COMANDOS_CONOCIDOS = listOf("añadir", "listar", "hecha", "borrar", "ayuda")

/**
 * Trocea una línea en palabras, respetando el texto entre comillas dobles.
 *
 * `añadir "Comprar pan" --prioridad alta` → [añadir, Comprar pan, --prioridad, alta]
 */
fun trocear(linea: String): List<String> {
    val trozos = mutableListOf<String>()
    val actual = StringBuilder()
    var dentroDeComillas = false

    for (caracter in linea) {
        when {
            caracter == '"' -> dentroDeComillas = !dentroDeComillas
            caracter.isWhitespace() && !dentroDeComillas -> {
                if (actual.isNotEmpty()) {
                    trozos += actual.toString()
                    actual.clear()
                }
            }
            else -> actual.append(caracter)
        }
    }
    if (actual.isNotEmpty()) trozos += actual.toString()

    return trozos
}

/**
 * Interpreta una línea ya escrita por el usuario.
 *
 * No lanza nunca: todo error viaja dentro de [ResultadoParseo.Error].
 */
fun parsearComando(linea: String): ResultadoParseo {
    val trozos = trocear(linea)
    if (trozos.isEmpty()) return ResultadoParseo.Ok(Comando.Vacio)

    val nombre = trozos.first().lowercase()
    val resto = trozos.drop(1)

    // Las opciones empiezan por `--`; lo demás son argumentos sueltos.
    val (opciones, argumentos) = resto.partition { it.startsWith("--") }

    return when (nombre) {
        "añadir", "anadir" -> parsearAnadir(argumentos, opciones, resto)
        "listar" -> ResultadoParseo.Ok(Comando.Listar(incluirHechas = "--todas" in opciones))
        "hecha" -> conIdentificador(argumentos, "hecha") { Comando.MarcarHecha(it) }
        "borrar" -> conIdentificador(argumentos, "borrar") { Comando.Borrar(it) }
        "ayuda" -> ResultadoParseo.Ok(Comando.Ayuda)
        else -> ResultadoParseo.Error(mensajeDeComandoDesconocido(nombre))
    }
}

private fun parsearAnadir(
    argumentos: List<String>,
    opciones: List<String>,
    resto: List<String>,
): ResultadoParseo {
    if (argumentos.isEmpty()) {
        return ResultadoParseo.Error("«añadir» necesita el texto de la tarea, entre comillas")
    }

    // El valor de una opción es el trozo siguiente: `--prioridad alta`.
    val prioridad = if ("--prioridad" in opciones) {
        val indice = resto.indexOf("--prioridad")
        val valor = resto.getOrNull(indice + 1)
            ?: return ResultadoParseo.Error("«--prioridad» necesita un valor: alta, normal o baja")

        Prioridad.entries.firstOrNull { it.name.equals(valor, ignoreCase = true) }
            ?: return ResultadoParseo.Error(
                "prioridad desconocida: «$valor». Usa alta, normal o baja",
            )
    } else {
        Prioridad.NORMAL
    }

    // El texto es el primer argumento suelto; si hubiera más, es que faltan comillas.
    val texto = argumentos.first()
    return ResultadoParseo.Ok(Comando.Anadir(texto, prioridad))
}

private fun conIdentificador(
    argumentos: List<String>,
    comando: String,
    construir: (Int) -> Comando,
): ResultadoParseo {
    val crudo = argumentos.firstOrNull()
        ?: return ResultadoParseo.Error("«$comando» necesita el número de la tarea")

    val id = crudo.toIntOrNull()
        ?: return ResultadoParseo.Error("«$crudo» no es un número de tarea")

    if (id <= 0) return ResultadoParseo.Error("el número de tarea debe ser positivo, no $id")

    return ResultadoParseo.Ok(construir(id))
}

/** Sugerir el comando más parecido cuesta muy poco y se agradece mucho. */
private fun mensajeDeComandoDesconocido(nombre: String): String {
    val parecido = COMANDOS_CONOCIDOS.firstOrNull { conocido ->
        conocido.take(3) == nombre.take(3) ||
            conocido.length == nombre.length && diferenciasEntre(conocido, nombre) <= 2
    }
    return buildString {
        append("no conozco el comando «$nombre»")
        if (parecido != null) append(". ¿Querías decir «$parecido»?")
        else append(". Escribe «ayuda» para ver la lista")
    }
}

/** Cuántos caracteres difieren, posición a posición. La versión de andar por casa. */
private fun diferenciasEntre(a: String, b: String): Int =
    a.zip(b).count { (uno, otro) -> uno != otro }

// -- La consola que ejecuta los comandos -----------------------------------------------------------

data class Tarea(val id: Int, val texto: String, val prioridad: Prioridad, val hecha: Boolean = false)

/**
 * Une el parser con un estado: la lista de tareas.
 *
 * El `when` de `ejecutar` es exhaustivo sin `else`: si mañana añades un comando al
 * `sealed interface`, esta función deja de compilar hasta que lo trates.
 */
class ConsolaDeTareas {

    private val tareas = mutableListOf<Tarea>()
    private var siguienteId = 1

    fun tareas(): List<Tarea> = tareas.toList()

    fun ejecutar(linea: String): String = when (val resultado = parsearComando(linea)) {
        is ResultadoParseo.Error -> "✗ ${resultado.mensaje}"
        is ResultadoParseo.Ok -> when (val comando = resultado.comando) {
            is Comando.Anadir -> {
                val tarea = Tarea(siguienteId++, comando.texto, comando.prioridad)
                tareas += tarea
                "✓ añadida #${tarea.id}: ${tarea.texto} (${tarea.prioridad.name.lowercase()})"
            }

            is Comando.Listar -> {
                val visibles = tareas.filter { comando.incluirHechas || !it.hecha }
                if (visibles.isEmpty()) {
                    "(no hay tareas que mostrar)"
                } else {
                    visibles.joinToString("\n") { tarea ->
                        val marca = if (tarea.hecha) "✓" else "·"
                        "$marca #${tarea.id} ${tarea.texto} [${tarea.prioridad.name.lowercase()}]"
                    }
                }
            }

            is Comando.MarcarHecha -> cambiar(comando.id) { it.copy(hecha = true) }
                ?.let { "✓ tarea #${comando.id} marcada como hecha" }
                ?: "✗ no existe la tarea #${comando.id}"

            is Comando.Borrar -> if (tareas.removeAll { it.id == comando.id }) {
                "✓ tarea #${comando.id} borrada"
            } else {
                "✗ no existe la tarea #${comando.id}"
            }

            Comando.Ayuda -> AYUDA
            Comando.Vacio -> ""
        }
    }

    private fun cambiar(id: Int, transformar: (Tarea) -> Tarea): Tarea? {
        val indice = tareas.indexOfFirst { it.id == id }
        if (indice < 0) return null
        val cambiada = transformar(tareas[indice])
        tareas[indice] = cambiada
        return cambiada
    }

    private companion object {
        const val AYUDA = """Comandos disponibles:
  añadir "texto" [--prioridad alta|normal|baja]
  listar [--todas]
  hecha <número>
  borrar <número>
  ayuda"""
    }
}

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun describirParseo(resultado: ResultadoParseo): String = when (resultado) {
    is ResultadoParseo.Ok -> "✓ ${resultado.comando}"
    is ResultadoParseo.Error -> "✗ ${resultado.mensaje}"
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `Comando.Editar(id, texto)` y deja que el compilador te lleve a los dos
//     sitios donde hay que tratarlo.
//  2. Prueba `añadir Comprar pan` (sin comillas) y decide si el mensaje de error
//     debería avisar de que faltan.
//  3. Haz que `trocear` admita comillas simples además de dobles.
