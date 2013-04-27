package de.greencity.bladenightapp.android.social;

import de.greencity.bladenightapp.dev.android.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;


public class ConfirmFriendDialog extends DialogFragment  {

	public interface ConfirmFriendDialogListener {
        void onFinishConfirmFriendDialog(String friendsName, String code);
    }
	
    public ConfirmFriendDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.confirm_friend_dialog, container);
        editName = (EditText) view.findViewById(R.id.confirm_friendsName);
        editCode = (EditText) view.findViewById(R.id.confirm_code);
        getDialog().setTitle(getResources().getString(R.string.title_friend_confirm));
        
        setDefaultName();
        
     // Show soft keyboard automatically
        editName.requestFocus();
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        Button confirmButton = (Button) view.findViewById(R.id.confirmFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	ConfirmFriendDialogListener activity = (ConfirmFriendDialogListener) getActivity();
                activity.onFinishConfirmFriendDialog(editName.getText().toString(),editCode.getText().toString());
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.confirmFriend_cancel);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	dismiss();
            }
        });
        
        return view;
    }
    
	public void setDefaultName() {
		int friendId = Friends.generateId(getActivity());
		if ( friendId > 0)
			editName.setText(getResources().getString(R.string.default_friend_name)+friendId);
	}

	private EditText editCode;
    private EditText editName;
}