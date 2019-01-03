package de.greencity.bladenightapp.android.actionbar;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.options.OptionsActivity;

public class ActionOptions implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_gear;
    }

    @Override
    public void performAction(View view) {
        Intent intent = new Intent(view.getContext(), OptionsActivity.class);
        view.getContext().startActivity(intent);
    }
}
