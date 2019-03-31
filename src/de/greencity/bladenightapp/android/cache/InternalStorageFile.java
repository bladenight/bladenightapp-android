package de.greencity.bladenightapp.android.cache;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class InternalStorageFile {

    public InternalStorageFile(Context context, String name) {
        this.name = name;
        this.context = context;
    }

    public boolean write(String content) {
        boolean success = false;
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
//          Log.i(TAG, "Writing " + content);
            fos.write(content.getBytes());
            success = true;
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to " + name + " : " + e.toString());
        }
        finally {
            if ( fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot close " + name + " : " + e.toString());
                }
        }
        return success;
    }

    public String read() {
        StringBuffer buffer = new StringBuffer();
        FileInputStream fis = null;
        boolean success = false;
        try {
            fis = context.openFileInput(name);
            InputStreamReader isr = new InputStreamReader(fis, "UTF8");
            Reader in = new BufferedReader(isr);
            int ch;
            while ((ch = in.read()) > -1) {
                buffer.append((char)ch);
            }
            success = true;
            in.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Cannot read " + name + " : " + e.toString());
        }
        finally {
            if ( fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot close " + name + " : " + e.toString());
                }
        }
        if ( success )
            return buffer.toString();
        else
            return null;
    }

    private String name;
    private Context context;
    private final static String TAG = "InternalStorageFile";
}
