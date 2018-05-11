package org.pricelessfestival.crossoff.server.api;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by ivan on 4/30/18.
 */
public class TimeUtil {

    public static String formatTimestamp(Instant timestamp, ZoneId zone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MM/dd/yy h:mm a")
                .withLocale( Locale.US ).withZone(zone);
        return formatter.format(timestamp);
    }

    public static String formatDuration(Duration duration) {
        if (duration.minusMinutes(1).isNegative()) {
            long seconds = duration.getSeconds();
            return String.format("%d second%s", seconds, seconds == 1 ? "":"s");
        } else if (duration.minusHours(1).isNegative()) {
            long minutes = duration.getSeconds() / 60;
            return String.format("%d minute%s", minutes, minutes == 1 ? "":"s");
        } else {
            long hours = duration.getSeconds() / 3600;
            return String.format("%d hour%s", hours, hours == 1 ? "":"s");
        }
    }
}
