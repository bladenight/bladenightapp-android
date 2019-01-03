package de.greencity.bladenightapp.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;

public class ClipboardUtils {

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    static public void copy(Context context, String string) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(string);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Code",string);
            clipboard.setPrimaryClip(clip);
        }
    }
}
