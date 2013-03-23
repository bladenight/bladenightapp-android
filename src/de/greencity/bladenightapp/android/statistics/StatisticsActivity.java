package de.greencity.bladenightapp.android.statistics;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;

public class StatisticsActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.activity_statistics);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
    ImageView titlebar = (ImageView)(findViewById(R.id.icon));
    titlebar.setImageResource(R.drawable.ic_stats);
    TextView titletext = (TextView)findViewById(R.id.title);
    titletext.setText(R.string.title_statistics);
  }
} 
