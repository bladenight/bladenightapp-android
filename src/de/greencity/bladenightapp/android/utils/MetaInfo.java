package de.greencity.bladenightapp.android.utils;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class MetaInfo {

    public static String getBuildTime(Context context) {
        try{
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zipFile = new ZipFile(ai.sourceDir);
            ZipEntry zipEntry = zipFile.getEntry("classes.dex");
            long time = zipEntry.getTime();
            zipFile.close();
            return SimpleDateFormat.getInstance().format(new java.util.Date(time));
        } catch(Exception e){
            Log.e("getBuildTime",e.toString());
            return "UNKNOWN_BUILD_TIME";
        }
    }
}
