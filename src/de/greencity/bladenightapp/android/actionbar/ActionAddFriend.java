package de.greencity.bladenightapp.android.actionbar;

import android.support.v4.app.FragmentManager;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.social.AddFriendDialog;
import de.greencity.bladenightapp.android.social.SocialActivity;

public class ActionAddFriend implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_add;
    }

	@Override
	public void performAction(View view) {
	    FragmentManager fm = ((SocialActivity) view.getContext()).getSupportFragmentManager();
	    AddFriendDialog addFriendDialog = new AddFriendDialog();
		addFriendDialog.show(fm, "fragment_add_friend");
	}
}
