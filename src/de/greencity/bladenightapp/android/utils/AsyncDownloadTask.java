package de.greencity.bladenightapp.android.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncDownloadTask extends AsyncTask<String, Long, Boolean> {
	
	public AsyncDownloadTask(StatusHandler handler) {
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

	private Boolean doInBackground2(String... params) {
		try {
			URL url = new URL(params[0]);
			URLConnection connection = url.openConnection();
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
	
}
