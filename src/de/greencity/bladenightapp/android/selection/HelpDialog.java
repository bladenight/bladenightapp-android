package de.greencity.bladenightapp.android.selection;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.dev.android.R;


public class HelpDialog extends DialogFragment  {
    public HelpDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.help_dialog, container);
        getDialog().setTitle(getResources().getString(R.string.title_help));
        setButtonListeners(rootView);
        TextView text = (TextView) rootView.findViewById(R.id.help_text);
        text.setMovementMethod(new ScrollingMovementMethod());

        return rootView;
    }

    private void setButtonListeners(View view){
        Button confirmButton = (Button) view.findViewById(R.id.help_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private View rootView;
}