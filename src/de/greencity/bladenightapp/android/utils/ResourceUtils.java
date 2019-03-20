package de.greencity.bladenightapp.android.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceUtils {
    private static final String TAG = "ResourceUtils";

    static public boolean extractMapFile(String resourcePath, File targetFile) {
        InputStream in = null;
        OutputStream out = null;

        boolean success = false;

        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);

            if (in == null) {
                Log.e(TAG, "Failed to open resource: " + resourcePath);
            }
            else {
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                success = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to extract map file", e);
        }

        closeSafely(in);
        closeSafely(out);

        if (!success) {
            targetFile.delete();
        }
        return success;
    }

    private static void closeSafely(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

}
