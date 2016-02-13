package nl.ulso.consul.registrator;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class AbstractBuilderTest extends AbstractBuilder {

    @Before
    public void setUp() throws Exception {
        Map<String, String> environment = new HashMap<>();
        environment.put("ADDRESS", "192.168.1.100");
        environment.put("PORT0", "9000");
        environment.put("PORT1", "4000");
        setEnvironmentForTesting(Collections.unmodifiableMap(environment));
    }

    @Test
    public void replaceSingleEnvironmentVariable() throws Exception {
        final String result = substituteEnvironmentVariables("${PORT0}");
        assertThat(result, is("9000"));
    }

    @Test
    public void replaceEmbeddedSingleEnvironmentVariable() throws Exception {
        final String result = substituteEnvironmentVariables("http://localhost:${PORT0}/health");
        assertThat(result, is("http://localhost:9000/health"));
    }

    @Test
    public void replaceMultipleEnvironmentVariables() throws Exception {
        final String result = substituteEnvironmentVariables("${PORT0}${PORT1}");
        assertThat(result, is("90004000"));
    }

    @Test
    public void replaceMultipleEmbeddedEnvironmentVariables() throws Exception {
        final String result = substituteEnvironmentVariables("http://${ADDRESS}:${PORT1}/health");
        assertThat(result, is("http://192.168.1.100:4000/health"));
    }

    @Test
    public void replaceSingleEnvironmentVariableWithFallback() throws Exception {
        final String result = substituteEnvironmentVariables("${PORT9:2000}");
        assertThat(result, is("2000"));
    }

    @Test
    public void replaceMultipleEnvironmentVariableWithFallback() throws Exception {
        final String result = substituteEnvironmentVariables("http://${HOST:localhost}:${PORT9:2000}/health");
        assertThat(result, is("http://localhost:2000/health"));
    }

    @Test
    public void replaceEnvironmentVariablesInEmptyString() throws Exception {
        assertThat(substituteEnvironmentVariables(""), is(""));
    }

    @Test
    public void replaceEnvironmentVariablesInPlainString() throws Exception {
        assertThat(
                substituteEnvironmentVariables("No environment variables here!"),
                is("No environment variables here!"));
    }

    @Test(expected = RegistratorException.class)
    public void replaceMissingEnvironmentVariableWithoutFallback() throws Exception {
        substituteEnvironmentVariables("${PORT9}");
    }
}