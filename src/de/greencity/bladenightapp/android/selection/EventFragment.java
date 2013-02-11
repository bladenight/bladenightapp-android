package de.greencity.bladenightapp.android.selection;


 
import java.util.Date;

import de.greencity.bladenightapp.android.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public class EventFragment extends Fragment {
	private Event event;
	private View view;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public EventFragment(Event event){
    	super();
    	this.event = event;
    }
 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
 
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.event_view, container, false); 
        updateEvent();
        return view;
    }
    
    private void updateEvent(){
  	  
  	  TextView textViewCourse = (TextView)view.findViewById(R.id.course);
  	  textViewCourse.setText(event.getCourse());
  	  TextView textViewDate = (TextView)view.findViewById(R.id.date);
  	  textViewDate.setText(event.getDateFormatted());
  	  TextView textViewLength = (TextView)view.findViewById(R.id.length);
  	  textViewLength.setText(event.getLength());
  	  
  	  updateStatus();
  	  updateSchedule(); 
  	  updateCourseImage();
  	  
    }
    
    private void updateCourseImage(){
  	  ImageView imageViewCourse = (ImageView)view.findViewById(R.id.course_image);
  	  if(event.getCourse().equals("Nord - kurz")){
  		  imageViewCourse.setImageResource(R.drawable.bn_nord_kurz);
  	  }
  	  else if(event.getCourse().equals("Nord - kurz")){
  		  imageViewCourse.setImageResource(R.drawable.bn_nord_kurz);
  	  }
  	  else if(event.getCourse().equals("Nord - lang")){
  		  imageViewCourse.setImageResource(R.drawable.bn_nord_lang);
  	  }
  	  else if(event.getCourse().equals("West - kurz")){
  		  imageViewCourse.setImageResource(R.drawable.bn_west_kurz);
  	  }
  	  else if(event.getCourse().equals("West - lang")){
  		  imageViewCourse.setImageResource(R.drawable.bn_west_lang);
  	  }
  	  else if(event.getCourse().equals("Ost - kurz")){
  		  imageViewCourse.setImageResource(R.drawable.bn_ost_kurz);
  	  }
  	  else if(event.getCourse().equals("Ost - lang")){
  		  imageViewCourse.setImageResource(R.drawable.bn_ost_lang);
  	  }
  	  else {
  		  throw new Error("This course is not valid.");
  	  }
    
    }
    
    private void updateStatus(){
  	  ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
  	  if(event.getStatus().equals("confirmed")){
  		  imageViewStatus.setImageResource(R.drawable.ic_status_confirmed);
  	  }
  	  else if(event.getStatus().equals("pending")){
  		  imageViewStatus.setImageResource(R.drawable.ic_status_pending);
  	  }
  	  else if(event.getStatus().equals("cancelled")){
  		  imageViewStatus.setImageResource(R.drawable.ic_status_cancelled);
  	  }
  	  else{
  		  throw new Error("This status is not valid");
  	  }
    }

    private void updateSchedule(){
  	  LinearLayout topgroup = (LinearLayout) view.findViewById(R.id.group_top);
  	  if(isUpcoming()){
  		  topgroup.setBackgroundResource(R.drawable.border_green);
  		  topgroup.setTag("upcoming");
  	  }
  	  else{
  		  topgroup.setBackgroundResource(R.drawable.border_orange);
  		  topgroup.setTag("old");
  	  }
    }
    
    private boolean isUpcoming(){
  	  Date now = new Date();
  	  return event.getDate().after(now);
    }
}
