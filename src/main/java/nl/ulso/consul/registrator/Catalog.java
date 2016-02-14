package nl.ulso.consul.registrator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static nl.ulso.consul.registrator.GoDuration.requireValidDuration;

class Catalog {

    private static final String DEFAULT_DELAY = "3s";

    private final String delay;
    private final Set<Service> services;
    private Map<String, String> keyValuePairs;

    private Catalog(Builder builder) {
        this.delay = builder.delay;
        this.services = unmodifiableSet(new HashSet<>(builder.services));
        this.keyValuePairs = unmodifiableMap(new HashMap<>(builder.keyValuePairs));
    }

    String getDelay() {
        return delay;
    }

    Set<Service> getServices() {
        return services;
    }

    Map<String, String> getKeyValuePairs() {
        return keyValuePairs;
    }

    static Builder newCatalog() {
        return new Builder();
    }

    static class Builder extends AbstractBuilder {

        private String delay = DEFAULT_DELAY;
        private final Set<Service> services = new HashSet<>();
        private final Map<String, String> keyValuePairs = new HashMap<>();

        Builder withDelay(String delay) {
            final String value = substituteEnvironmentVariables(delay);
            requireValidDuration(value);
            this.delay = value;
            return this;
        }

        Service.Builder newService() {
            return Service.newService(this);
        }

        Builder addService(Service service) {
            services.add(service);
            return this;
        }

        Builder withKeyValuePair(String key, String value) {
            String processedKey = substituteEnvironmentVariables(key);
            if (processedKey.startsWith("/")) {
                processedKey = processedKey.substring(1);
            }
            keyValuePairs.put(substituteEnvironmentVariables(processedKey), substituteEnvironmentVariables(value));
            return this;
        }

        Catalog build() {
            if (services.isEmpty()) {
                throw new RegistratorException("No services are defined in the catalog.");
            }
            return new Catalog(this);
        }
    }

}
