package com.alejandro.c31exercises

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Ejercicio 4 · comprueba la solución propuesta del sistema de usuarios. */
class SistemaUsuariosTest {

    private lateinit var repositorio: RepositorioDeUsuarios

    @BeforeTest
    fun preparar() {
        repositorio = RepositorioDeUsuarios()
    }

    private fun darDeAlta(nombre: String, email: String, rol: Rol = Rol.LECTOR): Usuario {
        val resultado = repositorio.alta(nombre, email, rol)
        assertIs<ResultadoAlta.Creado>(resultado, "el alta de «$nombre» debería funcionar")
        return resultado.usuario
    }

    private fun motivosDelRechazo(nombre: String, email: String): List<String> {
        val resultado = repositorio.alta(nombre, email, Rol.LECTOR)
        assertIs<ResultadoAlta.Rechazado>(resultado, "el alta de «$nombre» debería fallar")
        return resultado.motivos
    }

    // -- Alta ---------------------------------------------------------------------------

    @Test
    fun `el alta asigna identificadores correlativos desde uno`() {
        assertEquals(1, darDeAlta("Ana", "ana@ejemplo.com").id)
        assertEquals(2, darDeAlta("Luis", "luis@ejemplo.com").id)
        assertEquals(3, darDeAlta("Marta", "marta@ejemplo.com").id)
    }

    @Test
    fun `el alta normaliza el email y recorta el nombre`() {
        val usuario = darDeAlta("  Pedro Sanz  ", "  PEDRO@Ejemplo.COM  ")

        assertEquals("pedro@ejemplo.com", usuario.email)
        assertEquals("Pedro Sanz", usuario.nombre)
    }

    @Test
    fun `el alta deja al usuario activo`() {
        assertTrue(darDeAlta("Ana", "ana@ejemplo.com").activo)
    }

    @Test
    fun `rechaza un nombre en blanco`() {
        assertContentEquals(
            listOf("el nombre no puede estar en blanco"),
            motivosDelRechazo("   ", "valido@ejemplo.com"),
        )
    }

    @Test
    fun `rechaza un email invalido con el motivo del validador`() {
        val motivos = motivosDelRechazo("Sin Email", "no-es-un-email")

        assertTrue(motivos.any { "arroba" in it }, "esperaba el motivo del email: $motivos")
    }

    @Test
    fun `rechaza un email repetido`() {
        darDeAlta("Ana", "ana@ejemplo.com")

        val motivos = motivosDelRechazo("Otra Ana", "ana@ejemplo.com")

        assertTrue(motivos.any { "ya existe" in it }, "motivos: $motivos")
    }

    @Test
    fun `el email repetido se detecta aunque cambien las mayusculas`() {
        darDeAlta("Ana", "ana@ejemplo.com")

        val motivos = motivosDelRechazo("Otra Ana", "  ANA@EJEMPLO.COM ")

        assertTrue(motivos.any { "ya existe" in it }, "motivos: $motivos")
    }

    @Test
    fun `un alta rechazada no gasta identificador ni deja rastro`() {
        darDeAlta("Ana", "ana@ejemplo.com")
        motivosDelRechazo("", "otro@ejemplo.com")

        assertEquals(2, darDeAlta("Luis", "luis@ejemplo.com").id)
        assertEquals(2, repositorio.todos().size)
    }

    @Test
    fun `acumula varios motivos de rechazo a la vez`() {
        val motivos = motivosDelRechazo("", "no-es-un-email")

        assertTrue(motivos.size >= 2, "esperaba al menos dos motivos, fueron: $motivos")
    }

    // -- Consultas -----------------------------------------------------------------------

    @Test
    fun `buscarPorId devuelve null si no existe`() {
        assertNull(repositorio.buscarPorId(99))
    }

    @Test
    fun `buscarPorEmail no distingue mayusculas ni espacios`() {
        darDeAlta("Ana", "ana@ejemplo.com")

        assertEquals("Ana", repositorio.buscarPorEmail("  ANA@Ejemplo.com ")?.nombre)
    }

    @Test
    fun `listarPorRol filtra por rol`() {
        darDeAlta("Ana", "ana@ejemplo.com", Rol.ADMINISTRADOR)
        darDeAlta("Luis", "luis@ejemplo.com", Rol.LECTOR)
        darDeAlta("Marta", "marta@ejemplo.com", Rol.LECTOR)

        assertEquals(
            listOf("Luis", "Marta"),
            repositorio.listarPorRol(Rol.LECTOR).map { it.nombre },
        )
    }

    @Test
    fun `todos devuelve una copia, no la coleccion interna`() {
        darDeAlta("Ana", "ana@ejemplo.com")
        val copia = repositorio.todos()

        darDeAlta("Luis", "luis@ejemplo.com")

        assertEquals(1, copia.size, "la lista entregada no debería crecer sola")
        assertEquals(2, repositorio.todos().size)
    }

    @Test
    fun `resumenPorRol incluye los roles sin nadie`() {
        darDeAlta("Ana", "ana@ejemplo.com", Rol.ADMINISTRADOR)

        val resumen = repositorio.resumenPorRol()

        assertEquals(Rol.entries.size, resumen.size)
        assertEquals(1, resumen[Rol.ADMINISTRADOR])
        assertEquals(0, resumen[Rol.EDITOR])
    }

    // -- Modificaciones ------------------------------------------------------------------

    @Test
    fun `cambiarRol no muta el objeto que ya tenias`() {
        val antes = darDeAlta("Luis", "luis@ejemplo.com", Rol.EDITOR)

        repositorio.cambiarRol(antes.id, Rol.ADMINISTRADOR)

        assertEquals(Rol.EDITOR, antes.rol, "el objeto original NO debe cambiar")
        assertEquals(Rol.ADMINISTRADOR, repositorio.buscarPorId(antes.id)?.rol)
    }

    @Test
    fun `desactivar saca al usuario de activos pero no de todos`() {
        val ana = darDeAlta("Ana", "ana@ejemplo.com")
        darDeAlta("Luis", "luis@ejemplo.com")

        assertTrue(repositorio.desactivar(ana.id))

        assertEquals(listOf("Luis"), repositorio.activos().map { it.nombre })
        assertEquals(2, repositorio.todos().size)
    }

    @Test
    fun `desactivar o cambiar el rol de alguien que no existe devuelve false`() {
        assertFalse(repositorio.desactivar(99))
        assertFalse(repositorio.cambiarRol(99, Rol.LECTOR))
    }

    // -- El enum lleva sus permisos -------------------------------------------------------

    @Test
    fun `los permisos viven en el rol`() {
        assertTrue(Rol.ADMINISTRADOR.puedeEditar)
        assertTrue(Rol.ADMINISTRADOR.puedeAdministrar)
        assertTrue(Rol.EDITOR.puedeEditar)
        assertFalse(Rol.EDITOR.puedeAdministrar)
        assertFalse(Rol.LECTOR.puedeEditar)
    }
}
