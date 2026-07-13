package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Venta;
import edu.pucmm.icc352.services.GestorDb;
import edu.pucmm.icc352.views.LayoutHelper;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    public static void mostrarDashboard(Context ctx) {
        String html = """
            <h2 class="mb-4">Dashboard de Ventas</h2>
            <div class="row">
                <div class="col-md-4">
                    <div class="card text-white bg-success mb-3 shadow-sm">
                        <div class="card-header text-center fw-bold">Total Ingresos</div>
                        <div class="card-body text-center">
                            <h3 class="card-title" id="total-ventas">RD$ 0.00</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-8">
                    <div class="card shadow-sm p-4">
                        <h5 class="text-center">Distribución de Productos Vendidos</h5>
                        <canvas id="productosChart"></canvas>
                    </div>
                </div>
            </div>
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            <script>
                let chartInstance = null;

                // Uso de Fetch API 
                function cargarDatos() {
                    fetch('/api/dashboard-data')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('total-ventas').innerText = 'RD$ ' + data.total;

                            const ctxCanvas = document.getElementById('productosChart').getContext('2d');
                            if (chartInstance) {
                                chartInstance.destroy();
                            }

                            chartInstance = new Chart(ctxCanvas, {
                                type: 'pie', // Gráfico tipo patel
                                data: {
                                    labels: Object.keys(data.productos),
                                    datasets: [{
                                        label: 'Cantidad Vendida',
                                        data: Object.values(data.productos),
                                        backgroundColor: ['#ff9999','#66b3ff','#99ff99','#ffcc99', '#c2c2f0', '#ffb3e6']
                                    }]
                                }
                            });
                        })
                        .catch(err => console.error("Error cargando dashboard: ", err));
                }

                // Carga inicial
                cargarDatos();

                // Refrescar al recibir evento del WebSocket
                document.addEventListener('nueva_venta', () => {
                    console.log("Nueva venta detectada vía WebSockets. Refrescando gráfico...");
                    cargarDatos();
                });
            </script>
        """;
        ctx.html(LayoutHelper.render("Dashboard", html, ctx));
    }

    // Endpoint API para ser consumido por Fetch API
    public static void obtenerDatos(Context ctx) {
        EntityManager em = GestorDb.getEntityManager();
        List<Venta> ventas = em.createQuery("SELECT v FROM Venta v", Venta.class).getResultList();
        em.close();

        BigDecimal total = BigDecimal.ZERO;
        Map<String, Integer> productosVendidos = new HashMap<>();

        for (Venta v : ventas) {
            for (ItemCarrito item : v.listaProductos) {
                BigDecimal sub = item.producto.precio.multiply(new BigDecimal(item.cantidad));
                total = total.add(sub);
                String nombre = item.producto.nombre;
                productosVendidos.put(nombre, productosVendidos.getOrDefault(nombre, 0) + item.cantidad);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total.toString());
        data.put("productos", productosVendidos);

        ctx.json(data);
    }
}