package de.greencity.bladenightapp.android.selection;



import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.messages.EventMessage;

public class EventFragment extends Fragment {

	public EventFragment(){
		super();
	}

	public void setParameters(ViewPager viewPager, EventMessage event, boolean hasLeft, boolean hasRight) {
		this.event = event;
		this.hasLeft = hasLeft;
		this.hasRight = hasRight;
		this.startDateTime = fromDateFormat.parseDateTime(event.getStartDate()); 
	}

	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
	}

	public void setEventMessage(EventMessage eventMessage) {
		this.event = eventMessage;
	}

	public void hasLeft(boolean hasLeft) {
		this.hasLeft = hasLeft;
	}

	public void hasRight(boolean hasRight) {
		this.hasRight = hasRight;
	}

	public void isCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public void hasStatistics(boolean hasStatistics) {
		this.hasStatistics = hasStatistics;
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

		//		setColor(R.id.image_event_observe, getResources().getColor(R.color.bn_green));
		//		setColor(R.id.image_event_participate, getResources().getColor(R.color.bn_green));
		//		setColor(R.id.image_event_statistics, getResources().getColor(R.color.bn_green));

		View leftArrowView = view.findViewById(R.id.arrow_left);
		leftArrowView.setClickable(true);
		leftArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(viewPager.getCurrentItem()-1, true);
			}
		});

		View rightArrowView = view.findViewById(R.id.arrow_right);
		rightArrowView.setClickable(true);
		rightArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(viewPager.getCurrentItem()+1, true);
			}
		});

		View observeImage = view.findViewById(R.id.image_event_observe);
		observeImage.setClickable(true);
		observeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startMapActivity();
			}
		});

		View participateImage = view.findViewById(R.id.image_event_participate);
		participateImage.setClickable(true);
		participateImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ServiceUtils.startService(getActivity(), GpsTrackerService.class);
				startMapActivity();
			}
		});

		//tmp till statistics implemented
		//if ( ! hasStatistics )
			view.findViewById(R.id.image_event_statistics).setEnabled(false);

		if ( ! isCurrent )
			view.findViewById(R.id.image_event_participate).setEnabled(false);

		updateEvent();
		return view;
	}

	protected void setColor(int imageId, int color) {
		Log.i(TAG, "imageId="+imageId);
		Log.i(TAG, "color="+color);
		ImageView imageView = (ImageView)view.findViewById(imageId);
		if (imageView == null) {
			Log.e(TAG, "Failed to get " + imageId + " has ImageView");
			return;
		}
		imageView.setColorFilter(color, Mode.MULTIPLY);
	}


	private void updateEvent(){
		if ( event == null ) {
			Log.e(TAG, "updateEvent: event=" + event);
			return;
		}
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

	private void startMapActivity() {
		Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
		if ( event == null ) {
			Log.e(TAG, "No event currently shown");
			return;
		}
		if ( ! isCurrent )
			// BladenightMapActivity will assume the current route if not specified
			intent.putExtra(BladenightMapActivity.PARAM_ROUTENAME, event.getRouteName());
		view.getContext().startActivity(intent);
	}

	private void updateStatus(){
		ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
		switch (event.getStatus()) {
		case CAN:
			imageViewStatus.setImageResource(R.drawable.traffic_light_red);
			break;
		case CON:
			imageViewStatus.setImageResource(R.drawable.traffic_light_green);
			break;
		case PEN:
			if(isCurrent)
				imageViewStatus.setImageResource(R.drawable.traffic_light_orange);
			break;
		default:
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
			topgroup.setBackgroundResource(R.drawable.border_green);
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
	private boolean isCurrent;
	private boolean hasStatistics;
	private ViewPager viewPager;
	private DateTimeFormatter fromDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
	private static DateTimeFormatter toDateFormat = getDestinationDateFormatter(Locale.getDefault());
	final static String TAG = "EventFragment";
}
