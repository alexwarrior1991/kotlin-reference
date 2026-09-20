package com.alejandro.c15lambdas

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  15.2 · Patrones con funciones de orden superior
//
//  QUÉ ES
//    Los usos reales de pasar funciones como parámetro: callbacks, estrategias,
//    reintentos, memoización, validadores encadenados.
//
//  POR QUÉ IMPORTA
//    En Java estos patrones necesitan una interfaz y una clase (o una anónima) cada
//    uno. En Kotlin son un parámetro de tipo función. Al bajar tanto el coste de
//    escribirlos, se usan mucho más, y el código queda más pequeño y más flexible.
//
//  ERRORES COMUNES
//    · Callbacks anidados hasta formar una escalera (para eso están las corrutinas).
//    · Tipos función ilegibles: `(String, Int, Boolean) -> List<Pair<String, Int>>`.
//    · Guardar una lambda que captura un objeto grande y no soltarla nunca.
// =====================================================================================

/** Un alias hace legible una firma que se repite. */
private typealias Validador = (String) -> String?

/** Y otro para un callback con dos resultados posibles. */
private typealias AlTerminar = (exito: Boolean, mensaje: String) -> Unit

/**
 * Patrón 1: estrategia.
 */
fun demoStrategy() {
    section("Elegir el comportamiento en tiempo de ejecución")

    val datos = listOf("banana", "kiwi", "manzana", "uva")

    // En Java: una interfaz Ordenacion + tres clases que la implementan.
    // En Kotlin: un parámetro de tipo función.
    show("alfabético", ordenarCon(datos) { a, b -> a.compareTo(b) })
    show("por longitud", ordenarCon(datos) { a, b -> a.length - b.length })
    show("inverso", ordenarCon(datos) { a, b -> b.compareTo(a) })

    section("Con las estrategias guardadas en un mapa")

    val estrategias = mapOf<String, (String, String) -> Int>(
        "alfabetico" to { a, b -> a.compareTo(b) },
        "longitud" to { a, b -> a.length - b.length },
    )
    estrategias.forEach { (nombre, estrategia) ->
        show(nombre, ordenarCon(datos, estrategia))
    }

    bullet("Añadir una estrategia es añadir una entrada al mapa, no una clase.")
}

/**
 * Patrón 2: callbacks.
 */
fun demoCallbacks() {
    section("Avisar de lo que ha pasado")

    val resultados = mutableListOf<String>()

    descargarSimulado(
        url = "https://ejemplo.com/ok",
        alTerminar = { exito, mensaje -> resultados.add("exito=$exito · $mensaje") },
    )
    descargarSimulado(
        url = "fallo",
        alTerminar = { exito, mensaje -> resultados.add("exito=$exito · $mensaje") },
    )

    resultados.forEach { bullet(it) }

    section("Callbacks separados por caso")

    // Dos lambdas, una para cada rama: se lee mejor que un booleano.
    val salida = procesar(
        entrada = "42",
        siValido = { numero -> "procesado: ${numero * 2}" },
        siInvalido = { texto -> "no es un número: '$texto'" },
    )
    show("procesar(\"42\")", salida)
    show("procesar(\"abc\")", procesar("abc", { "procesado: $it" }, { "no es un número: '$it'" }))

    section("El problema de los callbacks: la escalera")

    bullet("descargar { a -> procesar(a) { b -> guardar(b) { c -> ... } } }")
    bullet("Tres niveles y ya no se lee. Es el 'callback hell' de JavaScript.")
    bullet("La solución en Kotlin son las corrutinas: capítulo 28.")
    bullet("Con `suspend`, eso mismo se escribe en tres líneas seguidas.")
}

/**
 * Patrón 3: envolver una operación (decorador funcional).
 */
fun demoWrapping() {
    section("Reintentar una operación")

    var intentos = 0
    val resultado = conReintentos(maximo = 5) {
        intentos++
        if (intentos < 3) error("fallo simulado nº $intentos")
        "conseguido al intento $intentos"
    }
    show("conReintentos", resultado)

    section("Cuando se agotan los intentos")

    var siempreFalla = 0
    val agotado = conReintentos(maximo = 3) {
        siempreFalla++
        error("siempre falla")
    }
    show("tras 3 intentos fallidos", agotado)
    show("veces que se ejecutó", siempreFalla)

    section("Medir sin ensuciar el código medido")

    val (valor, descripcion) = conMedicion {
        (1..100_000).sum()
    }
    show("valor calculado", valor)
    show("medición", descripcion)

    bullet("El bloque medido no sabe que lo están midiendo: no hay acoplamiento.")

    section("Registrar entrada y salida")

    val traza = mutableListOf<String>()
    val conTraza = conRegistro("calcular", traza) { 6 * 7 }
    show("resultado", conTraza)
    traza.forEach { bullet(it) }
}

/**
 * Patrón 4: componer y encadenar.
 */
fun demoComposition() {
    section("Encadenar validadores")

    // Cada validador devuelve null si todo va bien, o el error si no.
    val noVacio: Validador = { if (it.isBlank()) "no puede estar vacío" else null }
    val minimoTres: Validador = { if (it.length < 3) "mínimo 3 caracteres" else null }
    val sinEspacios: Validador = { if (" " in it) "no puede tener espacios" else null }

    val validarUsuario = combinar(noVacio, minimoTres, sinEspacios)

    listOf("", "ab", "un usuario", "alejandro").forEach { entrada ->
        show("validar('$entrada')", validarUsuario(entrada) ?: "válido")
    }

    bullet("Cada validador se prueba por separado y se combinan sin tocarlos.")
    bullet("Añadir una regla es añadir una lambda a la lista.")

    section("Componer transformaciones")

    val limpiar: (String) -> String = { it.trim() }
    val minusculas: (String) -> String = { it.lowercase() }
    val sinAcentos: (String) -> String = { it.replace("á", "a").replace("é", "e") }

    val normalizar = limpiar then minusculas then sinAcentos
    show("normalizar('  José ÁNGEL  ')", normalizar("  José ÁNGEL  "))

    bullet("`then` es una función de extensión sobre tipos función: 15 caracteres")
    bullet("que te dan composición para todo el proyecto.")

    section("Memoizar: recordar resultados")

    var llamadasReales = 0
    val cuadradoLento = memoizar<Int, Int> { n ->
        llamadasReales++
        n * n
    }

    show("cuadradoLento(4)", cuadradoLento(4))
    show("cuadradoLento(4) otra vez", cuadradoLento(4))
    show("cuadradoLento(5)", cuadradoLento(5))
    show("llamadas reales a la función", llamadasReales)

    bullet("Dos llamadas con el mismo argumento, un solo cálculo.")
    bullet("Sólo es correcto si la función es PURA: mismo argumento, mismo resultado.")
}

// -- Las funciones que usan las demos ---------------------------------------------------

private fun ordenarCon(datos: List<String>, comparar: (String, String) -> Int): List<String> =
    datos.sortedWith { a, b -> comparar(a, b) }

/** Simula una descarga y avisa por callback. No hay red de por medio. */
private fun descargarSimulado(url: String, alTerminar: AlTerminar) {
    if (url.startsWith("https://")) {
        alTerminar(true, "descargado $url (simulado)")
    } else {
        alTerminar(false, "URL no válida: $url")
    }
}

/** Dos callbacks, uno por rama: más claro que devolver un booleano. */
private fun procesar(
    entrada: String,
    siValido: (Int) -> String,
    siInvalido: (String) -> String,
): String {
    val numero = entrada.toIntOrNull()
    return if (numero != null) siValido(numero) else siInvalido(entrada)
}

/** Envuelve un bloque y lo reintenta si lanza. */
private fun conReintentos(maximo: Int, bloque: () -> String): String {
    var ultimoError: String? = null
    repeat(maximo) { intento ->
        val resultado = runCatching(bloque)
        if (resultado.isSuccess) return resultado.getOrThrow()
        ultimoError = resultado.exceptionOrNull()?.message
        if (intento == maximo - 1) return "agotados $maximo intentos (último error: $ultimoError)"
    }
    return "agotados $maximo intentos (último error: $ultimoError)"
}

/** Mide un bloque sin que el bloque se entere. */
private fun <T> conMedicion(bloque: () -> T): Pair<T, String> {
    val inicio = System.nanoTime()
    val valor = bloque()
    val transcurrido = System.nanoTime() - inicio
    // No imprimimos el número exacto: variaría en cada ejecución.
    return valor to if (transcurrido >= 0) "medido correctamente" else "imposible"
}

/** Registra entrada y salida alrededor de un bloque. */
private fun <T> conRegistro(nombre: String, destino: MutableList<String>, bloque: () -> T): T {
    destino.add("→ entrando en $nombre")
    val resultado = bloque()
    destino.add("← saliendo de $nombre con $resultado")
    return resultado
}

/** Combina varios validadores: devuelve el primer error que encuentre. */
private fun combinar(vararg validadores: Validador): Validador = { valor ->
    validadores.firstNotNullOfOrNull { it(valor) }
}

/** Composición de funciones, de izquierda a derecha. */
private infix fun <A, B, C> ((A) -> B).then(siguiente: (B) -> C): (A) -> C =
    { entrada -> siguiente(this(entrada)) }

/** Devuelve una versión de la función que recuerda los resultados. */
private fun <T, R> memoizar(funcion: (T) -> R): (T) -> R {
    val cache = mutableMapOf<T, R>()
    return { argumento -> cache.getOrPut(argumento) { funcion(argumento) } }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade un validador que exija al menos un dígito y mételo en `combinar`.
//  2. Haz que `conReintentos` espere entre intentos (pista: Thread.sleep, y mejor aún
//     `delay` cuando llegues al capítulo 28).
//  3. Escribe `compose` (de derecha a izquierda) y compáralo con `then`.
//  4. Añade a `memoizar` un límite de tamaño para que la caché no crezca sin fin.
