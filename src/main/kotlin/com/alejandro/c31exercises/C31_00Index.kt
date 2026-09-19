package com.alejandro.c31exercises

import com.alejandro.infra.chapter

/**
 * # Capítulo 31 · Ejercicios
 *
 * Once ejercicios que juntan lo de los treinta capítulos anteriores. Todos siguen la
 * misma estructura:
 *
 * ```
 * ENUNCIADO   → qué hay que construir y con qué reglas
 * PISTAS      → por dónde empezar, sin destripar la solución
 * SOLUCIÓN    → el código propuesto, ejecutándose de verdad
 * EXPLICACIÓN → por qué está hecho así y qué decisiones hay detrás
 * VARIANTE    → cómo seguir cuando el ejercicio ya te sale
 * ```
 *
 * ## Cómo usarlos (importante)
 * Lee **sólo el enunciado**, cierra el fichero y escribe tu versión en un *scratch
 * file* del IDE (`Ctrl+Alt+Shift+Insert` en IntelliJ). Sólo después compara con la
 * solución propuesta. Leer una solución no enseña casi nada; escribir la tuya,
 * compararla y entender las diferencias, muchísimo.
 *
 * Si te atascas, tira de las pistas antes que de la solución.
 *
 * ## El mapa
 * ```
 * 🟢 fácil        1. Calculadora simple          when, sealed, parseo
 *                 2. Validador de emails         String, buildList, colecciones
 *                 3. Conversor de temperaturas   enum, operadores, Comparable
 *
 * 🟡 medio        4. Sistema de usuarios         data class, copy, encapsulación
 *                 5. Carrito de compra           dinero en enteros, sealed, reglas
 *                 6. Parser de comandos          troceado, sealed, errores como datos
 *                 7. Máquina de estados          sealed con datos, when anidado
 *                 8. Operaciones con colecciones groupBy, fold, secuencias
 *
 * 🔴 difícil      9. Mini DSL de informes        lambdas con receptor, @DslMarker
 *                10. Descargas con corrutinas    async, supervisorScope, timeouts
 *                11. Flow de eventos             operadores, StateFlow, SharedFlow
 * ```
 *
 * ## Las soluciones están probadas
 * Cada ejercicio tiene su test en `src/test/kotlin/com/alejandro/c31exercises/`. No
 * son adorno: si rompes una solución al experimentar, `./gradlew test` te lo dice.
 * Y son, además, el mejor sitio para ver qué casos límite conviene comprobar.
 *
 * ```
 * ./gradlew test --tests '*CarritoTest'
 * ```
 *
 * ## Si quieres más
 * Las variantes difíciles de cada ejercicio dan para semanas. Y si te quedas corto,
 * los bloques `// PARA EXPERIMENTAR` del final de cada fichero de los capítulos 1 a
 * 30 son otras cien ideas.
 */
val chapter31 = chapter(
    number = 31,
    name = "Ejercicios",
    summary = "Once ejercicios con enunciado, pistas, solución explicada y variante difícil",
) {
    demo("🟢 Calculadora simple", ::ejercicio01Calculadora)
    demo("🟢 Validador de emails", ::ejercicio02ValidadorEmails)
    demo("🟢 Conversor de temperaturas", ::ejercicio03Temperaturas)
    demo("🟡 Sistema de usuarios", ::ejercicio04SistemaUsuarios)
    demo("🟡 Carrito de compra", ::ejercicio05CarritoDeCompra)
    demo("🟡 Parser de comandos", ::ejercicio06ParserComandos)
    demo("🟡 Máquina de estados de un pedido", ::ejercicio07MaquinaDeEstados)
    demo("🟡 Operaciones con colecciones", ::ejercicio08Colecciones)
    demo("🔴 Mini DSL de informes", ::ejercicio09MiniDsl)
    demo("🔴 Descargas con corrutinas", ::ejercicio10Descargas)
    demo("🔴 Flow de eventos", ::ejercicio11FlowDeEventos)
}

/** Ejecuta los once ejercicios. */
fun main() = chapter31.runAll()
