package de.greencity.bladenightapp.android.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class DateFormatter {

    public DateFormatter() {
        this.formatter = getDestinationDateFormatter(Locale.getDefault());
    }

    private static DateTimeFormatter getDestinationDateFormatter(Locale locale) {
        String country = locale.getISO3Country();
        String localString = locale.toString();
        if ( localString.startsWith("de") ||  "DEU".equals(country) ) {
            return DateTimeFormat.forPattern("dd. MMM YY, HH:mm").withLocale(locale);
        }
        if ( localString.startsWith("fr") ||  "FRA".equals(country) ) {
            return DateTimeFormat.forPattern("dd MMM YY, HH:mm").withLocale(locale);
        }
        if ( localString.startsWith("en") ||  "USA".equals(country) ) {
            return DateTimeFormat.forStyle("MS").withLocale(locale);
        }
        else {
            return DateTimeFormat.forStyle("MS").withLocale(locale);
        }
    }

    public String format(DateTime dateTime) {
        return formatter.print(dateTime);
    }

    private DateTimeFormatter formatter;
}
