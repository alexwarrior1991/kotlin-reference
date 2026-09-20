package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 4 · Sistema de usuarios                                       🟡 medio
//
//  Repasa: data class, copy, enum, sealed, colecciones, encapsulación, validación.
//  Capítulos: 07 (clases), 09 (data class), 10 (enum), 11 (sealed), 13 (colecciones).
// =====================================================================================

/** Ejercicio 4: alta, baja y consulta de usuarios. */
fun ejercicio04SistemaUsuarios() {
    enunciado(
        "Modela un registro de usuarios con reglas de verdad.",
        "",
        "1. `Usuario` con id, nombre, email, rol y si está activo.",
        "2. `Rol` como enum, con un permiso asociado (por ejemplo, si puede editar).",
        "3. Un repositorio en memoria con:",
        "   · `alta(nombre, email, rol)` que asigna el id automáticamente,",
        "   · `buscarPorId`, `buscarPorEmail` (sin distinguir mayúsculas),",
        "   · `listarPorRol`, `activos`, `desactivar`, `cambiarRol`.",
        "4. El alta debe fallar (con motivo, no con excepción) si el nombre está en",
        "   blanco, el email no es válido o ese email ya existe.",
        "5. El usuario debe ser INMUTABLE: modificar significa `copy`, no asignar.",
    )

    pistas(
        "Reutiliza `validarEmail` del ejercicio 2: los ejercicios se pueden apoyar",
        "   unos en otros, igual que los módulos de una aplicación real.",
        "Un `sealed interface ResultadoAlta` con Creado y Rechazado te evita tanto",
        "   devolver null (¿por qué falló?) como lanzar (no es excepcional).",
        "Guarda los usuarios en un `MutableMap<Int, Usuario>` privado y expón sólo",
        "   listas de sólo lectura: `.values.toList()` hace una copia (capítulo 13).",
        "Para buscar por email sin distinguir mayúsculas, normaliza AL GUARDAR, no",
        "   al buscar: si no, acabas con dos usuarios que son el mismo.",
        "`copy(activo = false)` crea uno nuevo; hay que volver a meterlo en el mapa.",
    )

    solucionEnMarcha()

    val repositorio = RepositorioDeUsuarios()

    section("Altas correctas")

    listOf(
        Triple("Ana García", "ana@ejemplo.com", Rol.ADMINISTRADOR),
        Triple("Luis Pérez", "luis@ejemplo.com", Rol.EDITOR),
        Triple("Marta Ruiz", "marta@otra.es", Rol.LECTOR),
        Triple("Pedro Sanz", "  PEDRO@Ejemplo.com ", Rol.LECTOR),
    ).forEach { (nombre, email, rol) ->
        show("alta($nombre)", describirAlta(repositorio.alta(nombre, email, rol)))
    }

    section("Altas rechazadas")

    listOf(
        Triple("", "vacio@ejemplo.com", Rol.LECTOR),
        Triple("Sin Email", "no-es-un-email", Rol.LECTOR),
        Triple("Repetida", "ana@ejemplo.com", Rol.LECTOR),
        Triple("Otra Vez", "ANA@EJEMPLO.COM", Rol.LECTOR),
    ).forEach { (nombre, email, rol) ->
        show("alta(\"$nombre\", \"$email\")", describirAlta(repositorio.alta(nombre, email, rol)))
    }

    section("Consultas")

    show("total", repositorio.todos().size)
    show("buscarPorId(1)", repositorio.buscarPorId(1)?.nombre)
    show("buscarPorId(99)", repositorio.buscarPorId(99))
    show("buscarPorEmail(\"ANA@ejemplo.com\")", repositorio.buscarPorEmail("ANA@ejemplo.com")?.nombre)
    show("listarPorRol(LECTOR)", repositorio.listarPorRol(Rol.LECTOR).map { it.nombre })
    show("quienes pueden editar", repositorio.todos().filter { it.rol.puedeEditar }.map { it.nombre })

    section("Modificar sin mutar")

    val antes = repositorio.buscarPorId(2)
    repositorio.cambiarRol(2, Rol.ADMINISTRADOR)
    val despues = repositorio.buscarPorId(2)

    show("antes", antes)
    show("después", despues)
    show("¿es el mismo objeto?", antes === despues)
    show("¿el de antes cambió?", antes?.rol)

    section("Bajas")

    show("desactivar(3)", repositorio.desactivar(3))
    show("desactivar(99)", repositorio.desactivar(99))
    show("activos", repositorio.activos().map { it.nombre })
    show("todos", repositorio.todos().size)

    section("Resumen por rol")

    repositorio.resumenPorRol().forEach { (rol, cuantos) -> show(rol.name, cuantos) }

    explicacion(
        "Tres decisiones que se repiten en cualquier repositorio real.",
        "",
        "INMUTABILIDAD. `Usuario` tiene sólo `val`. Para «cambiar» el rol se crea uno",
        "nuevo con `copy` y se sustituye en el mapa. Fíjate en la salida: el objeto",
        "que tenías en la mano NO cambió. Eso hace imposible el bug clásico de dos",
        "partes del código compartiendo un objeto mutable sin saberlo (capítulo 09).",
        "",
        "NORMALIZAR AL ESCRIBIR. El email se recorta y se pasa a minúsculas en el",
        "alta. Si sólo lo hicieras al buscar, podrías tener «Ana@x.com» y «ana@x.com»",
        "como dos usuarios distintos, y ningún `buscarPorEmail` lo arreglaría ya.",
        "",
        "ERRORES COMO DATOS. `alta` devuelve `ResultadoAlta`, no lanza. Un email",
        "repetido es un caso perfectamente esperable de un formulario, y quien llama",
        "necesita saber cuál de los motivos fue para enseñárselo al usuario.",
        "",
        "Y un detalle de encapsulación: `todos()` devuelve `.toList()`, una COPIA.",
        "Si devolviera la colección interna, cualquiera podría modificarla por detrás.",
    )

    varianteDificil(
        "1. Añade paginación: `listar(pagina, tamano)` con el total de páginas.",
        "2. Añade búsqueda por varios criterios a la vez (rol + activo + texto en",
        "   el nombre), con un objeto `Filtro` de campos nulables.",
        "3. Haz el repositorio INMUTABLE: que `alta` devuelva un repositorio nuevo",
        "   en vez de modificar el actual. Verás por qué casi nadie lo hace así.",
        "4. Añade un historial de cambios por usuario (quién, cuándo, qué campo).",
        "5. Hazlo seguro para varios hilos: `Mutex.withLock` o un `ConcurrentHashMap`,",
        "   y razona cuál es suficiente (capítulo 28).",
    )

    testEn("SistemaUsuariosTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/** El rol lleva consigo lo que permite hacer: así el permiso no se dispersa por el código. */
enum class Rol(val puedeEditar: Boolean, val puedeAdministrar: Boolean) {
    ADMINISTRADOR(puedeEditar = true, puedeAdministrar = true),
    EDITOR(puedeEditar = true, puedeAdministrar = false),
    LECTOR(puedeEditar = false, puedeAdministrar = false),
}

/** Inmutable: para «cambiar» algo se usa `copy`. */
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val activo: Boolean = true,
) {
    override fun toString(): String =
        "Usuario(#$id, $nombre, $email, $rol${if (activo) "" else ", INACTIVO"})"
}

sealed interface ResultadoAlta {
    data class Creado(val usuario: Usuario) : ResultadoAlta
    data class Rechazado(val motivos: List<String>) : ResultadoAlta
}

/**
 * Un repositorio en memoria.
 *
 * El mapa es privado y todo lo que sale es una copia de sólo lectura: nadie puede
 * modificar el estado interno sin pasar por los métodos.
 */
class RepositorioDeUsuarios {

    private val porId = linkedMapOf<Int, Usuario>()
    private var siguienteId = 1

    /**
     * Da de alta un usuario.
     *
     * Valida TODO antes de tocar nada: si algo falla, el repositorio queda igual que
     * estaba (nada de altas a medias).
     */
    fun alta(nombre: String, email: String, rol: Rol): ResultadoAlta {
        // Normalizar al escribir, nunca sólo al leer.
        val emailNormalizado = normalizarEmail(email)

        val motivos = buildList {
            if (nombre.isBlank()) add("el nombre no puede estar en blanco")
            when (val validacion = validarEmail(emailNormalizado)) {
                is ResultadoEmail.Valido -> Unit
                is ResultadoEmail.Invalido -> addAll(validacion.motivos)
            }
            if (porId.values.any { it.email == emailNormalizado }) {
                add("ya existe un usuario con el email $emailNormalizado")
            }
        }
        if (motivos.isNotEmpty()) return ResultadoAlta.Rechazado(motivos)

        val usuario = Usuario(siguienteId++, nombre.trim(), emailNormalizado, rol)
        porId[usuario.id] = usuario
        return ResultadoAlta.Creado(usuario)
    }

    fun buscarPorId(id: Int): Usuario? = porId[id]

    /** El email ya está normalizado en el mapa, así que basta normalizar la búsqueda. */
    fun buscarPorEmail(email: String): Usuario? {
        val buscado = normalizarEmail(email)
        return porId.values.firstOrNull { it.email == buscado }
    }

    /** Una COPIA de sólo lectura: el estado interno no se presta. */
    fun todos(): List<Usuario> = porId.values.toList()

    fun activos(): List<Usuario> = porId.values.filter { it.activo }

    fun listarPorRol(rol: Rol): List<Usuario> = porId.values.filter { it.rol == rol }

    /** @return `true` si existía y se ha desactivado. */
    fun desactivar(id: Int): Boolean {
        val usuario = porId[id] ?: return false
        porId[id] = usuario.copy(activo = false)   // objeto NUEVO, no mutación
        return true
    }

    fun cambiarRol(id: Int, nuevo: Rol): Boolean {
        val usuario = porId[id] ?: return false
        porId[id] = usuario.copy(rol = nuevo)
        return true
    }

    /** Cuántos usuarios hay de cada rol, incluidos los roles sin nadie. */
    fun resumenPorRol(): Map<Rol, Int> =
        Rol.entries.associateWith { rol -> porId.values.count { it.rol == rol } }
}

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun describirAlta(resultado: ResultadoAlta): String = when (resultado) {
    is ResultadoAlta.Creado -> "✓ ${resultado.usuario}"
    is ResultadoAlta.Rechazado -> "✗ " + resultado.motivos.joinToString("; ")
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `todos()` para que devuelva `porId.values` con tipo `Collection<Usuario>`
//     y comprueba que ahora es una VISTA viva del mapa: si das de alta a alguien
//     después, la colección que ya habías entregado cambia sola.
//  2. Cambia `val` por `var` en `Usuario` y observa cuántas garantías se pierden.
//  3. Añade `reactivar(id)` y un test que compruebe que el id no cambia.
