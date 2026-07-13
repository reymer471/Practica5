package edu.pucmm.icc352.config;

import edu.pucmm.icc352.controllers.*;
import io.javalin.Javalin;

public class Routes {
    public static void registrarRutas(Javalin app) {
        // ruta Publica
        app.get("/", CarritoController::catalogo);
        app.get("/producto/{id}", CarritoController::verProducto);
        app.post("/carrito/agregar", CarritoController::agregar);
        app.get("/carrito", CarritoController::verCarrito);
        app.post("/carrito/eliminar/{index}", CarritoController::eliminar);
        app.get("/carrito/limpiar", CarritoController::limpiar);
        app.post("/carrito/procesar", CarritoController::procesarCompra);
        app.post("/producto/{id}/comentar", CarritoController::agregarComentario);

        app.get("/login", LoginController::mostrarFormulario);
        app.post("/login", LoginController::procesarLogin);
        app.get("/logout", LoginController::cerrarSesion);

        // rutas de administración
        app.before("/admin/*", LoginController::verificarAcceso);
        app.get("/admin/productos", ProductosController::listarCRUD);
        app.post("/admin/productos/crear", ProductosController::crear);
        app.post("/admin/productos/eliminar/{id}", ProductosController::eliminar);
        app.get("/admin/ventas", VentasController::listar);
        app.post("/admin/producto/{idProd}/comentario/{idCom}/eliminar", CarritoController::eliminarComentario);

        //rutas para el Dashboard
        app.get("/admin/dashboard", DashboardController::mostrarDashboard);
        app.before("/api/*", LoginController::verificarAcceso);
        app.get("/api/dashboard-data", DashboardController::obtenerDatos);

        // configuración de WebSockets
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                WsManager.clientesConectados.add(ctx);
                WsManager.broadcastUserCount();
            });
            ws.onClose(ctx -> {
                WsManager.clientesConectados.remove(ctx);
                WsManager.broadcastUserCount();
            });
        });
    }
}