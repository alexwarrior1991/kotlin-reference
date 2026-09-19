package com.alejandro.c21operatoroverloading

import com.alejandro.infra.bullet
import com.alejandro.infra.section
import com.alejandro.infra.show

// =====================================================================================
//  21.3 · get, set, invoke, iterator y componentN: el caso Matriz
//
//  QUÉ ES
//    Los operadores que hacen que un objeto se comporte como una colección
//    (`m[1, 2]`), como una función (`objeto(x)`) o como algo recorrible
//    (`for (x in objeto)`).
//
//  POR QUÉ IMPORTA
//    `get` y `set` con varios índices permiten `matriz[fila, columna]`, que es la
//    notación de toda la vida. Y `invoke` es lo que hace que un objeto se pueda usar
//    donde se espera una función.
//
//  ERRORES COMUNES
//    · Usar `invoke` en clases donde no hay una "acción principal" evidente.
//    · Implementar `iterator` sin que el orden de recorrido sea obvio.
//    · Sobrecargar operadores porque se puede, no porque aclare algo.
// =====================================================================================

/**
 * `get` y `set`.
 */
fun demoGetAndSet() {
    section("Un índice")

    val cesta = CestaIndexada(mutableListOf("pan", "leche"))
    show("cesta[0]", cesta[0])
    show("cesta[1]", cesta[1])

    cesta[1] = "leche entera"
    show("tras cesta[1] = ...", cesta[1])

    bullet("`c[i]` → `c.get(i)`; `c[i] = v` → `c.set(i, v)`.")

    section("Varios índices: la gracia de verdad")

    val matriz = Matriz(2, 3)
    matriz[0, 0] = 1.0
    matriz[0, 1] = 2.0
    matriz[0, 2] = 3.0
    matriz[1, 0] = 4.0
    matriz[1, 1] = 5.0
    matriz[1, 2] = 6.0

    println()
    println(matriz)

    show("matriz[1, 2]", matriz[1, 2])
    show("matriz[0, 0]", matriz[0, 0])

    bullet("`m[f, c]` → `m.get(f, c)`. Java no tiene nada parecido.")
    bullet("Sin esto habría que escribir `m.get(1, 2)` o `m.datos[1 * cols + 2]`.")

    section("Índices de cualquier tipo")

    // `get` no obliga a que el índice sea un Int.
    val registro = RegistroPorClave()
    registro["host"] = "localhost"
    registro["puerto"] = "8080"

    show("registro[\"host\"]", registro["host"])
    show("registro[\"falta\"]", registro["falta"])

    section("Comprobar los límites")

    val fuera = try {
        matriz[5, 5].toString()
    } catch (e: IndexOutOfBoundsException) {
        "lanzó IndexOutOfBoundsException"
    }
    show("matriz[5, 5]", fuera)
    bullet("Comprueba los índices tú: un `get` que devuelve basura es peor que uno")
    bullet("que lanza.")
}

/**
 * `invoke`: el objeto que se llama como una función.
 */
fun demoInvoke() {
    section("Lo básico")

    val duplicar = Transformacion("duplicar") { it * 2 }

    // Como tiene `invoke`, se puede llamar directamente.
    show("duplicar(21)", duplicar(21))
    show("duplicar.invoke(21)  (lo mismo)", duplicar.invoke(21))
    show("duplicar.nombre", duplicar.nombre)

    bullet("`obj(x)` → `obj.invoke(x)`. Es lo que hace que las lambdas se llamen así.")

    section("Para qué sirve de verdad")

    // Un objeto con estado que además se puede usar como función: lo mejor de los
    // dos mundos. Una lambda no puede tener nombre ni propiedades.
    val contador = ContadorDeLlamadas()
    contador("a")
    contador("b")
    contador("a")

    show("veces llamado", contador.total)
    show("argumentos vistos", contador.argumentos)

    bullet("Una lambda no puede llevar contadores dentro y exponerlos.")
    bullet("Un objeto con `invoke` sí, y se sigue usando como si fuera una función.")

    section("Se puede pasar donde se espera una función")

    val numeros = listOf(1, 2, 3)
    show("map(duplicar::invoke)", numeros.map(duplicar::invoke))
    bullet("Con la referencia `::invoke` encaja en cualquier `(Int) -> Int`.")

    section("El uso más frecuente: companion object invocable")

    // Permite `Configuracion { ... }` en lugar de `Configuracion.crear { ... }`.
    val configuracion = Configuracion {
        put("modo", "rápido")
        put("reintentos", "3")
    }
    show("Configuracion { ... }", configuracion)
    bullet("Es el truco que usan muchos DSL para que la construcción parezca sintaxis.")

    section("Cuándo NO usar invoke")

    bullet("Si la clase hace varias cosas, ¿cuál sería `invoke`? Ninguna: pon nombres.")
    bullet("`usuario(5)` no dice nada; `usuario.buscarPedidos(5)` sí.")
    bullet("Regla: sólo si la clase tiene UNA acción principal evidente.")
}

/**
 * `iterator`: recorrer con `for`.
 */
fun demoIterator() {
    section("Habilitar el for")

    val semana = Semana(listOf("lunes", "martes", "miércoles"))

    val recogidos = buildList {
        for (dia in semana) add(dia)
    }
    show("for (dia in semana)", recogidos)

    bullet("`for (x in obj)` → `obj.iterator()`. No hace falta implementar Iterable.")

    section("También como extensión sobre un tipo ajeno")

    // Se puede añadir `iterator` a un tipo que no controlas.
    val matriz = Matriz(2, 2)
    matriz[0, 0] = 1.0
    matriz[0, 1] = 2.0
    matriz[1, 0] = 3.0
    matriz[1, 1] = 4.0

    val valores = buildList {
        for (valor in matriz) add(valor)
    }
    show("recorrer la matriz (por filas)", valores)

    bullet("El orden de recorrido debe ser OBVIO. Por filas en una matriz lo es;")
    bullet("en un árbol no, así que ahí mejor `recorridoEnOrden()` con nombre.")

    section("Iterable frente a iterator")

    bullet("`operator fun iterator()` → sólo habilita el `for`.")
    bullet("`: Iterable<T>` → además da map, filter, sum y las 200 extensiones.")
    bullet("Si tu tipo es una colección de verdad, implementa Iterable.")
}

/**
 * `componentN`: desestructuración.
 */
fun demoComponentN() {
    section("Lo que hace una data class por ti")

    val punto = PuntoManual(3, 4)
    val (x, y) = punto
    show("val (x, y) = PuntoManual(3, 4)", "x=$x, y=$y")

    bullet("`val (a, b) = obj` → `obj.component1()` y `obj.component2()`.")
    bullet("Una `data class` los genera; aquí están escritos a mano.")

    section("Se puede añadir a un tipo ajeno con extensiones")

    // `Matriz` no es nuestra data class, pero podemos darle desestructuración.
    val dimensiones = Matriz(3, 5)
    val (filas, columnas) = dimensiones
    show("val (filas, columnas) = matriz", "filas=$filas, columnas=$columnas")

    section("Y funciona en bucles y lambdas")

    val puntos = listOf(PuntoManual(1, 2), PuntoManual(3, 4))
    val sumas = buildList {
        for ((a, b) in puntos) add(a + b)
    }
    show("for ((a, b) in puntos)", sumas)
    show("en una lambda", puntos.map { (a, b) -> a * b })

    bullet("Se ve a fondo en el capítulo 22.")
}

/**
 * La regla de oro.
 */
fun demoTheRule() {
    section("Cuándo sobrecargar un operador")

    bullet("1. Existe una notación estándar para esa operación (matemática, conjuntos).")
    bullet("2. El significado es OBVIO sin leer la documentación.")
    bullet("3. Cumple las expectativas: `+` no debería borrar nada, `==` no debería")
    bullet("   tener efectos secundarios, `a + b` debería dar lo mismo que `b + a`")
    bullet("   si la operación es conmutativa en el dominio.")

    section("Ejemplos claramente buenos")

    bullet("Vector + Vector · Matriz * Matriz · Dinero + Dinero")
    bullet("Duración + Duración · Fecha + Duración")
    bullet("Conjunto + Elemento · Ruta / Segmento (como en java.nio.Path)")

    section("Ejemplos claramente malos")

    bullet("usuario + permiso   → ¿le añade uno? ¿crea otro usuario? ¿lo guarda?")
    bullet("lista * 2           → ¿duplica los elementos? ¿repite la lista?")
    bullet("conexion()          → invoke que abre una conexión: no es obvio")
    bullet("pedido[3]           → si no es una colección, no lleva corchetes")

    section("La prueba definitiva")

    bullet("Enséñale la expresión a alguien que no conozca tu clase.")
    bullet("¿Adivina qué hace? → adelante.")
    bullet("¿Tiene que preguntar? → función con nombre.")

    section("La tabla completa")

    bullet("a + b        plus            a[i]        get")
    bullet("a - b        minus           a[i] = v    set")
    bullet("a * b        times           a(x)        invoke")
    bullet("a / b        div             a in b      b.contains(a)")
    bullet("a % b        rem             a..b        rangeTo")
    bullet("+a / -a      unaryPlus/Minus a..<b       rangeUntil")
    bullet("!a           not             for (x in a) iterator")
    bullet("a++ / a--    inc / dec       a == b      equals")
    bullet("a += b       plusAssign      a < b       compareTo")
    bullet("val (x,y)=a  component1/2")
}

// -- Los tipos que usan las demos ------------------------------------------------------------

private class CestaIndexada(private val productos: MutableList<String>) {
    operator fun get(indice: Int): String = productos[indice]
    operator fun set(indice: Int, valor: String) {
        productos[indice] = valor
    }
}

/**
 * Matriz con `get`/`set` de dos índices, `iterator` y desestructuración.
 */
private class Matriz(val filas: Int, val columnas: Int) {

    private val datos = DoubleArray(filas * columnas)

    operator fun get(fila: Int, columna: Int): Double {
        comprobar(fila, columna)
        return datos[fila * columnas + columna]
    }

    operator fun set(fila: Int, columna: Int, valor: Double) {
        comprobar(fila, columna)
        datos[fila * columnas + columna] = valor
    }

    /** Recorre por filas, de izquierda a derecha: el orden obvio. */
    operator fun iterator(): Iterator<Double> = datos.iterator()

    private fun comprobar(fila: Int, columna: Int) {
        if (fila !in 0..<filas || columna !in 0..<columnas) {
            throw IndexOutOfBoundsException("($fila, $columna) fuera de ${filas}x$columnas")
        }
    }

    override fun toString(): String = (0..<filas).joinToString("\n") { fila ->
        "      [" + (0..<columnas).joinToString(", ") { "%.1f".format(this[fila, it]) } + "]"
    }
}

/** Desestructuración añadida desde fuera con extensiones. */
private operator fun Matriz.component1(): Int = filas
private operator fun Matriz.component2(): Int = columnas

private class RegistroPorClave {
    private val datos = mutableMapOf<String, String>()
    operator fun get(clave: String): String = datos[clave] ?: "(no definido)"
    operator fun set(clave: String, valor: String) {
        datos[clave] = valor
    }
}

/** Un objeto que se llama como una función y además tiene nombre. */
private class Transformacion(val nombre: String, private val funcion: (Int) -> Int) {
    operator fun invoke(valor: Int): Int = funcion(valor)
}

/** Lo que una lambda no puede hacer: llevar estado accesible desde fuera. */
private class ContadorDeLlamadas {
    var total: Int = 0
        private set

    private val vistos = mutableListOf<String>()
    val argumentos: List<String> get() = vistos.distinct()

    operator fun invoke(argumento: String) {
        total++
        vistos.add(argumento)
    }
}

/** Companion invocable: permite `Configuracion { ... }`. */
private class Configuracion private constructor(private val valores: Map<String, String>) {

    override fun toString(): String = valores.entries.joinToString { "${it.key}=${it.value}" }

    companion object {
        operator fun invoke(bloque: MutableMap<String, String>.() -> Unit): Configuracion =
            Configuracion(mutableMapOf<String, String>().apply(bloque).toMap())
    }
}

private class Semana(private val dias: List<String>) {
    operator fun iterator(): Iterator<String> = dias.iterator()
}

/** componentN escritos a mano: lo que genera una data class. */
private class PuntoManual(val x: Int, val y: Int) {
    operator fun component1(): Int = x
    operator fun component2(): Int = y
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `operator fun times(otra: Matriz): Matriz` con el producto matricial.
//  2. Haz que Semana implemente Iterable<String> y comprueba qué extensiones ganas.
//  3. Añade `component3()` a PuntoManual y desestructura con tres variables.
//  4. Escribe `operator fun Matriz.plus(otra: Matriz)` comprobando las dimensiones.
