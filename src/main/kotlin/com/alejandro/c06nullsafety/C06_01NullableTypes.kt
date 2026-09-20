package com.alejandro.c06nullsafety

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  6.1 · Tipos nulables
//
//  QUÉ ES
//    En Kotlin, `String` y `String?` son tipos DISTINTOS. El primero no puede valer
//    null nunca; el segundo sí. La diferencia la comprueba el compilador.
//
//  POR QUÉ IMPORTA
//    Tony Hoare, que inventó la referencia nula en 1965, la llamó "mi error de mil
//    millones de dólares". Kotlin no elimina el null: lo mueve al SISTEMA DE TIPOS,
//    de modo que el NullPointerException pasa de ser un fallo en producción a ser un
//    error de compilación.
//
//  ERRORES COMUNES
//    · Declarar todo nulable "por si acaso" y acabar con `?.` por todas partes.
//    · Usar un tipo nulable para señalar un error en vez de modelarlo (capítulo 11).
//    · Creer que Kotlin garantiza que nunca habrá NPE (los hay: `!!`, `lateinit`,
//      y los tipos que vienen de Java).
// =====================================================================================

/**
 * Dos tipos, no uno.
 */
fun demoNullableVsNonNullable() {
    section("La declaración")

    val seguro: String = "nunca será null"
    val nulable: String? = null

    show("val seguro: String", seguro)
    show("val nulable: String?", nulable)

    // val roto: String = null      // ERROR: Null can not be a value of a non-null type String
    bullet("Asignar null a un tipo no nulable es un error de COMPILACIÓN.")

    section("Qué puedes hacer con cada uno")

    // Sobre el no nulable, todo directamente.
    show("seguro.length", seguro.length)
    show("seguro.uppercase()", seguro.uppercase())

    // Sobre el nulable, el compilador te obliga a decidir qué pasa si es null.
    // nulable.length              // ERROR: Only safe (?.) or non-null asserted (!!.) calls are allowed
    show("nulable?.length", nulable?.length)
    show("nulable?.length ?: 0", nulable?.length ?: 0)

    bullet("El compilador no te deja olvidarte del caso null. Ésa es toda la idea.")

    section("Relación entre los dos tipos")

    // String es SUBTIPO de String?: donde se acepta un nulable, cabe un no nulable.
    val aceptaNulable: String? = seguro      // ✔ de String a String?
    // val alReves: String = nulable         // ✘ de String? a String, no
    show("String cabe en String?", aceptaNulable)

    bullet("`T` es subtipo de `T?`. Por eso puedes pasar un valor seguro donde piden uno nulable.")
    bullet("Al revés hace falta comprobar: `?.`, `?:`, `!!` o un `if`.")

    section("También en genéricos")

    val listaDeNulables: List<String?> = listOf("a", null, "b")
    val listaNulable: List<String>? = null

    show("List<String?>  (lista que contiene nulos)", listaDeNulables)
    show("List<String>?  (la lista misma puede ser null)", listaNulable)
    show("filtrar los nulos con filterNotNull()", listaDeNulables.filterNotNull())

    bullet("`List<String?>` y `List<String>?` son cosas muy distintas. Lee bien dónde va el `?`.")
}

/**
 * Comprobar con un `if`: la forma más básica.
 */
fun demoNullCheck() {
    section("El if de toda la vida")

    val textos = listOf<String?>("Kotlin", null)

    textos.forEach { texto ->
        // Tras comprobar `!= null`, el compilador SABE que dentro del if no es null:
        // eso es el smart cast (demo 6.7). Se puede usar `.length` sin `?`.
        val longitud = if (texto != null) texto.length else 0
        show("if (texto != null) texto.length else 0   [$texto]", longitud)
    }

    section("Guarda al principio de la función")

    show("procesar(\"Kotlin\")", procesar("Kotlin"))
    show("procesar(null)", procesar(null))

    section("Comparar con null no necesita ?.")

    val nulo: String? = null
    show("nulo == null", nulo == null)
    show("nulo != null", nulo != null)
    bullet("`==` con null es seguro siempre: nunca lanza NPE (capítulo 3.6).")
}

/**
 * Kotlin frente a Optional de Java.
 */
fun demoVsOptional() {
    section("Java, antes y después de Optional")

    bullet("Java 7:  String s = buscar();  if (s != null) { ... }   ← nada obliga a comprobar")
    bullet("Java 8+: Optional<String> s = buscar();  s.map(...).orElse(...)")
    bullet("Kotlin:  val s: String? = buscar();  s?.let { ... } ?: ...")

    section("Diferencias prácticas")

    bullet("Optional es un OBJETO envolvente: ocupa memoria y se puede olvidar de abrir.")
    bullet("`String?` no envuelve nada: en el bytecode sigue siendo un String.")
    bullet("Optional permite el absurdo `Optional<Optional<String>>`; `String??` no existe.")
    bullet("Optional se puede quedar a null él mismo. `String?` no tiene ese doble nivel.")

    section("La equivalencia de las operaciones")

    val valor: String? = "Kotlin"
    val vacio: String? = null

    show("optional.map{}      →  ?.let{}", valor?.let { it.uppercase() })
    show("optional.orElse(x)  →  ?: x", vacio ?: "por defecto")
    show("optional.isPresent  →  != null", valor != null)
    show("optional.filter{}   →  ?.takeIf{}", valor?.takeIf { it.length > 3 })
    show("filter que no pasa", valor?.takeIf { it.length > 100 })
}

/**
 * Dónde SÍ puede haber NullPointerException en Kotlin.
 */
fun demoWhereNpeStillHappens() {
    section("Kotlin no promete cero NPE: promete que los ves venir")

    val nulo: String? = null

    // 1) El operador !!, que es justamente pedirlo.
    val porBang = try {
        nulo!!.length.toString()
    } catch (e: NullPointerException) {
        "NPE (por el operador !!)"
    }
    show("nulo!!", porBang)

    // 2) `lateinit` usado antes de inicializar (demo 6.9).
    bullet("lateinit sin inicializar → UninitializedPropertyAccessException")

    // 3) Tipos que vienen de Java sin anotar: los "platform types" (demo 6.10).
    bullet("Un método Java que devuelve null donde Kotlin creía que no → NPE")

    // 4) Pasar null desde Java a una función Kotlin que declara parámetro no nulable.
    bullet("Java llamando a una función Kotlin con null → IllegalArgumentException")

    section("La lista completa")

    bullet("`!!` explícito")
    bullet("`lateinit` accedido demasiado pronto")
    bullet("Interoperabilidad con Java sin anotaciones de nulabilidad")
    bullet("Inicialización en un constructor que llama a un método `open` sobrescrito")

    bullet("Todas son situaciones que TÚ introduces. El código Kotlin puro no las tiene.")
}

/** Guarda al principio: si no hay valor, salimos ya. */
private fun procesar(texto: String?): String {
    if (texto == null) return "(nada que procesar)"
    // A partir de aquí el compilador trata `texto` como String, no String?.
    return texto.uppercase().reversed()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Descomenta `val roto: String = null` y lee el error exacto.
//  2. Escribe `nulable.length` sin el `?` y lee el mensaje que propone las alternativas.
//  3. Declara `val x: List<String?>?` y razona cuántos niveles de null tiene.
//  4. Cambia `procesar` para que devuelva `String?` y observa cómo se propaga el `?`.
