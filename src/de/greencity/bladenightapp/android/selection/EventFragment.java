package de.greencity.bladenightapp.android.selection;



import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.messages.EventMessage;

public class EventFragment extends Fragment {

	public EventFragment(){
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		configureFromBundle(getArguments());
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		configureFromBundle(savedInstanceState);
	}

	public void configureFromBundle(Bundle bundle) {
		if ( bundle == null ) {
			Log.e(TAG, "onViewStateRestored: savedInstanceState="+bundle);
			return;
		}
		eventMessage = (EventMessage) bundle.getSerializable("eventMessage");
		hasLeft = bundle.getBoolean("hasLeft");
		hasRight = bundle.getBoolean("hasRight");
		isCurrent = bundle.getBoolean("isCurrent");
		startDateTime = fromDateFormat.parseDateTime(eventMessage.getStartDate());
		updateUi();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.event_view, container, false); 

		View leftArrowView = view.findViewById(R.id.arrow_left);
		leftArrowView.setClickable(true);
		leftArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((SelectionActivity)getActivity()).showPreviousEvent();
			}
		});

		View rightArrowView = view.findViewById(R.id.arrow_right);
		rightArrowView.setClickable(true);
		rightArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((SelectionActivity)getActivity()).showNextEvent();
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

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		prepareBundle(savedInstanceState, eventMessage, hasLeft, hasRight, isCurrent);
	}


	public static Bundle prepareBundle(EventMessage eventMessage, boolean hasLeft, boolean hasRight, boolean isCurrent) {
		return prepareBundle(new Bundle(), eventMessage, hasLeft, hasRight, isCurrent);
	}

	public static Bundle prepareBundle(Bundle bundle, EventMessage eventMessage, boolean hasLeft, boolean hasRight, boolean isCurrent) {
		bundle.putSerializable("eventMessage", eventMessage);
		bundle.putBoolean("hasLeft", hasLeft);
		bundle.putBoolean("hasRight", hasRight);
		bundle.putBoolean("isCurrent", isCurrent);
		return bundle;
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


	private void updateUi(){
		if ( eventMessage == null ) {
			Log.e(TAG, "updateUi: event=" + eventMessage);
			Log.i(TAG, "Trace: " +Log.getStackTraceString(new Throwable()) );
			return;
		}

		TextView textViewCourse = (TextView)view.findViewById(R.id.course);
		textViewCourse.setText(eventMessage.getRouteName());

		TextView textViewDate = (TextView)view.findViewById(R.id.date);
		textViewDate.setText(toDateFormat.print(startDateTime));

		if ( eventMessage.getParticipantsCount() > 0 ) {
			TextView textViewParticipants = (TextView)view.findViewById(R.id.participants);
			textViewParticipants.setText(eventMessage.getParticipantsCount() + " " + getActivity().getResources().getString(R.string.msg_participants));
		}

		TextView textViewLeft = (TextView)view.findViewById(R.id.arrow_left);
		textViewLeft.setText(hasLeft ? R.string.arrow_left : R.string.arrow_no);

		TextView textViewRight = (TextView)view.findViewById(R.id.arrow_right);
		textViewRight.setText(hasRight ? R.string.arrow_right : R.string.arrow_no);

		//tmp till statistics implemented
		//if ( ! hasStatistics )
		view.findViewById(R.id.image_event_statistics).setEnabled(false);

		if ( ! isCurrent )
			view.findViewById(R.id.image_event_participate).setEnabled(false);

		updateStatus();
		updateSchedule(); 
	}

	private void startMapActivity() {
		Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
		if ( eventMessage.getRouteName() == null) {
			Log.e(TAG, "No event or no route available");
			return;
		}
		if ( ! isCurrent )
			// BladenightMapActivity will assume the current route if not specified
			intent.putExtra(BladenightMapActivity.PARAM_ROUTENAME, eventMessage.getRouteName());
		view.getContext().startActivity(intent);
	}

	private void updateStatus(){
		ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
		switch (eventMessage.getStatus()) {
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

	private DateTime startDateTime;
	private View view;
	private boolean hasRight;
	private boolean hasLeft;
	private boolean isCurrent;
	private EventMessage eventMessage;


	private DateTimeFormatter fromDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
	private static DateTimeFormatter toDateFormat = getDestinationDateFormatter(Locale.getDefault());
	final static String TAG = "EventFragment";
}
