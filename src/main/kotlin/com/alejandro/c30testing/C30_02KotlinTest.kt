package com.alejandro.c30testing

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  30.2 · kotlin.test: anatomía de un test
//
//  LA DEPENDENCIA
//    En `build.gradle.kts`:
//
//        testImplementation(kotlin("test"))
//
//    y, para que JUnit 5 ejecute los tests:
//
//        tasks.test { useJUnitPlatform() }
//
//    `kotlin.test` es una fachada: por debajo usa JUnit en la JVM, pero tú escribes
//    siempre `assertEquals` y `@Test`, sin importar de JUnit.
//
//  POR QUÉ ESTAS DEMOS NO USAN assertEquals
//    `kotlin.test` está en `testImplementation`, así que NO está en el classpath del
//    código de producción: estas demos no pueden llamarlo. Lo que se ve aquí es el
//    código de los tests como texto y una versión mínima hecha a mano para que se
//    entienda qué hace un aserto. Los tests de verdad están en `src/test/kotlin`.
// =====================================================================================

/**
 * La estructura de un test.
 */
fun demoAnatomyOfATest() {
    section("Dónde van los ficheros")

    bullet("src/main/kotlin/com/alejandro/c30testing/C30_01Testable.kt   ← el código")
    bullet("src/test/kotlin/com/alejandro/c30testing/PreciosTest.kt      ← su test")
    bullet("")
    bullet("Mismo paquete, distinto source set. Gradle los junta solo.")
    bullet("Como comparten paquete, el test VE las declaraciones `internal` del")
    bullet("código de producción (capítulo 07). Eso es a propósito.")

    section("Un test completo")

    imprimirCodigo(
        """
        package com.alejandro.c30testing

        import kotlin.test.Test
        import kotlin.test.assertEquals

        class PreciosTest {

            @Test
            fun `el subtotal suma cantidad por precio de cada linea`() {
                // Preparar
                val lineas = listOf(
                    LineaPedido("teclado", cantidad = 1, precioUnitario = 45.0),
                    LineaPedido("ratón", cantidad = 2, precioUnitario = 12.5),
                )

                // Actuar
                val subtotal = calcularSubtotal(lineas)

                // Comprobar
                assertEquals(70.0, subtotal)
            }
        }
        """.trimIndent(),
    )

    section("Las tres partes: preparar, actuar, comprobar")

    bullet("PREPARAR (arrange) → construir los datos de entrada.")
    bullet("ACTUAR   (act)     → llamar a lo que se está probando. UNA sola línea.")
    bullet("COMPROBAR (assert) → verificar el resultado.")
    bullet("")
    bullet("Si el bloque de 'actuar' tiene cinco llamadas, el test está probando")
    bullet("cinco cosas y cuando falle no sabrás cuál.")

    section("Nombres con acentos graves")

    bullet("fun `el subtotal suma cantidad por precio de cada linea`()")
    bullet("")
    bullet("Kotlin permite nombres de función entre ` ` en los tests. Úsalo: el")
    bullet("informe de fallos se lee como una lista de requisitos.")
    bullet("La plantilla que funciona: «QUÉ hace CUÁNDO pasa esto».")
    bullet("  `devuelve cero cuando la lista esta vacia`")
    bullet("  `lanza IllegalArgumentException si el descuento supera 100`")
    bullet("(Evita la ñ y los acentos en el nombre: algunos sistemas de informes")
    bullet(" y algunas plataformas todavía se atragantan con ellos.)")

    section("Un test por comportamiento, no por función")

    bullet("`aplicarDescuento` no tiene un test: tiene cuatro.")
    bullet("  · aplica el porcentaje correctamente")
    bullet("  · con 0 devuelve el mismo importe")
    bullet("  · con 100 devuelve cero")
    bullet("  · con un porcentaje fuera de rango, lanza")
    bullet("Cuando uno se pone rojo, el nombre ya te dice qué se ha roto.")

    section("@BeforeTest y @AfterTest")

    imprimirCodigo(
        """
        class GestorDeSesionesTest {

            private lateinit var reloj: RelojManipulable
            private lateinit var gestor: GestorDeSesiones

            @BeforeTest
            fun preparar() {
                reloj = RelojManipulable(inicioMs = 0)
                gestor = GestorDeSesiones(reloj, duracionMs = 1_000)
            }

            @Test
            fun `la sesion caduca al pasar su duracion`() { ... }
        }
        """.trimIndent(),
    )

    bullet("JUnit crea una INSTANCIA NUEVA de la clase por cada test, así que los")
    bullet("campos no se comparten. `@BeforeTest` es para la preparación repetida.")
    bullet("`@AfterTest` es para cerrar lo que haga falta (ficheros, conexiones).")

    section("Cómo se ejecutan")

    bullet("./gradlew test                         → toda la suite")
    bullet("./gradlew test --tests '*PreciosTest'  → una clase")
    bullet("./gradlew test --info                  → con la salida de cada test")
    bullet("En IntelliJ: el ▶ verde al lado de la clase o de cada `@Test`.")
    bullet("El informe HTML queda en build/reports/tests/test/index.html")
}

/**
 * El catálogo de asertos.
 */
fun demoAssertions() {
    section("Los que se usan el 95% de las veces")

    bullet("assertEquals(esperado, real)        ← OJO al orden: esperado primero")
    bullet("assertNotEquals(noEsperado, real)")
    bullet("assertTrue(condicion)")
    bullet("assertFalse(condicion)")
    bullet("assertNull(valor)")
    bullet("assertNotNull(valor)                ← además hace smart cast a no nulo")

    section("Para colecciones y arrays")

    bullet("assertContentEquals(listOf(1, 2), resultado)   ← compara ELEMENTO a elemento")
    bullet("assertContains(coleccion, elemento)")
    bullet("")
    bullet("`assertEquals` con dos IntArray compara referencias y falla siempre:")
    bullet("para arrays hay que usar `assertContentEquals` (capítulo 02).")

    section("Para excepciones")

    imprimirCodigo(
        """
        @Test
        fun `lanza si el descuento supera 100`() {
            val fallo = assertFailsWith<IllegalArgumentException> {
                aplicarDescuento(100.0, porcentaje = 150)
            }
            // El mensaje también es parte del contrato.
            assertContains(fallo.message.orEmpty(), "entre 0 y 100")
        }
        """.trimIndent(),
    )

    bullet("`assertFailsWith<T> { }` DEVUELVE la excepción: aprovéchalo para")
    bullet("comprobar el mensaje o los datos que lleva dentro.")
    bullet("Un try/catch a mano con `fail()` en el camino feliz también vale, pero")
    bullet("es cuatro líneas más y se olvida el `fail()`.")

    section("Los números con decimales")

    bullet("assertEquals(0.3, 0.1 + 0.2)              ← FALLA")
    show("0.1 + 0.2", 0.1 + 0.2)
    bullet("assertEquals(0.3, 0.1 + 0.2, absoluteTolerance = 1e-9)   ← pasa")
    bullet("Para dinero, `BigDecimal` o céntimos en `Int` (capítulo 02).")

    section("Los demás")

    bullet("assertIs<T>(valor)          → comprueba el tipo y hace smart cast")
    bullet("assertSame / assertNotSame  → identidad (===), no igualdad")
    bullet("fail(\"mensaje\")             → falla a propósito: para ramas imposibles")
    bullet("assertFails { }             → como assertFailsWith pero sin fijar el tipo")

    section("Qué se ve cuando un aserto falla")

    // Una versión mínima hecha a mano, para que se vea el mecanismo: un aserto
    // no es más que una comparación y un mensaje útil.
    show("comprobar(70.0, 70.0)", miniAssertEquals(70.0, 70.0))
    show("comprobar(70.0, 69.5)", miniAssertEquals(70.0, 69.5))

    bullet("El valor del aserto está en el MENSAJE: 'esperaba X pero era Y'.")
    bullet("Por eso `assertTrue(a == b)` es peor que `assertEquals(a, b)`: el")
    bullet("primero sólo puede decirte 'era false'.")

    section("El mensaje opcional")

    bullet("assertTrue(lista.isNotEmpty(), \"la búsqueda no devolvió resultados\")")
    bullet("Va SIEMPRE al final, y sólo hace falta cuando el aserto no se explica solo.")
}

/**
 * Dobles de test: cuando hay dependencias de por medio.
 */
fun demoTestDoubles() {
    section("Los cinco nombres")

    bullet("Dummy → se pasa para rellenar; nunca se usa.")
    bullet("Stub  → devuelve respuestas fijas. 'cuando te pidan X, devuelve Y'.")
    bullet("Fake  → una implementación de verdad, pero simplificada (un repositorio")
    bullet("        en memoria en vez de la base de datos). El más útil con diferencia.")
    bullet("Mock  → además VERIFICA las llamadas: 'se llamó a guardar una vez'.")
    bullet("Spy   → envuelve al real y registra lo que pasa por él.")

    section("En Kotlin casi siempre basta con un fake")

    imprimirCodigo(
        """
        // La interfaz que usa el código de producción
        interface RepositorioDeUsuarios {
            fun buscar(id: Int): Usuario?
            fun guardar(usuario: Usuario)
        }

        // El fake del test: diez líneas, cero dependencias, cero magia
        class RepositorioEnMemoria : RepositorioDeUsuarios {
            private val datos = mutableMapOf<Int, Usuario>()
            var vecesQueSeGuardo = 0
                private set

            override fun buscar(id: Int) = datos[id]
            override fun guardar(usuario: Usuario) {
                datos[usuario.id] = usuario
                vecesQueSeGuardo++
            }
        }
        """.trimIndent(),
    )

    bullet("Sin librerías de mocking, sin reflexión, sin sorpresas al actualizar.")
    bullet("Y de regalo puede contar llamadas, que es lo que se pediría a un mock.")

    section("El fake del reloj, funcionando")

    val reloj = RelojManipulable(inicioMs = 0)
    val gestor = GestorDeSesiones(reloj, duracionMs = 1_000)

    val token = gestor.abrir("ana")
    show("recién abierta", gestor.estaActiva(token))
    reloj.avanzarMinutos(1)
    show("un minuto después (duración 1 s)", gestor.estaActiva(token))

    bullet("Éste es el `Reloj` de la demo 30.2, que es exactamente un fake.")

    section("Cuándo sí merece una librería (MockK)")

    bullet("Interfaces grandes de las que sólo te interesan dos métodos.")
    bullet("Clases de terceros que no puedes envolver.")
    bullet("Cuando de verdad necesitas verificar el ORDEN de las llamadas.")
    bullet("Fuera de ahí, un fake escrito a mano se lee mejor y no se rompe solo.")

    section("El aviso")

    bullet("Si un test necesita cinco dobles, el problema no es el test: esa clase")
    bullet("tiene cinco dependencias y probablemente cinco responsabilidades.")
}

// -- Utilidades de la demo ---------------------------------------------------------------------

/**
 * Una versión mínima de `assertEquals`, sólo para ENSEÑAR qué hace un aserto.
 *
 * No la uses: en los tests de verdad está `kotlin.test.assertEquals`, que además
 * integra con el informe de JUnit y con el IDE.
 */
private fun miniAssertEquals(esperado: Any?, real: Any?): String =
    if (esperado == real) "✓ pasa" else "✗ falla: esperaba <$esperado> pero era <$real>"

/** Imprime un bloque de código con sangría, sin los adornos de `bullet`. */
private fun imprimirCodigo(texto: String) {
    texto.trimEnd().lines().forEach { println("      $it") }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Abre `src/test/kotlin/com/alejandro/c30testing/PreciosTest.kt` y rompe a
//     propósito una expectativa: lee el mensaje de fallo entero.
//  2. Cambia un `assertEquals` por `assertTrue(a == b)` y compara los dos mensajes.
//  3. Escribe un test con `assertFailsWith` para `anadirIva(100.0, -1)`.
//  4. Ejecuta `./gradlew test --tests '*ValidacionTest'` y mira el informe HTML.
