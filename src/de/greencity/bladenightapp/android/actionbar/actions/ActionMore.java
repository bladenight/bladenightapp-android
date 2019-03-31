package de.greencity.bladenightapp.android.actionbar.actions;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.actionbar.ActionAugmented;
import de.greencity.bladenightapp.android.mainactivity.MainActivity;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;

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
