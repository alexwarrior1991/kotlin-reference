package com.alejandro.c29dsl

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  29.4 · Un DSL de configuración: el ejemplo completo
//
//  QUÉ ES
//    El caso realista: describir la configuración de un servicio (puerto, seguridad,
//    base de datos, rutas) con un bloque de Kotlin en lugar de un YAML.
//
//  POR QUÉ IMPORTA
//    Es donde los DSL se ganan el sueldo: hay anidamiento, listas de elementos
//    repetidos, valores por defecto y validación. Y, a diferencia de un fichero de
//    texto, el compilador comprueba nombres y tipos ANTES de arrancar.
//
//  EL PRECIO
//    La configuración deja de poder cambiarse sin recompilar. Si eso importa, un DSL
//    no es la herramienta: lo es un fichero externo. Esa decisión se discute al final.
// =====================================================================================

/**
 * El DSL en uso.
 */
fun demoConfigDsl() {
    section("La configuración escrita como código")

    imprimirBloque(
        """
        servidor {
            nombre = "api-pedidos"
            escuchaEn(host = "0.0.0.0", puerto = 8080)

            seguridad {
                tls = true
                certificado = "/etc/certs/api.pem"
                permitirOrigen("https://ejemplo.com")
                permitirOrigen("https://admin.ejemplo.com")
            }

            baseDeDatos {
                url = "jdbc:postgresql://localhost/pedidos"
                usuario = "app"
                pool { tamanoMaximo = 20; esperaMaximaMs = 3_000 }
            }

            rutas {
                get("/salud")   { "ok" }
                post("/pedidos") { "pedido creado" }
                for (recurso in recursosPublicos) {     // es Kotlin: un bucle normal
                    get("/${'$'}recurso") { "lista de ${'$'}recurso" }
                }
            }
        }
        """.trimIndent(),
    )

    val recursosPublicos = listOf("clientes", "articulos")

    val configuracion = servidor {
        nombre = "api-pedidos"
        escuchaEn(host = "0.0.0.0", puerto = 8080)

        seguridad {
            tls = true
            certificado = "/etc/certs/api.pem"
            permitirOrigen("https://ejemplo.com")
            permitirOrigen("https://admin.ejemplo.com")
        }

        baseDeDatos {
            url = "jdbc:postgresql://localhost/pedidos"
            usuario = "app"
            pool {
                tamanoMaximo = 20
                esperaMaximaMs = 3_000
            }
        }

        rutas {
            get("/salud") { "ok" }
            post("/pedidos") { "pedido creado" }

            // Es Kotlin: un bucle normal genera rutas.
            for (recurso in recursosPublicos) {
                get("/$recurso") { "lista de $recurso" }
            }
        }
    }

    section("Lo que se ha construido")

    show("nombre", configuracion.nombre)
    show("escucha en", "${configuracion.host}:${configuracion.puerto}")
    show("TLS", configuracion.seguridad.tls)
    show("orígenes permitidos", configuracion.seguridad.origenesPermitidos)
    show("base de datos", configuracion.baseDeDatos?.url)
    show("pool (máx, espera)", configuracion.baseDeDatos?.pool?.let { "${it.tamanoMaximo}, ${it.esperaMaximaMs} ms" })
    show("rutas declaradas", configuracion.rutas.size)

    section("Las rutas, una por una")

    configuracion.rutas.forEach { ruta ->
        bullet("${ruta.metodo.name.padEnd(4)} ${ruta.camino.padEnd(14)} → ${ruta.manejador()}")
    }

    section("Valores por defecto: sólo se escribe lo que cambia")

    val minima = servidor {
        nombre = "api-minima"
        rutas { get("/") { "hola" } }
    }
    show("puerto por defecto", minima.puerto)
    show("host por defecto", minima.host)
    show("TLS por defecto", minima.seguridad.tls)
    show("base de datos", minima.baseDeDatos ?: "(ninguna: el bloque es opcional)")

    bullet("El bloque `seguridad { }` ni siquiera se escribió y la configuración")
    bullet("tiene valores sensatos. Un DSL bien hecho sólo pide lo que es obligatorio.")

    section("La validación, en el momento de construir")

    val sinNombre = runCatching { servidor { rutas { get("/") { "x" } } } }
    show("sin nombre", sinNombre.exceptionOrNull()?.message)

    val puertoMalo = runCatching {
        servidor {
            nombre = "x"
            escuchaEn(puerto = 70_000)
            rutas { get("/") { "x" } }
        }
    }
    show("puerto fuera de rango", puertoMalo.exceptionOrNull()?.message)

    val tlsSinCertificado = runCatching {
        servidor {
            nombre = "x"
            seguridad { tls = true }
            rutas { get("/") { "x" } }
        }
    }
    show("TLS sin certificado", tlsSinCertificado.exceptionOrNull()?.message)

    val rutaRepetida = runCatching {
        servidor {
            nombre = "x"
            rutas {
                get("/salud") { "a" }
                get("/salud") { "b" }
            }
        }
    }
    show("ruta duplicada", rutaRepetida.exceptionOrNull()?.message)

    bullet("Cada regla vive en el `construir()` de su bloque, cerca de sus datos.")
    bullet("El mensaje dice qué está mal y con qué valor: eso es la mitad del trabajo.")
}

/**
 * Cuándo merece la pena un DSL y cuándo no.
 */
fun demoWhenToUseDsl() {
    section("Lo que gana un DSL frente a un YAML")

    bullet("El compilador comprueba nombres, tipos y obligatoriedad.")
    bullet("Autocompletado y 'ir a la declaración' en el IDE.")
    bullet("Se puede refactorizar: renombrar un campo cambia todos los usos.")
    bullet("Se pueden usar bucles, condicionales y constantes del propio proyecto.")
    bullet("La validación está en el mismo sitio que la definición.")

    section("Lo que pierde")

    bullet("Hay que recompilar para cambiar un valor.")
    bullet("Quien lo edite necesita saber (algo de) Kotlin.")
    bullet("No se puede generar ni editar desde fuera de la aplicación.")
    bullet("Un error en el bloque puede ser un error de compilación críptico.")

    section("La regla práctica")

    bullet("¿Lo editan programadores, en el repositorio, junto al código? → DSL.")
    bullet("¿Cambia por entorno, sin recompilar, o lo toca alguien de operaciones?")
    bullet("  → fichero externo (y, si acaso, un DSL para LEERLO con tipos).")
    bullet("Gradle Kotlin DSL es el primer caso; el application.yml, el segundo.")

    section("Los tres olores de un DSL mal hecho")

    bullet("1. El orden de las líneas cambia el resultado sin que se note.")
    bullet("   (En HTML el orden es evidente; en una configuración, no.)")
    bullet("2. Hay que escribir cinco líneas para el caso más común: faltan")
    bullet("   valores por defecto.")
    bullet("3. Un bloque a medias no falla: se guarda y revienta horas después.")

    section("Lo que debería tener cualquier DSL tuyo")

    bullet("□ Una única función de entrada pública (`servidor { }`).")
    bullet("□ Builders con constructor `internal` o privado.")
    bullet("□ Resultado inmutable, con `val` y listas copiadas (`toList()`).")
    bullet("□ Validación en `construir()`, con mensajes que digan el valor culpable.")
    bullet("□ `@DslMarker` en cuanto haya dos niveles.")
    bullet("□ Valores por defecto para todo lo que no sea imprescindible.")

    section("Dónde has visto DSL sin darte cuenta")

    bullet("build.gradle.kts      → `plugins { }`, `dependencies { }`")
    bullet("kotlin.test / kotest  → `describe(\"...\") { it(\"...\") { } }`")
    bullet("Ktor                  → `routing { get(\"/\") { } }`")
    bullet("Compose               → `Column { Text(\"hola\") }`")
    bullet("La propia biblioteca  → `buildString { }`, `buildList { }`")
    bullet("Y el `infra/Demo.kt` de este repositorio: `chapter(28, ...) { demo(...) }`")

    section("El resumen del capítulo en cuatro líneas")

    bullet("1. `T.() -> Unit` es todo el mecanismo.")
    bullet("2. `Builder().apply(bloque).construir()` es todo el patrón.")
    bullet("3. La jerarquía de tipos decide qué puede ir dentro de qué.")
    bullet("4. `@DslMarker` evita que un nivel se cuele en otro.")
}

// -- El modelo resultante (inmutable) --------------------------------------------------------------

enum class MetodoHttp { GET, POST, PUT, DELETE }

class Ruta(val metodo: MetodoHttp, val camino: String, val manejador: () -> String)

class Pool(val tamanoMaximo: Int, val esperaMaximaMs: Long)

class BaseDeDatos(val url: String, val usuario: String, val pool: Pool)

class Seguridad(val tls: Boolean, val certificado: String?, val origenesPermitidos: List<String>)

class ConfiguracionServidor(
    val nombre: String,
    val host: String,
    val puerto: Int,
    val seguridad: Seguridad,
    val baseDeDatos: BaseDeDatos?,
    val rutas: List<Ruta>,
)

// -- El DSL ----------------------------------------------------------------------------------------

/** Marcador del DSL de configuración. */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ConfigDsl

@ConfigDsl
class ServidorBuilder internal constructor() {

    var nombre: String = ""

    private var host: String = "localhost"
    private var puerto: Int = 8080
    private var seguridad: Seguridad = Seguridad(tls = false, certificado = null, origenesPermitidos = emptyList())
    private var baseDeDatos: BaseDeDatos? = null
    private var rutas: List<Ruta> = emptyList()

    /**
     * Dos valores que van juntos, en una sola función con argumentos por defecto.
     *
     * Podrían ser dos propiedades (`host = ...`, `puerto = ...`), pero así se lee
     * como una sola decisión y no se puede poner una sin la otra por accidente.
     */
    fun escuchaEn(host: String = this.host, puerto: Int = this.puerto) {
        this.host = host
        this.puerto = puerto
    }

    fun seguridad(bloque: SeguridadBuilder.() -> Unit) {
        seguridad = SeguridadBuilder().apply(bloque).construir()
    }

    fun baseDeDatos(bloque: BaseDeDatosBuilder.() -> Unit) {
        baseDeDatos = BaseDeDatosBuilder().apply(bloque).construir()
    }

    fun rutas(bloque: RutasBuilder.() -> Unit) {
        rutas = RutasBuilder().apply(bloque).construir()
    }

    internal fun construir(): ConfiguracionServidor {
        require(nombre.isNotBlank()) { "el servidor necesita un `nombre`" }
        require(puerto in 1..65_535) { "puerto fuera de rango: $puerto (debe estar entre 1 y 65535)" }
        require(rutas.isNotEmpty()) { "el servidor '$nombre' no declara ninguna ruta" }
        return ConfiguracionServidor(nombre, host, puerto, seguridad, baseDeDatos, rutas)
    }
}

@ConfigDsl
class SeguridadBuilder internal constructor() {

    var tls: Boolean = false
    var certificado: String? = null

    private val origenes = mutableListOf<String>()

    fun permitirOrigen(origen: String) {
        origenes += origen
    }

    internal fun construir(): Seguridad {
        // La regla que sólo tiene sentido aquí dentro, escrita aquí dentro.
        require(!tls || certificado != null) { "con `tls = true` hay que indicar un `certificado`" }
        return Seguridad(tls, certificado, origenes.toList())
    }
}

@ConfigDsl
class BaseDeDatosBuilder internal constructor() {

    var url: String = ""
    var usuario: String = ""

    private var pool: Pool = Pool(tamanoMaximo = 10, esperaMaximaMs = 5_000)

    fun pool(bloque: PoolBuilder.() -> Unit) {
        pool = PoolBuilder().apply(bloque).construir()
    }

    internal fun construir(): BaseDeDatos {
        require(url.isNotBlank()) { "la base de datos necesita una `url`" }
        return BaseDeDatos(url, usuario, pool)
    }
}

@ConfigDsl
class PoolBuilder internal constructor() {

    var tamanoMaximo: Int = 10
    var esperaMaximaMs: Long = 5_000

    internal fun construir(): Pool {
        require(tamanoMaximo > 0) { "el pool necesita al menos una conexión (tamanoMaximo=$tamanoMaximo)" }
        return Pool(tamanoMaximo, esperaMaximaMs)
    }
}

@ConfigDsl
class RutasBuilder internal constructor() {

    private val rutas = mutableListOf<Ruta>()

    fun get(camino: String, manejador: () -> String) = anadir(MetodoHttp.GET, camino, manejador)
    fun post(camino: String, manejador: () -> String) = anadir(MetodoHttp.POST, camino, manejador)
    fun put(camino: String, manejador: () -> String) = anadir(MetodoHttp.PUT, camino, manejador)
    fun delete(camino: String, manejador: () -> String) = anadir(MetodoHttp.DELETE, camino, manejador)

    private fun anadir(metodo: MetodoHttp, camino: String, manejador: () -> String) {
        require(camino.startsWith("/")) { "la ruta '$camino' debe empezar por '/'" }
        rutas += Ruta(metodo, camino, manejador)
    }

    internal fun construir(): List<Ruta> {
        // Una comprobación que un YAML no haría nunca por ti.
        val duplicadas = rutas
            .groupBy { it.metodo to it.camino }
            .filterValues { it.size > 1 }
            .keys
        require(duplicadas.isEmpty()) {
            "rutas duplicadas: " + duplicadas.joinToString { (metodo, camino) -> "$metodo $camino" }
        }
        return rutas.toList()
    }
}

/** La única puerta de entrada del DSL. */
fun servidor(bloque: ServidorBuilder.() -> Unit): ConfiguracionServidor =
    ServidorBuilder().apply(bloque).construir()

/** Imprime un bloque de código con sangría, sin los adornos de `bullet`. */
private fun imprimirBloque(texto: String) {
    texto.trimEnd().lines().forEach { println("      $it") }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade un bloque `metricas { habilitadas = true; intervaloSegundos = 30 }`.
//  2. Haz que `rutas { }` admita un `grupo("/api/v1") { get("/pedidos") { } }` que
//     ponga prefijo a los caminos de dentro.
//  3. Quita `@ConfigDsl` de `PoolBuilder` y comprueba que desde dentro de `pool { }`
//     se puede llamar a `url = ...` de la base de datos: la fuga de la demo 29.4.
//  4. Cambia el manejador `() -> String` por `(Map<String, String>) -> String` para
//     admitir parámetros, y ajusta las llamadas.
