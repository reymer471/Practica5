package edu.pucmm.icc352.models;
import jakarta.persistence.*;

@Entity
public class Comentario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    @Column(columnDefinition = "TEXT")
    public String texto;
    public String autor;

    public Comentario() {}
    public Comentario(String texto, String autor) {
        this.texto = texto;
        this.autor = autor;
    }
}