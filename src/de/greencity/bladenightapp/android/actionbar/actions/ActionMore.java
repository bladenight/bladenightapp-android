package de.greencity.bladenightapp.android.actionbar.actions;

import android.view.View;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;

public class ActionMore extends ActionAugmented {
    @Override
    public int getDrawable() {
        return R.drawable.ic_more_vert;
    }

    @Override
    public void performAction(View view) {
        // Overriden in main activity
    }
}
