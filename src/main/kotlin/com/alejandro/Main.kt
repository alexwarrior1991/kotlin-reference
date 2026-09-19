package com.alejandro

import com.alejandro.c01basics.chapter01
import com.alejandro.c02types.chapter02
import com.alejandro.c03operators.chapter03
import com.alejandro.c04controlflow.chapter04
import com.alejandro.c05functions.chapter05
import com.alejandro.c06nullsafety.chapter06
import com.alejandro.c07classes.chapter07
import com.alejandro.c08inheritance.chapter08
import com.alejandro.c09dataclasses.chapter09
import com.alejandro.c10enums.chapter10
import com.alejandro.c11sealed.chapter11
import com.alejandro.c12generics.chapter12
import com.alejandro.c13collections.chapter13
import com.alejandro.c14sequences.chapter14
import com.alejandro.c15lambdas.chapter15
import com.alejandro.c16scopefunctions.chapter16
import com.alejandro.c17extensions.chapter17
import com.alejandro.c18delegation.chapter18
import com.alejandro.c19exceptions.chapter19
import com.alejandro.c20typechecks.chapter20
import com.alejandro.c21operatoroverloading.chapter21
import com.alejandro.c22destructuring.chapter22
import com.alejandro.c23annotations.chapter23
import com.alejandro.c24reflection.chapter24
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
    chapter05,
    chapter06,
    chapter07,
    chapter08,
    chapter09,
    chapter10,
    chapter11,
    chapter12,
    chapter13,
    chapter14,
    chapter15,
    chapter16,
    chapter17,
    chapter18,
    chapter19,
    chapter20,
    chapter21,
    chapter22,
    chapter23,
    chapter24,
)

fun main(args: Array<String>) {
    Launcher(chapters).start(args)
}
