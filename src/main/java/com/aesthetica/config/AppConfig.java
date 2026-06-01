package com.aesthetica.config;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {

    public AppConfig() {
        packages("com.aesthetica.controller");
        packages("com.aesthetica.middleware");
        register(MultiPartFeature.class);
    }
}