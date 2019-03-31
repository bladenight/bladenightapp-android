package de.greencity.bladenightapp.android.actionbar.actions;

import android.content.Intent;
import android.view.View;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;
import de.greencity.bladenightapp.android.options.OptionsActivity;

public class ActionOptions extends ActionAugmented {
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
