package com.alejandro.c13collections

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  13.6 · Ordenar, buscar y buenas prácticas
//
//  QUÉ ES
//    Ordenación (`sorted`, `sortedBy`, `sortedWith`, comparadores compuestos),
//    búsqueda (`find`, `first`, `single`, `indexOf`) y el resumen de cómo exponer
//    colecciones sin pegarse un tiro en el pie.
//
//  POR QUÉ IMPORTA
//    Los comparadores compuestos (`thenBy`) resuelven de una línea el clásico "ordena
//    por departamento y, dentro, por salario descendente". Y la distinción entre
//    `first` y `firstOrNull` es la diferencia entre una excepción y un `null`.
//
//  ERRORES COMUNES
//    · Usar `sort()` (muta) cuando querías `sorted()` (copia), o al revés.
//    · Llamar a `first { }` sobre algo que puede no existir.
//    · Devolver `MutableList` desde una clase y perder el control de tu propio estado.
// =====================================================================================

private data class Persona(val nombre: String, val departamento: String, val salario: Int, val edad: Int)

private val equipo = listOf(
    Persona("Ana", "Ingeniería", 52_000, 34),
    Persona("Luis", "Ingeniería", 47_000, 28),
    Persona("Marta", "Diseño", 45_000, 41),
    Persona("Carlos", "Ventas", 38_000, 25),
    Persona("Sara", "Diseño", 49_000, 31),
    Persona("Diego", "Ingeniería", 52_000, 45),
)

/**
 * Ordenar.
 */
fun demoSorting() {
    section("sorted: para tipos Comparable")

    show("números", listOf(3, 1, 2).sorted())
    show("descendente", listOf(3, 1, 2).sortedDescending())
    show("textos (orden alfabético)", listOf("pera", "manzana", "uva").sorted())

    section("sortedBy: por una propiedad")

    show("por salario", equipo.sortedBy { it.salario }.map { "${it.nombre}:${it.salario}" })
    show("por salario desc", equipo.sortedByDescending { it.salario }.map { "${it.nombre}:${it.salario}" })
    show("por longitud del nombre", equipo.sortedBy { it.nombre.length }.map { it.nombre })

    section("sorted NO muta: devuelve una lista nueva")

    val original = mutableListOf(3, 1, 2)
    val ordenada = original.sorted()
    show("original tras sorted()", original)
    show("resultado de sorted()", ordenada)

    // `sort()` (sin la 'ed') sí muta, y sólo existe en MutableList.
    original.sort()
    show("original tras sort()", original)

    bullet("La regla en toda la biblioteca: participio (`sorted`, `reversed`) → copia.")
    bullet("Imperativo (`sort`, `reverse`, `shuffle`) → muta, y sólo en Mutable*.")

    section("reversed frente a sortedDescending")

    val numeros = listOf(3, 1, 2)
    show("reversed()", numeros.reversed())
    show("sortedDescending()", numeros.sortedDescending())
    bullet("`reversed` le da la vuelta al orden ACTUAL; no ordena nada.")
}

/**
 * Comparadores compuestos.
 */
fun demoComparators() {
    section("El problema")

    bullet("Ordenar por departamento y, dentro de cada uno, por salario descendente.")

    section("Con compareBy y thenBy")

    val ordenado = equipo.sortedWith(
        compareBy<Persona> { it.departamento }
            .thenByDescending { it.salario }
            .thenBy { it.nombre },
    )
    ordenado.forEach { show(it.departamento, "${it.nombre} · ${it.salario} €") }

    bullet("`thenBy` sólo se aplica cuando el criterio anterior empata.")
    bullet("Ana y Diego cobran lo mismo: los desempata el nombre.")

    section("compareBy con varios criterios de golpe")

    // Forma corta cuando todos son ascendentes.
    val corto = equipo.sortedWith(compareBy({ it.departamento }, { it.nombre }))
    show("compareBy(a, b)", corto.map { "${it.departamento}/${it.nombre}" })

    section("Comparadores reutilizables")

    show("por antigüedad (edad desc)", equipo.sortedWith(PorEdadDescendente).map { "${it.nombre}:${it.edad}" })

    bullet("Un Comparator se puede guardar en un `val`, pasar como parámetro y")
    bullet("reutilizar. Es el ejemplo típico de `object` (capítulo 7.13).")

    section("Invertir y encadenar un comparador existente")

    val porNombre = compareBy<Persona> { it.nombre }
    show("normal", equipo.sortedWith(porNombre).map { it.nombre })
    show("reversed()", equipo.sortedWith(porNombre.reversed()).map { it.nombre })

    section("Nulos al ordenar")

    val conNulos = listOf("b", null, "a", null, "c")
    show("nullsFirst()", conNulos.sortedWith(nullsFirst(naturalOrder<String>())))
    show("nullsLast()", conNulos.sortedWith(nullsLast(naturalOrder<String>())))
    bullet("Sin esto, ordenar una lista con nulos lanzaría NullPointerException.")
}

/**
 * Buscar elementos.
 */
fun demoSearching() {
    section("find / firstOrNull: devuelven null si no hay")

    show("find { salario > 50_000 }", equipo.find { it.salario > 50_000 }?.nombre)
    show("find { salario > 99_000 }", equipo.find { it.salario > 99_000 }?.nombre)

    bullet("`find` y `firstOrNull` son EXACTAMENTE la misma función (alias).")

    section("first / last: lanzan si no hay")

    show("first { departamento == \"Diseño\" }", equipo.first { it.departamento == "Diseño" }.nombre)
    show("last { departamento == \"Diseño\" }", equipo.last { it.departamento == "Diseño" }.nombre)

    val falloFirst = try {
        equipo.first { it.salario > 99_000 }.nombre
    } catch (e: NoSuchElementException) {
        "lanzó NoSuchElementException"
    }
    show("first { salario > 99_000 }", falloFirst)

    bullet("Usa `first` sólo cuando el elemento DEBE existir: si no, es un bug y")
    bullet("la excepción es correcta. Si puede faltar, usa `firstOrNull`.")

    section("single: exige exactamente uno")

    show("single { nombre == \"Ana\" }", equipo.single { it.nombre == "Ana" }.nombre)

    val falloSingle = try {
        equipo.single { it.departamento == "Diseño" }.nombre
    } catch (e: IllegalArgumentException) {
        "lanzó IllegalArgumentException (hay más de uno)"
    }
    show("single { departamento == \"Diseño\" }", falloSingle)
    show("singleOrNull con dos resultados", equipo.singleOrNull { it.departamento == "Diseño" })

    bullet("`single` documenta una invariante: 'aquí sólo puede haber uno'.")
    bullet("Si hay cero o dos, lanza. `singleOrNull` devuelve null en ambos casos.")

    section("Posiciones")

    show("indexOfFirst { edad > 40 }", equipo.indexOfFirst { it.edad > 40 })
    show("indexOfLast { edad > 40 }", equipo.indexOfLast { it.edad > 40 })
    show("indexOf(elemento concreto)", equipo.indexOf(equipo[2]))
    show("no encontrado devuelve -1", equipo.indexOfFirst { it.edad > 99 })

    section("Comprobaciones rápidas")

    show("any { departamento == \"Ventas\" }", equipo.any { it.departamento == "Ventas" })
    show("none { salario < 20_000 }", equipo.none { it.salario < 20_000 })
    show("all { edad >= 18 }", equipo.all { it.edad >= 18 })
    show("count { departamento == \"Ingeniería\" }", equipo.count { it.departamento == "Ingeniería" })

    section("Acceso por índice seguro")

    show("equipo[0].nombre", equipo[0].nombre)
    show("getOrNull(99)", equipo.getOrNull(99))
    show("getOrElse(99) { ... }", equipo.getOrElse(99) { Persona("nadie", "-", 0, 0) }.nombre)
    show("firstOrNull() sobre lista vacía", emptyList<Persona>().firstOrNull())

    bullet("`lista[99]` lanza IndexOutOfBoundsException; `getOrNull(99)` devuelve null.")
}

/**
 * Buenas prácticas: cómo exponer colecciones.
 */
fun demoBestPractices() {
    section("1. Declara el tipo más restrictivo que sirva")

    bullet("Parámetro que sólo se recorre → `Iterable<T>` o `Collection<T>`")
    bullet("Parámetro que se indexa → `List<T>`")
    bullet("Parámetro que hay que modificar → `MutableList<T>` (y pregúntate por qué)")
    bullet("Retorno → `List<T>`, nunca `MutableList<T>` ni `ArrayList<T>`")

    section("2. Copia defensiva al salir de la clase")

    val cuenta = CuentaConMovimientos()
    cuenta.registrar("ingreso 100")
    val vista = cuenta.movimientos()
    cuenta.registrar("gasto 30")
    show("la vista NO vio el segundo movimiento", vista)
    show("la cuenta sí lo tiene", cuenta.movimientos())

    section("3. Prefiere transformar a mutar")

    // Mutar obliga a leer todo el bucle para saber qué acaba pasando.
    val mutando = mutableListOf<String>()
    for (p in equipo) if (p.salario > 46_000) mutando.add(p.nombre.uppercase())

    // Transformar lo dice en la propia expresión.
    val transformando = equipo.filter { it.salario > 46_000 }.map { it.nombre.uppercase() }

    show("mutando", mutando)
    show("transformando", transformando)

    section("4. Cuidado con encadenar sobre colecciones grandes")

    bullet("Cada `map`/`filter` crea una lista intermedia completa.")
    bullet("Con 5 operaciones sobre 1.000.000 de elementos, son 5 millones de objetos.")
    bullet("La solución es `asSequence()`: el capítulo 14, que viene justo ahora.")

    section("5. El orden de las operaciones importa")

    // Filtrar ANTES de transformar significa transformar menos elementos.
    bullet("lista.map { caro(it) }.filter { ... }  → transforma TODO y luego descarta")
    bullet("lista.filter { ... }.map { caro(it) }  → descarta y luego transforma poco")
    bullet("Mismo resultado, trabajo muy distinto. Filtra siempre lo antes posible.")
}

/** Un comparador reutilizable como `object`. */
private object PorEdadDescendente : Comparator<Persona> {
    override fun compare(a: Persona, b: Persona): Int = b.edad - a.edad
}

/** Copia defensiva: lo que sale no es lo que hay dentro. */
private class CuentaConMovimientos {
    private val interna = mutableListOf<String>()

    fun registrar(movimiento: String) {
        interna.add(movimiento)
    }

    fun movimientos(): List<String> = interna.toList()
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Ordena el equipo por departamento ascendente y edad descendente.
//  2. Cambia `first { }` por `firstOrNull { }` en la demo que lanza y compara.
//  3. Quita el `.toList()` de CuentaConMovimientos y vuelve a ejecutar la demo 13.36.
//  4. Invierte el orden de `filter` y `map` en la última demo y razona el coste.
