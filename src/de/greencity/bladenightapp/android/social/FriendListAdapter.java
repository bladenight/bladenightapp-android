package de.greencity.bladenightapp.android.social;

import java.util.List;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.Friend.FriendColor;
 
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendListAdapter extends BaseAdapter {
    private static LayoutInflater inflater=null;
    private SocialActivity activity;
 
    public FriendListAdapter(Activity activity) {
    	this.activity = (SocialActivity) activity;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
    	return activity.id_order.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        int id = activity.id_order.get(position);
        Friend friend = activity.friends.get(id);
        
        if(activity.is_in_action){
        	vi = inflateActionRow(friend);
        }
        else{
        	vi = inflateOfflineRow(friend);
        }
        
        LinearLayout row = (LinearLayout)vi.findViewById(R.id.row_friend);
        row.setTag(id);
        
        if(id==SocialActivity.ID_ME){
        	row.setBackgroundColor(vi.getResources().getColor(R.color.bn_orange2));
        }
        
        return vi;
    }
    
    private int ColorToInt(FriendColor color){
    	int exit = 0;
    	if(color.equals(FriendColor.ORANGE)) exit = R.color.bn_orange;
    	if(color.equals(FriendColor.RED)) exit = R.color.bn_red;
    	if(color.equals(FriendColor.BLUE)) exit = R.color.bn_blue;
    	if(color.equals(FriendColor.GREEN)) exit = R.color.bn_green;
    	if(color.equals(FriendColor.GREEN_LIGHT)) exit = R.color.bn_green_light;
    	if(color.equals(FriendColor.BLACK)) exit = R.color.black;
    	return exit;
    }
    
    private View inflateActionRow(Friend friend){
    	
    	
        View vi = inflater.inflate(R.layout.friend_list_row_action, null);
           
    	
 
        ImageView color_block=(ImageView)vi.findViewById(R.id.action_color_block);
        TextView name = (TextView)vi.findViewById(R.id.action_row_friend_name); 
        TextView time_rel = (TextView)vi.findViewById(R.id.action_row_time_rel); 
        TextView distance_rel = (TextView)vi.findViewById(R.id.action_row_distance_rel); 
        TextView time_abs = (TextView)vi.findViewById(R.id.action_row_time_abs); 
        TextView distance_abs = (TextView)vi.findViewById(R.id.action_row_distance_abs); 
 
        // Setting all values in listview
        color_block.setBackgroundColor(vi.getResources().getColor(ColorToInt(friend.getColor())));
        name.setText(friend.getName());
        time_rel.setText("+ " + secondsToTime(friend.getTimeRel()));
        distance_rel.setText("+ " + metersToDistance(friend.getDistanceRel()));
        time_abs.setText(secondsToTime(friend.getTimeAbs()));
        distance_abs.setText(metersToDistance(friend.getDistanceAbs()));
        return vi;
    }
    
    private View inflateOfflineRow(Friend friend){
    	
        View vi = inflater.inflate(R.layout.friend_list_row, null);
 
        TextView name = (TextView)vi.findViewById(R.id.row_friend_name); 
        TextView status = (TextView)vi.findViewById(R.id.row_friend_status); 
        //TextView counter = (TextView)vi.findViewById(R.id.bn_counter);
        ImageView color_block=(ImageView)vi.findViewById(R.id.color_block); 
 
        // Setting all values in listview
        name.setText(friend.getName());
        String statustext = "active";
        if(!friend.getActive()) statustext = "inactive";
        status.setText(statustext);
        color_block.setBackgroundColor(vi.getResources().getColor(ColorToInt(friend.getColor())));
        return vi;
    }
    
    private String secondsToTime(int secs){
    	String sec = Integer.toString(secs%60);
    	if (sec.length()==1) sec = "0" + sec;
    	String time = Integer.toString(secs/60) + ":" + sec;
    	return time;
    }
    
    private String metersToDistance(int meters){
    	String distance = Integer.toString(meters) + "m";
    	return distance;
    }
    

}


 

 
    
