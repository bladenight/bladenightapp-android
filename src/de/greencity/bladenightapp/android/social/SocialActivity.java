package de.greencity.bladenightapp.android.social;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class SocialActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_social);
		configureActionBar();

		if (! ServiceUtils.isServiceRunning(this, GpsTrackerService.class))
			ServiceUtils.startService(this, GpsTrackerService.class);
		else
			ServiceUtils.stopService(this, GpsTrackerService.class);
	}

	@Override
	public void onStart() {
		super.onStart();
		configureActionBar();
	}
	
	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.hide(ActionItemType.FRIENDS)
		.setTitle(R.string.title_social)
		.configure();
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {	  
		switch (view.getId()) {
		//	    case R.id.next: goUp();
		//	      break;

		}
	}
} 
