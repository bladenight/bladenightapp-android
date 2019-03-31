package de.greencity.bladenightapp.android.actionbar;

import android.content.Context;
import android.content.Intent;

import com.markupartist.android.widget.ActionBar.Action;

public abstract class ActionAugmented implements Action {
    protected void switchToActivity(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
