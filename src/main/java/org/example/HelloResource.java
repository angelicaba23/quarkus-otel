package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EnrichmentResource {

    private static final Logger LOG = Logger.getLogger(EnrichmentResource.class);
    private static final String[] FILES = {
        "dt_metadata_e617c525669e072eebe3d0f08212e8f2.json",
        "/var/lib/dynatrace/enrichment/dt_metadata.json",
        "/var/lib/dynatrace/enrichment/dt_host_metadata.json"
    };

    private Map<String, Object> enrichAttrs = new HashMap<>();

    public EnrichmentResource() {
        ObjectMapper mapper = new ObjectMapper();
        for (String path : FILES) {
            try {
                Map<String, Object> data = mapper.readValue(new File(path), Map.class);
                enrichAttrs.putAll(data);
                LOG.info("🟢 Cargado JSON desde " + path + ": " + data.size() + " atributos");
            } catch (Exception e) {
                LOG.debug("🔵 No se encontró o no se pudo leer: " + path);
            }
        }
    }

    @Inject
    Resource enrichedResource;

    /**
     * Observador que se ejecuta durante el arranque de la app.
     * Aquí imprimimos los atributos cargados en consola.
     */
    void onStart(@Observes StartupEvent ev) {
        if (enrichAttrs.isEmpty()) {
            LOG.warn("⚠️ No se cargaron atributos de enriquecimiento.");
        } else {
            LOG.info("🔍 Atributos de enriquecimiento encontrados al iniciar:");
            enrichAttrs.forEach((k, v) -> LOG.infof("   %s = %s", k, v));
        }
    }

    @Produces
    @ApplicationScoped
    public Resource enrichedResource() {
        Attributes.Builder builder = Attributes.builder();
        enrichAttrs.forEach((k, v) -> builder.put(k, v == null ? "" : v.toString()));
        return Resource.create(builder.build());
    }
}
