package de.greencity.bladenightapp.android.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import de.greencity.bladenightapp.android.network.SslHelper;

public class AsyncDownloadTaskHttpClient extends AsyncTask<String, Long, Boolean> {

    public AsyncDownloadTaskHttpClient(Context context, StatusHandler handler) {
        this.context = context;
        this.statusHandler = handler;
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

    static class MyVerifier extends AbstractVerifier {

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        }
    }


    private Boolean doInBackground2(String... params) {
        try {

            URI uri = new URI(params[0]);
            URL url = new URL(params[0]);

            HttpClient httpClient = null;
            HttpParams httpParams = new BasicHttpParams();
            if ( "https".equals(url.getProtocol()) ) {
                SchemeRegistry registry = new SchemeRegistry();
                Scheme scheme = new Scheme("https", SslHelper.getSocketFactory(context), 8081);
                registry.register(scheme);
                httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams);
            }
            else {
                httpClient = new DefaultHttpClient();
            }

            org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = (org.apache.http.conn.ssl.SSLSocketFactory)httpClient
                    .getConnectionManager().getSchemeRegistry().getScheme("https")
                    .getSocketFactory();
            sslSocketFactory.setHostnameVerifier(new MyVerifier());


            HttpResponse httpResponse = httpClient.execute( new HttpGet(uri) );

            fileSize = httpResponse.getEntity().getContentLength();
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

            InputStream input = httpResponse.getEntity().getContent();
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
            Log.e(TAG, "Got exception: ", e);
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
    private Context context;

}
