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
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.social.Friend;
import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class UserPositionOverlay extends ListOverlay implements LocationListener {

	public UserPositionOverlay(Context context, MapView mapView) {
		this.mapView = mapView;
		this.context = context;
		friends = new Friends(context);
		reinit();	
	}

	private void reinit() {
		int resourceIdentifier = R.drawable.user_symbol;

		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		drawable.setColorFilter(Friends.getOwnColor(context), Mode.MULTIPLY);

		externalCircle = createExternalCircle();
		getOverlayItems().add(externalCircle);

		userSymbol = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		getOverlayItems().add(userSymbol);

		friends.load();
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
		for ( Integer friendId : data.fri.keySet() ) {
			MovingPointMessage nvp = data.fri.get(friendId);
			Marker marker = getFriendMarker(friendId);
			marker.setGeoPoint(new GeoPoint(nvp.getLatitude(), nvp.getLongitude()));
		}
	}

	public Marker getFriendMarker(Integer friendId) {
		if ( friendMarkers.get(friendId) != null )
			return friendMarkers.get(friendId);

		int resourceIdentifier = R.drawable.user_symbol;
		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		int color = friends.get(friendId).getColor();

		drawable.mutate().setColorFilter(color, Mode.MULTIPLY);

		Marker marker = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		friendMarkers.put(friendId, marker);
		getOverlayItems().add(marker);
		return marker;
	}

	public void onResume() {
		// Colors might been have changed in the meantime:
		friendMarkers.clear();
	}


	private final MapView mapView;
	private Context context;
	private Marker userSymbol;
	private HashMap<Integer, Marker> friendMarkers = new HashMap<Integer, Marker>();
	private Circle externalCircle;
	private Friends friends;

	private final String TAG = "UserPositionOverlay";


}
