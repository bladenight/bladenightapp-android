package de.greencity.bladenightapp.android.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class AsyncDownloadTask extends AsyncTask<String, Long, Boolean> {

    public AsyncDownloadTask(StatusHandler handler) {
        this.statusHandler = handler;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }



    @Override
    protected Boolean doInBackground(String... params) {
        Log.i(TAG, "doInBackground");
        if ( doInBackground2(params) ) {
            statusHandler.onDownloadSuccess();
            return Boolean.TRUE;
        }
        else {
            statusHandler.onDownloadFailure();
            return Boolean.FALSE;
        }
    }

    private Boolean doInBackground2(String... params) {
        try {
            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();

            if ( "https".equals(url.getProtocol()) ) {
                if ( sslSocketFactory == null )
                    Log.w(TAG, "HTTPS requested but no sslSocketFactory provided");
                else {
                    HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) connection;
                    Log.i(TAG, "Configuring the SSL factory: " + sslSocketFactory);
                    httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);
                    httpsUrlConnection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }
            }
            Log.i(TAG, connection.toString());
            connection.connect();

            fileSize = connection.getContentLength();
            Log.i(TAG, "fileSize="+fileSize);

            File targetFile = new File(params[1]);
            File parentDir = targetFile.getParentFile();

            if ( ! parentDir.exists() ) {
                parentDir.mkdirs();
            }
            if ( ! parentDir.exists() || ! parentDir.isDirectory() ) {
                Log.e(TAG, "Could not create the directory: \"" + parentDir + "\"");
                return false;
            }

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(targetFile);

            byte data[] = new byte[10*1024];
            long transferred = 0;
            long count;
            while ((count = input.read(data)) != -1) {
                transferred += count;
                publishProgress(transferred, fileSize);
                output.write(data, 0, (int)count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e(TAG, "Got exception: " + e.toString(), e);
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        statusHandler.onProgress(values[0], values[1]);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.i(TAG, "onPreExecute");

    }

    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Log.i(TAG, "onPostExecute");

        if ( result ) {
            Log.i(TAG, "Download successful");
            statusHandler.onDownloadSuccess();
        }
        else {
            Log.i(TAG, "Download failed");
            statusHandler.onDownloadFailure();
        }

    }

    static public interface StatusHandler {
        public void onProgress(long current, long total);
        public void onDownloadFailure();
        public void onDownloadSuccess();
    };


    private final String TAG  = "AsyncDownloadTask";
    private long fileSize = 0;
    private StatusHandler statusHandler;
    private SSLSocketFactory sslSocketFactory;

}
