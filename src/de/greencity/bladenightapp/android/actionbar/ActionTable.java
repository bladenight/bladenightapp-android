package de.greencity.bladenightapp.android.actionbar;

import android.content.Intent;
import android.view.View;

import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.TableActivity;
import de.greencity.bladenightapp.android.mainactivity.MainActivity;
import de.greencity.bladenightapp.dev.android.R;

public class ActionTable implements Action {
    @Override
    public int getDrawable() {
        return R.drawable.ic_view_list;
    }

    @Override
    public void performAction(View view) {
        Intent intent = new Intent(view.getContext(), TableActivity.class);
        view.getContext().startActivity(intent);
    }
}
