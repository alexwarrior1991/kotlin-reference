# kotlin-reference

Una referencia **práctica** de Kotlin: 411 ejemplos ejecutables, comentados en
español, ordenados de `println` a `Flow`.

No es un tutorial que se lee una vez ni una chuleta de sintaxis. Es un proyecto que se
**ejecuta, se lee y se rompe a propósito** para entender por qué el resultado cambia.

- **31 capítulos**, del 01 (sintaxis básica) al 31 (ejercicios).
- **411 demos** que se pueden lanzar una a una desde la línea de comandos o el IDE.
- **300 tests** que verifican que todo funciona, incluido un test de humo que ejecuta
  **todas** las demos en cada `./gradlew build`.
- Comentarios que explican **por qué**, no sólo qué: errores comunes, trampas,
  alternativas y cuándo NO usar cada cosa.

---

## Arranque en 60 segundos

```bash
git clone <la-url-de-este-repositorio>
cd kotlin-reference

./gradlew run -q --console=plain --args="list"    # el índice completo
./gradlew run -q --console=plain --args="1"       # el capítulo 1 entero
./gradlew run -q --console=plain                  # menú interactivo
```

La primera vez, Gradle descarga su distribución y las dependencias: tarda un par de
minutos. Las siguientes, segundos.

En Windows, sustituye `./gradlew` por `gradlew.bat`.

### Desde IntelliJ IDEA

1. `File → Open` y selecciona la carpeta del proyecto (Gradle lo importa solo).
2. Pulsa ▶ en el `main()` de `src/main/kotlin/com/alejandro/Main.kt` → menú interactivo.
3. O pulsa ▶ en el `main()` de cualquier `CNN_00Index.kt` → ese capítulo entero.

---

## Cómo ejecutar

El prefijo es siempre `./gradlew run -q --console=plain`:

| Qué quieres | Comando |
|---|---|
| Menú interactivo | `./gradlew run -q --console=plain` |
| Índice de capítulos y demos | `… --args="list"` |
| **Todas** las demos, con tiempos | `… --args="all"` |
| Un capítulo entero | `… --args="13"` |
| **Una sola demo** | `… --args="13.4"` |
| Buscar por título | `… --args="search flow"` |
| Ayuda | `… --args="help"` |

Ejecutar una demo suelta (`--args="28.19"`) es lo que más vas a usar: vas al ejemplo,
lo lees, lo tocas y lo vuelves a lanzar.

**Sin Gradle de por medio** (arranque instantáneo, útil si vas a iterar mucho):

```bash
./gradlew installDist
./build/install/kotlin-reference/bin/kotlin-reference 28.19
```

---

## El mapa de los 31 capítulos

🟢 básico · 🟡 hay matices · 🔴 conviene volver más de una vez

| # | Capítulo | Demos | | Qué te llevas |
|---|---|---|---|---|
| 01 | Sintaxis básica | 11 | 🟢 | `main`, `println`, expresión frente a sentencia, inferencia |
| 02 | Variables y tipos | 19 | 🟢 | `val`/`var`/`const`, desbordamiento, `String`, arrays, rangos |
| 03 | Operadores | 14 | 🟢 | `==` frente a `===`, la caché de `Int`, `?.`, `?:`, `in` |
| 04 | Control de flujo | 17 | 🟢 | `if` es una expresión, `when` en todas sus formas, etiquetas |
| 05 | Funciones | 21 | 🟢 | defaults, `infix`, `tailrec`, `vararg`, `inline`, receptores |
| 06 | Null safety | 14 | 🟡 | `?.`, `?:`, cuándo `!!`, límites del smart cast, `lateinit` |
| 07 | Clases y objetos | 17 | 🟢 | orden de inicialización, `companion`, `object`, `inner` |
| 08 | Herencia e interfaces | 10 | 🟢 | `final` por defecto, `super<A>`, composición frente a herencia |
| 09 | Data classes | 12 | 🟢 | qué genera y qué NO, `copy`, `data object`, `value class` |
| 10 | Enum classes | 9 | 🟢 | constantes con comportamiento, `entries`, por qué no `ordinal` |
| 11 | Sealed classes | 11 | 🟡 | jerarquías cerradas, `when` exhaustivo, estados y resultados |
| 12 | Genéricos | 15 | 🔴 | restricciones, varianza `in`/`out`, `reified`, borrado de tipos |
| 13 | Colecciones | 25 | 🟡 | read-only ≠ inmutable, `groupBy`, `fold`, copias defensivas |
| 14 | Secuencias | 8 | 🟡 | perezoso frente a ansioso, infinitas, cuándo NO usarlas |
| 15 | Lambdas y orden superior | 16 | 🟡 | `it`, cierres y sus trampas, `typealias`, composición |
| 16 | Scope functions | 7 | 🟢 | `let`/`run`/`with`/`apply`/`also`: la tabla que lo resuelve |
| 17 | Extensiones | 11 | 🟡 | **resolución estática**, receptor nulable, el miembro gana |
| 18 | Delegación | 18 | 🔴 | `by`, `lazy`, `observable`, delegados propios, `provideDelegate` |
| 19 | Excepciones | 11 | 🟢 | no hay checked, `Result`, error esperado frente a excepcional |
| 20 | Comprobaciones y casts | 7 | 🟢 | `is`, smart casts y cuándo el compilador no puede, `as?` |
| 21 | Sobrecarga de operadores | 11 | 🟡 | `Vector2D`, `Money`, `Matrix`, `invoke`, y los abusos |
| 22 | Desestructuración | 8 | 🟢 | `componentN`, mapas, lambdas y la trampa posicional |
| 23 | Anotaciones | 7 | 🟡 | targets `@get:`/`@field:`, retención RUNTIME por defecto |
| 24 | Reflexión | 7 | 🔴 | qué necesita `kotlin-reflect`, un mini-serializador, su coste |
| 25 | Biblioteca estándar | 15 | 🟢 | `require`/`check`, `use`, `takeIf`, `measureTime`, `Random` |
| 26 | Ficheros e IO | 9 | 🟡 | `use`, streaming, `Path`, un CSV completo (todo en temporales) |
| 27 | Interoperabilidad Java | 10 | 🟡 | platform types, SAM, `@JvmStatic`, `@JvmOverloads`, `@Throws` |
| 28 | **Corrutinas** | 43 | 🔴 | `suspend`, `launch`/`async`, cancelación, `Flow`, `StateFlow` |
| 29 | DSLs | 9 | 🔴 | lambdas con receptor, `@DslMarker`, DSL de HTML y de configuración |
| 30 | Testing | 8 | 🟡 | código testeable, `kotlin.test`, `runTest` y tiempo virtual |
| 31 | **Ejercicios** | 11 | 🟢🟡🔴 | enunciado, pistas, solución explicada y variante difícil |

Los capítulos 13 y 28 son a los que más se vuelve. Los 12, 18 y 24 son los que más
cuesta que "hagan clic".

---

## Cuatro rutas de estudio

Elige la tuya; ninguna necesita las otras.

### 🚀 Exprés — «necesito escribir Kotlin esta semana»
`01 → 02 → 04 → 05 → 06 → 09 → 13 → 16`

Ocho capítulos, unas tres horas. Con esto se escribe Kotlin correcto y legible: tipos,
control de flujo, funciones, nulabilidad, data classes, colecciones y scope functions.

### 📚 Completa — «quiero aprenderlo bien»
`01 → 31`, en orden.

Cada capítulo da por sabido el anterior. Calcula dos o tres capítulos por sesión, y
para el 28 reserva tres sesiones (está dividido en tres tandas dentro de su propio
índice).

### ☕ Vengo de Java
`06 → 09 → 07 → 08 → 11 → 13 → 16 → 17 → 18 → 27`

Empieza por lo que NO existe en Java y por lo que funciona distinto: nulabilidad en el
sistema de tipos, data classes, clases `final` por defecto, jerarquías selladas,
colecciones read-only, scope functions, extensiones, delegación. El 27 cierra el
círculo: cómo conviven los dos lenguajes en el mismo proyecto.

### 🛠️ Backend o Android
`11 → 13 → 15 → 18 → 19 → 28 → 29 → 30`

Modelado con sealed, colecciones, lambdas, delegación, manejo de errores, corrutinas y
Flow, DSLs (Ktor, Gradle, Compose son DSLs) y testing. El 28 es la mitad del camino.

---

## Cómo experimentar (esto es lo importante)

Leer estos ficheros enseña la mitad; **romperlos** enseña la otra mitad.

**La receta de tres pasos:**

1. Ejecuta una demo: `./gradlew run -q --console=plain --args="13.4"`.
2. Abre su fichero, cambia una línea, guarda.
3. Vuelve a ejecutar el mismo comando y compara la salida.

**Cosas que merece la pena romper a propósito:**

- Quita un `?.` y mira qué error da el compilador (y por qué es mejor que un NPE).
- Cambia `List` por `MutableList` y comprueba qué garantías desaparecen.
- Quita un `.toList()` de un `Sequence` y observa cuántas operaciones se ejecutan.
- Quita un `throw e` de un `catch (e: CancellationException)` y verás una corrutina
  que ignora su cancelación.
- Añade una variante a una `sealed class` y deja que el compilador te lleve de la mano
  hasta todos los `when` que hay que completar.

**Cada fichero de contenido termina con un bloque `// ── PARA EXPERIMENTAR ──`** con
dos a seis ideas concretas: 117 bloques y más de 400 sugerencias en total.

**Scratch files:** en IntelliJ, `Ctrl+Alt+Shift+Insert` abre un fichero de pruebas
Kotlin que se ejecuta solo, sin tocar el proyecto. Es el mejor sitio para escribir tu
versión de un ejercicio antes de mirar la solución.

---

## Los temas que exigen más práctica (y por qué)

| Tema | Por qué cuesta | Dónde está |
|---|---|---|
| **Nulabilidad** | No es sintaxis: es un cambio de mentalidad. Lo difícil no es usar `?.`, es diseñar para que no haya nulos. | 06 |
| **Secuencias** | El orden de ejecución no es el que parece: cada elemento recorre la cadena entera antes de que empiece el siguiente. | 14 |
| **Varianza (`in`/`out`)** | Las reglas son sencillas; saber cuál toca en tu caso, no. Hay ejemplos que NO compilan, con el error exacto comentado. | 12 |
| **Delegación** | `by` parece magia hasta que ves `provideDelegate`. Y la trampa de las llamadas internas sorprende a todo el mundo. | 18 |
| **Cancelación de corrutinas** | Es **cooperativa**: si no la compruebas, no ocurre. Y `runCatching` la rompe en silencio. | 28.13 – 28.16 |
| **Excepciones en corrutinas** | `launch` falla hacia arriba, `async` guarda el fallo hasta el `await`. Las reglas NO son las de un try/catch normal. | 28.25 – 28.28 |
| **Flow frío frente a caliente** | Un flujo frío se ejecuta entero por cada colector. Confundirlo con un `StateFlow` produce bugs sutiles. | 28.29 – 28.40 |
| **DSLs** | Escribir uno es fácil; escribir uno que no se pueda usar mal exige `@DslMarker` y una jerarquía de tipos pensada. | 29 |

---

## Estructura y convenciones

```
src/
├── main/
│   ├── kotlin/com/alejandro/
│   │   ├── Main.kt                     el lanzador: registra los 31 capítulos
│   │   ├── infra/                      Demo.kt, Chapter, Console.kt, Launcher.kt
│   │   ├── c01basics/
│   │   │   ├── C01_00Index.kt          el índice del capítulo + su main()
│   │   │   ├── C01_01HelloWorld.kt
│   │   │   └── …
│   │   ├── …
│   │   └── c31exercises/
│   └── java/com/alejandro/c27javainterop/legacy/    4 clases Java de verdad
└── test/kotlin/com/alejandro/
    ├── infra/                          registro, humo y lanzador
    ├── c30testing/
    └── c31exercises/
```

- **Paquetes numerados** (`c13collections`): el orden alfabético del árbol de ficheros
  **es** el orden pedagógico.
- **Un `main()` por capítulo**, en su `CNN_00Index.kt`. (Dos `main()` top-level en el
  mismo paquete son un error de compilación; de ahí esta arquitectura.)
- **Identificadores en inglés** (paquetes, ficheros, funciones `demoXxx`), para poder
  cruzar cada capítulo con [kotlinlang.org](https://kotlinlang.org/docs/home.html).
  **Comentarios y salida en español.** Los modelos de dominio también
  (`Usuario`, `Carrito`, `Pedido`): se leen mejor.
- **Cada fichero de contenido** lleva la misma cabecera — *Qué es · Por qué importa ·
  Errores comunes* — y termina con `// ── PARA EXPERIMENTAR ──`.
- **Ninguna demo lanza excepciones ni se cuelga.** `TODO()`, `require`, `!!`, `as` y
  compañía se demuestran dentro de un `runCatching` que imprime el error; los flujos
  infinitos van acotados y todos los `delay` son de milisegundos.

### Añadir tu propia demo

```kotlin
// 1. En cualquier fichero del capítulo, una función sin parámetros:
fun demoLoMio() {
    section("Lo mío")
    show("resultado", 2 + 2)
}

// 2. Regístrala en el CNN_00Index.kt de ese capítulo:
demo("Lo mío", ::demoLoMio)
```

Nada más: el identificador (`13.26`) se genera solo por orden de declaración, y el test
de humo la ejecutará a partir de ese momento.

---

## Dependencias y JDK

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")  // capítulo 28
implementation(kotlin("reflect"))                                        // capítulo 24
implementation("org.jetbrains:annotations:26.1.0")                       // capítulo 27

testImplementation(kotlin("test"))                                       // capítulo 30
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
```

Kotlin **2.4.20**, Gradle **9.6.0** (incluido en el wrapper), JDK **21**.

**Para usar otro JDK**, cambia una línea de `gradle.properties`; no hace falta tocar
`build.gradle.kts`:

```properties
kotlinReference.jdk=25
```

Si Gradle no encuentra ese JDK instalado, intentará descargarlo (hace falta red).

> **Sobre `kotlin-reflect`:** sin esa dependencia, `::class` y las referencias a
> funciones siguen funcionando, pero `memberProperties`, `primaryConstructor` o
> `findAnnotation` fallan **en ejecución**, no al compilar. El capítulo 24 lo explica.

---

## Tests

```bash
./gradlew test                            # los 300 tests
./gradlew test --tests '*CarritoTest'     # una clase
./gradlew build                           # compila Kotlin + Java y ejecuta los tests
```

El informe HTML queda en `build/reports/tests/test/index.html`.

Qué hay dentro:

- **`infra/SmokeTest`** — ejecuta **las 411 demos** y falla si alguna lanza o no
  imprime nada. Es lo que convierte 152 ficheros de ejemplos en código *verificado*.
- **`infra/RegistryTest`** — los 31 capítulos están numerados sin huecos, no hay
  identificadores repetidos y nada está en blanco.
- **`infra/LauncherTest`** — el lanzador entiende sus comandos (contra capítulos de
  mentira, para que no se rompa al añadir uno nuevo).
- **`c30testing/`** — el código de producción del capítulo 30, incluido `runTest` con
  tiempo virtual.
- **`c31exercises/`** — una clase de test por ejercicio, validando la solución
  propuesta. Si rompes una al experimentar, te enteras.

### En cada push y cada pull request

`.github/workflows/build.yml` ejecuta en GitHub Actions lo mismo que ejecutas tú:
`./gradlew build` (compilación mixta Kotlin + Java y los 300 tests) y después el
recorrido completo de las 411 demos desde el lanzador. Si algún test falla, el informe
HTML queda como artefacto descargable de la ejecución.

---

## Problemas frecuentes

<details>
<summary><b>El menú interactivo no aparece / el programa sale solo</b></summary>

Falta la entrada estándar. Con `./gradlew run` funciona porque `build.gradle.kts`
declara `standardInput = System.in`. Si ejecutas desde CI o con una tubería vacía, el
lanzador lo detecta, imprime el índice y sale limpiamente en lugar de colgarse.
Usa `--args="13.4"` para ir directo a una demo.
</details>

<details>
<summary><b>Se ven interrogaciones en vez de acentos</b></summary>

Es la codificación de la consola. El proyecto ya fuerza UTF-8 en la JVM
(`-Dfile.encoding`, `-Dstdout.encoding`, `-Dstderr.encoding`), pero el terminal también
tiene que estar en UTF-8. En Windows: `chcp 65001`.
</details>

<details>
<summary><b>`Picked up JAVA_TOOL_OPTIONS: …` en la salida</b></summary>

No es un error: es la JVM avisando de que hay una variable de entorno definida. Sale
por la salida de error y no afecta a nada.
</details>

<details>
<summary><b>Gradle no encuentra el JDK</b></summary>

Cambia `kotlinReference.jdk` en `gradle.properties` al que tengas instalado (21 o
superior). Si pides uno que no está, Gradle intentará descargarlo y necesitará red.
</details>

<details>
<summary><b>`./gradlew: Permission denied`</b></summary>

`chmod +x gradlew`. En Windows usa `gradlew.bat`.
</details>

<details>
<summary><b>La primera ejecución tarda muchísimo</b></summary>

Está descargando Gradle 9.6.0 (~130 MB) y las dependencias. Sólo pasa una vez.
</details>

<details>
<summary><b>Quiero un botón ▶ por cada fichero, no sólo por capítulo</b></summary>

Kotlin no permite dos `fun main()` top-level en el mismo paquete ("conflicting
overloads"), y por eso hay uno por capítulo. Si quieres uno suelto, añade al final del
fichero:

```kotlin
object Run { @JvmStatic fun main(args: Array<String>) = demoLoQueSea() }
```

No se usa por defecto porque un centenar de objetos de ceremonia contradicen el Kotlin
idiomático que el repositorio pretende enseñar.
</details>

---

## Por dónde empezar, en una línea

```bash
./gradlew run -q --console=plain --args="1"
```

Y después, el capítulo 31: los ejercicios son donde de verdad se aprende.
