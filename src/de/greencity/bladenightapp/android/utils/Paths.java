package de.greencity.bladenightapp.android.utils;

import java.io.File;

import android.os.Environment;

public class Paths {
    public static File getAppDataDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "Bladenight");
    }
}
