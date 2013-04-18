package de.greencity.bladenightapp.android.utils;

import de.greencity.bladenightapp.network.messages.MovingPointMessage;

public class DistanceFormatting {
	public static String getDiffDistanceAsString(MovingPointMessage mp1, MovingPointMessage mp2) {
		return getDistanceAsString(mp2.getPosition()-mp1.getPosition(), true );
	}


	public static String getDistanceAsString(MovingPointMessage mp) {
		return getDistanceAsString(mp.getPosition(), true);
	}

	public static String getDistanceAsString(double distance, boolean isInProcession) {
		if ( ! isInProcession )
			return "---";
		double value = distance;
		String format;
		if ( Math.abs(distance) < 1000.0 ){
			value = Math.round(value);
			format = "%.0f m";
		}
		else {
			value /= 1000.0;
			format = "%.1f km";
		}
		return String.format(format, value);
	}

}
