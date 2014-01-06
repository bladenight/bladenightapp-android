package de.greencity.bladenightapp.android.statistics;


import java.text.SimpleDateFormat;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.selection.EventFragment;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;

public class StatisticsActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_statistics);
		getActivityParametersFromIntent(getIntent());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Log.i(TAG, "onResume");
		configureActionBar();
		super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            addSampleData();
            mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "onNewIntent");
		setIntent(intent);
		
	}
	
	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		new ActionBarConfigurator(actionBar)
		.show(ActionItemType.FRIENDS)
		.setTitle(R.string.title_statistics)
		.configure();

	}
	
	private void getActivityParametersFromIntent(Intent intent) {

		Bundle bundle = intent.getExtras();
		String json = bundle.getString(PARAM_EVENT_MESSAGE);
		Event event = EventGsonHelper.getGson().fromJson(json, Event.class);
		Log.i(TAG, "getActivityParametersFromIntent Routename="+ event.getRouteName());
		
		TextView course = (TextView)findViewById(R.id.statistics_course);
		TextView length = (TextView)findViewById(R.id.statistics_length);
		TextView avg_speed = (TextView)findViewById(R.id.statistics_avg_speed);
		TextView participants = (TextView)findViewById(R.id.statistics_participants);
		TextView date = (TextView)findViewById(R.id.statistics_date);

		course.setText(routeNameToText(event.getRouteName()));	
		participants.setText(event.getParticipants() + "");
		date.setText(event.getStartDateAsString());
		
	}
	
	private String routeNameToText(String routeName){
		if (routeName.equals("Nord - kurz")){
			return getResources().getString(R.string.course_north_short);
		}
		if (routeName.equals("Nord - lang")){
			return getResources().getString(R.string.course_north_long);
		}
		if (routeName.equals("West - kurz")){
			return getResources().getString(R.string.course_west_short);
		}
		if (routeName.equals("West - lang")){
			return getResources().getString(R.string.course_west_long);
		}
		if (routeName.equals("Ost - kurz")){
			return getResources().getString(R.string.course_east_short);
		}
		if (routeName.equals("Ost - lang")){
			return getResources().getString(R.string.course_east_long);
		}
		if (routeName.equals("Familie")){
			return getResources().getString(R.string.course_family);
		}
		return routeName;
	}
	
	final static String TAG = "StatisticsActivity";
	public static final String PARAM_EVENT_MESSAGE = "eventMessage";
	
	
	private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addSampleData() {
        mCurrentSeries.add(1, 2);
        mCurrentSeries.add(2, 3);
        mCurrentSeries.add(3, 2);
        mCurrentSeries.add(4, 5);
        mCurrentSeries.add(5, 4);
    }

    

  
} 
