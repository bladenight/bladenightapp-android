package de.greencity.bladenightapp.android.cache;

import org.apache.commons.lang3.exception.ExceptionUtils;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class RoutesCache {

    public RoutesCache(Context context) {
        this.context = context;
    }

    public void write(RouteMessage routeMessage) {
        Log.i(TAG, "Saving " + routeMessage.getRouteName() + " to cache");
        JsonCacheAccess<RouteMessage> routeCache = newCacheAccess(routeMessage.getRouteName());
        routeCache.set(routeMessage);
    }

    public RouteMessage read(String routeName) {
        Log.i(TAG, "Getting cache for route named \"" + routeName + "\"");
        if ( routeName == null) {
            Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
            return null;
        }
        JsonCacheAccess<RouteMessage> routeCache = newCacheAccess(routeName);
        return routeCache.get();
    }

    private static String getNameForRoute(String routeName) {
        return PREFIX + routeName;
    }

    private JsonCacheAccess<RouteMessage> newCacheAccess(String routeName) {
        return new JsonCacheAccess<RouteMessage>(context, RouteMessage.class, getNameForRoute(routeName));
    }


    public static final String PREFIX = "jsoncache-route-";
    private static final String TAG = "JsonRouteCache";
    private Context context;
}
