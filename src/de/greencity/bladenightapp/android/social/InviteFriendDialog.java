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
    public InviteFriendDialog() {
        // Empty constructor required for DialogFragment
    }


    public interface InviteFriendDialogListener {
        void onFinishInviteFriendDialog(int friendId, String inputText);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_friend_dialog, container);
        editText = (EditText) view.findViewById(R.id.txt_friends_name);
        getDialog().setTitle(getResources().getString(R.string.title_friend_invite));

        generateFriendIdAndName();

        // Show soft keyboard automatically
        editText.requestFocus();
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button confirmButton = (Button) view.findViewById(R.id.inviteFriend_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                InviteFriendDialogListener activity = (InviteFriendDialogListener) getActivity();
                activity.onFinishInviteFriendDialog(friendId, editText.getText().toString());
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

    public void generateFriendIdAndName() {
        friendId = Friends.generateId(getActivity());
        if ( friendId > 0)
            editText.setText(getResources().getString(R.string.default_friend_name)+friendId);
    }

    private int friendId;
    private EditText editText;
    final static public String ARG_PLAUSIBLE_FRIEND_ID = "plausibleFriendId";
}
