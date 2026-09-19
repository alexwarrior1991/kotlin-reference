package com.alejandro.c11sealed

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  11.2 · Modelar resultados y respuestas de API
//
//  QUÉ ES
//    El uso más frecuente de las sealed: un tipo que representa "salió bien con este
//    dato" o "falló por este motivo", con el motivo modelado como un tipo, no como un
//    texto suelto ni como una excepción.
//
//  POR QUÉ IMPORTA
//    Una excepción no aparece en la firma de la función: quien la llama no sabe que
//    puede fallar hasta que falla en producción. Un `Resultado<Usuario>` sí lo dice, y
//    el compilador obliga a tratar los dos casos.
//
//  ERRORES COMUNES
//    · Usar `String` como mensaje de error y perder la información estructurada.
//    · Modelar con sealed y luego hacer `!!` o `as Exito` para "atajar".
//    · Usar sealed para errores de programación (un bug) en vez de para errores
//      esperados (la red se cayó, el usuario no existe).
// =====================================================================================

/**
 * El patrón básico: Exito o Error.
 */
fun demoResultBasics() {
    section("Buscar un usuario que existe")

    when (val resultado = buscarUsuario(1)) {
        is Resultado.Exito -> show("encontrado", resultado.valor)
        is Resultado.Error -> show("error", resultado.mensaje)
    }

    section("Buscar uno que no existe")

    when (val resultado = buscarUsuario(999)) {
        is Resultado.Exito -> show("encontrado", resultado.valor)
        is Resultado.Error -> show("error", resultado.mensaje)
    }

    section("El compilador obliga a tratar los dos casos")

    bullet("No hay forma de leer `valor` sin haber comprobado antes que es un Exito.")
    bullet("Compara con devolver `Usuario?`: ahí un `!!` te salta la comprobación.")

    section("`out T` y `Nothing`: por qué Error no necesita parámetro de tipo")

    // `Resultado<out T>` es covariante y `Error : Resultado<Nothing>`. Como `Nothing`
    // es subtipo de TODO, un Error encaja en cualquier `Resultado<X>`. La varianza se
    // estudia en el capítulo 12; aquí basta con saber que es lo que hace que esto
    // funcione sin escribir `Resultado.Error<Usuario>`.
    val comoUsuario: Resultado<Usuario> = Resultado.Error("da igual el tipo")
    val comoTexto: Resultado<String> = Resultado.Error("el mismo objeto sirve para los dos")
    show("un Error vale como Resultado<Usuario>", comoUsuario)
    show("y como Resultado<String>", comoTexto)
}

/**
 * Errores estructurados: el motivo también es un tipo.
 */
fun demoStructuredErrors() {
    section("Un String no basta")

    bullet("`Error(\"no encontrado\")` no se puede tratar de forma distinta a")
    bullet("`Error(\"sin conexión\")` sin comparar cadenas, que es frágil.")

    section("Con una jerarquía de motivos")

    listOf("ana@ejemplo.com", "no-existe@ejemplo.com", "roto", "").forEach { entrada ->
        val resultado = registrar(entrada)
        show("registrar(\"$entrada\")", explicar(resultado))
    }

    section("Y cada motivo puede llevar sus propios datos")

    val resultado = registrar("roto")
    if (resultado is RespuestaApi.Fallo) {
        val motivo = resultado.motivo
        // Cada motivo tiene la información que le corresponde.
        when (motivo) {
            is MotivoDeFallo.Validacion -> show("campo problemático", motivo.campo)
            is MotivoDeFallo.NoEncontrado -> show("id buscado", motivo.id)
            is MotivoDeFallo.Red -> show("intentos hechos", motivo.intentos)
            MotivoDeFallo.SinPermiso -> show("sin permiso", "no hay datos extra")
        }
    }

    bullet("Esto es lo que un `String` de error no te puede dar.")
    bullet("Y el `when` sigue siendo exhaustivo en los dos niveles.")
}

/**
 * Encadenar operaciones sin escaleras de if.
 */
fun demoChaining() {
    section("El problema")

    bullet("Tres operaciones que pueden fallar, encadenadas.")
    bullet("Con `if` anidados salen tres niveles de sangría.")

    section("Con funciones de transformación")

    // Añadimos `map` y `flatMap` a nuestro Resultado: así se encadena en una línea.
    val bueno = buscarUsuario(1)
        .map { it.nombre }
        .map { it.uppercase() }
    show("buscar(1).map{nombre}.map{uppercase}", bueno)

    val malo = buscarUsuario(999)
        .map { it.nombre }
        .map { it.uppercase() }
    show("buscar(999).map{...}", malo)

    bullet("Si algo falla, el resto del encadenado NO se ejecuta: el error se propaga.")

    section("flatMap: cuando la transformación también puede fallar")

    show("buscar(1) y luego su empresa", buscarUsuario(1).flatMap { buscarEmpresa(it) })
    show("buscar(2) y luego su empresa", buscarUsuario(2).flatMap { buscarEmpresa(it) })

    section("Resolver al final")

    show("getOrElse con valor por defecto", buscarUsuario(999).getOrElse { Usuario(0, "invitado") })
    show("fold: un valor de cada rama", buscarUsuario(1).fold({ "OK: ${it.nombre}" }, { "KO: $it" }))
    show("fold sobre el error", buscarUsuario(999).fold({ "OK: ${it.nombre}" }, { "KO: $it" }))
}

/**
 * Comparación con las alternativas.
 */
fun demoVsAlternatives() {
    section("1. Excepciones")

    bullet("+ El camino feliz queda limpio.")
    bullet("- No aparecen en la firma: quien llama no sabe que puede fallar.")
    bullet("- En Kotlin NO hay checked exceptions: el compilador no te obliga a nada.")
    bullet("- Son caras: construir la traza de pila cuesta.")
    bullet("= Úsalas para lo excepcional de verdad (un bug, un estado imposible).")

    section("2. Devolver null")

    bullet("+ Muy corto, y el compilador obliga a comprobarlo.")
    bullet("- Sólo puede decir 'no hay valor', nunca POR QUÉ.")
    bullet("= Perfecto cuando sólo hay un motivo posible de fallo.")

    section("3. kotlin.Result<T>")

    // Kotlin trae su propio Result, que envuelve un Throwable.
    val conResultStdlib = runCatching { "42".toInt() }
    show("runCatching { \"42\".toInt() }", conResultStdlib.getOrNull())
    val falloStdlib = runCatching { "abc".toInt() }
    show("runCatching { \"abc\".toInt() }", falloStdlib.exceptionOrNull()?.let { it::class.simpleName })

    bullet("+ Ya está hecho, con map/fold/getOrElse incluidos.")
    bullet("- El error es siempre un Throwable: vuelves al problema de los motivos.")
    bullet("- No se puede usar como tipo de retorno público sin trucos (capítulo 19).")
    bullet("= Bien para envolver código que lanza; mal como modelo de dominio.")

    section("4. Sealed propia")

    bullet("+ Los motivos de fallo son tipos: exhaustivos y con sus propios datos.")
    bullet("+ Aparece en la firma; el compilador obliga a tratarlo.")
    bullet("- Hay que escribirla (aunque sean 10 líneas).")
    bullet("= La opción por defecto para el dominio de una aplicación.")
}

// -- El tipo Resultado -----------------------------------------------------------------

private data class Usuario(val id: Int, val nombre: String, val empresaId: Int? = null)
private data class Empresa(val nombre: String)

/**
 * `out T` hace que Resultado sea covariante, y eso permite que `Error` sea
 * `Resultado<Nothing>` y encaje en cualquier `Resultado<X>`. Capítulo 12.
 */
private sealed interface Resultado<out T> {
    data class Exito<out T>(val valor: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}

/** Transforma el valor si hay éxito; si no, deja pasar el error. */
private inline fun <T, R> Resultado<T>.map(transformar: (T) -> R): Resultado<R> = when (this) {
    is Resultado.Exito -> Resultado.Exito(transformar(valor))
    is Resultado.Error -> this
}

/** Como `map`, pero cuando la transformación también devuelve un Resultado. */
private inline fun <T, R> Resultado<T>.flatMap(transformar: (T) -> Resultado<R>): Resultado<R> =
    when (this) {
        is Resultado.Exito -> transformar(valor)
        is Resultado.Error -> this
    }

/** Valor por defecto cuando hay error. */
private inline fun <T> Resultado<T>.getOrElse(porDefecto: (String) -> T): T = when (this) {
    is Resultado.Exito -> valor
    is Resultado.Error -> porDefecto(mensaje)
}

/** Un valor de cada rama: la forma de "salir" del Resultado. */
private inline fun <T, R> Resultado<T>.fold(siExito: (T) -> R, siError: (String) -> R): R =
    when (this) {
        is Resultado.Exito -> siExito(valor)
        is Resultado.Error -> siError(mensaje)
    }

private val baseDeDatos = listOf(
    Usuario(1, "Ana", empresaId = 100),
    Usuario(2, "Luis", empresaId = null),
)

private fun buscarUsuario(id: Int): Resultado<Usuario> =
    baseDeDatos.find { it.id == id }
        ?.let { Resultado.Exito(it) }
        ?: Resultado.Error("no existe el usuario $id")

private fun buscarEmpresa(usuario: Usuario): Resultado<Empresa> =
    if (usuario.empresaId == null) Resultado.Error("${usuario.nombre} no tiene empresa")
    else Resultado.Exito(Empresa("Empresa ${usuario.empresaId}"))

// -- Errores estructurados --------------------------------------------------------------

private sealed interface MotivoDeFallo {
    data class Validacion(val campo: String, val detalle: String) : MotivoDeFallo
    data class NoEncontrado(val id: String) : MotivoDeFallo
    data class Red(val intentos: Int) : MotivoDeFallo
    data object SinPermiso : MotivoDeFallo
}

private sealed interface RespuestaApi {
    data class Exito(val usuario: Usuario) : RespuestaApi
    data class Fallo(val motivo: MotivoDeFallo) : RespuestaApi
}

private fun registrar(email: String): RespuestaApi = when {
    email.isBlank() -> RespuestaApi.Fallo(MotivoDeFallo.Validacion("email", "está vacío"))
    "@" !in email -> RespuestaApi.Fallo(MotivoDeFallo.Red(intentos = 3))
    email.startsWith("no-existe") -> RespuestaApi.Fallo(MotivoDeFallo.NoEncontrado(email))
    else -> RespuestaApi.Exito(Usuario(99, email.substringBefore('@')))
}

/** Un `when` anidado y exhaustivo en los dos niveles. */
private fun explicar(respuesta: RespuestaApi): String = when (respuesta) {
    is RespuestaApi.Exito -> "registrado como ${respuesta.usuario.nombre}"
    is RespuestaApi.Fallo -> when (val motivo = respuesta.motivo) {
        is MotivoDeFallo.Validacion -> "campo '${motivo.campo}' ${motivo.detalle}"
        is MotivoDeFallo.NoEncontrado -> "no se encontró '${motivo.id}'"
        is MotivoDeFallo.Red -> "fallo de red tras ${motivo.intentos} intentos"
        MotivoDeFallo.SinPermiso -> "no tienes permiso"
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `data object Caducado : MotivoDeFallo` y mira qué `when` dejan de compilar.
//  2. Escribe `onError { }` como función de extensión de Resultado.
//  3. Cambia `Resultado<out T>` por `Resultado<T>` y observa que Error deja de encajar.
//  4. Reescribe `buscarEmpresa` para que devuelva `Empresa?` y compara ambos diseños.
