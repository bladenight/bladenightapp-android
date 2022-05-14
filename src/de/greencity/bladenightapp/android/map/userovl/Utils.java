package de.greencity.bladenightapp.android.map.userovl;

import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;

public class Utils {
    static void removeLayerIfPresent(Layers layers, Layer layer)
    {
        if (layers.contains(layer))
            layers.remove(layer);
    }

    static void addLayerIfAbsent(Layers layers, Layer layer)
    {
        if (!layers.contains(layer))
            layers.add(layer);
    }
}
