package de.greencity.bladenightapp.android.social;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.greencity.bladenightapp.android.R;


public class AddFriendDialog extends DialogFragment  {

    public AddFriendDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_friend_dialog, container);

        getDialog().setTitle(getResources().getString(R.string.title_friend_add));

        Button confirmButton = (Button) view.findViewById(R.id.addFriend);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                InviteFriendDialog inviteFriendDialog = new InviteFriendDialog();
                inviteFriendDialog.show(fm, "fragment_confirm_friend");
                dismiss();
            }
        });
        Button cancelButton = (Button) view.findViewById(R.id.confirmFriend);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                ConfirmFriendDialog confirmFriendDialog = new ConfirmFriendDialog();
                confirmFriendDialog.show(fm, "fragment_confirm_friend");
                dismiss();
            }
        });

        return view;
    }
}