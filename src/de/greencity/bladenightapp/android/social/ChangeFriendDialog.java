package de.greencity.bladenightapp.android.social;

import de.greencity.bladenightapp.android.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;


public class ChangeFriendDialog extends DialogFragment  {

	public interface ChangeFriendDialogListener {
        void onFinishChangeFriendDialog(String friendsName, int index);
    }
	
    private EditText mEditText;
    private String friendsName;
    private int index;

    public ChangeFriendDialog() {
        // Empty constructor required for DialogFragment
    }
    
    public ChangeFriendDialog(String oldName, int index) {
        friendsName = oldName;
        this.index = index;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_friend_dialog, container);
        mEditText = (EditText) view.findViewById(R.id.change_friends_name);
        getDialog().setTitle("Change name and color of friend");
        
     // Show soft keyboard automatically
        mEditText.requestFocus();
        mEditText.setText(friendsName);
        getDialog().getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        Button confirmButton = (Button) view.findViewById(R.id.changeFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	ChangeFriendDialogListener activity = (ChangeFriendDialogListener) getActivity();
                activity.onFinishChangeFriendDialog(mEditText.getText().toString(),index);
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.changeFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	dismiss();
            }
        });
        
        return view;
    }
    
  
    
   

}