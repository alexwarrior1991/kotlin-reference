package com.alejandro.c25stdlib

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  25.1 · require, check, error, assert y TODO
//
//  QUÉ ES
//    Las funciones de la biblioteca estándar para declarar condiciones que deben
//    cumplirse. Cada una lanza una excepción distinta, y esa diferencia es lo que
//    comunica de quién es la culpa.
//
//  POR QUÉ IMPORTA
//    Elegir bien entre `require` y `check` documenta el contrato: `require` dice
//    "me has llamado mal" y `check` dice "estoy en un estado imposible". Quien lea
//    la traza sabrá dónde mirar.
//
//  ERRORES COMUNES
//    · Usar `assert` creyendo que comprueba algo: sin `-ea` no hace nada.
//    · Usar `require` para validar entrada de USUARIO (eso no es un bug: modélalo).
//    · Dejar un `TODO()` en producción.
// =====================================================================================

/**
 * `require`: los argumentos.
 */
fun demoRequire() {
    section("Comprueba los ARGUMENTOS")

    show("raizDe(16.0)", raizDe(16.0))

    val negativo = try {
        raizDe(-4.0).toString()
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException: ${e.message}"
    }
    show("raizDe(-4.0)", negativo)

    bullet("`require(condicion) { mensaje }` lanza IllegalArgumentException.")
    bullet("Significa: «quien me ha llamado se ha equivocado».")

    section("requireNotNull devuelve el valor no nulo")

    show("requireNotNull(\"x\")", requireNotNull("x") { "no debería ser null" })

    val conNulo = try {
        val nulo: String? = null
        requireNotNull(nulo) { "el nombre es obligatorio" }
    } catch (e: IllegalArgumentException) {
        "lanzó: ${e.message}"
    }
    show("requireNotNull(null) { ... }", conNulo)

    bullet("Es la alternativa a `!!` con un mensaje que se puede leer (capítulo 6.8).")

    section("El mensaje sólo se construye si falla")

    // La lambda es `inline`, así que la interpolación NO se evalúa en el camino
    // feliz. Por eso puedes poner todo el detalle que quieras.
    var vecesEvaluado = 0
    fun mensajeCaro(): String {
        vecesEvaluado++
        return "mensaje caro de construir"
    }

    require(true) { mensajeCaro() }
    show("tras un require que pasa", vecesEvaluado)

    runCatching { require(false) { mensajeCaro() } }
    show("tras un require que falla", vecesEvaluado)

    bullet("Cero coste cuando la condición se cumple. Aprovéchalo: mensajes detallados.")

    section("Varios require seguidos: valida todo de golpe")

    show("crearUsuario válido", crearUsuario("ana@ejemplo.com", 34))
    show("crearUsuario sin @", crearUsuarioSeguro("ana", 34))
    show("crearUsuario con edad rara", crearUsuarioSeguro("ana@ejemplo.com", 200))
}

/**
 * `check` y `error`: el estado.
 */
fun demoCheck() {
    section("Comprueba el ESTADO del objeto")

    val conexion = Conexion()

    val sinAbrir = try {
        conexion.enviar("hola")
    } catch (e: IllegalStateException) {
        "lanzó IllegalStateException: ${e.message}"
    }
    show("enviar() sin abrir", sinAbrir)

    conexion.abrir()
    show("tras abrir(), enviar()", conexion.enviar("hola"))

    conexion.cerrar()
    val trasCerrar = try {
        conexion.enviar("adiós")
    } catch (e: IllegalStateException) {
        "lanzó: ${e.message}"
    }
    show("enviar() tras cerrar", trasCerrar)

    bullet("`check(condicion) { mensaje }` lanza IllegalStateException.")
    bullet("Significa: «este objeto no está en condiciones de hacer eso».")

    section("La diferencia con require, en una frase")

    bullet("require → la culpa es de QUIEN LLAMA (argumento inválido)")
    bullet("check   → la culpa es del ESTADO (orden de llamadas incorrecto)")
    bullet("Quien lee la traza sabe al instante dónde buscar el error.")

    section("error(): lanzar sin condición")

    // `error(mensaje)` es `throw IllegalStateException(mensaje)`, y devuelve Nothing,
    // así que encaja donde se espera un valor.
    show("estadoDe(\"activo\")", estadoDe("activo"))
    val desconocido = try {
        estadoDe("marciano")
    } catch (e: IllegalStateException) {
        "lanzó: ${e.message}"
    }
    show("estadoDe(\"marciano\")", desconocido)

    bullet("Muy útil en la rama `else` de un `when` que nunca debería alcanzarse.")

    section("checkNotNull")

    show("checkNotNull con valor", checkNotNull("x") { "..." })
    bullet("Igual que requireNotNull, pero lanzando IllegalStateException.")
}

/**
 * `assert`: la que casi nunca hace nada.
 */
fun demoAssert() {
    section("La trampa")

    bullet("`assert(condicion)` NO hace nada salvo que la JVM arranque con -ea.")
    bullet("Sin esa opción, la condición ni siquiera se evalúa.")
    bullet("Es la misma semántica que el `assert` de Java.")

    section("En este proyecto sí está activado")

    // build.gradle.kts añade "-ea" a applicationDefaultJvmArgs, justo para que
    // esta demo se pueda ver funcionando.
    val conAssert = try {
        val valor = -1
        assert(valor >= 0) { "el valor debería ser positivo, pero es $valor" }
        "el assert pasó (o está desactivado)"
    } catch (e: AssertionError) {
        "lanzó AssertionError: ${e.message}"
    }
    show("assert(valor >= 0)", conAssert)

    bullet("Si ves 'lanzó AssertionError', las aserciones están activadas.")
    bullet("Si ves 'el assert pasó', es que alguien quitó el -ea del build.")

    section("Por qué casi nadie la usa")

    bullet("Una comprobación que puede estar desactivada es una comprobación")
    bullet("en la que no puedes confiar. Si la condición importa, usa `check`.")
    bullet("Y si no importa, quítala.")

    section("Su único nicho")

    bullet("Comprobaciones MUY caras que sólo quieres en desarrollo:")
    bullet("verificar que una lista de un millón de elementos sigue ordenada,")
    bullet("o que un invariante complejo se mantiene tras cada operación.")
    bullet("Para todo lo demás: require, check, o un test.")
}

/**
 * `TODO()`: el hueco que compila.
 */
fun demoTodo() {
    section("Devuelve Nothing, así que encaja en cualquier sitio")

    bullet("fun calcular(): Int = TODO()           ← compila")
    bullet("fun nombre(): String = TODO(\"pendiente\") ← también")
    bullet("Funciona porque `Nothing` es subtipo de todos los tipos (capítulo 5.4).")

    section("Para qué sirve")

    bullet("Escribir la ESTRUCTURA completa de un programa antes que el detalle:")
    bullet("todas las firmas compilan, y vas rellenando una a una.")
    bullet("Es mucho mejor que devolver 0 o null como relleno: eso compila y")
    bullet("se te olvida; un TODO() revienta en cuanto alguien lo ejecuta.")

    section("Funcionando")

    val pendiente = try {
        funcionSinImplementar()
    } catch (e: NotImplementedError) {
        "lanzó NotImplementedError: ${e.message}"
    }
    show("funcionSinImplementar()", pendiente)

    bullet("Lanza NotImplementedError, que hereda de Error (no de Exception).")
    bullet("IntelliJ además los lista en la ventana TODO, así que no se pierden.")

    section("El aviso")

    bullet("Un TODO() en producción es una bomba de relojería.")
    bullet("Pon una regla de análisis estático que falle la build si queda alguno.")
}

// -- Lo que usan las demos ---------------------------------------------------------------------

private fun raizDe(valor: Double): Double {
    require(valor >= 0) { "no se puede calcular la raíz de un número negativo: $valor" }
    return Math.sqrt(valor)
}

private fun crearUsuario(email: String, edad: Int): String {
    require("@" in email) { "'$email' no parece un email" }
    require(edad in 0..130) { "edad fuera de rango: $edad" }
    return "usuario $email ($edad años)"
}

/** Envuelve `crearUsuario` para que las demos no propaguen excepciones. */
private fun crearUsuarioSeguro(email: String, edad: Int): String =
    runCatching { crearUsuario(email, edad) }
        .getOrElse { "rechazado: ${it.message}" }

private class Conexion {
    private var abierta = false
    private var cerrada = false

    fun abrir() {
        check(!cerrada) { "no se puede reabrir una conexión cerrada" }
        abierta = true
    }

    fun enviar(mensaje: String): String {
        check(abierta) { "la conexión no está abierta" }
        check(!cerrada) { "la conexión ya está cerrada" }
        return "enviado: '$mensaje'"
    }

    fun cerrar() {
        abierta = false
        cerrada = true
    }
}

private fun estadoDe(nombre: String): Int = when (nombre) {
    "activo" -> 1
    "inactivo" -> 0
    else -> error("estado desconocido: '$nombre'")
}

private fun funcionSinImplementar(): String = TODO("esto se implementará en el sprint que viene")

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el "-ea" de applicationDefaultJvmArgs y vuelve a ejecutar la demo 25.3.
//  2. Cambia un `require` por un `check` en `crearUsuario` y razona qué comunica cada uno.
//  3. Escribe una función con `TODO()` y llámala: verás el NotImplementedError.
//  4. Añade a Conexion un `check` que impida enviar más de 3 mensajes.
