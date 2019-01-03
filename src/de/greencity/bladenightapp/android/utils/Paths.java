package de.greencity.bladenightapp.android.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class Paths {
    public static File getAppDataDirectory(Context context) {
        return context.getFilesDir();
    }
}
