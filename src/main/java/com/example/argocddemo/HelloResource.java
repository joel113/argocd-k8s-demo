package com.example.argocddemo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@Path("/")
public class HelloResource {

    @ConfigProperty(name = "APP_ENV", defaultValue = "local")
    String env;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from ArgoCD demo (Quarkus)",
                "env", env
        );
    }
}
