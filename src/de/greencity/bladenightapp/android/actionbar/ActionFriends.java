package de.greencity.bladenightapp.android.actionbar;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.social.SocialActivity;

public class ActionFriends implements Action {

    @Override
    public int getDrawable() {
        return R.drawable.ic_action_users;
    }

	@Override
	public void performAction(View view) {
	    Intent intent = new Intent(view.getContext(), SocialActivity.class);
	    view.getContext().startActivity(intent);
	}
}
