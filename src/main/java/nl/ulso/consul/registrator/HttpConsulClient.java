package nl.ulso.consul.registrator;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;
import static nl.ulso.consul.registrator.Logger.debug;

class HttpConsulClient implements ConsulClient {

    private static final int CONNECT_TIMEOUT_MILLISECONDS = 500;
    private static final int READ_TIMEOUT_MILLISECONDS = 500;
    private static final String CONSUL_AGENT_URL = "http://localhost:8500";
    private static final String DEREGISTRATION_URL = "/v1/agent/service/deregister/";
    private static final String REGISTRATION_URL = "/v1/agent/service/register";
    private static final String KEY_VALUE_URL = "/v1/kv/";

    @Override
    public void register(Service service) {
        withConnection(CONSUL_AGENT_URL + REGISTRATION_URL, (connection) -> {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            try (PrintWriter writer = new PrintWriter(connection.getOutputStream())) {
                final String json = service.toConsulRegistrationJson();
                debug("Payload for Consul registration: '%s'", json);
                writer.print(json);
            }
        });
    }

    @Override
    public void deregister(String serviceId) {
        withConnection(CONSUL_AGENT_URL + DEREGISTRATION_URL + serviceId, (connection) -> {
            // Actually, there's nothing to do here, because it's a plain GET.
        });
    }

    @Override
    public void storeKeyValue(String key, String value) {
        withConnection(CONSUL_AGENT_URL + KEY_VALUE_URL + key, (connection) -> {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            try (PrintWriter writer = new PrintWriter(connection.getOutputStream())) {
                debug("Payload for Consul key storage: '%s'", value);
                writer.print(value);
            }
        });
    }

    @Override
    public void removeKey(String key) {
        withConnection(CONSUL_AGENT_URL + KEY_VALUE_URL + key, (connection) -> connection.setRequestMethod("DELETE"));
    }

    private void withConnection(String url, HttpAction action) {
        final HttpURLConnection connection;
        try {
            debug("Connecting to Consul URL '%s'", url);
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLISECONDS);
            connection.setReadTimeout(READ_TIMEOUT_MILLISECONDS);
            action.accept(connection);
            connection.disconnect();
            final int responseCode = connection.getResponseCode();
            debug("Consul response code: %s", responseCode);
            if (HTTP_OK != responseCode) {
                throw new RegistratorException("Consul service call to '" + url
                        + "' responded with error: " + responseCode);
            }
        } catch (IOException e) {
            throw new RegistratorException("Could not connect to Consul agent on " + CONSUL_AGENT_URL
                    + ". Cause: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface HttpAction {
        void accept(HttpURLConnection connection) throws IOException;
    }
}
