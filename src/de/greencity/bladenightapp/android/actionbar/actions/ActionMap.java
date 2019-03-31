package de.greencity.bladenightapp.android.actionbar.actions;

import android.view.View;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;

public class ActionMap extends ActionAugmented {
    @Override
    public int getDrawable() {
        return R.drawable.ic_map_white;
    }

    @Override
    public void performAction(View view) {
        switchToActivity(view.getContext(), BladenightMapActivity.class);
    }
}
