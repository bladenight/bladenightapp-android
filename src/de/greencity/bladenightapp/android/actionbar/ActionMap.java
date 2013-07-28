package de.greencity.bladenightapp.android.actionbar;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;

public class ActionMap implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_globe_dyn;
    }

	@Override
	public void performAction(View view) {
	    Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    view.getContext().startActivity(intent);
	}
}
