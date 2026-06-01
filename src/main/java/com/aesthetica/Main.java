package com.aesthetica;

import com.aesthetica.config.AppConfig;
import com.aesthetica.listener.ContextPathListener;
import com.aesthetica.middleware.AuthAccessFilter;
import com.aesthetica.util.HibernateUtil;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.SessionFactory;

import java.io.File;

public class Main {

    private static final String CONTEXT_PATH = "/aesthetica";

    public static void main(String[] args) {
        // Do not force Hibernate initialization at startup. Initializing the
        // SessionFactory here causes the JVM to fail if the DB is unreachable.
        // Hibernate will initialize lazily when first used by the application.

        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.getConnector();

            Context context = tomcat.addWebapp(CONTEXT_PATH, new File("src/main/webapp").getAbsolutePath());
            Tomcat.addServlet(context, "JerseyServlet", new ServletContainer(new AppConfig()));
            context.addServletMappingDecoded("/api/*", "JerseyServlet");

            context.addApplicationListener(ContextPathListener.class.getName());
            context.addApplicationListener(AuthAccessFilter.class.getName());

            tomcat.start();
            System.out.println("App URL: http://localhost:8080" + CONTEXT_PATH);
            tomcat.getServer().await();
            // Attempt to open a session only if the SessionFactory could be built.
            SessionFactory sf = HibernateUtil.getSessionFactory();
            if (sf == null) {
                System.out.println("Hibernate SessionFactory not available; skipping DB session open.");
            }

        } catch (LifecycleException e) {
            throw new RuntimeException("Tomcat Embedded Server loading failed: " + e.getMessage());
        }

    }
}
