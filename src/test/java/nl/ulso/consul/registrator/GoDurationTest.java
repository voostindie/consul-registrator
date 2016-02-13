package nl.ulso.consul.registrator;

import org.junit.Test;

import static nl.ulso.consul.registrator.GoDuration.isValidDuration;
import static nl.ulso.consul.registrator.GoDuration.toMilliseconds;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GoDurationTest {

    @Test
    public void positiveDurationIsValid() throws Exception {
        assertTrue(isValidDuration("5s"));
    }

    @Test
    public void negativeDurationIsInvalid() throws Exception {
        assertFalse(isValidDuration("-5s"));
    }

    @Test
    public void durationWithFractionIsValid() throws Exception {
        assertTrue(isValidDuration("1.5s"));
    }

    @Test
    public void combinationOfDurationsIsValid() throws Exception {
        assertTrue(isValidDuration("100ms5s42m1.5h"));
    }

    @Test
    public void millisecondsToMilliseconds() throws Exception {
        assertThat(toMilliseconds("42ms"), is(42L));
    }

    @Test
    public void secondsToMilliseconds() throws Exception {
        assertThat(toMilliseconds("42s"), is(42000L));
    }

    @Test
    public void minutesToMilliseconds() throws Exception {
        assertThat(toMilliseconds("42m"), is(2520000L));
    }

    @Test
    public void hoursToMilliseconds() throws Exception {
        assertThat(toMilliseconds("42h"), is(151200000L));
    }
}