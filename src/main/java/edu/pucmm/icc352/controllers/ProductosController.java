package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.services.GestorDb;
import edu.pucmm.icc352.views.LayoutHelper;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

public class ProductosController {

    public static void listarCRUD(Context ctx) {
        EntityManager em = GestorDb.getEntityManager();
        List<Producto> productos = em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
        em.close();

        StringBuilder rows = new StringBuilder();
        for (Producto p : productos) {
            rows.append("""
                <tr>
                    <td>%s</td><td>RD$ %s</td>
                    <td>
                        <form action='/admin/productos/eliminar/%d' method='POST'>
                            <button class='btn btn-danger btn-sm'>Eliminar</button>
                        </form>
                    </td>
                </tr>
            """.formatted(p.nombre, p.precio, p.id));
        }

        String html = """
            <h2>Administrar Productos</h2>
            <div class="card p-3 mb-4 bg-light">
                <form action="/admin/productos/crear" method="POST" enctype="multipart/form-data">
                    <div class="row g-2 align-items-center">
                        <div class="col-auto"><input type="text" name="nombre" placeholder="Nombre" class="form-control" required></div>
                        <div class="col-auto"><input type="number" name="precio" placeholder="Precio" class="form-control" required></div>
                        <div class="col-auto"><input type="file" name="imagenes" class="form-control" multiple required accept="image/*"></div>
                        <div class="col-12 mt-2"><textarea name="descripcion" class="form-control" placeholder="Descripción del producto..." required></textarea></div>
                        <div class="col-12 mt-2"><button type="submit" class="btn btn-success">Crear Producto</button></div>
                    </div>
                </form>
            </div>
            <table class="table table-bordered">
                <thead><tr class="table-dark"><th>Producto</th><th>Precio</th><th>Acción</th></tr></thead>
                <tbody>%s</tbody>
            </table>
        """.formatted(rows);
        ctx.html(LayoutHelper.render("CRUD Productos", html, ctx));
    }

    public static void crear(Context ctx) throws Exception {
        String nombre = ctx.formParam("nombre");
        BigDecimal precio = new BigDecimal(ctx.formParam("precio"));
        String descripcion = ctx.formParam("descripcion");

        Producto nuevoProd = new Producto(nombre, precio, descripcion);
        List<UploadedFile> archivos = ctx.uploadedFiles("imagenes");
        for (UploadedFile archivo : archivos) {
            byte[] bytes = archivo.content().readAllBytes();
            nuevoProd.imagenesBase64.add(Base64.getEncoder().encodeToString(bytes));
        }

        EntityManager em = GestorDb.getEntityManager();
        em.getTransaction().begin();
        em.persist(nuevoProd);
        em.getTransaction().commit();
        em.close();

        ctx.redirect("/admin/productos");
    }

    public static void eliminar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        EntityManager em = GestorDb.getEntityManager();
        em.getTransaction().begin();
        Producto p = em.find(Producto.class, id);
        if (p != null) em.remove(p);
        em.getTransaction().commit();
        em.close();
        ctx.redirect("/admin/productos");
    }
}