package com.alejandro.c30testing

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// =====================================================================================
//  Tests de las sesiones que caducan.
//
//  Éste es EL ejemplo de por qué se inyecta el reloj: probar una caducidad de 30
//  minutos sin tocar el tiempo obligaría a esperar media hora. Con un reloj falso,
//  el test tarda microsegundos y además es determinista.
//
//  `@BeforeTest` se ejecuta antes de CADA test, y JUnit crea una instancia nueva de
//  la clase cada vez, así que `reloj` y `gestor` nunca se comparten entre tests.
// =====================================================================================

class GestorDeSesionesTest {

    private lateinit var reloj: RelojManipulable
    private lateinit var gestor: GestorDeSesiones

    @BeforeTest
    fun preparar() {
        reloj = RelojManipulable(inicioMs = 1_000_000)
        gestor = GestorDeSesiones(reloj, duracionMs = 30 * 60 * 1000L)
    }

    @Test
    fun `una sesion recien abierta esta activa`() {
        val token = gestor.abrir("ana")

        assertTrue(gestor.estaActiva(token))
    }

    @Test
    fun `la sesion sigue activa justo antes de caducar`() {
        val token = gestor.abrir("ana")

        reloj.avanzarMinutos(29)

        assertTrue(gestor.estaActiva(token), "a los 29 minutos todavía debería valer")
    }

    @Test
    fun `la sesion caduca al cumplirse su duracion`() {
        val token = gestor.abrir("ana")

        reloj.avanzarMinutos(30)

        assertFalse(gestor.estaActiva(token), "a los 30 minutos exactos ya ha caducado")
    }

    @Test
    fun `una sesion cerrada deja de estar activa`() {
        val token = gestor.abrir("ana")

        gestor.cerrar(token)

        assertFalse(gestor.estaActiva(token))
    }

    @Test
    fun `un token inventado nunca esta activo`() {
        assertFalse(gestor.estaActiva("no-existe"))
    }

    @Test
    fun `cerrar un token inexistente no falla`() {
        // Un `remove` de algo que no está es un no-op. Conviene dejarlo por escrito:
        // es una decisión de diseño, no una casualidad.
        gestor.cerrar("no-existe")

        assertEquals(0, gestor.sesionesAbiertas())
    }

    @Test
    fun `cada usuario tiene su propia sesion`() {
        val deAna = gestor.abrir("ana")
        reloj.avanzarMinutos(20)
        val deBerta = gestor.abrir("berta")

        reloj.avanzarMinutos(15)     // 35 para Ana, 15 para Berta

        assertFalse(gestor.estaActiva(deAna), "la de Ana ya debería haber caducado")
        assertTrue(gestor.estaActiva(deBerta), "la de Berta todavía no")
    }

    @Test
    fun `el token incluye el usuario y el instante de apertura`() {
        // El token es determinista a propósito, para poder comprobarlo.
        assertEquals("ana-1000000", gestor.abrir("ana"))
    }

    @Test
    fun `se puede usar un reloj hecho con una lambda`() {
        // `Reloj` es una `fun interface`, así que una lambda basta (capítulo 05).
        val congelado = GestorDeSesiones(Reloj { 0L }, duracionMs = 1_000)

        val token = congelado.abrir("ana")

        // El tiempo nunca avanza: la sesión no caduca jamás.
        assertTrue(congelado.estaActiva(token))
    }
}
