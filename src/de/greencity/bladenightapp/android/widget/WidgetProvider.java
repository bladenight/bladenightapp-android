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
import de.greencity.bladenightapp.dev.android.R;

public class WidgetProvider extends AppWidgetProvider {
	private static final String ACTION_CLICK = "ACTION_CLICK";
	private Drawable background;
	private Drawable progressBar;
	private final String TAG = "WidgetProvider";
	private final int renderingWidth = 500;
	private final int renderingHeight = 100;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				WidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// create some random data
			int number = (new Random().nextInt(100));

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			Log.i("WidgetExample", String.valueOf(number));
			// Set the text
			remoteViews.setTextViewText(R.id.update, String.valueOf(number));

			remoteViews.setProgressBar(R.id.progress_procession, 100, number, false);
			
			Bitmap bitmap = Bitmap.createBitmap(renderingWidth, renderingHeight, Config.ARGB_8888);
			bitmap.eraseColor(R.color.bn_orange);
			  
			//create a canvas from existant bitmap that will be used for drawing  
			Canvas canvas = new Canvas(bitmap);  
			
			drawProgressBar(context, canvas, number);
			
			remoteViews.setImageViewBitmap(R.id.imageView1, bitmap); 
			
			// Register an onClickListener
			Intent intent = new Intent(context, WidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
	
	private void drawProgressBar(Context context, Canvas canvas, int number) {
		Drawable background = getBackground(context);
		background.setBounds(0, 0, renderingWidth, renderingHeight);
		background.draw(canvas);
		
		Drawable progressBar = getProgressBar(context);
		progressBar.setBounds(0, 10, number, 90);
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
	
}
