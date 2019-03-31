package de.greencity.bladenightapp.android.actionbar.actions;

import android.content.Intent;
import android.view.View;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;
import de.greencity.bladenightapp.android.social.SocialActivity;

public class ActionFriends extends ActionAugmented {

    @Override
    public int getDrawable() {
        return R.drawable.ic_action_users;
    }

    @Override
    public void performAction(View view) {
        switchToActivity(view.getContext(), SocialActivity.class);
    }
}
