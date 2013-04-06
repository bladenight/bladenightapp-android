package de.greencity.bladenightapp.android.map;

import java.util.HashMap;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.Circle;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.network.messages.NetMovingPoint;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class UserPositionOverlay extends ListOverlay implements LocationListener {

	public UserPositionOverlay(Context context, MapView mapView) {
		this.mapView = mapView;
		this.context = context;
		reinit();
	}

	private void reinit() {
		int resourceIdentifier = R.drawable.user_symbol;
		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		drawable.setColorFilter(context.getResources().getColor(R.color.user_position_own), Mode.MULTIPLY);
		
		externalCircle = createExternalCircle();
		getOverlayItems().add(externalCircle);

		userSymbol = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		getOverlayItems().add(userSymbol);
	}

	private Circle createExternalCircle() {
		Paint paintFill = new Paint();
		paintFill.setColor(Color.argb(50, 150, 150, 255));
		paintFill.setAntiAlias(true);

		Paint paintStroke = new Paint();
		paintStroke.setColor(Color.argb(50, 20, 20, 100));
		paintStroke.setAntiAlias(true);
		return new Circle(new GeoPoint(0,0), 0, paintFill, paintStroke);
	}
	
	public void show() {
		mapView.getOverlays().add(this);
		mapView.redraw();
	}
	
	public void hide() {
		mapView.getOverlays().remove(this);
		mapView.redraw();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "onLocationChanged: " + location);
		GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
		
		userSymbol.setGeoPoint(gp);
		externalCircle.setGeoPoint(gp);
		externalCircle.setRadius(location.getAccuracy());
		show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "onProviderDisabled: " + provider);
		hide();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, "onProviderEnabled: " + provider);
		show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, "onStatusChanged: " + provider + " status="+status);
	}
	
	public void update(RealTimeUpdateData data) {
		for ( Long friendId : data.fri.keySet() ) {
			NetMovingPoint nvp = data.fri.get(friendId);
			Marker marker = getFriendMarker(friendId);
			marker.setGeoPoint(new GeoPoint(nvp.getLatitude(), nvp.getLongitude()));
		}
	}
	
	public Marker getFriendMarker(Long friendId) {
		if ( friendSymbols.get(friendId) != null )
			return friendSymbols.get(friendId);

		int resourceIdentifier = R.drawable.user_symbol;
		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		drawable.setColorFilter(Color.RED, Mode.MULTIPLY);
		
		Marker marker = friendSymbols.get(friendId);
		marker = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		friendSymbols.put(friendId, marker);
		getOverlayItems().add(marker);
		return marker;
	}

	
	private final MapView mapView;
	private Context context;
	private Marker userSymbol;
	private HashMap<Long, Marker> friendSymbols = new HashMap<Long, Marker>();
	private Circle externalCircle;

	private final String TAG = "UserPositionOverlay";

}
