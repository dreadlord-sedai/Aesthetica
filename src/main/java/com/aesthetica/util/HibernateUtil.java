package com.aesthetica.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // Lazy-initialized SessionFactory. Avoids initializing at classload time so
    // the application can start even if the DB is unavailable. First caller
    // will attempt to build the SessionFactory.
    private static volatile SessionFactory sessionFactory = null;

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Try the primary configuration (from hibernate.cfg.xml)
                sessionFactory = new Configuration().configure().buildSessionFactory();
            } catch (HibernateException e) {
                e.printStackTrace();
                System.err.println("Primary DB initialization failed. Attempting in-memory H2 fallback...");
                try {
                    Configuration fallback = new Configuration().configure();
                    // Override connection settings to use H2 in-memory database
                    fallback.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                    fallback.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                    fallback.setProperty("hibernate.connection.url", "jdbc:h2:mem:aesthetica;DB_CLOSE_DELAY=-1;MODE=MySQL");
                    fallback.setProperty("hibernate.connection.username", "sa");
                    fallback.setProperty("hibernate.connection.password", "");
                    fallback.setProperty("hibernate.hbm2ddl.auto", "create-drop");
                    sessionFactory = fallback.buildSessionFactory();
                    System.out.println("Fallback H2 SessionFactory created. Running with in-memory DB.");
                } catch (HibernateException ex) {
                    System.err.println("Fallback H2 initialization failed.");
                    ex.printStackTrace();
                    sessionFactory = null;
                }
            }
        }
        return sessionFactory;
    }

    public static void shutDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }


}
