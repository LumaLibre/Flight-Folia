/*
 * Flight
 * Copyright 2023 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.utils;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class TimeUtil {

    public String convertToReadableDate(long timeInMilliseconds, final String format) {
        final Date date = new Date(timeInMilliseconds);
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public String convertToReadableDate(long timeInMilliseconds) {
        return convertToReadableDate(timeInMilliseconds, "MMMM/dd/yyyy - hh:mm a");
    }

    public Map<TimeUnit, Long> getRemainingTime(final long milliseconds) {
        final Map<TimeUnit, Long> times = new HashMap<>();
        final long seconds = (milliseconds - System.currentTimeMillis()) / 1000;

        final long days = TimeUnit.SECONDS.toDays(seconds);
        final long hours = TimeUnit.SECONDS.toHours(seconds) - (days * 24L);
        final long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60L);
        final long secs = seconds - (TimeUnit.SECONDS.toMinutes(seconds) * 60L);

        times.put(TimeUnit.DAYS, days);
        times.put(TimeUnit.HOURS, hours);
        times.put(TimeUnit.MINUTES, minutes);
        times.put(TimeUnit.SECONDS, secs);

        return times;
    }

    public String getTimeStringFromMillis(final long milliseconds) {
        final Map<TimeUnit, Long> values = getRemainingTime(milliseconds);
        return String.format("%dd %dh %dm %ds", values.get(TimeUnit.DAYS), values.get(TimeUnit.HOURS), values.get(TimeUnit.MINUTES), values.get(TimeUnit.SECONDS));
    }
}
