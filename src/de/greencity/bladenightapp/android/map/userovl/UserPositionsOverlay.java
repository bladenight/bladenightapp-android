package de.greencity.bladenightapp.android.map.userovl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
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

public class UserPositionsOverlay {

    private final MapView mapView;
    private Context context;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, FriendMarker> friendMarkers = new HashMap<Integer, FriendMarker>();
    private Friends friends;

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
        throw new RuntimeException("Not implemented");
    }

    public void repaint() {
        mapView.repaint();
        mapView.getLayerManager().redrawLayers();
    }

    public void updateOwnMarker(Location location) {
        // Log.i(TAG, "UserPositionsOverlay.onLocationChanged: " + location);
        LatLong gp = new LatLong(location.getLatitude(), location.getLongitude());

        FriendMarker ownMarker = getFriendMarker(SocialActivity.ID_ME);
        ownMarker.setLatLong(gp);
        ownMarker.setRadius(location.getAccuracy());

        repaint();
    }


    public synchronized void update(RealTimeUpdateData data) {
        Set<Integer> depracatedFriendIds = new HashSet<Integer>(friendMarkers.keySet());
        depracatedFriendIds.remove(SocialActivity.ID_ME);
        for ( Integer friendId : data.fri.keySet() ) {
            MovingPointMessage nvp = data.fri.get(friendId);

            FriendMarker friendMarker = getFriendMarker(friendId);
            friendMarker.setRadius(nvp.getAccuracy());
            friendMarker.setLatLong(new LatLong(nvp.getLatitude(), nvp.getLongitude()));
            friendMarker.show();

            depracatedFriendIds.remove(friendId);
        }
        for (Integer depracatedFriendId : depracatedFriendIds) {
            hideFriend(depracatedFriendId);
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
        friends.load();
        // Colors might been have changed in the meantime:
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

    private void hideFriend(int friendId) {
        Log.i(TAG, "deleteFriend " + friendId);
        FriendMarker friendMarker = friendMarkers.get(friendId);
        if ( friendMarker != null ) {
            friendMarker.hide();
        }
    }
}
