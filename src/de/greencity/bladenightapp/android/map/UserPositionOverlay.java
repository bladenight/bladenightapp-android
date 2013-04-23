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
		friends.load();
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

		Marker ownMarker = getFriendMarker(SocialActivity.ID_ME);
		ownMarker.setGeoPoint(gp);
		
		Circle ownCircle = getAccuracyCircle(SocialActivity.ID_ME);
		ownCircle.setGeoPoint(gp);
		ownCircle.setRadius(location.getAccuracy());
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

			Circle circle = getAccuracyCircle(friendId);
			circle.setRadius(100);
			marker.setGeoPoint(new GeoPoint(nvp.getLatitude(), nvp.getLongitude()));

		}
	}

	public Marker getFriendMarker(Integer friendId) {
		if ( friendMarkers.get(friendId) != null )
			return friendMarkers.get(friendId);

		int resourceIdentifier = R.drawable.user_symbol;
		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		int color = friends.getFriendColor(friendId);

		drawable.mutate().setColorFilter(color, Mode.MULTIPLY);

		Marker marker = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		friendMarkers.put(friendId, marker);
		getOverlayItems().add(marker);
		return marker;
	}

	private Circle getAccuracyCircle(Integer friendId) {
		if ( friendAccuracyCircles.get(friendId) != null )
			return friendAccuracyCircles.get(friendId);

		int color = friends.getFriendColor(friendId);
		Log.i(TAG, "color="+Integer.toHexString(color));
		int alpha = 50;
		
		Paint paintFill = new Paint();
		paintFill.setColor(color);
		paintFill.setAlpha(alpha);
		paintFill.setAntiAlias(true);

		Paint paintStroke = new Paint();
		paintStroke.setColor(Color.WHITE);
		paintStroke.setAlpha(alpha);
		paintStroke.setAntiAlias(true);

		Circle circle = new Circle(new GeoPoint(0,0), 0, paintFill, paintStroke);
		friendAccuracyCircles.put(friendId, circle);
		getOverlayItems().add(circle);

		return circle;
	}


	public void onResume() {
		// Colors might been have changed in the meantime:
		friendMarkers.clear();
	}


	private final MapView mapView;
	private Context context;
	private HashMap<Integer, Marker> friendMarkers = new HashMap<Integer, Marker>();
	private HashMap<Integer, Circle> friendAccuracyCircles = new HashMap<Integer, Circle>();
	private Friends friends;

	private final String TAG = "UserPositionOverlay";


}
