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
import de.greencity.bladenightapp.android.color_picker.*;


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
    	friendColorsHelper.setCustomColor(friend.getColor());
    	
    	activeColor = friend.getColor();

        rootView = inflater.inflate(R.layout.change_friend_dialog, container);
        getImageView(FriendColorsHelper.customColorIndex).setBackgroundColor(friend.getColor());
        
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
            	friend.setColor(activeColor);
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
                	activeColor = friendColorsHelper.getIndexedColor(colorIndex);
                	setColor();
                }
            });
    	}
    	final ImageView custom_color_view = (ImageView) rootView.findViewById(R.id.color_costum);
    	custom_color_view.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
            	highlightColorBlock(FriendColorsHelper.customColorIndex);
        		ColorPickerDialog dialog = new ColorPickerDialog(view.getContext(), 
        				friendColorsHelper.getIndexedColor(FriendColorsHelper.customColorIndex),
                        new ColorPickerDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(ColorPickerDialog dialog, int color) {
                            friendColorsHelper.setCustomColor(color);
                            custom_color_view.setBackgroundColor(color);  
                            activeColor = friendColorsHelper.getIndexedColor(FriendColorsHelper.customColorIndex);
                            setColor();
                    }
                            
                    @Override
                    public void onCancel(ColorPickerDialog dialog) {
                    		setColor();
                    }
                });

                dialog.show();
            	
            }
        });
        
    }
    
    private ImageView getImageView(int index) {
		return (ImageView) rootView.findViewById(colorIndexToViewId.get(index));
    }
    
    //to set the highlight frame
    private void setColor() {
    	int index = friendColorsHelper.getIndexOfColor(activeColor);
    	highlightColorBlock(index);
    }
    
    private void highlightColorBlock(int index){
    	for (final Integer colorIndex : colorIndexToViewId.keySet() ) {
    		getImageView(colorIndex).setImageResource(R.drawable.color_field_off);
    	}
    	if ( index > 0 || index==FriendColorsHelper.customColorIndex)
    		getImageView(index).setImageResource(R.drawable.color_field);
    }
   
	private View rootView;
	
    private EditText mEditText;
    private CheckBox activeBox;
    private Friend friend;
    private int friendId;
    private FriendColorsHelper friendColorsHelper;
    private int activeColor;
    
    public static final String KEY_FRIENDOBJ = "friend";
    public static final String KEY_FRIENDID = "friendId";
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
        colorIndexToViewId.put(FriendColorsHelper.customColorIndex, R.id.color_costum);
    }

}