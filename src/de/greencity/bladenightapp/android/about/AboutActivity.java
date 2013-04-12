package de.greencity.bladenightapp.android.about;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;

public class AboutActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
	}

	@Override
	public void onStart() {
		super.onStart();

		configureActionBar();

	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.setTitle(R.string.title_about)
		.configure();
	}


	@Override
	public void onStop() {
		super.onStop();
	}



	final static String TAG = "BladenightAboutActivity";
} 
