package edu.pucmm.icc352;

import edu.pucmm.icc352.config.Routes;
import edu.pucmm.icc352.services.GestorDb;
import io.javalin.Javalin;
import org.h2.tools.Server;

public class Main {
    public static void main(String[] args) throws Exception {

        // arrancar la base de datos H2 en modo servidor
        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();

        // iniciar JPA
        GestorDb.iniciarEntityFactory();

        // inicializar Javalin y arrancar en el puerto 7000
        Javalin app = Javalin.create().start(7000);

        // registrar todas las rutas que me pasa la instancia de app
        Routes.registrarRutas(app);
    }
}