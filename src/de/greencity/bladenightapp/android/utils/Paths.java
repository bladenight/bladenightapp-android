package de.greencity.bladenightapp.android.utils;

import android.content.Context;

import java.io.File;

public class Paths {
    public static File getAppDataDirectory(Context context) {
        return context.getFilesDir();
    }
}
