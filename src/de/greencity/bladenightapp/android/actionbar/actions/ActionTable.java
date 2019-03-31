package de.greencity.bladenightapp.android.actionbar.actions;

import android.view.View;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionAugmented;
import de.greencity.bladenightapp.android.tableactivity.TableActivity;

public class ActionTable extends ActionAugmented {
    @Override
    public int getDrawable() {
        return R.drawable.ic_view_list;
    }

    @Override
    public void performAction(View view) {
        switchToActivity(view.getContext(), TableActivity.class);
    }
}
