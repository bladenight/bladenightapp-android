package de.greencity.bladenightapp.android.map;

import java.io.InputStream;

import org.mapsforge.map.rendertheme.XmlRenderTheme;

public enum CustomRenderTheme implements XmlRenderTheme {
    /**
     * A render-theme similar to the OpenStreetMap Osmarender style.
     *
     * @see <a href="http://wiki.openstreetmap.org/wiki/Osmarender">Osmarender</a>
     */
    CUSTOM_RENDER("/map/customrendertheme/", "rendertheme.xml");

    private final String absolutePath;
    private final String file;

    private CustomRenderTheme(String absolutePath, String file) {
        this.absolutePath = absolutePath;
        this.file = file;
    }

    @Override
    public String getRelativePathPrefix() {
        return this.absolutePath;
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        return Thread.currentThread().getClass().getResourceAsStream(this.absolutePath + this.file);
    }
}
