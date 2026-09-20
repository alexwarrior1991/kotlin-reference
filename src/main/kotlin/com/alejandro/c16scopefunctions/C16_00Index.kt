package com.alejandro.c16scopefunctions

import com.alejandro.infra.chapter

/**
 * # Capítulo 16 · Scope functions
 *
 * `let`, `run`, `with`, `apply` y `also`. Cinco funciones que hacen casi lo mismo y
 * que se diferencian en sólo dos cosas. Memorizar esa tabla convierte la elección en
 * algo mecánico.
 *
 * ## La tabla
 * ```
 * función   objeto   devuelve         se llama como
 * let       it       el bloque        obj.let { }
 * run       this     el bloque        obj.run { }
 * with      this     el bloque        with(obj) { }
 * apply     this     EL OBJETO        obj.apply { }
 * also      it       EL OBJETO        obj.also { }
 * ```
 *
 * ## Lo que hay que llevarse sí o sí
 * - Las que devuelven el objeto son `apply` (this) y `also` (it). Las demás devuelven
 *   el valor del bloque.
 * - `with` **no es función de extensión**, así que no existe `?.with`: para nulos hay
 *   que usar `?.let` o `?.run`.
 * - `apply` para **configurar**, `also` para **efectos secundarios** (trazas,
 *   validaciones), `let` para **transformar o proteger de nulos**, `run`/`with` para
 *   **calcular** a partir de varias propiedades.
 * - Las tres preguntas que deciden: ¿quiero recuperar el objeto? ¿hay nulos?
 *   ¿prefiero `it` o `this`?
 *
 * ## Cuándo NO usarlas
 * - `?.let` sobre algo que no es nulable: sólo añade ruido.
 * - `?.let { } ?: ...` donde un `if (x != null) ... else ...` se lee mejor gracias al
 *   smart cast.
 * - Tres anidadas con tres `it` distintos. Si tienes que contar los `it`, parte la
 *   cadena y pon nombres.
 *
 * Siguiente paso: capítulo 17, extensiones.
 */
val chapter16 = chapter(
    number = 16,
    name = "Scope functions",
    summary = "let, run, with, apply, also: cuál elegir y cuándo no usar ninguna",
) {
    demo("La tabla que hay que memorizar", ::demoTheTable)
    demo("Las cinco sobre el mismo objeto", ::demoSideBySide)
    demo("Cuándo usar cada una", ::demoWhenToUseEach)
    demo("Casos en los que son intercambiables", ::demoEquivalences)
    demo("Los idiomas que sí merecen la pena", ::demoGoodIdioms)
    demo("Cuándo NO usar una scope function", ::demoWhenNotToUse)
    demo("Encadenar con cabeza", ::demoChaining)
}

/** Ejecuta el capítulo 16 completo. */
fun main() = chapter16.runAll()
