package com.alejandro.c15lambdas

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  15.3 · Closures: lambdas que capturan su entorno
//
//  QUÉ ES
//    Una lambda puede LEER y MODIFICAR las variables del ámbito donde se escribió,
//    aunque se ejecute mucho más tarde y en otro sitio. A eso se le llama "cierre"
//    (closure).
//
//  POR QUÉ IMPORTA
//    Es lo que hace que `var total = 0; lista.forEach { total += it }` funcione, y lo
//    que permite las fábricas de funciones. También es la causa de un par de sorpresas
//    clásicas que conviene conocer antes de sufrirlas.
//
//  ERRORES COMUNES
//    · Suponer que la lambda captura una COPIA del valor: captura la VARIABLE.
//    · Crear lambdas dentro de un bucle esperando que cada una vea su propio valor.
//    · Guardar una lambda que captura un objeto enorme y no soltarla nunca (fuga).
// =====================================================================================

/**
 * Lo básico: capturar y modificar.
 */
fun demoBasicCapture() {
    section("Leer una variable de fuera")

    val factor = 10
    val multiplicar: (Int) -> Int = { it * factor }   // captura `factor`
    show("multiplicar(5) con factor=10", multiplicar(5))

    section("Modificar una variable de fuera")

    // En Java, una lambda sólo puede capturar variables `final` o "efectivamente
    // finales": esto NO compilaría. Kotlin sí lo permite.
    var total = 0
    listOf(1, 2, 3, 4).forEach { total += it }
    show("total tras forEach", total)

    bullet("En Java habría que usar un array de un elemento o una AtomicInteger.")
    bullet("Kotlin lo permite porque envuelve la variable en un objeto (`Ref`) por detrás.")

    section("Captura la VARIABLE, no el valor")

    var contador = 0
    val leerContador = { contador }        // captura la variable, no el 0 de ahora

    show("antes de cambiar", leerContador())
    contador = 99
    show("después de cambiar", leerContador())

    bullet("La lambda ve el valor ACTUAL, no el que había al crearla.")
    bullet("Es la diferencia clave entre capturar por valor y capturar por referencia.")

    section("Y por eso puede sobrevivir a su ámbito")

    val contadorIndependiente = crearContador()
    show("primera llamada", contadorIndependiente())
    show("segunda llamada", contadorIndependiente())
    show("tercera llamada", contadorIndependiente())

    bullet("`crearContador()` ya terminó, pero su variable local sigue viva:")
    bullet("la lambda la mantiene. Eso es exactamente un closure.")

    section("Cada llamada crea un cierre NUEVO")

    val otro = crearContador()
    show("otro contador, primera llamada", otro())
    show("el primero sigue por su cuenta", contadorIndependiente())
}

/**
 * La trampa del bucle.
 */
fun demoLoopCaptureTrap() {
    section("Con `for`, cada vuelta tiene su propia variable")

    // En Kotlin la variable del `for` es nueva en cada iteración, así que esto
    // funciona como esperarías.
    val funcionesFor = mutableListOf<() -> Int>()
    for (i in 1..3) {
        funcionesFor.add { i }
    }
    show("valores capturados con for", funcionesFor.map { it() })

    bullet("[1, 2, 3], que es lo intuitivo. En Java esto también funciona.")

    section("Con una variable declarada FUERA, no")

    // Aquí todas las lambdas capturan LA MISMA variable, que al final vale 4.
    val funcionesVar = mutableListOf<() -> Int>()
    var j = 1
    while (j <= 3) {
        funcionesVar.add { j }
        j++
    }
    show("valores capturados con var externa", funcionesVar.map { it() })

    bullet("[4, 4, 4]: las tres lambdas miran la MISMA variable, que acabó valiendo 4.")
    bullet("Es el bug clásico. En JavaScript con `var` pasa exactamente lo mismo.")

    section("La solución: una copia local por vuelta")

    val funcionesCorregidas = mutableListOf<() -> Int>()
    var k = 1
    while (k <= 3) {
        val copia = k              // ← cada vuelta crea su propio `copia`
        funcionesCorregidas.add { copia }
        k++
    }
    show("con copia local", funcionesCorregidas.map { it() })

    bullet("Regla: si vas a guardar la lambda para después, captura un `val` local.")
}

/**
 * Qué se captura y qué cuesta.
 */
fun demoCaptureCost() {
    section("Una lambda SIN captura es un singleton")

    // El compilador puede reutilizar la misma instancia: no depende de nada externo.
    val sinCaptura1: (Int) -> Int = { it * 2 }
    val sinCaptura2: (Int) -> Int = { it * 2 }
    show("dos lambdas idénticas sin captura", sinCaptura1(5) == sinCaptura2(5))
    bullet("Sin captura, la lambda no necesita estado: se puede crear una sola vez.")

    section("Una lambda CON captura necesita un objeto por cada creación")

    val fabricadas = (1..3).map { factor -> { n: Int -> n * factor } }
    show("tres lambdas con distinto factor", fabricadas.map { it(10) })
    bullet("Cada una guarda su `factor`: son tres objetos distintos.")

    section("Dentro de una función `inline` no se crea nada")

    // `forEach`, `map`, `let`, `apply`... son inline: el cuerpo se copia y la lambda
    // desaparece. Por eso encadenar operaciones de colección no genera objetos.
    bullet("inline (forEach, map, let, run...) → sin objeto lambda.")
    bullet("No inline (guardarla en una variable, pasarla a un constructor) → sí.")
    bullet("Capítulo 5.18 para el detalle de `inline`.")

    section("El riesgo real: fugas de memoria")

    // Si una lambda que captura un objeto grande se guarda en algo de larga vida
    // (un registro de callbacks, un singleton), ese objeto no se libera nunca.
    val registro = RegistroDeCallbacks()
    registro.registrar { "callback que no captura nada" }
    show("callbacks registrados", registro.cuantos())

    bullet("En Android fue la causa nº 1 de fugas: un listener que capturaba la")
    bullet("Activity y vivía más que ella.")
    bullet("Regla: si guardas una lambda, ten claro quién la va a quitar.")
}

/**
 * Closures en la práctica.
 */
fun demoPracticalClosures() {
    section("1. Configuración capturada")

    val formateadorEuros = crearFormateador("€", 2)
    val formateadorDolares = crearFormateador("$", 0)

    show("formateadorEuros(1234.567)", formateadorEuros(1234.567))
    show("formateadorDolares(1234.567)", formateadorDolares(1234.567))

    bullet("El símbolo y los decimales se capturan una vez y se reutilizan.")

    section("2. Acumulador con estado privado")

    val estadisticas = crearEstadisticas()
    listOf(10, 20, 5, 30).forEach { estadisticas.anotar(it) }
    show("resumen", estadisticas.resumen())

    bullet("El estado vive en el closure: nadie de fuera puede tocarlo.")
    bullet("Es encapsulación sin escribir una clase.")

    section("3. Generador perezoso")

    val siguienteId = crearGeneradorDeIds("PED")
    show("primer id", siguienteId())
    show("segundo id", siguienteId())
    show("tercer id", siguienteId())

    section("4. Recordar el último valor")

    val detectorDeCambios = crearDetectorDeCambios()
    listOf("a", "a", "b", "b", "c").forEach { valor ->
        show("valor '$valor'", if (detectorDeCambios(valor)) "CAMBIÓ" else "igual que antes")
    }
}

// -- Las funciones que usan las demos ---------------------------------------------------

/** Devuelve una lambda que recuerda su propio contador. */
private fun crearContador(): () -> Int {
    var cuenta = 0          // vive mientras viva la lambda devuelta
    return { ++cuenta }
}

private fun crearFormateador(simbolo: String, decimales: Int): (Double) -> String {
    val patron = "%.${decimales}f"
    return { cantidad -> "${patron.format(cantidad)} $simbolo" }
}

/** Un objeto cuyo estado vive en el closure, no en propiedades. */
private class Estadisticas(
    val anotar: (Int) -> Unit,
    val resumen: () -> String,
)

private fun crearEstadisticas(): Estadisticas {
    var cantidad = 0
    var total = 0
    var maximo = Int.MIN_VALUE

    return Estadisticas(
        anotar = { valor ->
            cantidad++
            total += valor
            maximo = maxOf(maximo, valor)
        },
        resumen = { "n=$cantidad total=$total max=$maximo media=${if (cantidad > 0) total / cantidad else 0}" },
    )
}

private fun crearGeneradorDeIds(prefijo: String): () -> String {
    var siguiente = 1
    return { "$prefijo-${siguiente++}" }
}

/** Devuelve true si el valor es distinto del anterior. */
private fun crearDetectorDeCambios(): (String) -> Boolean {
    var anterior: String? = null
    return { valor ->
        val cambio = valor != anterior
        anterior = valor
        cambio
    }
}

private class RegistroDeCallbacks {
    private val callbacks = mutableListOf<() -> String>()
    fun registrar(callback: () -> String) {
        callbacks.add(callback)
    }

    fun cuantos(): Int = callbacks.size
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia el `while` de demoLoopCaptureTrap por un `for` y comprueba la diferencia.
//  2. Llama dos veces a `crearContador()` y verifica que los contadores son independientes.
//  3. Añade a `crearEstadisticas` un `reiniciar()` que ponga el estado a cero.
//  4. Escribe `crearLimitador(max: Int)` que devuelva una lambda que sólo deje pasar
//     las primeras `max` llamadas.
