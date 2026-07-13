package edu.pucmm.icc352.models;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public Date fechaCompra;
    public String nombreCliente;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ItemCarrito> listaProductos = new ArrayList<>();

    public Venta() {}
    public Venta(Date fechaCompra, String nombreCliente, List<ItemCarrito> listaProductos) {
        this.fechaCompra = fechaCompra;
        this.nombreCliente = nombreCliente;
        this.listaProductos = listaProductos;
    }
}