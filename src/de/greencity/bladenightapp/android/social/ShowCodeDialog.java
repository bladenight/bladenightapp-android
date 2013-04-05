package de.greencity.bladenightapp.android.social;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;


public class ShowCodeDialog extends DialogFragment  {

	private String friendName;
	private String code;
	
    public ShowCodeDialog() {
        // Empty constructor required for DialogFragment
    }
    
    public ShowCodeDialog(String friendName, String code) {
        this.friendName = friendName;
        this.code = code;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_code_dialog, container);
       
        getDialog().setTitle("Give this code to " + friendName);
        
        TextView codetext = (TextView) view.findViewById(R.id.code_text);
        codetext.setText(code);
        
        Button confirmButton = (Button) view.findViewById(R.id.show_code_confirm);
        confirmButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        
        
        return view;
    }
    
  
    
   

}