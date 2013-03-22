package de.greencity.bladenightapp.android.statistics;


import de.greencity.bladenightapp.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

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
