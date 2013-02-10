package de.greencity.bladenightapp.android.selection;

import java.util.Date;
import java.util.LinkedList;


import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.action.ActionActivity;
import de.greencity.bladenightapp.android.options.OptionsActivity;
import de.greencity.bladenightapp.android.selection.SimpleGestureFilter.SimpleGestureListener;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectionActivity extends Activity implements SimpleGestureListener{
  private EventsDataSource datasource;
  private int activeEventNumber;
  private LinkedList<Event> allEvents;
  private SimpleGestureFilter detector;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.activity_selection);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
    ImageView titlebar = (ImageView)findViewById(R.id.icon);
    titlebar.setImageResource(R.drawable.ic_calendar);
    TextView titletext = (TextView)findViewById(R.id.title);
    titletext.setText(R.string.title_selection);
    
    detector = new SimpleGestureFilter(this,this);

    datasource = new EventsDataSource(this);
    datasource.open();

    allEvents = datasource.getAllEvents();
    
    //workaround, should be refreshed with data on server
    if(allEvents.size()==0){
	    datasource.createEvent("Nord - lang", "15.06.2012", "confirmed", "17.6 km");
	    datasource.createEvent("Ost - kurz", "22.06.2012", "cancelled", "11.3 km");
	    datasource.createEvent("West - kurz", "11.06.2013", "confirmed", "12.4 km");
	    datasource.createEvent("West - lang", "18.06.2013", "pending", "17.4 km");
	    datasource.createEvent("West - kurz", "27.06.2013", "pending", "12.4 km");
	    allEvents = datasource.getAllEvents();
    }
    
    
    activeEventNumber = 2;
    updateEvent();
    
  
    
  }
  
  @Override
  public boolean dispatchTouchEvent(MotionEvent me){
	  this.detector.onTouchEvent(me);
	  return super.dispatchTouchEvent(me);
  }
  
  @Override
  public void onSwipe(int direction) {
   
   switch (direction) {
   
   case SimpleGestureFilter.SWIPE_RIGHT : goDown();
     break;
   case SimpleGestureFilter.SWIPE_LEFT :  goUp();
     break;                                       
   } 
  }
  
  
  private void updateEvent(){
	  TextView textViewCourse = (TextView)findViewById(R.id.course);
	  textViewCourse.setText(allEvents.get(activeEventNumber).getCourse());
	  TextView textViewDate = (TextView)findViewById(R.id.date);
	  textViewDate.setText(allEvents.get(activeEventNumber).getDateFormatted());
	  TextView textViewLength = (TextView)findViewById(R.id.length);
	  textViewLength.setText(allEvents.get(activeEventNumber).getLength());
	  
	  updateStatus();
	  updateSchedule(); 
	  updateCourseImage();
	  
  }

  // Will be called via the onClick attribute
  // of the buttons in main.xml
  public void onClick(View view) {
	  
    switch (view.getId()) {
	    case R.id.next: goUp();
	      break;
	    case R.id.last: goDown();
	      break;
	    case R.id.go_in: goIn();
	      break;
	    case R.id.options: goOptions();
	      break;
	    case R.id.social: goSocial();
	      break;
    }
  }
  
  private void goUp(){
	  if(activeEventNumber<allEvents.size()-1){
  		activeEventNumber++;
  		updateEvent();
  	}
  }
  
  private void goDown(){
	  if(activeEventNumber>0){
  		activeEventNumber--;
  		updateEvent();
  	}
  }
  
  private void goIn(){
	  Intent intent;
	  if(isUpcoming()){
		  intent = new Intent(SelectionActivity.this, ActionActivity.class);
	  }
	  else{
		  intent = new Intent(SelectionActivity.this, StatisticsActivity.class);
	  }
	  startActivity(intent);
  }
  
  private void goSocial(){
	  Intent intent = new Intent(SelectionActivity.this, SocialActivity.class);
	  startActivity(intent);
  }
  
  private void goOptions(){
	  Intent intent = new Intent(SelectionActivity.this, OptionsActivity.class);
	  startActivity(intent);
  }

  @Override
  protected void onResume() {
    datasource.open();
    super.onResume();
  }

  @Override
  protected void onPause() {
    datasource.close();
    super.onPause();
  }
  
  private boolean isUpcoming(){
	  Date now = new Date();
	  return allEvents.get(activeEventNumber).getDate().after(now);
  }
  
  private void updateCourseImage(){
	  ImageView imageViewCourse = (ImageView)findViewById(R.id.course_image);
	  if(allEvents.get(activeEventNumber).getCourse().equals("Nord - kurz")){
		  imageViewCourse.setImageResource(R.drawable.bn_nord_kurz);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("Nord - kurz")){
		  imageViewCourse.setImageResource(R.drawable.bn_nord_kurz);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("Nord - lang")){
		  imageViewCourse.setImageResource(R.drawable.bn_nord_lang);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("West - kurz")){
		  imageViewCourse.setImageResource(R.drawable.bn_west_kurz);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("West - lang")){
		  imageViewCourse.setImageResource(R.drawable.bn_west_lang);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("Ost - kurz")){
		  imageViewCourse.setImageResource(R.drawable.bn_ost_kurz);
	  }
	  else if(allEvents.get(activeEventNumber).getCourse().equals("Ost - lang")){
		  imageViewCourse.setImageResource(R.drawable.bn_ost_lang);
	  }
	  else {
		  throw new Error("This course is not valid.");
	  }
  
  }
  
  private void updateStatus(){
	  ImageView imageViewStatus = (ImageView)findViewById(R.id.status);
	  if(allEvents.get(activeEventNumber).getStatus().equals("confirmed")){
		  imageViewStatus.setImageResource(R.drawable.ic_status_confirmed);
	  }
	  else if(allEvents.get(activeEventNumber).getStatus().equals("pending")){
		  imageViewStatus.setImageResource(R.drawable.ic_status_pending);
	  }
	  else if(allEvents.get(activeEventNumber).getStatus().equals("cancelled")){
		  imageViewStatus.setImageResource(R.drawable.ic_status_cancelled);
	  }
	  else{
		  throw new Error("This status is not valid");
	  }
  }

  private void updateSchedule(){
	  LinearLayout topgroup = (LinearLayout) findViewById(R.id.group_top);
	  Button buttonGoIn = (Button) findViewById(R.id.go_in);
	  if(isUpcoming()){
		  topgroup.setBackgroundResource(R.drawable.border_green);
		  buttonGoIn.setText(R.string.button_action);
	  }
	  else{
		  topgroup.setBackgroundResource(R.drawable.border_orange);
		  buttonGoIn.setText(R.string.button_statistics);
	  }
  }
  
} 