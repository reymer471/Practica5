package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.services.GestorDb;
import edu.pucmm.icc352.views.LayoutHelper;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import org.jasypt.util.text.BasicTextEncryptor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;

public class LoginController {
    private static final String SECRET_KEY = "Pucmm_ICC352_Seguridad";

    public static void verificarAcceso(Context ctx) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(SECRET_KEY);
        String usuarioSesion = ctx.sessionAttribute("usuario");
        String cookieEncriptada = ctx.cookie("remember_me");

        if (usuarioSesion == null && cookieEncriptada != null) {
            try {
                String usuarioDesencriptado = textEncryptor.decrypt(cookieEncriptada);
                ctx.sessionAttribute("usuario", usuarioDesencriptado);
                usuarioSesion = usuarioDesencriptado;
            } catch (Exception e) {
                ctx.removeCookie("remember_me");
            }
        }
        if (usuarioSesion == null || !usuarioSesion.equals("admin")) {
            ctx.redirect("/login");
        }
    }

    public static void mostrarFormulario(Context ctx) {
        String html = """
            <div class="row justify-content-center mt-5">
                <div class="col-md-4 card p-4 shadow-sm bg-white">
                    <h3 class="text-center mb-3">Acceso Admin</h3>
                    <form action="/login" method="POST">
                        <div class="mb-3"><label>Usuario</label><input type="text" name="usuario" class="form-control" required></div>
                        <div class="mb-3"><label>Contraseña</label><input type="password" name="password" class="form-control" required></div>
                        <div class="form-check mb-3">
                            <input class="form-check-input" type="checkbox" name="recordar" id="recordar">
                            <label class="form-check-label" for="recordar">Recordar mi usuario</label>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">Ingresar</button>
                    </form>
                </div>
            </div>
        """;
        ctx.html(LayoutHelper.render("Login", html, ctx));
    }

    public static void procesarLogin(Context ctx) {
        String u = ctx.formParam("usuario");
        String p = ctx.formParam("password");
        boolean recordar = ctx.formParam("recordar") != null;

        EntityManager em = GestorDb.getEntityManager();
        Usuario usuario = em.find(Usuario.class, u);
        em.close();

        if (usuario != null && usuario.password.equals(p)) {
            ctx.sessionAttribute("usuario", u);
            if (recordar) {
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(SECRET_KEY);
                ctx.cookie("remember_me", textEncryptor.encrypt(u), 604800);
            }
            registrarLoginCockroachDB(u);
            ctx.redirect("/admin/productos");
        } else {
            ctx.redirect("/login");
        }
    }

    private static void registrarLoginCockroachDB(String usuario) {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        if (dbUrl != null && !dbUrl.isEmpty()) {
            try (Connection conn = DriverManager.getConnection(dbUrl)) {
                String createTable = "CREATE TABLE IF NOT EXISTS login_logs (id SERIAL PRIMARY KEY, usuario VARCHAR(50), fecha TIMESTAMP)";
                conn.createStatement().execute(createTable);
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO login_logs (usuario, fecha) VALUES (?, ?)")) {
                    pstmt.setString(1, usuario);
                    pstmt.setTimestamp(2, new java.sql.Timestamp(new Date().getTime()));
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println("Error JDBC CockroachDB: " + e.getMessage());
            }
        }
    }

    public static void cerrarSesion(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.removeCookie("remember_me");
        ctx.redirect("/");
    }
}