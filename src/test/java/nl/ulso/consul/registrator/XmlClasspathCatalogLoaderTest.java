package nl.ulso.consul.registrator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class XmlClasspathCatalogLoaderTest {

    @Test
    public void testMinimalCatalog() throws Exception {
        Catalog catalog = load("minimal-catalog.xml");
        assertNotNull(catalog);
        assertThat(catalog.getDelay(), is("3s"));
        assertThat(catalog.getServices().size(), is(1));
        final Service service = findService(catalog, "myservice");
        assertNotNull(service);
    }

    @Test
    public void testComplexCatalog() throws Exception {
        Catalog catalog = load("complex-catalog.xml");
        assertNotNull(catalog);
        assertThat(catalog.getDelay(), is("0s"));
        assertThat(catalog.getServices().size(), is(3));
        final Service service1 = findService(catalog, "service1");
        assertNotNull(service1);
        final Service service2 = findService(catalog, "service2");
        assertNotNull(service2);
        final Service service3 = findService(catalog, "service3");
        assertNotNull(service3);
    }

    private Service findService(Catalog catalog, String name) {
        return catalog.getServices().stream().filter(s -> s.getId().startsWith(name)).findFirst().get();
    }

    private Catalog load(String filename) {
        return new XmlClasspathCatalogLoader("catalogs/" + filename).load();
    }
}