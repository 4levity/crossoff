package org.pricelessfestival.crossoff.server;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

/**
 * Created by ivan on 4/30/18.
 */
public class TimeUtilTests extends CrossoffTests {

    @Test
    public void testFormatTimestamp() {
        assertEquals("Fri 02/15/13 3:20 AM", TimeUtil.formatTimestamp(Instant.ofEpochMilli(1360898400000L), ZoneId.of("UTC")));
        assertEquals("Wed 09/11/13 8:07 PM", TimeUtil.formatTimestamp(Instant.ofEpochMilli(1378930048680L), ZoneId.of("UTC")));
    }

    @Test
    public void testFormatDuration() {
        Duration d = Duration.ZERO;
        assertEquals("0 seconds", TimeUtil.formatDuration(d));
        d = Duration.ofMillis(999);
        assertEquals("0 seconds", TimeUtil.formatDuration(d));
        d = Duration.ofMillis(3999);
        assertEquals("3 seconds", TimeUtil.formatDuration(d));
        d = Duration.ofMinutes(1);
        assertEquals("1 minute", TimeUtil.formatDuration(d));
        d = Duration.ofMinutes(59);
        assertEquals("59 minutes", TimeUtil.formatDuration(d));
        d = Duration.ofMinutes(60);
        assertEquals("1 hour", TimeUtil.formatDuration(d));
        d = Duration.ofHours(23);
        assertEquals("23 hours", TimeUtil.formatDuration(d));
        d = Duration.ofHours(72);
        assertEquals("72 hours", TimeUtil.formatDuration(d));
    }
}
