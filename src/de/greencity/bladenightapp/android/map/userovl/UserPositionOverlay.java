package de.greencity.bladenightapp.android.map.userovl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class UserPositionOverlay extends ListOverlay implements LocationListener {

	public UserPositionOverlay(Context context, MapView mapView) {
		this.mapView = mapView;
		this.context = context;
		friends = new Friends(context);
		show();
		onResume();
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
		//		Log.i(TAG, "onLocationChanged: " + location);
		GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());


		FriendMarker ownMarker = getFriendMarker(SocialActivity.ID_ME);
		ownMarker.setGeoPoint(gp);
		ownMarker.setRadius(location.getAccuracy());
	}

	@Override
	public void onProviderDisabled(String provider) {
		//		Log.i(TAG, "onProviderDisabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		//		Log.i(TAG, "onProviderEnabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//		Log.i(TAG, "onStatusChanged: " + provider + " status="+status);
	}

	public synchronized void update(RealTimeUpdateData data) {
		Set<Integer> depracatedFriendIds = new HashSet<Integer>(friendMarkers.keySet());
		depracatedFriendIds.remove(SocialActivity.ID_ME);
		for ( Integer friendId : data.fri.keySet() ) {
			MovingPointMessage nvp = data.fri.get(friendId);

			FriendMarker friendMarker = getFriendMarker(friendId);
			friendMarker.setRadius(nvp.getAccuracy());
			friendMarker.setGeoPoint(new GeoPoint(nvp.getLatitude(), nvp.getLongitude()));

			depracatedFriendIds.remove(friendId);
		}
		for (Integer depracatedFriendId : depracatedFriendIds) {
			deleteFriend(depracatedFriendId);
		}
	}

	public FriendMarker getFriendMarker(Integer friendId) {
		if ( friendMarkers.get(friendId) != null ) {
			return friendMarkers.get(friendId);
		}

		FriendMarker friendMarker = new FriendMarker(context, this, friends.getFriendColor(friendId));
		friendMarkers.put(friendId, friendMarker);
		friendMarker.show();

		return friendMarker;
	}

	public void onResume() {
		// Colors might been have changed in the meantime:
		friends.load();
		updateColors();
	}

	private void updateColors() {
		for (int friendId: friendMarkers.keySet()) {
			int color = friends.getFriendColor(friendId);
			getFriendMarker(friendId).setColor(color);
		}
	}

	private void deleteFriend(int friendId) {
		FriendMarker friendMarker = friendMarkers.get(friendId);
		if ( friendMarker != null ) {
			friendMarker.remove();
			friendMarkers.remove(friendId);
		}
	}


	private final MapView mapView;
	private Context context;
	private HashMap<Integer, FriendMarker> friendMarkers = new HashMap<Integer, FriendMarker>();
	private Friends friends;

	private final String TAG = "UserPositionOverlay";


}
