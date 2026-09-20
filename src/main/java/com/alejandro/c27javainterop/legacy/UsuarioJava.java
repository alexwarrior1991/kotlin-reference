package com.alejandro.c27javainterop.legacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Un POJO de Java "de toda la vida", con getters y setters.
 *
 * <p>Sirve para ver tres cosas desde Kotlin:
 * <ul>
 *   <li>que {@code getNombre()} se usa como si fuera una propiedad: {@code usuario.nombre}</li>
 *   <li>que un campo SIN anotar llega a Kotlin como "platform type" ({@code String!})</li>
 *   <li>que un campo anotado con {@code @Nullable} obliga a Kotlin a comprobarlo</li>
 * </ul>
 *
 * <p>Está escrito a propósito en el estilo Java clásico: si fuera Kotlin serían
 * tres líneas.
 */
public class UsuarioJava {

    private String nombre;
    private int edad;
    private String apodo;   // este SÍ puede ser null

    public UsuarioJava(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
        this.apodo = null;
    }

    /**
     * Sin anotación de nulabilidad: Kotlin lo ve como {@code String!} y NO te obliga
     * a comprobarlo. Si devolviera null, tendrías un NPE sin aviso previo.
     */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    /**
     * Anotado con {@code @Nullable}: Kotlin lo ve como {@code String?} y SÍ te
     * obliga a tratarlo. Ésta es la diferencia práctica de anotar.
     */
    @Nullable
    public String getApodo() {
        return apodo;
    }

    public void setApodo(@Nullable String apodo) {
        this.apodo = apodo;
    }

    /**
     * Anotado con {@code @NotNull}: Kotlin lo ve como {@code String} y no hace falta
     * ni {@code ?.} ni {@code !!}.
     */
    @NotNull
    public String descripcion() {
        return nombre + " (" + edad + ")";
    }

    /**
     * Un método que devuelve null sin decirlo. Es la trampa clásica de la
     * interoperabilidad: Kotlin no puede saberlo y no te avisa.
     */
    public String buscarSobrenombre() {
        return null;
    }

    /**
     * Un getter que NO sigue la convención JavaBean (no empieza por get/is), así que
     * desde Kotlin hay que llamarlo como función, no como propiedad.
     */
    public String obtenerResumen() {
        return "resumen de " + nombre;
    }
}
