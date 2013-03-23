package de.greencity.bladenightapp.android.actionbar;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;

public class ActionMap implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_globe;
    }

	@Override
	public void performAction(View view) {
	    Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
	    view.getContext().startActivity(intent);
	}
}
