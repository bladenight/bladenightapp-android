package de.greencity.bladenightapp.android.social;

import java.util.ArrayList;
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
import android.widget.TextView;

public class FriendListAdapter extends BaseAdapter {
    private List<Friend> friends;
    private static LayoutInflater inflater=null;
 
    public FriendListAdapter(Activity activity, List<Friend> friends) {
        this.friends = friends;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return friends.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.friend_list_row, null);
 
        TextView name = (TextView)vi.findViewById(R.id.row_friend_name); 
        TextView status = (TextView)vi.findViewById(R.id.row_friend_status); 
        //TextView counter = (TextView)vi.findViewById(R.id.bn_counter);
        ImageView color_block=(ImageView)vi.findViewById(R.id.color_block); 
 
       
        Friend friend = friends.get(position);
 
        // Setting all values in listview
        name.setText(friend.getName());
        String statustext = "active";
        if(!friend.getActive()) statustext = "inactive";
        status.setText(statustext);
        color_block.setBackgroundColor(vi.getResources().getColor(ColorToInt(friend.getColor())));
        return vi;
    }
    
    private int ColorToInt(FriendColor color){
    	int exit = 0;
    	if(color.equals(FriendColor.ORANGE)) exit = R.color.bn_orange;
    	if(color.equals(FriendColor.RED)) exit = R.color.bn_red;
    	if(color.equals(FriendColor.BLUE)) exit = R.color.bn_blue;
    	if(color.equals(FriendColor.GREEN)) exit = R.color.bn_green;
    	if(color.equals(FriendColor.GREEN_LIGHT)) exit = R.color.bn_green_light;
    	return exit;
    }
}


 

 
    
