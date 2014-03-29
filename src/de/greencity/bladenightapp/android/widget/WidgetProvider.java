package de.greencity.bladenightapp.android.widget;

import java.util.Random;

import de.greencity.bladenightapp.dev.android.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	  private static final String ACTION_CLICK = "ACTION_CLICK";

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
}
