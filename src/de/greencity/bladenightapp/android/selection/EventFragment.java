package de.greencity.bladenightapp.android.selection;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;

public class EventFragment extends Fragment {
	private Event event;
	private View view;
	private boolean hasRight;
	private boolean hasLeft;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public EventFragment(Event event, boolean hasLeft, boolean hasRight){
		super();
		this.event = event;
		this.hasLeft = hasLeft;
		this.hasRight = hasRight;
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
		TextView textViewLeft = (TextView)view.findViewById(R.id.arrow_left);
		textViewLeft.setText(hasLeft ? R.string.arrow_left : R.string.arrow_no);
		TextView textViewRight = (TextView)view.findViewById(R.id.arrow_right);
		textViewRight.setText(hasRight ? R.string.arrow_right : R.string.arrow_no);

		//  	  TextView textViewLength = (TextView)view.findViewById(R.id.length);
		//  	  textViewLength.setText(event.getLength());

		updateStatus();
		updateSchedule(); 

	}


	private void updateStatus(){
		ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
		if(event.getStatus().equals("confirmed")){
			imageViewStatus.setImageResource(R.drawable.icon_ok);
		}
		else if(event.getStatus().equals("pending")){
			imageViewStatus.setImageResource(R.drawable.icon_pending);
		}
		else if(event.getStatus().equals("cancelled")){
			imageViewStatus.setImageResource(R.drawable.icon_no);
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
		return event.getDate().isAfterNow();
	}
}
