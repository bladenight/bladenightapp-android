package de.greencity.bladenightapp.android.selection;



import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.network.messages.EventMessage;

public class EventFragment extends Fragment {

	public EventFragment(){
		super();
	}
	
	public void setParameters(EventMessage event, boolean hasLeft, boolean hasRight) {
		this.event = event;
		this.hasLeft = hasLeft;
		this.hasRight = hasRight;
		this.startDateTime = fromDateFormat.parseDateTime(event.getStartDate()); 
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		textViewCourse.setText(event.getRouteName());
		TextView textViewDate = (TextView)view.findViewById(R.id.date);
		textViewDate.setText(toDateFormat.print(startDateTime));
		TextView textViewLeft = (TextView)view.findViewById(R.id.arrow_left);
		textViewLeft.setText(hasLeft ? R.string.arrow_left : R.string.arrow_no);
		TextView textViewRight = (TextView)view.findViewById(R.id.arrow_right);
		textViewRight.setText(hasRight ? R.string.arrow_right : R.string.arrow_no);

		updateStatus();
		updateSchedule(); 
	}


	private void updateStatus(){
		ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
		switch (event.getStatus()) {
		case CAN:
			imageViewStatus.setImageResource(R.drawable.icon_no);
			break;
		case CON:
			imageViewStatus.setImageResource(R.drawable.icon_ok);
			break;
		case PEN:
			imageViewStatus.setImageResource(R.drawable.icon_pending);
			break;
		default:
			throw new Error("This status is not valid");
		}
	}

	private void updateSchedule(){
		LinearLayout topgroup = (LinearLayout) view.findViewById(R.id.group_top);
		if(isUpcoming()){
			// topgroup.setBackgroundResource(R.drawable.border_green);
			topgroup.setBackgroundResource(R.drawable.border_white);
			topgroup.setTag("upcoming");
		}
		else{
			// topgroup.setBackgroundResource(R.drawable.border_orange);
			topgroup.setBackgroundResource(R.drawable.border_white);
			topgroup.setTag("old");
		}
	}

	private boolean isUpcoming(){
		return startDateTime.isAfterNow();
	}

	private static DateTimeFormatter getDestinationDateFormatter(Locale locale) {
		String country = locale.getISO3Country();
		String localString = locale.toString();
		Log.i(TAG,"localString="+localString + " / country="+country);
		if ( localString.startsWith("de") ||  "DEU".equals(country) ) {
			return DateTimeFormat.forPattern("dd. MMM YY, HH:mm").withLocale(locale);
		}
		if ( localString.startsWith("fr") ||  "FRA".equals(country) ) {
			return DateTimeFormat.forPattern("dd MMM YY, HH:mm").withLocale(locale);
		}
		if ( localString.startsWith("en") ||  "USA".equals(country) ) {
			return DateTimeFormat.forStyle("MS").withLocale(locale);
		}
		else {
			return DateTimeFormat.forStyle("MS").withLocale(locale);
		}
	}
	
	private EventMessage event;
	private DateTime startDateTime;
	private View view;
	private boolean hasRight;
	private boolean hasLeft;
	private DateTimeFormatter fromDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
	private static DateTimeFormatter toDateFormat = getDestinationDateFormatter(Locale.getDefault());
	final static String TAG = "EventFragment";
}
