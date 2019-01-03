package de.greencity.bladenightapp.android.selection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import de.greencity.bladenightapp.dev.android.R;

public class SplashActivity extends Activity {

    private static String TAG = SplashActivity.class.getName();
    private static long SLEEP_TIME = 3; // Sleep for some time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide notification bar

        setContentView(R.layout.splash_screen);

        // Start timer and launch main activity
        IntentLauncher launcher = new IntentLauncher();
        launcher.start();
    }

    private class IntentLauncher extends Thread {
        @Override
        /**
         * Sleep for some time and then start new activity.
         */
        public void run() {
            try {
                // Sleeping
                Thread.sleep(SLEEP_TIME * 1000);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Start main activity
            Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();
        }
    }
}
