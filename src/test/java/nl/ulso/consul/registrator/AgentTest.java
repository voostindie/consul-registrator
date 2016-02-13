package nl.ulso.consul.registrator;

import org.junit.Test;

import java.util.Map;

import static nl.ulso.consul.registrator.Logger.Level.DEBUG;
import static nl.ulso.consul.registrator.Logger.Level.INFO;
import static nl.ulso.consul.registrator.Logger.Level.SILENT;
import static nl.ulso.consul.registrator.Logger.getLogLevel;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AgentTest {

    @Test
    public void emptyArguments() throws Exception {
        final Map<String, String> map = Agent.parseArguments(null);
        assertNotNull(map);
        assertThat(map.isEmpty(), is(true));
    }

    @Test
    public void oneArgument() throws Exception {
        final Map<String, String> map = Agent.parseArguments("logger=debug");
        assertThat(map.get("logger"), is("debug"));
    }

    @Test
    public void multipleArguments() throws Exception {
        final Map<String, String> map = Agent.parseArguments("logger=debug;foo=bar;bar=baz");
        assertThat(map.get("logger"), is("debug"));
        assertThat(map.get("foo"), is("bar"));
        assertThat(map.get("bar"), is("baz"));
    }

    @Test
    public void lowerCaseArguments() throws Exception {
        final Map<String, String> map = Agent.parseArguments("LOGGER=DEBUG");
        assertThat(map.get("logger"), is("debug"));
    }

    @Test
    public void verboseLogging() throws Exception {
        Agent.configureLogging("debug");
        assertThat(getLogLevel(), is(DEBUG));
    }

    @Test
    public void silentLogging() throws Exception {
        Agent.configureLogging("silent");
        assertThat(getLogLevel(), is(SILENT));
    }

    @Test
    public void defaultLogging() throws Exception {
        Agent.configureLogging("whatever...");
        assertThat(getLogLevel(), is(INFO));
    }
}