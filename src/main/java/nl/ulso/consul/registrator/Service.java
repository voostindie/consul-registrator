package nl.ulso.consul.registrator;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;
import static nl.ulso.consul.registrator.GoDuration.requireValidDuration;

class Service {

    private static final String DEFAULT_HEALTH_CHECK_INTERVAL = "5s";
    private static final int MINIMUM_PORT_NUMBER = 1024;
    private static final int MAXIMUM_PORT_NUMBER = 65536;

    private final String name;
    private final String id;
    private final String address;
    private final Integer port;
    private final String url;
    private final String interval;
    private final Set<String> tags;

    private Service(Builder builder) {
        this.name = builder.name;
        this.id = builder.id != null ? builder.id : name + ":" + UUID.randomUUID().toString();
        this.address = builder.address;
        this.port = builder.port;
        this.url = builder.url;
        this.interval = builder.interval;
        this.tags = unmodifiableSet(new HashSet<>(builder.tags));
    }

    String getId() {
        return id;
    }

    static Builder newService(Catalog.Builder catalogBuilder) {
        return new Builder(catalogBuilder);
    }

    String toConsulRegistrationJson() {
        // Not the most beautiful piece of code I've ever written, but it does the job:
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        append(builder, "ID", id);
        builder.append(",");
        append(builder, "Name", name);
        builder.append(",");
        if (!tags.isEmpty()) {
            builder.append("\"Tags\":[");
            StringJoiner joiner = new StringJoiner(",");
            tags.stream().map(t -> "\"" + t + "\"").forEach(joiner::add);
            builder.append(joiner.toString());
            builder.append("],");
        }
        if (address != null) {
            append(builder, "Address", address);
            builder.append(",");
        }
        if (port != null) {
            append(builder, "Port", port);
            builder.append(",");
        }
        builder.append("\"Check\":{");
        append(builder, "HTTP", url);
        builder.append(",");
        append(builder, "Interval", interval);
        builder.append("}");
        builder.append("}");
        return builder.toString();
    }

    private void append(StringBuilder builder, String key, String value) {
        builder.append("\"");
        builder.append(key);
        builder.append("\":\"");
        builder.append(value);
        builder.append("\"");
    }

    private void append(StringBuilder builder, String key, Integer value) {
        builder.append("\"");
        builder.append(key);
        builder.append("\":");
        builder.append(value);
        builder.append("");
    }

    static class Builder {

        private final Catalog.Builder catalogBuilder;
        private String name;
        private String id;
        private String url;
        private String address;
        private Integer port;
        private String interval = DEFAULT_HEALTH_CHECK_INTERVAL;
        private Set<String> tags = new HashSet<>();

        private Builder(Catalog.Builder catalogBuilder) {
            this.catalogBuilder = catalogBuilder;
        }

        Builder withName(String name) {
            this.name = name;
            return this;
        }

        Builder withId(String id) {
            this.id = id;
            return this;
        }

        void withAddress(String address) {
            this.address = address;
        }

        void withPort(String port) {
            try {
                this.port = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new RegistratorException("Invalid port: " + port);
            }
            if (this.port < MINIMUM_PORT_NUMBER || this.port > MAXIMUM_PORT_NUMBER) {
                throw new RegistratorException("Invalid port: " + port);
            }
        }

        Builder withHttpCheckUrl(String url) {
            this.url = url;
            return this;
        }

        Builder withHttpCheckInterval(String interval) {
            requireValidDuration(interval);
            this.interval = interval;
            return this;
        }

        void withTag(String tag) {
            tags.add(tag);
        }

        Catalog.Builder build() {
            if (name == null) {
                throw new RegistratorException("Required service name is missing");
            }
            if (url == null) {
                throw new RegistratorException("Required HTTP check URL is missing");
            }
            return catalogBuilder.addService(new Service(this));
        }
    }
}
