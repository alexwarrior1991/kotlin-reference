package com.alejandro.c31exercises

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  Ejercicio 5 · Carrito de compra                                         🟡 medio
//
//  Repasa: data class, sealed, colecciones, dinero en enteros, reglas de negocio.
//  Capítulos: 02 (números), 09 (data class), 11 (sealed), 13 (colecciones).
// =====================================================================================

/** Ejercicio 5: carrito de la compra con cupones. */
fun ejercicio05CarritoDeCompra() {
    enunciado(
        "Un carrito de compra que sume bien. Suena fácil; no lo es.",
        "",
        "1. `Articulo` con código, nombre y precio. IMPORTANTE: el precio va en",
        "   CÉNTIMOS enteros, no en Double (mira la explicación si te sorprende).",
        "2. `Carrito` con `anadir(articulo, unidades)`, `quitar(codigo)`,",
        "   `cambiarUnidades(codigo, unidades)` y `vaciar()`.",
        "3. Añadir dos veces el mismo artículo debe SUMAR unidades, no duplicar línea.",
        "4. Poner unidades a 0 (o menos) debe quitar la línea.",
        "5. Cupones como jerarquía sellada: porcentaje, importe fijo y envío gratis.",
        "6. `resumen()` con subtotal, descuento, envío y total. Envío: 4,95 € salvo",
        "   que el subtotal pase de 50 € o haya cupón de envío gratis.",
        "7. El descuento nunca puede dejar el total por debajo de cero.",
    )

    pistas(
        "El céntimo entero es la clave: `0.1 + 0.2 != 0.3` en Double (capítulo 02).",
        "   Con `Int` de céntimos, todo cuadra y sólo divides al IMPRIMIR.",
        "Guarda las líneas en un `LinkedHashMap<String, LineaCarrito>` con el código",
        "   como clave: buscar y sumar unidades pasa a ser una línea.",
        "`merge` del Map hace justo lo de «si ya está, súmalo; si no, ponlo».",
        "Un `sealed interface Cupon` con tres implementaciones y un `when` para",
        "   calcular el descuento: añadir un cupón nuevo será imposible de olvidar.",
        "Para el tope, `coerceAtMost(subtotal)`: un descuento nunca supera lo que hay.",
    )

    solucionEnMarcha()

    val teclado = Articulo("TEC-1", "Teclado mecánico", precioCentimos = 4_500)
    val raton = Articulo("RAT-1", "Ratón inalámbrico", precioCentimos = 1_250)
    val monitor = Articulo("MON-1", "Monitor 27\"", precioCentimos = 21_900)

    section("Añadir y acumular")

    val carrito = Carrito()
    carrito.anadir(teclado, 1)
    carrito.anadir(raton, 2)
    carrito.anadir(raton, 1)          // ← no crea línea nueva: suma unidades

    carrito.lineas().forEach { show(it.articulo.nombre, "${it.unidades} × ${euros(it.articulo.precioCentimos)} = ${euros(it.subtotalCentimos)}") }
    show("líneas", carrito.lineas().size)
    show("unidades totales", carrito.unidadesTotales())

    section("Cambiar y quitar")

    carrito.cambiarUnidades("RAT-1", 5)
    show("ratones tras cambiar", carrito.lineas().first { it.articulo.codigo == "RAT-1" }.unidades)

    carrito.cambiarUnidades("RAT-1", 0)
    show("tras poner 0 unidades", carrito.lineas().map { it.articulo.codigo })

    show("quitar un código que no está", carrito.quitar("NO-EXISTE"))

    section("Resumen sin cupón (envío no gratis)")

    imprimirResumen(carrito.resumen())

    section("Resumen pasando de 50 € (envío gratis por importe)")

    carrito.anadir(monitor, 1)
    imprimirResumen(carrito.resumen())

    section("Con cupón de porcentaje")

    imprimirResumen(carrito.resumen(Cupon.Porcentaje("VERANO10", 10)))

    section("Con cupón de importe fijo")

    imprimirResumen(carrito.resumen(Cupon.ImporteFijo("MENOS5", 500)))

    section("Con cupón de envío gratis en un carrito pequeño")

    val pequeno = Carrito().apply { anadir(raton, 1) }
    imprimirResumen(pequeno.resumen())
    imprimirResumen(pequeno.resumen(Cupon.EnvioGratis("PORTES")))

    section("Un descuento mayor que el carrito")

    imprimirResumen(pequeno.resumen(Cupon.ImporteFijo("EXAGERADO", 999_999)))

    explicacion(
        "LO MÁS IMPORTANTE DE ESTE EJERCICIO: el dinero NO se guarda en Double.",
        "",
        "`0.1 + 0.2` da 0.30000000000000004 en cualquier lenguaje con IEEE 754, y un",
        "carrito con veinte líneas acumula céntimos fantasma que luego no cuadran con",
        "la pasarela de pago. Las dos soluciones aceptables son enteros de céntimos",
        "(rápido, suficiente) o BigDecimal (más lento, con escala y redondeo",
        "explícitos). Double no es una de ellas. Nunca.",
        "",
        "Aquí se usan Int de céntimos y se divide entre 100 SÓLO al imprimir. Todos",
        "los cálculos intermedios son exactos.",
        "",
        "Los cupones son un `sealed interface`: cuando añadas 'dos por uno' el `when`",
        "de `calcularDescuento` dejará de compilar hasta que lo trates. Eso es",
        "justamente lo que quieres de una regla de negocio (capítulo 11).",
        "",
        "Y el `coerceAtMost(subtotal)`: sin él, un cupón de 100 € en un carrito de",
        "12,50 € daría un total negativo. Es el tipo de detalle que sólo aparece en",
        "producción... o en un test de caso límite (capítulo 30).",
    )

    varianteDificil(
        "1. Cupones combinables: acepta una lista y decide el orden de aplicación",
        "   (¿el porcentaje sobre el precio original o sobre el ya descontado?).",
        "2. IVA por tipo de artículo (21%, 10%, 4%) calculado por línea.",
        "3. Fecha de caducidad en el cupón y un `Reloj` inyectado (capítulo 30).",
        "4. Stock: que `anadir` falle si no hay unidades suficientes, devolviendo un",
        "   `sealed` con el motivo en vez de un Boolean.",
        "5. Haz el carrito inmutable: cada operación devuelve un carrito nuevo. Compara",
        "   la comodidad de las dos versiones antes de decidir cuál prefieres.",
    )

    testEn("CarritoTest.kt")
}

// =====================================================================================
//  SOLUCIÓN PROPUESTA
// =====================================================================================

/** El precio va en CÉNTIMOS enteros: nada de Double para dinero. */
data class Articulo(val codigo: String, val nombre: String, val precioCentimos: Int) {
    init {
        require(codigo.isNotBlank()) { "el artículo necesita un código" }
        require(precioCentimos >= 0) { "el precio no puede ser negativo: $precioCentimos" }
    }
}

data class LineaCarrito(val articulo: Articulo, val unidades: Int) {
    val subtotalCentimos: Int get() = articulo.precioCentimos * unidades
}

/** Los tipos de cupón. Añadir uno nuevo rompe el `when` hasta que lo trates. */
sealed interface Cupon {
    val codigo: String

    data class Porcentaje(override val codigo: String, val porcentaje: Int) : Cupon {
        init {
            require(porcentaje in 1..100) { "porcentaje fuera de rango: $porcentaje" }
        }
    }

    data class ImporteFijo(override val codigo: String, val centimos: Int) : Cupon {
        init {
            require(centimos > 0) { "el importe del cupón debe ser positivo" }
        }
    }

    data class EnvioGratis(override val codigo: String) : Cupon
}

/** Lo que hay que pagar, desglosado. Todo en céntimos. */
data class ResumenCarrito(
    val subtotalCentimos: Int,
    val descuentoCentimos: Int,
    val envioCentimos: Int,
    val cuponAplicado: String?,
) {
    val totalCentimos: Int get() = subtotalCentimos - descuentoCentimos + envioCentimos
}

class Carrito {

    // LinkedHashMap: conserva el orden en que se añadieron los artículos.
    private val lineas = linkedMapOf<String, LineaCarrito>()

    /** Si el artículo ya estaba, SUMA unidades en vez de crear otra línea. */
    fun anadir(articulo: Articulo, unidades: Int = 1) {
        require(unidades > 0) { "hay que añadir al menos una unidad, no $unidades" }

        lineas.merge(articulo.codigo, LineaCarrito(articulo, unidades)) { vieja, nueva ->
            vieja.copy(unidades = vieja.unidades + nueva.unidades)
        }
    }

    /** @return `true` si el artículo estaba en el carrito. */
    fun quitar(codigo: String): Boolean = lineas.remove(codigo) != null

    /** Poner 0 o menos unidades equivale a quitar la línea. */
    fun cambiarUnidades(codigo: String, unidades: Int): Boolean {
        val linea = lineas[codigo] ?: return false
        if (unidades <= 0) {
            lineas.remove(codigo)
        } else {
            lineas[codigo] = linea.copy(unidades = unidades)
        }
        return true
    }

    fun vaciar() = lineas.clear()

    fun lineas(): List<LineaCarrito> = lineas.values.toList()

    fun estaVacio(): Boolean = lineas.isEmpty()

    fun unidadesTotales(): Int = lineas.values.sumOf { it.unidades }

    fun subtotalCentimos(): Int = lineas.values.sumOf { it.subtotalCentimos }

    /**
     * Calcula lo que hay que pagar.
     *
     * El orden importa: primero el descuento sobre el subtotal, después el envío
     * (que depende del subtotal SIN descontar, para no premiar dos veces).
     */
    fun resumen(cupon: Cupon? = null): ResumenCarrito {
        val subtotal = subtotalCentimos()

        val descuento = when (cupon) {
            null -> 0
            is Cupon.Porcentaje -> subtotal * cupon.porcentaje / 100
            is Cupon.ImporteFijo -> cupon.centimos
            is Cupon.EnvioGratis -> 0
        }.coerceAtMost(subtotal)      // un descuento nunca deja el total en negativo

        val envio = when {
            subtotal == 0 -> 0                            // carrito vacío: no hay envío
            cupon is Cupon.EnvioGratis -> 0
            subtotal >= UMBRAL_ENVIO_GRATIS_CENTIMOS -> 0
            else -> ENVIO_CENTIMOS
        }

        return ResumenCarrito(subtotal, descuento, envio, cupon?.codigo)
    }

    companion object {
        const val ENVIO_CENTIMOS = 495
        const val UMBRAL_ENVIO_GRATIS_CENTIMOS = 5_000
    }
}

/**
 * Céntimos → texto legible. La división por 100 se hace SÓLO aquí.
 *
 * Nada de `%,d`: el separador de miles depende de la configuración regional de la
 * máquina y haría que la salida cambiara de un ordenador a otro.
 */
fun euros(centimos: Int): String = "${centimos / 100},${"%02d".format(centimos % 100)} €"

// -- Sólo para imprimir la demo ------------------------------------------------------------------

private fun imprimirResumen(resumen: ResumenCarrito) {
    show("subtotal", euros(resumen.subtotalCentimos))
    if (resumen.cuponAplicado != null) {
        show("cupón ${resumen.cuponAplicado}", "-${euros(resumen.descuentoCentimos)}")
    }
    show("envío", if (resumen.envioCentimos == 0) "gratis" else euros(resumen.envioCentimos))
    show("TOTAL", euros(resumen.totalCentimos))
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `precioCentimos: Int` por `precio: Double` y suma 0,1 € diez veces.
//     Compara con 1,00 €. Ahí está el motivo del ejercicio entero.
//  2. Añade `Cupon.DosPorUno(codigo, codigoArticulo)` y deja que el compilador te
//     lleve de la mano hasta el `when` que hay que completar.
//  3. Calcula el envío sobre el subtotal YA descontado y decide si te gusta más.
