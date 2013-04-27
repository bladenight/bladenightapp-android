package de.greencity.bladenightapp.android.social;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.dev.android.R;


public class DeleteFriendDialog extends DialogFragment  {

	public interface DeleteFriendDialogListener {
        void onFinishDeleteFriendDialog(String friendName, int index);
    }
	
	private View rootView;
	
    private TextView text;
    private Friend friend;
    private int index;
    
    
    public static final String KEY_FRIENDOBJ = "friend";
    public static final String KEY_FRIENDID = "index";
    
    public DeleteFriendDialog() {
        // Empty constructor required for DialogFragment
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	this.friend = (Friend)getArguments().getSerializable(KEY_FRIENDOBJ);
    	this.index = getArguments().getInt(KEY_FRIENDID);
    	
        rootView = inflater.inflate(R.layout.delete_friend_dialog, container);
        
        getDialog().setTitle(getResources().getString(R.string.title_friend_delete));
        
        text = (TextView) rootView.findViewById(R.id.deleteFriend_text);
        
        
        text.setText(getResources().getString(R.string.text_friend_delete) + friend.getName() + "?");

        setButtonListeners(rootView);
        
        return rootView;
    }
    
    private void setButtonListeners(View view){
    	Button confirmButton = (Button) view.findViewById(R.id.deleteFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
            	DeleteFriendDialogListener activity = (DeleteFriendDialogListener) getActivity();
                activity.onFinishDeleteFriendDialog(friend.getName(),index);
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.deleteFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
            	dismiss();
            }
        });
    }
    
  
    

}