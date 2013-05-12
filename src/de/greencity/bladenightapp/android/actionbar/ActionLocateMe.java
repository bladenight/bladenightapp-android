package de.greencity.bladenightapp.android.actionbar;

import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;

public class ActionLocateMe implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_location;
    }

	@Override
	public void performAction(View view) {
	}
}
