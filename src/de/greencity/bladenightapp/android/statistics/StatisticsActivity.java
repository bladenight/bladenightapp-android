package de.greencity.bladenightapp.android.statistics;


import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionEventSelection;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.dev.android.R;

public class StatisticsActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_statistics);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Log.i(TAG, "onResume");
		configureActionBar();
	}
	
	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		
		new ActionBarConfigurator(actionBar)
		.show(ActionItemType.FRIENDS)
		.setTitle(R.string.title_statistics)
		.configure();

	}
} 
