package nl.ulso.consul.registrator;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static nl.ulso.consul.registrator.GoDuration.toMilliseconds;
import static nl.ulso.consul.registrator.Logger.debug;
import static nl.ulso.consul.registrator.Logger.info;

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
                runAndExitOnException(this::registerServices);
                // The shutdown hook should only be installed if service registration completed.
                // Which is why we do it here:
                addShutdownHook(catalog);
            }

            private void registerServices() {
                catalog.getServices().stream()
                        .peek(s -> info("Registering service %s with Consul", s.getId()))
                        .forEach(consulClient::register);
            }

        }, toMilliseconds(catalog.getDelay()));
    }

    static void runAndExitOnException(Runnable runnable) {
        try {
            runnable.run();
        } catch (RegistratorException e) {
            final String message = e.getMessage();
            Logger.error("Services could not be (de)registered with Consul");
            Logger.error("%s", message);
            Logger.error("Exiting application");
            System.err.println("Exiting application due to error: " + message);
            System.exit(-1);
        }
    }

    private void addShutdownHook(Catalog catalog) {
        debug("Registering shutdown hook for service deregistration");
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(consulClient, extractServiceIds(catalog)));
    }

    private Set<String> extractServiceIds(Catalog catalog) {
        return catalog.getServices().stream().map(Service::getId).collect(Collectors.toSet());
    }

    private static class ShutdownHook extends Thread {
        private final ConsulClient consulClient;
        private final Set<String> serviceIds;

        private ShutdownHook(ConsulClient consulClient, Set<String> serviceIds) {
            // The only thing the agent keeps in the memory application are the Consul client
            // and the service IDs. All other things (like the catalog) may be garbage collected.
            this.consulClient = consulClient;
            this.serviceIds = serviceIds;
        }

        @Override
        public void run() {
            runAndExitOnException(this::deregisterServices);
        }

        private void deregisterServices() {
            serviceIds.stream()
                    .peek(s -> info("Deregistering service %s from Consul", s))
                    .forEach(consulClient::deregister);
        }
    }
}
