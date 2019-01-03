package de.greencity.bladenightapp.android.global;

import android.location.Location;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class GlobalState {
    public Location getLocationFromGps() {
        return locationFromGps;
    }

    public void setLocationFromGps(Location locationFromGps) {
        this.locationFromGps = locationFromGps;
    }

    public RealTimeUpdateData getRealTimeUpdateData() {
        return realTimeUpdateDataFromServer;
    }

    public void setRealTimeUpdateData(
            RealTimeUpdateData realTimeUpdateDataFromServer) {
        this.realTimeUpdateDataFromServer = realTimeUpdateDataFromServer;
    }

    public EventList getEventList() {
        return eventList;
    }

    public void setEventList(EventList eventList) {
        // Log.i(TAG, "setEventList: " + eventList);
        this.eventList = eventList;
    }

    private Location            locationFromGps;
    private RealTimeUpdateData  realTimeUpdateDataFromServer;
    private EventList           eventList;
    // private static final String TAG = "GlobalState";

}
