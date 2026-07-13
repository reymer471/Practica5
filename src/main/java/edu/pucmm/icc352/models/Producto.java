package edu.pucmm.icc352.models;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    public String nombre;
    public BigDecimal precio;
    @Column(columnDefinition = "TEXT")
    public String descripcion;

    @ElementCollection(fetch = FetchType.EAGER)
    @Lob
    public List<String> imagenesBase64 = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    public List<Comentario> comentarios = new ArrayList<>();

    public Producto() {}
    public Producto(String nombre, BigDecimal precio, String descripcion) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
    }
}