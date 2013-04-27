package de.greencity.bladenightapp.android.social;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.social.SocialActivity.GetRealTimeDataFromServerHandler;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;

public class FriendListAdapter extends BaseAdapter {
	private static LayoutInflater inflater=null;
	private SocialActivity activity;
	private boolean isServiceRunning;

	public FriendListAdapter(Activity activity) {
		this.activity = (SocialActivity) activity;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return activity.sortedFriendIdsToDisplay.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view=convertView;
		int friendId = activity.sortedFriendIdsToDisplay.get(position);
		Friend friend = activity.friends.get(friendId);

		isServiceRunning = ServiceUtils.isServiceRunning(activity, GpsTrackerService.class);
		if( isServiceRunning ){
			view = inflateActionRow(friendId, friend);
		}
		else{
			view = inflateOfflineRow(friendId, friend);
		}

		LinearLayout row = (LinearLayout)view.findViewById(R.id.row_friend);
		row.setTag(friendId);

		return view;
	}


	private View inflateActionRow(int friendId, Friend friend){
		View view = inflater.inflate(R.layout.friend_list_row_action, null);

		ImageView colorBlockImageView = (ImageView)view.findViewById(R.id.action_color_block);
		setColorForBlock(colorBlockImageView, friendId, friend);
		
		TextView name = (TextView)view.findViewById(R.id.action_row_friend_name); 
		TextView textViewRelativeTime = (TextView)view.findViewById(R.id.action_row_time_rel); 
		TextView textViewRelativeDistance = (TextView)view.findViewById(R.id.action_row_distance_rel); 
		TextView textViewAbsolutePosition = (TextView)view.findViewById(R.id.action_row_distance_abs); 

		
		name.setText(friend.getName());
		if ( friend.getRelativeTime() != null)
			textViewRelativeTime.setText(formatTime(friend.getRelativeTime()));
		else
			textViewRelativeTime.setText("-");
		if ( friend.getRelativeDistance() != null)
			textViewRelativeDistance.setText(formatDistance(friend.getRelativeDistance()));
		else
			textViewRelativeDistance.setText("-");
		if ( friend.getAbsolutePosition() != null)
			textViewAbsolutePosition.setText(formatDistance(friend.getAbsolutePosition()));
		else
			textViewAbsolutePosition.setText("-");
		return view;
	}

	private View inflateOfflineRow(int friendId, Friend friend){

		View view = inflater.inflate(R.layout.friend_list_row, null);

		ImageView colorBlockImageView = (ImageView)view.findViewById(R.id.color_block);
		setColorForBlock(colorBlockImageView, friendId, friend);

		TextView textViewName = (TextView)view.findViewById(R.id.row_friend_name); 
		TextView textViewStatus = (TextView)view.findViewById(R.id.row_friend_status); 

		// Setting all values in listview
		textViewName.setText(friend.getName());
		updateStatus(friend, textViewStatus );

		return view;
	}
	
	private void setColorForBlock(ImageView colorBlockImageView, int friendId, Friend friend) {
		colorBlockImageView.setBackgroundColor(friend.getColor());
		if ( friendId == SocialActivity.ID_HEAD ||  friendId == SocialActivity.ID_TAIL )
			colorBlockImageView.setVisibility(View.INVISIBLE);
	}
	
	private void updateStatus(Friend friend, TextView textViewStatus) {
		String statustext = textViewStatus.getResources().getString(R.string.status_active);
		if ( ! friend.isValid() ) {
			statustext = textViewStatus.getResources().getString(R.string.status_obsolete);
		}
		else if ( friend.getRequestId() > 0 ) {
			statustext = textViewStatus.getResources().getString(R.string.status_pending) + " (" + SocialActivity.formatRequestId(friend.getRequestId()) + ")";
		}
		else if ( ! friend.isActive() ) {
			statustext = textViewStatus.getResources().getString(R.string.status_inactive);
		}
//		else if ( friend.isOnline() )
//			statustext = "online";
//		else
//			statustext = "offline";
		textViewStatus.setText(statustext);
	}

	private String formatTime(long timeInMilliseconds){
		String sign = ( timeInMilliseconds >=0 ? "" : "-");
		long timeInSeconds = Math.abs(timeInMilliseconds/1000);
		String sec = Long.toString(timeInSeconds%60);
		if (sec.length()==1)
			sec = "0" + sec;
		String string = Long.toString(timeInSeconds/60) + ":" + sec;
		return sign + string;
	}

	private String formatDistance(long meters){
		String distance = Long.toString(meters) + "m";
		return distance;
	}

	private static final String TAG = "FriendListAdapter"; 

}






