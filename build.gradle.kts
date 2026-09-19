// =====================================================================================
//  kotlin-reference · configuración de build
//
//  Este fichero también es material didáctico: cada dependencia lleva un comentario
//  explicando QUÉ capítulo la necesita y QUÉ pasa si falta.
// =====================================================================================

plugins {
    kotlin("jvm") version "2.4.20"

    // El plugin `application` aporta dos tareas que usamos constantemente:
    //   · run         -> lanza el índice de demos (com.alejandro.MainKt)
    //   · installDist -> genera un script ejecutable sin Gradle de por medio
    application
}

group = "com.alejandro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // --- Capítulo 28: corrutinas ---------------------------------------------------
    // Ésta es LA dependencia que hay que añadir para las corrutinas. Sin ella no
    // existen launch, async, Job, Flow, StateFlow ni Channel: `suspend` es parte del
    // lenguaje, pero el runtime de corrutinas vive en esta librería aparte.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // --- Capítulo 24: reflexión ----------------------------------------------------
    // Matiz importante: `::class`, `simpleName` y las referencias a funciones YA
    // funcionan sin esta dependencia. Sólo la API "pesada" (memberProperties,
    // primaryConstructor, findAnnotation...) la necesita, y si falta el error aparece
    // EN TIEMPO DE EJECUCIÓN (KotlinReflectionNotSupportedError), no al compilar.
    implementation(kotlin("reflect"))

    // --- Capítulo 27: interoperabilidad con Java -----------------------------------
    // Aporta @Nullable/@NotNull para las clases Java de ejemplo, y así poder comparar
    // un "platform type" (String!) con un tipo correctamente anotado.
    implementation("org.jetbrains:annotations:26.1.0")

    // --- Capítulo 30: testing ------------------------------------------------------
    testImplementation(kotlin("test"))
    // runTest + tiempo virtual: un delay(5.seconds) en un test tarda milisegundos.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}

// =====================================================================================
//  JDK del proyecto
//
//  Se lee de gradle.properties (kotlinReference.jdk) para poder cambiarlo sin tocar
//  este fichero. El valor por defecto es 21 porque es el LTS más extendido; si tienes
//  otro JDK instalado (por ejemplo 25) basta con cambiar esa propiedad.
//
//  Ojo: fijar aquí una versión muy nueva haría que el proyecto no compilase en
//  cualquier máquina que no la tenga instalada.
// =====================================================================================
val projectJdk: Int = providers.gradleProperty("kotlinReference.jdk").getOrElse("21").toInt()

kotlin {
    jvmToolchain(projectJdk)
}

application {
    // Main.kt declara `package com.alejandro`, así que el compilador genera la clase
    // `com.alejandro.MainKt` (nombre del fichero + sufijo Kt). Esto NO depende de en
    // qué carpeta esté el fichero, sólo del paquete y del nombre.
    mainClass.set("com.alejandro.MainKt")
    applicationName = "kotlin-reference"

    // Se aplican tanto a `gradlew run` como al script que genera `installDist`.
    applicationDefaultJvmArgs = listOf(
        // Todo el repositorio está escrito en español. Matiz poco conocido: desde el
        // JDK 19, `System.out` NO usa `file.encoding`, sino la codificación nativa de
        // la consola; en un terminal con locale "C" eso convierte cada "í" en un "?".
        // Hacen falta las tres propiedades.
        "-Dfile.encoding=UTF-8",
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",

        // `assert(...)` de la biblioteca estándar es un no-op salvo que la JVM arranque
        // con -ea. Lo activamos para que la demo del capítulo 25 haga algo de verdad.
        "-ea",
    )
}

tasks.named<JavaExec>("run") {
    // El lanzador tiene un menú interactivo: sin esto, `gradlew run` no recibiría
    // nada por teclado.
    standardInput = System.`in`
    defaultCharacterEncoding = "UTF-8"
}

// Mismo motivo para las clases Java del capítulo 27, que también llevan comentarios
// en español.
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    defaultCharacterEncoding = "UTF-8"
    jvmArgs("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
    testLogging {
        events("passed", "failed", "skipped")
    }
}
