package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section

// =====================================================================================
//  El formato común de los once ejercicios.
//
//  Todos siguen exactamente la misma estructura, para que se puedan leer en cualquier
//  orden sin perderse:
//
//      ENUNCIADO  → qué hay que construir y con qué reglas
//      PISTAS     → por dónde empezar, sin destripar la solución
//      SOLUCIÓN   → el código propuesto, ejecutándose de verdad
//      EXPLICACIÓN→ por qué está hecho así y qué decisiones hay detrás
//      VARIANTE   → cómo seguir cuando el ejercicio ya te sale
//
//  Consejo de uso: lee el ENUNCIADO, tapa el resto y escribe tu versión en un
//  scratch file del IDE. Sólo después compara. Leer una solución no enseña casi nada;
//  escribir la tuya y compararla, muchísimo.
// =====================================================================================

fun enunciado(vararg lineas: String) {
    section("ENUNCIADO")
    lineas.forEach { bullet(it) }
}

/**
 * Imprime las pistas numeradas.
 *
 * Una línea que empieza por espacio se considera continuación de la pista anterior
 * y se imprime tal cual, sin número, para que quede alineada debajo.
 */
fun pistas(vararg lineas: String) {
    section("PISTAS")
    var numero = 0
    lineas.forEach { linea ->
        when {
            linea.isBlank() -> bullet("")
            linea.startsWith(" ") -> bullet(linea)
            else -> {
                numero++
                bullet("$numero. $linea")
            }
        }
    }
}

/** Cabecera de la parte que sí se ejecuta. */
fun solucionEnMarcha(titulo: String = "LA SOLUCIÓN PROPUESTA, FUNCIONANDO") {
    section(titulo)
}

fun explicacion(vararg lineas: String) {
    section("EXPLICACIÓN")
    lineas.forEach { bullet(it) }
}

fun varianteDificil(vararg lineas: String) {
    section("VARIANTE MÁS DIFÍCIL")
    lineas.forEach { bullet(it) }
}

/** Dónde está el test que comprueba la solución propuesta. */
fun testEn(fichero: String) {
    section("COMPROBADO POR")
    bullet("src/test/kotlin/com/alejandro/c31exercises/$fichero")
    bullet("Ejecuta: ./gradlew test --tests '*${fichero.removeSuffix(".kt")}'")
}
