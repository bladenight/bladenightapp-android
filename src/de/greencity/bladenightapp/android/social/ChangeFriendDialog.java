package de.greencity.bladenightapp.android.social;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.Friend.FriendColor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;


public class ChangeFriendDialog extends DialogFragment  {

	public interface ChangeFriendDialogListener {
        void onFinishChangeFriendDialog(Friend friend, int index);
    }
	
    private EditText mEditText;
    private CheckBox activeBox;
    private Friend friend;
    private int index;
    
    private ImageView color_orange;
    private ImageView color_red;
    private ImageView color_blue;
    private ImageView color_green;
    private ImageView color_greenlight;

    public ChangeFriendDialog() {
        // Empty constructor required for DialogFragment
    }
    
    public ChangeFriendDialog(Friend friend, int index) {
        this.friend = friend;
        this.index = index;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_friend_dialog, container);
        
        getDialog().setTitle("Change name and color of friend");
        
        mEditText = (EditText) view.findViewById(R.id.change_friends_name);
        activeBox = (CheckBox) view.findViewById(R.id.friend_active);
        
        color_orange = (ImageView) view.findViewById(R.id.color_orange);
    	color_red = (ImageView) view.findViewById(R.id.color_red);
    	color_blue = (ImageView) view.findViewById(R.id.color_blue);
    	color_green = (ImageView) view.findViewById(R.id.color_green);
    	color_greenlight = (ImageView) view.findViewById(R.id.color_greenlight);
        
        mEditText.setText(friend.getName());
        activeBox.setChecked(friend.getActive()); 
        setColor();

        setButtonListeners(view);
        setColorListeners(view);
        
        return view;
    }
    
    private void setButtonListeners(View view){
    	Button confirmButton = (Button) view.findViewById(R.id.changeFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	ChangeFriendDialogListener activity = (ChangeFriendDialogListener) getActivity();
            	friend.setName(mEditText.getText().toString());
            	friend.setActive(activeBox.isChecked());
                activity.onFinishChangeFriendDialog(friend,index);
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.changeFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	dismiss();
            }
        });
    }
    
  
    private void setColorListeners(View view){
    	
        color_orange.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
            	friend.setColor(FriendColor.ORANGE);
            	setColor();
            }
        });
        
        color_red.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
            	friend.setColor(FriendColor.RED);
            	setColor();
            }
        });
        
        color_blue.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
            	friend.setColor(FriendColor.BLUE);
            	setColor();
            }
        });
        
        color_green.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
            	friend.setColor(FriendColor.GREEN);
            	setColor();
            }
        });
        
        color_greenlight.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View v) {
            	friend.setColor(FriendColor.GREEN_LIGHT);
            	setColor();
            }
        });
    }
    
    private void setColor(){
    	FriendColor color = friend.getColor();
    	color_orange.setImageResource(R.drawable.color_field_off);
    	color_red.setImageResource(R.drawable.color_field_off);
    	color_blue.setImageResource(R.drawable.color_field_off);
    	color_green.setImageResource(R.drawable.color_field_off);
    	color_greenlight.setImageResource(R.drawable.color_field_off);
    	if(color.equals(FriendColor.ORANGE)){
    		color_orange.setImageResource(R.drawable.color_field);
    	}
    	if(color.equals(FriendColor.RED)){
    		color_red.setImageResource(R.drawable.color_field);
    	}
    	if(color.equals(FriendColor.BLUE)){
    		color_blue.setImageResource(R.drawable.color_field);
    	}
    	if(color.equals(FriendColor.GREEN)){
    		color_green.setImageResource(R.drawable.color_field);
    	}
    	if(color.equals(FriendColor.GREEN_LIGHT)){
    		color_greenlight.setImageResource(R.drawable.color_field);
    	}
    }
   

}