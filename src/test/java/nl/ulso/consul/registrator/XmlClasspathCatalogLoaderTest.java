package nl.ulso.consul.registrator;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class XmlClasspathCatalogLoaderTest {

    @Test
    public void minimalCatalog() throws Exception {
        Catalog catalog = load("minimal-catalog.xml");
        assertNotNull(catalog);
        assertThat(catalog.getDelay(), is("3s"));
        assertThat(catalog.getServices().size(), is(1));
        final Service service = findService(catalog, "myservice");
        assertNotNull(service);
        final String s = service.toString();
        assertThat(s, containsString("id=myservice:"));
        assertThat(s, containsString("name=myservice"));
        assertThat(s, containsString("address=null"));
        assertThat(s, containsString("port=8100"));
        assertThat(s, containsString("url=http://localhost/health"));
        assertThat(s, containsString("interval=5s")); // default
        assertThat(s, containsString("tags=[]"));
    }

    @Test
    public void minimalCatalogWithKeyValues() throws Exception {
        Catalog catalog = load("minimal-catalog-key-value-pairs.xml");
        assertNotNull(catalog);
        Map<String, String> pairs = catalog.getKeyValuePairs();
        assertThat(pairs.get("foo"), is("bar"));
    }

    @Test
    public void complexCatalog() throws Exception {
        Catalog catalog = load("complex-catalog.xml");
        assertNotNull(catalog);
        assertThat(catalog.getDelay(), is("0s"));
        assertThat(catalog.getServices().size(), is(3));
        final String service1 = findService(catalog, "service1").toString();
        assertThat(service1, containsString("id=service1:foo"));
        assertThat(service1, containsString("name=service1"));
        assertThat(service1, containsString("address=127.0.0.1"));
        assertThat(service1, containsString("port=8180"));
        assertThat(service1, containsString("url=http://localhost:8180/health"));
        assertThat(service1, containsString("interval=10s"));
        assertThat(service1, containsString("tags=[v1, master]"));
        final String service2 = findService(catalog, "service2").toString();
        assertThat(service2, containsString("id=service2:bar"));
        assertThat(service2, containsString("name=service2"));
        assertThat(service2, containsString("address=127.0.0.1"));
        assertThat(service2, containsString("port=9000"));
        assertThat(service2, containsString("url=http://localhost:9000/health"));
        assertThat(service2, containsString("interval=1m"));
        assertThat(service2, containsString("tags=[slave]"));
        final String service3 = findService(catalog, "service3").toString();
        assertThat(service3, containsString("id=service3:baz"));
        assertThat(service3, containsString("name=service3"));
        assertThat(service3, containsString("address=127.0.0.1"));
        assertThat(service3, containsString("port=9400"));
        assertThat(service3, containsString("url=http://localhost:9400/health"));
        assertThat(service3, containsString("interval=500ms"));
        assertThat(service3, containsString("tags=[]"));
    }

    @Test(expected = RegistratorException.class)
    public void missingCatalog() throws Exception {
        load("non-existing.xml");
    }

    @Test(expected = RegistratorException.class)
    public void invalidRoot() throws Exception {
        load("invalid-root.xml");
    }

    @Test(expected = RegistratorException.class)
    public void invalidXml() throws Exception {
        load("invalid-xml.xml");
    }

    private Service findService(Catalog catalog, String name) {
        return catalog.getServices().stream().filter(s -> s.getId().startsWith(name)).findFirst().get();
    }

    private Catalog load(String filename) {
        return new XmlClasspathCatalogLoader("catalogs/" + filename).load();
    }
}