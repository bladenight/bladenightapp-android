package de.greencity.bladenightapp.android.actionbar.actions;

import android.view.View;
import android.widget.Toast;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;

public class ActionReload extends ActionAugmented {
    @Override
    public int getDrawable() {
        return R.drawable.ic_action_reload;
    }

    @Override
    public void performAction(View view) {
        Toast.makeText(view.getContext(), "Reload should be overriden in the activity", Toast.LENGTH_LONG).show();
    }
}
