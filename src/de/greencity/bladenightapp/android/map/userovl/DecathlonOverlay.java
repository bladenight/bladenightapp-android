package de.greencity.bladenightapp.android.map.userovl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greencity.bladenightapp.android.social.Friends;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.network.messages.MovingPointMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class DecathlonOverlay {

    private final MapView mapView;
    private Context context;

    private final String TAG = "DecathlonOverlay";

    public DecathlonOverlay(Context context, MapView mapView) {
        this.mapView = mapView;
        this.context = context;
        List<LatLong> coordinates = new ArrayList<LatLong>();
        coordinates.add(new LatLong(48.141447, 11.561761));
        coordinates.add(new LatLong(48.136657,11.546981));
        coordinates.add(new LatLong(48.182503,11.530678));
        coordinates.add(new LatLong(48.177481,11.636327));

        for(LatLong coordinate: coordinates) {
            DecathlonMarker marker = new DecathlonMarker(mapView);
            marker.setLatLong(coordinate);
            marker.show();
        }

        show();
    }

    public void show() {
        repaint();
    }

    public void hide() {
        // TODO
    }

    public void repaint() {
        mapView.repaint();
        mapView.getLayerManager().redrawLayers();
    }
}
