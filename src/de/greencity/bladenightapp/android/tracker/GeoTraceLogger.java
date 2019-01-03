package de.greencity.bladenightapp.android.tracker;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.valuelogger.ValueLogger;


public class GeoTraceLogger {

    public enum FIELD {
        LATITUDE("la"),
        LONGITUDE("lo"),
        ACCURACY("ac"),
        LINEAR_POSITION("lp"),
        ;
        /**
         * @param text
         */
        private FIELD(final String text) {
            this.text = text;
        }

        private final String text;

        @Override
        public String toString() {
            return text;
        }
    }

    public GeoTraceLogger(File traceFile) {
        this.valueLogger = new ValueLogger(traceFile);
    }

    public void setLatitude(double latitude) {
        valueLogger.setValue(FIELD.LATITUDE.toString(), Double.toString(latitude));
    }

    public void setLongitude(double longitude) {
        valueLogger.setValue(FIELD.LONGITUDE.toString(), Double.toString(longitude));
    }

    public void setAccuracy(double accuracy) {
        valueLogger.setValue(FIELD.ACCURACY.toString(), Double.toString(accuracy));
    }

    public void setLinearPosition(double linearPosition) {
        valueLogger.setValue(FIELD.LINEAR_POSITION.toString(), Double.toString(linearPosition));
    }

    public void flushAllValues() {
        valueLogger.flushAllValues();
    }

    public void setTimestamp(DateTime dateTime) {
        valueLogger.setTimestamp(dateTime);
    }

    public void setValue(String key, String value) {
        valueLogger.setValue(key, value);
    }

    public void write() throws IOException {
        valueLogger.write();
    }

    public void writeWithTimeLimit(long timeLimitInMs) throws IOException {
        valueLogger.writeWithTimeLimit(timeLimitInMs);
    }

    private ValueLogger valueLogger;

}
