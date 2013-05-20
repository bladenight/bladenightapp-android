package de.greencity.bladenightapp.android.social;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.dev.android.R;

@SuppressLint("UseSparseArrays")
public class FriendColorsHelper {

	private Context context;
	

	FriendColorsHelper(Context context) {
		this.context = context;
	}

	public int numberOfIndexedColor() {
		return indexToColorId.size();
	}

	public int getIndexedColor(int index) {
		//tmp
		if(index==customColorIndex){
			return customColor;
		}
		if ( indexToColorId.get(index) == null ) {
			Log.e(TAG, "Invalid color index: " + index);
			Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
			return 0;
		}
		return context.getResources().getColor(indexToColorId.get(index));
	}

	public int getIndexOfColor(int color) {
		for (Integer colorIndex: indexToColorId.keySet()) {
			if ( getIndexedColor(colorIndex) == color )
				return colorIndex;
		}
		if(color==customColor)
			return customColorIndex;
		return -1;
	}


	public double getColorDistance(int color1, int color2) {
		double distance = 0;
		for ( int i = 0 ; i < 3 ; i ++) {
			int d = (color1 & 0xff) - (color2 & 0xff); 
			distance += ((d*d)/(255.0*255.0));
			color1 = color1 >> 8;
		color2 = color2 >> 8;
		}
		return distance;
	}

	public int getDistinguishableColor(List<Integer> colors) {
		Map<Integer, Integer> usageCount = new HashMap<Integer, Integer>();
		for (Integer colorIndex: indexToColorId.keySet()) {
			int indexedColor = getIndexedColor(colorIndex);
			int count = 0;
			for (Integer colorFromInput: colors) {
				if ( getColorDistance(indexedColor, colorFromInput) < 0.1 )
					count++;
			}
			usageCount.put(colorIndex, count);
		}
		Entry<Integer, Integer> min = null;
		for (Entry<Integer, Integer> entry : usageCount.entrySet()) {
			if (min == null || min.getValue() > entry.getValue()) {
				min = entry;
			}
		}
		return getIndexedColor(min.getKey());
	}
	
	public void setCustomColor(int color){
		customColor = color;
	}

	private static Map<Integer,Integer> indexToColorId;
	private static final String TAG = "FriendColorsHelper";
	private int customColor;
	static final int customColorIndex = -2;


	static {
		indexToColorId = new HashMap<Integer,Integer>();
		int i = 1;
		indexToColorId.put(i++, R.color.new_friend1);
		indexToColorId.put(i++, R.color.new_friend2);
		indexToColorId.put(i++, R.color.new_friend3);
		indexToColorId.put(i++, R.color.new_friend4);
		indexToColorId.put(i++, R.color.new_friend5);
		indexToColorId.put(i++, R.color.new_friend6);
	}

}
