package co.edu.uniquindio.chat.modelo;

import java.io.Serializable;
import java.util.ArrayList;

public class Usuario implements Serializable {
    private String idUsuario;
    private String nombre;

    public Usuario(String idUsuario, String nombre) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}