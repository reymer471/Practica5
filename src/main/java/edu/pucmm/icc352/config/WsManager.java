package edu.pucmm.icc352.config;

import io.javalin.websocket.WsContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WsManager {
    public static Set<WsContext> clientesConectados = ConcurrentHashMap.newKeySet();

    // actualiza la cantidad de usuarios en tiempo real
    public static void broadcastUserCount() {
        int count = clientesConectados.size();
        String msg = "{\"type\":\"USER_COUNT\", \"count\":" + count + "}";
        clientesConectados.stream()
                .filter(ctx -> ctx.session.isOpen())
                .forEach(session -> session.send(msg));
    }

    // notifica la eliminación de un comentario
    public static void broadcastCommentDeleted(int idProd, int idCom) {
        String msg = String.format("{\"type\":\"COMMENT_DELETED\", \"idProd\":%d, \"idCom\":%d}", idProd, idCom);
        clientesConectados.stream()
                .filter(ctx -> ctx.session.isOpen())
                .forEach(session -> session.send(msg));
    }

    // notifica una nueva venta para refrescar el Dashboard
    public static void broadcastNewSale() {
        String msg = "{\"type\":\"NEW_SALE\"}";
        clientesConectados.stream()
                .filter(ctx -> ctx.session.isOpen())
                .forEach(session -> session.send(msg));
    }
}