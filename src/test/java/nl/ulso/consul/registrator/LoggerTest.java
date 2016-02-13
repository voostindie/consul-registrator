package nl.ulso.consul.registrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static nl.ulso.consul.registrator.Logger.Level.*;
import static nl.ulso.consul.registrator.Logger.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class LoggerTest {

    private ByteArrayOutputStream output;

    @Before
    public void setUp() throws Exception {
        output = new ByteArrayOutputStream();
        setOut(new PrintStream(output));
    }

    @After
    public void tearDown() throws Exception {
        output = null;
    }

    private String getLog() throws Exception {
        output.flush();
        return output.toString();
    }

    @Test
    public void applicationNameIsPresent() throws Exception {
        setLogLevel(INFO);
        info("MARK");
        assertThat(getLog(), containsString("CONSUL-AGENT"));
        assertThat(getLog(), endsWith("- MARK" + System.getProperty("line.separator")));
    }

    @Test
    public void nothingGetsLoggedWhenDisabled() throws Exception {
        setLogLevel(SILENT);
        debug("DEBUG");
        info("INFO");
        assertThat(getLog(), is(""));
    }

    @Test
    public void debugIsMissingOnInfo() throws Exception {
        setLogLevel(INFO);
        debug("DEBUG");
        info("INFO");
        assertThat(getLog(), not(containsString("DEBUG")));
        assertThat(getLog(), containsString("INFO"));
    }

    @Test
    public void everythingIsPresentOnDebug() throws Exception {
        setLogLevel(DEBUG);
        debug("DEBUG");
        info("INFO");
        assertThat(getLog(), containsString("DEBUG"));
        assertThat(getLog(), containsString("INFO"));
    }

    @Test
    public void errorIsAlwaysPresent() throws Exception {
        setLogLevel(SILENT);
        error("ERROR!");
        assertThat(getLog(), containsString("ERROR!"));
    }
}