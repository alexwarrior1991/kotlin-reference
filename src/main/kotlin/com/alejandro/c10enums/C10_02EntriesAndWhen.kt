package com.alejandro.c10enums

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import java.util.EnumMap
import java.util.EnumSet

// =====================================================================================
//  10.2 · entries, valueOf, ordinal y el when exhaustivo
//
//  QUÉ ES
//    Las operaciones que todo enum trae de serie: listar sus constantes, convertir
//    texto a constante, y usarlas en un `when` que el compilador comprueba.
//
//  POR QUÉ IMPORTA
//    El `when` exhaustivo es la razón principal para usar un enum en lugar de un
//    String: cuando añadas una constante nueva, el compilador te señalará TODOS los
//    sitios que hay que actualizar. Es refactorización asistida gratis.
//
//  ERRORES COMUNES
//    · Usar `values()` (crea un array nuevo en cada llamada) en lugar de `entries`.
//    · Llamar a `valueOf` con texto de usuario sin capturar la excepción.
//    · Persistir `ordinal`: basta reordenar las constantes para corromper los datos.
//    · Poner `else` en un `when` sobre enum y perder la comprobación de exhaustividad.
// =====================================================================================

/**
 * `entries` frente a `values()`.
 */
fun demoEntriesVsValues() {
    section("La forma moderna: entries")

    show("Prioridad.entries", Prioridad.entries)
    show("tipo", "EnumEntries<Prioridad>, que es una List<Prioridad>")
    show("entries[0]", Prioridad.entries[0])
    show("entries.size", Prioridad.entries.size)

    section("La forma antigua: values()")

    @Suppress("DEPRECATION")
    val array = Prioridad.values()
    show("Prioridad.values()", array)
    show("tipo", "Array<Prioridad>")

    section("Por qué entries es mejor")

    bullet("`values()` devuelve un ARRAY NUEVO en cada llamada, para que nadie pueda")
    bullet("modificar el interno. En un bucle, eso es basura generada sin parar.")
    bullet("`entries` devuelve siempre la MISMA lista inmutable: cero coste.")
    bullet("Además es una List, así que tiene filter, map, find... sin convertir nada.")

    section("Operaciones habituales")

    show("filtrar", Prioridad.entries.filter { it.urgente })
    show("buscar", Prioridad.entries.find { it.name.startsWith("M") })
    show("mapear", Prioridad.entries.map { it.etiqueta })
    show("la primera urgente", Prioridad.entries.first { it.urgente })

    bullet("`entries` está disponible desde Kotlin 1.9. `values()` sigue funcionando")
    bullet("por compatibilidad, pero el IDE ya sugiere cambiarlo.")
}

/**
 * De texto a constante: `valueOf` y sus alternativas seguras.
 */
fun demoValueOf() {
    section("valueOf: lanza si no existe")

    show("Prioridad.valueOf(\"ALTA\")", Prioridad.valueOf("ALTA"))

    val falloValueOf = try {
        Prioridad.valueOf("URGENTÍSIMA").toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException"
    }
    show("Prioridad.valueOf(\"URGENTÍSIMA\")", falloValueOf)

    bullet("Distingue mayúsculas: `valueOf(\"alta\")` también falla.")

    section("La versión segura: entries.find")

    show("desdeTexto(\"ALTA\")", desdeTexto("ALTA"))
    show("desdeTexto(\"alta\")   (ignorando mayúsculas)", desdeTexto("alta"))
    show("desdeTexto(\"basura\")", desdeTexto("basura"))

    bullet("Para texto que viene de fuera (JSON, formulario, fichero), SIEMPRE la segura.")

    section("Con valor por defecto")

    show("desdeTextoODefecto(\"basura\")", desdeTextoODefecto("basura"))

    section("enumValueOf / enumValues genéricos")

    // Versiones `reified` de la biblioteca estándar, útiles cuando el enum concreto
    // es un parámetro de tipo genérico. Se verán en el capítulo 12.
    show("enumValueOf<Prioridad>(\"BAJA\")", enumValueOf<Prioridad>("BAJA"))
    show("enumValues<Prioridad>()", enumValues<Prioridad>())
    bullet("Sirven cuando el enum concreto es un parámetro de tipo genérico `T`,")
    bullet("donde no puedes escribir `Prioridad.entries` porque no conoces el tipo.")
    bullet("Existe además `enumEntries<T>()` como equivalente moderno de enumValues.")
}

/**
 * `ordinal`: úsalo poco y nunca lo guardes.
 */
fun demoOrdinal() {
    section("Qué es")

    Prioridad.entries.forEach { show(it.name, "ordinal=${it.ordinal}") }
    bullet("Es la posición en la DECLARACIÓN, empezando en 0.")

    section("Por qué no debes persistirlo")

    bullet("Guardas ALTA como 2 en la base de datos.")
    bullet("Mañana alguien añade CRÍTICA entre MEDIA y ALTA, por orden lógico.")
    bullet("Ahora el 2 significa CRÍTICA. Todos los datos históricos están mal.")
    bullet("Y no hay ningún error: el programa sigue funcionando, mintiendo.")

    section("Qué hacer en su lugar")

    // Guarda el NOMBRE, o mejor aún, un código explícito que nunca cambie.
    show("guardar el nombre", Prioridad.ALTA.name)
    show("guardar un código propio", Prioridad.ALTA.codigo)
    show("recuperar por código", Prioridad.entries.find { it.codigo == 30 })

    bullet("El nombre es estable mientras no renombres la constante.")
    bullet("Un código explícito es estable siempre: es un dato más, no una posición.")

    section("Usos legítimos del ordinal")

    bullet("Comparar (`<`, `>`) cuando el orden de declaración ES el orden lógico.")
    bullet("Indexar un array interno de tamaño fijo, dentro de la misma ejecución.")
    bullet("EnumMap y EnumSet lo usan por dentro, y por eso son tan rápidos.")
}

/**
 * El `when` exhaustivo: la razón de ser de los enums.
 */
fun demoExhaustiveWhen() {
    section("Sin else: el compilador comprueba que están todos")

    Prioridad.entries.forEach { prioridad ->
        show(prioridad.name, plazoDeRespuesta(prioridad))
    }

    bullet("`plazoDeRespuesta` no tiene `else`, y aun así compila:")
    bullet("el compilador ve que las cuatro constantes están cubiertas.")

    section("Qué pasa si añades una constante")

    bullet("Añade CRITICA a Prioridad y `plazoDeRespuesta` DEJA de compilar:")
    bullet("  \"'when' expression must be exhaustive, add necessary 'CRITICA' branch\"")
    bullet("El compilador te lleva de la mano por todos los sitios a actualizar.")

    section("Por qué NO poner `else`")

    // Con `else`, el código compila igual al añadir una constante... y se comporta
    // como si la nueva fuera "cualquier otra cosa". El fallo aparece en ejecución.
    show("versión con else, con ALTA", plazoConElse(Prioridad.ALTA))
    show("versión con else, con BAJA", plazoConElse(Prioridad.BAJA))

    bullet("`else` convierte un error de compilación en un bug silencioso.")
    bullet("Regla: en un `when` sobre enum o sealed, NUNCA pongas `else`...")
    bullet("...salvo que de verdad quieras agrupar 'todo lo demás' a propósito.")

    section("Desde Kotlin 1.7, también como sentencia")

    // Antes, un `when` sin usar el valor podía dejarse incompleto. Ahora también
    // debe ser exhaustivo si el sujeto es un enum o una sealed.
    bullet("Un `when` sobre enum debe ser exhaustivo aunque no uses su resultado.")
}

/**
 * EnumMap y EnumSet: colecciones optimizadas para enums.
 */
fun demoEnumCollections() {
    section("EnumSet: un conjunto de constantes")

    val urgentes = EnumSet.of(Prioridad.ALTA, Prioridad.MEDIA)
    show("EnumSet.of(ALTA, MEDIA)", urgentes)
    show("contiene ALTA", Prioridad.ALTA in urgentes)
    show("EnumSet.complementOf(urgentes)", EnumSet.complementOf(urgentes))
    show("EnumSet.allOf", EnumSet.allOf(Prioridad::class.java))

    bullet("Por dentro es un campo de bits: ocupa un long y las operaciones son")
    bullet("un AND o un OR. Muchísimo más rápido que un HashSet.")

    section("EnumMap: un mapa con claves enum")

    val responsables = EnumMap<Prioridad, String>(Prioridad::class.java)
    responsables[Prioridad.ALTA] = "guardia"
    responsables[Prioridad.BAJA] = "cola normal"
    show("EnumMap", responsables)
    show("orden: siempre el de declaración", responsables.keys)

    bullet("Por dentro es un array indexado por `ordinal`: sin hash, sin colisiones.")
    bullet("Y siempre itera en el orden de declaración, que suele ser el que quieres.")

    section("Alternativa 100% Kotlin")

    // Para la mayoría de los casos, un mapa normal creado desde `entries` basta.
    val plazos = Prioridad.entries.associateWith { plazoDeRespuesta(it) }
    show("entries.associateWith { }", plazos)

    bullet("EnumMap/EnumSet vienen de Java y hay que importarlos de java.util.")
    bullet("Úsalos si el rendimiento importa; si no, `associateWith` se lee mejor.")
}

// -- El enum que usan las demos -------------------------------------------------------

private enum class Prioridad(val codigo: Int, val etiqueta: String, val urgente: Boolean) {
    BAJA(10, "Puede esperar", false),
    MEDIA(20, "Esta semana", false),
    ALTA(30, "Hoy", true),
    BLOQUEANTE(40, "Ahora mismo", true),
}

/** Conversión segura: devuelve null en lugar de lanzar. */
private fun desdeTexto(texto: String): Prioridad? =
    Prioridad.entries.find { it.name.equals(texto, ignoreCase = true) }

/** Conversión con valor por defecto. */
private fun desdeTextoODefecto(texto: String): Prioridad = desdeTexto(texto) ?: Prioridad.MEDIA

/** `when` exhaustivo SIN else: el compilador vigila que estén todas. */
private fun plazoDeRespuesta(prioridad: Prioridad): String = when (prioridad) {
    Prioridad.BAJA -> "30 días"
    Prioridad.MEDIA -> "7 días"
    Prioridad.ALTA -> "24 horas"
    Prioridad.BLOQUEANTE -> "1 hora"
}

/** La misma lógica CON else: compila siempre, pero pierde la comprobación. */
private fun plazoConElse(prioridad: Prioridad): String = when (prioridad) {
    Prioridad.ALTA -> "24 horas"
    Prioridad.BLOQUEANTE -> "1 hora"
    else -> "sin prisa"      // ← una constante nueva caería aquí en silencio
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade CRITICA(35, "Muy urgente", true) a Prioridad y mira qué deja de compilar:
//     `plazoDeRespuesta` sí, `plazoConElse` no. Ésa es toda la lección.
//  2. Cambia `entries` por `values()` en un bucle grande y piensa en la basura generada.
//  3. Reordena las constantes y comprueba cómo cambian todos los `ordinal`.
//  4. Sustituye el EnumMap por un `mapOf` normal y compara el orden de iteración.
