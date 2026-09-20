package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.1 · Crear colecciones: List, Set y Map
//
//  QUÉ ES
//    Los tres tipos de colección de Kotlin, cada uno en dos variantes: de sólo lectura
//    (`List`) y mutable (`MutableList`).
//
//  POR QUÉ IMPORTA
//    La distinción sólo-lectura / mutable es una de las mejores decisiones de diseño de
//    Kotlin, y también una de las peor entendidas: **sólo lectura no significa
//    inmutable**. Entender ese matiz evita bugs muy difíciles de encontrar.
//
//  ERRORES COMUNES
//    · Creer que `listOf` devuelve algo inmutable de verdad.
//    · Declarar `MutableList` cuando nadie va a modificarla.
//    · Usar `List` donde hacía falta un `Set` (y buscar con `contains` en bucle).
// =====================================================================================

/**
 * Los tres tipos y sus dos variantes.
 */
fun demoCollectionTypes() {
    section("List: ordenada, admite duplicados")

    val lista = listOf("a", "b", "a", "c")
    show("listOf(\"a\", \"b\", \"a\", \"c\")", lista)
    show("size", lista.size)
    show("lista[0]  (acceso por índice)", lista[0])
    show("mantiene el orden y los duplicados", lista.count { it == "a" })

    section("Set: sin duplicados, sin índice")

    val conjunto = setOf("a", "b", "a", "c")
    show("setOf(\"a\", \"b\", \"a\", \"c\")", conjunto)
    show("size  (la 'a' repetida se descartó)", conjunto.size)
    show("\"b\" in conjunto  (búsqueda O(1))", "b" in conjunto)

    bullet("Un Set no tiene `set[0]`: no hay índices, sólo pertenencia.")
    bullet("`setOf` conserva el orden de inserción (usa LinkedHashSet por dentro).")

    section("Map: pares clave → valor")

    val mapa = mapOf("uno" to 1, "dos" to 2)
    show("mapOf(\"uno\" to 1, \"dos\" to 2)", mapa)
    show("mapa[\"uno\"]", mapa["uno"])
    show("mapa[\"tres\"]  (no existe)", mapa["tres"])
    show("mapa.keys", mapa.keys)
    show("mapa.values", mapa.values)

    bullet("`to` es una función infija que crea un Pair. No es sintaxis especial.")

    section("Cuál elegir")

    bullet("¿Importa el orden o hay duplicados? → List")
    bullet("¿Sólo importa la pertenencia y no quieres repetidos? → Set")
    bullet("¿Buscas por una clave? → Map")
    bullet("Buscar en una List es O(n); en un Set o un Map, O(1). Con miles de")
    bullet("elementos dentro de un bucle, la diferencia es enorme.")
}

/**
 * Formas de crear cada colección.
 */
fun demoCreation() {
    section("Las funciones básicas")

    show("listOf(1, 2, 3)", listOf(1, 2, 3))
    show("mutableListOf(1, 2)", mutableListOf(1, 2))
    show("setOf(1, 2, 2)", setOf(1, 2, 2))
    show("mutableSetOf(1, 2)", mutableSetOf(1, 2))
    show("mapOf(1 to \"a\")", mapOf(1 to "a"))
    show("mutableMapOf(1 to \"a\")", mutableMapOf(1 to "a"))

    section("Vacías")

    show("emptyList<Int>()", emptyList<Int>())
    show("listOf<Int>()", listOf<Int>())
    show("emptySet<String>()", emptySet<String>())
    show("emptyMap<String, Int>()", emptyMap<String, Int>())

    bullet("`emptyList()` devuelve siempre el MISMO objeto compartido: cero coste.")
    bullet("Por eso devolver una lista vacía es mejor que devolver null (capítulo 6).")

    section("Sin nulos")

    // Muy útil al construir una lista donde algunos elementos pueden faltar.
    show("listOfNotNull(1, null, 3)", listOfNotNull(1, null, 3))

    section("Generadas")

    show("List(5) { it }", List(5) { it })
    show("List(5) { it * it }", List(5) { it * it })
    show("List(3) { \"x\" }", List(3) { "x" })

    section("Desde otra cosa")

    show("(1..5).toList()", (1..5).toList())
    show("\"hola\".toList()", "hola".toList())
    show("\"a,b,c\".split(\",\")", "a,b,c".split(","))
    show("listOf(1,2,2,3).toSet()", listOf(1, 2, 2, 3).toSet())
    show("listOf(\"a\" to 1).toMap()", listOf("a" to 1).toMap())

    section("Los builders: buildList, buildSet, buildMap")

    // Construyen una colección mutable por dentro y devuelven una de sólo lectura.
    // Es la forma idiomática cuando hay lógica de por medio.
    val incluirPie = listOf(1, 2, 3).isNotEmpty()
    val construida = buildList {
        add("cabecera")
        for (i in 1..3) add("línea $i")
        if (incluirPie) add("pie")
    }
    show("buildList { ... }", construida)

    val mapaConstruido = buildMap {
        put("a", 1)
        putAll(mapOf("b" to 2))
        if (size < 5) put("c", 3)
    }
    show("buildMap { ... }", mapaConstruido)

    bullet("Dentro del bloque tienes la versión MUTABLE; fuera recibes la de sólo lectura.")
    bullet("Es mejor que crear un `mutableListOf` y devolverlo: el tipo lo deja claro.")
}

/**
 * El matiz más importante: sólo lectura ≠ inmutable.
 */
fun demoReadOnlyIsNotImmutable() {
    section("Lo que SÍ garantiza `List`")

    val soloLectura: List<String> = listOf("a", "b")
    // soloLectura.add("c")    // ERROR: Unresolved reference 'add'
    show("List<String> no tiene add()", soloLectura)
    bullet("A través de ESA referencia no se puede modificar. Eso es todo.")

    section("Lo que NO garantiza")

    // La misma colección puede tener otra referencia que SÍ sea mutable.
    val mutable = mutableListOf("a", "b")
    val vista: List<String> = mutable        // misma colección, referencia de sólo lectura

    show("vista antes", vista)
    mutable.add("c")                          // se modifica por la otra referencia
    show("vista después (¡cambió!)", vista)

    bullet("`vista` no puede modificar, pero sí puede VER las modificaciones de otro.")
    bullet("En Java esto es aún peor: Collections.unmodifiableList tiene el mismo problema.")

    section("Cuándo importa de verdad")

    bullet("Al devolver una colección desde una clase: si expones la interna como")
    bullet("List, el que la reciba verá los cambios futuros. Puede ser lo que quieres...")
    bullet("...o una fuga de encapsulación.")

    val repositorio = RepositorioInseguro()
    val expuesta = repositorio.elementos()
    repositorio.agregar("nuevo")
    show("lista expuesta tras agregar", expuesta)

    section("La solución: copia defensiva")

    val seguro = RepositorioSeguro()
    val copia = seguro.elementos()
    seguro.agregar("nuevo")
    show("copia defensiva tras agregar", copia)
    show("y el repositorio sí tiene el nuevo", seguro.elementos())

    bullet("`toList()` crea una copia de verdad. Cuesta memoria, pero aísla.")
    bullet("Regla: copia al SALIR de la clase, no al entrar.")
}

/**
 * Convertir entre tipos.
 */
fun demoConversions() {
    section("De sólo lectura a mutable, y al revés")

    val lista = listOf(3, 1, 2)
    val mutable = lista.toMutableList()
    mutable.add(4)
    mutable.sort()

    show("original", lista)
    show("toMutableList() modificada", mutable)
    show("el original no cambió", lista)

    bullet("`toMutableList()` COPIA: son dos colecciones independientes.")
    bullet("Compara con el `as MutableList` que a veces se ve por ahí: eso NO copia")
    bullet("y rompe la garantía de sólo lectura. No lo hagas.")

    section("Entre tipos de colección")

    val conDuplicados = listOf("b", "a", "b", "c")
    show("toSet()  (quita duplicados)", conDuplicados.toSet())
    show("toSet().toList()  (deduplicar manteniendo orden)", conDuplicados.toSet().toList())
    show("distinct()  (lo mismo, más directo)", conDuplicados.distinct())
    show("sorted().toSet()", conDuplicados.sorted().toSet())

    section("Colecciones ↔ Map")

    val pares = listOf("a" to 1, "b" to 2)
    show("toMap()", pares.toMap())
    show("mapa.toList()", mapOf("a" to 1).toList())
    show("associateWith", listOf("a", "bb").associateWith { it.length })
    show("associateBy", listOf("ana", "luis").associateBy { it.first() })

    section("Colecciones ↔ Array")

    show("toTypedArray()", listOf(1, 2, 3).toTypedArray())
    show("toIntArray()", listOf(1, 2, 3).toIntArray())
    show("arrayOf(1,2,3).toList()", arrayOf(1, 2, 3).toList())
}

// -- Clases que usan las demos ----------------------------------------------------------

/** Expone su lista interna: quien la reciba verá los cambios futuros. */
private class RepositorioInseguro {
    private val interna = mutableListOf("inicial")
    fun agregar(elemento: String) {
        interna.add(elemento)
    }

    /** Devuelve la MISMA colección, sólo que con el tipo de sólo lectura. */
    fun elementos(): List<String> = interna
}

/** Devuelve una copia: el interior queda aislado del exterior. */
private class RepositorioSeguro {
    private val interna = mutableListOf("inicial")
    fun agregar(elemento: String) {
        interna.add(elemento)
    }

    fun elementos(): List<String> = interna.toList()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `RepositorioInseguro.elementos()` para que devuelva `interna.toList()`
//     y comprueba cómo cambia la salida.
//  2. Intenta `(soloLectura as MutableList).add("x")` y piensa por qué es mala idea.
//  3. Sustituye un `mutableListOf` + bucle por un `buildList` y compara la legibilidad.
//  4. Mide (mentalmente) el coste de buscar en una List de 100.000 frente a un Set.
