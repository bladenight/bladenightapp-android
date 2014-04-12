package de.greencity.bladenightapp.android.widget;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.utils.LocalBroadcastReceiversRegister;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class WidgetProvider extends AppWidgetProvider {
	private Drawable background;
	private Drawable progressBar;
	private final String TAG = "WidgetProvider";
	private final int renderingWidth = 500;
	private final int renderingHeight = 100;
	private LocalBroadcastReceiversRegister localBroadcastReceiversRegister;


	@Override
	public void onEnabled(Context context) {
		Log.i(TAG, "onEnabled");
		registerLocalBroadcastReceiversIfRequired(context);
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		getLocalBroadcastReceiversRegister(context).unregisterReceivers();
	}

	private LocalBroadcastReceiversRegister getLocalBroadcastReceiversRegister(Context context) {
		if ( localBroadcastReceiversRegister == null )
			localBroadcastReceiversRegister = new LocalBroadcastReceiversRegister(context);
		return localBroadcastReceiversRegister;
	}
	
	private void registerLocalBroadcastReceiversIfRequired(Context context) {
		if ( getLocalBroadcastReceiversRegister(context).getNumberORegisteredReceivers() == 0 ) {
			Log.i(TAG, "Registering receiver for GOT_REALTIME_DATA");
			getLocalBroadcastReceiversRegister(context).registerReceiver(LocalBroadcast.GOT_REALTIME_DATA, this);
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// onEnabled() is not always called, so make sure we have registered to broadcast at the 
		// latest when we update the widget
		registerLocalBroadcastReceiversIfRequired(context);
		
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		for (int widgetId : allWidgetIds) {
			// create some random data
			int number = (new Random().nextInt(100));

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

			Log.i(TAG, "onUpdate");

			// Set the text
			remoteViews.setTextViewText(R.id.widgetText, String.valueOf(number));

			// remoteViews.setProgressBar(R.id.progress_procession, 100, number, false);

			Bitmap bitmap = Bitmap.createBitmap(renderingWidth, renderingHeight, Config.ARGB_8888);
			bitmap.eraseColor(R.color.bn_orange);

			//create a canvas from existent bitmap that will be used for drawing  
			Canvas canvas = new Canvas(bitmap);  

			drawProgressBar(context, canvas, number);

			remoteViews.setImageViewBitmap(R.id.imageView1, bitmap); 

			// Register an onClickListener
			setOnClickListener(context, appWidgetIds, remoteViews);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	private void setOnClickListener(Context context, int[] appWidgetIds,
			RemoteViews remoteViews) {
		Intent intent = new Intent(context, WidgetProvider.class);

		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widgetText, pendingIntent);
	}

	private void drawProgressBar(Context context, Canvas canvas, int number) {

		GlobalStateAccess globalStateAccess = new GlobalStateAccess(context);

		Drawable background = getBackground(context);
		background.setBounds(0, 0, renderingWidth, renderingHeight);
		background.draw(canvas);

		RealTimeUpdateData realTimeUpdateData = globalStateAccess.getRealTimeUpdateData();
		Log.i(TAG, "realTimeUpdateData="+realTimeUpdateData);
		if ( realTimeUpdateData == null ) {
			return; // no data available
		}

		int xMin = (int)(1.0 * renderingWidth * realTimeUpdateData.getTailPosition() / realTimeUpdateData.getRouteLength());
		int xMax = (int)(1.0 * renderingWidth * realTimeUpdateData.getHeadPosition() / realTimeUpdateData.getRouteLength());

		int yMin = (int)(0.0 * renderingHeight);
		int yMax = (int)(1.0 * renderingHeight);

		Log.i(TAG, "xMin="+xMin);
		Log.i(TAG, "xMax="+xMax);

		Drawable progressBar = getProgressBar(context);
		progressBar.setBounds(xMin, yMin, xMax, yMax);
		progressBar.draw(canvas);
	}

	private Drawable getBackground(Context context) {
		if ( background == null )
			background = context.getResources().getDrawable(R.drawable.progression_background);
		Log.i(TAG, background.getClass().getName());
		return background;
	}

	private Drawable getProgressBar(Context context) {
		if ( progressBar == null )
			progressBar = context.getResources().getDrawable(R.drawable.procession_progressbar);
		return progressBar;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i(TAG, "onReceive: " + intent.getAction());
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		onUpdate(context, manager, manager.getAppWidgetIds(componentName)); 
	}

}
