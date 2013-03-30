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


public class InviteFriendDialog extends DialogFragment  {

	public interface InviteFriendDialogListener {
        void onFinishInviteFriendDialog(String inputText);
    }
	
    private EditText mEditText;

    public InviteFriendDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_friend_dialog, container);
        mEditText = (EditText) view.findViewById(R.id.txt_friends_name);
        getDialog().setTitle("Invite Friend");
        
     // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        Button confirmButton = (Button) view.findViewById(R.id.inviteFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	InviteFriendDialogListener activity = (InviteFriendDialogListener) getActivity();
                activity.onFinishInviteFriendDialog(mEditText.getText().toString());
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.inviteFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	dismiss();
            }
        });
        
        return view;
    }
    
  
    
   

}