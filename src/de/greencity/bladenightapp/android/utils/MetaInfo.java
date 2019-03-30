package de.greencity.bladenightapp.android.utils;

import android.content.Context;

import java.util.Date;

import de.greencity.bladenightapp.android.BuildConfig;

public class MetaInfo {

    public static String getBuildTime(Context context) {
        return new Date(BuildConfig.BUILD_TIMESTAMP).toString();
    }
}
