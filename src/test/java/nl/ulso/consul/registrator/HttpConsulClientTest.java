package nl.ulso.consul.registrator;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpConsulClientTest {
    @Rule
    public WireMockRule consulAgent = new WireMockRule(8500);

    private ConsulClient client = new HttpConsulClient();

    @Test
    public void registerServiceSuccess() throws Exception {
        consulAgent.stubFor(put(urlEqualTo("/v1/agent/service/register"))
                .willReturn(aResponse()));
        final Catalog catalog = Catalog.newCatalog()
                .newService().withName("foo").withPort("7000").withHttpCheckUrl("bar").build()
                .build();
        final Service service = catalog.getServices().iterator().next();
        client.register(service);
    }

    @Test(expected = RegistratorException.class)
    public void registerServiceFailure() throws Exception {
        consulAgent.stubFor(put(urlEqualTo("/v1/agent/service/register"))
                .willReturn(aResponse().withStatus(409)));
        final Catalog catalog = Catalog.newCatalog()
                .newService().withName("foo").withPort("7000").withHttpCheckUrl("bar").build()
                .build();
        final Service service = catalog.getServices().iterator().next();
        client.register(service);
    }

    @Test
    public void deregisterServiceSuccess() throws Exception {
        consulAgent.stubFor(get(urlEqualTo("/v1/agent/service/deregister/foo")).willReturn(aResponse()));
        client.deregister("foo");
    }

    @Test(expected = RegistratorException.class)
    public void deregisterServiceFailure() throws Exception {
        consulAgent.stubFor(get(urlEqualTo("/v1/agent/service/deregister/foo"))
                .willReturn(aResponse().withStatus(409)));
        client.deregister("foo");
    }

    @Test(expected = RegistratorException.class)
    public void timeout() throws Exception {
        consulAgent.stubFor(get(urlEqualTo("/v1/agent/service/deregister/foo"))
                .willReturn(aResponse().withFixedDelay(1000)));
        client.deregister("foo");
    }
}