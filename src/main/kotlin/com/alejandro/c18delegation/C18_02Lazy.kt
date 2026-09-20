package com.alejandro.c18delegation

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  18.2 · Delegación de propiedades: `by lazy`
//
//  QUÉ ES
//    `val x by lazy { ... }` calcula el valor la PRIMERA vez que se lee y lo recuerda.
//    Es la delegación de propiedades más usada con diferencia.
//
//  POR QUÉ IMPORTA
//    Resuelve tres problemas a la vez: no pagar por lo que no se usa, romper
//    dependencias de orden de inicialización, y hacerlo de forma segura entre hilos
//    sin escribir un doble chequeo a mano.
//
//  ERRORES COMUNES
//    · Usar `lazy` para algo que siempre se va a leer (no ahorra nada y añade un objeto).
//    · Usar `LazyThreadSafetyMode.NONE` sin estar seguro de que sólo hay un hilo.
//    · Esperar que `lazy` reevalúe cuando cambian sus dependencias (no lo hace nunca).
// =====================================================================================

/**
 * Cómo funciona.
 */
fun demoLazyBasics() {
    section("El valor se calcula al primer acceso")

    val objeto = ConfiguracionPerezosa()
    show("¿se calculó al construir?", objeto.vecesCalculado)

    show("primera lectura", objeto.rutaCompleta)
    show("veces calculado", objeto.vecesCalculado)

    show("segunda lectura", objeto.rutaCompleta)
    show("tercera lectura", objeto.rutaCompleta)
    show("veces calculado (sigue siendo 1)", objeto.vecesCalculado)

    bullet("Se calcula UNA vez y se guarda. Las siguientes lecturas son gratis.")

    section("Si nunca se lee, nunca se calcula")

    val nuncaUsado = ConfiguracionPerezosa()
    show("veces calculado sin leer nada", nuncaUsado.vecesCalculado)
    bullet("Ése es el ahorro: lo que no se usa, no cuesta.")

    section("También funciona con variables locales")

    var vecesLocal = 0
    val resultado by lazy {
        vecesLocal++
        "calculado"
    }
    show("antes de leer", vecesLocal)
    show("leyendo", resultado)
    show("después de leer", vecesLocal)
}

/**
 * Para qué sirve de verdad.
 */
fun demoLazyUseCases() {
    section("1. Inicialización cara que quizá no haga falta")

    val informe = Informe(listOf("a", "b", "c"))
    show("cabecera (barata)", informe.cabecera)
    show("¿se generó el cuerpo?", informe.cuerpoGenerado)

    show("ahora sí, el cuerpo", informe.cuerpo.take(30) + "...")
    show("¿se generó?", informe.cuerpoGenerado)

    bullet("Si el usuario sólo mira la cabecera, el cuerpo nunca se construye.")

    section("2. Romper dependencias de orden de inicialización")

    // Sin `lazy`, `doble` no podría usar `base` porque se declara más abajo.
    val orden = OrdenDeclaracion()
    show("doble (declarado ANTES que base)", orden.doble)
    bullet("Sin `lazy` esto sería 'Variable must be initialized'. Capítulo 7.2.")

    section("3. Referencias circulares controladas")

    bullet("Dos objetos que se necesitan mutuamente pueden resolverse con `lazy`,")
    bullet("porque la referencia se resuelve en el primer uso, no al construir.")
    bullet("Aun así: si te hacen falta, revisa el diseño antes.")

    section("4. Singletons perezosos dentro de una clase")

    // Un `object` de nivel superior ya es perezoso; `lazy` sirve para el mismo
    // efecto pero asociado a una instancia concreta.
    val servicio = ServicioConCliente()
    show("¿cliente creado al construir?", servicio.clienteCreado)
    show("usando el cliente", servicio.enviar("hola"))
    show("¿cliente creado ahora?", servicio.clienteCreado)
}

/**
 * Los modos de sincronización.
 */
fun demoLazyModes() {
    section("SYNCHRONIZED (el valor por defecto)")

    bullet("Usa un cerrojo: sólo un hilo ejecuta la inicialización.")
    bullet("Los demás esperan y reciben el mismo valor. Es lo que quieres casi siempre.")

    val porDefecto by lazy { "calculado con cerrojo" }
    show("by lazy { }", porDefecto)

    section("PUBLICATION")

    bullet("Varios hilos pueden ejecutar la inicialización a la vez, pero sólo el")
    bullet("primero en terminar 'gana' y su valor es el que se guarda.")
    bullet("Útil si la inicialización es barata y sin efectos secundarios.")

    val publicacion by lazy(LazyThreadSafetyMode.PUBLICATION) { "calculado sin cerrojo" }
    show("lazy(PUBLICATION) { }", publicacion)

    section("NONE")

    bullet("Sin ninguna protección. Es el más rápido y el más peligroso.")
    bullet("Sólo si TIENES LA CERTEZA de que un único hilo toca esa propiedad.")
    bullet("Caso típico: propiedades de una vista en la interfaz de usuario.")

    val sinSincronizar by lazy(LazyThreadSafetyMode.NONE) { "sin protección" }
    show("lazy(NONE) { }", sinSincronizar)

    section("Cómo elegir")

    bullet("¿Dudas? → el valor por defecto (SYNCHRONIZED).")
    bullet("¿Sólo un hilo, seguro, y el rendimiento importa? → NONE.")
    bullet("PUBLICATION es un caso raro: si no sabes que lo necesitas, no lo necesitas.")
}

/**
 * `lazy` frente a las alternativas.
 */
fun demoLazyVsAlternatives() {
    section("lazy frente a un getter personalizado")

    val conGetter = Comparativa()
    show("propiedadCalculada, lectura 1", conGetter.propiedadCalculada)
    show("propiedadCalculada, lectura 2", conGetter.propiedadCalculada)
    show("veces que se calculó", conGetter.vecesCalculada)

    show("propiedadPerezosa, lectura 1", conGetter.propiedadPerezosa)
    show("propiedadPerezosa, lectura 2", conGetter.propiedadPerezosa)
    show("veces que se calculó", conGetter.vecesPerezosa)

    bullet("Un `get()` se ejecuta en CADA lectura; `lazy` sólo la primera.")
    bullet("Si el valor puede cambiar, quieres el getter. Si no, quieres lazy.")

    section("lazy frente a lateinit")

    bullet("lazy    → `val`, se calcula solo, no puede ser primitivo por rendimiento")
    bullet("           (funciona, pero envuelve el valor).")
    bullet("lateinit → `var`, lo asigna alguien de fuera, y no admite primitivos")
    bullet("           ni tipos nulables.")
    bullet("¿Sabes cómo calcularlo? → lazy. ¿Te lo dan de fuera? → lateinit.")

    section("lazy frente a inicialización directa")

    bullet("Si la propiedad es barata y siempre se usa, la inicialización directa")
    bullet("es más simple y no crea el objeto Lazy intermedio.")
    bullet("`lazy` tiene un coste: un objeto extra y una comprobación en cada lectura.")

    section("El aviso importante")

    // `lazy` captura las dependencias EN EL MOMENTO del primer acceso, y después
    // ya no las vuelve a mirar nunca.
    val cambiante = ValorCambiante()
    show("perezoso, primera lectura", cambiante.doblePerezoso)
    cambiante.base = 100
    show("tras cambiar base a 100, perezoso", cambiante.doblePerezoso)
    show("tras cambiar base a 100, con getter", cambiante.dobleConGetter)

    bullet("`lazy` NO reevalúa. Si el valor depende de algo que cambia, usa un getter.")
}

// -- Las clases que usan las demos --------------------------------------------------------

private class ConfiguracionPerezosa {
    var vecesCalculado: Int = 0
        private set

    val rutaCompleta: String by lazy {
        vecesCalculado++
        "/opt/aplicacion/configuracion.yaml"
    }
}

private class Informe(private val datos: List<String>) {
    var cuerpoGenerado: Boolean = false
        private set

    /** Barato: se calcula siempre. */
    val cabecera: String = "Informe de ${datos.size} elementos"

    /** Caro: sólo si alguien lo pide. */
    val cuerpo: String by lazy {
        cuerpoGenerado = true
        datos.joinToString("\n") { "línea con el dato '$it' y bastante relleno detrás" }
    }
}

/** `doble` se declara antes que `base` y aun así funciona, gracias a lazy. */
private class OrdenDeclaracion {
    val doble: Int by lazy { base * 2 }
    val base: Int = 21
}

private class ServicioConCliente {
    var clienteCreado: Boolean = false
        private set

    private val cliente: String by lazy {
        clienteCreado = true
        "cliente-http"
    }

    fun enviar(mensaje: String): String = "$cliente envió '$mensaje'"
}

private class Comparativa {
    var vecesCalculada: Int = 0
        private set
    var vecesPerezosa: Int = 0
        private set

    /** Se ejecuta en cada lectura. */
    val propiedadCalculada: String
        get() {
            vecesCalculada++
            return "valor"
        }

    /** Se ejecuta una sola vez. */
    val propiedadPerezosa: String by lazy {
        vecesPerezosa++
        "valor"
    }
}

private class ValorCambiante {
    var base: Int = 1

    /** Congela el valor de `base` en la primera lectura. */
    val doblePerezoso: Int by lazy { base * 2 }

    /** Lee `base` cada vez. */
    val dobleConGetter: Int get() = base * 2
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `by lazy` de OrdenDeclaracion y lee el error de inicialización.
//  2. Cambia `doblePerezoso` por un getter y comprueba que ya sí refleja los cambios.
//  3. Añade una traza dentro del bloque `lazy` y cuenta cuántas veces se imprime.
//  4. Usa `lazy(NONE)` en una propiedad y razona si sería seguro con varios hilos.
