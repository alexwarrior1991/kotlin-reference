package com.alejandro.c27javainterop.legacy;

import com.alejandro.c27javainterop.ApiParaJava;
import com.alejandro.c27javainterop.ConfiguracionKotlin;
import com.alejandro.c27javainterop.FabricaKotlin;

/**
 * El sentido CONTRARIO: código Java que usa código Kotlin.
 *
 * <p>Esta clase existe para demostrar, compilando de verdad, qué aspecto tiene
 * desde Java el Kotlin del fichero {@code C27_03KotlinSideJava.kt}:
 *
 * <ul>
 *   <li>las funciones de nivel superior viven en una clase {@code ...Kt}</li>
 *   <li>con {@code @JvmName} esa clase pasa a llamarse como tú quieras</li>
 *   <li>sin {@code @JvmOverloads}, los valores por defecto NO existen para Java</li>
 *   <li>sin {@code @JvmStatic}, el companion object obliga a escribir
 *       {@code Clase.Companion.metodo()}</li>
 *   <li>{@code @JvmField} expone un campo en lugar de un getter</li>
 * </ul>
 *
 * <p>Que este fichero compile es la prueba de que todo lo anterior es cierto: si
 * quitas cualquiera de esas anotaciones del lado Kotlin, deja de compilar.
 */
public final class ConsumidorDeKotlinJava {

    private ConsumidorDeKotlinJava() {
    }

    /** Llama a funciones Kotlin de nivel superior desde Java. */
    public static String llamarFuncionesDeNivelSuperior() {
        // Gracias a @JvmName("ApiParaJava"), la clase se llama así y no
        // "C27_03KotlinSideJavaKt".
        String saludo = ApiParaJava.saludar("Java");

        // Sin @JvmOverloads habría que pasar SIEMPRE los tres argumentos.
        // Con ella, estas tres llamadas existen:
        String uno = ApiParaJava.conectar("localhost");
        String dos = ApiParaJava.conectar("localhost", 8080);
        String tres = ApiParaJava.conectar("localhost", 8080, false);

        return saludo + " | " + uno + " | " + dos + " | " + tres;
    }

    /** Usa el companion object de una clase Kotlin. */
    public static String llamarCompanion() {
        // `crear` lleva @JvmStatic: se llama como un estático normal.
        ConfiguracionKotlin conJvmStatic = FabricaKotlin.crear("con-jvmstatic");

        // `crearSinJvmStatic` NO la lleva: hay que pasar por Companion.
        ConfiguracionKotlin sinJvmStatic =
                FabricaKotlin.Companion.crearSinJvmStatic("sin-jvmstatic");

        return conJvmStatic.getNombre() + " | " + sinJvmStatic.getNombre();
    }

    /** Lee propiedades y campos de una clase Kotlin. */
    public static String leerPropiedades() {
        ConfiguracionKotlin configuracion = FabricaKotlin.crear("demo");

        // Una propiedad normal de Kotlin se lee con su getter.
        String porGetter = configuracion.getNombre();

        // Una propiedad con @JvmField es un CAMPO público: sin getter.
        int campoDirecto = configuracion.version;

        // Una constante `const val` del companion también es un campo estático.
        String constante = ConfiguracionKotlin.PREFIJO;

        return porGetter + " | v" + campoDirecto + " | " + constante;
    }

    /** Captura una excepción lanzada desde Kotlin gracias a @Throws. */
    public static String capturarExcepcionDeKotlin() {
        try {
            ApiParaJava.validarRuta("ruta/mala");
            return "no lanzó";
        } catch (java.io.IOException e) {
            // Sin @Throws en el lado Kotlin, este catch NO compilaría: javac diría
            // que IOException nunca se lanza en el bloque try.
            return "capturada: " + e.getMessage();
        }
    }
}
