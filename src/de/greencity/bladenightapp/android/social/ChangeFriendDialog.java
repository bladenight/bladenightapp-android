package de.greencity.bladenightapp.android.social;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import de.greencity.bladenightapp.dev.android.R;


public class ChangeFriendDialog extends DialogFragment  {

	public interface ChangeFriendDialogListener {
        void onFinishChangeFriendDialog(Friend friend, int index);
    }
	
	private View rootView;
	
    private EditText mEditText;
    private CheckBox activeBox;
    private Friend friend;
    private int index;
    
    private ImageView color_friend1;
    private ImageView color_friend2;
    private ImageView color_friend3;
    private ImageView color_friend4;
    private ImageView color_friend5;
    private ImageView color_friend6;

    public static final String KEY_FRIENDOBJ = "friend";
    public static final String KEY_FRIENDID = "index";
    
    public ChangeFriendDialog() {
        // Empty constructor required for DialogFragment
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	this.friend = (Friend)getArguments().getSerializable(KEY_FRIENDOBJ);
    	this.index = getArguments().getInt(KEY_FRIENDID);
    	
        rootView = inflater.inflate(R.layout.change_friend_dialog, container);
        
        getDialog().setTitle("Change name and color of friend");
        
        mEditText = (EditText) rootView.findViewById(R.id.change_friends_name);
        activeBox = (CheckBox) rootView.findViewById(R.id.friend_active);
        
        color_friend1 = (ImageView) rootView.findViewById(R.id.color_friend1);
    	color_friend2 = (ImageView) rootView.findViewById(R.id.color_friend2);
    	color_friend3 = (ImageView) rootView.findViewById(R.id.color_friend3);
    	color_friend4 = (ImageView) rootView.findViewById(R.id.color_friend4);
    	color_friend5 = (ImageView) rootView.findViewById(R.id.color_friend5);
    	color_friend6 = (ImageView) rootView.findViewById(R.id.color_friend6);
        
        mEditText.setText(friend.getName());
        activeBox.setChecked(friend.isActive()); 
        setColor();

        setButtonListeners(rootView);
        setColorListeners(rootView);
        
        return rootView;
    }
    
    private void setButtonListeners(View view){
    	Button confirmButton = (Button) view.findViewById(R.id.changeFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
            	ChangeFriendDialogListener activity = (ChangeFriendDialogListener) getActivity();
            	friend.setName(mEditText.getText().toString());
            	friend.isActive(activeBox.isChecked());
                activity.onFinishChangeFriendDialog(friend,index);
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.changeFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
            	dismiss();
            }
        });
    }
    
  
    private void setColorListeners(View view){
    	
        color_friend1.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend1));
            	setColor();
            }
        });
        
        color_friend2.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend2));
            	setColor();
            }
        });
        
        color_friend3.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend3));
            	setColor();
            }
        });
        
        color_friend4.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend4));
            	setColor();
            }
        });
        
        color_friend5.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend5));
            	setColor();
            }
        });

        color_friend6.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	friend.setColor(view.getResources().getColor(R.color.new_friend6));
            	setColor();
            }
        });
    }
    
    private void setColor() {
    	int currentColor = friend.getColor();
    	
    	color_friend1.setImageResource(R.drawable.color_field_off);
    	color_friend2.setImageResource(R.drawable.color_field_off);
    	color_friend3.setImageResource(R.drawable.color_field_off);
    	color_friend4.setImageResource(R.drawable.color_field_off);
    	color_friend5.setImageResource(R.drawable.color_field_off);
    	color_friend6.setImageResource(R.drawable.color_field_off);
    	
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend1)){
    		color_friend1.setImageResource(R.drawable.color_field);
    	}
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend2)){
    		color_friend2.setImageResource(R.drawable.color_field);
    	}
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend3)){
    		color_friend3.setImageResource(R.drawable.color_field);
    	}
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend4)){
    		color_friend4.setImageResource(R.drawable.color_field);
    	}
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend5)){
    		color_friend5.setImageResource(R.drawable.color_field);
    	}
    	if(currentColor == rootView.getResources().getColor(R.color.new_friend6)){
    		color_friend6.setImageResource(R.drawable.color_field);
    	}
    }
   

}