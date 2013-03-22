package de.greencity.bladenightapp.android.options;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;

public class OptionsActivity extends Activity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_options);
	}

	@Override
	public void onStart() {
		super.onStart();
		configureActionBar();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
			.hide(ActionItemType.OPTIONS)
			.hide(ActionItemType.RELOAD)
			.hide(ActionItemType.TRACKER_CONTROL)
			.setTitle(R.string.title_options)
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
