package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Venta;
import edu.pucmm.icc352.services.GestorDb;
import edu.pucmm.icc352.views.LayoutHelper;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

public class VentasController {
    public static void listar(Context ctx) {
        EntityManager em = GestorDb.getEntityManager();
        List<Venta> ventas = em.createQuery("SELECT v FROM Venta v ORDER BY v.fechaCompra DESC", Venta.class).getResultList();
        em.close();

        StringBuilder html = new StringBuilder("<h2>Ventas Procesadas</h2><hr>");
        for (Venta v : ventas) {
            html.append("<h5 class='text-primary mt-4'>Cliente: %s | Fecha: %s</h5>".formatted(v.nombreCliente, v.fechaCompra.toString()));
            html.append("<table class='table table-sm table-bordered'><thead><tr class='table-secondary'><th>Producto</th><th>Precio</th><th>Cant</th><th>Subtotal</th></tr></thead><tbody>");
            BigDecimal totalVenta = BigDecimal.ZERO;
            for (ItemCarrito item : v.listaProductos) {
                BigDecimal sub = item.producto.precio.multiply(new BigDecimal(item.cantidad));
                totalVenta = totalVenta.add(sub);
                html.append("<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td></tr>".formatted(item.producto.nombre, item.producto.precio, item.cantidad, sub));
            }
            html.append("<tr><td colspan='3' class='text-end'><b>Total Compra:</b></td><td><b>RD$ %s</b></td></tr></tbody></table>".formatted(totalVenta));
        }
        if (ventas.isEmpty()) html.append("<p>Aún no hay ventas registradas.</p>");
        ctx.html(LayoutHelper.render("Reporte Ventas", html.toString(), ctx));
    }
}