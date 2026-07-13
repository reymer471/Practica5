package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.config.WsManager;
import edu.pucmm.icc352.models.*;
import edu.pucmm.icc352.services.GestorDb;
import edu.pucmm.icc352.views.LayoutHelper;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarritoController {

    @SuppressWarnings("unchecked")
    private static List<ItemCarrito> obtenerCarrito(Context ctx) {
        List<ItemCarrito> carrito = ctx.sessionAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            ctx.sessionAttribute("carrito", carrito);
        }
        return carrito;
    }

    public static void catalogo(Context ctx) {
        int pagina = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int limite = 10;
        int offset = (pagina - 1) * limite;

        EntityManager em = GestorDb.getEntityManager();
        List<Producto> productos = em.createQuery("SELECT p FROM Producto p", Producto.class)
                .setFirstResult(offset).setMaxResults(limite).getResultList();
        long totalProds = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();
        em.close();

        int totalPaginas = (int) Math.ceil((double) totalProds / limite);
        StringBuilder rows = new StringBuilder();

        for (Producto p : productos) {
            String imgBase = p.imagenesBase64.isEmpty() ? "" : "<img src='data:image/jpeg;base64," + p.imagenesBase64.get(0) + "' width='60' height='60' class='rounded'>";
            rows.append("""
                <tr>
                    <td class="text-center">%s</td>
                    <td class="align-middle"><a href="/producto/%d" class="fw-bold text-decoration-none">%s</a></td>
                    <td class="align-middle">RD$ %s</td>
                    <td class="align-middle">
                        <form action="/carrito/agregar" method="POST" class="d-flex">
                            <input type="hidden" name="id" value="%d">
                            <input type="number" name="cantidad" value="1" min="1" class="form-control me-2" style="width: 80px;">
                            <button type="submit" class="btn btn-primary btn-sm">Añadir</button>
                        </form>
                    </td>
                </tr>
            """.formatted(imgBase, p.id, p.nombre, p.precio, p.id));
        }

        StringBuilder paginacion = new StringBuilder("<ul class='pagination justify-content-center mt-4'>");
        for(int i = 1; i <= totalPaginas; i++) {
            paginacion.append("<li class='page-item ").append(i == pagina ? "active" : "").append("'><a class='page-link' href='/?page=").append(i).append("'>").append(i).append("</a></li>");
        }
        paginacion.append("</ul>");

        String html = """
            <h2 class="mb-4">Catálogo de Productos</h2>
            <div class="card shadow-sm"><table class="table table-hover mb-0">
                <thead class="table-dark"><tr><th>Img</th><th>Producto</th><th>Precio</th><th>Acción</th></tr></thead>
                <tbody>%s</tbody>
            </table></div>
            %s
        """.formatted(rows, paginacion);
        ctx.html(LayoutHelper.render("Catálogo", html, ctx));
    }

    public static void verProducto(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        EntityManager em = GestorDb.getEntityManager();
        Producto p = em.find(Producto.class, id);
        em.close();

        if (p == null) { ctx.redirect("/"); return; }

        StringBuilder imgs = new StringBuilder();
        for (String base64 : p.imagenesBase64) {
            imgs.append("<img src='data:image/jpeg;base64,").append(base64).append("' class='img-fluid rounded border me-2 mb-2' style='max-width:200px;'>");
        }

        boolean isAdmin = "admin".equals(ctx.sessionAttribute("usuario"));
        StringBuilder comments = new StringBuilder();
        for (Comentario c : p.comentarios) {
            String deleteBtn = isAdmin ? "<form action='/admin/producto/%d/comentario/%d/eliminar' method='POST' class='d-inline float-end'><button class='btn btn-sm btn-danger'>Borrar</button></form>".formatted(p.id, c.id) : "";

            // le puse un id al div pa q el ws lo borre y ya
            comments.append("<div id='comentario-%d' class='alert alert-secondary'><b>%s</b>: %s %s</div>".formatted(c.id, c.autor, c.texto, deleteBtn));
        }

        String html = """
            <div class="row bg-white p-4 rounded shadow-sm">
                <div class="col-md-6">%s</div>
                <div class="col-md-6">
                    <h2>%s</h2><h3 class="text-success">RD$ %s</h3><p class="mt-3">%s</p>
                    <form action="/carrito/agregar" method="POST" class="d-flex mb-4">
                        <input type="hidden" name="id" value="%d">
                        <input type="number" name="cantidad" value="1" min="1" class="form-control me-2 w-25">
                        <button type="submit" class="btn btn-lg btn-primary">Agregar al Carrito</button>
                    </form>
                </div>
            </div>
            <div class="mt-5 card shadow-sm p-4">
                <h4>Comentarios</h4><hr>
                <div id="comentarios-container">%s</div>
                <form action="/producto/%d/comentar" method="POST" class="mt-4">
                    <input type="text" name="autor" placeholder="Tu nombre" class="form-control mb-2" required>
                    <textarea name="texto" class="form-control mb-2" placeholder="Escribe un comentario..." required></textarea>
                    <button class="btn btn-secondary">Publicar</button>
                </form>
            </div>
        """.formatted(imgs, p.nombre, p.precio, p.descripcion, p.id, comments, p.id);
        ctx.html(LayoutHelper.render(p.nombre, html, ctx));
    }

    public static void agregarComentario(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        EntityManager em = GestorDb.getEntityManager();
        em.getTransaction().begin();
        Producto p = em.find(Producto.class, id);
        if (p != null) {
            p.comentarios.add(new Comentario(ctx.formParam("texto"), ctx.formParam("autor")));
            em.merge(p);
        }
        em.getTransaction().commit();
        em.close();
        ctx.redirect("/producto/" + id);
    }

    public static void eliminarComentario(Context ctx) {
        int idProd = Integer.parseInt(ctx.pathParam("idProd"));
        int idCom = Integer.parseInt(ctx.pathParam("idCom"));
        EntityManager em = GestorDb.getEntityManager();
        em.getTransaction().begin();
        Producto p = em.find(Producto.class, idProd);
        if (p != null) {
            p.comentarios.removeIf(c -> c.id == idCom);
            em.merge(p);
        }
        em.getTransaction().commit();
        em.close();

        // aqui le aviso a to el mundo por ws pa q se borre y ya
        WsManager.broadcastCommentDeleted(idProd, idCom);

        ctx.redirect("/producto/" + idProd);
    }

    public static void agregar(Context ctx) {
        int id = Integer.parseInt(ctx.formParam("id"));
        int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

        EntityManager em = GestorDb.getEntityManager();
        Producto prod = em.find(Producto.class, id);
        em.close();

        if (prod != null) {
            List<ItemCarrito> carrito = obtenerCarrito(ctx);
            ItemCarrito existente = carrito.stream().filter(item -> item.producto.id == id).findFirst().orElse(null);
            if (existente != null) existente.cantidad += cantidad;
            else carrito.add(new ItemCarrito(prod, cantidad));
        }
        ctx.redirect("/");
    }

    public static void verCarrito(Context ctx) {
        List<ItemCarrito> carrito = obtenerCarrito(ctx);
        StringBuilder rows = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;
        int index = 0;

        for (ItemCarrito item : carrito) {
            BigDecimal sub = item.producto.precio.multiply(new BigDecimal(item.cantidad));
            total = total.add(sub);
            rows.append("""
                <tr>
                    <td>%s</td><td>RD$ %s</td><td>%d</td><td>RD$ %s</td>
                    <td><form action="/carrito/eliminar/%d" method="POST"><button class="btn btn-danger btn-sm">Quitar</button></form></td>
                </tr>
            """.formatted(item.producto.nombre, item.producto.precio, item.cantidad, sub, index++));
        }

        String html = """
            <h2 class="mb-4">Tu Carrito</h2>
            <form action="/carrito/procesar" method="POST" class="card p-4 shadow-sm mb-4 bg-white">
                <div class="mb-4"><label class="fw-bold form-label">Cliente:</label><input type="text" name="cliente" class="form-control" required></div>
                <table class="table table-bordered">
                    <thead class="table-dark"><tr><th>Producto</th><th>Precio</th><th>Cant</th><th>Total</th><th>Acción</th></tr></thead>
                    <tbody>%s</tbody>
                </table>
                <h4 class="text-end text-success fw-bold">Total: RD$ %s</h4>
                <div class="d-flex justify-content-between">
                    <a href="/carrito/limpiar" class="btn btn-warning">Limpiar</a>
                    <button type="submit" class="btn btn-success px-4">Procesar Factura</button>
                </div>
            </form>
        """.formatted(rows.isEmpty() ? "<tr><td colspan='5' class='text-center'>Vacío</td></tr>" : rows, total);
        ctx.html(LayoutHelper.render("Carrito", html, ctx));
    }

    public static void eliminar(Context ctx) {
        int index = Integer.parseInt(ctx.pathParam("index"));
        List<ItemCarrito> carrito = obtenerCarrito(ctx);
        if (index >= 0 && index < carrito.size()) carrito.remove(index);
        ctx.redirect("/carrito");
    }

    public static void limpiar(Context ctx) {
        ctx.sessionAttribute("carrito", new ArrayList<ItemCarrito>());
        ctx.redirect("/carrito");
    }

    public static void procesarCompra(Context ctx) {
        String cliente = ctx.formParam("cliente");
        List<ItemCarrito> carritoSession = obtenerCarrito(ctx);

        if (!carritoSession.isEmpty() && cliente != null && !cliente.isEmpty()) {
            EntityManager em = GestorDb.getEntityManager();
            em.getTransaction().begin();
            Venta venta = new Venta(new Date(), cliente, new ArrayList<>());
            for (ItemCarrito ic : carritoSession) {
                Producto p = em.find(Producto.class, ic.producto.id);
                venta.listaProductos.add(new ItemCarrito(p, ic.cantidad));
            }
            em.persist(venta);
            em.getTransaction().commit();
            em.close();
            ctx.sessionAttribute("carrito", new ArrayList<ItemCarrito>());

            // eto e pa avisar al dashboard q se vendio algo y ya simple
            WsManager.broadcastNewSale();
        }
        ctx.redirect("/");
    }
}