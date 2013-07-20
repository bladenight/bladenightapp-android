package de.greencity.bladenightapp.android.selection;

import java.util.Locale;

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
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;

public class EventFragment extends Fragment {

	public EventFragment(){
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "onCreate: " + this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		configureFromBundle(getArguments());
		// Log.i(TAG, "onActivityCreated: " + this);
	}

	public void configureFromBundle(Bundle bundle) {
		// Log.i(TAG, "configureFromBundle: savedInstanceState="+bundle);
		if ( bundle == null ) {
			// Log.i(TAG, "configureFromBundle: savedInstanceState="+bundle);
			// Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
			return;
		}
		event = (Event) bundle.getSerializable(PARAM_EVENT_MESSAGE);
		hasLeft = bundle.getBoolean(PARAM_HAS_LEFT);
		hasRight = bundle.getBoolean(PARAM_HAS_RIGHT);
		hasStatistics = bundle.getBoolean(PARAM_HAS_STATISTICS);
		showStatus = bundle.getBoolean(PARAM_IS_SHOW_STATUS);
		allowParticipate = bundle.getBoolean(PARAM_ALLOW_PARTICIPATE);
		updateUi();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Log.i(TAG, "onCreateView: this="+this);

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
		
		View statisticsImage = view.findViewById(R.id.image_event_statistics);
		statisticsImage.setClickable(true);
		statisticsImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startStatisticsActivity();
			}
		});

		return view;
	}

	public static Bundle prepareBundle(Event eventMessage, boolean hasLeft, boolean hasRight, boolean hasStatistics, boolean showStatus, boolean allowParticipate) {
		return prepareBundle(new Bundle(), eventMessage, hasLeft, hasRight, hasStatistics, showStatus, allowParticipate);
	}

	public static Bundle prepareBundle(Bundle bundle, Event eventMessage, boolean hasLeft, boolean hasRight, boolean hasStatistics, boolean showStatus, boolean allowParticipate) {
		bundle.putSerializable(PARAM_EVENT_MESSAGE, eventMessage);
		bundle.putBoolean(PARAM_HAS_LEFT, hasLeft);
		bundle.putBoolean(PARAM_HAS_RIGHT, hasRight);
		bundle.putBoolean(PARAM_IS_SHOW_STATUS, showStatus);
		bundle.putBoolean(PARAM_ALLOW_PARTICIPATE, allowParticipate);
		bundle.putBoolean(PARAM_HAS_STATISTICS, hasStatistics);
		return bundle;
	}



	protected void setColor(int imageId, int color) {
		// Log.i(TAG, "imageId="+imageId);
		// Log.i(TAG, "color="+color);
		ImageView imageView = (ImageView)view.findViewById(imageId);
		if (imageView == null) {
			Log.e(TAG, "Failed to get " + imageId + " has ImageView");
			return;
		}
		imageView.setColorFilter(color, Mode.MULTIPLY);
	}


	private void updateUi(){
		// Log.i(TAG, "updateUi: event=" + event);
		if ( event == null ) {
			Log.i(TAG, "Trace: " +Log.getStackTraceString(new Throwable()) );
			return;
		}

		TextView textViewCourse = (TextView)view.findViewById(R.id.course);
		textViewCourse.setText(routeNameToText(event.getRouteName()));

		TextView textViewDate = (TextView)view.findViewById(R.id.date);
		textViewDate.setText(toDateFormat.print(event.getStartDate()));

		// not sure anymore if participants are necessary on the main screen
//		if ( eventMessage.getParticipants() > 0 ) {
//			TextView textViewParticipants = (TextView)view.findViewById(R.id.participants);
//			textViewParticipants.setText(eventMessage.getParticipants() + " " + getActivity().getResources().getString(R.string.msg_participants));
//		}

		TextView textViewLeft = (TextView)view.findViewById(R.id.arrow_left);
		textViewLeft.setText(hasLeft ? R.string.arrow_left : R.string.arrow_no);

		TextView textViewRight = (TextView)view.findViewById(R.id.arrow_right);
		textViewRight.setText(hasRight ? R.string.arrow_right : R.string.arrow_no);


		view.findViewById(R.id.image_event_statistics).setEnabled(hasStatistics);

		view.findViewById(R.id.image_event_participate).setEnabled(allowParticipate);

		if(showStatus)
			updateStatus();
		updateSchedule(); 
	}

	private void startMapActivity() {
		Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
		if ( event.getRouteName() == null) {
			Log.e(TAG, "No event or no route available");
			return;
		}
		intent.putExtra(BladenightMapActivity.PARAM_EVENT_MESSAGE, EventGsonHelper.toJson(event));
		view.getContext().startActivity(intent);
	}
	
	private void startStatisticsActivity() {
		Intent intent = new Intent(view.getContext(), StatisticsActivity.class);
		view.getContext().startActivity(intent);
	}

	private void updateStatus(){
		ImageView imageViewStatus = (ImageView)view.findViewById(R.id.status);
		switch (event.getStatus()) {
		case CANCELLED:
			imageViewStatus.setImageResource(R.drawable.light_red);
			break;
		case CONFIRMED:
			imageViewStatus.setImageResource(R.drawable.light_green);
			break;
		case PENDING:
			imageViewStatus.setImageResource(R.drawable.light_yellow);
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
		return event.getStartDate().isAfterNow();
	}

	private static DateTimeFormatter getDestinationDateFormatter(Locale locale) {
		String country = locale.getISO3Country();
		String localString = locale.toString();
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
	
	private String routeNameToText(String routeName){
		if (routeName.equals("Nord - kurz")){
			return view.getResources().getString(R.string.course_north_short);
		}
		if (routeName.equals("Nord - lang")){
			return view.getResources().getString(R.string.course_north_long);
		}
		if (routeName.equals("West - kurz")){
			return view.getResources().getString(R.string.course_west_short);
		}
		if (routeName.equals("West - lang")){
			return view.getResources().getString(R.string.course_west_long);
		}
		if (routeName.equals("Ost - kurz")){
			return view.getResources().getString(R.string.course_east_short);
		}
		if (routeName.equals("Ost - lang")){
			return view.getResources().getString(R.string.course_east_long);
		}
		if (routeName.equals("Familie")){
			return view.getResources().getString(R.string.course_family);
		}
		return routeName;
	}

	private View view;
	private boolean hasRight;
	private boolean hasLeft;
	private boolean hasStatistics;
	private boolean showStatus;
	private boolean allowParticipate;
	private Event event;

	static public final String PARAM_HAS_RIGHT = "hasRight";
	static public final String PARAM_HAS_LEFT = "hasLeft";
	static public final String PARAM_HAS_STATISTICS = "hasStatistics";
	static public final String PARAM_IS_SHOW_STATUS = "showStatus";
	static public final String PARAM_ALLOW_PARTICIPATE = "allowParticipate";
	static public final String PARAM_EVENT_MESSAGE = "eventMessage";


	private static DateTimeFormatter toDateFormat = getDestinationDateFormatter(Locale.getDefault());
	final static String TAG = "EventFragment";
}
