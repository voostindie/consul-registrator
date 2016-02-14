package nl.ulso.consul.registrator;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class CatalogTest {

    @Test
    public void minimalCatalog() throws Exception {
        final Catalog catalog = Catalog.newCatalog()
                .newService()
                .withName("foo")
                .withPort("7000")
                .withHttpCheckUrl("bar")
                .build()
                .build();
        assertNotNull(catalog);
        final Service service = findService(catalog, "foo");
        final JsonObject object = toJson(service);
        assertThat(object.getString("Name"), is("foo"));
        assertThat(object.getString("ID"), startsWith("foo:"));
        assertThat(object.getString("Address", "N/A"), is("N/A"));
        assertThat(object.getInt("Port"), is(7000));
        assertThat(object.getJsonObject("Check").getString("HTTP"), is("bar"));
        assertThat(object.getJsonObject("Check").getString("Interval"), is("5s"));
        assertNull(object.getJsonArray("Tags"));
    }

    @Test
    public void completeService() throws Exception {
        final Catalog catalog = Catalog.newCatalog()
                .newService()
                .withName("foo")
                .withId("bar")
                .withPort("8000")
                .withAddress("127.0.0.1")
                .withHttpCheckUrl("bar")
                .withHttpCheckInterval("1m")
                .withTag("master")
                .withTag("v1")
                .build()
                .build();
        assertNotNull(catalog);
        final Service service = findService(catalog, "bar");
        final JsonObject object = toJson(service);
        assertThat(object.getString("Name"), is("foo"));
        assertThat(object.getString("ID"), is("bar"));
        assertThat(object.getString("Address"), is("127.0.0.1"));
        assertThat(object.getInt("Port"), is(8000));
        assertThat(object.getJsonObject("Check").getString("HTTP"), is("bar"));
        assertThat(object.getJsonObject("Check").getString("Interval"), is("1m"));
        assertThat(object.getJsonArray("Tags").size(), is(2));
        assertThat(object.getJsonArray("Tags").getString(0), is("v1"));
        assertThat(object.getJsonArray("Tags").getString(1), is("master"));
    }

    @Test(expected = RegistratorException.class)
    public void emptyCatalog() throws Exception {
        Catalog.newCatalog().build();
    }

    @Test(expected = RegistratorException.class)
    public void missingServiceName() throws Exception {
        Catalog.newCatalog()
                .newService()
                .withPort("7000")
                .withHttpCheckUrl("bar")
                .build()
                .build();
    }

    @Test(expected = RegistratorException.class)
    public void missingServicePort() throws Exception {
        Catalog.newCatalog()
                .newService()
                .withName("name")
                .withHttpCheckUrl("bar")
                .build()
                .build();
    }

    @Test(expected = RegistratorException.class)
    public void missingServiceHttpCheckUrl() throws Exception {
        Catalog.newCatalog()
                .newService()
                .withName("name")
                .withPort("7000")
                .build()
                .build();
    }

    @Test(expected = RegistratorException.class)
    public void invalidServicePort() throws Exception {
        Catalog.newCatalog()
                .newService()
                .withPort("NaN")
                .withName("name")
                .withHttpCheckUrl("bar")
                .build()
                .build();
    }

    @Test(expected = RegistratorException.class)
    public void illegalServicePort() throws Exception {
        Catalog.newCatalog()
                .newService()
                .withPort("80")
                .withName("name")
                .withHttpCheckUrl("bar")
                .build()
                .build();
    }

    @Test
    public void completeServiceWithEnvironmentVariables() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("DELAY", "10s");
        map.put("NAME", "foo");
        map.put("ID", "bar");
        map.put("ADDRESS", "127.0.0.1");
        map.put("PORT", "8000");
        map.put("URL", "bar");
        map.put("INTERVAL", "1m");
        map.put("TAG", "master");
        map.put("KEY1_NAME", "key1");
        map.put("KEY1_VALUE", "value1");
        map.put("KEY2_NAME", "/key2");
        map.put("KEY2_VALUE", "value2");
        AbstractBuilder.setEnvironmentForTesting(map);
        final Catalog catalog = Catalog.newCatalog()
                .newService()
                .withName("${NAME}")
                .withId("${ID}")
                .withAddress("${ADDRESS}")
                .withPort("${PORT}")
                .withHttpCheckUrl("${URL}")
                .withHttpCheckInterval("${INTERVAL}")
                .withTag("${TAG}")
                .build()
                .withDelay("${DELAY}")
                .withKeyValuePair("${KEY1_NAME}", "${KEY1_VALUE}")
                .withKeyValuePair("${KEY2_NAME}", "${KEY2_VALUE}")
                .build();
        assertNotNull(catalog);
        assertThat(catalog.getDelay(), is("10s"));
        assertThat(catalog.getKeyValuePairs().get("key1"), is("value1"));
        assertThat(catalog.getKeyValuePairs().get("key2"), is("value2"));
        final Service service = findService(catalog, "bar");
        final JsonObject object = toJson(service);
        assertThat(object.getString("Name"), is("foo"));
        assertThat(object.getString("ID"), is("bar"));
        assertThat(object.getString("Address"), is("127.0.0.1"));
        assertThat(object.getInt("Port"), is(8000));
        assertThat(object.getJsonObject("Check").getString("HTTP"), is("bar"));
        assertThat(object.getJsonObject("Check").getString("Interval"), is("1m"));
        assertThat(object.getJsonArray("Tags").size(), is(1));
        assertThat(object.getJsonArray("Tags").getString(0), is("master"));
    }

    private Service findService(Catalog catalog, String name) {
        return catalog.getServices().stream().filter(s -> s.getId().startsWith(name)).findFirst().get();
    }

    private JsonObject toJson(Service service) {
        JsonReader reader = Json.createReader(new StringReader(service.toConsulRegistrationJson()));
        return (JsonObject) reader.read();
    }
}