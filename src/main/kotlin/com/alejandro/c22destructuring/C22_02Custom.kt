package com.alejandro.c22destructuring

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  22.2 · componentN propio y las limitaciones
//
//  QUÉ ES
//    Escribir `component1()`, `component2()`... a mano, o añadirlos desde fuera con
//    extensiones, para que un tipo que no es `data class` también se pueda
//    desestructurar.
//
//  POR QUÉ IMPORTA
//    Permite desestructurar tipos de librerías ajenas, y da control sobre QUÉ
//    componentes se exponen y en qué orden. Y entender la limitación posicional evita
//    el bug más silencioso de toda la característica.
//
//  ERRORES COMUNES
//    · Reordenar las propiedades de una data class sin revisar quién la desestructura.
//    · Escribir `componentN` sin `operator` (no funciona y el error despista).
//    · Exponer diez componentes: a partir del tercero ya nadie recuerda el orden.
// =====================================================================================

/**
 * Escribir componentN a mano.
 */
fun demoManualComponents() {
    section("En una clase normal")

    val coordenada = Coordenada(40.4168, -3.7038)
    val (latitud, longitud) = coordenada
    show("val (lat, lon) = Coordenada(...)", "lat=$latitud lon=$longitud")

    bullet("`operator fun component1()` y `component2()` escritos a mano.")
    bullet("Es literalmente lo que genera una `data class`.")

    section("Puedes exponer lo que quieras, no sólo propiedades")

    // Los componentes pueden ser valores CALCULADOS, no campos almacenados.
    val duracion = Duracion(totalSegundos = 3_725)
    val (horas, minutos, segundos) = duracion
    show("val (h, m, s) = Duracion(3725)", "$horas h $minutos min $segundos s")
    show("y el campo real sigue siendo", duracion.totalSegundos)

    bullet("Una data class expone sus propiedades; aquí exponemos una DESCOMPOSICIÓN.")
    bullet("Es el caso en que escribirlos a mano aporta algo de verdad.")

    section("Y puedes elegir el orden y cuántos")

    val usuarioCompleto = UsuarioConMuchosCampos(
        id = 7,
        nombre = "Ana",
        email = "ana@ejemplo.com",
        telefono = "600000000",
        direccion = "Calle Mayor 1",
    )

    // Aunque la clase tiene cinco campos, sólo exponemos los dos más usados.
    val (id, nombre) = usuarioCompleto
    show("val (id, nombre) = usuario", "$id / $nombre")

    bullet("Exponer sólo dos componentes en una clase de cinco campos es una")
    bullet("decisión de diseño legítima: evita desestructuraciones ilegibles.")
}

/**
 * Añadir componentN desde fuera.
 */
fun demoExtensionComponents() {
    section("Sobre un tipo que no controlas")

    // No podemos tocar la clase, pero sí añadirle desestructuración.
    val rango = 10..20
    val (inicio, fin) = rango
    show("val (inicio, fin) = 10..20", "$inicio..$fin")

    section("Sobre un tipo de la biblioteca estándar")

    val entrada = mapOf("clave" to 1).entries.first()
    val (clave, valor) = entrada
    show("val (k, v) = Map.Entry", "$clave=$valor")
    bullet("Éste ya lo trae Kotlin: `Map.Entry.component1/2` son extensiones suyas.")

    section("Sobre una lista, para coger los primeros")

    // Kotlin ya trae component1..component5 para List.
    val coordenadas = listOf(1, 2, 3, 4, 5)
    val (x, y, z) = coordenadas
    show("val (x, y, z) = listOf(1,2,3,4,5)", "$x, $y, $z")

    bullet("La stdlib define component1() hasta component5() para List.")
    bullet("Con menos elementos de los que pides, lanza IndexOutOfBoundsException:")

    val fallo = try {
        val (a, b, c) = listOf(1, 2)
        "$a$b$c"
    } catch (e: IndexOutOfBoundsException) {
        "lanzó IndexOutOfBoundsException"
    }
    show("val (a, b, c) = listOf(1, 2)", fallo)

    bullet("Por eso desestructurar listas es cómodo pero frágil: el compilador no")
    bullet("puede saber cuántos elementos habrá en ejecución.")
}

/**
 * La limitación posicional: el bug silencioso.
 */
fun demoPositionalTrap() {
    section("El problema")

    // Dos String seguidos. Si alguien reordena las propiedades de la clase,
    // esta desestructuración sigue compilando... y asigna al revés.
    val contacto = Contacto(nombre = "Ana", email = "ana@ejemplo.com")
    val (nombre, email) = contacto

    show("nombre", nombre)
    show("email", email)

    bullet("Ahora imagina que alguien cambia el orden en la clase:")
    bullet("  data class Contacto(val email: String, val nombre: String)")
    bullet("Esta línea SIGUE COMPILANDO, pero `nombre` contendrá el email.")

    section("Por qué el compilador no ayuda")

    bullet("La desestructuración es por POSICIÓN, no por nombre. El compilador")
    bullet("sólo comprueba los TIPOS, y aquí los dos son String.")
    bullet("Con tipos distintos sí avisaría, pero es casualidad, no protección.")

    section("Cómo protegerse")

    bullet("1. Desestructura sólo cuando el orden sea EVIDENTE (x/y, clave/valor,")
    bullet("   mín/máx). Ahí nadie va a reordenar nada.")
    bullet("2. Con más de dos o tres componentes, usa las propiedades por nombre:")

    show("por nombre (a prueba de reordenaciones)", "${contacto.nombre} / ${contacto.email}")

    bullet("3. Usa `value class` (capítulo 9.11) para que los tipos sean distintos:")
    bullet("   `Contacto(val nombre: Nombre, val email: Email)` ya no se puede cruzar.")

    section("El aviso que sí existe")

    bullet("IntelliJ avisa con 'Variable name matches the name of a different")
    bullet("component' si desestructuras usando nombres que existen en la clase")
    bullet("pero en otra posición. Hazle caso: es exactamente este bug.")
}

/**
 * Lo que NO se puede hacer.
 */
fun demoLimitations() {
    section("1. No hay anidamiento")

    bullet("`val ((a, b), c) = ...` no compila. Hay que hacerlo en dos pasos.")

    val anidado = Pair(Pair(1, 2), 3)
    val (interior, tercero) = anidado
    val (primero, segundo) = interior
    show("en dos pasos", "$primero, $segundo, $tercero")

    section("2. No se puede desestructurar por nombre")

    bullet("`val (email, nombre) = contacto` NO busca por nombre: coge las")
    bullet("posiciones 1 y 2, se llamen como se llamen tus variables.")
    bullet("Kotlin no tiene la desestructuración por nombre de JavaScript.")

    section("3. Máximo el número de componentN que existan")

    bullet("Una data class de 3 propiedades genera component1..component3.")
    bullet("Pedir un cuarto es un error de compilación (eso sí lo detecta).")

    section("4. Sólo el constructor primario")

    val conCuerpo = ConPropiedadEnCuerpo("visible")
    val (soloLaDelConstructor) = conCuerpo
    show("val (x) = ConPropiedadEnCuerpo(...)", soloLaDelConstructor)
    show("la del cuerpo no es componente", conCuerpo.calculada)

    bullet("Es la misma regla que para equals/hashCode/toString (capítulo 9.2).")

    section("5. No funciona en parámetros de función")

    bullet("`fun f((a, b): Pair<Int, Int>)` no existe. Sólo en `val`/`var`, bucles")
    bullet("y parámetros de LAMBDA.")

    section("Cuándo usar desestructuración, en resumen")

    bullet("SÍ: `for ((k, v) in mapa)` · `val (min, max) = ...` · lambdas sobre pares")
    bullet("SÍ: cuando el orden es parte del concepto (coordenadas, fracciones)")
    bullet("NO: objetos de dominio con muchos campos del mismo tipo")
    bullet("NO: sólo por ahorrarte escribir `.propiedad`")
}

// -- Los tipos que usan las demos ------------------------------------------------------------

/** componentN escritos a mano en una clase que no es `data`. */
private class Coordenada(val latitud: Double, val longitud: Double) {
    operator fun component1(): Double = latitud
    operator fun component2(): Double = longitud
}

/** Los componentes son valores CALCULADOS, no campos. */
private class Duracion(val totalSegundos: Int) {
    operator fun component1(): Int = totalSegundos / 3600
    operator fun component2(): Int = (totalSegundos % 3600) / 60
    operator fun component3(): Int = totalSegundos % 60
}

/** Cinco campos, pero sólo dos componentes: una decisión deliberada. */
private class UsuarioConMuchosCampos(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
) {
    operator fun component1(): Int = id
    operator fun component2(): String = nombre
}

/** Desestructuración añadida a un tipo ajeno mediante extensiones. */
private operator fun IntRange.component1(): Int = first
private operator fun IntRange.component2(): Int = last

private data class Contacto(val nombre: String, val email: String)

private data class ConPropiedadEnCuerpo(val delConstructor: String) {
    /** No entra en componentN, igual que no entra en equals. */
    val calculada: String get() = delConstructor.uppercase()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia el orden de `nombre` y `email` en Contacto y comprueba que la demo
//     22.7 sigue compilando pero imprime los valores cruzados.
//  2. Añade `component4()` a UsuarioConMuchosCampos y desestructura cuatro valores.
//  3. Intenta `val ((a, b), c) = Pair(Pair(1,2), 3)` y lee el error.
//  4. Escribe `operator fun LocalDate.component1()` para desestructurar una fecha.
