package de.greencity.bladenightapp.android.map;

import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;

import java.io.InputStream;

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
    public XmlRenderThemeMenuCallback getMenuCallback() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getRelativePathPrefix() {
        return this.absolutePath;
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        String completePath  = this.absolutePath + this.file;
        InputStream is = getClass().getResourceAsStream(completePath);
        if(is == null)
            throw new IllegalStateException("Could not open resource: \"" + completePath + "\"");
        return is;
    }

    @Override
    public void setMenuCallback(XmlRenderThemeMenuCallback menuCallback) {

    }
}
