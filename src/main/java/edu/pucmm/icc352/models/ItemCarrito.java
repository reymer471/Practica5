package edu.pucmm.icc352.models;
import jakarta.persistence.*;

@Entity
public class ItemCarrito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    @ManyToOne
    public Producto producto;
    public int cantidad;

    public ItemCarrito() {}
    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
}