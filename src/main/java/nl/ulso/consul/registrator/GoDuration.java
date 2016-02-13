package nl.ulso.consul.registrator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * See <a href="https://golang.org/pkg/time/#ParseDuration">Go's ParseDuration</a>.
 * <p>
 * Two differences to the Go implementation:
 * </p>
 * <ul>
 * <li>Negative values are not supported.</li>
 * <li>Nanoseconds (ns) and microseconds (us) are not supported.</li>
 * </ul>
 */
class GoDuration {

    private static final Pattern DURATION_PATTERN = Pattern.compile("((\\d+(\\.\\d+)?)(ms|s|m|h))+");

    static void requireValidDuration(String durationString) {
        if (!isValidDuration(durationString)) {
            throw new RegistratorException("Invalid duration: " + durationString);
        }
    }

    static boolean isValidDuration(String durationString) {
        return DURATION_PATTERN.matcher(durationString.toLowerCase()).matches();
    }

    static long toMilliseconds(String durationString) {
        long total = 0L;
        final Matcher matcher = DURATION_PATTERN.matcher(durationString.toLowerCase());
        while (matcher.find()) {
            total += computeDuration(matcher.group(2), matcher.group(4));
        }
        return total;
    }

    private static long computeDuration(String decimals, String unitSuffix) {
        final double duration = Double.parseDouble(decimals);
        final double multiplier = toMultiplier(unitSuffix);
        return (long) (duration * multiplier);
    }

    private static double toMultiplier(String unitSuffix) {
        switch (unitSuffix) {
            case "ms":
                return 1.0E00;
            case "s":
                return 1.0E03;
            case "m":
                return 6.0E04;
            case "h":
                return 3.6E06;
            default:
                return 1.0d;
        }
    }
}
