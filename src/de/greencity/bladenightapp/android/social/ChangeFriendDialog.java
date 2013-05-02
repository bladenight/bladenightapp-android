package de.greencity.bladenightapp.android.social;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
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


@SuppressLint("UseSparseArrays")
public class ChangeFriendDialog extends DialogFragment  {

	public interface ChangeFriendDialogListener {
        void onFinishChangeFriendDialog(Friend friend, int friendId);
    }

    public ChangeFriendDialog() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	this.friend = (Friend)getArguments().getSerializable(KEY_FRIENDOBJ);
    	this.friendId = getArguments().getInt(KEY_FRIENDID);

    	friendColorsHelper = new FriendColorsHelper(getActivity());

        rootView = inflater.inflate(R.layout.change_friend_dialog, container);
        
        getDialog().setTitle(getResources().getString(R.string.title_friend_change));
        
        mEditText = (EditText) rootView.findViewById(R.id.change_friends_name);
        activeBox = (CheckBox) rootView.findViewById(R.id.friend_active);
        
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
                activity.onFinishChangeFriendDialog(friend,friendId);
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
    	for (final Integer colorIndex : colorIndexToViewId.keySet() ) {
    		ImageView imageView = getImageView(colorIndex);
    		imageView.setOnClickListener(new ImageView.OnClickListener() {
                public void onClick(View view) {
                	friend.setColor(friendColorsHelper.getIndexedColor(colorIndex));
                	setColor();
                }
            });
    	}
        
    }
    
    private ImageView getImageView(int index) {
		return (ImageView) rootView.findViewById(colorIndexToViewId.get(index));
    }
    
    private void setColor() {
    	int currentColor = friend.getColor();

    	for (final Integer colorIndex : colorIndexToViewId.keySet() ) {
    		getImageView(colorIndex).setImageResource(R.drawable.color_field_off);
    	}
    	int index = friendColorsHelper.getIndexOfColor(currentColor);
    	if ( index > 0 )
    		getImageView(index).setImageResource(R.drawable.color_field);
    }
   
	private View rootView;
	
    private EditText mEditText;
    private CheckBox activeBox;
    private Friend friend;
    private int friendId;
    private FriendColorsHelper friendColorsHelper;
    
    public static final String KEY_FRIENDOBJ = "friend";
    public static final String KEY_FRIENDID = "index";
	private static Map<Integer, Integer> colorIndexToViewId;
    static {
    	colorIndexToViewId = new HashMap<Integer,Integer>();
        int i = 1;
        colorIndexToViewId.put(i++, R.id.color_friend1);
        colorIndexToViewId.put(i++, R.id.color_friend2);
        colorIndexToViewId.put(i++, R.id.color_friend3);
        colorIndexToViewId.put(i++, R.id.color_friend4);
        colorIndexToViewId.put(i++, R.id.color_friend5);
        colorIndexToViewId.put(i++, R.id.color_friend6);
    }

}