package de.greencity.bladenightapp.android.mainactivity;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionEventSelection;
import de.greencity.bladenightapp.android.actionbar.ActionHome;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTaskHttpClient;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.Permissions;
import de.greencity.bladenightapp.dev.android.R;

public class MainActivity extends Activity {

    static private final String LANDING_PAGE_REMOTE_PATH = "landing.html";
    static WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureActionBar();

        Permissions.verifyPermissionsForApp(this);

        webView = (WebView) findViewById(R.id.main_webview);
        webView.loadUrl("file://" + getLandingPageLocalPath());

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        triggerLandingPageDownload();
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        new ActionBarConfigurator(actionBar)
                .setAction(ActionBarConfigurator.ActionItemType.HOME, new ActionHome() {
                    @Override
                    public void performAction(View view) {
                        triggerLandingPageDownload();
                    }
                })
                .show(ActionBarConfigurator.ActionItemType.FRIENDS)
                .show(ActionBarConfigurator.ActionItemType.TRACKER_CONTROL)
                .setTitle(R.string.title_main)
                .configure();
    }


    private String getLandingPageRemotePath() {
        return LANDING_PAGE_REMOTE_PATH;
    }

    private String getLandingPageLocalPath() {
        return Paths.getAppDataDirectory(this) + "/" + LANDING_PAGE_REMOTE_PATH;
    }

    private void triggerLandingPageDownload() {
        BladeNightApplication.networkClient.downloadFile(getLandingPageLocalPath(), getLandingPageRemotePath(),
                new AsyncDownloadTaskHttpClient.StatusHandler() {
                    @Override
                    public void onProgress(long current, long total) {
                        // We don't care about progress
                    }

                    @Override
                    public void onDownloadFailure() {
                        // Hopefully a temporary issue, ignore
                    }

                    @Override
                    public void onDownloadSuccess() {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                webView.reload();
                            }
                        });
                    }
                });
    }
}
