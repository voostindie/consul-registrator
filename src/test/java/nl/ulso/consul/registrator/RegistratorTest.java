package nl.ulso.consul.registrator;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * This is definitely not the best of tests...
 * <p>
 * Testing challenges I still have:
 * </p>
 * <ul>
 * <li>Testing the shutdown hook. How to do that without crippling the Registrator code?</li>
 * <li>Testing exceptional situations. How to do that without shutting down the JVM?</li>
 * </ul>
 */
public class RegistratorTest {

    @Test
    public void installWithoutExceptions() throws Exception {
        final DummyConsulClient client = new DummyConsulClient();
        final Registrator registrator = new Registrator(new DummyCatalogLoader(), client);
        registrator.install();
        TimeUnit.MILLISECONDS.sleep(500); // Give the separate Thread some time to work...
        assertTrue(client.services.contains("service"));
        assertTrue(client.keys.contains("key"));
    }

    private static class DummyCatalogLoader implements CatalogLoader {
        @Override
        public Catalog load() {
            return Catalog.newCatalog()
                    .withDelay("0s")
                    .newService().withName("service").withId("service").withPort("8000").withHttpCheckUrl("localhost").build()
                    .withKeyValuePair("key", "value")
                    .build();
        }
    }

    private static class DummyConsulClient implements ConsulClient {

        Set<String> services = new HashSet<>();
        Set<String> keys = new HashSet<>();

        @Override
        public void deregister(String serviceId) {
            // Nothing to do here. Unfortunately...
        }

        @Override
        public void register(Service service) {
            services.add(service.getId());
        }

        @Override
        public void storeKeyValue(String key, String value) {
            keys.add(key);
        }

        @Override
        public void removeKey(String key) {
            // Nothing to do here. Unfortunately...
        }
    }
}