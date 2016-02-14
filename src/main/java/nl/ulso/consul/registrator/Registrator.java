package nl.ulso.consul.registrator;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static nl.ulso.consul.registrator.GoDuration.toMilliseconds;
import static nl.ulso.consul.registrator.Logger.debug;
import static nl.ulso.consul.registrator.Logger.info;

/**
 * Core orchestration class in the agent, taking care of loading the Consul catalog from the application, registering
 * services and keys with Consul after a configurable delay and installing a shutdown hook to remove everything that was
 * stored in Consul on startup.
 */
class Registrator {

    private final CatalogLoader catalogLoader;
    private final ConsulClient consulClient;

    public Registrator(CatalogLoader catalogLoader, ConsulClient consulClient) {
        this.catalogLoader = catalogLoader;
        this.consulClient = consulClient;
    }

    void install() throws RegistratorException {
        debug("Loading service catalog from application");
        final Catalog catalog = catalogLoader.load();

        debug("Scheduling service registration in %s", catalog.getDelay());
        new Timer(false).schedule(new TimerTask() {
            @Override
            public void run() {
                runAndExitOnException(this::registerWithConsul);
                // The shutdown hook should only be installed if service registration completed.
                // Which is why we do it here:
                debug("Registering shutdown hook for service deregistration");
                Runtime.getRuntime().addShutdownHook(new ShutdownHook(consulClient, catalog));
            }

            private void registerWithConsul() {
                registerServices();
                registerKeyValuePairs();
            }

            private void registerServices() {
                catalog.getServices().stream()
                        .peek(s -> info("Registering service '%s' with Consul", s.getId()))
                        .forEach(consulClient::register);
            }

            private void registerKeyValuePairs() {
                catalog.getKeyValuePairs().entrySet().stream()
                        .peek(e -> info("Storing key '%s' with value '%s' in Consul", e.getKey(), e.getValue()))
                        .forEach(e -> consulClient.storeKeyValue(e.getKey(), e.getValue()));
            }

        }, toMilliseconds(catalog.getDelay()));
    }

    static void runAndExitOnException(Runnable runnable) {
        try {
            runnable.run();
        } catch (RegistratorException e) {
            final String message = e.getMessage();
            Logger.error("%s", message);
            Logger.error("Exiting application.");
            System.err.println("Exiting application due to error: " + message);
            System.exit(-1);
        }
    }

    private static class ShutdownHook extends Thread {
        private final ConsulClient consulClient;
        private final Set<String> serviceIds;
        private final Set<String> keys;

        public ShutdownHook(ConsulClient consulClient, Catalog catalog) {
            // The only thing the agent keeps in the memory application are the Consul client,
            // the service IDs and the keys. All other things (the rest of the catalog) may be garbage collected.
            this.consulClient = consulClient;
            this.serviceIds = extractServiceIds(catalog);
            this.keys = extractKeys(catalog);
        }

        private Set<String> extractServiceIds(Catalog catalog) {
            return catalog.getServices().stream().map(Service::getId).collect(Collectors.toSet());
        }

        private Set<String> extractKeys(Catalog catalog) {
            return catalog.getKeyValuePairs().keySet().stream().collect(Collectors.toSet());
        }

        @Override
        public void run() {
            runAndExitOnException(this::deregisterFromConsul);
        }

        private void deregisterFromConsul() {
            deregisterKeys();
            deregisterServices();
        }

        private void deregisterServices() {
            serviceIds.stream()
                    .peek(s -> info("Deregistering service '%s' from Consul", s))
                    .forEach(consulClient::deregister);
        }

        private void deregisterKeys() {
            keys.stream()
                    .peek(k -> info("Removing key '%s' from Consul", k))
                    .forEach(consulClient::removeKey);
        }
    }
}
