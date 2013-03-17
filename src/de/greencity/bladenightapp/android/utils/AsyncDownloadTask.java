package de.greencity.bladenightapp.android.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import de.greencity.bladenightapp.android.network.Actions;

public class AsyncDownloadTask extends AsyncTask<String, Long, Boolean> {
	
	public AsyncDownloadTask(Context context, String downloadId) {
		this.context = context;
		this.downloadId = downloadId;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		Log.i(TAG, "doInBackground");
		try {
			URL url = new URL(params[0]);
			URLConnection connection = url.openConnection();
			connection.connect();

			fileSize = connection.getContentLength();

			File targetFile = new File(params[1]);
			File parentDir = targetFile.getParentFile();

			if ( ! parentDir.exists() ) {
				parentDir.mkdirs();
			}
			if ( ! parentDir.exists() || ! parentDir.isDirectory() ) {
				Log.e(TAG, "Could not create the directory " + parentDir);
				return false;
			}

			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(targetFile);

			byte data[] = new byte[10*1024];
			long transferred = 0;
			long count;
			while ((count = input.read(data)) != -1) {
				transferred += count;
				publishProgress(transferred);
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

		long newProgress = values[0];

		Log.i(TAG, "onProgressUpdate " + newProgress);

		Intent intent = new Intent(Actions.DOWNLOAD_PROGRESS);
		intent.putExtra("id", downloadId);
		intent.putExtra("total", fileSize);
		intent.putExtra("current", newProgress);
		
		context.sendBroadcast(intent);
		
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
			onDownloadSuccess();
		}
		else {
			Log.i(TAG, "Download failed");
			onDownloadFailure();
		}

//		if ( result ) {
//			Toast.makeText(context, "Das Kartenmaterial ist erfolgreich heruntergeladen worden", Toast.LENGTH_LONG).show();
//			try {
//				Log.i(TAG, "Clearing Mapsforge cache...");
//				String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
//				String CACHE_DIRECTORY = "/Android/data/org.mapsforge.android.maps/cache/";
//				String cacheDirectoryPath = externalStorageDirectory + CACHE_DIRECTORY;
//				FileUtils.deleteDirectory(new File(cacheDirectoryPath));
//			} catch (Exception e) {
//				Log.w(TAG, "Failed to clear the MapsForge cache",e);
//			}
//			createNewMapView();
//		}
//		else 
//			Toast.makeText(context, "Fehler beim herunterladen", Toast.LENGTH_LONG).show();

	}
	
	public void onDownloadSuccess() {
		
	}

	public void onDownloadFailure() {
		
	}

	private final String TAG  = "AsyncDownloadTask";
	private long fileSize = 0;
	private String downloadId;
	private Context context;

}
