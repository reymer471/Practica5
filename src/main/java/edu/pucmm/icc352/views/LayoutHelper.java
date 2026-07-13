package edu.pucmm.icc352.views;

import edu.pucmm.icc352.models.ItemCarrito;
import io.javalin.http.Context;
import java.util.List;

public class LayoutHelper {
    @SuppressWarnings("unchecked")
    public static String render(String titulo, String contenido, Context ctx) {
        List<ItemCarrito> carrito = ctx.sessionAttribute("carrito");
        int cantCarrito = (carrito != null) ? carrito.stream().mapToInt(i -> i.cantidad).sum() : 0;
        boolean isAdmin = "admin".equals(ctx.sessionAttribute("usuario"));

        String adminLinks = isAdmin
                ? """
                <li class="nav-item"><a class="nav-link" href="/admin/dashboard">Dashboard</a></li>
                <li class="nav-item"><a class="nav-link" href="/admin/ventas">Ventas</a></li>
                <li class="nav-item"><a class="nav-link" href="/admin/productos">Productos</a></li>
                <li class="nav-item"><a class="nav-link text-danger fw-bold" href="/logout">Salir</a></li>
              """
                : "<li class='nav-item'><a class='nav-link' href='/login'>Acceso Admin</a></li>";

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>
            <body class="bg-light">
                <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4 shadow-sm">
                    <div class="container">
                        <a class="navbar-brand" href="/">🛒 MiTiendita</a>
                        <div class="collapse navbar-collapse d-flex justify-content-end">
                            <ul class="navbar-nav align-items-center">
                                <li class="nav-item me-3 text-white">
                                    <small>👥 Activos: <span id="ws-user-count" class="badge bg-info text-dark">0</span></small>
                                </li>
                                <li class="nav-item"><a class="nav-link" href="/">Catálogo</a></li>
                                %s
                                <li class="nav-item ms-3">
                                    <a class="btn btn-outline-light d-flex align-items-center" href="/carrito">
                                        Carrito <span class="badge bg-primary ms-2">%d</span>
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>
                <div class="container pb-5">
                    %s
                </div>
                
                <script>
                    const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
                    const socket = new WebSocket(wsProtocol + window.location.host + "/ws");
                    
                    socket.onmessage = function(event) {
                        const data = JSON.parse(event.data);
                        
                        // Actualizar número de usuarios 
                        if (data.type === 'USER_COUNT') {
                            const countEl = document.getElementById('ws-user-count');
                            if(countEl) countEl.innerText = data.count;
                        } 
                        // Remover comentario en tiempo real 
                        else if (data.type === 'COMMENT_DELETED') {
                            const commentDiv = document.getElementById('comentario-' + data.idCom);
                            if(commentDiv) {
                                commentDiv.style.transition = 'opacity 0.5s ease';
                                commentDiv.style.opacity = '0';
                                setTimeout(() => commentDiv.remove(), 500);
                            }
                        } 
                        // Disparar evento para actualizar dashboard (si está abierto) 
                        else if (data.type === 'NEW_SALE') {
                            document.dispatchEvent(new Event('nueva_venta'));
                        }
                    };
                </script>
            </body>
            </html>
        """.formatted(titulo, adminLinks, cantCarrito, contenido);
    }
}