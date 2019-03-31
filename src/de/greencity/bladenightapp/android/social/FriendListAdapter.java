package de.greencity.bladenightapp.android.social;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.android.R;

public class FriendListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private SocialActivity activity;

    public FriendListAdapter(Activity activity) {
        this.activity = (SocialActivity) activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View view = convertView;
        int friendId = activity.sortedFriendIdsToDisplay.get(position);
        Friend friend = activity.friends.get(friendId);

        if (activity.isEventActive) {
            view = inflateActionRow(friendId, friend);
        } else {
            view = inflateOfflineRow(friendId, friend);
        }

        LinearLayout row = (LinearLayout) view.findViewById(R.id.row_friend);
        row.setTag(friendId);

        return view;
    }


    private View inflateActionRow(int friendId, Friend friend) {
        View view = inflater.inflate(R.layout.friend_list_row_action, null);

        TextView textViewName = (TextView) view.findViewById(R.id.action_row_friend_name);
        TextView textViewRelativeTime = (TextView) view.findViewById(R.id.action_row_time_rel);
        TextView textViewRelativeDistance = (TextView) view.findViewById(R.id.action_row_distance_rel);
        TextView textViewAbsolutePosition = (TextView) view.findViewById(R.id.action_row_distance_abs);

        int textColor = Friends.getFriendColorOrDefault(activity, friendId, friend);

        if (friendId == SocialActivity.ID_HEAD || friendId == SocialActivity.ID_TAIL) {
            View row = (View) view.findViewById(R.id.row_friend);
            row.setBackgroundColor(view.getResources().getColor(R.color.dialog_grey));
            textColor = view.getResources().getColor(R.color.bn_white);
        }
        if (friendId == SocialActivity.ID_ME) {
            friend.setColor(textColor);
        }

        textViewRelativeTime.setTextColor(textColor);
        textViewRelativeDistance.setTextColor(textColor);
        textViewAbsolutePosition.setTextColor(textColor);
        textViewName.setTextColor(textColor);

        ImageView colorBlockImageView = (ImageView) view.findViewById(R.id.action_color_block);
        setColorForBlock(colorBlockImageView, friendId, friend);

        textViewName.setText(friend.getName());
        if (friend.getRelativeTime() != null)
            textViewRelativeTime.setText(formatTime(friend.getRelativeTime()));
        else
            textViewRelativeTime.setText("-");
        if (friend.getRelativeDistance() != null)
            textViewRelativeDistance.setText(formatDistance(friend.getRelativeDistance()));
        else
            textViewRelativeDistance.setText("-");
        if (friend.getAbsolutePosition() != null)
            textViewAbsolutePosition.setText(formatDistance(friend.getAbsolutePosition()));
        else
            textViewAbsolutePosition.setText("-");
        return view;
    }

    private View inflateOfflineRow(int friendId, Friend friend) {

        View view = inflater.inflate(R.layout.friend_list_row, null);

        ImageView colorBlockImageView = (ImageView) view.findViewById(R.id.color_block);
        setColorForBlock(colorBlockImageView, friendId, friend);

        TextView textViewName = (TextView) view.findViewById(R.id.row_friend_name);
        TextView textViewStatus = (TextView) view.findViewById(R.id.row_friend_status);

        // Setting all values in listview
        textViewName.setText(friend.getName());
        updateStatus(friend, textViewStatus);

        return view;
    }

    private void setColorForBlock(ImageView colorBlockImageView, int friendId, Friend friend) {
        if (friendId == SocialActivity.ID_HEAD || friendId == SocialActivity.ID_TAIL)
            colorBlockImageView.setVisibility(View.INVISIBLE);
        else
            colorBlockImageView.setBackgroundColor(Friends.getFriendColorOrDefault(activity, friendId, friend));
    }

    private void updateStatus(Friend friend, TextView textViewStatus) {
        String statustext = textViewStatus.getResources().getString(R.string.status_active);
        if (!friend.isValid()) {
            statustext = textViewStatus.getResources().getString(R.string.status_obsolete);
        } else if (friend.getRequestId() > 0) {
            statustext = textViewStatus.getResources().getString(R.string.status_pending) + " (" + SocialActivity.formatRequestId(friend.getRequestId()) + ")";
        } else if (!friend.isActive()) {
            statustext = textViewStatus.getResources().getString(R.string.status_inactive);
        }
        //      else if ( friend.isOnline() )
        //          statustext = "online";
        //      else
        //          statustext = "offline";
        textViewStatus.setText(statustext);
    }

    private String formatTime(long timeInMilliseconds) {
        String sign = (timeInMilliseconds >= 0 ? "" : "-");
        long timeInSeconds = Math.abs(timeInMilliseconds / 1000);
        String sec = Long.toString(timeInSeconds % 60);
        if (sec.length() == 1)
            sec = "0" + sec;
        String string = Long.toString(timeInSeconds / 60) + ":" + sec;
        return sign + string;
    }

    @SuppressLint("DefaultLocale")
    private String formatDistance(long meters) {
        String s = "-";
        if (Math.abs(meters) < 1000) {
            s = Long.toString(meters) + "m";
        } else {
            double km = meters / 1000.0;
            s = String.format("%.2fk", km);
        }

        return s;
    }

    @SuppressWarnings("unused")
    private static final String TAG = "FriendListAdapter";

}






