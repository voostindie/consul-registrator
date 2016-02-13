package nl.ulso.consul.registrator;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static nl.ulso.consul.registrator.Logger.Level.*;
import static nl.ulso.consul.registrator.Logger.debug;
import static nl.ulso.consul.registrator.Logger.setLogLevel;
import static nl.ulso.consul.registrator.Registrator.runAndExitOnException;

public class Agent {

    public static void premain(String arguments) {
        Map<String, String> options = parseArguments(arguments);
        configureLogging(options.getOrDefault("logger", "info"));

        debug("Starting Consul registrator agent");

        final Registrator registrator = new Registrator(
                new XmlClasspathCatalogLoader(),
                new HttpConsulClient()
        );
        runAndExitOnException(registrator::install);
    }

    static Map<String, String> parseArguments(String arguments) {
        if (arguments == null) {
            return emptyMap();
        }
        final HashMap<String, String> map = new HashMap<>();
        final String[] options = arguments.split(";");
        for (String option : options) {
            final String[] pair = option.split("=");
            if (pair.length != 2) {
                continue;
            }
            map.put(pair[0].toLowerCase(), pair[1].toLowerCase());
        }
        return unmodifiableMap(map);
    }

    static void configureLogging(String level) {
        switch (level) {
            case "debug":
                setLogLevel(DEBUG);
                break;
            case "silent":
                setLogLevel(SILENT);
                break;
            default:
                setLogLevel(INFO);
        }
    }
}
