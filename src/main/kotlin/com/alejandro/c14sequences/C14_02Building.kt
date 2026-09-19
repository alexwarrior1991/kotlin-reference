package com.alejandro.c14sequences

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show
import kotlin.time.measureTime

// =====================================================================================
//  14.2 · Construir secuencias y cuándo usarlas
//
//  QUÉ ES
//    Las tres formas de crear una Sequence (`asSequence`, `generateSequence`,
//    `sequence { }`), las secuencias infinitas, y el criterio para decidir cuándo
//    merecen la pena.
//
//  POR QUÉ IMPORTA
//    Una secuencia infinita suena a truco de laboratorio, pero es la forma natural de
//    expresar "los números primos", "las líneas de un fichero" o "los reintentos con
//    espera creciente": series que no sabes dónde acaban hasta que llegas.
//
//  ERRORES COMUNES
//    · Llamar a `toList()` sobre una secuencia infinita (se queda colgado para siempre).
//    · Usar secuencias para colecciones pequeñas y perder rendimiento.
//    · Confundir `yield` con `return`.
// =====================================================================================

/**
 * Las tres formas de crear una secuencia.
 */
fun demoCreatingSequences() {
    section("1. asSequence(): desde una colección existente")

    val desdeColeccion = listOf(1, 2, 3).asSequence()
    show("listOf(1,2,3).asSequence().toList()", desdeColeccion.toList())
    bullet("La más frecuente: ya tienes los datos y sólo quieres evaluación perezosa.")

    section("2. sequenceOf(): elementos sueltos")

    show("sequenceOf(1, 2, 3).toList()", sequenceOf(1, 2, 3).toList())

    section("3. generateSequence(): a partir del anterior")

    // La función recibe el elemento anterior y devuelve el siguiente, o null para parar.
    val potencias = generateSequence(1) { if (it < 500) it * 2 else null }
    show("potencias de 2 hasta 500", potencias.toList())

    val cuentaAtras = generateSequence(5) { if (it > 1) it - 1 else null }
    show("cuenta atrás", cuentaAtras.toList())

    // Con un solo lambda (sin semilla) se llama hasta que devuelve null.
    var contador = 0
    val hastaCinco = generateSequence { if (contador < 5) contador++ else null }
    show("generateSequence { } sin semilla", hastaCinco.toList())

    section("4. sequence { }: el constructor con yield")

    // Dentro del bloque puedes emitir valores uno a uno con `yield`, o de golpe con
    // `yieldAll`. Es una función suspendida restringida: se pausa entre cada valor.
    val personalizada = sequence {
        yield(1)
        yield(2)
        yieldAll(listOf(3, 4))
        yieldAll(generateSequence(5) { if (it < 7) it + 1 else null })
    }
    show("sequence { yield ... }", personalizada.toList())

    bullet("`yield` entrega UN valor y espera; `yieldAll`, varios de una vez.")
    bullet("El bloque se pausa y se reanuda: es la misma maquinaria que las corrutinas.")
}

/**
 * Secuencias infinitas.
 */
fun demoInfiniteSequences() {
    section("Infinitas, pero seguras si las cortas")

    // Sin el `null` de parada, la secuencia no termina nunca. Es perfectamente válida
    // mientras uses una operación que corte: take, first, takeWhile...
    val naturales = generateSequence(1) { it + 1 }

    show("take(10)", naturales.take(10).toList())
    show("first { it % 7 == 0 }", naturales.first { it % 7 == 0 })
    show("takeWhile { it < 6 }", naturales.takeWhile { it < 6 }.toList())
    show("filter y take combinados", naturales.filter { it % 3 == 0 }.take(5).toList())

    bullet("¡NUNCA llames a toList(), count(), sum() o max() sobre una infinita!")
    bullet("Se quedaría girando para siempre. Corta SIEMPRE antes.")

    section("Fibonacci")

    val fibonacci = generateSequence(0 to 1) { (a, b) -> b to (a + b) }.map { it.first }
    show("primeros 12", fibonacci.take(12).toList())
    show("el primero mayor que 1000", fibonacci.first { it > 1000 })

    section("Números primos con sequence { }")

    show("primeros 10 primos", primos().take(10).toList())

    section("Espera creciente para reintentos")

    val esperas = generateSequence(100L) { (it * 2).coerceAtMost(3_000L) }
    show("backoff exponencial, 6 intentos", esperas.take(6).toList())
    bullet("Un patrón real: 100ms, 200ms, 400ms... con techo de 3 segundos.")

    section("Ciclos")

    val turnos = generateSequence(0) { (it + 1) % 3 }.map { listOf("Ana", "Luis", "Marta")[it] }
    show("reparto cíclico de 7 tareas", turnos.take(7).toList())
}

/**
 * Cuándo usar secuencias y cuándo no.
 */
fun demoWhenToUse() {
    section("SÍ usa secuencias cuando...")

    bullet("La colección es grande (miles de elementos o más).")
    bullet("Encadenas VARIAS operaciones (cada una ahorra una lista intermedia).")
    bullet("Vas a cortar antes del final: `first`, `any`, `take`, `find`.")
    bullet("La fuente es infinita o de tamaño desconocido (un fichero, un flujo).")
    bullet("Generar el siguiente elemento es caro y quizá no lo necesites.")

    section("NO uses secuencias cuando...")

    bullet("La colección es pequeña: montar la secuencia cuesta más de lo que ahorra.")
    bullet("Haces UNA sola operación: no hay listas intermedias que evitar.")
    bullet("Necesitas índices, `size`, o recorrerla varias veces.")
    bullet("La operación es `sorted`: de todas formas tiene que materializar todo.")

    section("La regla de bolsillo")

    bullet("Menos de ~1.000 elementos o una sola operación → colección.")
    bullet("Muchos elementos y varias operaciones encadenadas → secuencia.")
    bullet("Y ante la duda, mide: las intuiciones sobre rendimiento suelen fallar.")
}

/**
 * Una medición orientativa.
 */
fun demoPerformanceComparison() {
    section("Aviso importante antes de mirar los números")

    bullet("Estas mediciones son ORIENTATIVAS, no un benchmark.")
    bullet("La JVM optimiza sobre la marcha (JIT), así que la primera ejecución es")
    bullet("más lenta, y el recolector de basura puede entrar en cualquier momento.")
    bullet("Para medir en serio se usa JMH, que calienta la JVM y repite muchas veces.")
    bullet("Lo que SÍ es fiable aquí es el número de OPERACIONES, no los milisegundos.")

    val datos = (1..300_000).toList()

    section("Caso 1: varias operaciones y consumir todo")

    val tiempoLista = measureTime {
        datos.map { it * 2 }.filter { it % 3 == 0 }.map { it + 1 }.count()
    }
    val tiempoSecuencia = measureTime {
        datos.asSequence().map { it * 2 }.filter { it % 3 == 0 }.map { it + 1 }.count()
    }
    show("con lista", tiempoLista)
    show("con secuencia", tiempoSecuencia)
    bullet("Aquí la secuencia suele ganar: se ahorra dos listas de 300.000 elementos.")

    section("Caso 2: cortar pronto")

    var opsLista = 0
    val cortarLista = measureTime {
        datos.map { opsLista++; it * 2 }.first { it > 100 }
    }
    var opsSecuencia = 0
    val cortarSecuencia = measureTime {
        datos.asSequence().map { opsSecuencia++; it * 2 }.first { it > 100 }
    }
    show("lista: tiempo", cortarLista)
    show("lista: operaciones", opsLista)
    show("secuencia: tiempo", cortarSecuencia)
    show("secuencia: operaciones", opsSecuencia)
    bullet("Aquí la diferencia no es de porcentajes: es de 300.000 frente a 51.")

    section("Caso 3: colección pequeña")

    val pocos = (1..10).toList()
    val pequenaLista = measureTime { repeat(10_000) { pocos.map { n -> n * 2 }.filter { n -> n > 5 } } }
    val pequenaSecuencia = measureTime {
        repeat(10_000) { pocos.asSequence().map { n -> n * 2 }.filter { n -> n > 5 }.toList() }
    }
    show("10 elementos, lista", pequenaLista)
    show("10 elementos, secuencia", pequenaSecuencia)
    bullet("Con pocos elementos la secuencia no compensa: hay que crear los objetos")
    bullet("intermedios de la propia maquinaria perezosa en cada vuelta.")
}

/**
 * Criba de primos con el constructor `sequence { }`.
 *
 * Es un buen ejemplo de por qué existe `sequence`: la lógica tiene estado (la lista
 * de primos encontrados) y no se sabe de antemano cuántos se van a pedir.
 */
private fun primos(): Sequence<Int> = sequence {
    val encontrados = mutableListOf<Int>()
    var candidato = 2
    while (true) {
        if (encontrados.none { candidato % it == 0 }) {
            encontrados.add(candidato)
            yield(candidato)          // entrega el primo y espera a que pidan el siguiente
        }
        candidato++
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `.take(10)` de `primos()` y comprueba (con Ctrl+C a mano) que no termina.
//  2. Escribe la secuencia de Fibonacci con `sequence { }` en lugar de generateSequence.
//  3. Sube `datos` a 3.000.000 y vuelve a mirar los tiempos del caso 1.
//  4. Añade un tercer `map` a los tres casos y observa cómo se separan más las curvas.
