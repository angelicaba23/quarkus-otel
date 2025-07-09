package org.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;

@Path("/hello")
public class HelloResource {

    private static final Logger LOG = Logger.getLogger(HelloResource.class);

    @GET
    public String hello() {
        LOG.info("Handling /hello request");
        return "Hello from Quarkus with OpenTelemetry!";
    }
}
