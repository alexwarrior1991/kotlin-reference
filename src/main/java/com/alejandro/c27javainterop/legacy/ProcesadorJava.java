package com.alejandro.c27javainterop.legacy;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Una interfaz de UN SOLO método abstracto (SAM, Single Abstract Method) y un método
 * que declara una excepción comprobada.
 *
 * <p>Sirve para ver desde Kotlin:
 * <ul>
 *   <li>que a un parámetro SAM de Java se le puede pasar una lambda directamente</li>
 *   <li>que Kotlin NO obliga a capturar las {@code checked exceptions} de Java</li>
 * </ul>
 */
public class ProcesadorJava {

    /**
     * Interfaz SAM: un único método abstracto. Kotlin convierte una lambda a esto
     * automáticamente.
     */
    public interface Transformacion {
        @NotNull
        String aplicar(@NotNull String entrada);
    }

    /** Otra SAM, con dos parámetros. */
    public interface Combinador {
        @NotNull
        String combinar(@NotNull String a, @NotNull String b);
    }

    private final String nombre;

    public ProcesadorJava(@NotNull String nombre) {
        this.nombre = nombre;
    }

    /**
     * Recibe una interfaz SAM. Desde Kotlin se le pasa una lambda:
     * {@code procesador.procesar("x") { it.uppercase() }}
     */
    @NotNull
    public String procesar(@NotNull String entrada, @NotNull Transformacion transformacion) {
        return nombre + ": " + transformacion.aplicar(entrada);
    }

    @NotNull
    public String combinar(@NotNull String a, @NotNull String b, @NotNull Combinador combinador) {
        return combinador.combinar(a, b);
    }

    /**
     * Declara una excepción COMPROBADA. En Java, quien llame está obligado a
     * capturarla o a declararla. En Kotlin, no: el compilador ni lo menciona.
     *
     * @throws IOException si la entrada empieza por "error"
     */
    @NotNull
    public String leerRecurso(@NotNull String ruta) throws IOException {
        if (ruta.startsWith("error")) {
            throw new IOException("no se pudo leer '" + ruta + "'");
        }
        return "contenido simulado de " + ruta;
    }

    /**
     * Recibe un array de Java. Kotlin lo ve como {@code Array<(out) String!>!}.
     */
    @NotNull
    public String contar(@NotNull String[] elementos) {
        return elementos.length + " elementos";
    }
}
