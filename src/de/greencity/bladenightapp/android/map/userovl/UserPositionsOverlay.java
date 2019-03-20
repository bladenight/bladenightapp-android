package de.greencity.bladenightapp.android.map.userovl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class UserPositionsOverlay implements LocationListener {

    private final MapView mapView;
    private Context context;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, FriendMarker> friendMarkers = new HashMap<Integer, FriendMarker>();
    private Friends friends;
    private Location lastOwnLocation;

    private final String TAG = "UserPositionsOverlay";

    public UserPositionsOverlay(Context context, MapView mapView) {
        this.mapView = mapView;
        this.context = context;
        friends = new Friends(context);
        show();
        onResume();
    }

    public void show() {
        repaint();
    }

    public void hide() {
        // TODO
    }

    public void repaint() {
        mapView.repaint();
        // TODO
        // mapView.getOverlayController().redrawOverlays();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastOwnLocation = location;

        // Log.i(TAG, "UserPositionsOverlay.onLocationChanged: " + location);
        LatLong gp = new LatLong(location.getLatitude(), location.getLongitude());

        FriendMarker ownMarker = getFriendMarker(SocialActivity.ID_ME);
        ownMarker.setLatLong(gp);
        ownMarker.setRadius(location.getAccuracy());

        repaint();
    }


    @Override
    public void onProviderDisabled(String provider) {
        //      Log.i(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        //      Log.i(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //      Log.i(TAG, "onStatusChanged: " + provider + " status="+status);
    }

    public Location getLastOwnLocation() {
        return lastOwnLocation;
    }

    public synchronized void update(RealTimeUpdateData data) {
        Set<Integer> depracatedFriendIds = new HashSet<Integer>(friendMarkers.keySet());
        depracatedFriendIds.remove(SocialActivity.ID_ME);
        for ( Integer friendId : data.fri.keySet() ) {
            MovingPointMessage nvp = data.fri.get(friendId);

            FriendMarker friendMarker = getFriendMarker(friendId);
            friendMarker.setRadius(nvp.getAccuracy());
            friendMarker.setLatLong(new LatLong(nvp.getLatitude(), nvp.getLongitude()));

            depracatedFriendIds.remove(friendId);
        }
        for (Integer depracatedFriendId : depracatedFriendIds) {
            deleteFriend(depracatedFriendId);
        }
        repaint();
    }

    public FriendMarker getFriendMarker(Integer friendId) {
        if ( friendMarkers.get(friendId) != null ) {
            return friendMarkers.get(friendId);
        }

        FriendMarker friendMarker = new FriendMarker(mapView, friends.getFriendColor(friendId));
        friendMarkers.put(friendId, friendMarker);
        friendMarker.show();

        return friendMarker;
    }

    public void onResume() {
        // Colors might been have changed in the meantime:
        friends.load();
        updateColors();
        repaint();
    }

    private void updateColors() {
        for (int friendId: friendMarkers.keySet()) {
            int color = friends.getFriendColor(friendId);
            getFriendMarker(friendId).setColor(color);
        }
        repaint();
    }

    private void deleteFriend(int friendId) {
        Log.i(TAG, "deleteFriend " + friendId);
        FriendMarker friendMarker = friendMarkers.get(friendId);
        if ( friendMarker != null ) {
            // TODO
            // friendMarker.remove();
            // friendMarkers.remove(friendId);
        }
    }
}
