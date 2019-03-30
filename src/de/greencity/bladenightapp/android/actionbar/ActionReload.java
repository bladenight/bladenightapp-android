package de.greencity.bladenightapp.android.actionbar;

import android.view.View;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;

public class ActionReload implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_reload;
    }

    @Override
    public void performAction(View view) {
        Toast.makeText(view.getContext(), "Example action", Toast.LENGTH_LONG).show();
    }
}
