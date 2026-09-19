package com.alejandro

import com.alejandro.c01basics.chapter01
import com.alejandro.c02types.chapter02
import com.alejandro.c03operators.chapter03
import com.alejandro.c04controlflow.chapter04
import com.alejandro.infra.Launcher

// =====================================================================================
//  kotlin-reference · punto de entrada
//
//  Este fichero es el índice del repositorio: agrega los capítulos y delega en el
//  lanzador. Si vienes a aprender Kotlin, NO empieces aquí: empieza por
//  `c01basics/C01_01HelloWorld.kt` y ve subiendo de número.
//
//  Cómo ejecutar
//  -------------
//      ./gradlew run -q --console=plain                  menú interactivo
//      ./gradlew run -q --console=plain --args="list"    índice completo
//      ./gradlew run -q --console=plain --args="13"      el capítulo 13 entero
//      ./gradlew run -q --console=plain --args="13.4"    sólo la demo 13.4
//      ./gradlew run -q --console=plain --args="all"     absolutamente todo
//
//  Desde IntelliJ, pulsa ▶ en el `main()` de abajo, o en el de cualquier
//  `CNN_00Index.kt` para ejecutar ese capítulo suelto.
// =====================================================================================

/**
 * Los capítulos del recorrido, en orden pedagógico.
 *
 * Es una lista escrita a mano a propósito: el compilador la verifica, no hace falta
 * reflexión ni escanear el classpath, y añadir un capítulo es añadir una línea.
 */
val chapters = listOf(
    chapter01,
    chapter02,
    chapter03,
    chapter04,
)

fun main(args: Array<String>) {
    Launcher(chapters).start(args)
}
