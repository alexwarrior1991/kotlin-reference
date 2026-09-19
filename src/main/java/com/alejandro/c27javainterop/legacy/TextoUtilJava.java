package com.alejandro.c27javainterop.legacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades estáticas al estilo Java, para ver desde Kotlin:
 *
 * <ul>
 *   <li>cómo se llaman los métodos {@code static} (sin {@code Companion} de por medio)</li>
 *   <li>cómo se leen las constantes {@code static final}</li>
 *   <li>qué pasa con un {@code List} sin genéricos (tipo "raw")</li>
 *   <li>cómo se reciben los {@code varargs} de Java</li>
 * </ul>
 */
public final class TextoUtilJava {

    /** Constante: desde Kotlin se lee como {@code TextoUtilJava.SEPARADOR}. */
    public static final String SEPARADOR = " · ";

    public static final int LONGITUD_MAXIMA = 20;

    /** Clase de utilidades: no se instancia. */
    private TextoUtilJava() {
    }

    /**
     * Método estático normal. Desde Kotlin: {@code TextoUtilJava.enMayusculas("x")}.
     */
    @NotNull
    public static String enMayusculas(@NotNull String texto) {
        return texto.toUpperCase();
    }

    /**
     * Devuelve null si el texto es demasiado corto. Al estar anotado, Kotlin obliga
     * a comprobarlo.
     */
    @Nullable
    public static String recortar(@Nullable String texto) {
        if (texto == null || texto.length() < 3) {
            return null;
        }
        return texto.length() <= LONGITUD_MAXIMA
                ? texto
                : texto.substring(0, LONGITUD_MAXIMA - 1) + "…";
    }

    /**
     * Varargs de Java. Desde Kotlin se llama igual que un {@code vararg}, incluido
     * el operador de propagación {@code *}.
     */
    @NotNull
    public static String unir(@NotNull String... partes) {
        return String.join(SEPARADOR, partes);
    }

    /**
     * Devuelve una lista SIN genéricos (un "raw type"). Kotlin la ve como
     * {@code (Mutable)List<Any!>!}, que es tan permisivo como incómodo.
     *
     * <p>Es justo lo que hay que evitar al escribir APIs Java que vayan a usarse
     * desde Kotlin.
     */
    @SuppressWarnings("rawtypes")
    @NotNull
    public static List sinGenericos() {
        List lista = new ArrayList();
        lista.add("uno");
        lista.add(2);
        return lista;
    }

    /**
     * La misma idea, pero bien tipada. Kotlin la ve como {@code MutableList<String>}.
     */
    @NotNull
    public static List<String> conGenericos() {
        List<String> lista = new ArrayList<>();
        lista.add("uno");
        lista.add("dos");
        return lista;
    }
}
