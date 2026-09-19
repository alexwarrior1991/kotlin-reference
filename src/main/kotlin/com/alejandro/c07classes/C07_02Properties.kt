package com.alejandro.c07classes

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  7.2 · Propiedades, getters, setters y backing fields
//
//  QUÉ ES
//    En Kotlin no hay campos públicos: cada `val`/`var` de una clase es una PROPIEDAD,
//    es decir, un getter (y un setter si es `var`). El campo que guarda el valor —el
//    *backing field*— sólo existe si hace falta, y se llama `field`.
//
//  POR QUÉ IMPORTA
//    Permite empezar con `val nombre: String` y, más adelante, añadirle validación o
//    hacerlo calculado SIN cambiar una sola línea del código que lo usa. En Java eso
//    exige convertir el campo en `getNombre()` y tocar a todos los que llamaban.
//
//  ERRORES COMUNES
//    · Escribir getters y setters a mano estilo Java (`getNombre()`).
//    · Usar el nombre de la propiedad dentro de su propio getter → recursión infinita.
//    · Poner lógica cara en un getter: quien lo lee cree que es un acceso barato.
// =====================================================================================

/**
 * Propiedades frente a campos.
 */
fun demoProperties() {
    section("Una propiedad no es un campo")

    val persona = Persona("Ana", 34)
    show("persona.nombre", persona.nombre)
    show("persona.edad", persona.edad)

    // Al asignar, se llama al setter; al leer, al getter. La sintaxis es la misma
    // que un campo público, pero por debajo son métodos.
    persona.edad = 35
    show("tras persona.edad = 35", persona.edad)

    bullet("En Java: private int edad; public int getEdad(); public void setEdad(int).")
    bullet("En Kotlin: `var edad: Int`. Y desde Java se ve exactamente igual que allí.")

    section("val genera sólo getter; var, getter y setter")

    // persona.nombre = "Otra"   // ERROR: Val cannot be reassigned
    bullet("`val` no es lo mismo que `final`: es 'sin setter'. Puede tener getter propio.")
}

/**
 * Propiedades calculadas: getter personalizado, sin backing field.
 */
fun demoComputedProperties() {
    section("Una propiedad que se calcula en cada lectura")

    val rectangulo = Rectangulo(ancho = 3.0, alto = 4.0)
    show("rectangulo.area", rectangulo.area)
    show("rectangulo.perimetro", rectangulo.perimetro)
    show("rectangulo.esCuadrado", rectangulo.esCuadrado)

    // Al no tener backing field, el valor se recalcula: si cambian los datos, cambia.
    rectangulo.alto = 3.0
    show("tras alto = 3.0 → area", rectangulo.area)
    show("tras alto = 3.0 → esCuadrado", rectangulo.esCuadrado)

    section("¿Propiedad calculada o función?")

    bullet("Propiedad si: es barata, no tiene efectos, y se lee como un ATRIBUTO.")
    bullet("Función si: es cara, puede fallar, o se lee como una ACCIÓN.")
    bullet("`lista.size` es propiedad; `lista.sum()` es función. Ése es el criterio.")

    section("El error clásico: recursión infinita")

    // val nombreCompleto: String
    //     get() = nombreCompleto          // ← se llama a sí mismo hasta desbordar
    //
    // Dentro del getter hay que usar `field`, no el nombre de la propiedad.
    bullet("Dentro del getter, el nombre de la propiedad significa 'llamar al getter'.")
    bullet("Para llegar al valor almacenado hay que escribir `field`.")
}

/**
 * Setters personalizados y el backing field.
 */
fun demoCustomSetters() {
    section("Validar al asignar")

    val cuenta = CuentaBancaria(saldoInicial = 100.0)
    show("saldo inicial", cuenta.saldo)

    cuenta.saldo = 250.0
    show("tras saldo = 250.0", cuenta.saldo)

    // El setter rechaza los negativos en lugar de guardarlos.
    cuenta.saldo = -50.0
    show("tras saldo = -50.0 (rechazado)", cuenta.saldo)
    show("intentos rechazados", cuenta.intentosInvalidos)

    section("Normalizar al asignar")

    val usuario = UsuarioNormalizado()
    usuario.email = "  ANA@Ejemplo.COM  "
    show("tras asignar '  ANA@Ejemplo.COM  '", usuario.email)

    section("`field`: el almacén real")

    bullet("`field` sólo existe dentro del getter y del setter de esa propiedad.")
    bullet("El compilador crea el backing field ÚNICAMENTE si usas `field`.")
    bullet("Si tu getter no lo usa, la propiedad no ocupa memoria: es pura función.")

    section("Setter con visibilidad reducida")

    // Un patrón muy común: leer desde fuera, escribir sólo desde dentro.
    val partida = Partida()
    show("partida.puntos", partida.puntos)
    partida.anotar(10)
    partida.anotar(5)
    show("tras anotar 10 y 5", partida.puntos)
    // partida.puntos = 1000   // ERROR: Cannot assign to 'puntos': the setter is private
    bullet("`var puntos: Int = 0; private set` → público para leer, privado para escribir.")
}

/**
 * Propiedades de nivel superior y constantes.
 */
fun demoTopLevelProperties() {
    section("Fuera de cualquier clase")

    show("VERSION (const val)", VERSION)
    show("limiteCalculado (val con getter)", limiteCalculado)

    section("Diferencias")

    bullet("const val  → se resuelve al compilar; se inserta donde se use.")
    bullet("val        → se calcula una vez, al cargar la clase del fichero.")
    bullet("val con get() → se recalcula en CADA lectura.")

    show("contadorDeLecturas tras 3 lecturas", "${limiteCalculado}, ${limiteCalculado}, ${limiteCalculado}")

    section("Cuándo usarlas")

    bullet("Constantes de configuración, expresiones regulares compiladas, tablas fijas.")
    bullet("Si necesitan estado mutable compartido, párate a pensar: suele ser mala idea.")
}

// -- Las clases que usan las demos ----------------------------------------------------

private class Persona(val nombre: String, var edad: Int)

/**
 * `area` y `perimetro` no guardan nada: se calculan al leerlas.
 * Por eso reflejan al instante cualquier cambio en `ancho` o `alto`.
 */
private class Rectangulo(var ancho: Double, var alto: Double) {

    val area: Double
        get() = ancho * alto

    val perimetro: Double
        get() = 2 * (ancho + alto)

    val esCuadrado: Boolean
        get() = ancho == alto
}

/** Setter que valida: el valor inválido no llega a guardarse. */
private class CuentaBancaria(saldoInicial: Double) {

    var intentosInvalidos: Int = 0
        private set

    var saldo: Double = saldoInicial
        set(nuevo) {
            if (nuevo < 0) {
                intentosInvalidos++
                return                 // no tocamos `field`: el saldo se queda como estaba
            }
            field = nuevo              // `field` es el almacén real
        }
}

/** Setter que normaliza: lo que se guarda no es exactamente lo que se asignó. */
private class UsuarioNormalizado {
    var email: String = ""
        set(nuevo) {
            field = nuevo.trim().lowercase()
        }
}

/** El patrón "público para leer, privado para escribir". */
private class Partida {
    var puntos: Int = 0
        private set

    fun anotar(cantidad: Int) {
        require(cantidad > 0) { "los puntos deben ser positivos" }
        puntos += cantidad
    }
}

// Propiedades de nivel superior, fuera de toda clase.
private const val VERSION = "1.0"

private var contadorDeLecturas = 0

/** Un `val` con getter se recalcula en cada lectura. */
private val limiteCalculado: Int
    get() = ++contadorDeLecturas

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. En Rectangulo, cambia `val area get() = ...` por `val area = ancho * alto` y
//     comprueba que deja de actualizarse al cambiar el alto.
//  2. Escribe un getter que use el nombre de su propia propiedad y observa el
//     StackOverflowError (o el aviso del IDE, que lo detecta antes).
//  3. Quita el `private set` de Partida y comprueba que ya se puede hacer trampa.
//  4. Añade a UsuarioNormalizado un getter que devuelva el email enmascarado.
