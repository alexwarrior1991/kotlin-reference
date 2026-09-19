package com.alejandro.c27javainterop

import com.alejandro.infra.chapter

/**
 * # Capítulo 27 · Interoperabilidad con Java
 *
 * Los dos sentidos: usar Java desde Kotlin, y hacer que tu Kotlin sea cómodo de usar
 * desde Java.
 *
 * ## Este capítulo tiene Java de verdad
 * En `src/main/java/com/alejandro/c27javainterop/legacy/` hay cuatro clases Java que
 * se compilan junto con el Kotlin, sin ninguna configuración extra en Gradle:
 * - `UsuarioJava` — un POJO con getters/setters, con y sin anotaciones de nulabilidad
 * - `TextoUtilJava` — estáticos, constantes, varargs y un tipo "raw"
 * - `ProcesadorJava` — interfaces SAM y una excepción comprobada
 * - `ConsumidorDeKotlinJava` — **código Java que llama al Kotlin de este capítulo**
 *
 * Ese último fichero es la prueba: si quitas `@JvmName`, `@JvmOverloads`, `@JvmStatic`
 * o `@Throws` del lado Kotlin, **la build falla**. No es una promesa, es un test.
 *
 * ## Lo que hay que llevarse sí o sí
 * - Los `getX()/setX()` de Java se usan como **propiedades** desde Kotlin.
 * - Una interfaz Java de un solo método acepta una **lambda** directamente (SAM).
 *   En Kotlin hace falta declararla `fun interface`.
 * - Kotlin **no te obliga** a capturar las excepciones comprobadas de Java... pero
 *   siguen existiendo y propagándose.
 * - **Platform types (`String!`)**: es el único agujero de la seguridad frente a
 *   nulos. Protégete **declarando el tipo en la frontera**: `val x: String? = javaApi()`.
 *   Con `String` (no nulable), el NPE ocurre en la asignación, donde está la causa.
 * - Para que tu Kotlin se use bien desde Java: `@file:JvmName`, `@JvmOverloads`,
 *   `@JvmStatic`, `@JvmField`, `@Throws`.
 * - `internal` **no es privado para Java**: se ve, con el nombre alterado.
 *
 * ## Si tu proyecto es sólo Kotlin
 * Nada de esto hace falta. Son anotaciones para la frontera.
 *
 * Siguiente paso: capítulo 28, corrutinas.
 */
val chapter27 = chapter(
    number = 27,
    name = "Interoperabilidad Java",
    summary = "llamar a Java, platform types, SAM, y las anotaciones @Jvm* para el camino inverso",
) {
    demo("Getters y setters como propiedades", ::demoPropertyAccess)
    demo("Estáticos, constantes y varargs", ::demoStatics)
    demo("Conversión SAM: lambdas a interfaces Java", ::demoSamConversion)
    demo("Excepciones comprobadas, arrays y tipos raw", ::demoCheckedExceptionsAndArrays)
    demo("Platform types: el agujero", ::demoPlatformTypes)
    demo("Cómo protegerse de los platform types", ::demoProtecting)
    demo("Lo que Kotlin sí garantiza hacia Java", ::demoKotlinSideGuarantees)
    demo("Las anotaciones @Jvm*", ::demoJvmAnnotations)
    demo("Java real llamando a este Kotlin", ::demoJavaCallingKotlin)
    demo("Buenas prácticas en la frontera", ::demoBestPractices)
}

/** Ejecuta el capítulo 27 completo. */
fun main() = chapter27.runAll()
