package nl.ulso.consul.registrator;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static nl.ulso.consul.registrator.GoDuration.requireValidDuration;

class Catalog {

    private static final String DEFAULT_DELAY = "3s";

    private final String delay;
    private final Set<Service> services;

    private Catalog(Builder builder) {
        this.delay = builder.delay;
        this.services = unmodifiableSet(new HashSet<>(builder.services));
    }

    String getDelay() {
        return delay;
    }

    Set<Service> getServices() {
        return services;
    }

    static Builder newCatalog() {
        return new Builder();
    }

    static class Builder extends AbstractBuilder {

        private String delay = DEFAULT_DELAY;
        private final Set<Service> services = new HashSet<>();

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

        Catalog build() {
            if (services.isEmpty()) {
                throw new RegistratorException("No services are defined in the catalog.");
            }
            return new Catalog(this);
        }
    }

}
