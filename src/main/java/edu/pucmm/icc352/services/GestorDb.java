package edu.pucmm.icc352.services;
import edu.pucmm.icc352.models.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GestorDb {
    private static EntityManagerFactory emf;

    public static void iniciarEntityFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("MiTienditaPU");
            crearDatosBase();
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null) iniciarEntityFactory();
        return emf.createEntityManager();
    }

    private static void crearDatosBase() {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        if (em.find(Usuario.class, "admin") == null) {
            em.persist(new Usuario("admin", "Administrador", "admin"));
        }
        em.getTransaction().commit();
        em.close();
    }
}