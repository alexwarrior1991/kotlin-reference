package com.alejandro.infra

import java.io.ByteArrayOutputStream
import java.io.PrintStream

// =====================================================================================
//  Utilidad compartida por los tests de `infra`.
//
//  Las demos de este repositorio imprimen por consola: son su razón de ser. Para poder
//  testearlas hay que redirigir `System.out` a un buffer y, MUY IMPORTANTE, volver a
//  dejarlo como estaba pase lo que pase. Ése es exactamente el caso de uso de
//  `try`/`finally` (capítulo 19).
// =====================================================================================

/**
 * Ejecuta [bloque] capturando todo lo que imprima.
 *
 * @return el valor devuelto por el bloque y el texto que escribió, en ese orden.
 */
fun <T> capturandoLaSalida(bloque: () -> T): Pair<T, String> {
    val buffer = ByteArrayOutputStream()
    val original = System.out
    // `true` activa el autoflush; UTF-8 para que los acentos del repositorio lleguen enteros.
    System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
    return try {
        val resultado = bloque()
        resultado to buffer.toString(Charsets.UTF_8)
    } finally {
        // Sin este `finally`, un fallo dejaría la consola rota para el resto de la suite.
        System.setOut(original)
    }
}

/** Como [capturandoLaSalida], pero cuando el texto no interesa. */
fun silenciandoLaSalida(bloque: () -> Unit) {
    capturandoLaSalida(bloque)
}
