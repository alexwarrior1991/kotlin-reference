package com.alejandro.c22destructuring

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  22.1 · Declaraciones de desestructuración
//
//  QUÉ ES
//    `val (a, b) = objeto` reparte un objeto en varias variables. Por debajo llama a
//    `objeto.component1()` y `objeto.component2()`.
//
//  POR QUÉ IMPORTA
//    Convierte `val nombre = par.first; val edad = par.second` en una línea con
//    nombres con significado. Y en bucles sobre mapas es lo que hace que
//    `for ((clave, valor) in mapa)` se lea como una frase.
//
//  ERRORES COMUNES
//    · Olvidar que el orden es POSICIONAL, no por nombre: reordenar las propiedades
//      de la data class rompe todas las desestructuraciones en silencio.
//    · Desestructurar objetos de más de tres o cuatro componentes.
//    · Creer que funciona con cualquier clase (hace falta `componentN`).
// =====================================================================================

private data class Usuario(val nombre: String, val edad: Int, val ciudad: String)

private val usuarios = listOf(
    Usuario("Ana", 34, "Madrid"),
    Usuario("Luis", 28, "Sevilla"),
    Usuario("Marta", 41, "Bilbao"),
)

/**
 * Lo básico.
 */
fun demoBasics() {
    section("Con una data class")

    val usuario = Usuario("Ana", 34, "Madrid")

    val (nombre, edad, ciudad) = usuario
    show("val (nombre, edad, ciudad) = usuario", "$nombre / $edad / $ciudad")

    bullet("Equivale a tres líneas: `val nombre = usuario.component1()`, etc.")
    bullet("Y `component1()` de una data class devuelve la PRIMERA propiedad del")
    bullet("constructor primario, `component2()` la segunda, y así.")

    section("Se pueden coger menos componentes de los que hay")

    val (soloNombre) = usuario
    show("val (soloNombre) = usuario", soloNombre)

    val (n, e) = usuario
    show("val (n, e) = usuario", "$n, $e")

    bullet("Puedes parar donde quieras, pero siempre desde el principio.")

    section("Ignorar con guion bajo")

    // No se puede saltar el primero SIN nombrarlo, pero sí marcarlo como ignorado.
    val (_, _, soloCiudad) = usuario
    show("val (_, _, soloCiudad) = usuario", soloCiudad)

    bullet("`_` documenta que ese componente no te interesa.")
    bullet("Sin él tendrías que inventar un nombre que luego no usas.")

    section("Con Pair y Triple")

    val par = "clave" to 42
    val (clave, valor) = par
    show("val (clave, valor) = \"clave\" to 42", "$clave=$valor")

    val triple = Triple("a", 1, true)
    val (primero, segundo, tercero) = triple
    show("val (a, b, c) = Triple(...)", "$primero / $segundo / $tercero")

    bullet("Pair y Triple son data classes de la biblioteca estándar.")
    bullet("Para más de dos o tres valores, una data class propia se lee mucho mejor.")

    section("Con `var`")

    var (a, b) = 1 to 2
    a += 10
    b += 20
    show("tras modificar", "$a, $b")
    bullet("`var (a, b) = ...` crea dos variables modificables.")
}

/**
 * En bucles.
 */
fun demoInLoops() {
    section("Sobre una lista de objetos")

    val resumen = buildList {
        for ((nombre, edad) in usuarios) {
            add("$nombre tiene $edad")
        }
    }
    show("for ((nombre, edad) in usuarios)", resumen)

    section("Sobre un Map: el caso estrella")

    val poblacion = mapOf("Madrid" to 3_200_000, "Sevilla" to 690_000)

    val lineas = buildList {
        for ((ciudad, habitantes) in poblacion) {
            add("$ciudad: $habitantes")
        }
    }
    show("for ((ciudad, habitantes) in mapa)", lineas)

    bullet("`Map.Entry` tiene `component1()` (la clave) y `component2()` (el valor)")
    bullet("como extensiones de la biblioteca estándar. Por eso funciona.")

    section("Con withIndex")

    val numerados = buildList {
        for ((indice, usuario) in usuarios.withIndex()) {
            add("${indice + 1}. ${usuario.nombre}")
        }
    }
    show("for ((i, u) in lista.withIndex())", numerados)

    section("Combinando los dos niveles")

    // `withIndex` da un IndexedValue, y el valor es a su vez desestructurable...
    // pero no se puede anidar la desestructuración. Hay que hacerlo en dos pasos.
    val dosNiveles = buildList {
        for ((indice, usuario) in usuarios.withIndex()) {
            val (nombre, edad) = usuario
            add("$indice: $nombre ($edad)")
        }
    }
    show("desestructuración anidada, en dos pasos", dosNiveles)

    bullet("Kotlin NO permite `for ((i, (nombre, edad)) in ...)`: no hay anidamiento.")
}

/**
 * En lambdas.
 */
fun demoInLambdas() {
    section("Un parámetro desestructurado")

    show("sin desestructurar", usuarios.map { "${it.nombre} (${it.edad})" })
    show("desestructurando", usuarios.map { (nombre, edad) -> "$nombre ($edad)" })

    bullet("Los paréntesis alrededor de los parámetros son lo que activa la")
    bullet("desestructuración. Sin ellos serían DOS parámetros distintos.")

    section("La diferencia que confunde")

    val pares = listOf(1 to "uno", 2 to "dos")

    // `{ (a, b) -> ... }` : UN parámetro (el Pair), desestructurado.
    show("{ (a, b) -> ... }  un Pair desestructurado", pares.map { (n, t) -> "$n=$t" })

    // `{ a, b -> ... }` : DOS parámetros. Sólo vale si la función los pasa.
    val conDos = pares.fold("") { acumulado, par -> acumulado + par.second }
    show("{ a, b -> ... }  dos parámetros", conDos)

    section("Sobre un Map")

    val precios = mapOf("pan" to 1.20, "leche" to 0.95)

    show("forEach { (k, v) -> }", precios.map { (producto, precio) -> "$producto: $precio €" })
    show("filter { (_, v) -> }", precios.filter { (_, precio) -> precio > 1.0 }.keys)

    section("Ignorando componentes")

    show("sólo el segundo", pares.map { (_, texto) -> texto.uppercase() })
    bullet("El `_` también funciona dentro de la desestructuración de una lambda.")
}

/**
 * Devolver varios valores.
 */
fun demoReturningMultipleValues() {
    section("Con Pair")

    val (minimo, maximo) = extremosDe(listOf(5, 3, 9, 1))
    show("val (min, max) = extremosDe(...)", "min=$minimo max=$maximo")

    section("Con una data class: mucho mejor")

    val (media, mediana, moda) = estadisticasDe(listOf(1, 2, 2, 3, 9))
    show("val (media, mediana, moda) = ...", "media=%.1f mediana=$mediana moda=$moda".format(media))

    bullet("Con Pair<Double, Int> nadie sabe qué es cada cosa.")
    bullet("Con `Estadisticas(media, mediana, moda)` la firma se explica sola,")
    bullet("y quien no quiera desestructurar puede usar `.media` directamente.")

    section("La regla")

    bullet("Dos valores muy relacionados y obvios (mín/máx, clave/valor) → Pair.")
    bullet("Todo lo demás → data class con nombres.")
    bullet("Nunca Triple en una API pública: `Triple<String, Int, Boolean>` no dice nada.")

    section("Y el caso que ya conoces")

    val (validos, invalidos) = listOf("1", "x", "3").partition { it.toIntOrNull() != null }
    show("partition devuelve un Pair", "válidos=$validos inválidos=$invalidos")
}

private fun extremosDe(numeros: List<Int>): Pair<Int, Int> = numeros.min() to numeros.max()

private data class Estadisticas(val media: Double, val mediana: Int, val moda: Int)

private fun estadisticasDe(numeros: List<Int>): Estadisticas {
    val ordenados = numeros.sorted()
    return Estadisticas(
        media = numeros.average(),
        mediana = ordenados[ordenados.size / 2],
        moda = numeros.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: 0,
    )
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Intenta `val (_, _, _, cuarto) = usuario` y lee el error: sólo hay 3 componentes.
//  2. Cambia el orden de `edad` y `ciudad` en Usuario y mira cuántas demos se rompen
//     SIN que el compilador diga nada (la de `val (n, e) = usuario` sí fallará; la de
//     `(nombre, edad)` en la lambda... también, por el tipo. Prueba con dos String).
//  3. Escribe `for ((i, (n, e)) in usuarios.withIndex())` y comprueba que no compila.
//  4. Convierte `extremosDe` para que devuelva una data class y compara las llamadas.
