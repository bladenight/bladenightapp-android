package de.greencity.bladenightapp.android.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import de.greencity.bladenightapp.dev.android.BuildConfig;

public class MetaInfo {

    public static String getBuildTime(Context context) {
        return new Date(BuildConfig.BUILD_TIMESTAMP).toString();
    }
}
