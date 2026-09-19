package com.alejandro.infra

import com.alejandro.chapters
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

// =====================================================================================
//  El test de humo: ejecuta TODAS las demos del repositorio.
//
//  Es el test más valioso de la suite, y el más simple. Convierte cientos de ficheros
//  de ejemplos en código VERIFICADO: si una demo lanza una excepción, si un `require`
//  deja de cumplirse o si alguien rompe un ejemplo al editarlo, `./gradlew build` se
//  pone rojo con el identificador exacto de la demo culpable.
//
//  Detalle importante: llama a `demo.action()` y NO a `runDemo(demo)`. `runDemo`
//  captura las excepciones a propósito, para que una demo rota no corte el recorrido
//  del lanzador; aquí eso escondería justo lo que queremos detectar.
//
//  Va en UNA sola pasada porque ejecutar el repositorio entero cuesta unos segundos:
//  las dos comprobaciones (que no lanza y que imprime algo) se hacen a la vez.
// =====================================================================================

class SmokeTest {

    @Test
    fun `todas las demos se ejecutan sin lanzar y escriben algo`() {
        val fallos = mutableListOf<String>()
        val mudas = mutableListOf<String>()
        var ejecutadas = 0

        val tiempo = measureTime {
            for (capitulo in chapters) {
                for (demo in capitulo.demos) {
                    if (demo.skipInSmokeTest) continue
                    ejecutadas++

                    val (fallo, salida) = capturandoLaSalida {
                        try {
                            demo.action()
                            null
                        } catch (e: Throwable) {
                            e
                        }
                    }

                    if (fallo != null) fallos += "  ${demo.id} · ${demo.title} → $fallo"
                    // Una demo que no imprime nada no enseña nada: casi siempre
                    // significa que alguien dejó el cuerpo a medias.
                    if (salida.isBlank()) mudas += "  ${demo.id} · ${demo.title}"
                }
            }
        }

        println("Test de humo: $ejecutadas demos en ${chapters.size} capítulos, $tiempo")

        assertTrue(fallos.isEmpty(), "demos que lanzan:\n" + fallos.joinToString("\n"))
        assertTrue(mudas.isEmpty(), "demos que no imprimen nada:\n" + mudas.joinToString("\n"))
    }

    @Test
    fun `el recorrido completo termina en un tiempo razonable`() {
        // No es una prueba de rendimiento: es un guardián contra una demo que se
        // cuelgue esperando entrada, una red que no existe o un flujo infinito sin
        // acotar. Si alguna vez salta, mira la demo que hayas tocado.
        val tiempo = measureTime {
            silenciandoLaSalida {
                chapters.flatMap { it.demos }
                    .filterNot { it.skipInSmokeTest }
                    .forEach { runCatching { it.action() } }
            }
        }

        assertTrue(
            tiempo.inWholeSeconds < 120,
            "el repositorio entero tardó $tiempo; algo se está colgando",
        )
    }
}
